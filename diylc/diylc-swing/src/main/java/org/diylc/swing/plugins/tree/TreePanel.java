/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.viewer.categoryexplorer.TreeModelAdapter;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;

import org.diylc.common.BlockInstantiationMode;
import org.diylc.common.ComponentType;
import org.diylc.common.Favorite;
import org.diylc.common.IBlockProcessor;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Favorite.FavoriteType;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Template;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.SearchTextField;
import org.diylc.utils.IconLoader;
import org.diylc.swing.plugins.toolbox.ComponentButtonFactory;

public class TreePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(TreePanel.class);

  private static final String CLICK_TO_INSTANTIATE =
      LangUtil.translate("Left click to instantiate this component, right click for more options");

  public static final String COMPONENT_SHORTCUT_KEY = "componentShortcuts";

  private JLabel titleLabel;
  private CustomTreeModel treeModel;
  private JTree tree;
  private JScrollPane treeScroll;
  private JTextField searchField;
  private List<Favorite> favorites;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  private JPopupMenu popup;

  /**
   * When the selection listener already ran the leaf action for this path, the following
   * left-button {@code mouseReleased} must not run it again. Cleared after that release or on the
   * next EDT tick (keyboard-only selection).
   */
  private TreePath suppressLeafActivationForPath;

  private int hoveredVariantsButtonRow = -1;
  private boolean variantsButtonPressed;

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
    
//    add(getTitleLabel(), gbc);
//    
//    gbc.gridy++;

    add(getSearchField(), gbc);

    gbc.gridy++;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    gbc.weightx = 1;

    add(getTreeScroll(), gbc);

    setPreferredSize(new Dimension(290, 200));
   
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
  
  public JLabel getTitleLabel() {
    if (titleLabel == null) {
      titleLabel = new JLabel(getName());
      titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
//      titleLabel.setBorder(BorderFactory.createEtchedBorder());
//      titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
    }
    return titleLabel;
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
      tree = new JTree(getTreeModel()) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void processMouseEvent(MouseEvent e) {
          if (!processVariantsButtonEvent(e)) {
            super.processMouseEvent(e);
          }
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
          if (!processVariantsButtonEvent(e)) {
            super.processMouseMotionEvent(e);
          }
        }

        @Override
        public String getToolTipText(MouseEvent e) {
          TreePath path = getVariantsButtonPathAt(e.getX(), e.getY());
          if (path != null) {
            return String.format(LangUtil.translate("Click to choose from %s variants"),
                countVariants(((TreeNode) path.getLastPathComponent()).getComponentType()));
          }
          return super.getToolTipText(e);
        }
      };
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
              final TreePath path = e.getPath();
              suppressLeafActivationForPath = path;
              payload.getClickListener().mouseClicked(null);
              SwingUtilities.invokeLater(() -> {
                if (suppressLeafActivationForPath != null && suppressLeafActivationForPath.equals(path)) {
                  suppressLeafActivationForPath = null;
                }
              });
            }
          }
        }
      });

      tree.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() != 1 || !SwingUtilities.isRightMouseButton(e))
            return;

          int row = tree.getClosestRowForLocation(e.getX(), e.getY());
          tree.setSelectionRow(row);
          getPopup().show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {
          updateVariantsButtonHover(-1, -1);
        }

        /**
         * Prefer release over click: {@code mouseClicked} is not posted if the cursor moves
         * between press and release (common with DnD or hand jitter), while selection may still
         * change - then re-clicks on the same row fire no selection event. Every release is
         * forwarded rather than only the first, so that the listener can tell a double click from
         * a single one; re-arming the slot on the second release is harmless.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
          if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            return;
          }
          TreePath path = tree.getPathForLocation(e.getX(), e.getY());
          if (path == null) {
            path = tree.getClosestPathForLocation(e.getX(), e.getY());
          }
          if (path == null) {
            return;
          }

          if (path.equals(suppressLeafActivationForPath)) {
            suppressLeafActivationForPath = null;
            return;
          }
          if (!path.equals(tree.getSelectionPath())) {
            return;
          }

          Object node = path.getLastPathComponent();
          if (node instanceof TreeNode) {
            TreeNode payload = (TreeNode) node;
            if (payload.getClickListener() != null) {
              payload.getClickListener().mouseClicked(e);
            }
          }
        }
      });

      tree.addMouseMotionListener(new MouseMotionAdapter() {

        @Override
        public void mouseMoved(MouseEvent e) {
          updateVariantsButtonHover(e.getX(), e.getY());
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

          final String identifier = componentType == null
              ? IBlockProcessor.BLOCK_PREFIX + payload.toString()
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

            addVariantItems(popup, componentType, true);
          } else if (getTreeModel().isLeaf(selectedNode)) {
            popup.add(new InsertBlockAsGroupAction(plugInPort, payload.toString()));
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

  private int countVariants(ComponentType componentType) {
    int count = 0;
    if (componentType.getDatasheet() != null) {
      count += componentType.getDatasheet().size();
    }
    List<Template> variants = plugInPort.getVariantsFor(componentType);
    if (variants != null) {
      count += variants.size();
    }
    return count;
  }

  /**
   * Returns the on-screen bounds of the variants button drawn in the row under {@code path}, in
   * tree coordinates, or null when that row has no button. The renderer is a rubber stamp, so it
   * is configured and laid out for the row on demand to find where the button lands.
   */
  private Rectangle getVariantsButtonBounds(TreePath path) {
    Object node = path.getLastPathComponent();
    if (!(node instanceof TreeNode) || ((TreeNode) node).getComponentType() == null) {
      return null;
    }
    int row = tree.getRowForPath(path);
    Rectangle rowBounds = tree.getPathBounds(path);
    if (rowBounds == null) {
      return null;
    }
    ComponentCellRenderer renderer = (ComponentCellRenderer) tree.getCellRenderer();
    renderer.getTreeCellRendererComponent(tree, node, tree.isRowSelected(row),
        tree.isExpanded(row), getTreeModel().isLeaf(node), row, false);
    if (!renderer.variantsButton.isVisible()) {
      return null;
    }
    renderer.setBounds(rowBounds);
    renderer.doLayout();
    Rectangle bounds = renderer.variantsButton.getButtonBounds();
    bounds.translate(rowBounds.x + renderer.variantsButton.getX(),
        rowBounds.y + renderer.variantsButton.getY());
    return bounds;
  }

  private TreePath getVariantsButtonPathAt(int x, int y) {
    TreePath path = tree.getPathForLocation(x, y);
    if (path == null) {
      return null;
    }
    Rectangle bounds = getVariantsButtonBounds(path);
    return bounds != null && bounds.contains(x, y) ? path : null;
  }

  /**
   * A left press on the variants button opens the variants menu and is kept away from the tree
   * UI and the drag recognizer, so it neither selects the row (which would instantiate the
   * component) nor starts a drag. Returns true when the event was handled here.
   */
  private boolean processVariantsButtonEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      variantsButtonPressed = false;
      if (!SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
        return false;
      }
      TreePath path = getVariantsButtonPathAt(e.getX(), e.getY());
      if (path == null) {
        return false;
      }
      variantsButtonPressed = true;
      Rectangle bounds = getVariantsButtonBounds(path);
      ComponentType componentType = ((TreeNode) path.getLastPathComponent()).getComponentType();
      JPopupMenu variantsPopup = new JPopupMenu();
      addVariantItems(variantsPopup, componentType, false);
      variantsPopup.show(tree, bounds.x, bounds.y + bounds.height);
      return true;
    }
    if (variantsButtonPressed && (e.getID() == MouseEvent.MOUSE_RELEASED
        || e.getID() == MouseEvent.MOUSE_CLICKED || e.getID() == MouseEvent.MOUSE_DRAGGED)) {
      return true;
    }
    return false;
  }

  private void updateVariantsButtonHover(int x, int y) {
    int row = -1;
    if (x >= 0 && y >= 0) {
      TreePath path = getVariantsButtonPathAt(x, y);
      if (path != null) {
        row = tree.getRowForPath(path);
      }
    }
    if (row == hoveredVariantsButtonRow) {
      return;
    }
    repaintRow(hoveredVariantsButtonRow);
    hoveredVariantsButtonRow = row;
    repaintRow(row);
    tree.setCursor(row >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null);
  }

  private void repaintRow(int row) {
    if (row < 0) {
      return;
    }
    Rectangle bounds = tree.getRowBounds(row);
    if (bounds != null) {
      tree.repaint(bounds);
    }
  }

  /**
   * Datasheet models first, then saved variants - the same items that the count on the variants
   * button adds up.
   */
  private void addVariantItems(JPopupMenu menu, final ComponentType componentType,
      boolean showEmpty) {
    if (componentType.getDatasheet() != null) {
      List<Component> items = ComponentButtonFactory.createDatasheetItems(plugInPort,
          componentType, new ArrayList<String>(), model -> {
            LOG.info("Creating datasheet model " + String.join(", ", model));
            plugInPort.setNewComponentTypeSlot(componentType, null, model, false);
          });
      for (Component item : items) {
        menu.add(item);
      }
    }

    List<Template> templates = plugInPort.getVariantsFor(componentType);
    if (templates == null || templates.isEmpty()) {
      if (showEmpty) {
        if (componentType.getDatasheet() != null) {
          menu.add(new JSeparator());
        }
        JMenuItem item = new JMenuItem("<no variants>");
        item.setEnabled(false);
        menu.add(item);
      }
    } else {
      if (componentType.getDatasheet() != null) {
        menu.add(new JSeparator());
      }
      for (Template template : templates) {
        JMenuItem item =
            ComponentButtonFactory.createVariantItem(plugInPort, template, componentType);
        menu.add(item);
      }
    }
  }

  public class ComponentCellRenderer extends JPanel implements TreeCellRenderer {

    private static final long serialVersionUID = 1L;

    private DefaultTreeCellRenderer label;
    private VariantsButton variantsButton;

    public ComponentCellRenderer() {
      super(new BorderLayout());
      setOpaque(false);
      label = new DefaultTreeCellRenderer();
      label.setVerticalAlignment(SwingConstants.CENTER);
      label.setVerticalTextPosition(SwingConstants.CENTER);
      variantsButton = new VariantsButton();
      add(label, BorderLayout.CENTER);
      add(variantsButton, BorderLayout.EAST);
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      if (d != null) {
        d.height = Math.max(d.height, 20);
      }
      return d;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value,
        final boolean selected, final boolean expanded, final boolean leaf, final int row,
        final boolean hasFocus) {

      label.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      variantsButton.setVisible(false);
      setToolTipText(null);

      Object obj = value;
      if (obj != null && obj.getClass().equals(TreeNode.class)) {
        TreeNode payload = (TreeNode) obj;
        label.setPreferredSize(null);
        if (payload.getComponentType() == null) {
          if (leaf) {
            label.setIcon(IconLoader.Component.getIcon());
          }
        } else {
          setToolTipText("<html><b>" + payload.getComponentType().getName() + "</b><br>"
              + payload.getComponentType().getDescription() + "<br>Author: "
              + payload.getComponentType().getAuthor() + "<br><br>" + CLICK_TO_INSTANTIATE
              + "</html>");
          label.setIcon(payload.getComponentType().getIcon());
        }

        String shortCutHtml = "";

        HashMap<String, String> shortcutMap = (HashMap<String, String>) ConfigurationManager
            .getInstance().readObject(TreePanel.COMPONENT_SHORTCUT_KEY, null);
        String identifier = payload.getComponentType() == null
            ? IBlockProcessor.BLOCK_PREFIX + payload.toString()
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
          int count = countVariants(payload.getComponentType());
          if (count > 0) {
            variantsButton.setFont(tree.getFont());
            variantsButton.setCount(count);
            variantsButton.setHovered(row == hoveredVariantsButtonRow);
            variantsButton.setVisible(true);
          }
        }

        int viewportWidth = tree.getVisibleRect().width;
        if (viewportWidth <= 0 && tree.getParent() != null) {
          viewportWidth = tree.getParent().getWidth();
        }
        if (viewportWidth <= 0 && tree.getWidth() > 0) {
          viewportWidth = tree.getWidth();
        }
        if (viewportWidth <= 0) {
          viewportWidth = 240;
        }
        // the tree caches row sizes and does not re-measure them when the vertical scroll bar
        // appears, so its width is reserved up front to keep it off the variants button
        JScrollBar scrollBar = getTreeScroll().getVerticalScrollBar();
        if (!scrollBar.isVisible()) {
          viewportWidth -= scrollBar.getPreferredSize().width;
        }

        int overhead = (payload.getComponentType() != null) ? 95 : 45;
        // the button sits beside the text rather than inside it, so it takes its width out of
        // the wrap width to keep the row as wide as before
        if (variantsButton.isVisible()) {
          overhead += variantsButton.getPreferredSize().width;
        }
        int wrapWidth = Math.max(100, viewportWidth - overhead);

        // translate categories
        label.setText("<html><div style=\"width: " + wrapWidth + "px;\">"
            + (payload.getComponentType() == null ? LangUtil.translate(payload.forDisplay())
                : payload.forDisplay())
            + shortCutHtml + "</div></html>");
      }

      // consecutive rows are often stamped at the same size, which CellRendererPane takes as
      // still valid; without this the label would keep the width it had next to the previous
      // row's button
      invalidate();
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

  /**
   * Left click on a building block node inserts it in composite mode (a single rigid component)
   * by default - see design decision D10 in {@code docs/plans/composite-building-blocks.md}.
   * This action offers the one extra click needed to fall back to the original behavior:
   * every component of the block instantiated separately and auto-grouped.
   */
  public static class InsertBlockAsGroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private String blockName;

    public InsertBlockAsGroupAction(IPlugInPort plugInPort, String blockName) {
      super();
      this.plugInPort = plugInPort;
      this.blockName = blockName;
      putValue(AbstractAction.NAME, "Insert as Grouped Components");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      try {
        plugInPort.loadBlock(blockName, BlockInstantiationMode.GROUP);
      } catch (InvalidBlockException e1) {
        LOG.error("Could not find building block: " + blockName, e1);
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

  /**
   * Pill-shaped "+N" drop-down button painted at the end of a component row. It is only ever
   * painted through the cell renderer, so the clicks it appears to take are hit-tested and
   * handled by the tree in {@link TreePanel#processVariantsButtonEvent(MouseEvent)}.
   */
  private static class VariantsButton extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final Color FILL_COLOR = new Color(0xD7FFC6);
    private static final Color HOVER_FILL_COLOR = new Color(0xB8F0A0);
    private static final Color BORDER_COLOR = new Color(0x7DBA63);
    private static final Color TEXT_COLOR = new Color(0x3F6B2C);

    private static final int LEFT_MARGIN = 4;
    private static final int RIGHT_MARGIN = 6;
    private static final int PADDING = 5;
    private static final int ARROW_GAP = 4;
    private static final int ARROW_WIDTH = 7;
    private static final int BUTTON_HEIGHT = 16;

    private String text = "";
    private boolean hovered;

    public void setCount(int count) {
      this.text = "+" + count;
    }

    public void setHovered(boolean hovered) {
      this.hovered = hovered;
    }

    @Override
    public Dimension getPreferredSize() {
      int textWidth = getFontMetrics(getFont()).stringWidth(text);
      return new Dimension(LEFT_MARGIN + PADDING + textWidth + ARROW_GAP + ARROW_WIDTH + PADDING
          + RIGHT_MARGIN, BUTTON_HEIGHT);
    }

    public Rectangle getButtonBounds() {
      return new Rectangle(LEFT_MARGIN, (getHeight() - BUTTON_HEIGHT) / 2,
          getWidth() - LEFT_MARGIN - RIGHT_MARGIN, BUTTON_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      Rectangle b = getButtonBounds();
      g2d.setColor(hovered ? HOVER_FILL_COLOR : FILL_COLOR);
      g2d.fillRoundRect(b.x, b.y, b.width - 1, b.height - 1, b.height, b.height);
      g2d.setColor(BORDER_COLOR);
      g2d.drawRoundRect(b.x, b.y, b.width - 1, b.height - 1, b.height, b.height);

      g2d.setFont(getFont());
      FontMetrics fm = g2d.getFontMetrics();
      int textX = b.x + PADDING;
      int textY = b.y + (b.height - fm.getHeight()) / 2 + fm.getAscent();
      g2d.setColor(TEXT_COLOR);
      g2d.drawString(text, textX, textY);

      int arrowX = textX + fm.stringWidth(text) + ARROW_GAP;
      int arrowY = b.y + b.height / 2 - 1;
      g2d.fillPolygon(new int[] {arrowX, arrowX + ARROW_WIDTH, arrowX + ARROW_WIDTH / 2},
          new int[] {arrowY - 1, arrowY - 1, arrowY + 3}, 3);
      g2d.dispose();
    }
  }
}
