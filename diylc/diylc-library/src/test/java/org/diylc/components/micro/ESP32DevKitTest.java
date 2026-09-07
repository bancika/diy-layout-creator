package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.components.AbstractMakerBoard;
import org.diylc.netlist.Node;
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

    // Silkscreen, exactly as printed down each side of the board
    assertSilk(devKit, 0, "EN", "VP", "VN", "D34", "D35", "D32", "D33", "D25", "D26", "D27",
        "D14", "D12", "D13", "GND", "VIN");
    assertSilk(devKit, 15, "D23", "D22", "TX0", "RX0", "D21", "D19", "D18", "D5", "TX2", "RX2",
        "D4", "D2", "D15", "GND", "3V3");

    // Netlist identity: the GPIO number for numbered pins, the signal name otherwise
    assertNodeIds(devKit, 0, "EN", "VP", "VN", "GPIO34", "GPIO35", "GPIO32", "GPIO33", "GPIO25",
        "GPIO26", "GPIO27", "GPIO14", "GPIO12", "GPIO13", "GND_1", "VIN");
    assertNodeIds(devKit, 15, "GPIO23", "GPIO22", "TX0", "RX0", "GPIO21", "GPIO19", "GPIO18",
        "GPIO5", "TX2", "RX2", "GPIO4", "GPIO2", "GPIO15", "GND_2", "3V3");

    // The annotation carries what the silkscreen cannot
    Assert.assertEquals("GPIO34 (ADC1_CH6, VDET_1)", devKit.getControlPointNodeName(3));
    Assert.assertEquals("TX0 (GPIO1, U0TXD)", devKit.getControlPointNodeName(17));
  }

  /** Asserts the silkscreen labels printed next to consecutive pins starting at {@code start}. */
  private void assertSilk(ESP32DevKit devKit, int start, String... expected) {
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("Silkscreen for pin " + (start + i), expected[i],
          devKit.getSilkPinLabel(start + i));
    }
  }

  /**
   * Asserts how consecutive pins appear in a netlist, which is the node name with its parenthesized
   * annotation stripped.
   */
  private void assertNodeIds(ESP32DevKit devKit, int start, String... expected) {
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("Node name for pin " + (start + i), expected[i],
          Node.sanitizeNodeName(devKit.getControlPointNodeName(start + i)));
    }
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

    // Silkscreen (J2 then J3), matching the Espressif user guide's header tables
    assertSilk(devKit, 0, "3V3", "EN", "VP", "VN", "IO34", "IO35", "IO32", "IO33", "IO25", "IO26",
        "IO27", "IO14", "IO12", "GND", "IO13", "D2", "D3", "CMD", "5V");
    assertSilk(devKit, 19, "GND", "IO23", "IO22", "TX", "RX", "IO21", "GND", "IO19", "IO18", "IO5",
        "IO17", "IO16", "IO4", "IO0", "IO2", "IO15", "D1", "D0", "CLK");

    assertNodeIds(devKit, 0, "3V3", "EN", "VP", "VN", "GPIO34", "GPIO35", "GPIO32", "GPIO33",
        "GPIO25", "GPIO26", "GPIO27", "GPIO14", "GPIO12", "GND_1", "GPIO13", "D2", "D3", "CMD", "5V");
    assertNodeIds(devKit, 19, "GND_2", "GPIO23", "GPIO22", "TX", "RX", "GPIO21", "GND_3", "GPIO19",
        "GPIO18", "GPIO5", "GPIO17", "GPIO16", "GPIO4", "GPIO0", "GPIO2", "GPIO15", "D1", "D0", "CLK");

    Assert.assertEquals("GPIO0 (ADC2_CH1, TOUCH_CH1, Boot)", devKit.getControlPointNodeName(32));
    Assert.assertEquals("D0 (GPIO7, flash)", devKit.getControlPointNodeName(36));
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
    Point2D pRightTop = devKit.getControlPoint(15);
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

    double expectedWidth = ESP32DevKit.BOARD_WIDTH_30.convertToPixels();
    double expectedHeight = ESP32DevKit.BOARD_LENGTH_30.convertToPixels();
    double expectedTopMargin = ESP32DevKit.TOP_MARGIN_30.convertToPixels();

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

    double expectedWidth = ESP32DevKit.BOARD_WIDTH_38.convertToPixels();
    double expectedTotalLength = ESP32DevKit.BOARD_LENGTH_38.convertToPixels();

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

    // Silkscreen (J1 then J3), matching the Espressif user guide's header tables
    assertSilk(devKit, 0, "3V3", "3V3", "RST", "4", "5", "6", "7", "15", "16", "17", "18", "8", "3",
        "46", "9", "10", "11", "12", "13", "14", "5V", "G");
    assertSilk(devKit, 22, "G", "TX", "RX", "1", "2", "42", "41", "40", "39", "38", "37", "36", "35",
        "0", "45", "48", "47", "21", "20", "19", "G", "G");

    assertNodeIds(devKit, 0, "3V3_1", "3V3_2", "RST", "GPIO4", "GPIO5", "GPIO6", "GPIO7", "GPIO15",
        "GPIO16", "GPIO17", "GPIO18", "GPIO8", "GPIO3", "GPIO46", "GPIO9", "GPIO10", "GPIO11",
        "GPIO12", "GPIO13", "GPIO14", "5V", "GND_1");
    assertNodeIds(devKit, 22, "GND_2", "TX", "RX", "GPIO1", "GPIO2", "GPIO42", "GPIO41", "GPIO40",
        "GPIO39", "GPIO38", "GPIO37", "GPIO36", "GPIO35", "GPIO0", "GPIO45", "GPIO48", "GPIO47",
        "GPIO21", "GPIO20", "GPIO19", "GND_3", "GND_4");

    Assert.assertEquals("GPIO20 (U1CTS, ADC2_CH9, USB_D+)", devKit.getControlPointNodeName(40));
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
    Assert.assertEquals("5V", devKit.getSilkPinLabel(18));
    Assert.assertEquals("CLK", devKit.getSilkPinLabel(37));

    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_S3_DevKitC_44Pin);
    Assert.assertEquals(44, devKit.getControlPointCount());
    Assert.assertEquals("3V3_1", devKit.getControlPointNodeName(0));
    Assert.assertEquals("GND_4", devKit.getControlPointNodeName(43));
    Assert.assertEquals("3V3", devKit.getSilkPinLabel(0));

    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_C3_DevKitM_1);
    Assert.assertEquals(30, devKit.getControlPointCount());

    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_C6_DevKitC_1);
    Assert.assertEquals(32, devKit.getControlPointCount());

    devKit.setVersion(ESP32DevKit.DevKitVersion.DevKit_V1_30Pin);
    Assert.assertEquals(30, devKit.getControlPointCount());
    Assert.assertEquals("VIN", devKit.getControlPointNodeName(14));
    Assert.assertEquals("3V3", devKit.getControlPointNodeName(29));
    Assert.assertEquals("D13", devKit.getSilkPinLabel(12));
  }

  @Test
  public void testControlPointCountAndNamesC3() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_C3_DevKitM_1);

    Assert.assertEquals("ESP32-C3 DevKitM-1 (30-Pin)",
        ESP32DevKit.DevKitVersion.ESP32_C3_DevKitM_1.toString());
    Assert.assertEquals(30, devKit.getControlPointCount());

    for (int i = 0; i < devKit.getControlPointCount(); i++) {
      String name = devKit.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    assertSilk(devKit, 0, "GND", "3V3", "3V3", "IO2", "IO3", "GND", "RST", "GND", "IO0", "IO1",
        "IO10", "GND", "5V", "5V", "GND");
    assertSilk(devKit, 15, "GND", "TX", "RX", "GND", "IO9", "IO8", "GND", "IO7", "IO6", "IO5",
        "IO4", "GND", "IO18", "IO19", "GND");

    assertNodeIds(devKit, 0, "GND_1", "3V3_1", "3V3_2", "GPIO2", "GPIO3", "GND_2", "RST", "GND_3",
        "GPIO0", "GPIO1", "GPIO10", "GND_4", "5V_1", "5V_2", "GND_5");
    assertNodeIds(devKit, 15, "GND_6", "TX", "RX", "GND_7", "GPIO9", "GPIO8", "GND_8", "GPIO7",
        "GPIO6", "GPIO5", "GPIO4", "GND_9", "GPIO18", "GPIO19", "GND_10");

    Assert.assertEquals("GPIO8 (RGB LED)", devKit.getControlPointNodeName(20));
  }

  @Test
  public void testControlPointCountAndNamesC6() {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(ESP32DevKit.DevKitVersion.ESP32_C6_DevKitC_1);

    Assert.assertEquals("ESP32-C6 DevKitC-1 (32-Pin)",
        ESP32DevKit.DevKitVersion.ESP32_C6_DevKitC_1.toString());
    Assert.assertEquals(32, devKit.getControlPointCount());

    for (int i = 0; i < devKit.getControlPointCount(); i++) {
      String name = devKit.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    assertSilk(devKit, 0, "3V3", "RST", "4", "5", "6", "7", "0", "1", "8", "10", "11", "2", "3",
        "5V", "G", "NC");
    assertSilk(devKit, 16, "G", "TX", "RX", "15", "23", "22", "21", "20", "19", "18", "9", "G",
        "13", "12", "G", "NC");

    assertNodeIds(devKit, 0, "3V3", "RST", "GPIO4", "GPIO5", "GPIO6", "GPIO7", "GPIO0", "GPIO1",
        "GPIO8", "GPIO10", "GPIO11", "GPIO2", "GPIO3", "5V", "GND_1", "NC_1");
    assertNodeIds(devKit, 16, "GND_2", "TX", "RX", "GPIO15", "GPIO23", "GPIO22", "GPIO21", "GPIO20",
        "GPIO19", "GPIO18", "GPIO9", "GND_3", "GPIO13", "GPIO12", "GND_4", "NC_2");

    Assert.assertEquals("GPIO13 (USB_D+)", devKit.getControlPointNodeName(28));
  }

  @Test
  public void testRiscVPinGeometryAndBodyShape() {
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();
    double rowSpacing = new Size(22.86d, SizeUnit.mm).convertToPixels();

    for (ESP32DevKit.DevKitVersion version : new ESP32DevKit.DevKitVersion[] {
        ESP32DevKit.DevKitVersion.ESP32_C3_DevKitM_1, ESP32DevKit.DevKitVersion.ESP32_C6_DevKitC_1}) {
      ESP32DevKit devKit = new ESP32DevKit();
      devKit.setVersion(version);
      int pinsPerRow = devKit.getControlPointCount() / 2;

      // Two straight columns on 0.1" pitch, 0.9" apart
      for (int row = 0; row < 2; row++) {
        for (int i = 0; i < pinsPerRow - 1; i++) {
          Point2D p1 = devKit.getControlPoint(row * pinsPerRow + i);
          Point2D p2 = devKit.getControlPoint(row * pinsPerRow + i + 1);
          Assert.assertEquals(version + " pitch", spacing, p1.distance(p2), 0.01);
          Assert.assertEquals(version + " column should be straight", p1.getX(), p2.getX(), 0.01);
        }
      }
      Assert.assertEquals(version + " row spacing", rowSpacing,
          devKit.getControlPoint(0).distance(devKit.getControlPoint(pinsPerRow)), 0.01);

      // The body is 25.4 mm wide and the pin field is centred between the rows
      Shape body = devKit.getBodyShape();
      Rectangle2D bounds = body.getBounds2D();
      Assert.assertEquals(version + " board width",
          new Size(25.40d, SizeUnit.mm).convertToPixels(), bounds.getWidth(), 0.1);
      double centerX = devKit.getControlPoint(0).getX() + rowSpacing / 2.0;
      Assert.assertEquals(version + " pin field should be centred", centerX, bounds.getCenterX(), 0.1);
    }
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
    Point2D p30_right = devKit.getControlPoint(15);
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

  @Test
  public void testUsbConstants() {
    Assert.assertEquals(7.5d, AbstractMakerBoard.USB_MICRO_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_MICRO_WIDTH.getUnit());

    Assert.assertEquals(5.6d, AbstractMakerBoard.USB_MICRO_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_MICRO_LENGTH.getUnit());

    Assert.assertEquals(0.5d, AbstractMakerBoard.USB_MICRO_OVERHANG.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_MICRO_OVERHANG.getUnit());

    Assert.assertEquals(8.94d, AbstractMakerBoard.USB_C_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_C_WIDTH.getUnit());

    Assert.assertEquals(7.5d, AbstractMakerBoard.USB_C_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_C_LENGTH.getUnit());

    Assert.assertEquals(14.5d, AbstractMakerBoard.USB_A_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_A_WIDTH.getUnit());

    Assert.assertEquals(14.0d, AbstractMakerBoard.USB_A_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_A_LENGTH.getUnit());
  }

  @Test
  public void testAllESP32VersionsDrawing() {
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

    for (ESP32DevKit.DevKitVersion version : ESP32DevKit.DevKitVersion.values()) {
      ESP32DevKit devKit = new ESP32DevKit();
      devKit.setVersion(version);
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

      // Rotated drawing (all orientations)
      for (org.diylc.common.Orientation orientation : org.diylc.common.Orientation.values()) {
        devKit.setOrientation(orientation);
        devKit.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);
      }
    }

    g2d.dispose();
  }

  @Test
  public void testPinLabelConstants() {
    Assert.assertNotNull(ESP32DevKit.PIN_LABEL_OFFSET);
    Assert.assertNotNull(ESP32DevKit.PIN_FONT);
    Assert.assertEquals(8, ESP32DevKit.PIN_FONT.getSize());
    Assert.assertEquals(1.8d, ESP32DevKit.PIN_LABEL_OFFSET.getValue(), 0.001);
  }

  @Test
  public void testDisplayPinLabelFormatting() {
    Assert.assertEquals("ABC", ESP32DevKit.getDisplayPinLabel("ABC (XYZ)"));
    Assert.assertEquals("VP", ESP32DevKit.getDisplayPinLabel("VP (GPIO36)"));
    Assert.assertEquals("VN", ESP32DevKit.getDisplayPinLabel("VN (GPIO39)"));
    Assert.assertEquals("D16", ESP32DevKit.getDisplayPinLabel("D16 (RX2)"));
    Assert.assertEquals("D17", ESP32DevKit.getDisplayPinLabel("D17 (TX2)"));
    Assert.assertEquals("RX0", ESP32DevKit.getDisplayPinLabel("RX0 (GPIO3)"));
    Assert.assertEquals("TX0", ESP32DevKit.getDisplayPinLabel("TX0 (GPIO1)"));
    Assert.assertEquals("D2", ESP32DevKit.getDisplayPinLabel("D2 (GPIO9)"));
    Assert.assertEquals("D3", ESP32DevKit.getDisplayPinLabel("D3 (GPIO10)"));
    Assert.assertEquals("CMD", ESP32DevKit.getDisplayPinLabel("CMD (GPIO11)"));
    Assert.assertEquals("GPIO43", ESP32DevKit.getDisplayPinLabel("GPIO43 (U0TXD)"));
    Assert.assertEquals("GPIO44", ESP32DevKit.getDisplayPinLabel("GPIO44 (U0RXD)"));
    Assert.assertEquals("GPIO0", ESP32DevKit.getDisplayPinLabel("GPIO0 (BOOT)"));
    Assert.assertEquals("GPIO20", ESP32DevKit.getDisplayPinLabel("GPIO20 (USB D+)"));
    Assert.assertEquals("GPIO19", ESP32DevKit.getDisplayPinLabel("GPIO19 (USB D-)"));

    // Names without parentheses
    Assert.assertEquals("EN", ESP32DevKit.getDisplayPinLabel("EN"));
    Assert.assertEquals("D34", ESP32DevKit.getDisplayPinLabel("D34"));
    Assert.assertEquals("3V3", ESP32DevKit.getDisplayPinLabel("3V3"));
    Assert.assertEquals("3V3", ESP32DevKit.getDisplayPinLabel("3V3_1"));
    Assert.assertEquals("GND1", ESP32DevKit.getDisplayPinLabel("GND1"));
    Assert.assertEquals("", ESP32DevKit.getDisplayPinLabel(null));
    Assert.assertEquals("", ESP32DevKit.getDisplayPinLabel(""));
  }
}
