/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
 * 
 */
package org.diylc.swing.plugins.file;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.lang.LangUtil;
import org.diylc.netlist.ParsedNetlistEntry;
import org.diylc.swingframework.ButtonDialog;

public class NetlistImportDialog extends ButtonDialog {

  private static final long serialVersionUID = 1L;

  public static String NETLIST_DEFAULT_TYPES = "netlistDefaultTypes";

  private List<Class<?>> selectedTypes;
  private List<Boolean> setDefault;
  private Map<Class<?>, String> componentTypeDisplay;
  private List<Class<?>> allTypes;
  private Map<String, String> netlistDefaultTypes;
  private List<String> rawTypes;
  private Map<String, List<Class<?>>> availableTypes;

  private JTable table;

  @SuppressWarnings("unchecked")
  public NetlistImportDialog(JFrame owner, IPlugInPort plugInPort,
      List<ParsedNetlistEntry> entries) {
    super(owner, LangUtil.translate("Import Netlist"),
        new String[] {ButtonDialog.OK, ButtonDialog.CANCEL});
    this.rawTypes = entries.stream().map(x -> x.getRawType()).distinct().sorted().collect(Collectors.toList());
    this.availableTypes = new HashMap<String, List<Class<?>>>();
    for (ParsedNetlistEntry entry : entries) {
      this.availableTypes.put(entry.getRawType(), entry.getTypeCandidates());
    }

    this.netlistDefaultTypes = (Map<String, String>) ConfigurationManager.getInstance()
        .readObject(NETLIST_DEFAULT_TYPES, null);
    this.setDefault = rawTypes.stream().map(
        type -> netlistDefaultTypes != null && netlistDefaultTypes.containsKey(type))
        .collect(Collectors.toList());

    Map<String, List<ComponentType>> componentTypes = plugInPort.getComponentTypes();
    List<ComponentType> allComponentTypes = new ArrayList<ComponentType>();
    this.componentTypeDisplay = new HashMap<Class<?>, String>();
    for (Map.Entry<String, List<ComponentType>> e : componentTypes.entrySet()) {
      for (ComponentType type : e.getValue()) {
        this.componentTypeDisplay.put(type.getInstanceClass(), e.getKey() + " - " + type.getName());
        allComponentTypes.add(type);
      }
    }
    this.allTypes = allComponentTypes.stream()
        .sorted((t1, t2) -> (t1.getCategory() + " = " + t1.getName())
            .compareToIgnoreCase((t2.getCategory() + " = " + t2.getName())))
        .map(x -> x.getInstanceClass()).collect(Collectors.toList());
    
    this.selectedTypes =
        rawTypes.stream().map(type -> getDefaultType(type)).collect(Collectors.toList());

    setPreferredSize(new Dimension(600, 400));
    layoutGui();
  }

  private Class<?> getDefaultType(String rawType) {
    if (this.netlistDefaultTypes != null
        && this.netlistDefaultTypes.containsKey(rawType)) {

      Optional<Class<?>> first = this.allTypes.stream()
          .filter(
              e -> e.getCanonicalName().equals(this.netlistDefaultTypes.get(rawType)))
          .findFirst();
      if (first.isPresent())
        return first.get();
    }
    
    List<Class<?>> list = this.availableTypes.get(rawType);
    return list == null || list.isEmpty() ? null : list.get(0);
  }

  public Map<String, Class<?>> getResults() {
    Map<String, Class<?>> result = new HashMap<String, Class<?>>();
    for (int i = 0; i < rawTypes.size(); i++)
      result.put(rawTypes.get(i), selectedTypes.get(i));
    return result;
  }

  @Override
  protected JComponent getMainComponent() {
    JScrollPane scrollPane = new JScrollPane(getTable());
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    return scrollPane;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    
    // hiding, store changes in config
    if (!b && OK.equals(getSelectedButtonCaption())) {
      netlistDefaultTypes = (Map<String, String>) ConfigurationManager.getInstance()
          .readObject(NETLIST_DEFAULT_TYPES, null);
      if (netlistDefaultTypes == null)
        netlistDefaultTypes = new HashMap<String, String>();
      for (int i = 0; i < rawTypes.size(); i++)
        if (setDefault.get(i))
          netlistDefaultTypes.put(rawTypes.get(i),
              selectedTypes.get(i).getCanonicalName());
        else
          netlistDefaultTypes.remove(rawTypes.get(i));
      
      ConfigurationManager.getInstance().writeValue(NETLIST_DEFAULT_TYPES, netlistDefaultTypes);
    }
  }

  public JTable getTable() {
    if (table == null) {
      table = new JTable(new NetlistTableModel());
      table.setFillsViewportHeight(true);
      JComboBox<Class<?>> comboBox = new JComboBox<Class<?>>();
      comboBox.setRenderer(new TypeListCellRenderer());
      table.setDefaultRenderer(Class.class, new TypeTableCellRenderer());
      table.setDefaultEditor(Class.class, new TypeCellEditor(comboBox));
      resizeColumnWidth(table);
    }
    return table;
  }

  public void resizeColumnWidth(JTable table) {
    final TableColumnModel columnModel = table.getColumnModel();
    for (int column = 0; column < table.getColumnCount(); column++) {
      int width = 48; // Min width
      for (int row = 0; row < table.getRowCount(); row++) {
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Component comp = table.prepareRenderer(renderer, row, column);
        width = Math.max(comp.getPreferredSize().width + 1, width);
      }
      if (width > 300)
        width = 300;
      columnModel.getColumn(column).setPreferredWidth(width);
    }
  }

  class NetlistTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    @Override
    public int getRowCount() {
      return rawTypes.size();
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {        
        case 0:
          return "Imported Type";
        case 1:
          return "DIYLC Component Type";
        case 2:
          return "Default";
      }
      return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {    
        case 0:
          return String.class;
        case 1:
          return Class.class;
        case 2:
          return Boolean.class;
      }
      return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex >= 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 0:
          return rawTypes.get(rowIndex);
        case 1:
          return selectedTypes.get(rowIndex);
        case 2:
          return setDefault.get(rowIndex);
      }
      return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case 1:
          selectedTypes.set(rowIndex, (Class<?>) aValue);
          break;
        case 2:
          setDefault.set(rowIndex, (Boolean) aValue);
          break;
      }
    }
  }

  class TypeListCellRenderer extends JLabel implements ListCellRenderer<Class<?>> {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<? extends Class<?>> list, Class<?> value,
        int index, boolean isSelected, boolean cellHasFocus) {
      if (value == null)
        setText("<html><i>Undefined - skip this component</i></html>");
      else
        setText(componentTypeDisplay.get(value));
      return this;
    }
  }

  class TypeTableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
          hasFocus, row, column);
      if (value == null)
        label.setText("<html><i>Undefined - skip this component</i></html>");
      else
        label.setText(componentTypeDisplay.get(value));
      return label;
    }
  }

  class TypeCellEditor extends DefaultCellEditor {

    private static final long serialVersionUID = 1L;

    public TypeCellEditor(JComboBox<Class<?>> comboBox) {
      super(comboBox);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {
      JComboBox<Class<?>> comboBox = (JComboBox<Class<?>>) super.getTableCellEditorComponent(table,
          value, isSelected, row, column);
      comboBox.removeAllItems();      
      List<Class<?>> showTypes = availableTypes.get(rawTypes.get(row));
      if (showTypes == null || showTypes.isEmpty())
        showTypes = allTypes;

      comboBox.addItem(null);
      for (Class<?> c : showTypes) {
        comboBox.addItem(c);
      }
      comboBox.setSelectedItem(selectedTypes.get(row));
      return comboBox;
    }
  }
}
