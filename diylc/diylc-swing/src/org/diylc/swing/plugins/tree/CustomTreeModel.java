/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.swing.plugins.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
import org.diylc.common.Favorite;
import org.diylc.common.Favorite.FavoriteType;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;

public class CustomTreeModel implements TreeModel {

  private static final Logger LOG = Logger.getLogger(CustomTreeModel.class);

  private IPlugInPort plugInPort;
  private Object root = "root";
  private String searchText = null;
  private Map<String, List<ComponentType>> componentTypes;
  private List<String> categories;
  private List<TreeNode> visibleCategories;
  private Map<String, List<TreeNode>> visibleLeaves;
  private Map<String, ComponentType> typesByClass;

  private List<Favorite> favorites;
  private List<String> recentComponents;
  private List<String> blocks;

  private TreeNode favoritesNode;
  private TreeNode recentNode;
  private TreeNode blocksNode;

  private EventListenerList listenerList = new EventListenerList();

  private static final String FAVORITES = "(Favorites)";
  private static final String RECENTLY_USED = "(Recently Used)";
  private static final String BUILDING_BLOCKS = "(Building Blocks)";

  public CustomTreeModel(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    this.componentTypes = plugInPort.getComponentTypes();
    this.categories = new ArrayList<String>(this.componentTypes.keySet());
    Collections.sort(this.categories);
    this.categories.add(0, BUILDING_BLOCKS);
    this.categories.add(0, RECENTLY_USED);
    this.categories.add(0, FAVORITES);
    this.visibleLeaves = new HashMap<String, List<TreeNode>>();

    this.typesByClass = new HashMap<String, ComponentType>();
    for (Map.Entry<String, List<ComponentType>> e : componentTypes.entrySet())
      for (ComponentType c : e.getValue())
        typesByClass.put(c.getInstanceClass().getCanonicalName(), c);

    this.initialize();
    updateVisibleCategories();
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText.toLowerCase();
    updateVisibleCategories();
  }

  private void updateVisibleCategories() {
    this.visibleCategories = new ArrayList<TreeNode>();
    for (String category : this.categories)
      if (categoryMatches(this.searchText, category))
        this.visibleCategories.add(new TreeNode(category, null, false));

    this.visibleLeaves.clear();

    fireTreeStructureChanged(new Object[] {root});
  }

  private List<TreeNode> getLeavesFor(String category) {
    if (this.visibleLeaves.containsKey(category))
      return this.visibleLeaves.get(category);
    List<TreeNode> visibleTypes = new ArrayList<TreeNode>();

    if (FAVORITES.equals(category)) {
      for (Favorite fav : this.favorites) {
        if (fav.getType() == FavoriteType.Component) {
          ComponentType type = this.typesByClass.get(fav.getName());
          if (this.searchText == null || type.getName().toLowerCase().contains(this.searchText)) {
            visibleTypes.add(new TreeNode(type, new MouseAdapter() {

              @Override
              public void mouseClicked(MouseEvent e) {
                plugInPort.setNewComponentTypeSlot(type, null, null, false);
              }
            }));
          }
        } else {
          // block
          if (this.searchText == null || fav.getName().toLowerCase().contains(this.searchText)) {
            visibleTypes.add(new TreeNode(fav.getName(), new MouseAdapter() {

              long previousActionTime = 0;

              @Override
              public void mouseClicked(MouseEvent e) {
                if (e == null || SwingUtilities.isLeftMouseButton(e)
                    && System.currentTimeMillis() - previousActionTime > 100) {
                  previousActionTime = System.currentTimeMillis();
                  try {
                    plugInPort.loadBlock(fav.getName());
                  } catch (InvalidBlockException e1) {
                    e1.printStackTrace();
                  }
                }
              }
            }, true));
          }
        }
      }
    }

    if (RECENTLY_USED.equals(category)) {
      for (String className : this.recentComponents) {
        ComponentType type = this.typesByClass.get(className);
        if (this.searchText == null || type.getName().toLowerCase().contains(this.searchText)) {
          visibleTypes.add(new TreeNode(type, new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
              plugInPort.setNewComponentTypeSlot(type, null, null, false);
            }
          }));
        }
      }
    }

    if (BUILDING_BLOCKS.equals(category)) {
      for (String block : this.blocks) {
        if (this.searchText == null || block.toLowerCase().contains(this.searchText)) {
          visibleTypes.add(new TreeNode(block, new MouseAdapter() {

            long previousActionTime = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
              if (e == null || SwingUtilities.isLeftMouseButton(e)
                  && System.currentTimeMillis() - previousActionTime > 100) {
                previousActionTime = System.currentTimeMillis();
                try {
                  plugInPort.loadBlock(block);
                } catch (InvalidBlockException e1) {
                  e1.printStackTrace();
                }
              }
            }
          }, true));
        }
      }
    }

    if (componentTypes.containsKey(category))
      for (ComponentType type : componentTypes.get(category))
        if (this.searchText == null
            || category.toLowerCase().contains(this.searchText.toLowerCase()) ||

            typeMatches(this.searchText, type))
          visibleTypes.add(new TreeNode(type, new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
              plugInPort.setNewComponentTypeSlot(type, null, null, false);
            }
          }));
    this.visibleLeaves.put(category, visibleTypes);
    return visibleTypes;
  }

  private boolean typeMatches(String searchText, ComponentType type) {
    if (searchText == null || searchText.isEmpty())
      return true;
    if (type.getName().toLowerCase().contains(searchText))
      return true;
    return false;
  }

  private boolean categoryMatches(String searchText, String category) {
    if (searchText == null || searchText.isEmpty())
      return true;
    if (category.toLowerCase().contains(searchText))
      return true;

    if (FAVORITES.equals(category)) {
      for (Favorite fav : this.favorites) {
        ComponentType type = typesByClass.get(fav.getName());
        if (type.getName().toLowerCase().contains(searchText))
          return true;
      }
    }

    if (RECENTLY_USED.equals(category)) {
      for (String className : this.recentComponents) {
        ComponentType type = typesByClass.get(className);
        if (type.getName().toLowerCase().contains(searchText))
          return true;
      }
    }

    if (BUILDING_BLOCKS.equals(category)) {
      for (String block : this.blocks) {
        if (block.toLowerCase().contains(searchText))
          return true;
      }
    }

    if (componentTypes.containsKey(category))
      for (ComponentType type : componentTypes.get(category))
        if (typeMatches(searchText, type))
          return true;
    return false;
  }

  @Override
  public Object getRoot() {
    return root;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent == root) {
      TreeNode node = visibleCategories.get(index);
      if (RECENTLY_USED.equals(node.getCategory()))
        recentNode = node;
      if (FAVORITES.equals(node.getCategory()))
        favoritesNode = node;
      if (BUILDING_BLOCKS.equals(node.getCategory()))
        blocksNode = node;
      return node;
    }

    List<TreeNode> typesFor = getLeavesFor(parent.toString());
    Object obj = typesFor.get(index);
    return obj;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent == root)
      return visibleCategories.size();
    List<TreeNode> typesFor = getLeavesFor(parent.toString());
    return typesFor.size();
  }

  @Override
  public boolean isLeaf(Object node) {
    if (node == root)
      return false;

    if (node instanceof TreeNode) {
      TreeNode p = (TreeNode) node;
      return p.isLeaf();
    }
    return true;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // read only, nothing to do here
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent == root)
      return visibleCategories.indexOf(child);
    List<TreeNode> typesFor = getLeavesFor(parent.toString());
    return typesFor.indexOf(child);
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(TreeModelListener.class, l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(TreeModelListener.class, l);
  }

  protected void fireTreeStructureChanged(Object[] path) {
    TreeModelEvent event = new TreeModelEvent(this, path);
    EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
    for (int i = 0; i < listeners.length; i++)
      ((TreeModelListener) listeners[i]).treeStructureChanged(event);
  }

  @SuppressWarnings("unchecked")
  private void initialize() {
    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.FAVORITES_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            List<Favorite> newFavorites = (List<Favorite>) value;
            if (newFavorites != null && !newFavorites.equals(CustomTreeModel.this.favorites)) {
              LOG.info("Detected favorites change");
              CustomTreeModel.this.favorites = new ArrayList<Favorite>(newFavorites);
              CustomTreeModel.this.visibleLeaves.remove(FAVORITES);
              fireTreeStructureChanged(new Object[] {root, CustomTreeModel.this.favoritesNode});
            } else
              LOG.info("Detected no favorites change");
          }
        });
    this.favorites = new ArrayList<Favorite>((List<Favorite>) ConfigurationManager.getInstance()
        .readObject(IPlugInPort.FAVORITES_KEY, new ArrayList<Favorite>()));

    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.RECENT_COMPONENTS_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            List<String> newComponents = (List<String>) value;
            if (newComponents != null && !new HashSet<String>(newComponents)
                .equals(new HashSet<String>(CustomTreeModel.this.recentComponents))) {
              LOG.info("Detected recent component change");
              CustomTreeModel.this.recentComponents = new ArrayList<String>(newComponents);
              CustomTreeModel.this.visibleLeaves.remove(RECENTLY_USED);
              fireTreeStructureChanged(new Object[] {root, CustomTreeModel.this.recentNode});
            } else
              LOG.info("Detected no recent component change");
          }
        });
    this.recentComponents = new ArrayList<String>((List<String>) ConfigurationManager.getInstance()
        .readObject(IPlugInPort.RECENT_COMPONENTS_KEY, new ArrayList<String>()));

    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.BLOCKS_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            Map<String, List<IDIYComponent<?>>> newBlocks =
                (Map<String, List<IDIYComponent<?>>>) value;
            if (newBlocks != null) {
              List<String> blockNames = new ArrayList<String>(newBlocks.keySet());
              Collections.sort(blockNames);
              if (!blockNames.equals(CustomTreeModel.this.blocks)) {
                LOG.info("Detected block change");
                CustomTreeModel.this.blocks = blockNames;
                CustomTreeModel.this.visibleLeaves.remove(BUILDING_BLOCKS);
                fireTreeStructureChanged(new Object[] {root, CustomTreeModel.this.blocksNode});
              } else
                LOG.info("Detected no block change");
            } else
              LOG.info("Detected no block change");
          }
        });
    Map<String, List<IDIYComponent<?>>> newBlocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getInstance()
            .readObject(IPlugInPort.BLOCKS_KEY, null);
    if (newBlocks == null)
      this.blocks = new ArrayList<String>();
    else {
      this.blocks = new ArrayList<String>(newBlocks.keySet());
      Collections.sort(this.blocks);
    }
  }
}
