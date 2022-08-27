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
package org.diylc.swing.plugins.cloud.view;

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
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.diylc.swingframework.ButtonDialog;

public class ChangePasswordDialog extends ButtonDialog {

  private static final long serialVersionUID = 1L;

  private JPanel mainPanel;

  private JPasswordField oldPasswordField;
  private JPasswordField newPasswordField;
  private JPasswordField confirmPasswordField;

  private String oldPassword;
  private String newPassword;

  public ChangePasswordDialog(JFrame owner) {
    super(owner, "Change Password", new String[] {OK, CANCEL});

    setMinimumSize(new Dimension(240, 32));
    layoutGui();
    refreshState();
  }

  public String getNewPassword() {
    return newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  @Override
  protected JComponent getMainComponent() {
    if (mainPanel == null) {
      mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.fill = GridBagConstraints.NONE;
      gbc.insets = new Insets(4, 2, 2, 2);

      gbc.gridx = 0;

      gbc.gridy = 0;
      mainPanel.add(new JLabel("Old Password:"), gbc);

      gbc.gridy = 1;
      mainPanel.add(new JLabel("New Password:"), gbc);

      gbc.gridy = 2;
      mainPanel.add(new JLabel("Confirm Password:"), gbc);

      gbc.gridx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.weightx = 1;

      gbc.gridy = 0;
      mainPanel.add(getOldPasswordField(), gbc);

      gbc.gridy = 1;
      mainPanel.add(getNewPasswordField(), gbc);

      gbc.gridy = 2;
      mainPanel.add(getConfirmPasswordField(), gbc);

    }
    return mainPanel;
  }

  private void refreshState() {
    this.oldPassword = new String(getOldPasswordField().getPassword());

    String newPassword = new String(getNewPasswordField().getPassword());
    String confirmPassword = new String(getConfirmPasswordField().getPassword());
    if (newPassword.equals(confirmPassword)) {
      this.newPassword = newPassword;
    } else {
      this.newPassword = null;
    }

    JButton okButton = getButton(OK);
    okButton.setEnabled((this.newPassword != null) && (this.newPassword.length() > 0) && (this.oldPassword != null)
        && (this.oldPassword.length() > 0));
  }

  private JPasswordField getOldPasswordField() {
    if (oldPasswordField == null) {
      oldPasswordField = new JPasswordField();
      oldPasswordField.getDocument().addDocumentListener(new DocumentListener() {

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
    return oldPasswordField;
  }

  private JPasswordField getNewPasswordField() {
    if (newPasswordField == null) {
      newPasswordField = new JPasswordField();
      newPasswordField.getDocument().addDocumentListener(new DocumentListener() {

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
    return newPasswordField;
  }

  private JPasswordField getConfirmPasswordField() {
    if (confirmPasswordField == null) {
      confirmPasswordField = new JPasswordField();
      confirmPasswordField.getDocument().addDocumentListener(new DocumentListener() {

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
    return confirmPasswordField;
  }
}
