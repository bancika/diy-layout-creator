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
    Assert.assertEquals("A0", mcu.getControlPointNodeName(0));
    Assert.assertEquals("RSV_1", mcu.getControlPointNodeName(1));
    Assert.assertEquals("RSV_2", mcu.getControlPointNodeName(2));
    Assert.assertEquals("SD3", mcu.getControlPointNodeName(3));
    Assert.assertEquals("SD2", mcu.getControlPointNodeName(4));
    Assert.assertEquals("SD1", mcu.getControlPointNodeName(5));
    Assert.assertEquals("CMD", mcu.getControlPointNodeName(6));
    Assert.assertEquals("SD0", mcu.getControlPointNodeName(7));
    Assert.assertEquals("CLK", mcu.getControlPointNodeName(8));
    Assert.assertEquals("GND_1", mcu.getControlPointNodeName(9));
    Assert.assertEquals("3V3_1", mcu.getControlPointNodeName(10));
    Assert.assertEquals("EN", mcu.getControlPointNodeName(11));
    Assert.assertEquals("RST", mcu.getControlPointNodeName(12));
    Assert.assertEquals("GND_2", mcu.getControlPointNodeName(13));
    Assert.assertEquals("VIN", mcu.getControlPointNodeName(14));

    // Right row (15..29)
    Assert.assertEquals("D0", mcu.getControlPointNodeName(15));
    Assert.assertEquals("D1", mcu.getControlPointNodeName(16));
    Assert.assertEquals("D2", mcu.getControlPointNodeName(17));
    Assert.assertEquals("D3", mcu.getControlPointNodeName(18));
    Assert.assertEquals("D4", mcu.getControlPointNodeName(19));
    Assert.assertEquals("3V3_2", mcu.getControlPointNodeName(20));
    Assert.assertEquals("GND_3", mcu.getControlPointNodeName(21));
    Assert.assertEquals("D5", mcu.getControlPointNodeName(22));
    Assert.assertEquals("D6", mcu.getControlPointNodeName(23));
    Assert.assertEquals("D7", mcu.getControlPointNodeName(24));
    Assert.assertEquals("D8", mcu.getControlPointNodeName(25));
    Assert.assertEquals("RX", mcu.getControlPointNodeName(26));
    Assert.assertEquals("TX", mcu.getControlPointNodeName(27));
    Assert.assertEquals("GND_4", mcu.getControlPointNodeName(28));
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

  @Test
  public void testDrawingAndOrientations() {
    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(400, 400, java.awt.image.BufferedImage.TYPE_INT_ARGB);
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

    ESP8266NodeMCU mcu = new ESP8266NodeMCU();
    mcu.setControlPoint(new Point2D.Double(100, 100), 0);

    // Normal mode with solder pads
    mcu.setHeaders(false);
    mcu.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);

    // Normal mode with pin headers
    mcu.setHeaders(true);
    mcu.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);

    // Selected mode
    mcu.draw(g2d, org.diylc.core.ComponentState.SELECTED, false, project, observer);

    // Outline mode
    mcu.draw(g2d, org.diylc.core.ComponentState.NORMAL, true, project, observer);

    // Rotated drawing (all orientations)
    for (org.diylc.common.Orientation orientation : org.diylc.common.Orientation.values()) {
      mcu.setOrientation(orientation);
      mcu.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);
    }

    // Icon drawing
    mcu.drawIcon(g2d, 32, 32);

    g2d.dispose();
  }

  @Test
  public void testDisplayPinLabels() {
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();

    // Verify parenthesis stripping
    Assert.assertEquals("A0", ESP8266NodeMCU.getDisplayPinLabel("A0 (ADC0)"));
    Assert.assertEquals("D0", ESP8266NodeMCU.getDisplayPinLabel("D0 (GPIO16)"));
    Assert.assertEquals("D1", ESP8266NodeMCU.getDisplayPinLabel("D1 (GPIO5)"));
    Assert.assertEquals("RX", ESP8266NodeMCU.getDisplayPinLabel("RX (GPIO3)"));
    Assert.assertEquals("TX", ESP8266NodeMCU.getDisplayPinLabel("TX (GPIO1)"));

    // Verify underscore stripping
    Assert.assertEquals("3V3", ESP8266NodeMCU.getDisplayPinLabel("3V3_1"));
    Assert.assertEquals("3V3", ESP8266NodeMCU.getDisplayPinLabel("3V3_2"));
    Assert.assertEquals("3V3", ESP8266NodeMCU.getDisplayPinLabel("3V3_3"));
    Assert.assertEquals("3V3", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(10))); // "3V3_1"
    Assert.assertEquals("3V3", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(20))); // "3V3_2"
    Assert.assertEquals("3V3", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(29))); // "3V3_3"

    // Verify plain labels
    Assert.assertEquals("EN", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(11))); // "EN"
    Assert.assertEquals("RST", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(12))); // "RST"
    Assert.assertEquals("VIN", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(14))); // "VIN"
    Assert.assertEquals("RSV", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(1))); // "RSV_1"
    Assert.assertEquals("RSV", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(2))); // "RSV_2"
    Assert.assertEquals("GND", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(9))); // "GND_1"
    Assert.assertEquals("GND", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(13))); // "GND_2"
    Assert.assertEquals("GND", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(21))); // "GND_3"
    Assert.assertEquals("GND", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(28))); // "GND_4"
    Assert.assertEquals("A0", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(0))); // "A0"
    Assert.assertEquals("D0", ESP8266NodeMCU.getDisplayPinLabel(mcu.getControlPointNodeName(15))); // "D0"
  }

  @Test
  public void testPinLabelConstants() {
    Assert.assertNotNull(ESP8266NodeMCU.PIN_LABEL_OFFSET);
    Assert.assertNotNull(ESP8266NodeMCU.PIN_FONT);
    Assert.assertEquals(8, ESP8266NodeMCU.PIN_FONT.getSize());
    Assert.assertEquals(1.8d, ESP8266NodeMCU.PIN_LABEL_OFFSET.getValue(), 0.001);
  }
}
