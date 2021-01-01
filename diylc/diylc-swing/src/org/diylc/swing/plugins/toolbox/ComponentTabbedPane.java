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
package org.diylc.swing.plugins.toolbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
import org.diylc.common.Favorite;
import org.diylc.common.Favorite.FavoriteType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.lang.LangUtil;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.tree.TreePanel;
import org.diylc.swing.plugins.tree.TreePanel.SelectAllAction;

/**
 * Tabbed pane that shows all available components categorized into tabs.
 * 
 * @author Branislav Stojkovic
 */
class ComponentTabbedPane extends JTabbedPane {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ComponentTabbedPane.class);

  public static String LAST_SELECTED_TAB = "lastSelectedTab";

  private final IPlugInPort plugInPort;
  private Container recentToolbar;
  private Container buildingBlocksToolbar;
  private Container favoritesToolbar;

  private Map<String, ComponentType> typesByClass;

  private List<Favorite> favorites;
  private List<String> pendingRecentComponents = null;
  private List<String> blocks;

  public ComponentTabbedPane(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;

    initialize();

    // layout UI
    addTab(LangUtil.translate("(Favorites)"), createFavoritesPanel());
    addTab(LangUtil.translate("(Recently Used)"), createRecentComponentsPanel());
    addTab(LangUtil.translate("(Building Blocks)"), createBuildingBlocksPanel());
    Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
    List<String> categories = new ArrayList<String>(componentTypes.keySet());
    Collections.sort(categories);
    for (String category : categories) {
      JPanel panel = createTab((componentTypes.get(category)));
      addTab(category, panel);
    }

    // restore last selected tab
    int lastSelectedTab = ConfigurationManager.getInstance().readInt(LAST_SELECTED_TAB, -1);
    if (lastSelectedTab >= 0)
      setSelectedIndex(lastSelectedTab);

    addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        ConfigurationManager.getInstance().writeValue(LAST_SELECTED_TAB, getSelectedIndex());
        ComponentTabbedPane.this.plugInPort.setNewComponentTypeSlot(null, null, false);
        // Refresh recent components if needed
        if (pendingRecentComponents != null) {
          refreshRecentComponentsToolbar(pendingRecentComponents);
          getRecentToolbar().invalidate();
          pendingRecentComponents = null;
        }
      }
    });


  }

  @SuppressWarnings("unchecked")
  private void initialize() {
    Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
    this.typesByClass = new HashMap<String, ComponentType>();
    for (Map.Entry<String, List<ComponentType>> e : componentTypes.entrySet())
      for (ComponentType c : e.getValue())
        typesByClass.put(c.getInstanceClass().getCanonicalName(), c);

    // load building blocks
    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.BLOCKS_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            Map<String, List<IDIYComponent<?>>> newBlocks =
                (Map<String, List<IDIYComponent<?>>>) value;
            if (newBlocks != null) {
              List<String> blockNames = new ArrayList<String>(newBlocks.keySet());
              Collections.sort(blockNames);
              if (!blockNames.equals(ComponentTabbedPane.this.blocks)) {
                LOG.info("Detected block change");
                ComponentTabbedPane.this.blocks = blockNames;
                refreshBuildingBlocksToolbar();
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

    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.FAVORITES_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            List<Favorite> newFavorites = (List<Favorite>) value;
            if (newFavorites != null && !newFavorites.equals(ComponentTabbedPane.this.favorites)) {
              LOG.info("Detected favorites change");
              ComponentTabbedPane.this.favorites = new ArrayList<Favorite>(newFavorites);
              refreshFavoritesToolbar();
            } else
              LOG.info("Detected no favorites change");
          }
        });
    this.favorites = new ArrayList<Favorite>((List<Favorite>) ConfigurationManager.getInstance()
        .readObject(IPlugInPort.FAVORITES_KEY, new ArrayList<Favorite>()));
  }

  private JPanel createTab(List<ComponentType> componentTypes) {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setOpaque(false);
    panel.add(createComponentPanel(componentTypes), BorderLayout.CENTER);

    return panel;
  }

  public Container getRecentToolbar() {
    if (recentToolbar == null) {
      recentToolbar = new Container();
      recentToolbar.setLayout(new BoxLayout(recentToolbar, BoxLayout.X_AXIS));
    }
    return recentToolbar;
  }

  public Container getBuildingBlocksToolbar() {
    if (buildingBlocksToolbar == null) {
      buildingBlocksToolbar = new Container();
      buildingBlocksToolbar.setLayout(new BoxLayout(buildingBlocksToolbar, BoxLayout.X_AXIS));
    }
    return buildingBlocksToolbar;
  }

  public Container getFavoritesToolbar() {
    if (favoritesToolbar == null) {
      favoritesToolbar = new Container();
      favoritesToolbar.setLayout(new BoxLayout(favoritesToolbar, BoxLayout.X_AXIS));
    }
    return favoritesToolbar;
  }

  private Component createComponentPanel(List<ComponentType> componentTypes) {
    Container toolbar = new Container();
    Collections.sort(componentTypes, ComparatorFactory.getInstance().getComponentTypeComparator());
    toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
    for (ComponentType componentType : componentTypes) {
      try {
        Component button = ComponentButtonFactory.create(plugInPort, componentType,
            createVariantPopup(componentType));
        toolbar.add(button);
      } catch (Exception e) {
        LOG.error("Could not create recent component button for " + componentType.getName(), e);
      }
    }

    return new ToolbarScrollPane(toolbar);
  }

  @SuppressWarnings("unchecked")
  private Component createRecentComponentsPanel() {
    final Container toolbar = getRecentToolbar();
    refreshRecentComponentsToolbar((List<String>) ConfigurationManager.getInstance()
        .readObject(IPlugInPort.RECENT_COMPONENTS_KEY, new ArrayList<String>()));
    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.RECENT_COMPONENTS_KEY,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            // Cache the new list, we'll refresh when there's a
            // chance
            pendingRecentComponents = (List<String>) value;
          }
        });

    JPanel panel = new ToolbarScrollPane(toolbar);

    return panel;
  }


  private Component createBuildingBlocksPanel() {
    final Container toolbar = getBuildingBlocksToolbar();

    JPanel panel = new ToolbarScrollPane(toolbar);

    refreshBuildingBlocksToolbar();

    return panel;
  }

  private Component createFavoritesPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);

    final Container toolbar = getFavoritesToolbar();

    panel.add(toolbar, BorderLayout.CENTER);

    refreshFavoritesToolbar();

    return panel;
  }


  @SuppressWarnings("unchecked")
  private void refreshRecentComponentsToolbar(List<String> recentComponentClassList) {
    Container toolbar = getRecentToolbar();
    toolbar.removeAll();
    for (String componentClassName : recentComponentClassList) {
      ComponentType componentType;
      try {
        componentType = ComponentProcessor.getInstance().extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) Class.forName(componentClassName));
        Component button = ComponentButtonFactory.create(plugInPort, componentType,
            createVariantPopup(componentType));
        toolbar.add(button);
      } catch (Exception e) {
        LOG.error("Could not create recent component button for " + componentClassName, e);
      }
    }
  }

  private void refreshBuildingBlocksToolbar() {
    Container toolbar = getBuildingBlocksToolbar();
    toolbar.removeAll();
    for (String block : this.blocks) {
      Component button = ComponentButtonFactory.createBuildingBlockButton(plugInPort, block);
      toolbar.add(button);
    }
  }

  private void refreshFavoritesToolbar() {
    Container toolbar = getFavoritesToolbar();
    toolbar.removeAll();
    for (Favorite fav : this.favorites) {
      Component button;
      if (fav.getType() == FavoriteType.Block) {
        button = ComponentButtonFactory.createBuildingBlockButton(plugInPort, fav.getName());
      } else {
        ComponentType componentType = this.typesByClass.get(fav.getName());
        button = ComponentButtonFactory.create(plugInPort, componentType,
            createVariantPopup(componentType));
      }
      toolbar.add(button);
    }
  }

  private JPopupMenu createVariantPopup(final ComponentType componentType) {
    final JPopupMenu variantPopup = new JPopupMenu();
    variantPopup.add("Loading...");
    variantPopup.addPopupMenuListener(new PopupMenuListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        variantPopup.removeAll();

        final String identifier = componentType.getInstanceClass().getCanonicalName();

        JMenu shortcutSubmenu = new JMenu("Assign Shortcut");
        final JRadioButtonMenuItem  noneItem = new JRadioButtonMenuItem ("None");
        noneItem.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            HashMap<String, String> map = (HashMap<String, String>) ConfigurationManager
                .getInstance().readObject(TreePanel.COMPONENT_SHORTCUT_KEY, null);
            if (map == null)
              map = new HashMap<String, String>();

            Iterator<Entry<String, String>> it = map.entrySet().iterator();
            while (it.hasNext()) {
              Entry<String, String> item = it.next();
              if (item.getValue().equals(identifier))
                it.remove();
            }

            ConfigurationManager.getInstance().writeValue(TreePanel.COMPONENT_SHORTCUT_KEY, map);
          }
        });

        HashMap<String, String> map = (HashMap<String, String>) ConfigurationManager
            .getInstance().readObject(TreePanel.COMPONENT_SHORTCUT_KEY, null);
        noneItem.setSelected(map == null);
        shortcutSubmenu.add(noneItem);

        for (int i = 1; i <= 12; i++) {
          final JRadioButtonMenuItem item = new JRadioButtonMenuItem("F" + i);
          item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              HashMap<String, String> map = (HashMap<String, String>) ConfigurationManager
                  .getInstance().readObject(TreePanel.COMPONENT_SHORTCUT_KEY, null);
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

              ConfigurationManager.getInstance().writeValue(TreePanel.COMPONENT_SHORTCUT_KEY, map);
            }
          });
          
          boolean selected;
        
          if (map == null)
            selected = false;
          else
            selected = map.containsKey(item.getText()) && map.get(item.getText()).equals(identifier);        
          
          item.setSelected(selected);
          shortcutSubmenu.add(item);
        }

        final Favorite fav =
            new Favorite(FavoriteType.Component, componentType.getInstanceClass().getCanonicalName());
        final boolean isFavorite = favorites != null && favorites.indexOf(fav) >= 0;
        final JMenuItem favoritesItem =
            new JMenuItem(isFavorite ? "Remove From Favorites" : "Add To Favorites",
                isFavorite ? IconLoader.StarBlue.getIcon() : IconLoader.StarGrey.getIcon());
        favoritesItem.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            List<Favorite> favorites = new ArrayList<Favorite>(ComponentTabbedPane.this.favorites);
            if (isFavorite) {
              favorites.remove(fav);
            } else {
              favorites.add(fav);
              Collections.sort(favorites);
            }
            ConfigurationManager.getInstance().writeValue(IPlugInPort.FAVORITES_KEY, favorites);
          }
        });
        variantPopup.add(favoritesItem);

        variantPopup.add(new SelectAllAction(plugInPort, componentType));
        variantPopup.add(shortcutSubmenu);

        List<Template> variants = plugInPort.getVariantsFor(componentType);
        if (variants == null || variants.isEmpty()) {
          JMenuItem item = new JMenuItem("<no variants>");
          item.setEnabled(false);
          variantPopup.add(item);
        } else {
          for (Template variant : variants) {
            JMenuItem item =
                ComponentButtonFactory.createVariantItem(plugInPort, variant, componentType);
            variantPopup.add(item);
          }
        }
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {}
    });
    return variantPopup;
  }
}
