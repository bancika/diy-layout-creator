package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Assert;
import org.junit.Test;

public class ArduinoNanoTest {

  @Test
  public void testControlPointCountAndNames() {
    ArduinoNano nano = new ArduinoNano();
    Assert.assertEquals(36, nano.getControlPointCount());

    for (int i = 0; i < nano.getControlPointCount(); i++) {
      String name = nano.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..14)
    Assert.assertEquals("D1 (TX)", nano.getControlPointNodeName(0));
    Assert.assertEquals("D0 (RX)", nano.getControlPointNodeName(1));
    Assert.assertEquals("RESET", nano.getControlPointNodeName(2));
    Assert.assertEquals("GND1", nano.getControlPointNodeName(3));
    Assert.assertEquals("D2", nano.getControlPointNodeName(4));
    Assert.assertEquals("D3 (~)", nano.getControlPointNodeName(5));
    Assert.assertEquals("D12", nano.getControlPointNodeName(14));

    // Right row (15..29)
    Assert.assertEquals("VIN", nano.getControlPointNodeName(15));
    Assert.assertEquals("GND2", nano.getControlPointNodeName(16));
    Assert.assertEquals("RST2", nano.getControlPointNodeName(17));
    Assert.assertEquals("5V", nano.getControlPointNodeName(18));
    Assert.assertEquals("A7", nano.getControlPointNodeName(19));
    Assert.assertEquals("A6", nano.getControlPointNodeName(20));
    Assert.assertEquals("A5", nano.getControlPointNodeName(21));
    Assert.assertEquals("A4", nano.getControlPointNodeName(22));
    Assert.assertEquals("A3", nano.getControlPointNodeName(23));
    Assert.assertEquals("A2", nano.getControlPointNodeName(24));
    Assert.assertEquals("A1", nano.getControlPointNodeName(25));
    Assert.assertEquals("A0", nano.getControlPointNodeName(26));
    Assert.assertEquals("AREF", nano.getControlPointNodeName(27));
    Assert.assertEquals("3.3V", nano.getControlPointNodeName(28));
    Assert.assertEquals("D13", nano.getControlPointNodeName(29));

    // ICSP header (30..35)
    Assert.assertEquals("MISO", nano.getControlPointNodeName(30));
    Assert.assertEquals("5V_ICSP", nano.getControlPointNodeName(31));
    Assert.assertEquals("SCK", nano.getControlPointNodeName(32));
    Assert.assertEquals("MOSI", nano.getControlPointNodeName(33));
    Assert.assertEquals("RST_ICSP", nano.getControlPointNodeName(34));
    Assert.assertEquals("GND_ICSP", nano.getControlPointNodeName(35));
  }

  @Test
  public void testPinGeometryAndSpacing() {
    ArduinoNano nano = new ArduinoNano();

    // Left row spacing (20px per pin = 0.10")
    for (int i = 0; i < 14; i++) {
      Point2D p1 = nano.getControlPoint(i);
      Point2D p2 = nano.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Right row spacing (20px per pin = 0.10")
    for (int i = 15; i < 29; i++) {
      Point2D p1 = nano.getControlPoint(i);
      Point2D p2 = nano.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 0.60" = 120px between Left row and Right row
    Point2D pLeftTop = nano.getControlPoint(0);
    Point2D pRightTop = nano.getControlPoint(15);
    Assert.assertEquals(120.0, pLeftTop.distance(pRightTop), 0.01);
    Assert.assertEquals(pLeftTop.getY(), pRightTop.getY(), 0.01);

    // ICSP header spacing: 20px (0.10") between pins
    Point2D pMiso = nano.getControlPoint(30);
    Point2D p5v = nano.getControlPoint(31);
    Assert.assertEquals(20.0, pMiso.distance(p5v), 0.01);

    Point2D pSck = nano.getControlPoint(32);
    Assert.assertEquals(20.0, pMiso.distance(pSck), 0.01);

    // ICSP inner row is aligned with Pin 0 / Pin 15 along Y
    Assert.assertEquals(pLeftTop.getY(), p5v.getY(), 0.01);

    // ICSP outer row is 20px above Pin 0 (towards top edge)
    Assert.assertEquals(pLeftTop.getY() - 20.0, pMiso.getY(), 0.01);
  }

  @Test
  public void testBodyShapeDimensions() {
    ArduinoNano nano = new ArduinoNano();

    Shape body = nano.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();
    // Width = 0.73" = 146px
    Assert.assertEquals(146.0, bounds.getWidth(), 0.1);
    // Length = 1.70" = 340px
    Assert.assertEquals(340.0, bounds.getHeight(), 0.1);

    // Margin from top edge to Pin 0 is 30px (0.15")
    Point2D p0 = nano.getControlPoint(0);
    Assert.assertEquals(30.0, p0.getY() - bounds.getY(), 0.1);
  }
}
