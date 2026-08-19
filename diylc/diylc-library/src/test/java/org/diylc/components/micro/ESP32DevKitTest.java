package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ESP32DevKitTest {

  @Test
  public void testControlPointCountAndNames() {
    ESP32DevKit devKit = new ESP32DevKit();
    Assert.assertEquals(30, devKit.getControlPointCount());

    for (int i = 0; i < devKit.getControlPointCount(); i++) {
      String name = devKit.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..14)
    Assert.assertEquals("EN", devKit.getControlPointNodeName(0));
    Assert.assertEquals("VP (GPIO36)", devKit.getControlPointNodeName(1));
    Assert.assertEquals("VN (GPIO39)", devKit.getControlPointNodeName(2));
    Assert.assertEquals("D34", devKit.getControlPointNodeName(3));
    Assert.assertEquals("D35", devKit.getControlPointNodeName(4));
    Assert.assertEquals("D32", devKit.getControlPointNodeName(5));
    Assert.assertEquals("D33", devKit.getControlPointNodeName(6));
    Assert.assertEquals("D25", devKit.getControlPointNodeName(7));
    Assert.assertEquals("D26", devKit.getControlPointNodeName(8));
    Assert.assertEquals("D27", devKit.getControlPointNodeName(9));
    Assert.assertEquals("D14", devKit.getControlPointNodeName(10));
    Assert.assertEquals("D12", devKit.getControlPointNodeName(11));
    Assert.assertEquals("GND1", devKit.getControlPointNodeName(12));
    Assert.assertEquals("D13", devKit.getControlPointNodeName(13));
    Assert.assertEquals("3V3", devKit.getControlPointNodeName(14));

    // Right row (15..29)
    Assert.assertEquals("VIN", devKit.getControlPointNodeName(15));
    Assert.assertEquals("GND2", devKit.getControlPointNodeName(16));
    Assert.assertEquals("D15", devKit.getControlPointNodeName(17));
    Assert.assertEquals("D2", devKit.getControlPointNodeName(18));
    Assert.assertEquals("D4", devKit.getControlPointNodeName(19));
    Assert.assertEquals("D16 (RX2)", devKit.getControlPointNodeName(20));
    Assert.assertEquals("D17 (TX2)", devKit.getControlPointNodeName(21));
    Assert.assertEquals("D5", devKit.getControlPointNodeName(22));
    Assert.assertEquals("D18", devKit.getControlPointNodeName(23));
    Assert.assertEquals("D19", devKit.getControlPointNodeName(24));
    Assert.assertEquals("D21", devKit.getControlPointNodeName(25));
    Assert.assertEquals("RX0 (GPIO3)", devKit.getControlPointNodeName(26));
    Assert.assertEquals("TX0 (GPIO1)", devKit.getControlPointNodeName(27));
    Assert.assertEquals("D22", devKit.getControlPointNodeName(28));
    Assert.assertEquals("D23", devKit.getControlPointNodeName(29));
  }

  @Test
  public void testPinGeometryAndSpacing() {
    ESP32DevKit devKit = new ESP32DevKit();

    // Left row pitch (20px per pin = 0.10")
    for (int i = 0; i < 14; i++) {
      Point2D p1 = devKit.getControlPoint(i);
      Point2D p2 = devKit.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (20px per pin = 0.10")
    for (int i = 15; i < 29; i++) {
      Point2D p1 = devKit.getControlPoint(i);
      Point2D p2 = devKit.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 0.90" = 180px between Left row and Right row
    Point2D pLeftTop = devKit.getControlPoint(0);
    Point2D pRightTop = devKit.getControlPoint(29);
    Assert.assertEquals(180.0, pLeftTop.distance(pRightTop), 0.01);
    Assert.assertEquals(pLeftTop.getY(), pRightTop.getY(), 0.01);
  }

  @Test
  public void testBodyShapeDimensions() {
    ESP32DevKit devKit = new ESP32DevKit();

    Shape body = devKit.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(28.2d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(51.8d, SizeUnit.mm).convertToPixels();
    double expectedTopMargin = new Size(6.7d, SizeUnit.mm).convertToPixels();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    Point2D p0 = devKit.getControlPoint(0);
    Assert.assertEquals(expectedTopMargin, p0.getY() - bounds.getY(), 0.1);
  }
}
