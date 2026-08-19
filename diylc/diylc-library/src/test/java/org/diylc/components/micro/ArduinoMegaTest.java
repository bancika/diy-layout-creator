package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Assert;
import org.junit.Test;

public class ArduinoMegaTest {

  @Test
  public void testControlPointCountAndNames() {
    ArduinoMega mega = new ArduinoMega();
    Assert.assertEquals(98, mega.getControlPointCount());

    for (int i = 0; i < mega.getControlPointCount(); i++) {
      String name = mega.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Power header (0..7)
    Assert.assertEquals("NC", mega.getControlPointNodeName(0));
    Assert.assertEquals("IOREF", mega.getControlPointNodeName(1));
    Assert.assertEquals("RESET", mega.getControlPointNodeName(2));
    Assert.assertEquals("3.3V", mega.getControlPointNodeName(3));
    Assert.assertEquals("5V", mega.getControlPointNodeName(4));
    Assert.assertEquals("GND1", mega.getControlPointNodeName(5));
    Assert.assertEquals("GND2", mega.getControlPointNodeName(6));
    Assert.assertEquals("VIN", mega.getControlPointNodeName(7));

    // Analog Low header (8..15)
    Assert.assertEquals("A0", mega.getControlPointNodeName(8));
    Assert.assertEquals("A7", mega.getControlPointNodeName(15));

    // Analog High header (16..23)
    Assert.assertEquals("A8", mega.getControlPointNodeName(16));
    Assert.assertEquals("A15", mega.getControlPointNodeName(23));

    // Digital low header (24..31)
    Assert.assertEquals("D0 (RX0)", mega.getControlPointNodeName(24));
    Assert.assertEquals("D7 (~)", mega.getControlPointNodeName(31));

    // Digital high header (32..41)
    Assert.assertEquals("D8 (~)", mega.getControlPointNodeName(32));
    Assert.assertEquals("SCL", mega.getControlPointNodeName(41));

    // Communication header (42..49)
    Assert.assertEquals("D14 (TX3)", mega.getControlPointNodeName(42));
    Assert.assertEquals("D21 (SCL)", mega.getControlPointNodeName(49));

    // Double Digital 2x18 Header (50..85)
    Assert.assertEquals("D22", mega.getControlPointNodeName(50));
    Assert.assertEquals("D23", mega.getControlPointNodeName(51));
    Assert.assertEquals("D52 (SCK)", mega.getControlPointNodeName(80));
    Assert.assertEquals("D53 (SS)", mega.getControlPointNodeName(81));
    Assert.assertEquals("GND_EXT1", mega.getControlPointNodeName(82));
    Assert.assertEquals("GND_EXT2", mega.getControlPointNodeName(83));
    Assert.assertEquals("5V_EXT1", mega.getControlPointNodeName(84));
    Assert.assertEquals("5V_EXT2", mega.getControlPointNodeName(85));

    // Main ICSP header (86..91, ATmega2560)
    Assert.assertEquals("MISO", mega.getControlPointNodeName(86));
    Assert.assertEquals("5V_ICSP", mega.getControlPointNodeName(87));
    Assert.assertEquals("SCK", mega.getControlPointNodeName(88));
    Assert.assertEquals("MOSI", mega.getControlPointNodeName(89));
    Assert.assertEquals("RST_ICSP", mega.getControlPointNodeName(90));
    Assert.assertEquals("GND_ICSP", mega.getControlPointNodeName(91));

    // Top-left ICSP header (92..97, ATmega16U2)
    Assert.assertEquals("MISO_16U2", mega.getControlPointNodeName(92));
    Assert.assertEquals("5V_16U2", mega.getControlPointNodeName(93));
    Assert.assertEquals("SCK_16U2", mega.getControlPointNodeName(94));
    Assert.assertEquals("MOSI_16U2", mega.getControlPointNodeName(95));
    Assert.assertEquals("RST_16U2", mega.getControlPointNodeName(96));
    Assert.assertEquals("GND_16U2", mega.getControlPointNodeName(97));
  }

  @Test
  public void testPinGeometryAndGaps() {
    ArduinoMega mega = new ArduinoMega();

    // Power header spacing (20px per pin)
    for (int i = 0; i < 7; i++) {
      Point2D p1 = mega.getControlPoint(i);
      Point2D p2 = mega.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getY(), p2.getY(), 0.01);
    }

    // Gap between VIN (Pin 7) and A0 (Pin 8) is 0.2" = 40px
    Point2D pVin = mega.getControlPoint(7);
    Point2D pA0 = mega.getControlPoint(8);
    Assert.assertEquals(40.0, pVin.distance(pA0), 0.01);

    // Gap between A7 (Pin 15) and A8 (Pin 16) is 0.2" = 40px
    Point2D pA7 = mega.getControlPoint(15);
    Point2D pA8 = mega.getControlPoint(16);
    Assert.assertEquals(40.0, pA7.distance(pA8), 0.01);

    // D0 aligns with A5 (dx = 280 from NC)
    Point2D pD0 = mega.getControlPoint(24);
    Point2D pA5 = mega.getControlPoint(13);
    Assert.assertEquals(pA5.getX(), pD0.getX(), 0.01);

    // D7 aligns with VIN (dx = 140 from NC)
    Point2D pD7 = mega.getControlPoint(31);
    Assert.assertEquals(pVin.getX(), pD7.getX(), 0.01);

    // Gap between D7 (Pin 31) and D8 (Pin 32) is 0.16" = 32px
    Point2D pD8 = mega.getControlPoint(32);
    Assert.assertEquals(32.0, pD7.distance(pD8), 0.01);

    // D14 aligns with A7 (dx = 320 from NC)
    Point2D pD14 = mega.getControlPoint(42);
    Assert.assertEquals(pA7.getX(), pD14.getX(), 0.01);

    // Gap between D0 (Pin 24) and D14 (Pin 42) is 0.2" = 40px
    Assert.assertEquals(40.0, pD0.distance(pD14), 0.01);

    // Double Digital 2x18 Header (Pin 50 inner, Pin 51 outer)
    Point2D pNc = mega.getControlPoint(0);
    Point2D pD22 = mega.getControlPoint(50);
    Point2D pD23 = mega.getControlPoint(51);
    Assert.assertEquals(20.0, pD22.distance(pD23), 0.01);
    Assert.assertEquals(pD22.getY(), pD23.getY(), 0.01);
    Assert.assertEquals(500.0, pD22.getX() - pNc.getX(), 0.01);
    Assert.assertEquals(520.0, pD23.getX() - pNc.getX(), 0.01);

    // Main ICSP header (Pin 86 MISO, Pin 87 5V_ICSP)
    Point2D pMiso = mega.getControlPoint(86);
    Point2D p5vIcsp = mega.getControlPoint(87);
    Assert.assertEquals(20.0, pMiso.distance(p5vIcsp), 0.01);

    // Top-left ICSP header (Pin 92 MISO_16U2, Pin 93 5V_16U2)
    Point2D pMiso16 = mega.getControlPoint(92);
    Point2D p5v16 = mega.getControlPoint(93);
    Assert.assertEquals(20.0, pMiso16.distance(p5v16), 0.01);
  }

  @Test
  public void testBodyShape() {
    ArduinoMega mega = new ArduinoMega();

    Shape body = mega.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();
    Assert.assertEquals(799.78, bounds.getWidth(), 1.0);
    Assert.assertEquals(419.78, bounds.getHeight(), 1.0);
  }
}
