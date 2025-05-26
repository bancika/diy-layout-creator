/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.diylc.swingframework.ButtonDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.swing.gui.components.HTMLTextArea;
import org.diylc.swing.plugins.cloud.ThumbnailGenerator;
import org.diylc.utils.KeywordExtractor;

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
  private JComboBox<String> categoryBox;
  private JScrollPane descriptionPane;
  private JTextArea descriptionArea;
  private JTextField keywordsField;

  private IPlugInPort plugInPort;
  private String[] categories;

  private ThumbnailGenerator thumbnailGenerator;

  public UploadDialog(JFrame owner, IPlugInPort plugInPort, String[] categories, boolean isUpdate) {
    super(owner, isUpdate ? "Re-Upload A Project" : "Upload A Project", new String[] {OK, CANCEL});
    this.plugInPort = plugInPort;
    this.thumbnailGenerator = new ThumbnailGenerator(plugInPort);
    this.categories = categories;
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
      mainPanel.add(getDescriptionPane(), gbc);

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

          UploadDialog.this.thumbnailGenerator.paintThumbnail(g, getBounds());
        }
      }, gbc);

      thumbnailPanel.setPreferredSize(this.thumbnailGenerator.getThumbnailSize());
      thumbnailPanel.setBorder(BorderFactory.createEtchedBorder());
    }
    return thumbnailPanel;
  }

  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();

      List<PropertyWrapper> props = plugInPort.getProperties(plugInPort.getCurrentProject());

      // set default from the project
      for (int i = 0; i < props.size(); i++)
        if (props.get(i).getName().equals("Title"))
          nameField.setText(props.get(i).getValue().toString());
    }
    return nameField;
  }

  private JComboBox<String> getCategoryBox() {
    if (categoryBox == null) {
      categoryBox = new JComboBox<String>(categories);
    }
    return categoryBox;
  }

  private JScrollPane getDescriptionPane() {
    if (descriptionPane == null) {
      descriptionPane = new JScrollPane(getDescriptionArea());
      descriptionPane.setBorder(getNameField().getBorder());
      descriptionPane.setPreferredSize(new Dimension(1, 128));
    }
    return descriptionPane;
  }

  private JTextArea getDescriptionArea() {
    if (descriptionArea == null) {
      descriptionArea = new HTMLTextArea();

      List<PropertyWrapper> props = plugInPort.getProperties(plugInPort.getCurrentProject());

      // set default from the project
      for (int i = 0; i < props.size(); i++)
        if (props.get(i).getName().equals("Description"))
          try {
            descriptionArea.setText(props.get(i).getValue().toString());
          } catch (Exception e) {
          }
      descriptionArea.setBorder(null);
      descriptionArea.setFont(getNameField().getFont());
    }
    return descriptionArea;
  }

  private JTextField getKeywordsField() {
    if (keywordsField == null) {
      keywordsField = new JTextField();
      keywordsField.setColumns(32);
      keywordsField.setText(KeywordExtractor.getInstance().extractKeywords(plugInPort.getCurrentProject()));
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

  public String getKeywords() {
    return getKeywordsField().getText();
  }
}
