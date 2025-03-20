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
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.diylc.swingframework.ButtonDialog;

import org.diylc.lang.LangUtil;

public class FindDialog extends ButtonDialog {    

  private static final long serialVersionUID = 1L;
  
  private static final String TOOLTIP_HTML = "<html>Use RegEx to search for component names, types and values.<br>"
      + "<br><b>Character classes:</b><br>"
      + ". - matches any character<br>"
      + "\\s - whitespace<br>"
      + "\\S - non-whitespace<br>"
      + "\\d - digit<br>"
      + "\\D - non-digit<br>"
      + "<br><b>Quantifiers:</b><br>"
      + "* - zero or more times<br>"
      + "? - zero or one time<br>"
      + "+ - one or more times<br>"
      + "<br><b>Examples:</b><br>"
      + "'R27' - matches a component named R27<br>"
      + "'Resistor' - matches all resistors<br>"
      + "'R\\d+' - matches components starting with R, followed by at least one digit"
      + "</html>";
  
  private static final String FIND = LangUtil.translate("Find");
  private static final String LOOK_FOR = LangUtil.translate("Look for");

  private JPanel mainPanel;

  private JTextField criteriaField;  
  private JLabel tooltipLabel;

  private String criteria;  

  public FindDialog(JFrame owner) {
    super(owner, FIND, new String[] {OK, CANCEL});
    setMinimumSize(new Dimension(400, 32));
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
      mainPanel.add(new JLabel(LOOK_FOR + ":"), gbc);

      gbc.gridx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;

      gbc.gridy = 0;
      mainPanel.add(getCriteriaField(), gbc);
      
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.gridwidth = 2;
      mainPanel.add(getTooltipLabel(), gbc);
    }
    return mainPanel;
  }
  
  private JLabel getTooltipLabel() {
    if (tooltipLabel == null) {
      tooltipLabel = new JLabel();
      tooltipLabel.setOpaque(true);
      tooltipLabel.setBackground(UIManager.getColor("ToolTip.background"));
      tooltipLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
          BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      tooltipLabel.setText(TOOLTIP_HTML);
    }
    return tooltipLabel;
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
