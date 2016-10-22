package org.diylc.swing.plugins.cloud.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swingframework.ButtonDialog;

public class UploadDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	private static final String BULLET = "&nbsp;&nbsp;&nbsp;&#8226;&nbsp;";
	private static final String TERMS_HTML = "<html>"
			+ BULLET
			+ "All content uploaded to DIY Cloud is shared under Creative Commons 3.0 Licence.<br>"
			+ BULLET
			+ "Only finished and resonably verified projects may be uploaded to the library.<br>"
			+ BULLET
			+ "Uploaded content may be reviewed and/or removed by the administrators.<br>"
			+ "</html>";

	private JPanel mainPanel;
	private JLabel termsLabel;
	private JCheckBox agreeBox;
	private JPanel thumbnailPanel;
	private JTextField nameField;
	private JComboBox categoryBox;
	private JTextArea descriptionArea;
	private JTextField keywordsField;

	private IPlugInPort plugInPort;

	public UploadDialog(JFrame owner, IPlugInPort plugInPort) {
		super(owner, "Upload A Project", new String[] { OK, CANCEL });
		this.plugInPort = plugInPort;
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
			gbc.insets = new Insets(2, 2, 2, 2);

			gbc.gridx = 0;
			gbc.gridy = 0;
			mainPanel.add(getThumbnailPanel(), gbc);

			gbc.gridy = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			mainPanel.add(getTermsLabel(), gbc);

			gbc.gridy = 2;
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
			termsLabel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(),
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			termsLabel.setText(TERMS_HTML);
		}
		return termsLabel;
	}

	public JPanel getThumbnailPanel() {
		if (thumbnailPanel == null) {
			thumbnailPanel = new JPanel(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;

			thumbnailPanel.add(new JComponent() {

				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {
					super.paint(g);

					Graphics2D g2d = (Graphics2D) g;
					Dimension d = UploadDialog.this.plugInPort
							.getCanvasDimensions(false);

					Rectangle rect = getBounds();

					g2d.setColor(Color.white);
					g2d.fill(rect);

					double projectRatio = d.getWidth() / d.getHeight();
					double actualRatio = rect.getWidth() / rect.getHeight();
					double zoomRatio;
					if (projectRatio > actualRatio) {
						zoomRatio = rect.getWidth() / d.getWidth();
					} else {
						zoomRatio = rect.getHeight() / d.getHeight();
					}

					g2d.scale(zoomRatio, zoomRatio);
					UploadDialog.this.plugInPort.draw(g2d,
							EnumSet.noneOf(DrawOption.class), null);
				}
			}, gbc);
			Dimension d = UploadDialog.this.plugInPort
					.getCanvasDimensions(false);
			if (d.height > d.width)
				thumbnailPanel.setPreferredSize(new Dimension(192 * d.width
						/ d.height, 192));
			else
				thumbnailPanel.setPreferredSize(new Dimension(192, 192
						* d.height / d.width));
			thumbnailPanel.setBorder(BorderFactory.createEtchedBorder());
		}
		return thumbnailPanel;
	}

	public JTextField getNameField() {
		return nameField;
	}

	public JComboBox getCategoryBox() {
		return categoryBox;
	}

	public JTextArea getDescriptionArea() {
		return descriptionArea;
	}

	public JTextField getKeywordsField() {
		return keywordsField;
	}
}
