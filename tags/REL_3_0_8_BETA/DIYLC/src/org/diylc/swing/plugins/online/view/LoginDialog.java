package org.diylc.swing.plugins.online.view;

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
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.diyfever.gui.ButtonDialog;

public class LoginDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;

	private JTextField userNameField;
	private JPasswordField passwordField;

	private String userName;
	private String password;

	public LoginDialog(JFrame owner) {
		super(owner, "Login", new String[] { OK, CANCEL });
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
			mainPanel.add(new JLabel("User Name:"), gbc);

			gbc.gridy = 1;
			mainPanel.add(new JLabel("Password:"), gbc);

			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;

			gbc.gridy = 0;
			mainPanel.add(getUserNameField(), gbc);

			gbc.gridy = 1;
			mainPanel.add(getPasswordField(), gbc);
		}
		return mainPanel;
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
