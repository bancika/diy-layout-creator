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
package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.VolatileImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.diylc.images.IconLoader;

/**
 * JPanel that shows cloud animation while visible.
 * 
 * @author bancika
 */
public class AnimatedCloudGlassPane extends JPanel implements MouseListener, KeyListener {

  public static final AnimatedCloudGlassPane GLASS_PANE = new AnimatedCloudGlassPane();

  private static final long serialVersionUID = -5344758920442881290L;

  private long startTime;
  private final int msPerPx = 6;
  private final Color bgColor = new Color(192, 222, 237);

  private Timer animationTimer;

  private VolatileImage volatileImg;

  public AnimatedCloudGlassPane() {
    addKeyListener(this);
    addMouseListener(this);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    setOpaque(false);
    setBackground(bgColor);

    setLayout(new GridBagLayout());


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

  private void createBackBuffer() {
    GraphicsConfiguration gc = getGraphicsConfiguration();
    volatileImg = gc.createCompatibleVolatileImage(getWidth(), getHeight());
  }

  @Override
  public void paint(java.awt.Graphics g) {

    createBackBuffer();

    do {

      // Validate the volatile image for the graphics configuration of this
      // component. If the volatile image doesn't apply for this graphics configuration
      // (in other words, the hardware acceleration doesn't apply for the new device)
      // then we need to re-create it.
      GraphicsConfiguration gc = this.getGraphicsConfiguration();
      int valCode = volatileImg.validate(gc);

      // This means the device doesn't match up to this hardware accelerated image.
      if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
        createBackBuffer(); // recreate the hardware accelerated image.
      }

      Graphics offscreenGraphics = volatileImg.getGraphics();
      Graphics2D g2d = (Graphics2D) offscreenGraphics;
      Image cloud = IconLoader.CloudBg.getImage();

      g2d.setColor(bgColor);
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
      g2d.drawString(text, x, y);

      // paint back buffer to main graphics
      g.drawImage(volatileImg, 0, 0, this);
      // Test if content is lost
    } while (volatileImg.contentsLost());
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
