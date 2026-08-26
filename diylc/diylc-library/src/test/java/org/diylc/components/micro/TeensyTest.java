package org.diylc.components.micro;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import org.diylc.common.Orientation;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class TeensyTest {

  @Test
  public void testControlPointCountAndNames40() {
    Teensy teensy = new Teensy();
    Assert.assertEquals(Teensy.TeensyVersion.Teensy_4_0, teensy.getVersion());
    // 14 left + 14 right + 5 end + 1 VUSB = 34 pins
    Assert.assertEquals(34, teensy.getControlPointCount());

    for (int i = 0; i < teensy.getControlPointCount(); i++) {
      String name = teensy.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..13, top to bottom)
    Assert.assertEquals("GND", teensy.getControlPointNodeName(0));
    Assert.assertEquals("0 (RX1/CS1/CRX2)", teensy.getControlPointNodeName(1));
    Assert.assertEquals("1 (TX1/MISO1/CTX2)", teensy.getControlPointNodeName(2));
    Assert.assertEquals("12 (MISO/MQSL)", teensy.getControlPointNodeName(13));

    // Right row (14..27, top to bottom)
    Assert.assertEquals("VIN (3.6-5.5V)", teensy.getControlPointNodeName(14));
    Assert.assertEquals("GND", teensy.getControlPointNodeName(15));
    Assert.assertEquals("3.3V (250mA)", teensy.getControlPointNodeName(16));
    Assert.assertEquals("23 (A9/CRX1)", teensy.getControlPointNodeName(17));
    Assert.assertEquals("13 (SCK/CRX1/LED)", teensy.getControlPointNodeName(27));

    // End cluster (28..32)
    Assert.assertEquals("VBAT", teensy.getControlPointNodeName(28));
    Assert.assertEquals("3.3V (End)", teensy.getControlPointNodeName(29));
    Assert.assertEquals("GND (End)", teensy.getControlPointNodeName(30));
    Assert.assertEquals("Program", teensy.getControlPointNodeName(31));
    Assert.assertEquals("On/Off", teensy.getControlPointNodeName(32));

    // VUSB (33)
    Assert.assertEquals("VUSB", teensy.getControlPointNodeName(33));
  }

  @Test
  public void testControlPointCountAndNames41() {
    Teensy teensy = new Teensy();
    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    Assert.assertEquals(Teensy.TeensyVersion.Teensy_4_1, teensy.getVersion());
    // 24 left + 24 right + 5 mid + 6 eth + 5 usb + 1 VUSB = 65 pins
    Assert.assertEquals(65, teensy.getControlPointCount());

    for (int i = 0; i < teensy.getControlPointCount(); i++) {
      String name = teensy.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Left row (0..23, top to bottom)
    Assert.assertEquals("GND", teensy.getControlPointNodeName(0));
    Assert.assertEquals("0 (RX1/CS1/CRX2)", teensy.getControlPointNodeName(1));
    Assert.assertEquals("3.3V", teensy.getControlPointNodeName(14));
    Assert.assertEquals("32 (OUT1B)", teensy.getControlPointNodeName(23));

    // Right row (24..47, top to bottom)
    Assert.assertEquals("VIN (3.6-5.5V)", teensy.getControlPointNodeName(24));
    Assert.assertEquals("GND", teensy.getControlPointNodeName(25));
    Assert.assertEquals("13 (SCK/LED)", teensy.getControlPointNodeName(37));
    Assert.assertEquals("33 (MCLK2)", teensy.getControlPointNodeName(47));

    // Middle cluster (48..52)
    Assert.assertEquals("VBAT", teensy.getControlPointNodeName(48));
    Assert.assertEquals("3.3V (Mid)", teensy.getControlPointNodeName(49));
    Assert.assertEquals("GND (Mid)", teensy.getControlPointNodeName(50));
    Assert.assertEquals("Program", teensy.getControlPointNodeName(51));
    Assert.assertEquals("On/Off", teensy.getControlPointNodeName(52));

    // Ethernet Header (53..58)
    Assert.assertEquals("ETH_Rx+", teensy.getControlPointNodeName(53));
    Assert.assertEquals("ETH_Tx+", teensy.getControlPointNodeName(58));

    // USB Host Header (59..63)
    Assert.assertEquals("USB_5V", teensy.getControlPointNodeName(59));
    Assert.assertEquals("USB_GND2", teensy.getControlPointNodeName(63));

    // VUSB (64)
    Assert.assertEquals("VUSB", teensy.getControlPointNodeName(64));
  }

  @Test
  public void testPinGeometryAndSpacing40() {
    Teensy teensy = new Teensy();
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();

    // Left row pitch (0.1" per pin, 14 pins)
    for (int i = 0; i < 13; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("Left pin " + i + " Y spacing", spacing, p2.getY() - p1.getY(), 0.01);
      Assert.assertEquals("Left pin " + i + " X alignment", p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (14 pins)
    for (int i = 14; i < 27; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("Right pin " + i + " Y spacing", spacing, p2.getY() - p1.getY(), 0.01);
      Assert.assertEquals("Right pin " + i + " X alignment", p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 0.60" (15.24 mm)
    double rowSpacing = new Size(15.24d, SizeUnit.mm).convertToPixels();
    Point2D pLeft = teensy.getControlPoint(0);
    Point2D pRight = teensy.getControlPoint(14);
    Assert.assertEquals("Row spacing", rowSpacing, pRight.getX() - pLeft.getX(), 0.01);
    Assert.assertEquals("Row Y alignment", pLeft.getY(), pRight.getY(), 0.01);

    // End row horizontal spacing: 0.1" pitch at Y = 13 * spacing
    for (int i = 28; i < 32; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("End pin " + i + " X spacing", spacing, p2.getX() - p1.getX(), 0.01);
      Assert.assertEquals("End pin " + i + " Y alignment", p1.getY(), p2.getY(), 0.01);
    }
  }

  @Test
  public void testPinGeometryAndSpacing41() {
    Teensy teensy = new Teensy();
    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();

    // Left row pitch (0.1" per pin, 24 pins)
    for (int i = 0; i < 23; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("Left pin " + i + " Y spacing", spacing, p2.getY() - p1.getY(), 0.01);
      Assert.assertEquals("Left pin " + i + " X alignment", p1.getX(), p2.getX(), 0.01);
    }

    // Right row pitch (24 pins)
    for (int i = 24; i < 47; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("Right pin " + i + " Y spacing", spacing, p2.getY() - p1.getY(), 0.01);
      Assert.assertEquals("Right pin " + i + " X alignment", p1.getX(), p2.getX(), 0.01);
    }

    // Row spacing: 0.60" (15.24 mm)
    double rowSpacing = new Size(15.24d, SizeUnit.mm).convertToPixels();
    Point2D pLeft = teensy.getControlPoint(0);
    Point2D pRight = teensy.getControlPoint(24);
    Assert.assertEquals("Row spacing", rowSpacing, pRight.getX() - pLeft.getX(), 0.01);
    Assert.assertEquals("Row Y alignment", pLeft.getY(), pRight.getY(), 0.01);

    // Middle cluster: horizontal at 0.1" pitch
    for (int i = 48; i < 52; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("Middle pin " + i + " X spacing", spacing, p2.getX() - p1.getX(), 0.01);
      Assert.assertEquals("Middle pin " + i + " Y alignment", p1.getY(), p2.getY(), 0.01);
    }

    // USB Host Header: vertical at 0.1" pitch
    for (int i = 59; i < 63; i++) {
      Point2D p1 = teensy.getControlPoint(i);
      Point2D p2 = teensy.getControlPoint(i + 1);
      Assert.assertEquals("USB pin " + i + " Y spacing", spacing, p2.getY() - p1.getY(), 0.01);
      Assert.assertEquals("USB pin " + i + " X alignment", p1.getX(), p2.getX(), 0.01);
    }
  }

  @Test
  public void testBodyShapeDimensions40() {
    Teensy teensy = new Teensy();

    Shape body = teensy.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(17.78d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(35.56d, SizeUnit.mm).convertToPixels();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    // Pin 1 offset should be 1.27 mm (0.05") from left and top edges
    Point2D p0 = teensy.getControlPoint(0);
    double expectedOffsetX = new Size(1.27d, SizeUnit.mm).convertToPixels();
    double expectedOffsetY = new Size(1.27d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals("Pin 1 X offset from left edge", expectedOffsetX, p0.getX() - bounds.getX(), 0.1);
    Assert.assertEquals("Pin 1 Y offset from top edge", expectedOffsetY, p0.getY() - bounds.getY(), 0.1);
  }

  @Test
  public void testBodyShapeDimensions41() {
    Teensy teensy = new Teensy();
    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);

    Shape body = teensy.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    double expectedWidth = new Size(17.78d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(60.96d, SizeUnit.mm).convertToPixels();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    // Pin 1 offset should be 1.27 mm (0.05") from left and top edges
    Point2D p0 = teensy.getControlPoint(0);
    double expectedOffsetX = new Size(1.27d, SizeUnit.mm).convertToPixels();
    double expectedOffsetY = new Size(1.27d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals("Pin 1 X offset from left edge", expectedOffsetX, p0.getX() - bounds.getX(), 0.1);
    Assert.assertEquals("Pin 1 Y offset from top edge", expectedOffsetY, p0.getY() - bounds.getY(), 0.1);
  }

  @Test
  public void testAllPinsWithinBoardBounds() {
    // Teensy 4.0: all 34 pins must be within board bounds
    Teensy teensy40 = new Teensy();
    Shape body40 = teensy40.getBodyShape();
    Rectangle2D bounds40 = body40.getBounds2D();
    for (int i = 0; i < teensy40.getControlPointCount(); i++) {
      Point2D p = teensy40.getControlPoint(i);
      Assert.assertTrue("T4.0 Pin " + i + " (" + teensy40.getControlPointNodeName(i) +
          ") X=" + p.getX() + " should be within board X bounds [" + bounds40.getMinX() + ", " + bounds40.getMaxX() + "]",
          p.getX() >= bounds40.getMinX() - 0.1 && p.getX() <= bounds40.getMaxX() + 0.1);
      Assert.assertTrue("T4.0 Pin " + i + " (" + teensy40.getControlPointNodeName(i) +
          ") Y=" + p.getY() + " should be within board Y bounds [" + bounds40.getMinY() + ", " + bounds40.getMaxY() + "]",
          p.getY() >= bounds40.getMinY() - 0.1 && p.getY() <= bounds40.getMaxY() + 0.1);
    }

    // Teensy 4.1: all 65 pins must be within board bounds
    Teensy teensy41 = new Teensy();
    teensy41.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    Shape body41 = teensy41.getBodyShape();
    Rectangle2D bounds41 = body41.getBounds2D();
    for (int i = 0; i < teensy41.getControlPointCount(); i++) {
      Point2D p = teensy41.getControlPoint(i);
      Assert.assertTrue("T4.1 Pin " + i + " (" + teensy41.getControlPointNodeName(i) +
          ") X=" + p.getX() + " should be within board X bounds [" + bounds41.getMinX() + ", " + bounds41.getMaxX() + "]",
          p.getX() >= bounds41.getMinX() - 0.1 && p.getX() <= bounds41.getMaxX() + 0.1);
      Assert.assertTrue("T4.1 Pin " + i + " (" + teensy41.getControlPointNodeName(i) +
          ") Y=" + p.getY() + " should be within board Y bounds [" + bounds41.getMinY() + ", " + bounds41.getMaxY() + "]",
          p.getY() >= bounds41.getMinY() - 0.1 && p.getY() <= bounds41.getMaxY() + 0.1);
    }
  }

  @Test
  public void testVersionSwitching() {
    Teensy teensy = new Teensy();
    Assert.assertEquals(34, teensy.getControlPointCount());

    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    Assert.assertEquals(65, teensy.getControlPointCount());
    Assert.assertEquals("VUSB", teensy.getControlPointNodeName(64));
    Assert.assertEquals("GND", teensy.getControlPointNodeName(0));

    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_0);
    Assert.assertEquals(34, teensy.getControlPointCount());
    Assert.assertEquals("VUSB", teensy.getControlPointNodeName(33));
    Assert.assertEquals("GND", teensy.getControlPointNodeName(0));
  }

  @Test
  public void testDrawingAndOutline() {
    Teensy teensy = new Teensy();
    teensy.setControlPoint(new Point2D.Double(200, 200), 0);

    BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 600, 600));

    Project project = new Project();
    final AtomicInteger trackingStartCount = new AtomicInteger(0);
    final AtomicInteger trackingStopCount = new AtomicInteger(0);

    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {
        trackingStartCount.incrementAndGet();
      }
      @Override public void stopTracking() {
        trackingStopCount.incrementAndGet();
      }
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    // Test normal draw
    teensy.draw(g2d, ComponentState.NORMAL, false, project, observer);
    Assert.assertEquals(1, trackingStartCount.get());
    Assert.assertEquals(1, trackingStopCount.get());

    // Test selected state and outline mode
    teensy.draw(g2d, ComponentState.SELECTED, false, project, observer);
    teensy.draw(g2d, ComponentState.NORMAL, true, project, observer);

    // Test icon drawing
    teensy.drawIcon(g2d, 32, 32);

    // Test Teensy 4.1 draw
    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    teensy.draw(g2d, ComponentState.NORMAL, false, project, observer);

    g2d.dispose();
  }

  @Test
  public void testRotation() {
    Teensy teensy = new Teensy();
    teensy.setControlPoint(new Point2D.Double(200, 200), 0);

    Point2D p0Initial = teensy.getControlPoint(0);
    Point2D p14Initial = teensy.getControlPoint(14); // First right-row pin

    teensy.setOrientation(Orientation._90);
    Point2D p0Rot = teensy.getControlPoint(0);
    Point2D p14Rot = teensy.getControlPoint(14);

    // p0 should stay in place
    Assert.assertEquals(p0Initial.getX(), p0Rot.getX(), 0.01);
    Assert.assertEquals(p0Initial.getY(), p0Rot.getY(), 0.01);

    // After 90 deg clockwise rotation, right row pin should move from (x + rowSpacing, y) to (x, y + rowSpacing)
    double rowSpacing = new Size(15.24d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(p0Rot.getX(), p14Rot.getX(), 0.01);
    Assert.assertEquals(p0Rot.getY() + rowSpacing, p14Rot.getY(), 0.01);
  }

  @Test
  public void testBoardColor() {
    Teensy teensy = new Teensy();
    // Board should be green (like Pi Zero)
    Assert.assertEquals(Teensy.TEENSY_GREEN, teensy.getBodyColor());
  }

  @Test
  public void testHeadersProperty() {
    Teensy teensy = new Teensy();
    Assert.assertFalse("Headers should be false by default", teensy.getHeaders());

    teensy.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", teensy.getHeaders());

    teensy.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", teensy.getHeaders());

    // Test drawing with headers true and false for both 4.0 and 4.1
    BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 600, 600));
    Project project = new Project();
    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    teensy.setHeaders(false);
    teensy.draw(g2d, ComponentState.NORMAL, false, project, observer);

    teensy.setHeaders(true);
    teensy.draw(g2d, ComponentState.NORMAL, false, project, observer);

    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    teensy.setHeaders(true);
    teensy.draw(g2d, ComponentState.NORMAL, false, project, observer);

    g2d.dispose();
  }
}
