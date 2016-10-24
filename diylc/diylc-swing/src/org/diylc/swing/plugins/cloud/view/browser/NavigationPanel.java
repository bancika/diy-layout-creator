package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.diylc.images.IconLoader;

public class NavigationPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JButton prevPageButton;
  private JButton nextPageButton;
  private JLabel pageLabel;

  public NavigationPanel() {
    super(new GridBagLayout());

    this.setBackground(Color.white);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.SOUTHWEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    this.add(getPrevPageButton(), gbc);

    gbc.gridx++;
    gbc.weightx = 1;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    this.add(getPageLabel(), gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    gbc.anchor = GridBagConstraints.SOUTHWEST;
    gbc.fill = GridBagConstraints.NONE;
    this.add(getNextPageButton(), gbc);
  }

  public JButton getPrevPageButton() {
    if (prevPageButton == null) {
      prevPageButton = new JButton(IconLoader.NavLeftBlue.getIcon());
      prevPageButton.setBorderPainted(false);
      prevPageButton.setContentAreaFilled(false);
      prevPageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    return prevPageButton;
  }

  public JButton getNextPageButton() {
    if (nextPageButton == null) {
      nextPageButton = new JButton(IconLoader.NavRightBlue.getIcon());
      nextPageButton.setBorderPainted(false);
      nextPageButton.setContentAreaFilled(false);
      nextPageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    return nextPageButton;
  }

  public JLabel getPageLabel() {
    if (pageLabel == null) {
      pageLabel = new JLabel();
      pageLabel.setFont(pageLabel.getFont().deriveFont(12f));
      pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    return pageLabel;
  }
}
