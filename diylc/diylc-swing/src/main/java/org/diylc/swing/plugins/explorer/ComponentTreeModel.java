package org.diylc.swing.plugins.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.diylc.core.IDIYComponent;

public class ComponentTreeModel extends DefaultTreeModel {

  private static final long serialVersionUID = 1L;

  private DefaultMutableTreeNode root;
  private List<IDIYComponent<?>> componentsRaw;
  private List<IDIYComponent<?>> components;
  private Set<Set<IDIYComponent<?>>> groups;

  private String searchText;
  private ExplorerPane.Sort sort = ExplorerPane.Sort.Z_INDEX;

  public ComponentTreeModel() {
    super(new DefaultMutableTreeNode("Root"));
    this.root = (DefaultMutableTreeNode) getRoot();
  }

  public void setComponents(List<IDIYComponent<?>> components, Set<Set<IDIYComponent<?>>> groups) {
    this.componentsRaw = components;
    this.groups = groups != null ? groups : new HashSet<>();
    refresh();
  }
  
  public void setComponents(List<IDIYComponent<?>> components) {
    setComponents(components, null);
  }
  
  /**
   * Returns the group that contains the given component, or null if not in a group.
   */
  public Set<IDIYComponent<?>> getGroupForComponent(IDIYComponent<?> component) {
    if (groups == null) {
      return null;
    }
    for (Set<IDIYComponent<?>> group : groups) {
      if (group.contains(component)) {
        return group;
      }
    }
    return null;
  }

  public List<IDIYComponent<?>> getComponents() {
    return components;
  }

  public ExplorerPane.Sort getSort() {
    return sort;
  }

  public void setSort(ExplorerPane.Sort sort) {
    this.sort = sort;
    refresh();
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText.toLowerCase();
    refresh();
  }

  private void refresh() {
    // Clear existing children
    root.removeAllChildren();

    // Sort components
    if (sort == ExplorerPane.Sort.NAME) {
      this.components =
          componentsRaw.stream().sorted(Comparator.comparing(x -> x.toString().toLowerCase()))
              .collect(Collectors.toList());
    } else if (sort == ExplorerPane.Sort.TYPE) {
      this.components = componentsRaw.stream()
          .sorted(Comparator.comparing(x -> x.getClass().getName().toLowerCase())
              .thenComparing(x -> x.toString().toLowerCase()))
          .collect(Collectors.toList());
    } else {
      List<IDIYComponent<?>> componentsClone = new ArrayList<IDIYComponent<?>>(componentsRaw);
      Collections.reverse(componentsClone);
      this.components = componentsClone;
    }

    // Filter by search text
    if (searchText != null && !searchText.trim().isEmpty()) {
      this.components = this.components.stream().filter(component -> {
        return component.getName().toLowerCase().contains(searchText);
      }).collect(Collectors.toList());
    }

    // Build a map of components to their groups
    Set<IDIYComponent<?>> groupedComponents = new HashSet<>();
    if (groups != null) {
      for (Set<IDIYComponent<?>> group : groups) {
        groupedComponents.addAll(group);
      }
    }

    // Create a list to hold all tree items (groups and ungrouped components) for sorting
    List<TreeItem> allItems = new ArrayList<>();
    
    // Create group nodes
    if (groups != null && !groups.isEmpty()) {
      for (Set<IDIYComponent<?>> group : groups) {
        // Get ALL group components from raw list (for maxIndex calculation)
        List<IDIYComponent<?>> groupComponentsRaw = componentsRaw.stream()
            .filter(group::contains)
            .collect(Collectors.toList());
        
        // Apply search filter to get components to display
        List<IDIYComponent<?>> groupComponents = groupComponentsRaw.stream()
            .filter(component -> {
              if (searchText != null && !searchText.trim().isEmpty()) {
                return component.getName().toLowerCase().contains(searchText);
              }
              return true;
            })
            .collect(Collectors.toList());
        
        if (!groupComponents.isEmpty()) {
          // Sort components within the group based on sort mode
          if (sort == ExplorerPane.Sort.NAME) {
            groupComponents.sort(Comparator.comparing(x -> x.toString().toLowerCase()));
          } else if (sort == ExplorerPane.Sort.TYPE) {
            groupComponents.sort(Comparator.comparing(x -> x.getClass().getName().toLowerCase())
                .thenComparing(x -> x.toString().toLowerCase()));
          } else {
            // For Z_INDEX, sort by index in raw list (reverse order - highest first)
            groupComponents.sort((c1, c2) -> {
              int idx1 = componentsRaw.indexOf(c1);
              int idx2 = componentsRaw.indexOf(c2);
              return Integer.compare(idx2, idx1); // Reverse: higher index first
            });
          }
          
          // Store both the display components and all group components (for maxIndex calculation)
          allItems.add(new TreeItem(group, groupComponents, groupComponentsRaw, true));
        }
      }
    }
    
    // Add ungrouped components
    for (IDIYComponent<?> component : this.components) {
      if (!groupedComponents.contains(component)) {
        allItems.add(new TreeItem(null, Collections.singletonList(component), Collections.singletonList(component), false));
      }
    }
    
    // Sort all items together based on sort mode
    if (sort == ExplorerPane.Sort.Z_INDEX) {
      // Sort by maximum z-order (index) - groups use max index of ALL their components (not just filtered)
      Collections.sort(allItems, (item1, item2) -> {
        int maxIndex1 = getMaxComponentIndex(item1.allComponents);
        int maxIndex2 = getMaxComponentIndex(item2.allComponents);
        // Reverse order: higher index first (since components are in reverse order)
        return Integer.compare(maxIndex2, maxIndex1);
      });
    } else if (sort == ExplorerPane.Sort.NAME) {
      Collections.sort(allItems, (item1, item2) -> {
        String name1 = item1.components.get(0).getName().toLowerCase();
        String name2 = item2.components.get(0).getName().toLowerCase();
        return name1.compareTo(name2);
      });
    } else if (sort == ExplorerPane.Sort.TYPE) {
      Collections.sort(allItems, (item1, item2) -> {
        String type1 = item1.components.get(0).getClass().getName().toLowerCase();
        String type2 = item2.components.get(0).getClass().getName().toLowerCase();
        int typeCompare = type1.compareTo(type2);
        if (typeCompare != 0) {
          return typeCompare;
        }
        String name1 = item1.components.get(0).getName().toLowerCase();
        String name2 = item2.components.get(0).getName().toLowerCase();
        return name1.compareTo(name2);
      });
    }
    
    // Add sorted items to the tree
    for (TreeItem item : allItems) {
      if (item.isGroup) {
        DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new GroupNode(item.group));
        for (IDIYComponent<?> component : item.components) {
          DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(component);
          groupNode.add(componentNode);
        }
        root.add(groupNode);
      } else {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(item.components.get(0));
        root.add(node);
      }
    }

    // Notify tree model listeners
    nodeStructureChanged(root);
  }
  
  /**
   * Wrapper class to represent a group node in the tree.
   */
  public static class GroupNode {
    private final Set<IDIYComponent<?>> group;
    
    public GroupNode(Set<IDIYComponent<?>> group) {
      this.group = group;
    }
    
    public Set<IDIYComponent<?>> getGroup() {
      return group;
    }
    
    @Override
    public String toString() {
      return "Group (" + group.size() + " components)";
    }
  }

  public int getSize() {
    return this.components == null ? 0 : this.components.size();
  }
  
  /**
   * Gets the maximum index of components in the raw components list.
   * This represents the maximum z-order for the group.
   * Returns -1 if no valid index is found.
   */
  private int getMaxComponentIndex(List<IDIYComponent<?>> groupComponents) {
    int maxIndex = -1;
    for (IDIYComponent<?> component : groupComponents) {
      int index = componentsRaw.indexOf(component);
      if (index >= 0 && index > maxIndex) {
        maxIndex = index;
      }
    }
    return maxIndex;
  }
  
  /**
   * Helper class to hold either a group or a single ungrouped component for sorting.
   */
  private static class TreeItem {
    final Set<IDIYComponent<?>> group;
    final List<IDIYComponent<?>> components; // Components to display (may be filtered)
    final List<IDIYComponent<?>> allComponents; // All components for maxIndex calculation
    final boolean isGroup;
    
    TreeItem(Set<IDIYComponent<?>> group, List<IDIYComponent<?>> components, 
        List<IDIYComponent<?>> allComponents, boolean isGroup) {
      this.group = group;
      this.components = components;
      this.allComponents = allComponents;
      this.isGroup = isGroup;
    }
  }
}

