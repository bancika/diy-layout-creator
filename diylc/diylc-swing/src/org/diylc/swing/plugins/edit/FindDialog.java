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
package org.diylc.swing.plugins.edit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.swingframework.ButtonDialog;

public class FindDialog extends ButtonDialog {

  private static final long serialVersionUID = 1L;

  private JPanel mainPanel;

  private JTextField criteriaField;  

  private String criteria;  

  public FindDialog(JFrame owner) {
    super(owner, "Find", new String[] {OK, CANCEL});
    setMinimumSize(new Dimension(240, 32));
    layoutGui();
    refreshState();
  }

  public String getCriteria() {
    return criteria;
  }

  @Override
  protected JComponent getMainComponent() {
    if (mainPanel == null) {
      mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.LINE_START;
      gbc.fill = GridBagConstraints.NONE;
      gbc.insets = new Insets(2, 2, 2, 2);

      gbc.gridx = 0;

      gbc.gridy = 0;
      mainPanel.add(new JLabel("Look for:"), gbc);

      gbc.gridx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;

      gbc.gridy = 0;
      mainPanel.add(getCriteriaField(), gbc);
    }
    return mainPanel;
  }

  private void refreshState() {
    this.criteria = getCriteriaField().getText();

    JButton okButton = getButton(OK);
    okButton.setEnabled(this.criteria.length() > 0);
  }

  private JTextField getCriteriaField() {
    if (criteriaField == null) {
      criteriaField = new JTextField();
      criteriaField.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void removeUpdate(DocumentEvent e) {
          refreshState();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          refreshState();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          refreshState();
        }
      });
    }
    return criteriaField;
  }
}
