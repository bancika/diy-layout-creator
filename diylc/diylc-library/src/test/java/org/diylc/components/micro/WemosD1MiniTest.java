package org.diylc.components.micro;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class WemosD1MiniTest {

  @Test
  public void testControlPointCountAndNames() {
    WemosD1Mini mcu = new WemosD1Mini();
    Assert.assertEquals(16, mcu.getControlPointCount());

    for (int i = 0; i < mcu.getControlPointCount(); i++) {
      String name = mcu.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..7)
    Assert.assertEquals("RST", mcu.getControlPointNodeName(0));
    Assert.assertEquals("A0", mcu.getControlPointNodeName(1));
    Assert.assertEquals("D0 (GPIO16)", mcu.getControlPointNodeName(2));
    Assert.assertEquals("D5 (GPIO14)", mcu.getControlPointNodeName(3));
    Assert.assertEquals("D6 (GPIO12)", mcu.getControlPointNodeName(4));
    Assert.assertEquals("D7 (GPIO13)", mcu.getControlPointNodeName(5));
    Assert.assertEquals("D8 (GPIO15)", mcu.getControlPointNodeName(6));
    Assert.assertEquals("3V3", mcu.getControlPointNodeName(7));

    // Right row (8..15)
    Assert.assertEquals("5V", mcu.getControlPointNodeName(8));
    Assert.assertEquals("G (GND)", mcu.getControlPointNodeName(9));
    Assert.assertEquals("D4 (GPIO2)", mcu.getControlPointNodeName(10));
    Assert.assertEquals("D3 (GPIO0)", mcu.getControlPointNodeName(11));
    Assert.assertEquals("D2 (GPIO4)", mcu.getControlPointNodeName(12));
    Assert.assertEquals("D1 (GPIO5)", mcu.getControlPointNodeName(13));
    Assert.assertEquals("RX (GPIO3)", mcu.getControlPointNodeName(14));
    Assert.assertEquals("TX (GPIO1)", mcu.getControlPointNodeName(15));
  }

  @Test
  public void testPinGeometryAndSpacing() {
    WemosD1Mini mcu = new WemosD1Mini();

    // Left row pitch (20px per pin = 0.10")
    for (int i = 0; i < 7; i++) {
      Point2D p1 = mcu.getControlPoint(i);
      Point2D p2 = mcu.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (20px per pin = 0.10")
    for (int i = 8; i < 15; i++) {
      Point2D p1 = mcu.getControlPoint(i);
      Point2D p2 = mcu.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 0.90" = 180px between Left row and Right row
    Point2D pLeftTop = mcu.getControlPoint(0);
    Point2D pRightTop = mcu.getControlPoint(15);
    Assert.assertEquals(180.0, pLeftTop.distance(pRightTop), 0.01);
    Assert.assertEquals(pLeftTop.getY(), pRightTop.getY(), 0.01);
  }

  @Test
  public void testBodyShapeDimensions() {
    WemosD1Mini mcu = new WemosD1Mini();

    Shape body = mcu.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(1.0d, SizeUnit.in).convertToPixels();
    double expectedHeight = new Size(1.34d, SizeUnit.in).convertToPixels();
    double expectedTopMargin = new Size(0.275d, SizeUnit.in).convertToPixels();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    Point2D p0 = mcu.getControlPoint(0);
    Assert.assertEquals(expectedTopMargin, p0.getY() - bounds.getY(), 0.1);
  }

  @Test
  public void testDrawingModes() {
    WemosD1Mini mcu = new WemosD1Mini();
    mcu.setControlPoint(new Point2D.Double(100, 100), 0);

    BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 400, 400));

    Project project = new Project();
    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    // Normal mode
    mcu.draw(g2d, ComponentState.NORMAL, false, project, observer);
    // Selected mode
    mcu.draw(g2d, ComponentState.SELECTED, false, project, observer);
    // Outline mode
    mcu.draw(g2d, ComponentState.NORMAL, true, project, observer);
    // Icon
    mcu.drawIcon(g2d, 32, 32);

    g2d.dispose();
  }
}
