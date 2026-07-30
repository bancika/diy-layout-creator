/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.pickup.CoilDefinition;
import org.diylc.components.guitar.pickup.ElectricalSpec;
import org.diylc.components.guitar.pickup.Measurement;
import org.diylc.components.guitar.pickup.MagnetSpec;
import org.diylc.components.guitar.pickup.PhysicalSpec;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.components.guitar.pickup.PickupLibrary;
import org.diylc.components.guitar.pickup.PickupSelectionModel;
import org.diylc.components.guitar.pickup.SourceReference;
import org.diylc.components.guitar.pickup.TerminalDefinition;

/**
 * Search/filter/select dialog for the pickup library, per the task's UX: pre-filled format
 * filter, free-text search across manufacturer/model/variant, format and manufacturer filters,
 * a results table, a read-only details panel (intrinsic specs and terminal colours - no
 * installation controls), and Apply/Cancel.
 *
 * <p>All filtering/selection logic lives in {@link PickupSelectionModel}, which has no Swing
 * dependency and is unit tested on its own; this class is a thin view over it.
 */
public class PickupLibraryDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private static final String ANY_FORMAT_LABEL = "Any Format";
  private static final String ANY_MANUFACTURER_LABEL = "Any Manufacturer";

  private final PickupSelectionModel model;
  private boolean applied = false;

  private JPanel mainPanel;
  private JTextField searchField;
  private JComboBox<String> formatCombo;
  private JComboBox<String> manufacturerCombo;
  private JTable resultsTable;
  private PickupTableModel tableModel;
  private JTextArea detailsArea;
  private JButton applyButton;
  private JButton cancelButton;

  public PickupLibraryDialog(Window owner, PickupSelectionModel model) {
    super(owner, "Select Pickup from Library");
    this.model = model;
    setModal(true);
    setContentPane(getMainPanel());
    selectRowForId(model.getSelectedDefinitionId());
    setSize(720, 480);
    setLocationRelativeTo(owner);
  }

  /**
   * Convenience entry point used by the "Select Pickup from Library..." action: shows the dialog
   * pre-filled/preselected from the given component and returns the definition the user applied,
   * or {@code null} if the dialog was cancelled.
   */
  public static PickupDefinition showDialog(Component parent, AbstractGuitarPickup component) {
    PickupSelectionModel model = PickupSelectionModel.forComponent(PickupLibrary.getInstance(), component);
    Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
    PickupLibraryDialog dialog = new PickupLibraryDialog(owner, model);
    dialog.setVisible(true);
    return dialog.applied ? model.getSelectedDefinition() : null;
  }

  public boolean isApplied() {
    return applied;
  }

  private JPanel getMainPanel() {
    if (mainPanel == null) {
      mainPanel = new JPanel(new BorderLayout(4, 4));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      mainPanel.add(getFilterPanel(), BorderLayout.NORTH);
      mainPanel.add(getResultsSplitPane(), BorderLayout.CENTER);
      mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
    }
    return mainPanel;
  }

  private JPanel getFilterPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    c.fill = GridBagConstraints.HORIZONTAL;

    c.gridx = 0;
    c.gridy = 0;
    panel.add(new JLabel("Search:"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(getSearchField(), c);

    c.gridx = 2;
    c.weightx = 0;
    panel.add(new JLabel("Format:"), c);
    c.gridx = 3;
    panel.add(getFormatCombo(), c);

    c.gridx = 4;
    panel.add(new JLabel("Manufacturer:"), c);
    c.gridx = 5;
    panel.add(getManufacturerCombo(), c);

    return panel;
  }

  private JTextField getSearchField() {
    if (searchField == null) {
      searchField = new JTextField();
      searchField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          onSearchTextChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          onSearchTextChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          onSearchTextChanged();
        }
      });
    }
    return searchField;
  }

  private void onSearchTextChanged() {
    model.setSearchText(searchField.getText());
    refreshResults();
  }

  private JComboBox<String> getFormatCombo() {
    if (formatCombo == null) {
      List<String> items = new ArrayList<>();
      items.add(ANY_FORMAT_LABEL);
      for (PickupFormat format : PickupFormat.values()) {
        items.add(format.name());
      }
      formatCombo = new JComboBox<>(items.toArray(new String[0]));
      formatCombo.setSelectedItem(model.getFormatFilter() == null ? ANY_FORMAT_LABEL : model.getFormatFilter().name());
      formatCombo.addActionListener(e -> {
        String selected = (String) formatCombo.getSelectedItem();
        model.setFormatFilter(ANY_FORMAT_LABEL.equals(selected) ? null : PickupFormat.valueOf(selected));
        refreshResults();
      });
    }
    return formatCombo;
  }

  private JComboBox<String> getManufacturerCombo() {
    if (manufacturerCombo == null) {
      List<String> items = new ArrayList<>();
      items.add(ANY_MANUFACTURER_LABEL);
      items.addAll(model.getAvailableManufacturers());
      manufacturerCombo = new JComboBox<>(items.toArray(new String[0]));
      manufacturerCombo.addActionListener(e -> {
        String selected = (String) manufacturerCombo.getSelectedItem();
        model.setManufacturerFilter(ANY_MANUFACTURER_LABEL.equals(selected) ? null : selected);
        refreshResults();
      });
    }
    return manufacturerCombo;
  }

  private JSplitPane getResultsSplitPane() {
    tableModel = new PickupTableModel(model.getResults());
    resultsTable = new JTable(tableModel);
    resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    resultsTable.getSelectionModel().addListSelectionListener(this::onTableSelectionChanged);

    detailsArea = new JTextArea();
    detailsArea.setEditable(false);
    detailsArea.setLineWrap(true);
    detailsArea.setWrapStyleWord(true);
    detailsArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    JSplitPane splitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(resultsTable), new JScrollPane(detailsArea));
    splitPane.setResizeWeight(0.65);
    splitPane.setPreferredSize(new Dimension(700, 360));
    return splitPane;
  }

  private void onTableSelectionChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;
    }
    int row = resultsTable.getSelectedRow();
    PickupDefinition selected = row < 0 ? null : tableModel.getRowAt(row);
    model.setSelectedDefinitionId(selected == null ? null : selected.id());
    detailsArea.setText(formatDetails(selected));
    detailsArea.setCaretPosition(0);
    getApplyButton().setEnabled(selected != null);
  }

  private JPanel getButtonPanel() {
    JPanel panel = new JPanel();
    panel.add(getApplyButton());
    panel.add(getCancelButton());
    return panel;
  }

  private JButton getApplyButton() {
    if (applyButton == null) {
      applyButton = new JButton("Apply");
      applyButton.setEnabled(model.getSelectedDefinitionId() != null);
      applyButton.addActionListener(e -> {
        applied = true;
        dispose();
      });
    }
    return applyButton;
  }

  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> {
        applied = false;
        dispose();
      });
    }
    return cancelButton;
  }

  private void refreshResults() {
    tableModel.setRows(model.getResults());
    selectRowForId(model.getSelectedDefinitionId());
  }

  private void selectRowForId(String id) {
    if (id == null) {
      resultsTable.clearSelection();
      return;
    }
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      if (id.equals(tableModel.getRowAt(i).id())) {
        resultsTable.setRowSelectionInterval(i, i);
        resultsTable.scrollRectToVisible(resultsTable.getCellRect(i, 0, true));
        return;
      }
    }
    resultsTable.clearSelection();
  }

  private static String formatDetails(PickupDefinition d) {
    if (d == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(d.getDisplayName()).append('\n');
    sb.append("Format: ").append(d.format()).append('\n');
    if (d.stringCount() != null) {
      sb.append("Strings: ").append(d.stringCount()).append('\n');
    }
    sb.append('\n');

    ElectricalSpec electrical = d.electrical();
    if (electrical != null) {
      sb.append("Electrical:\n");
      appendMeasurement(sb, "  Series DC resistance", electrical.seriesDcResistance());
      appendMeasurement(sb, "  Series inductance", electrical.seriesInductance());
      appendMeasurement(sb, "  Parasitic capacitance", electrical.parasiticCapacitance());
      sb.append('\n');
    }

    MagnetSpec magnet = d.magnet();
    if (magnet != null && (magnet.material() != null || magnet.grade() != null)) {
      sb.append("Magnet: ");
      if (magnet.material() != null) {
        sb.append(magnet.material());
      }
      if (magnet.grade() != null) {
        sb.append(' ').append(magnet.grade());
      }
      sb.append("\n\n");
    }

    PhysicalSpec physical = d.physical();
    if (physical != null) {
      sb.append("Physical:\n");
      appendMeasurement(sb, "  Width", physical.width());
      appendMeasurement(sb, "  Length", physical.length());
      appendMeasurement(sb, "  Depth", physical.depth());
      appendMeasurement(sb, "  Pole spacing", physical.poleSpacing());
      sb.append('\n');
    }

    if (!d.coils().isEmpty()) {
      sb.append("Coils:\n");
      for (CoilDefinition coil : d.coils()) {
        sb.append("  ").append(coil.id());
        if (coil.magneticPolarity() != null) {
          sb.append(" - ").append(coil.magneticPolarity());
        }
        if (coil.polePieceType() != null) {
          sb.append(" - ").append(coil.polePieceType());
        }
        sb.append('\n');
      }
      sb.append('\n');
    }

    if (!d.terminals().isEmpty()) {
      sb.append("Terminals:\n");
      for (TerminalDefinition terminal : d.terminals()) {
        sb.append("  ").append(terminal.id());
        if (terminal.role() != null) {
          sb.append(" (").append(terminal.role()).append(')');
        }
        if (terminal.lead() != null && terminal.lead().displayName() != null) {
          sb.append(" - ").append(terminal.lead().displayName());
        }
        sb.append('\n');
      }
      sb.append('\n');
    }

    if (!d.sources().isEmpty()) {
      sb.append("Sources:\n");
      for (SourceReference source : d.sources()) {
        sb.append("  ").append(source.reference()).append('\n');
      }
    }

    return sb.toString();
  }

  private static void appendMeasurement(StringBuilder sb, String label, Measurement measurement) {
    if (measurement != null) {
      sb.append(label).append(": ").append(measurement).append('\n');
    }
  }

  private static final class PickupTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = {"Manufacturer", "Model", "Variant", "Format", "DC Resistance"};

    private List<PickupDefinition> rows;

    PickupTableModel(List<PickupDefinition> rows) {
      this.rows = rows;
    }

    void setRows(List<PickupDefinition> rows) {
      this.rows = rows;
      fireTableDataChanged();
    }

    PickupDefinition getRowAt(int row) {
      return rows.get(row);
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
      return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      PickupDefinition d = rows.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return d.manufacturer();
        case 1:
          return d.model();
        case 2:
          return d.variant() == null ? "" : d.variant();
        case 3:
          return d.format();
        case 4:
          return d.electrical() == null || d.electrical().seriesDcResistance() == null ? ""
              : d.electrical().seriesDcResistance().toString();
        default:
          return "";
      }
    }
  }
}
