package org.diylc.swing.plugins.explorer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ISwingUI;

public class ComponentTreeTransferHandler extends TransferHandler {

  private static final long serialVersionUID = 1L;

  private static final String REORDERING_NOT_POSSIBLE = LangUtil.translate(
      "Component reordering is possible only when Project Explorer is sorted by z-index.");

  private final ComponentTreeModel treeModel;
  private final ISwingUI swingUI;
  private final IPlugInPort plugInPort;

  public ComponentTreeTransferHandler(ComponentTreeModel treeModel, ISwingUI swingUI,
      IPlugInPort plugInPort) {
    this.treeModel = treeModel;
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
  }

  @Override
  public int getSourceActions(JComponent c) {
    return MOVE;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    if (!(c instanceof JTree)) {
      return null;
    }

    JTree tree = (JTree) c;
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths == null || selectionPaths.length == 0) {
      return null;
    }

    // Get the first selected path
    TreePath firstPath = selectionPaths[0];
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) firstPath.getLastPathComponent();
    Object userObject = node.getUserObject();
    
    // Only allow dragging components, not group nodes
    if (!(userObject instanceof IDIYComponent)) {
      return null;
    }
    
    // Store the component's group info in the transferable
    IDIYComponent<?> component = (IDIYComponent<?>) userObject;
    Set<IDIYComponent<?>> group = treeModel.getGroupForComponent(component);
    String groupId = group != null ? Integer.toString(group.hashCode()) : "null";
    
    int row = tree.getRowForPath(firstPath);
    return new StringSelection(row + ":" + groupId);
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport support) {
    if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      return false;
    }

    if (!(support.getComponent() instanceof JTree)) {
      return false;
    }

    JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
    TreePath dropPath = dl.getPath();
    if (dropPath == null) {
      return false;
    }

    // Only allow reordering when sorted by Z_INDEX
    if (treeModel.getSort() != ExplorerPane.Sort.Z_INDEX) {
      return false;
    }

    // Check if dropping on a group node - not allowed
    DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
    if (dropNode.getUserObject() instanceof ComponentTreeModel.GroupNode) {
      return false;
    }

    // Get the source component's group from the transferable
    try {
      Transferable transferable = support.getTransferable();
      String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
      String[] parts = data.split(":");
      if (parts.length == 2) {
        String sourceGroupId = parts[1];
        
        // Get the target component's group
        Object targetUserObject = dropNode.getUserObject();
        if (targetUserObject instanceof IDIYComponent) {
          IDIYComponent<?> targetComponent = (IDIYComponent<?>) targetUserObject;
          Set<IDIYComponent<?>> targetGroup = treeModel.getGroupForComponent(targetComponent);
          String targetGroupId = targetGroup != null ? Integer.toString(targetGroup.hashCode()) : "null";
          
          // Only allow dragging within the same group (or both ungrouped)
          if (!sourceGroupId.equals(targetGroupId)) {
            return false;
          }
        }
      }
    } catch (Exception e) {
      // If we can't parse the transferable, don't allow the drop
      return false;
    }

    return true;
  }

  @Override
  public boolean importData(TransferHandler.TransferSupport support) {
    if (!canImport(support)) {
      // Show error message if not sorted by Z_INDEX
      if (treeModel.getSort() != ExplorerPane.Sort.Z_INDEX) {
        swingUI.showMessage(REORDERING_NOT_POSSIBLE, "Error", ISwingUI.ERROR_MESSAGE);
      }
      return false;
    }

    if (!(support.getComponent() instanceof JTree)) {
      return false;
    }

    JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
    TreePath dropPath = dl.getPath();
    
    if (dropPath == null) {
      return false;
    }
    
    DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
    Object dropUserObject = dropNode.getUserObject();
    
    // Can't drop on group nodes
    if (dropUserObject instanceof ComponentTreeModel.GroupNode) {
      return false;
    }
    
    if (!(dropUserObject instanceof IDIYComponent)) {
      return false;
    }
    
    // Get the parent - if it's a group node, we need to calculate index within the group
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dropNode.getParent();
    
    if (parent != null && parent.getUserObject() instanceof ComponentTreeModel.GroupNode) {
      // Dropping within a group - calculate index within the group's children
      int childIndex = dl.getChildIndex();
      int dropTargetIndex;
      
      if (childIndex != -1) {
        dropTargetIndex = childIndex;
      } else {
        // Dropping on a component within the group
        dropTargetIndex = parent.getIndex(dropNode);
      }
      
      // For groups, we need to calculate z-index based on the group's components
      // Get all components in the group in z-order (they're already sorted in the model)
      ComponentTreeModel.GroupNode groupNode = (ComponentTreeModel.GroupNode) parent.getUserObject();
      List<IDIYComponent<?>> groupComponents = treeModel.getComponents().stream()
          .filter(groupNode.getGroup()::contains)
          .collect(java.util.stream.Collectors.toList());
      
      if (dropTargetIndex < groupComponents.size()) {
        IDIYComponent<?> targetComponent = groupComponents.get(dropTargetIndex);
        // Find the component's index in the full component list
        int componentIndex = treeModel.getComponents().indexOf(targetComponent);
        if (componentIndex >= 0) {
          int targetZIndex = treeModel.getSize() - componentIndex;
          plugInPort.moveSelectionToZIndex(targetZIndex);
        }
      }
    } else {
      // Dropping on an ungrouped component at root level
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
      int childIndex = dl.getChildIndex();
      int dropTargetIndex;
      
      if (childIndex != -1) {
        dropTargetIndex = childIndex;
      } else {
        dropTargetIndex = root.getIndex(dropNode);
      }
      
      // Calculate the target z-index (components are in reverse order for z-index)
      int targetZIndex = treeModel.getSize() - dropTargetIndex;
      plugInPort.moveSelectionToZIndex(targetZIndex);
    }

    return true;
  }
}

