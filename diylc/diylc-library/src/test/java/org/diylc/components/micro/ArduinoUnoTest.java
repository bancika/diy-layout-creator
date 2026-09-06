package org.diylc.components.micro;

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Test;

public class ArduinoUnoTest {

  @Test
  public void testControlPointCountAndNames() {
    ArduinoUno uno = new ArduinoUno();
    Assert.assertEquals(44, uno.getControlPointCount());

    for (int i = 0; i < uno.getControlPointCount(); i++) {
      String name = uno.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Power header
    Assert.assertEquals("NC", uno.getControlPointNodeName(0));
    Assert.assertEquals("IOREF", uno.getControlPointNodeName(1));
    Assert.assertEquals("RESET", uno.getControlPointNodeName(2));
    Assert.assertEquals("3.3V", uno.getControlPointNodeName(3));
    Assert.assertEquals("5V", uno.getControlPointNodeName(4));
    Assert.assertEquals("GND1", uno.getControlPointNodeName(5));
    Assert.assertEquals("GND2", uno.getControlPointNodeName(6));
    Assert.assertEquals("VIN", uno.getControlPointNodeName(7));

    // Analog header
    Assert.assertEquals("A0", uno.getControlPointNodeName(8));
    Assert.assertEquals("A5", uno.getControlPointNodeName(13));

    // Digital low header
    Assert.assertEquals("D0 (RX)", uno.getControlPointNodeName(14));
    Assert.assertEquals("D7", uno.getControlPointNodeName(21));

    // Digital high header
    Assert.assertEquals("D8", uno.getControlPointNodeName(22));
    Assert.assertEquals("SCL", uno.getControlPointNodeName(31));

    // ICSP header
    Assert.assertEquals("MISO", uno.getControlPointNodeName(32));
    Assert.assertEquals("GND_ICSP", uno.getControlPointNodeName(37));

    // Top-left ICSP header (16U2)
    Assert.assertEquals("MISO_16U2", uno.getControlPointNodeName(38));
    Assert.assertEquals("GND_16U2", uno.getControlPointNodeName(43));
  }

  @Test
  public void testPinGeometryAndGaps() {
    ArduinoUno uno = new ArduinoUno();

    // Power header spacing (20px per pin)
    for (int i = 0; i < 7; i++) {
      Point2D p1 = uno.getControlPoint(i);
      Point2D p2 = uno.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getY(), p2.getY(), 0.01);
    }

    // Gap between VIN (Pin 7) and A0 (Pin 8) is 0.2" = 40px
    Point2D pVin = uno.getControlPoint(7);
    Point2D pA0 = uno.getControlPoint(8);
    Assert.assertEquals(40.0, pVin.distance(pA0), 0.01);

    // D0 aligns with A5 (dx = 280 from NC)
    Point2D pD0 = uno.getControlPoint(14);
    Point2D pA5 = uno.getControlPoint(13);
    Assert.assertEquals(pA5.getX(), pD0.getX(), 0.01);

    // D7 aligns with VIN (dx = 140 from NC)
    Point2D pD7 = uno.getControlPoint(21);
    Assert.assertEquals(pVin.getX(), pD7.getX(), 0.01);

    // Gap between D7 (Pin 21) and D8 (Pin 22) is 0.16" = 32px
    Point2D pD8 = uno.getControlPoint(22);
    Assert.assertEquals(32.0, pD7.distance(pD8), 0.01);

    // Top-left ICSP header: 2 rows of 3 pins
    Point2D pIcsp2_0 = uno.getControlPoint(38);
    Point2D pIcsp2_1 = uno.getControlPoint(39);
    Assert.assertEquals(20.0, pIcsp2_0.distance(pIcsp2_1), 0.01);
  }

  @Test
  public void testVersionProperty() {
    ArduinoUno uno = new ArduinoUno();
    Assert.assertEquals("Default version should be R3", ArduinoUno.ArduinoUnoVersion.REV3, uno.getVersion());
    Assert.assertEquals("UNO R3", ArduinoUno.ArduinoUnoVersion.REV3.toString());
    Assert.assertEquals("UNO R4 WiFi", ArduinoUno.ArduinoUnoVersion.R4_WIFI.toString());
    Assert.assertEquals("UNO R4 Minima", ArduinoUno.ArduinoUnoVersion.R4_MINIMA.toString());
    Assert.assertEquals("Leonardo", ArduinoUno.ArduinoUnoVersion.LEONARDO.toString());

    uno.setVersion(ArduinoUno.ArduinoUnoVersion.R4_MINIMA);
    Assert.assertEquals(ArduinoUno.ArduinoUnoVersion.R4_MINIMA, uno.getVersion());
  }

  @Test
  public void testR4MinimaMatchesR4WiFi() {
    // The Minima is the same board without the radio, so pin count and geometry are identical.
    ArduinoUno wifi = new ArduinoUno();
    wifi.setVersion(ArduinoUno.ArduinoUnoVersion.R4_WIFI);
    ArduinoUno minima = new ArduinoUno();
    minima.setVersion(ArduinoUno.ArduinoUnoVersion.R4_MINIMA);

    Assert.assertEquals(47, wifi.getControlPointCount());
    Assert.assertEquals(47, minima.getControlPointCount());
    Assert.assertEquals(wifi.getBodyShape().getBounds2D(), minima.getBodyShape().getBounds2D());
    for (int i = 0; i < wifi.getControlPointCount(); i++) {
      Assert.assertEquals("Pin " + i, wifi.getControlPoint(i), minima.getControlPoint(i));
      Assert.assertEquals("Pin " + i, wifi.getControlPointNodeName(i), minima.getControlPointNodeName(i));
    }
  }

  @Test
  public void testLeonardoDropsSecondIcspHeader() {
    ArduinoUno leonardo = new ArduinoUno();
    leonardo.setVersion(ArduinoUno.ArduinoUnoVersion.LEONARDO);

    // No ATmega16U2, so the second 2x3 ICSP block is gone: 44 - 6 = 38 pins
    Assert.assertEquals(38, leonardo.getControlPointCount());

    // The shield footprint is unchanged, so the header pins land exactly where the R3's do
    ArduinoUno r3 = new ArduinoUno();
    Assert.assertEquals(r3.getBodyShape().getBounds2D(), leonardo.getBodyShape().getBounds2D());
    for (int i = 0; i < leonardo.getControlPointCount(); i++) {
      Assert.assertEquals("Pin " + i, r3.getControlPoint(i), leonardo.getControlPoint(i));
    }

    // I2C is shared with D2 / D3 on the Leonardo
    Assert.assertEquals("D2 (SDA)", leonardo.getControlPointNodeName(16));
    Assert.assertEquals("D3 (~, SCL)", leonardo.getControlPointNodeName(17));
    Assert.assertEquals("GND_ICSP", leonardo.getControlPointNodeName(37));

    for (int i = 0; i < leonardo.getControlPointCount(); i++) {
      String name = leonardo.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }
  }

  @Test
  public void testAllVersionsDrawing() {
    java.awt.image.BufferedImage img =
        new java.awt.image.BufferedImage(800, 800, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g2d = img.createGraphics();
    org.diylc.core.Project project = new org.diylc.core.Project();
    org.diylc.core.IDrawingObserver observer = new org.diylc.core.IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    for (ArduinoUno.ArduinoUnoVersion version : ArduinoUno.ArduinoUnoVersion.values()) {
      ArduinoUno uno = new ArduinoUno();
      uno.setVersion(version);
      uno.setControlPoint(new Point2D.Double(400, 400), 0);

      uno.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);
      uno.draw(g2d, org.diylc.core.ComponentState.SELECTED, false, project, observer);
      uno.draw(g2d, org.diylc.core.ComponentState.NORMAL, true, project, observer);
      uno.drawIcon(g2d, 32, 32);
    }

    g2d.dispose();
  }
}
