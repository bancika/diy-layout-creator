package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.diylc.images.IconLoader;

/**
 * JPanel that shows cloud animation while visible.
 * 
 * @author bancika
 */
public class SimpleCloudGlassPane extends JPanel implements MouseListener, KeyListener {

  public static final SimpleCloudGlassPane GLASS_PANE = new SimpleCloudGlassPane();

  private static final long serialVersionUID = -5344758920442881290L;


  public SimpleCloudGlassPane() {
    addKeyListener(this);
    addMouseListener(this);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    setOpaque(false);

    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.insets = new Insets(0, 0, 80, 0);
    JLabel cloudLabel = new JLabel(IconLoader.CloudWait.getIcon());
    cloudLabel.setHorizontalAlignment(SwingConstants.CENTER);
    add(cloudLabel, gbc);
//
//    gbc.gridy = 1;
//    gbc.weighty = 1.5;
//    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
//    JLabel label = new JLabel("<html><font size='5' color='#999999'>Querying the cloud...</font></html>");
//    label.setHorizontalAlignment(SwingConstants.CENTER);
//    add(label, gbc);
  }

  @Override
  public void mouseClicked(final MouseEvent pArg0) {}

  @Override
  public void mouseEntered(final MouseEvent pArg0) {}

  @Override
  public void mouseExited(final MouseEvent pArg0) {}

  @Override
  public void mousePressed(final MouseEvent pArg0) {}

  @Override
  public void mouseReleased(final MouseEvent pArg0) {}

  @Override
  public void keyPressed(KeyEvent e) {}

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void keyTyped(KeyEvent e) {}
}
