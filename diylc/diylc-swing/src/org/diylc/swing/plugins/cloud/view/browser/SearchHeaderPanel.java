package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.diylc.images.IconLoader;

public class SearchHeaderPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JTextField searchField;
  private JComboBox categoryBox;
  private JComboBox sortBox;
  private JButton goButton;

  public SearchHeaderPanel() {
    setLayout(new GridBagLayout());

    this.setBackground(Color.white);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.insets = new Insets(2, 2, 2, 2);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0;
    this.add(new JLabel("Search For:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1;
    this.add(getSearchField(), gbc);

    gbc.gridx = 2;
    gbc.weightx = 0;
    this.add(new JLabel("Filter By Category:"), gbc);

    gbc.gridx = 3;
    gbc.weightx = 0.5;
    this.add(getCategoryBox(), gbc);

    gbc.gridx = 4;
    gbc.weightx = 0;
    this.add(new JLabel("Sort By:"), gbc);

    gbc.gridx = 5;
    this.add(getSortBox(), gbc);

    gbc.gridx = 6;
    gbc.weighty = 1;
    this.add(getGoButton(), gbc);
  }

  public void setFocus() {
    getSearchField().requestFocusInWindow();
  }

  public void updateLists(String[] categories, String[] sortings) {
    Object selectedCategory = getCategoryBox().getSelectedItem();
    Object selectedSorting = getSortBox().getSelectedItem();
    getCategoryBox().setModel(new DefaultComboBoxModel(categories));
    getSortBox().setModel(new DefaultComboBoxModel(sortings));
    getCategoryBox().setSelectedItem(selectedCategory);
    getSortBox().setSelectedItem(selectedSorting);
  }

  private JTextField getSearchField() {
    if (searchField == null) {
      searchField = new JTextField(60);
    }
    return searchField;
  }

  private JComboBox getCategoryBox() {
    if (categoryBox == null) {
      categoryBox = new JComboBox();
    }
    return categoryBox;
  }

  private JComboBox getSortBox() {
    if (sortBox == null) {
      sortBox = new JComboBox();
    }
    return sortBox;
  }

  public JButton getGoButton() {
    if (goButton == null) {
      goButton = new JButton("Go", IconLoader.DataFind.getIcon());
    }
    return goButton;
  }

  public String getSearchText() {
    return getSearchField().getText();
  }

  public String getCategory() {
    return getCategoryBox().getSelectedItem() == null ? "" : getCategoryBox().getSelectedItem().toString();
  }

  public String getSorting() {
    return getSortBox().getSelectedItem() == null ? "" : getSortBox().getSelectedItem().toString();
  }
}
