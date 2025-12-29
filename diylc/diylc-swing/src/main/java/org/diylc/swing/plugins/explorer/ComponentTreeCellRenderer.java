package org.diylc.swing.plugins.explorer;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;

public class ComponentTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = 1L;

  // private static Color[] layerColors = new Color[] { Color.gray, Color.decode("#ff6363"),
  // Color.decode("#293462"), Color.decode("#138588"),
  // Color.decode("#c0ca03"), Color.decode("#990000") };

  private Map<String, ComponentType> componentTypes;

  public ComponentTreeCellRenderer(Map<String, List<ComponentType>> componentTypeMap) {
    this.componentTypes = componentTypeMap.values().stream().flatMap(x -> x.stream())
        .collect(Collectors.toMap(x -> x.getInstanceClass().getCanonicalName(), x -> x));
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {

    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

    if (!(value instanceof DefaultMutableTreeNode)) {
      return this;
    }

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();

    // Handle group nodes
    if (userObject instanceof ComponentTreeModel.GroupNode) {
      ComponentTreeModel.GroupNode groupNode = (ComponentTreeModel.GroupNode) userObject;
      setText(groupNode.toString());
      setIcon(null); // No icon for group nodes
      setPreferredSize(new Dimension(250, 16));
      return this;
    }

    if (!(userObject instanceof IDIYComponent)) {
      return this;
    }

    IDIYComponent<?> component = (IDIYComponent<?>) userObject;
    ComponentType componentType = componentTypes.get(component.getClass().getCanonicalName());

    if (componentType == null) {
      return this;
    }

    // Set the component's icon
    Icon icon = componentType.getIcon();
    if (icon != null) {
      setIcon(icon);
    }

    int layerId = (int) Math.round(componentType.getZOrder());

    String valueForDisplay = component.getValueForDisplay();
    if (selected) {
      if (valueForDisplay == null || valueForDisplay.trim().isEmpty()) {
        setText(String.format("[L%s] %s", layerId, component.getName()));
      } else {
        setText(String.format("[L%s] %s (%s)", layerId, component.getName(), valueForDisplay));
      }
    } else {
      if (valueForDisplay == null || valueForDisplay.trim().isEmpty()) {
        setText(String.format("<html><font color='#c0c0c0'>[L%s]</font> %s</html>",
            layerId, component.getName()));
      } else {
        setText(String.format(
            "<html><font color='#c0c0c0'>[L%s]</font> %s (<font color='#666666'>%s</font>)</html>",
            layerId, component.getName(), valueForDisplay));
      }
    }

    // Set preferred size after all properties are set (like TreePanel does)
    setPreferredSize(new Dimension(250, 16));

    return this;
  }
}

