package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiZeroTest {

  @Test
  public void testControlPointCountAndNames() {
    RaspberryPiZero pi = new RaspberryPiZero();
    Assert.assertEquals(41, pi.getControlPointCount());

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
    Assert.assertEquals("MIPI (CSI)", pi.getControlPointNodeName(40));
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

    // Camera Connector (CSI / MIPI) at pin index 40
    Point2D pCsi = pi.getControlPoint(40);
    double csiW = new Size(3.2d, SizeUnit.mm).convertToPixels();
    double csiH = new Size(16.5d, SizeUnit.mm).convertToPixels();
    double expectedCsiCenterX = bounds.getX() + expectedWidth - new Size(4.2d, SizeUnit.mm).convertToPixels() + csiW / 2.0;
    double expectedCsiCenterY = bounds.getY() + new Size(6.75d, SizeUnit.mm).convertToPixels() + csiH / 2.0;
    Assert.assertEquals(expectedCsiCenterX, pCsi.getX(), 0.1);
    Assert.assertEquals(expectedCsiCenterY, pCsi.getY(), 0.1);
  }

  @Test
  public void testHeadersProperty() {
    RaspberryPiZero pi = new RaspberryPiZero();
    Assert.assertFalse("Headers should be false by default", pi.getHeaders());

    pi.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", pi.getHeaders());

    pi.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", pi.getHeaders());
  }
}
