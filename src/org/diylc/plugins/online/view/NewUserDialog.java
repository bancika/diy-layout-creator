package org.diylc.plugins.online.view;

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

public class NewUserDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;

	private JTextField userNameField;
	private JPasswordField passwordField;
	private JPasswordField confirmPasswordField;
	private JTextField emailField;

	private String userName;
	private String password;
	private String email;

	public NewUserDialog(JFrame owner) {
		super(owner, "New Account", new String[] { OK, CANCEL });
		setMinimumSize(new Dimension(240, 32));
		layoutGui();
		refreshState();
		userNameField.setText(System.getProperty("user.name"));
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
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

			gbc.gridy = 2;
			mainPanel.add(new JLabel("Confirm Password:"), gbc);

			gbc.gridy = 3;
			mainPanel.add(new JLabel("eMail:"), gbc);

			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;

			gbc.gridy = 0;
			mainPanel.add(getUserNameField(), gbc);

			gbc.gridy = 1;
			mainPanel.add(getPasswordField(), gbc);

			gbc.gridy = 2;
			mainPanel.add(getConfirmPasswordField(), gbc);

			gbc.gridy = 3;
			mainPanel.add(getEmailField(), gbc);
		}
		return mainPanel;
	}

	private void refreshState() {
		this.userName = getUserNameField().getText();
		String password = new String(getPasswordField().getPassword());
		String confirmPassword = new String(getConfirmPasswordField().getPassword());
		if (password.equals(confirmPassword)) {
			this.password = password;
		} else {
			this.password = null;
		}
		this.email = getEmailField().getText();
		JButton okButton = getButton(OK);
		okButton.setEnabled((this.userName.length() > 0) && (this.password != null)
				&& (this.password.length() > 0));
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

	private JTextField getEmailField() {
		if (emailField == null) {
			emailField = new JTextField();
			emailField.getDocument().addDocumentListener(new DocumentListener() {

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
		return emailField;
	}
}
