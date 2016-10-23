package org.diylc.swing.plugins.cloud.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swingframework.ButtonDialog;

public class UploadDialog extends ButtonDialog {

  private static final long serialVersionUID = 1L;

  private static final String BULLET = "&nbsp;&nbsp;&nbsp;&#8226;&nbsp;";
  private static final String TERMS_HTML = "<html>" + BULLET
      + "All content uploaded to DIY Cloud is shared under Creative Commons 3.0 Licence.<br>" + BULLET
      + "Only finished and resonably verified projects may be uploaded to the DIY Cloud.<br>" + BULLET
      + "Please provide a meaningful project description to make it possible for others to find the project.<br>"
      + BULLET + "If you are building on top of someone else's work, please leave credits in the description.<br>"
      + BULLET
      + "Uploaded content may be reviewed, edited and removed by the administrators if it is not up to standards.<br>"
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
  private CloudPresenter cloudPresenter;

  public UploadDialog(JFrame owner, IPlugInPort plugInPort, CloudPresenter cloudPresenter) {
    super(owner, "Upload A Project", new String[] {OK, CANCEL});
    this.plugInPort = plugInPort;
    this.cloudPresenter = cloudPresenter;
    layoutGui();
    getButton(OK).setEnabled(false);
  }

  @Override
  protected JComponent getMainComponent() {
    if (mainPanel == null) {
      mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.insets = new Insets(2, 2, 2, 2);

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.gridheight = 4;
      mainPanel.add(getThumbnailPanel(), gbc);

      gbc.gridheight = 1;
      gbc.gridx = 1;
      gbc.weightx = 0;
      gbc.insets = new Insets(4, 2, 2, 2);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.add(new JLabel("Project Name: "), gbc);

      gbc.gridy = 1;
      mainPanel.add(new JLabel("Category:"), gbc);

      gbc.gridy = 2;
      mainPanel.add(new JLabel("Description: "), gbc);

      gbc.gridy = 3;
      mainPanel.add(new JLabel("Keywords: "), gbc);

      gbc.gridx = 2;
      gbc.gridy = 0;
      gbc.weightx = 1;
      gbc.insets = new Insets(2, 2, 2, 2);
      mainPanel.add(getNameField(), gbc);

      gbc.gridy = 1;
      mainPanel.add(getCategoryBox(), gbc);

      gbc.gridy = 2;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.fill = GridBagConstraints.BOTH;
      mainPanel.add(getDescriptionArea(), gbc);

      gbc.gridy = 3;
      gbc.weighty = 0;
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.add(getKeywordsField(), gbc);

      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.gridwidth = 3;
      gbc.fill = GridBagConstraints.BOTH;
      mainPanel.add(getTermsLabel(), gbc);

      gbc.gridy = 5;
      gbc.anchor = GridBagConstraints.CENTER;
      gbc.fill = GridBagConstraints.NONE;
      gbc.weightx = 0;
      gbc.weighty = 0;
      mainPanel.add(getAgreeBox(), gbc);
    }
    return mainPanel;
  }

  private JCheckBox getAgreeBox() {
    if (agreeBox == null) {
      agreeBox = new JCheckBox("I agree to these terms");
      agreeBox.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          getButton(OK).setEnabled(getAgreeBox().isSelected());
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
      termsLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
          BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      termsLabel.setText(TERMS_HTML);
    }
    return termsLabel;
  }

  private JPanel getThumbnailPanel() {
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

          paintThumbnail(g, getBounds());
        }
      }, gbc);
      Dimension d = UploadDialog.this.plugInPort.getCanvasDimensions(false);
      if (d.height > d.width)
        thumbnailPanel.setPreferredSize(new Dimension(192 * d.width / d.height, 192));
      else
        thumbnailPanel.setPreferredSize(new Dimension(192, 192 * d.height / d.width));
      thumbnailPanel.setBorder(BorderFactory.createEtchedBorder());
    }
    return thumbnailPanel;
  }

  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();

      // set default from the project
      for (int i = 0; i < plugInPort.getProjectProperties().size(); i++)
        if (plugInPort.getProjectProperties().get(i).getName().equals("Title"))
          nameField.setText(plugInPort.getProjectProperties().get(i).getValue().toString());
    }
    return nameField;
  }

  private JComboBox getCategoryBox() {
    if (categoryBox == null) {
      try {
        categoryBox = new JComboBox(cloudPresenter.getCategories());
      } catch (CloudException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return categoryBox;
  }

  private JTextArea getDescriptionArea() {
    if (descriptionArea == null) {
      descriptionArea = new JTextArea();

      // set default from the project
      for (int i = 0; i < plugInPort.getProjectProperties().size(); i++)
        if (plugInPort.getProjectProperties().get(i).getName().equals("Description"))
          descriptionArea.setText(plugInPort.getProjectProperties().get(i).getValue().toString());
      descriptionArea.setBorder(getNameField().getBorder());
      descriptionArea.setFont(getNameField().getFont());
    }
    return descriptionArea;
  }

  private JTextField getKeywordsField() {
    if (keywordsField == null) {
      keywordsField = new JTextField();
    }
    return keywordsField;
  }

  public String getName() {
    return getNameField().getText();
  }

  public String getDescription() {
    return getDescriptionArea().getText();
  }

  public String getCategory() {
    return getCategoryBox().getSelectedItem().toString();
  }

  public BufferedImage getThumbnail() {
    BufferedImage thumbnailImage =
        new BufferedImage(getThumbnailPanel().getWidth(), getThumbnailPanel().getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D cg = thumbnailImage.createGraphics();

    paintThumbnail(cg, new Rectangle(thumbnailImage.getWidth(), thumbnailImage.getHeight()));
    return thumbnailImage;
  }

  private void paintThumbnail(Graphics g, Rectangle rect) {
    Graphics2D g2d = (Graphics2D) g;
    Dimension d = UploadDialog.this.plugInPort.getCanvasDimensions(false);

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
    UploadDialog.this.plugInPort.draw(g2d, EnumSet.of(DrawOption.ANTIALIASING), null);
  }

  public String getKeywords() {
    return getKeywordsField().getText();
  }
}
