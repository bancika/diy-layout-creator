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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.lang.LangUtil;
import org.diylc.swing.gui.ComponentPopupMenu;
import org.diylc.swing.gui.SearchTextField;

public class ExplorerPane extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ExplorerPane.class);

  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);

  private IPlugInPort plugInPort;
  private JTextField searchField;
  private JScrollPane componentScrollPane;
  private ComponentListModel componentListModel;
  private JList<IDIYComponent<?>> componentList;
  private ComponentPopupMenu popupMenu;
  private JComboBox<Sort> sortBox;

  public ExplorerPane(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    setName("Project Explorer");

    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weighty = 0;
    gbc.gridwidth = 1;

    add(getSearchField(), gbc);
    
    gbc.gridy++;

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0;
    gbc.weighty = 0;

    add(getSortBox(), gbc);

    gbc.gridy++;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;
    gbc.weightx = 1;

    add(getComponentScrollPane(), gbc);

    setPreferredSize(new Dimension(180, 200));
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

  public JList<IDIYComponent<?>> getComponentList() {
    if (componentList == null) {
      componentList = new JList<IDIYComponent<?>>();
      componentList.setModel(getComponentListModel());
      componentList.setFont(DEFAULT_FONT);
      componentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      componentList.addListSelectionListener(new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting())
            return;

          List<IDIYComponent<?>> selectedValuesList = componentList.getSelectedValuesList();
          if (selectedValuesList.size() > 0 && !new HashSet<IDIYComponent<?>>(selectedValuesList)
              .equals(new HashSet<IDIYComponent<?>>(plugInPort.getSelectedComponents()))) {
            plugInPort.setSelection(selectedValuesList, true);
          }
        }
      });

      componentList.addMouseListener(new MouseAdapter() {

        private MouseEvent pressedEvent;

        @Override
        public void mousePressed(MouseEvent e) {
          ExplorerPane.this.requestFocus();
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

                final List<IDIYComponent<?>> selectedValues = componentList.getSelectedValuesList();
                getComponentPopupMenu().prepareAndShowAt(selectedValues, selectedValues, e.getX(),
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

  public void setComponents(List<IDIYComponent<?>> components) {
    this.getComponentListModel().setComponents(components);
  }

  public void setSelection(Set<IDIYComponent<?>> selectedComponents) {
    List<IDIYComponent<?>> selectedValuesList = componentList.getSelectedValuesList();
    if (new HashSet<IDIYComponent<?>>(selectedValuesList)
        .equals(new HashSet<IDIYComponent<?>>(plugInPort.getSelectedComponents())))
      return;

    final JList<IDIYComponent<?>> list = this.getComponentList();

    if (selectedComponents.size() == 0) {
      list.clearSelection();
    } else {

      int[] indices = new int[selectedComponents.size()];
      int n = 0;
      List<IDIYComponent<?>> components = this.getComponentListModel().getComponents();
      for (int i = 0; i < components.size(); i++) {
        indices[n] = -1; // this should not happen
        if (selectedComponents.contains(components.get(i)))
          indices[n++] = i;
        if (n == indices.length)
          break;
      }

      list.getSelectionModel().setValueIsAdjusting(true);
      list.setSelectedIndices(indices);
      list.getSelectionModel().setValueIsAdjusting(false);
      int firstVisibleIndex = list.getFirstVisibleIndex();
      int lastVisibleIndex = list.getFirstVisibleIndex();
      // if none of the selected items is visible, jump to the first one
      if (!Arrays.stream(indices)
          .anyMatch(index -> firstVisibleIndex <= index && index <= lastVisibleIndex))
        list.ensureIndexIsVisible(indices[0]);
    }
  }

  private class ComponentListModel extends AbstractListModel<IDIYComponent<?>> {

    private static final long serialVersionUID = 1L;

    private List<IDIYComponent<?>> componentsRaw;
    private List<IDIYComponent<?>> components;

    private String searchText;
    private Sort sort;

    public void setComponents(List<IDIYComponent<?>> components) {
      this.componentsRaw = components;
      int oldSize = getSize();

      refresh(oldSize);
    }

    public List<IDIYComponent<?>> getComponents() {
      return components;
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
      if (sort == Sort.NAME) {
        this.components =
            componentsRaw.stream().sorted(Comparator.comparing(x -> x.toString().toLowerCase()))
                .collect(Collectors.toList());
      } else if (sort == Sort.TYPE) {
        this.components = componentsRaw.stream()
            .sorted(Comparator.comparing(x -> x.getClass().getName().toLowerCase())
                .thenComparing(x -> x.toString().toLowerCase()))
            .collect(Collectors.toList());
      } else {
        List<IDIYComponent<?>> componentsClone = new ArrayList<IDIYComponent<?>>(componentsRaw);
        Collections.reverse(componentsClone);
        this.components = componentsClone;
      }

      if (searchText != null && !searchText.trim().isEmpty()) {
        this.components = this.components.stream().filter(component -> {
          return component.getName().toLowerCase().contains(searchText);
        }).collect(Collectors.toList());
      }

      int newSize = this.components.size();

      fireContentsChanged(this, 0, Math.min(newSize, oldSize));
      if (oldSize > newSize)
        fireIntervalRemoved(this, newSize, oldSize);
      else if (newSize > oldSize)
        fireIntervalAdded(this, oldSize, newSize);
    }

    @Override
    public int getSize() {
      return this.components == null ? 0 : this.components.size();
    }

    @Override
    public IDIYComponent<?> getElementAt(int index) {
      return this.components.get(index);
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
}
