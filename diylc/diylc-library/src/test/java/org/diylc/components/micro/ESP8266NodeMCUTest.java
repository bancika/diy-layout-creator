package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ESP8266NodeMCUTest {

  @Test
  public void testControlPointCountAndNames() {
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();
    Assert.assertEquals(30, mcu.getControlPointCount());

    for (int i = 0; i < mcu.getControlPointCount(); i++) {
      String name = mcu.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..14)
    Assert.assertEquals("A0 (ADC0)", mcu.getControlPointNodeName(0));
    Assert.assertEquals("RSV1", mcu.getControlPointNodeName(1));
    Assert.assertEquals("RSV2", mcu.getControlPointNodeName(2));
    Assert.assertEquals("SD3", mcu.getControlPointNodeName(3));
    Assert.assertEquals("SD2", mcu.getControlPointNodeName(4));
    Assert.assertEquals("SD1", mcu.getControlPointNodeName(5));
    Assert.assertEquals("CMD", mcu.getControlPointNodeName(6));
    Assert.assertEquals("SD0", mcu.getControlPointNodeName(7));
    Assert.assertEquals("CLK", mcu.getControlPointNodeName(8));
    Assert.assertEquals("GND1", mcu.getControlPointNodeName(9));
    Assert.assertEquals("3V3_1", mcu.getControlPointNodeName(10));
    Assert.assertEquals("EN", mcu.getControlPointNodeName(11));
    Assert.assertEquals("RST", mcu.getControlPointNodeName(12));
    Assert.assertEquals("GND2", mcu.getControlPointNodeName(13));
    Assert.assertEquals("VIN", mcu.getControlPointNodeName(14));

    // Right row (15..29)
    Assert.assertEquals("D0 (GPIO16)", mcu.getControlPointNodeName(15));
    Assert.assertEquals("D1 (GPIO5)", mcu.getControlPointNodeName(16));
    Assert.assertEquals("D2 (GPIO4)", mcu.getControlPointNodeName(17));
    Assert.assertEquals("D3 (GPIO0)", mcu.getControlPointNodeName(18));
    Assert.assertEquals("D4 (GPIO2)", mcu.getControlPointNodeName(19));
    Assert.assertEquals("3V3_2", mcu.getControlPointNodeName(20));
    Assert.assertEquals("GND3", mcu.getControlPointNodeName(21));
    Assert.assertEquals("D5 (GPIO14)", mcu.getControlPointNodeName(22));
    Assert.assertEquals("D6 (GPIO12)", mcu.getControlPointNodeName(23));
    Assert.assertEquals("D7 (GPIO13)", mcu.getControlPointNodeName(24));
    Assert.assertEquals("D8 (GPIO15)", mcu.getControlPointNodeName(25));
    Assert.assertEquals("RX (GPIO3)", mcu.getControlPointNodeName(26));
    Assert.assertEquals("TX (GPIO1)", mcu.getControlPointNodeName(27));
    Assert.assertEquals("GND4", mcu.getControlPointNodeName(28));
    Assert.assertEquals("3V3_3", mcu.getControlPointNodeName(29));
  }

  @Test
  public void testPinGeometryAndSpacing() {
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();

    // Left row pitch (20px per pin = 0.10")
    for (int i = 0; i < 14; i++) {
      Point2D p1 = mcu.getControlPoint(i);
      Point2D p2 = mcu.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (20px per pin = 0.10")
    for (int i = 15; i < 29; i++) {
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
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();

    Shape body = mcu.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(25.7d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(48.0d, SizeUnit.mm).convertToPixels();
    double expectedTopMargin = new Size(6.22d, SizeUnit.mm).convertToPixels();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    Point2D p0 = mcu.getControlPoint(0);
    Assert.assertEquals(expectedTopMargin, p0.getY() - bounds.getY(), 0.1);
  }

  @Test
  public void testHeadersProperty() {
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();
    Assert.assertFalse("Headers should be false by default", mcu.getHeaders());

    mcu.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", mcu.getHeaders());

    mcu.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", mcu.getHeaders());
  }
}
