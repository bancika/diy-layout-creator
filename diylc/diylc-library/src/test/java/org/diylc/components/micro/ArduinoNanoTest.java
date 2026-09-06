package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
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

  @Test
  public void testHeadersProperty() {
    ArduinoNano nano = new ArduinoNano();
    Assert.assertFalse("Headers should be false by default", nano.getHeaders());

    nano.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", nano.getHeaders());

    nano.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", nano.getHeaders());
  }

  @Test
  public void testVersionProperty() {
    ArduinoNano nano = new ArduinoNano();
    Assert.assertEquals("Default version should be the classic Nano", ArduinoNano.NanoVersion.CLASSIC,
        nano.getVersion());
    Assert.assertEquals("Nano (ATmega328)", ArduinoNano.NanoVersion.CLASSIC.toString());
    Assert.assertEquals("Nano Every", ArduinoNano.NanoVersion.EVERY.toString());
    Assert.assertEquals("Nano 33 IoT", ArduinoNano.NanoVersion.NANO_33_IOT.toString());
    Assert.assertEquals("Nano 33 BLE", ArduinoNano.NanoVersion.NANO_33_BLE.toString());
    Assert.assertEquals("Nano 33 BLE Sense", ArduinoNano.NanoVersion.NANO_33_BLE_SENSE.toString());
    Assert.assertEquals("Nano RP2040 Connect", ArduinoNano.NanoVersion.NANO_RP2040_CONNECT.toString());
    Assert.assertEquals("Nano ESP32", ArduinoNano.NanoVersion.NANO_ESP32.toString());

    nano.setVersion(ArduinoNano.NanoVersion.NANO_ESP32);
    Assert.assertEquals(ArduinoNano.NanoVersion.NANO_ESP32, nano.getVersion());
  }

  @Test
  public void testGeometryIsVersionIndependent() {
    // Every Nano keeps the 0.73" x 1.70" outline and the same 2x15 pin positions. Only the classic
    // populates the 2x3 ICSP block, so it has 36 control points where the rest have 30.
    ArduinoNano classic = new ArduinoNano();
    Shape classicBody = classic.getBodyShape();
    Assert.assertEquals(36, classic.getControlPointCount());

    for (ArduinoNano.NanoVersion version : ArduinoNano.NanoVersion.values()) {
      ArduinoNano nano = new ArduinoNano();
      nano.setVersion(version);

      int expected = version == ArduinoNano.NanoVersion.CLASSIC ? 36 : 30;
      Assert.assertEquals(version + " pin count", expected, nano.getControlPointCount());
      Assert.assertEquals(version + " outline should be unchanged",
          classicBody.getBounds2D(), nano.getBodyShape().getBounds2D());
      for (int i = 0; i < nano.getControlPointCount(); i++) {
        Assert.assertEquals(version + " pin " + i, classic.getControlPoint(i), nano.getControlPoint(i));
      }
    }
  }

  @Test
  public void testModernVersionsHaveNoIcspBlock() {
    // The 2x3 block belongs to the classic Nano alone; the later boards use that end of the PCB
    // for the radio module or USB bridge, which is drawn in its place.
    for (ArduinoNano.NanoVersion version : ArduinoNano.NanoVersion.values()) {
      ArduinoNano nano = new ArduinoNano();
      nano.setVersion(version);

      if (version == ArduinoNano.NanoVersion.CLASSIC) {
        Assert.assertEquals(36, nano.getControlPointCount());
        Assert.assertEquals("MISO", nano.getControlPointNodeName(30));
        Assert.assertNull("The classic Nano has the header, not a module", version.getModuleLabel());
      } else {
        Assert.assertEquals(version + " should stop at the pin rows", 30, nano.getControlPointCount());
        Assert.assertNotNull(version + " should name the part in place of the ICSP block",
            version.getModuleLabel());
      }
    }

    // Boards whose MCU is a die inside the module do not also draw it as a separate chip
    Assert.assertNull(ArduinoNano.NanoVersion.NANO_33_BLE.getMcuLabel());
    Assert.assertNull(ArduinoNano.NanoVersion.NANO_33_BLE_SENSE.getMcuLabel());
    Assert.assertNull(ArduinoNano.NanoVersion.NANO_ESP32.getMcuLabel());
    Assert.assertEquals("NORA-W106", ArduinoNano.NanoVersion.NANO_ESP32.getModuleLabel());
    Assert.assertEquals("SAMD21", ArduinoNano.NanoVersion.NANO_33_IOT.getMcuLabel());
    Assert.assertEquals("NINA-W102", ArduinoNano.NanoVersion.NANO_33_IOT.getModuleLabel());
  }

  @Test
  public void testPinNamesPerVersion() {
    for (ArduinoNano.NanoVersion version : ArduinoNano.NanoVersion.values()) {
      ArduinoNano nano = new ArduinoNano();
      nano.setVersion(version);
      for (int i = 0; i < nano.getControlPointCount(); i++) {
        String name = nano.getControlPointNodeName(i);
        Assert.assertNotNull(version + " pin " + i + " name should not be null", name);
        Assert.assertFalse(version + " pin " + i + " name should not be empty", name.trim().isEmpty());
      }
    }

    // The classic silk leaves the bus functions off A4 / A5
    ArduinoNano classic = new ArduinoNano();
    Assert.assertEquals("A5", classic.getControlPointNodeName(21));
    Assert.assertEquals("A4", classic.getControlPointNodeName(22));

    // Every later board annotates them
    ArduinoNano every = new ArduinoNano();
    every.setVersion(ArduinoNano.NanoVersion.EVERY);
    Assert.assertEquals("A5 (SCL)", every.getControlPointNodeName(21));
    Assert.assertEquals("A4 (SDA)", every.getControlPointNodeName(22));

    // The Nano ESP32 additionally carries the Espressif GPIO number
    ArduinoNano esp32 = new ArduinoNano();
    esp32.setVersion(ArduinoNano.NanoVersion.NANO_ESP32);
    Assert.assertEquals("D2 (GPIO5)", esp32.getControlPointNodeName(4));
    Assert.assertEquals("A4 (SDA/GPIO11)", esp32.getControlPointNodeName(22));
  }

  @Test
  public void testModernVersionsHaveCastellatedEdges() {
    // The classic Nano is a plain rounded rectangle; every later board has the pad columns bitten
    // out of both long edges the way the Pico does.
    ArduinoNano classic = new ArduinoNano();
    Shape classicBody = classic.getBodyShape();
    Rectangle2D bounds = classicBody.getBounds2D();

    // A point on the left edge level with a pin is inside the classic outline
    Point2D p0 = classic.getControlPoint(0);
    double probeX = bounds.getX() + 0.5;
    Assert.assertTrue("The classic Nano edge should be solid at pin level",
        classicBody.contains(probeX, p0.getY()));

    for (ArduinoNano.NanoVersion version : ArduinoNano.NanoVersion.values()) {
      if (version == ArduinoNano.NanoVersion.CLASSIC) {
        continue;
      }
      ArduinoNano nano = new ArduinoNano();
      nano.setVersion(version);
      Shape body = nano.getBodyShape();

      Assert.assertFalse(version + " should be notched at the left pin column",
          body.contains(probeX, nano.getControlPoint(0).getY()));
      Assert.assertFalse(version + " should be notched at the right pin column",
          body.contains(bounds.getMaxX() - 0.5, nano.getControlPoint(15).getY()));

      // The outline between two pins stays solid, and the overall footprint is unchanged
      double betweenY = nano.getControlPoint(0).getY() + PIN_SPACING_PX / 2.0;
      Assert.assertTrue(version + " should be solid between pins", body.contains(probeX, betweenY));
      Assert.assertEquals(version + " footprint should be unchanged", bounds, body.getBounds2D());
    }
  }

  private static final double PIN_SPACING_PX = new Size(0.1d, SizeUnit.in).convertToPixels();

  @Test
  public void testAllVersionsDrawing() {
    java.awt.image.BufferedImage img =
        new java.awt.image.BufferedImage(600, 600, java.awt.image.BufferedImage.TYPE_INT_ARGB);
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

    for (ArduinoNano.NanoVersion version : ArduinoNano.NanoVersion.values()) {
      ArduinoNano nano = new ArduinoNano();
      nano.setVersion(version);
      nano.setControlPoint(new Point2D.Double(200, 200), 0);

      for (boolean headers : new boolean[] {false, true}) {
        nano.setHeaders(headers);
        nano.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);
        nano.draw(g2d, org.diylc.core.ComponentState.SELECTED, false, project, observer);
        nano.draw(g2d, org.diylc.core.ComponentState.NORMAL, true, project, observer);
      }
      nano.drawIcon(g2d, 32, 32);
    }

    g2d.dispose();
  }
}
