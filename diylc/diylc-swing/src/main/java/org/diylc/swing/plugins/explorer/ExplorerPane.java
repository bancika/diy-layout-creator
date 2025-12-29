package org.diylc.swing.plugins.explorer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DropMode;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.ComponentPopupMenu;
import org.diylc.swing.gui.SearchTextField;

public class ExplorerPane extends JPanel {

  private static final long serialVersionUID = 1L;

  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  private JLabel titleLabel;
  private JTextField searchField;
  private JScrollPane componentScrollPane;
  private ComponentTreeModel componentTreeModel;
  private JTree componentTree;
  private ComponentPopupMenu popupMenu;
  private JComboBox<Sort> sortBox;

  public ExplorerPane(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
    setName("Project Explorer");

    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    gbc.gridwidth = 1;

    // add(getTitleLabel(), gbc);
    //
    // gbc.gridy++;

    add(getSearchField(), gbc);

    gbc.gridy++;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    gbc.weightx = 1;

    add(getComponentScrollPane(), gbc);

    gbc.gridy++;

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0;
    gbc.weighty = 0;

    add(getSortBox(), gbc);

    setPreferredSize(new Dimension(240, 200));
  }

  public JLabel getTitleLabel() {
    if (titleLabel == null) {
      titleLabel = new JLabel(getName());
      titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
      // titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
    }
    return titleLabel;
  }

  public JTextField getSearchField() {
    if (searchField == null) {
      searchField = new SearchTextField(true, 'X', text -> {
        getComponentTreeModel().setSearchText(text);
      });
    }
    return searchField;
  }

  public JComboBox<Sort> getSortBox() {
    if (sortBox == null) {
      sortBox = new JComboBox<Sort>(Sort.values());
      sortBox.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          getComponentTreeModel().setSort((Sort) e.getItem());
        }
      });
    }
    return sortBox;
  }

  public JScrollPane getComponentScrollPane() {
    if (componentScrollPane == null) {
      componentScrollPane = new JScrollPane(getComponentTree(),
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    return componentScrollPane;
  }

  public JTree getComponentTree() {
    if (componentTree == null) {
      componentTree = new JTree(getComponentTreeModel());
      componentTree.setRootVisible(false);
      componentTree.setShowsRootHandles(true);
      componentTree.setFont(DEFAULT_FONT);
      componentTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
      componentTree.setCellRenderer(new ComponentTreeCellRenderer(plugInPort.getComponentTypes()));
      // Set row height after renderer is set, using a fixed value
      componentTree.setRowHeight(24);
      
      // Enable drag and drop
      componentTree.setDragEnabled(true);
      componentTree.setDropMode(DropMode.INSERT);
      componentTree.setTransferHandler(new ComponentTreeTransferHandler(getComponentTreeModel(),
          swingUI, plugInPort));
      
      componentTree.addTreeSelectionListener(new TreeSelectionListener() {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
          if (e.getNewLeadSelectionPath() == null)
            return;

          List<IDIYComponent<?>> selectedValuesList = getSelectedComponents();
          if (selectedValuesList.size() > 0 && !new HashSet<IDIYComponent<?>>(selectedValuesList)
              .equals(new HashSet<IDIYComponent<?>>(plugInPort.getSelectedComponents()))) {
            plugInPort.setSelection(selectedValuesList, true);
          }
        }
      });

      componentTree.addMouseListener(new MouseAdapter() {

        private MouseEvent pressedEvent;
        
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            plugInPort.editSelection();
          }          
        }

        @Override
        public void mousePressed(MouseEvent e) {
          componentTree.requestFocus();
          pressedEvent = e;
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
          // Invoke the rest of the code later so we get the chance to
          // process selection messages.
          SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              if (plugInPort.getNewComponentTypeSlot() == null && (e.isPopupTrigger()
                  || (pressedEvent != null && pressedEvent.isPopupTrigger()))) {

                final List<IDIYComponent<?>> selectedValues = getSelectedComponents();
                getComponentPopupMenu().prepareAndShowAt(selectedValues, selectedValues, e.getX(),
                    e.getY());
              }
            }
          });
        }
      });
    }
    return componentTree;
  }
  
  private List<IDIYComponent<?>> getSelectedComponents() {
    List<IDIYComponent<?>> selectedComponents = new ArrayList<>();
    TreePath[] selectionPaths = componentTree.getSelectionPaths();
    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        Object lastPathComponent = path.getLastPathComponent();
        if (lastPathComponent instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastPathComponent;
          Object userObject = node.getUserObject();
          if (userObject instanceof IDIYComponent) {
            selectedComponents.add((IDIYComponent<?>) userObject);
          }
        }
      }
    }
    return selectedComponents;
  }

  public ComponentTreeModel getComponentTreeModel() {
    if (componentTreeModel == null) {
      componentTreeModel = new ComponentTreeModel();
    }
    return componentTreeModel;
  }

  public ComponentPopupMenu getComponentPopupMenu() {
    if (popupMenu == null) {
      popupMenu = new ComponentPopupMenu(plugInPort, getComponentTree(), false);
    }
    return popupMenu;
  }

  public void setComponents(List<IDIYComponent<?>> components,
      Collection<IDIYComponent<?>> selectedComponents) {
    // Get groups from the current project
    Set<Set<IDIYComponent<?>>> groups = null;
    if (plugInPort.getCurrentProject() != null) {
      groups = plugInPort.getCurrentProject().getGroups();
    }
    this.getComponentTreeModel().setComponents(components, groups);
    setSelection(new HashSet<IDIYComponent<?>>(selectedComponents));
    
    // Expand all group nodes after model is updated
    SwingUtilities.invokeLater(() -> {
      JTree tree = getComponentTree();
      for (int i = 0; i < tree.getRowCount(); i++) {
        TreePath path = tree.getPathForRow(i);
        if (path != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof ComponentTreeModel.GroupNode) {
            tree.expandPath(path);
          }
        }
      }
    });
  }

  public void setSelection(Set<IDIYComponent<?>> selectedComponents) {
    List<IDIYComponent<?>> currentSelected = getSelectedComponents();
    if (new HashSet<IDIYComponent<?>>(currentSelected)
        .equals(new HashSet<IDIYComponent<?>>(plugInPort.getSelectedComponents())))
      return;

    final JTree tree = this.getComponentTree();

    if (selectedComponents.size() == 0) {
      tree.clearSelection();
    } else {
      List<TreePath> pathsToSelect = new ArrayList<>();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) getComponentTreeModel().getRoot();
      
      for (int i = 0; i < root.getChildCount(); i++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
        Object userObject = child.getUserObject();
        if (userObject instanceof IDIYComponent && selectedComponents.contains(userObject)) {
          pathsToSelect.add(new TreePath(child.getPath()));
        }
      }

      tree.getSelectionModel().setSelectionPaths(pathsToSelect.toArray(new TreePath[0]));
      
      // If none of the selected items is visible, scroll to the first one
      if (!pathsToSelect.isEmpty()) {
        TreePath firstPath = pathsToSelect.get(0);
        if (tree.getRowForPath(firstPath) == -1) {
          tree.scrollPathToVisible(firstPath);
        }
      }
    }
  }



  static enum Sort {
    Z_INDEX("Sort by Z-index"), NAME("Sort by Name"), TYPE("Sort by Type");

    private String label;

    Sort(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return LangUtil.translate(label);
    }
  }
}
