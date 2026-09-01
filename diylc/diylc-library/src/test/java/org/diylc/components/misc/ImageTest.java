package org.diylc.components.misc;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

import org.diylc.common.Orientation;
import org.diylc.common.Percentage;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.junit.Assert;
import org.junit.Test;

public class ImageTest {

  private static final IDrawingObserver NOOP_OBSERVER = new IDrawingObserver() {
    @Override public void startTracking() {}
    @Override public void stopTracking() {}
    @Override public void startTrackingContinuityArea(boolean positive) {}
    @Override public void stopTrackingContinuityArea() {}
    @Override public boolean isTrackingContinuityArea() { return false; }
    @Override public void setContinuityMarker(String marker) {}
  };

  @Test
  public void testImageRectangleScaleModeDefaultOrientation() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(100, 200), 0);
    image.setScale(new Percentage(100));

    ImageIcon icon = image.getImage();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    Rectangle2D bounds = image.getImageRectangle();
    Assert.assertEquals(100.0, bounds.getX(), 0.01);
    Assert.assertEquals(200.0, bounds.getY(), 0.01);
    Assert.assertEquals((double) w, bounds.getWidth(), 0.01);
    Assert.assertEquals((double) h, bounds.getHeight(), 0.01);
  }

  @Test
  public void testImageRectangleScaleMode50Percent() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(100, 200), 0);
    image.setScale(new Percentage(50));

    ImageIcon icon = image.getImage();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    Rectangle2D bounds = image.getImageRectangle();
    Assert.assertEquals(100.0, bounds.getX(), 0.01);
    Assert.assertEquals(200.0, bounds.getY(), 0.01);
    Assert.assertEquals(w * 0.5, bounds.getWidth(), 0.01);
    Assert.assertEquals(h * 0.5, bounds.getHeight(), 0.01);
  }

  @Test
  public void testImageRectangleRotated180() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(500, 500), 0);
    image.setScale(new Percentage(100));
    image.setOrientation(Orientation._180);

    ImageIcon icon = image.getImage();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    Rectangle2D bounds = image.getImageRectangle();
    Assert.assertEquals(500.0 - w, bounds.getX(), 0.01);
    Assert.assertEquals(500.0 - h, bounds.getY(), 0.01);
    Assert.assertEquals((double) w, bounds.getWidth(), 0.01);
    Assert.assertEquals((double) h, bounds.getHeight(), 0.01);
  }

  @Test
  public void testImageRectangleRotated90() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(500, 500), 0);
    image.setScale(new Percentage(100));
    image.setOrientation(Orientation._90);

    ImageIcon icon = image.getImage();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    Rectangle2D bounds = image.getImageRectangle();
    Assert.assertEquals(500.0 - h, bounds.getX(), 0.01);
    Assert.assertEquals(500.0, bounds.getY(), 0.01);
    Assert.assertEquals((double) h, bounds.getWidth(), 0.01);
    Assert.assertEquals((double) w, bounds.getHeight(), 0.01);
  }

  @Test
  public void testImageRectangleRotated270() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(500, 500), 0);
    image.setScale(new Percentage(100));
    image.setOrientation(Orientation._270);

    ImageIcon icon = image.getImage();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    Rectangle2D bounds = image.getImageRectangle();
    Assert.assertEquals(500.0, bounds.getX(), 0.01);
    Assert.assertEquals(500.0 - w, bounds.getY(), 0.01);
    Assert.assertEquals((double) h, bounds.getWidth(), 0.01);
    Assert.assertEquals((double) w, bounds.getHeight(), 0.01);
  }

  @Test
  public void testImageRectangleTwoPointsMode() {
    Image image = new Image();
    image.setSizingMode(Image.ImageSizingMode.TwoPoints);
    image.setControlPoint(new Point2D.Double(400, 500), 0);
    image.setControlPoint(new Point2D.Double(100, 200), 1);

    Rectangle2D bounds = image.getImageRectangle();
    Assert.assertEquals(100.0, bounds.getX(), 0.01);
    Assert.assertEquals(200.0, bounds.getY(), 0.01);
    Assert.assertEquals(300.0, bounds.getWidth(), 0.01);
    Assert.assertEquals(300.0, bounds.getHeight(), 0.01);
  }

  @Test
  public void testDrawWithNullClipDoesNotThrow() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(50, 50), 0);

    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(null);

    // Should not throw NPE
    image.draw(g2d, ComponentState.NORMAL, false, new Project(), NOOP_OBSERVER);
    g2d.dispose();
  }

  @Test
  public void testDrawRotatedImageRendersWhenRotatedIntoClip() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(100, 100), 0);
    image.setScale(new Percentage(100));
    image.setOrientation(Orientation._180);

    ImageIcon icon = image.getImage();
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();

    // The image at (100, 100) rotated 180 extends to (100 - w, 100 - h).
    // The unrotated rectangle would be (100, 100, w, h).
    // Clip at (0, 0, 100, 100) overlaps the rotated image, but NOT the unrotated rect.
    BufferedImage target = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = target.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 100, 100));

    // Must not skip rendering
    image.draw(g2d, ComponentState.NORMAL, false, new Project(), NOOP_OBSERVER);
    g2d.dispose();

    // Verify some pixels were drawn in the (0, 0, 100, 100) region
    boolean hasNonZeroPixel = false;
    for (int x = 0; x < 100; x++) {
      for (int y = 0; y < 100; y++) {
        if ((target.getRGB(x, y) & 0xFF000000) != 0) {
          hasNonZeroPixel = true;
          break;
        }
      }
      if (hasNonZeroPixel) break;
    }
    Assert.assertTrue("Rotated image should have rendered pixels in the clip region", hasNonZeroPixel);
  }

  @Test
  public void testDrawSkipsWhenCompletelyOutsideClip() {
    Image image = new Image();
    image.setControlPoint(new Point2D.Double(500, 500), 0);
    image.setScale(new Percentage(100));

    BufferedImage target = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = target.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 100, 100));

    image.draw(g2d, ComponentState.NORMAL, false, new Project(), NOOP_OBSERVER);
    g2d.dispose();

    // Target image should remain completely blank
    for (int x = 0; x < 100; x++) {
      for (int y = 0; y < 100; y++) {
        Assert.assertEquals(0, target.getRGB(x, y));
      }
    }
  }
}
