package org.diylc.swing.plugins.online.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.diyfever.gui.ButtonDialog;

public class UploadDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	private static final String BULLET = "&nbsp;&nbsp;&nbsp;&#8226;&nbsp;";
	private static final String TERMS_HTML = "<html><b>Terms of Service</b><br><br>"
			+ BULLET
			+ "Online Library is provided free of charge for DIY Layout Creator users.<br>"
			+ BULLET
			+ "Only finished and verified projects may be uploaded to the library. Online Library is <b>not</b> a repository for unfinished projects and temprary work.<br>"
			+ BULLET
			+ "Projects uploaded to the library may be reviewed, modified or deleted by the administrator without notice.<br>"
			+ BULLET
			+ "Once uploaded, project becomes avaialble to all other users. Anyone may use the project, modify it and upload modified version. When modifying an existing projects credit for the original author must be left.<br>"
			+ BULLET
			+ "You can <b>not</b> use Online Library to advertise products or services.<br>"
			+ BULLET
			+ "Users that do not follow these terms may be banned from the server, with or without notice.<br>"
			+ BULLET + "Administrator is not responsible for library contents.<br>" + "</html>";

	private JPanel mainPanel;
	private JLabel termsLabel;
	private JCheckBox agreeBox;

	public UploadDialog(JFrame owner) {
		super(owner, "Upload", new String[] { OK, CANCEL });
		layoutGui();
		refreshState();
	}

	@Override
	protected JComponent getMainComponent() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new GridBagLayout());
			mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.insets = new Insets(2, 2, 2, 2);

			gbc.gridx = 0;
			gbc.gridy = 0;
			mainPanel.add(getTermsLabel(), gbc);

			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.NONE;
			gbc.weightx = 0;
			gbc.weighty = 0;
			mainPanel.add(getAgreeBox(), gbc);
		}
		return mainPanel;
	}

	private void refreshState() {
		getButton(OK).setEnabled(getAgreeBox().isSelected());
	}

	private JCheckBox getAgreeBox() {
		if (agreeBox == null) {
			agreeBox = new JCheckBox("I agree to these terms");
			agreeBox.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					refreshState();
				}
			});
		}
		return agreeBox;
	}

	private JLabel getTermsLabel() {
		if (termsLabel == null) {
			termsLabel = new JLabel();
			termsLabel.setOpaque(true);
			termsLabel.setBackground(UIManager.getColor("ToolTip.background"));
			termsLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
					.createEtchedBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			termsLabel.setText(TERMS_HTML);
		}
		return termsLabel;
	}
}
