/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
import org.diylc.swing.images.IconLoader;

public class SearchHeaderPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JTextField searchField;
  private JComboBox categoryBox;
  private JComboBox sortBox;
  private JButton goButton;

  private boolean initialized = false;

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

  public void initializeLists(String[] categories, String[] sortings) {
    getCategoryBox().setModel(new DefaultComboBoxModel(categories));
    getSortBox().setModel(new DefaultComboBoxModel(sortings));
    initialized = true;
  }

  public boolean isInitialized() {
    return initialized;
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
