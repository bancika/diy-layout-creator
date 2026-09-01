package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiTest {

  @Test
  public void testControlPointCountAndNames() {
    RaspberryPi pi = new RaspberryPi();
    Assert.assertEquals(48, pi.getControlPointCount());

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
    Assert.assertEquals("PoE TR0 (Pin 1)", pi.getControlPointNodeName(40));
    Assert.assertEquals("PoE TR1 (Pin 2)", pi.getControlPointNodeName(41));
    Assert.assertEquals("PoE TR2 (Pin 3)", pi.getControlPointNodeName(42));
    Assert.assertEquals("PoE TR3 (Pin 4)", pi.getControlPointNodeName(43));
    Assert.assertEquals("PCIe", pi.getControlPointNodeName(44));
    Assert.assertEquals("MIPI 1", pi.getControlPointNodeName(45));
    Assert.assertEquals("MIPI 0", pi.getControlPointNodeName(46));
    Assert.assertEquals("UART", pi.getControlPointNodeName(47));
  }

  @Test
  public void testDimensionsAndPinGeometry() {
    RaspberryPi pi = new RaspberryPi();

    // Board dimensions: 85 mm x 56 mm
    double expectedWidth = new Size(85.0d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(56.0d, SizeUnit.mm).convertToPixels();

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

    // Header center should be at 32.5 mm from left edge (dead in the middle between left hole 3.5mm and right hole 61.5mm)
    Point2D pLastOdd = pi.getControlPoint(38); // Pin 39 (col 19)
    double headerCenterX = (p0.getX() + pLastOdd.getX()) / 2.0;
    double expectedHeaderCenterX = bounds.getX() + new Size(32.5d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedHeaderCenterX, headerCenterX, 0.1);

    // PoE Header (pins 40..43) 2x2 header sitting 6mm above bottom-right mounting hole (X=61.5mm, Y=46.5mm)
    double poeExpectedCenterX = bounds.getX() + new Size(61.5d, SizeUnit.mm).convertToPixels();
    double poeExpectedCenterY = bounds.getY() + new Size(46.5d, SizeUnit.mm).convertToPixels();

    Point2D p40 = pi.getControlPoint(40);
    Point2D p41 = pi.getControlPoint(41);
    Point2D p42 = pi.getControlPoint(42);
    Point2D p43 = pi.getControlPoint(43);

    Assert.assertEquals(poeExpectedCenterX - spacing / 2.0, p40.getX(), 0.1);
    Assert.assertEquals(poeExpectedCenterY - spacing / 2.0, p40.getY(), 0.1);

    Assert.assertEquals(poeExpectedCenterX - spacing / 2.0, p41.getX(), 0.1);
    Assert.assertEquals(poeExpectedCenterY + spacing / 2.0, p41.getY(), 0.1);

    Assert.assertEquals(poeExpectedCenterX + spacing / 2.0, p42.getX(), 0.1);
    Assert.assertEquals(poeExpectedCenterY - spacing / 2.0, p42.getY(), 0.1);

    Assert.assertEquals(poeExpectedCenterX + spacing / 2.0, p43.getX(), 0.1);
    Assert.assertEquals(poeExpectedCenterY + spacing / 2.0, p43.getY(), 0.1);

    // Connector control points (PCIe = 44, MIPI 1 = 45, MIPI 0 = 46, UART = 47)
    Point2D pPcie = pi.getControlPoint(44);
    Point2D pMipi1 = pi.getControlPoint(45);
    Point2D pMipi0 = pi.getControlPoint(46);
    Point2D pUart = pi.getControlPoint(47);

    double expectedPcieCenterX = bounds.getX() + new Size(0.1d, SizeUnit.in).convertToPixels() - new Size(1.0d, SizeUnit.mm).convertToPixels() + new Size(3.0d, SizeUnit.mm).convertToPixels() / 2.0;
    double expectedPcieCenterY = bounds.getY() + new Size(1.0d, SizeUnit.in).convertToPixels() + new Size(0.75d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedPcieCenterX, pPcie.getX(), 0.1);
    Assert.assertEquals(expectedPcieCenterY, pPcie.getY(), 0.1);

    double expectedMipiCenterY = bounds.getY() + expectedHeight - new Size(1.0d, SizeUnit.mm).convertToPixels() - new Size(15.5d, SizeUnit.mm).convertToPixels() / 2.0;
    double expectedMipi1CenterX = bounds.getX() + new Size(48.5d, SizeUnit.mm).convertToPixels();
    double expectedMipi0CenterX = bounds.getX() + new Size(54.5d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedMipi1CenterX, pMipi1.getX(), 0.1);
    Assert.assertEquals(expectedMipiCenterY, pMipi1.getY(), 0.1);
    Assert.assertEquals(expectedMipi0CenterX, pMipi0.getX(), 0.1);
    Assert.assertEquals(expectedMipiCenterY, pMipi0.getY(), 0.1);

    double expectedUartCenterX = bounds.getX() + new Size(32.5d, SizeUnit.mm).convertToPixels();
    double expectedUartCenterY = bounds.getY() + new Size(52.5d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedUartCenterX, pUart.getX(), 0.1);
    Assert.assertEquals(expectedUartCenterY, pUart.getY(), 0.1);
  }
}
