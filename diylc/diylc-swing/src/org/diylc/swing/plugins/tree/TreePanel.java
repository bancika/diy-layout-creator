package org.diylc.swing.plugins.tree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;

public class TreePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private DefaultTreeModel treeModel;
  private JTree tree;
  private JTextField searchField;

  private IPlugInPort plugInPort;

  public TreePanel(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;

    add(getSearchField(), gbc);

    gbc.gridy++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    gbc.weightx = 1;

    JScrollPane scroll = new JScrollPane(getTree());
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll, gbc);

    setPreferredSize(new Dimension(240, 200));
  }

  public DefaultTreeModel getTreeModel() {
    if (treeModel == null) {
      final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Components", true);
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
      tree.addTreeSelectionListener(new TreeSelectionListener() {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
          Object c = e.getPath().getLastPathComponent();
          if (c != null && c.getClass().equals(DefaultMutableTreeNode.class)) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) c;
            Object obj = node.getUserObject();
            if (obj != null && obj.getClass().equals(Payload.class)) {
              Payload payload = (Payload) obj;
              plugInPort.setNewComponentTypeSlot(payload.getComponentType(), null);
            }
          }
        }
      });
    }
    return tree;
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
            g2d.drawString("Search", 4, 3 + searchField.getFont().getSize());
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
}
