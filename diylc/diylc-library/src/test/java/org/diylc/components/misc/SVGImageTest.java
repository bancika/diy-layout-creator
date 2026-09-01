package org.diylc.components.misc;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.diylc.common.Orientation;
import org.diylc.common.Percentage;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.junit.Assert;
import org.junit.Test;

public class SVGImageTest {

  private static final IDrawingObserver NOOP_OBSERVER = new IDrawingObserver() {
    @Override public void startTracking() {}
    @Override public void stopTracking() {}
    @Override public void startTrackingContinuityArea(boolean positive) {}
    @Override public void stopTrackingContinuityArea() {}
    @Override public boolean isTrackingContinuityArea() { return false; }
    @Override public void setContinuityMarker(String marker) {}
  };

  @Test
  public void testSVGImageRectangleScaleMode() {
    SVGImage svg = new SVGImage();
    svg.setControlPoint(new Point2D.Double(100, 200), 0);
    svg.setScale(new Percentage(100));

    Rectangle2D bounds = svg.getImageRectangle();
    Assert.assertEquals(100.0, bounds.getX(), 0.01);
    Assert.assertEquals(200.0, bounds.getY(), 0.01);
    Assert.assertTrue(bounds.getWidth() > 0);
    Assert.assertTrue(bounds.getHeight() > 0);
  }

  @Test
  public void testSVGImageDrawWithNullClipDoesNotThrow() {
    SVGImage svg = new SVGImage();
    svg.setControlPoint(new Point2D.Double(50, 50), 0);

    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(null);

    svg.draw(g2d, ComponentState.NORMAL, false, new Project(), NOOP_OBSERVER);
    g2d.dispose();
  }

  @Test
  public void testSVGImageRotated180Clipping() {
    SVGImage svg = new SVGImage();
    svg.setControlPoint(new Point2D.Double(100, 100), 0);
    svg.setScale(new Percentage(100));
    svg.setOrientation(Orientation._180);

    Rectangle2D bounds = svg.getImageRectangle();
    Assert.assertTrue(bounds.getX() < 100.0);
    Assert.assertTrue(bounds.getY() < 100.0);

    BufferedImage target = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = target.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 100, 100));

    svg.draw(g2d, ComponentState.NORMAL, false, new Project(), NOOP_OBSERVER);
    g2d.dispose();
  }
}
