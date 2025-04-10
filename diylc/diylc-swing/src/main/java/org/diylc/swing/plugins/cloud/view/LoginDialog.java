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
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.diylc.lang.LangUtil;
import org.diylc.swing.plugins.cloud.actions.LoginAction;
import org.diylc.swingframework.ButtonDialog;

public class LoginDialog extends ButtonDialog {

  private static final long serialVersionUID = 1L;
  private static final String PASSWORD_RESET_URL = "http://www.diy-fever.com/diylc/api/v1/request_password_reset.php";

  private static final Logger LOG = Logger.getLogger(LoginDialog.class);

  private JPanel mainPanel;
  private JTextField userNameField;
  private JPasswordField passwordField;
  private JLabel forgotPasswordLabel;

  private String userName;
  private String password;

  public LoginDialog(JFrame owner) {
    super(owner, "Login", new String[] {OK, CANCEL});
    setMinimumSize(new Dimension(240, 32));
    layoutGui();
    refreshState();
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
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
      mainPanel.add(new JLabel(LangUtil.translate("User Name") + ":"), gbc);

      gbc.gridy = 1;
      mainPanel.add(new JLabel(LangUtil.translate("Password") + ":"), gbc);

      gbc.gridx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;

      gbc.gridy = 0;
      mainPanel.add(getUserNameField(), gbc);

      gbc.gridy = 1;
      mainPanel.add(getPasswordField(), gbc);
      
      // Add forgot password link
      gbc.gridy = 2;
      gbc.gridx = 1;
      gbc.anchor = GridBagConstraints.LINE_END;
      mainPanel.add(getForgotPasswordLabel(), gbc);
    }
    return mainPanel;
  }

  private JLabel getForgotPasswordLabel() {
    if (forgotPasswordLabel == null) {
      forgotPasswordLabel = new JLabel("<html><u>" + LangUtil.translate("Forgot Password?") + "</u></html>");
      forgotPasswordLabel.setForeground(Color.BLUE);
      forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      forgotPasswordLabel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          try {
            Desktop.getDesktop().browse(new URI(PASSWORD_RESET_URL));
          } catch (Exception ex) {
            LOG.error("Could not open password reset page", ex);
          }
        }
      });
    }
    return forgotPasswordLabel;
  }

  private void refreshState() {
    this.userName = getUserNameField().getText();
    this.password = new String(getPasswordField().getPassword());

    JButton okButton = getButton(OK);
    okButton.setEnabled((this.userName.length() > 0) && (this.password.length() > 0));
  }

  private JTextField getUserNameField() {
    if (userNameField == null) {
      userNameField = new JTextField();
      userNameField.getDocument().addDocumentListener(new DocumentListener() {

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
    return userNameField;
  }

  private JPasswordField getPasswordField() {
    if (passwordField == null) {
      passwordField = new JPasswordField();
      passwordField.getDocument().addDocumentListener(new DocumentListener() {

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
    return passwordField;
  }
}
