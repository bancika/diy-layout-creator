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
  public void testControlPointCountAndNames30Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    Assert.assertEquals(ESP32DevKit.DevKitVersion.DevKit_V1_30Pin, devKit.getVersion());
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
  public void testControlPointCountAndNames38Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.DevKitC_V4_38Pin);
    Assert.assertEquals(ESP32DevKit.DevKitVersion.DevKitC_V4_38Pin, devKit.getVersion());
    Assert.assertEquals(38, devKit.getControlPointCount());

    for (int i = 0; i < devKit.getControlPointCount(); i++) {
      String name = devKit.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..18, top to bottom)
    Assert.assertEquals("3V3", devKit.getControlPointNodeName(0));
    Assert.assertEquals("EN", devKit.getControlPointNodeName(1));
    Assert.assertEquals("VP (GPIO36)", devKit.getControlPointNodeName(2));
    Assert.assertEquals("VN (GPIO39)", devKit.getControlPointNodeName(3));
    Assert.assertEquals("GPIO34", devKit.getControlPointNodeName(4));
    Assert.assertEquals("GPIO35", devKit.getControlPointNodeName(5));
    Assert.assertEquals("GPIO32", devKit.getControlPointNodeName(6));
    Assert.assertEquals("GPIO33", devKit.getControlPointNodeName(7));
    Assert.assertEquals("GPIO25", devKit.getControlPointNodeName(8));
    Assert.assertEquals("GPIO26", devKit.getControlPointNodeName(9));
    Assert.assertEquals("GPIO27", devKit.getControlPointNodeName(10));
    Assert.assertEquals("GPIO14", devKit.getControlPointNodeName(11));
    Assert.assertEquals("GPIO12", devKit.getControlPointNodeName(12));
    Assert.assertEquals("GND1", devKit.getControlPointNodeName(13));
    Assert.assertEquals("GPIO13", devKit.getControlPointNodeName(14));
    Assert.assertEquals("D2 (GPIO9)", devKit.getControlPointNodeName(15));
    Assert.assertEquals("D3 (GPIO10)", devKit.getControlPointNodeName(16));
    Assert.assertEquals("CMD (GPIO11)", devKit.getControlPointNodeName(17));
    Assert.assertEquals("5V", devKit.getControlPointNodeName(18));

    // Right row (19..37, top to bottom)
    Assert.assertEquals("GND2", devKit.getControlPointNodeName(19));
    Assert.assertEquals("GPIO23", devKit.getControlPointNodeName(20));
    Assert.assertEquals("GPIO22", devKit.getControlPointNodeName(21));
    Assert.assertEquals("TX0 (GPIO1)", devKit.getControlPointNodeName(22));
    Assert.assertEquals("RX0 (GPIO3)", devKit.getControlPointNodeName(23));
    Assert.assertEquals("GPIO21", devKit.getControlPointNodeName(24));
    Assert.assertEquals("GND3", devKit.getControlPointNodeName(25));
    Assert.assertEquals("GPIO19", devKit.getControlPointNodeName(26));
    Assert.assertEquals("GPIO18", devKit.getControlPointNodeName(27));
    Assert.assertEquals("GPIO5", devKit.getControlPointNodeName(28));
    Assert.assertEquals("GPIO17", devKit.getControlPointNodeName(29));
    Assert.assertEquals("GPIO16", devKit.getControlPointNodeName(30));
    Assert.assertEquals("GPIO4", devKit.getControlPointNodeName(31));
    Assert.assertEquals("GPIO0", devKit.getControlPointNodeName(32));
    Assert.assertEquals("GPIO2", devKit.getControlPointNodeName(33));
    Assert.assertEquals("GPIO15", devKit.getControlPointNodeName(34));
    Assert.assertEquals("D1 (GPIO8)", devKit.getControlPointNodeName(35));
    Assert.assertEquals("D0 (GPIO7)", devKit.getControlPointNodeName(36));
    Assert.assertEquals("CLK (GPIO6)", devKit.getControlPointNodeName(37));
  }

  @Test
  public void testPinGeometryAndSpacing30Pin() {
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

    // Row spacing: 1.00" = 200px between Left row and Right row
    Point2D pLeftTop = devKit.getControlPoint(0);
    Point2D pRightTop = devKit.getControlPoint(29);
    Assert.assertEquals(200.0, pLeftTop.distance(pRightTop), 0.01);
    Assert.assertEquals(pLeftTop.getY(), pRightTop.getY(), 0.01);
  }

  @Test
  public void testPinGeometryAndSpacing38Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.DevKitC_V4_38Pin);

    // Left row pitch (20px per pin = 0.10")
    for (int i = 0; i < 18; i++) {
      Point2D p1 = devKit.getControlPoint(i);
      Point2D p2 = devKit.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (20px per pin = 0.10")
    for (int i = 19; i < 37; i++) {
      Point2D p1 = devKit.getControlPoint(i);
      Point2D p2 = devKit.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 1.00" = 200px between Left row and Right row
    Point2D pLeftTop = devKit.getControlPoint(0);
    Point2D pRightTop = devKit.getControlPoint(19);
    Assert.assertEquals(200.0, pLeftTop.distance(pRightTop), 0.01);
    Assert.assertEquals(pLeftTop.getY(), pRightTop.getY(), 0.01);
  }

  @Test
  public void testBodyShapeDimensions30Pin() {
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

  @Test
  public void testBodyShapeDimensions38Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.DevKitC_V4_38Pin);

    Shape body = devKit.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(27.9d, SizeUnit.mm).convertToPixels();
    double expectedTotalLength = new Size(54.4d, SizeUnit.mm).convertToPixels();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedTotalLength, bounds.getHeight(), 0.1);
  }

  @Test
  public void testControlPointCountAndNamesS3_44Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);
    Assert.assertEquals(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin, devKit.getVersion());
    Assert.assertEquals(44, devKit.getControlPointCount());

    for (int i = 0; i < devKit.getControlPointCount(); i++) {
      String name = devKit.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..21, top to bottom)
    Assert.assertEquals("3V3_1", devKit.getControlPointNodeName(0));
    Assert.assertEquals("3V3_2", devKit.getControlPointNodeName(1));
    Assert.assertEquals("RST", devKit.getControlPointNodeName(2));
    Assert.assertEquals("GPIO4", devKit.getControlPointNodeName(3));
    Assert.assertEquals("GPIO5", devKit.getControlPointNodeName(4));
    Assert.assertEquals("GPIO6", devKit.getControlPointNodeName(5));
    Assert.assertEquals("GPIO7", devKit.getControlPointNodeName(6));
    Assert.assertEquals("GPIO15", devKit.getControlPointNodeName(7));
    Assert.assertEquals("GPIO16", devKit.getControlPointNodeName(8));
    Assert.assertEquals("GPIO17", devKit.getControlPointNodeName(9));
    Assert.assertEquals("GPIO18", devKit.getControlPointNodeName(10));
    Assert.assertEquals("GPIO8", devKit.getControlPointNodeName(11));
    Assert.assertEquals("GPIO3", devKit.getControlPointNodeName(12));
    Assert.assertEquals("GPIO46", devKit.getControlPointNodeName(13));
    Assert.assertEquals("GPIO9", devKit.getControlPointNodeName(14));
    Assert.assertEquals("GPIO10", devKit.getControlPointNodeName(15));
    Assert.assertEquals("GPIO11", devKit.getControlPointNodeName(16));
    Assert.assertEquals("GPIO12", devKit.getControlPointNodeName(17));
    Assert.assertEquals("GPIO13", devKit.getControlPointNodeName(18));
    Assert.assertEquals("GPIO14", devKit.getControlPointNodeName(19));
    Assert.assertEquals("5V0", devKit.getControlPointNodeName(20));
    Assert.assertEquals("GND1", devKit.getControlPointNodeName(21));

    // Right row (22..43, top to bottom)
    Assert.assertEquals("GND2", devKit.getControlPointNodeName(22));
    Assert.assertEquals("GPIO43 (U0TXD)", devKit.getControlPointNodeName(23));
    Assert.assertEquals("GPIO44 (U0RXD)", devKit.getControlPointNodeName(24));
    Assert.assertEquals("GPIO1", devKit.getControlPointNodeName(25));
    Assert.assertEquals("GPIO2", devKit.getControlPointNodeName(26));
    Assert.assertEquals("GPIO42", devKit.getControlPointNodeName(27));
    Assert.assertEquals("GPIO41", devKit.getControlPointNodeName(28));
    Assert.assertEquals("GPIO40", devKit.getControlPointNodeName(29));
    Assert.assertEquals("GPIO39", devKit.getControlPointNodeName(30));
    Assert.assertEquals("GPIO38", devKit.getControlPointNodeName(31));
    Assert.assertEquals("GPIO37", devKit.getControlPointNodeName(32));
    Assert.assertEquals("GPIO36", devKit.getControlPointNodeName(33));
    Assert.assertEquals("GPIO35", devKit.getControlPointNodeName(34));
    Assert.assertEquals("GPIO0 (BOOT)", devKit.getControlPointNodeName(35));
    Assert.assertEquals("GPIO45", devKit.getControlPointNodeName(36));
    Assert.assertEquals("GPIO48", devKit.getControlPointNodeName(37));
    Assert.assertEquals("GPIO47", devKit.getControlPointNodeName(38));
    Assert.assertEquals("GPIO21", devKit.getControlPointNodeName(39));
    Assert.assertEquals("GPIO20 (USB D+)", devKit.getControlPointNodeName(40));
    Assert.assertEquals("GPIO19 (USB D-)", devKit.getControlPointNodeName(41));
    Assert.assertEquals("GND3", devKit.getControlPointNodeName(42));
    Assert.assertEquals("GND4", devKit.getControlPointNodeName(43));
  }

  @Test
  public void testPinGeometryAndSpacingS3_44Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);

    // Left row pitch (20px per pin = 0.10")
    for (int i = 0; i < 21; i++) {
      Point2D p1 = devKit.getControlPoint(i);
      Point2D p2 = devKit.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (20px per pin = 0.10")
    for (int i = 22; i < 43; i++) {
      Point2D p1 = devKit.getControlPoint(i);
      Point2D p2 = devKit.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 22.86mm (~0.9") between Left row and Right row
    double expectedRowSpacing = new Size(22.86d, SizeUnit.mm).convertToPixels();
    Point2D pLeftTop = devKit.getControlPoint(0);
    Point2D pRightTop = devKit.getControlPoint(22);
    Assert.assertEquals(expectedRowSpacing, pLeftTop.distance(pRightTop), 0.01);
    Assert.assertEquals(pLeftTop.getY(), pRightTop.getY(), 0.01);
  }

  @Test
  public void testBodyShapeDimensionsS3_44Pin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);

    Shape body = devKit.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(25.40d, SizeUnit.mm).convertToPixels();
    // Total height = main body (62.74mm) + antenna (6.0mm)
    double expectedTotalLength = new Size(62.74d + 6.0d, SizeUnit.mm).convertToPixels();

    // Width is the board width (antenna is 18mm, narrower than 25.4mm)
    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedTotalLength, bounds.getHeight(), 1.0);
  }

  @Test
  public void testVersionSwitching() {
    ESP32DevKit devKit = new ESP32DevKit();
    Assert.assertEquals(30, devKit.getControlPointCount());

    devKit.setVersion(ESP32DevKit.DevKitVersion.DevKitC_V4_38Pin);
    Assert.assertEquals(38, devKit.getControlPointCount());
    Assert.assertEquals("5V", devKit.getControlPointNodeName(18));
    Assert.assertEquals("CLK (GPIO6)", devKit.getControlPointNodeName(37));

    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);
    Assert.assertEquals(44, devKit.getControlPointCount());
    Assert.assertEquals("3V3_1", devKit.getControlPointNodeName(0));
    Assert.assertEquals("GND4", devKit.getControlPointNodeName(43));

    devKit.setVersion(ESP32DevKit.DevKitVersion.DevKit_V1_30Pin);
    Assert.assertEquals(30, devKit.getControlPointCount());
    Assert.assertEquals("3V3", devKit.getControlPointNodeName(14));
    Assert.assertEquals("D23", devKit.getControlPointNodeName(29));
  }

  @Test
  public void testHeadersProperty() {
    ESP32DevKit devKit = new ESP32DevKit();
    Assert.assertFalse("Headers should be false by default", devKit.getHeaders());

    devKit.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", devKit.getHeaders());

    devKit.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", devKit.getHeaders());
  }

  @Test
  public void testS3RowSpacingDiffersFromOriginal() {
    ESP32DevKit devKit = new ESP32DevKit();

    // Original uses 1.0" (25.4mm) row spacing
    Point2D p30_left = devKit.getControlPoint(0);
    Point2D p30_right = devKit.getControlPoint(29);
    double spacing30 = Math.abs(p30_right.getX() - p30_left.getX());

    // S3 uses 22.86mm (~0.9") row spacing
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);
    Point2D pS3_left = devKit.getControlPoint(0);
    Point2D pS3_right = devKit.getControlPoint(22);
    double spacingS3 = Math.abs(pS3_right.getX() - pS3_left.getX());

    // S3 row spacing should be narrower than original
    Assert.assertTrue("S3 row spacing should be narrower than original", spacingS3 < spacing30);

    double expectedS3Spacing = new Size(22.86d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedS3Spacing, spacingS3, 0.01);
  }

  @Test
  public void testS3BottomMargin() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);

    Point2D p0 = devKit.getControlPoint(0);
    Point2D p21 = devKit.getControlPoint(21);
    Point2D p43 = devKit.getControlPoint(43);

    double mainY = p0.getY() - ESP32DevKit.TOP_MARGIN_S3.convertToPixels();
    double boardBottom = mainY + ESP32DevKit.BOARD_LENGTH_S3.convertToPixels();

    double expectedBottomMargin = ESP32DevKit.BOTTOM_MARGIN_S3.convertToPixels();
    Assert.assertEquals(expectedBottomMargin, boardBottom - p21.getY(), 0.01);
    Assert.assertEquals(expectedBottomMargin, boardBottom - p43.getY(), 0.01);
  }

  @Test
  public void testS3Drawing() {
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

    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);
    devKit.setControlPoint(new Point2D.Double(100, 100), 0);

    // Normal mode with solder pads
    devKit.setHeaders(false);
    devKit.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);

    // Normal mode with pin headers
    devKit.setHeaders(true);
    devKit.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);

    // Selected mode
    devKit.draw(g2d, org.diylc.core.ComponentState.SELECTED, false, project, observer);

    // Outline mode
    devKit.draw(g2d, org.diylc.core.ComponentState.NORMAL, true, project, observer);

    // Icon drawing
    devKit.drawIcon(g2d, 32, 32);

    g2d.dispose();
  }
}
