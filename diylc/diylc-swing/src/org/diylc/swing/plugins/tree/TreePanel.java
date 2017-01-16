package org.diylc.swing.plugins.tree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.images.IconLoader;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.swing.plugins.toolbox.ComponentButtonFactory;

public class TreePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(TreePanel.class);

  private DefaultTreeModel treeModel;
  private JTree tree;
  private JScrollPane treeScroll;
  private JTextField searchField;
  private DefaultMutableTreeNode recentNode;
  private DefaultMutableTreeNode blocksNode;
  private List<String> recentComponents;
  private List<String> blocks;

  private IPlugInPort plugInPort;

  private JPopupMenu popup;

  public TreePanel(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
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

    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.RECENT_COMPONENTS_KEY, new IConfigListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void valueChanged(String key, Object value) {
        List<String> newComponents = (List<String>) value;
        if (newComponents != null
            && !new HashSet<String>(newComponents).equals(new HashSet<String>(TreePanel.this.recentComponents))) {
          LOG.info("Detected recent component change");
          refreshRecentComponents(newComponents);
        } else
          LOG.info("Detected no recent component  change");
        TreePanel.this.recentComponents = new ArrayList<String>(newComponents);
      }
    });

    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.BLOCKS_KEY, new IConfigListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void valueChanged(String key, Object value) {
        Map<String, Collection<IDIYComponent<?>>> newBlocks = (Map<String, Collection<IDIYComponent<?>>>) value;
        if (newBlocks != null) {
          List<String> blockNames = new ArrayList<String>(newBlocks.keySet());
          Collections.sort(blockNames);
          if (!blockNames.equals(TreePanel.this.blocks)) {
            LOG.info("Detected block change");
            refreshBlocks(blockNames);
          } else
            LOG.info("Detected no block change");
        } else
          LOG.info("Detected no block change");
      }
    });

    getTree().expandRow(0);
  }

  public JScrollPane getTreeScroll() {
    if (treeScroll == null) {
      treeScroll = new JScrollPane(getTree());
      treeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    return treeScroll;
  }

  public DefaultMutableTreeNode getRecentNode() {
    if (this.recentNode == null) {
      this.recentNode = new DefaultMutableTreeNode(new Payload("(Recently Used)", null), true);
      @SuppressWarnings("unchecked")
      List<String> recent =
          (List<String>) ConfigurationManager.getInstance().readObject(IPlugInPort.RECENT_COMPONENTS_KEY, null);
      if (recent != null) {
        this.recentComponents = new ArrayList<String>(recent);
        refreshRecentComponents(recent);
      } else {
        this.recentComponents = new ArrayList<String>();
      }
    }
    return this.recentNode;
  }

  public DefaultMutableTreeNode getBlocksNode() {
    if (this.blocksNode == null) {
      this.blocksNode = new DefaultMutableTreeNode(new Payload("(Building Blocks)", null), true);
      @SuppressWarnings("unchecked")
      Map<String, Collection<IDIYComponent<?>>> newBlocks =
          (Map<String, Collection<IDIYComponent<?>>>) ConfigurationManager.getInstance().readObject(
              IPlugInPort.BLOCKS_KEY, null);
      if (newBlocks != null) {
        List<String> blockNames = new ArrayList<String>(newBlocks.keySet());
        Collections.sort(blockNames);
        refreshBlocks(blockNames);
        this.blocks = blockNames;
      } else {
        this.blocks = new ArrayList<String>();
      }
    }
    return this.blocksNode;
  }

  @SuppressWarnings("unchecked")
  private void refreshRecentComponents(List<String> recentComponentClassList) {
    getRecentNode().removeAllChildren();
    for (String componentClassName : recentComponentClassList) {
      final ComponentType componentType;
      try {
        componentType =
            ComponentProcessor.getInstance().extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) Class.forName(componentClassName));
        Payload payload = new Payload(componentType, new MouseAdapter() {

          @Override
          public void mouseClicked(MouseEvent e) {
            plugInPort.setNewComponentTypeSlot(componentType, null);
          }
        });
        final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
        String text = getSearchField().getText();
        boolean visible =
            text.trim().length() == 0 || componentType.getName().toLowerCase().contains(text.toLowerCase())
                || componentType.getDescription().toLowerCase().contains(text.toLowerCase())
                || componentType.getCategory().toLowerCase().contains(text.toLowerCase());
        payload.setVisible(visible);
        getRecentNode().add(componentNode);
      } catch (ClassNotFoundException e) {
        LOG.error("Could not create recent component button for " + componentClassName, e);
      }
    }
    getTreeModel().nodeStructureChanged(getRecentNode());
  }

  private void refreshBlocks(List<String> blocks) {
    getBlocksNode().removeAllChildren();
    for (final String block : blocks) {
      Payload payload = new Payload(block, new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (SwingUtilities.isLeftMouseButton(e)) {
            try {
              plugInPort.loadBlock(block);
            } catch (InvalidBlockException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
          }
        }
      });
      final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(payload, false);
      String text = getSearchField().getText();
      boolean visible = text.trim().length() == 0 || block.toLowerCase().contains(text.toLowerCase());
      payload.setVisible(visible);
      getBlocksNode().add(componentNode);
    }
    getTreeModel().nodeStructureChanged(getBlocksNode());
  }

  public DefaultTreeModel getTreeModel() {
    if (treeModel == null) {
      final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Components", true);
      rootNode.add(getRecentNode());
      rootNode.add(getBlocksNode());
      Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
      List<String> categories = new ArrayList<String>(componentTypes.keySet());
      Collections.sort(categories);
      for (String category : categories) {
        final DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(new Payload(category, null));
        rootNode.add(categoryNode);
        for (final ComponentType type : componentTypes.get(category)) {
          final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(new Payload(type, new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
              plugInPort.setNewComponentTypeSlot(type, null);
            }
          }), false);
          categoryNode.add(componentNode);
        }
      }
      treeModel = new DefaultTreeModel(rootNode);
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
      ToolTipManager.sharedInstance().registerComponent(tree);

      tree.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node != null && node.getUserObject() != null) {
            Payload payload = (Payload) node.getUserObject();
            if (payload.getClickListener() != null) {
              payload.getClickListener().mouseClicked(e);
            }
          }
          if (SwingUtilities.isRightMouseButton(e)) {
            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);
            getPopup().show(e.getComponent(), e.getX(), e.getY());
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

          DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

          if (selectedNode == null || selectedNode.getUserObject() == null)
            return;

          Payload payload = (Payload) selectedNode.getUserObject();

          ComponentType componentType = payload.getComponentType();
          if (componentType != null) {
            popup.add(new SelectAllAction(plugInPort));
            popup.add(new JSeparator());

            List<Template> templates = plugInPort.getTemplatesFor(componentType.getCategory(), componentType.getName());
            if (templates == null || templates.isEmpty()) {
              JMenuItem item = new JMenuItem("<no variants>");
              item.setEnabled(false);
              popup.add(item);
            } else {
              for (Template template : templates) {
                JMenuItem item = ComponentButtonFactory.createTemplateItem(plugInPort, template, componentType);
                popup.add(item);
              }
            }
          } else if (selectedNode.isLeaf()) {
            popup.add(new DeleteBlockAction(plugInPort, payload.toString()));
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
      searchField = new JTextField() {

        private static final long serialVersionUID = 1L;

        @Override
        public void paint(Graphics g) {
          super.paint(g);

          Graphics2D g2d = (Graphics2D) g;
          g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
          Icon icon = IconLoader.SearchBox.getIcon();
          icon.paintIcon(searchField, g2d, searchField.getWidth() - 18, 3);

          if (searchField.getText().trim().length() == 0 && !searchField.hasFocus()) {
            g2d.setColor(Color.gray);
            g2d.setFont(searchField.getFont());
            g2d.drawString("Search (press Q to jump here)", 4, 3 + searchField.getFont().getSize());
          }
        }
      };

      searchField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          searchField.repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
          searchField.repaint();
        }
      });

      searchField.getDocument().addDocumentListener(new DocumentListener() {

        public void changedUpdate(DocumentEvent e) {
          process();
        }

        public void removeUpdate(DocumentEvent e) {
          process();
        }

        public void insertUpdate(DocumentEvent e) {
          process();
        }

        public void process() {
          String text = searchField.getText();
          DefaultTreeModel model = getTreeModel();
          DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
          for (int i = 0; i < model.getChildCount(rootNode); i++) {
            int visibleCount = 0;
            DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode) model.getChild(rootNode, i);
            for (int j = 0; j < model.getChildCount(categoryNode); j++) {
              DefaultMutableTreeNode componentNode = (DefaultMutableTreeNode) model.getChild(categoryNode, j);
              Object obj = componentNode.getUserObject();
              if (obj != null && obj.getClass().equals(Payload.class)) {
                Payload payload = (Payload) obj;
                boolean visible = false;
                if (text.trim().length() == 0) {
                  visible = true;
                } else if (payload.getComponentType() != null) {
                  ComponentType type = payload.getComponentType();
                  visible =
                      type.getName().toLowerCase().contains(text.toLowerCase())
                          || type.getDescription().toLowerCase().contains(text.toLowerCase())
                          || type.getCategory().toLowerCase().contains(text.toLowerCase());
                } else {
                  visible = payload.toString().toLowerCase().contains(text.toLowerCase());
                }
                if (visible != payload.isVisible()) {
                  payload.setVisible(visible);
                  model.nodeStructureChanged(componentNode);
                }
                if (visible)
                  visibleCount++;
              }
            }

            Object obj = categoryNode.getUserObject();
            if (obj != null && obj.getClass().equals(Payload.class)) {
              Payload payload = (Payload) obj;
              boolean categoryVisible = visibleCount > 0;
              if (categoryVisible != payload.isVisible()) {
                payload.setVisible(categoryVisible);
                model.nodeStructureChanged(rootNode);
              }
              if (categoryVisible && text.trim().length() > 0) {
                getTree().expandPath(new TreePath(categoryNode.getPath()));
              }
            }
          }

          for (int i = 0; i < model.getChildCount(rootNode); i++) {
            DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode) model.getChild(rootNode, i);
            getTree().expandPath(new TreePath(categoryNode.getPath()));
          }
        }
      });
    }
    return searchField;
  }

  public class ComponentCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected,
        final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {

      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      Object obj = ((DefaultMutableTreeNode) value).getUserObject();
      if (obj != null && obj.getClass().equals(Payload.class)) {
        Payload payload = (Payload) obj;
        if (payload.getComponentType() == null) {
          setToolTipText(null);
          if (leaf)
            setIcon(IconLoader.Component.getIcon());
          if (payload.isVisible())
            setPreferredSize(new Dimension(240, 20));
          else
            setPreferredSize(new Dimension(0, 0));
        } else {
          setToolTipText("<html><b>" + payload.getComponentType().getName() + "</b><br>"
              + payload.getComponentType().getDescription() + "<br>Author: " + payload.getComponentType().getAuthor()
              + "<br><br>Left click to instantiate this component, right click for more options" + "</html>");
          setIcon(payload.getComponentType().getIcon());
          if (payload.isVisible())
            setPreferredSize(new Dimension(240, 32));
          else
            setPreferredSize(new Dimension(0, 0));
        }
      }

      return this;
    }
  }

  public class Payload {
    private ComponentType componentType;
    private String category;
    private boolean isVisible;
    private MouseListener clickListener;

    public Payload(ComponentType componentType, MouseListener clickListener) {
      super();
      this.componentType = componentType;
      this.clickListener = clickListener;
      this.isVisible = true;
    }

    public Payload(String category, MouseListener clickListener) {
      super();
      this.category = category;
      this.clickListener = clickListener;
      this.isVisible = true;
    }

    public ComponentType getComponentType() {
      return componentType;
    }

    public boolean isVisible() {
      return isVisible;
    }

    public void setVisible(boolean isVisible) {
      this.isVisible = isVisible;
    }

    public MouseListener getClickListener() {
      return clickListener;
    }

    @Override
    public String toString() {
      return componentType == null ? category : componentType.getName();
    }
  }

  public static class SelectAllAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;

    public SelectAllAction(IPlugInPort plugInPort) {
      super();
      this.plugInPort = plugInPort;
      putValue(AbstractAction.NAME, "Select All");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      ComponentType componentType = plugInPort.getNewComponentTypeSlot();
      if (componentType != null) {
        List<IDIYComponent<?>> components = plugInPort.getCurrentProject().getComponents();
        List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
        for (IDIYComponent<?> component : components) {
          if (componentType.getInstanceClass().equals(component.getClass())) {
            newSelection.add(component);
          }
        }

        plugInPort.updateSelection(newSelection);
        plugInPort.setNewComponentTypeSlot(null, null);
        plugInPort.refresh();
      }
    }
  }

  public static class DeleteBlockAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IPlugInPort plugInPort;
    private String blockName;

    public DeleteBlockAction(IPlugInPort plugInPort, String blockName) {
      super();
      this.plugInPort = plugInPort;
      this.blockName = blockName;
      putValue(AbstractAction.NAME, "Delete Building Block");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      LOG.info(getValue(AbstractAction.NAME) + " triggered");
      plugInPort.deleteBlock(blockName);
    }
  }
}
