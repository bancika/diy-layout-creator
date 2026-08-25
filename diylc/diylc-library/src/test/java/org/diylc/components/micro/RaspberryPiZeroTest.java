package org.diylc.components.micro;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import org.diylc.common.Orientation;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiZeroTest {

  @Test
  public void testControlPointCountAndNames() {
    RaspberryPiZero pi = new RaspberryPiZero();
    Assert.assertEquals(40, pi.getControlPointCount());

    for (int i = 0; i < pi.getControlPointCount(); i++) {
      String name = pi.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    Assert.assertEquals("3.3V (Pin 1)", pi.getControlPointNodeName(0));
    Assert.assertEquals("5V (Pin 2)", pi.getControlPointNodeName(1));
    Assert.assertEquals("GPIO2/SDA (Pin 3)", pi.getControlPointNodeName(2));
    Assert.assertEquals("5V (Pin 4)", pi.getControlPointNodeName(3));
    Assert.assertEquals("GND (Pin 39)", pi.getControlPointNodeName(38));
    Assert.assertEquals("GPIO21 (Pin 40)", pi.getControlPointNodeName(39));
  }

  @Test
  public void testDimensionsAndPinGeometry() {
    RaspberryPiZero pi = new RaspberryPiZero();

    // Board dimensions: 65 mm x 30 mm
    double expectedWidth = new Size(65.0d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(30.0d, SizeUnit.mm).convertToPixels();

    Shape body = pi.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    // Verify Pin 1 location relative to board top-left edge
    // Pin 1 offset X: 8.37 mm from left edge (centered between holes at X=3.5mm and X=61.5mm)
    // Pin 1 offset Y: 4.77 mm from top edge (3.5 mm + spacing/2 = 4.77 mm)
    // Pin 2 offset Y: 2.23 mm from top edge (3.5 mm - spacing/2 = 2.23 mm)
    // Vertical center of header: (4.77 + 2.23) / 2 = 3.5 mm (dead-centered with mounting holes)
    double expectedPin1OffsetX = new Size(8.37d, SizeUnit.mm).convertToPixels();
    double expectedPin1OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() + new Size(0.1d, SizeUnit.in).convertToPixels() / 2.0;

    Point2D p0 = pi.getControlPoint(0); // Pin 1
    Assert.assertEquals(expectedPin1OffsetX, p0.getX() - bounds.getX(), 0.1);
    Assert.assertEquals(expectedPin1OffsetY, p0.getY() - bounds.getY(), 0.1);

    // Pin 2 (outer row) should be at Y = 2.23 mm from top edge (3.5 mm - spacing/2)
    Point2D p1 = pi.getControlPoint(1); // Pin 2
    double expectedPin2OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() - new Size(0.1d, SizeUnit.in).convertToPixels() / 2.0;
    Assert.assertEquals(p0.getX(), p1.getX(), 0.01);
    Assert.assertEquals(expectedPin2OffsetY, p1.getY() - bounds.getY(), 0.1);

    // Verify vertical center of header is dead in the middle with the 3.5mm mounting hole line
    double headerCenterY = (p0.getY() + p1.getY()) / 2.0 - bounds.getY();
    Assert.assertEquals(new Size(3.5d, SizeUnit.mm).convertToPixels(), headerCenterY, 0.1);

    // Check pin spacing (20px per 0.1" pitch)
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();
    for (int col = 0; col < 19; col++) {
      int pinOdd1 = col * 2;
      int pinOdd2 = (col + 1) * 2;
      Point2D po1 = pi.getControlPoint(pinOdd1);
      Point2D po2 = pi.getControlPoint(pinOdd2);
      Assert.assertEquals(spacing, po2.getX() - po1.getX(), 0.01);
      Assert.assertEquals(po1.getY(), po2.getY(), 0.01);

      int pinEven1 = col * 2 + 1;
      int pinEven2 = (col + 1) * 2 + 1;
      Point2D pe1 = pi.getControlPoint(pinEven1);
      Point2D pe2 = pi.getControlPoint(pinEven2);
      Assert.assertEquals(spacing, pe2.getX() - pe1.getX(), 0.01);
      Assert.assertEquals(pe1.getY(), pe2.getY(), 0.01);
    }

    // Header center should be at 32.5 mm from left edge
    Point2D pLastOdd = pi.getControlPoint(38); // Pin 39 (col 19)
    double headerCenterX = (p0.getX() + pLastOdd.getX()) / 2.0;
    double expectedHeaderCenterX = bounds.getX() + new Size(32.5d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedHeaderCenterX, headerCenterX, 0.1);
  }

  @Test
  public void testDrawingAndOutline() {
    RaspberryPiZero pi = new RaspberryPiZero();
    pi.setControlPoint(new Point2D.Double(200, 200), 0);

    BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 600, 600));

    Project project = new Project();
    final AtomicInteger trackingStartCount = new AtomicInteger(0);
    final AtomicInteger trackingStopCount = new AtomicInteger(0);

    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {
        trackingStartCount.incrementAndGet();
      }
      @Override public void stopTracking() {
        trackingStopCount.incrementAndGet();
      }
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    pi.draw(g2d, ComponentState.NORMAL, false, project, observer);
    Assert.assertEquals(1, trackingStartCount.get());
    Assert.assertEquals(1, trackingStopCount.get());

    pi.draw(g2d, ComponentState.SELECTED, false, project, observer);
    pi.draw(g2d, ComponentState.NORMAL, true, project, observer);
    pi.drawIcon(g2d, 32, 32);

    g2d.dispose();
  }

  @Test
  public void testRotation() {
    RaspberryPiZero pi = new RaspberryPiZero();
    pi.setControlPoint(new Point2D.Double(200, 200), 0);

    Point2D p0Initial = pi.getControlPoint(0);
    Point2D p1Initial = pi.getControlPoint(1);

    pi.setOrientation(Orientation._90);
    Point2D p0Rot = pi.getControlPoint(0);
    Point2D p1Rot = pi.getControlPoint(1);

    Assert.assertEquals(p0Initial.getX(), p0Rot.getX(), 0.01);
    Assert.assertEquals(p0Initial.getY(), p0Rot.getY(), 0.01);
    // After 90 deg clockwise rotation, p1 (which was at (x, y - spacing)) should be at (x + spacing, y)
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();
    Assert.assertEquals(p0Rot.getX() + spacing, p1Rot.getX(), 0.01);
    Assert.assertEquals(p0Rot.getY(), p1Rot.getY(), 0.01);
  }
}
