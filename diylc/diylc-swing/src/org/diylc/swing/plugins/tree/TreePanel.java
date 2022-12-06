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
 */
package org.diylc.swing.plugins.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.viewer.categoryexplorer.TreeModelAdapter;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
import org.diylc.common.Favorite;
import org.diylc.common.Favorite.FavoriteType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Template;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.SearchTextField;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.toolbox.ComponentButtonFactory;

public class TreePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(TreePanel.class);

  private static final String CLICK_TO_INSTANTIATE =
      LangUtil.translate("Left click to instantiate this component, right click for more options");

  public static final String COMPONENT_SHORTCUT_KEY = "componentShortcuts";

  private CustomTreeModel treeModel;
  private JTree tree;
  private JScrollPane treeScroll;
  private JTextField searchField;
  private List<Favorite> favorites;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  private JPopupMenu popup;

  @SuppressWarnings("unchecked")
  public TreePanel(IPlugInPort plugInPort, ISwingUI swingUI) {
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    
    setName("Toolbox");
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    gbc.gridwidth = 1;

    add(getSearchField(), gbc);

    gbc.gridy++;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    gbc.weightx = 1;

    add(getTreeScroll(), gbc);

    setPreferredSize(new Dimension(240, 200));
   
    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.FAVORITES_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            List<Favorite> newFavorites = (List<Favorite>) value;
            if (newFavorites != null && !newFavorites.equals(TreePanel.this.favorites)) {
              LOG.info("Detected favorites change");
              TreePanel.this.favorites = new ArrayList<Favorite>(newFavorites);
            } else
              LOG.info("Detected no favorites change");
          }
        });
    this.favorites = (List<Favorite>) ConfigurationManager.getInstance()
        .readObject(IPlugInPort.FAVORITES_KEY, new ArrayList<Favorite>());

    getTree().expandRow(0);

    initializeDnD();
  }

  private void initializeDnD() {
    // Initialize drag source recognizer.
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(getTree(),
        DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK,
        new TreeGestureListener(plugInPort));
    // Initialize drop target.
    new DropTarget(getTree(), DnDConstants.ACTION_COPY_OR_MOVE, new TreeTargetListener(plugInPort),
        true);
  }

  public JScrollPane getTreeScroll() {
    if (treeScroll == null) {
      treeScroll = new JScrollPane(getTree());
      treeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    return treeScroll;
  }  

  public CustomTreeModel getTreeModel() {
    if (treeModel == null) {
      treeModel = new CustomTreeModel(plugInPort);
      treeModel.addTreeModelListener(new TreeModelAdapter() {

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
          String searchText = treeModel.getSearchText();
          
          // when we are searching for something, expand all nodes
          if (searchText != null && !searchText.isEmpty()) {
            int n = treeModel.getChildCount(treeModel.getRoot());
            for (int i = 0; i < n; i++) {
              Object toExpand = treeModel.getChild(treeModel.getRoot(), i);
              tree.expandPath(new TreePath(new Object[] { treeModel.getRoot(), toExpand } ));
            }
          }
        }
      });
    }
    return treeModel;
  }

  public JTree getTree() {
    if (tree == null) {
      tree = new JTree(getTreeModel());
      tree.setRootVisible(false);
      tree.setCellRenderer(new ComponentCellRenderer());
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.setRowHeight(0);
      tree.setToggleClickCount(1);
      ToolTipManager.sharedInstance().registerComponent(tree);

      tree.addTreeSelectionListener(new TreeSelectionListener() {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
          if (!e.isAddedPath())
            return;

          Object node = e.getPath().getLastPathComponent();
          if (node != null && node instanceof TreeNode) {
            TreeNode payload = (TreeNode) node;
            if (payload.getClickListener() != null) {
              payload.getClickListener().mouseClicked(null);
            }
          }
        }
      });

      tree.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() != 1)
            return;

          if (SwingUtilities.isRightMouseButton(e)) {
            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);
            getPopup().show(e.getComponent(), e.getX(), e.getY());
          } else {
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            Object node = path.getLastPathComponent();
            if (node != null && node instanceof TreeNode) {
              TreeNode payload = (TreeNode) node;
              if (payload.getClickListener() != null) {
                payload.getClickListener().mouseClicked(e);
              }
            }
          }
        }
      });
    }
    return tree;
  }

  public JPopupMenu getPopup() {
    if (popup == null) {
      popup = new JPopupMenu();
      popup.add("Loading...");
      popup.addPopupMenuListener(new PopupMenuListener() {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          popup.removeAll();

          Object selectedNode = tree.getLastSelectedPathComponent();

          if (selectedNode == null || !(selectedNode instanceof TreeNode))
            return;

          TreeNode payload = (TreeNode) selectedNode;
          final ComponentType componentType = payload.getComponentType();

          final String identifier = componentType == null ? "block:" + payload.toString()
              : componentType.getInstanceClass().getCanonicalName();

          JMenu shortcutSubmenu = new JMenu("Assign Shortcut");
          final JMenuItem noneItem = new JMenuItem("None");
          noneItem.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
              HashMap<String, String> map = (HashMap<String, String>) ConfigurationManager
                  .getInstance().readObject(COMPONENT_SHORTCUT_KEY, null);
              if (map == null)
                map = new HashMap<String, String>();

              Iterator<Entry<String, String>> it = map.entrySet().iterator();
              while (it.hasNext()) {
                Entry<String, String> item = it.next();
                if (item.getValue().equals(identifier))
                  it.remove();
              }

              ConfigurationManager.getInstance().writeValue(COMPONENT_SHORTCUT_KEY, map);

              TreePanel.this.invalidate();
              TreePanel.this.repaint();
            }

          });

          shortcutSubmenu.add(noneItem);

          for (int i = 1; i <= 12; i++) {
            final JMenuItem item = new JMenuItem("F" + i);
            item.addActionListener(new ActionListener() {

              @SuppressWarnings("unchecked")
              @Override
              public void actionPerformed(ActionEvent e) {
                HashMap<String, String> map = (HashMap<String, String>) ConfigurationManager
                    .getInstance().readObject(COMPONENT_SHORTCUT_KEY, null);
                if (map == null)
                  map = new HashMap<String, String>();

                if (map.containsKey(item.getText()))
                  map.remove(item.getText());

                Iterator<Entry<String, String>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                  Entry<String, String> item = it.next();
                  if (item.getValue().equals(identifier))
                    it.remove();
                }

                map.put(item.getText(), identifier);

                ConfigurationManager.getInstance().writeValue(COMPONENT_SHORTCUT_KEY, map);

                TreePanel.this.invalidate();
                TreePanel.this.repaint();
              }
            });
            shortcutSubmenu.add(item);
          }

          if (getTreeModel().isLeaf(selectedNode)) {
            final Favorite fav =
                new Favorite(componentType == null ? FavoriteType.Block : FavoriteType.Component,
                    componentType == null ? payload.toString()
                        : componentType.getInstanceClass().getCanonicalName());
            final boolean isFavorite = favorites != null && favorites.indexOf(fav) >= 0;
            final JMenuItem favoritesItem =
                new JMenuItem(isFavorite ? "Remove From Favorites" : "Add To Favorites",
                    isFavorite ? IconLoader.StarBlue.getIcon() : IconLoader.StarGrey.getIcon());
            favoritesItem.addActionListener(new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                List<Favorite> favorites = new ArrayList<Favorite>(TreePanel.this.favorites);
                if (isFavorite) {
                  favorites.remove(fav);
                } else {
                  favorites.add(fav);
                  Collections.sort(favorites);
                }
                ConfigurationManager.getInstance().writeValue(IPlugInPort.FAVORITES_KEY, favorites);
              }
            });
            popup.add(favoritesItem);
          }

          if (componentType != null) {
            popup.add(new SelectAllAction(plugInPort, componentType));           
            
            popup.add(shortcutSubmenu);
            popup.add(new JSeparator());
            
            if (componentType.getDatasheet() != null) {
              List<Component> items = ComponentButtonFactory.createDatasheetItems(plugInPort, componentType, new ArrayList<String>(), model -> {
                LOG.info("Creating datasheet model " + String.join(", ", model));
                plugInPort.setNewComponentTypeSlot(componentType, null, model, false);
              });
              for (Component item : items) {
                popup.add(item);
              }
              popup.add(new JSeparator());
            }

            List<Template> templates = plugInPort.getVariantsFor(componentType);
            if (templates == null || templates.isEmpty()) {
              JMenuItem item = new JMenuItem("<no variants>");
              item.setEnabled(false);
              popup.add(item);
            } else {
              for (Template template : templates) {
                JMenuItem item =
                    ComponentButtonFactory.createVariantItem(plugInPort, template, componentType);
                popup.add(item);
              }
            }
          } else if (getTreeModel().isLeaf(selectedNode)) {
            popup.add(shortcutSubmenu);
            popup.add(new DeleteBlockAction(plugInPort, swingUI, payload.toString()));
          }
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {}
      });
    }
    return popup;
  }

  public JTextField getSearchField() {
    if (searchField == null) {
      searchField = new SearchTextField(true, 'Q', text -> {
        CustomTreeModel model = getTreeModel();
        model.setSearchText(text);
      });      
    }
    return searchField;
  }

  public class ComponentCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value,
        final boolean selected, final boolean expanded, final boolean leaf, final int row,
        final boolean hasFocus) {

      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      Object obj = value;
      if (obj != null && obj.getClass().equals(TreeNode.class)) {
        TreeNode payload = (TreeNode) obj;
        if (payload.getComponentType() == null) {
          setToolTipText(null);
          if (leaf)
            setIcon(IconLoader.Component.getIcon());

          setPreferredSize(new Dimension(250, 20));
        } else {
          setToolTipText("<html><b>" + payload.getComponentType().getName() + "</b><br>"
              + payload.getComponentType().getDescription() + "<br>Author: "
              + payload.getComponentType().getAuthor() + "<br><br>" + CLICK_TO_INSTANTIATE
              + "</html>");
          setIcon(payload.getComponentType().getIcon());
          setPreferredSize(new Dimension(250, 32));
        }

        String shortCutHtml = "";
        String variantsHtml = "";

        HashMap<String, String> shortcutMap = (HashMap<String, String>) ConfigurationManager
            .getInstance().readObject(TreePanel.COMPONENT_SHORTCUT_KEY, null);
        String identifier = payload.getComponentType() == null ? "block:" + payload.toString()
            : payload.getComponentType().getInstanceClass().getCanonicalName();
        if (shortcutMap != null && shortcutMap.containsValue(identifier)) {
          for (String key : shortcutMap.keySet()) {
            if (shortcutMap.get(key).equals(identifier)) {
              shortCutHtml =
                  " <a style=\"text-shadow: -1px 0 black, 0 1px black, 1px 0 black, 0 -1px black; background-color: #eeeeee; color: #666666;\">&nbsp;"
                      + key + "&nbsp;</a>";
            }
          }
        }

        if (payload.getComponentType() != null) {
          List<Template> variants = plugInPort.getVariantsFor(payload.getComponentType());
          int count = 0;
          if (payload.getComponentType().getDatasheet() != null) {
            count += payload.getComponentType().getDatasheet().size();
          }
          if (variants != null && !variants.isEmpty()) {
            count += variants.size();
          }
          if (count > 0) {
            variantsHtml =
                " <a style=\"text-shadow: -1px 0 black, 0 1px black, 1px 0 black, 0 -1px black; background-color: #D7FFC6; color: #666666;\">[+"
                    + count + "]</a>";
          }
        }

        // translate categories
        setText("<html>"
            + (payload.getComponentType() == null ? LangUtil.translate(payload.forDisplay())
                : payload.forDisplay())
            + shortCutHtml + variantsHtml + "</html>");
      }

      return this;
    }
  }

  public static class SelectAllAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ComponentType componentType;

    public SelectAllAction(IPlugInPort plugInPort, ComponentType componentType) {
      super();
      this.plugInPort = plugInPort;
      this.componentType = componentType;
      putValue(AbstractAction.NAME, "Select All");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      if (componentType != null) {
        plugInPort.setNewComponentTypeSlot(null, null, null, false);
        List<IDIYComponent<?>> components = plugInPort.getCurrentProject().getComponents();
        List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
        for (IDIYComponent<?> component : components) {
          if (componentType.getInstanceClass().equals(component.getClass())) {
            newSelection.add(component);
          }
        }

        plugInPort.setSelection(newSelection, true);        
      }
    }
  }

  public static class DeleteBlockAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private ISwingUI swingUI;
    private String blockName;

    public DeleteBlockAction(IPlugInPort plugInPort, ISwingUI swingUI, String blockName) {
      super();
      this.plugInPort = plugInPort;
      this.swingUI = swingUI;
      this.blockName = blockName;
      putValue(AbstractAction.NAME, "Delete Building Block");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      if (swingUI.showConfirmDialog(
          "Are you sure you want to delete building block \"" + blockName + "\"?",
          "Delete Building Block", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) == IView.YES_OPTION)
        plugInPort.deleteBlock(blockName);
    }
  }
}
