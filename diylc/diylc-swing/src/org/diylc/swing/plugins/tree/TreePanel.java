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
import java.util.ArrayList;
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

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.ComponentType;
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
  private List<String> recentComponents;

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
        if (!new HashSet<String>(newComponents).equals(new HashSet<String>(TreePanel.this.recentComponents))) {
          LOG.info("Detected change");
          refreshRecentComponentsToolbar(newComponents);
        } else
          LOG.info("Detected no change");
        TreePanel.this.recentComponents = new ArrayList<String>(newComponents);
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
      this.recentNode = new DefaultMutableTreeNode(new Payload("(Recently Used)"), true);
      @SuppressWarnings("unchecked")
      List<String> recent =
          (List<String>) ConfigurationManager.getInstance().readObject(IPlugInPort.RECENT_COMPONENTS_KEY, null);
      if (recent != null) {
        this.recentComponents = new ArrayList<String>(recent);
        refreshRecentComponentsToolbar(recent);
      } else {
        this.recentComponents = new ArrayList<String>();
      }
    }
    return this.recentNode;
  }

  @SuppressWarnings("unchecked")
  private void refreshRecentComponentsToolbar(List<String> recentComponentClassList) {
    getRecentNode().removeAllChildren();
    for (String componentClassName : recentComponentClassList) {
      ComponentType componentType;
      try {
        componentType =
            ComponentProcessor.getInstance().extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) Class.forName(componentClassName));
        final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(new Payload(componentType), false);
        getRecentNode().add(componentNode);
      } catch (ClassNotFoundException e) {
        LOG.error("Could not create recent component button for " + componentClassName, e);
      }
    }
    getTreeModel().nodeStructureChanged(getRecentNode());
  }

  public DefaultTreeModel getTreeModel() {
    if (treeModel == null) {
      final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Components", true);
      rootNode.add(getRecentNode());
      Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
      List<String> categories = new ArrayList<String>(componentTypes.keySet());
      Collections.sort(categories);
      for (String category : categories) {
        final DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(new Payload(category));
        rootNode.add(categoryNode);
        for (ComponentType type : componentTypes.get(category)) {
          final DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(new Payload(type), false);
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
      tree.setRowHeight(0);
      ToolTipManager.sharedInstance().registerComponent(tree);
      // tree.addTreeSelectionListener(new TreeSelectionListener() {
      //
      // @Override
      // public void valueChanged(TreeSelectionEvent e) {
      // Object c = e.getPath().getLastPathComponent();
      // if (c != null && c.getClass().equals(DefaultMutableTreeNode.class)) {
      // DefaultMutableTreeNode node = (DefaultMutableTreeNode) c;
      // Object obj = node.getUserObject();
      // if (obj != null && obj.getClass().equals(Payload.class)) {
      // Payload payload = (Payload) obj;
      // plugInPort.setNewComponentTypeSlot(payload.getComponentType(), null);
      // }
      // }
      // }
      // });

      tree.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (SwingUtilities.isRightMouseButton(e)) {
            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);
            getPopup().show(e.getComponent(), e.getX(), e.getY());
          } else {
            TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node != null && node.getUserObject() != null) {
              Payload payload = (Payload) node.getUserObject();
              if (payload.getComponentType() != null) {
                plugInPort.setNewComponentTypeSlot(payload.getComponentType(), null);
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

          popup.add(new SelectAllAction(plugInPort));
          ComponentType componentType = plugInPort.getNewComponentTypeSlot();
          if (componentType != null) {
            popup.add(new JSeparator());

            List<Template> templates = plugInPort.getTemplatesFor(componentType.getCategory(), componentType.getName());
            if (templates == null || templates.isEmpty()) {
              JMenuItem item = new JMenuItem("<no templates>");
              item.setEnabled(false);
              popup.add(item);
            } else {
              for (Template template : templates) {
                JMenuItem item = ComponentButtonFactory.createTemplateItem(plugInPort, template, componentType);
                popup.add(item);
              }
            }
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
                } else {
                  ComponentType type = payload.getComponentType();
                  visible =
                      type.getName().toLowerCase().contains(text.toLowerCase())
                          || type.getDescription().toLowerCase().contains(text.toLowerCase())
                          || type.getCategory().toLowerCase().contains(text.toLowerCase());
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
                tree.expandPath(new TreePath(categoryNode.getPath()));
              }
            }
          }

          for (int i = 0; i < model.getChildCount(rootNode); i++) {
            DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode) model.getChild(rootNode, i);
            tree.expandPath(new TreePath(categoryNode.getPath()));
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
        Payload type = (Payload) obj;
        if (type.getComponentType() == null) {
          if (type.isVisible())
            setPreferredSize(new Dimension(240, 20));
          else
            setPreferredSize(new Dimension(0, 0));
        } else {
          setToolTipText("<html><b>" + type.getComponentType().getName() + "</b><br>"
              + type.getComponentType().getDescription() + "<br>Author: " + type.getComponentType().getAuthor()
              + "<br><br>Left click to instantiate this component, right click for more options" + "</html>");
          setIcon(type.getComponentType().getIcon());
          if (type.isVisible())
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

    public Payload(ComponentType componentType) {
      super();
      this.componentType = componentType;
      this.isVisible = true;
    }

    public Payload(String category) {
      super();
      this.category = category;
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
}
