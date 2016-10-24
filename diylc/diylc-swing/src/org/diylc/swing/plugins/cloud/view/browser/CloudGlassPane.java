package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.diylc.images.IconLoader;

/**
 * JPanel that shows cloud animation while visible.
 * 
 * @author bancika
 */
public class CloudGlassPane extends JPanel implements MouseListener, KeyListener {

  public static final CloudGlassPane GLASS_PANE = new CloudGlassPane();

  private static final long serialVersionUID = -5344758920442881290L;

  private long startTime;
  private final int msPerPx = 6;

  private Timer animationTimer;

  public CloudGlassPane() {
    addKeyListener(this);
    addMouseListener(this);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    setOpaque(false);
    setBackground(new Color(192, 222, 237));

    animationTimer = new Timer(20, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        repaint();
      }
    });
  }

  @Override
  public void setVisible(boolean aFlag) {
    if (aFlag) {
      startTime = System.currentTimeMillis();
      animationTimer.start();
    } else
      animationTimer.stop();
    super.setVisible(aFlag);
  };

  @Override
  public void paint(java.awt.Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

    Image cloud = IconLoader.CloudBg.getImage();

    g2d.setColor(new Color(192, 222, 237));
    g2d.fillRect(0, cloud.getHeight(null), getWidth(), getHeight());

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    long ellapsed = System.currentTimeMillis() - startTime;
    long dx = (ellapsed / msPerPx) % cloud.getWidth(null);
    g2d.drawImage(cloud, -(int) dx, 0, null);
    if (cloud.getWidth(null) - dx < getWidth()) {
      g2d.drawImage(cloud, (int) (cloud.getWidth(null) - dx), 0, null);
    }

    g2d.setColor(Color.white);
    g2d.setFont(g2d.getFont().deriveFont(22f));
    String text = "Querying the cloud, please hold on...";
    FontMetrics metrics = g.getFontMetrics(g2d.getFont());
    Rectangle rect = new Rectangle(getSize());
    int x = (rect.width - metrics.stringWidth(text)) / 2;
    int y = ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
    g.drawString(text, x, y);
  };

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
