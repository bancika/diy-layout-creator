package org.diylc.swing.plugins.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ComponentGroup;
import org.diylc.core.IDIYComponent;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.ComponentPopupMenu;
import org.diylc.swing.gui.DragDropList;
import org.diylc.swing.gui.IDragDropListListener;
import org.diylc.swing.gui.SearchTextField;

public class ExplorerPane extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final String REORDERING_NOT_POSSIBLE = LangUtil.translate(
      "Component reordering is possible only when Project Explorer is sorted by z-index.");

  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  private JLabel titleLabel;
  private JTextField searchField;
  private JScrollPane componentScrollPane;
  private ComponentListModel componentListModel;
  private JList<ComponentGroupItem> componentList;
  private ComponentPopupMenu popupMenu;
  private JComboBox<Sort> sortBox;
  private ListSelectionListener selectionListener;

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
        getComponentListModel().setSearchText(text);
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
          getComponentListModel().setSort((Sort) e.getItem());
        }
      });
    }
    return sortBox;
  }

  public JScrollPane getComponentScrollPane() {
    if (componentScrollPane == null) {
      componentScrollPane = new JScrollPane(getComponentList(),
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    return componentScrollPane;
  }

  public JList<ComponentGroupItem> getComponentList() {
    if (componentList == null) {
      componentList = new DragDropList<ComponentGroupItem>(new IDragDropListListener() {

        @Override
        public boolean dropComplete(int selectedIndex, int dropTargetIndex) {
          if (getComponentListModel().getSort() != Sort.Z_INDEX) {
            swingUI.showMessage(REORDERING_NOT_POSSIBLE, "Error", ISwingUI.ERROR_MESSAGE);
            return false;
          }
          // The list is displayed in reverse order (highest z-index first, index 0 = last in component list)
          // Component list is in normal order (lowest z-index first, index 0 = first component)
          // In INSERT mode, dropTargetIndex is where the item will be inserted AFTER removal
          int listSize = getComponentListModel().getSize();
          int componentCount = plugInPort.getCurrentProject().getComponents().size();
          
          // The list size may be different from component count due to groups
          // listSize = componentCount - groupedComponents + groups
          // We need to find which component will be at dropTargetIndex in the final list
          // and convert that to a component index
          
          // Find the ComponentGroupItem that will be at dropTargetIndex after removal
          // We want to insert before the component that will be at dropTargetIndex after insertion
          List<ComponentGroupItem> components = getComponentListModel().getComponents();
          
          int componentAtTargetListIndex;
          
          if (dropTargetIndex >= listSize - 1) {
            // Dropping at the end
            plugInPort.moveSelectionToZIndex(componentCount);
            return true;
          } else if (selectedIndex < dropTargetIndex) {
            // Dragging up: removal happens before target
            // After removal, the component that was at dropTargetIndex is now at dropTargetIndex-1
            // After insertion at dropTargetIndex, it will be at dropTargetIndex+1
            // So we want to insert before the component currently at dropTargetIndex
            componentAtTargetListIndex = dropTargetIndex;
          } else {
            // Dragging down: removal happens after target
            // After removal, the component at dropTargetIndex stays at dropTargetIndex
            // After insertion at dropTargetIndex, it will be at dropTargetIndex+1
            // So we want to insert before the component currently at dropTargetIndex
            componentAtTargetListIndex = dropTargetIndex;
          }
          
          if (componentAtTargetListIndex >= listSize) {
            // Insert at the end
            plugInPort.moveSelectionToZIndex(componentCount);
            return true;
          }
          
          // Get the ComponentGroupItem at that position
          ComponentGroupItem targetItem = components.get(componentAtTargetListIndex);
          
          // Find the highest z-index component in this group (or the component itself if single)
          // Since the list is sorted by z-index (reversed), this is the component that determines
          // the position of this group in the list
          IDIYComponent<?> targetComponent = null;
          int maxComponentIndex = -1;
          List<IDIYComponent<?>> allComponents = plugInPort.getCurrentProject().getComponents();
          
          for (IDIYComponent<?> comp : targetItem.getGroup()) {
            int compIndex = allComponents.indexOf(comp);
            if (compIndex > maxComponentIndex) {
              maxComponentIndex = compIndex;
              targetComponent = comp;
            }
          }
          
          if (targetComponent == null || maxComponentIndex < 0) {
            return false;
          }
          
          // We want to insert before the component at maxComponentIndex
          // moveSelectionToZIndex will automatically decrement zIndex for each selected component
          // that's before the target, so we can use maxComponentIndex directly
          // However, we need to add 1 because we want to insert at dropTargetIndex, not before it
          int targetComponentIndex = maxComponentIndex + 1;
          
          if (targetComponentIndex < 0) {
            targetComponentIndex = 0;
          } else if (targetComponentIndex > componentCount) {
            targetComponentIndex = componentCount;
          }
          
          plugInPort.moveSelectionToZIndex(targetComponentIndex);
          return true;
        }
      });
      componentList.setModel(getComponentListModel());
      componentList.setFont(DEFAULT_FONT);
      componentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      componentList.setCellRenderer(new ComponentListCellRenderer(plugInPort.getComponentTypes()));
      selectionListener = new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting())
            return;

          List<ComponentGroupItem> selectedValuesList = componentList.getSelectedValuesList();
          Set<IDIYComponent<?>> expandedSelection = new HashSet<>();
          
          for (ComponentGroupItem selected : selectedValuesList) {
            // Always add all components from the group item
            expandedSelection.addAll(selected.getGroup());
          }
          
          if (expandedSelection.size() > 0 && !expandedSelection
              .equals(new HashSet<IDIYComponent<?>>(plugInPort.getSelectedComponents()))) {
            plugInPort.setSelection(expandedSelection, true);
          }
        }
      };
      componentList.addListSelectionListener(selectionListener);

      componentList.addMouseListener(new MouseAdapter() {

        private MouseEvent pressedEvent;
        
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            plugInPort.editSelection();
          }          
        }

        @Override
        public void mousePressed(MouseEvent e) {
          componentList.requestFocus();
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

                final List<ComponentGroupItem> selectedValues = componentList.getSelectedValuesList();
                // Extract actual components from selected items
                List<IDIYComponent<?>> components = new ArrayList<>();
                for (ComponentGroupItem item : selectedValues) {
                  components.addAll(item.getGroup());
                }
                getComponentPopupMenu().prepareAndShowAt(components, components, e.getX(),
                    e.getY());
              }
            }
          });
        }
      });
    }
    return componentList;
  }

  public ComponentListModel getComponentListModel() {
    if (componentListModel == null) {
      componentListModel = new ComponentListModel();
    }
    return componentListModel;
  }

  public ComponentPopupMenu getComponentPopupMenu() {
    if (popupMenu == null) {
      popupMenu = new ComponentPopupMenu(plugInPort, getComponentList(), false);
    }
    return popupMenu;
  }

  public void setComponents(List<IDIYComponent<?>> components,
      Collection<IDIYComponent<?>> selectedComponents) {
    this.getComponentListModel().setComponents(components);
    setSelection(new HashSet<IDIYComponent<?>>(selectedComponents));
  }

  public void setSelection(Set<IDIYComponent<?>> selectedComponents) {
    List<ComponentGroupItem> selectedValuesList = componentList.getSelectedValuesList();
    
    // Extract components from selected values
    Set<IDIYComponent<?>> currentSelected = new HashSet<>();
    for (ComponentGroupItem item : selectedValuesList) {
      currentSelected.addAll(item.getGroup());
    }
    
    if (currentSelected.equals(selectedComponents))
      return;

    final JList<ComponentGroupItem> list = this.getComponentList();

    // Temporarily remove listener to prevent infinite loop
    list.removeListSelectionListener(selectionListener);
    try {
      if (selectedComponents.size() == 0) {
        list.clearSelection();
      } else {
        // Build a map from component to its group (if any)
        Set<ComponentGroup> groups = plugInPort.getComponentGroups();
        Map<IDIYComponent<?>, Set<IDIYComponent<?>>> componentToGroup = new java.util.HashMap<>();
        if (groups != null && !groups.isEmpty()) {
          // Build a map from UUID to component for quick lookup
          List<IDIYComponent<?>> allComponents = plugInPort.getCurrentProject().getComponents();
          Map<UUID, IDIYComponent<?>> idToComponent = allComponents.stream()
              .collect(Collectors.toMap(IDIYComponent::getId, comp -> comp));
          
          for (ComponentGroup group : groups) {
            // Convert ComponentGroup (with UUIDs) to Set<IDIYComponent<?>>
            Set<IDIYComponent<?>> componentSet = group.getComponentIds().stream()
                .map(idToComponent::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
            
            for (IDIYComponent<?> component : componentSet) {
              componentToGroup.put(component, componentSet);
            }
          }
        }

        // Find indices of items to select
        List<ComponentGroupItem> components = this.getComponentListModel().getComponents();
        Set<Integer> selectedIndices = new HashSet<>();
        Set<Set<IDIYComponent<?>>> selectedGroups = new HashSet<>();
        
        for (IDIYComponent<?> selected : selectedComponents) {
          Set<IDIYComponent<?>> group = componentToGroup.get(selected);
          if (group != null) {
            // Component is in a group
            if (!selectedGroups.contains(group)) {
              // Find the ComponentGroupItem for this group
              for (int i = 0; i < components.size(); i++) {
                ComponentGroupItem groupItem = components.get(i);
                if (groupItem.getGroup().equals(group)) {
                  selectedIndices.add(i);
                  selectedGroups.add(group);
                  break;
                }
              }
            }
          } else {
            // Component is not in any group, find the ComponentGroupItem that contains only this component
            for (int i = 0; i < components.size(); i++) {
              ComponentGroupItem groupItem = components.get(i);
              if (groupItem.getGroup().size() == 1 && groupItem.getGroup().contains(selected)) {
                selectedIndices.add(i);
                break;
              }
            }
          }
        }

        int[] indices = selectedIndices.stream().mapToInt(i -> i).toArray();

        list.getSelectionModel().setValueIsAdjusting(true);
        list.setSelectedIndices(indices);
        list.getSelectionModel().setValueIsAdjusting(false);
        if (indices.length > 0) {
          int firstVisibleIndex = list.getFirstVisibleIndex();
          int lastVisibleIndex = list.getLastVisibleIndex();
          // if none of the selected items is visible, jump to the first one
          if (!Arrays.stream(indices)
              .anyMatch(index -> firstVisibleIndex <= index && index <= lastVisibleIndex))
            list.ensureIndexIsVisible(indices[0]);
        }
      }
    } finally {
      // Re-add listener
      list.addListSelectionListener(selectionListener);
    }
  }

  private class ComponentListModel extends AbstractListModel<ComponentGroupItem> {

    private static final long serialVersionUID = 1L;

    private List<IDIYComponent<?>> componentsRaw;
    private List<ComponentGroupItem> components;

    private String searchText;
    private Sort sort = Sort.Z_INDEX;

    public void setComponents(List<IDIYComponent<?>> components) {
      this.componentsRaw = components;
      int oldSize = getSize();

      refresh(oldSize);
    }

    public List<ComponentGroupItem> getComponents() {
      return components;
    }

    public Sort getSort() {
      return sort;
    }

    public void setSort(Sort sort) {
      this.sort = sort;

      refresh(getSize());
    }

    public void setSearchText(String searchText) {
      this.searchText = searchText.toLowerCase();

      refresh(getSize());
    }

    private void refresh(int oldSize) {
      List<IDIYComponent<?>> sortedComponents;
      
      if (sort == Sort.NAME) {
        sortedComponents =
            componentsRaw.stream().sorted(Comparator.comparing(x -> x.toString().toLowerCase()))
                .collect(Collectors.toList());
      } else if (sort == Sort.TYPE) {
        sortedComponents = componentsRaw.stream()
            .sorted(Comparator.comparing(x -> x.getClass().getName().toLowerCase())
                .thenComparing(x -> x.toString().toLowerCase()))
            .collect(Collectors.toList());
      } else {
        List<IDIYComponent<?>> componentsClone = new ArrayList<IDIYComponent<?>>(componentsRaw);
        Collections.reverse(componentsClone);
        sortedComponents = componentsClone;
      }

      if (searchText != null && !searchText.trim().isEmpty()) {
        sortedComponents = sortedComponents.stream().filter(component -> {
          return component.getName().toLowerCase().contains(searchText);
        }).collect(Collectors.toList());
      }

      // Build list with groups and ungrouped components
      this.components = buildComponentListWithGroups(sortedComponents);

      int newSize = this.components.size();

      fireContentsChanged(this, 0, Math.min(newSize, oldSize));
      if (oldSize > newSize)
        fireIntervalRemoved(this, newSize, oldSize);
      else if (newSize > oldSize)
        fireIntervalAdded(this, oldSize, newSize);
    }
    
    /**
     * Builds the component list, using ComponentGroupItem for all items.
     * For grouped components, creates one ComponentGroupItem per group.
     * For ungrouped components, creates a ComponentGroupItem with a single component.
     */
    private List<ComponentGroupItem> buildComponentListWithGroups(List<IDIYComponent<?>> sortedComponents) {
      Set<ComponentGroup> groups = plugInPort.getComponentGroups();
      
      // Build a map from component to its group (if any) and group name
      Map<IDIYComponent<?>, Set<IDIYComponent<?>>> componentToGroup = new java.util.HashMap<>();
      Map<Set<IDIYComponent<?>>, String> groupToName = new java.util.HashMap<>();
      if (groups != null && !groups.isEmpty()) {
        // Build a map from UUID to component for quick lookup
        List<IDIYComponent<?>> allComponents = plugInPort.getCurrentProject().getComponents();
        Map<UUID, IDIYComponent<?>> idToComponent = allComponents.stream()
            .collect(Collectors.toMap(IDIYComponent::getId, comp -> comp));
        
        for (ComponentGroup group : groups) {
          // Convert ComponentGroup (with UUIDs) to Set<IDIYComponent<?>>
          Set<IDIYComponent<?>> componentSet = group.getComponentIds().stream()
              .map(idToComponent::get)
              .filter(java.util.Objects::nonNull)
              .collect(Collectors.toSet());
          
          for (IDIYComponent<?> component : componentSet) {
            componentToGroup.put(component, componentSet);
          }
          
          // Store the group name
          if (group.getName() != null && !group.getName().isEmpty()) {
            groupToName.put(componentSet, group.getName());
          }
        }
      }
      
      // Track which groups we've already added
      Set<Set<IDIYComponent<?>>> addedGroups = new HashSet<>();
      List<ComponentGroupItem> result = new ArrayList<>();
      
      for (IDIYComponent<?> component : sortedComponents) {
        Set<IDIYComponent<?>> group = componentToGroup.get(component);
        if (group == null) {
          // Component is not in any group, create a ComponentGroupItem with just this component
          Set<IDIYComponent<?>> singleComponentGroup = new HashSet<>();
          singleComponentGroup.add(component);
          result.add(new ComponentGroupItem(singleComponentGroup, null));
        } else {
          // Component is in a group
          if (!addedGroups.contains(group)) {
            // This is the first component from this group we've seen, add the group item
            String groupName = groupToName.get(group);
            result.add(new ComponentGroupItem(group, groupName));
            addedGroups.add(group);
          }
          // Otherwise, skip this component as we already have a group item for it
        }
      }
      
      return result;
    }

    @Override
    public int getSize() {
      return this.components == null ? 0 : this.components.size();
    }

    @Override
    public ComponentGroupItem getElementAt(int index) {
      return this.components.get(index);
    }
  }

  private static class ComponentListCellRenderer implements ListCellRenderer<ComponentGroupItem> {

    // private static Color[] layerColors = new Color[] { Color.gray, Color.decode("#ff6363"),
    // Color.decode("#293462"), Color.decode("#138588"),
    // Color.decode("#c0ca03"), Color.decode("#990000") };

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    private Map<String, ComponentType> componentTypes;

    public ComponentListCellRenderer(Map<String, List<ComponentType>> componentTypeMap) {
      this.componentTypes = componentTypeMap.values().stream().flatMap(x -> x.stream())
          .collect(Collectors.toMap(x -> x.getInstanceClass().getCanonicalName(), x -> x));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ComponentGroupItem> list,
        ComponentGroupItem value, int index, boolean isSelected, boolean cellHasFocus) {

      JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
          isSelected, cellHasFocus);

      ComponentGroupItem groupItem = value;
      
      if (groupItem.getComponentCount() == 1) {
        // Render as individual component (ungrouped)
        IDIYComponent<?> component = groupItem.getGroup().iterator().next();
        ComponentType componentType = componentTypes.get(component.getClass().getCanonicalName());
        
        if (componentType == null) {
          renderer.setText(component.toString());
          return renderer;
        }

        int layerId = (int) Math.round(componentType.getZOrder());

        String valueForDisplay = component.getValueForDisplay();
        if (isSelected) {
          if (valueForDisplay == null || valueForDisplay.trim().isEmpty()) {
            renderer.setText(String.format("[L%s] %s", layerId, component.getName()));
          } else {
            renderer
                .setText(String.format("[L%s] %s (%s)", layerId, component.getName(), valueForDisplay));
          }
        } else {
          if (valueForDisplay == null || valueForDisplay.trim().isEmpty()) {
            renderer.setText(String.format("<html><font color='#c0c0c0'>[L%s]</font> %s</html>",
                layerId, component.getName()));
          } else {
            renderer.setText(String.format(
                "<html><font color='#c0c0c0'>[L%s]</font> %s (<font color='#666666'>%s</font>)</html>",
                layerId, component.getName(), valueForDisplay));
          }
        }
      } else {
        // Render as group
        // Calculate maximum layer from components in the group
        int maxLayerId = 0;
        for (IDIYComponent<?> component : groupItem.getGroup()) {
          ComponentType componentType = componentTypes.get(component.getClass().getCanonicalName());
          if (componentType != null) {
            int layerId = (int) Math.round(componentType.getZOrder());
            if (layerId > maxLayerId) {
              maxLayerId = layerId;
            }
          }
        }
        
        // Format: name (component count), similar to regular components showing name (value)
        String groupName = groupItem.getGroupName() != null && !groupItem.getGroupName().isEmpty()
            ? groupItem.getGroupName()
            : "Group";
        String componentCount = String.valueOf(groupItem.getComponentCount());
        
        if (isSelected) {
          renderer.setText(String.format("<html>[L%s] <b>%s</b> (<b>%s</b>)</html>", maxLayerId, groupName, componentCount));
        } else {
          renderer.setText(String.format(
              "<html><font color='#c0c0c0'>[L%s]</font> %s (<font color='#666666'>%s</font>)</html>",
              maxLayerId, groupName, componentCount));
        }
      }

      return renderer;
    }
  }

  private static enum Sort {
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
  
  /**
   * Wrapper class to represent a group of components in the explorer list.
   */
  public static class ComponentGroupItem {
    private final Set<IDIYComponent<?>> group;
    private final String groupName;
    
    public ComponentGroupItem(Set<IDIYComponent<?>> group, String groupName) {
      this.group = group;
      this.groupName = groupName;
    }
    
    public Set<IDIYComponent<?>> getGroup() {
      return group;
    }
    
    public int getComponentCount() {
      return group.size();
    }
    
    public String getGroupName() {
      return groupName;
    }
    
    @Override
    public String toString() {
      if (groupName != null && !groupName.isEmpty()) {
        return groupName;
      }
      return "Group of " + group.size() + " components";
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null || getClass() != obj.getClass())
        return false;
      ComponentGroupItem other = (ComponentGroupItem) obj;
      return group.equals(other.group);
    }
    
    @Override
    public int hashCode() {
      return group.hashCode();
    }
  }
}
