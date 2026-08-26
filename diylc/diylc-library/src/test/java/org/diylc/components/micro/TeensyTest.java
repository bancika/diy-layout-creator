package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
    Assert.assertEquals("ETH_TX-", teensy.getControlPointNodeName(53));
    Assert.assertEquals("ETH_LED", teensy.getControlPointNodeName(54));
    Assert.assertEquals("ETH_RX+", teensy.getControlPointNodeName(55));
    Assert.assertEquals("ETH_TX+", teensy.getControlPointNodeName(56));
    Assert.assertEquals("ETH_GND", teensy.getControlPointNodeName(57));
    Assert.assertEquals("ETH_RX-", teensy.getControlPointNodeName(58));

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

    // Ethernet Header: 3x2 at 2.0mm pitch, center is 4.45mm left of right column, 13.97mm down from pin 1
    double ethPitch = new Size(2.0d, SizeUnit.mm).convertToPixels();
    double expectedEthCenterX = pRight.getX() - new Size(4.45d, SizeUnit.mm).convertToPixels();
    double expectedEthCenterY = pLeft.getY() + new Size(13.97d, SizeUnit.mm).convertToPixels();
    double sumX = 0;
    double sumY = 0;
    for (int i = 53; i <= 58; i++) {
      sumX += teensy.getControlPoint(i).getX();
      sumY += teensy.getControlPoint(i).getY();
    }
    Assert.assertEquals("Ethernet center X", expectedEthCenterX, sumX / 6.0, 0.01);
    Assert.assertEquals("Ethernet center Y", expectedEthCenterY, sumY / 6.0, 0.01);
    // Row 1 (53..55) X spacing is ethPitch
    Assert.assertEquals("ETH Row 1 Col 0-1 X spacing", ethPitch, teensy.getControlPoint(54).getX() - teensy.getControlPoint(53).getX(), 0.01);
    Assert.assertEquals("ETH Row 1 Col 1-2 X spacing", ethPitch, teensy.getControlPoint(55).getX() - teensy.getControlPoint(54).getX(), 0.01);
    // Row 2 (56..58) X spacing is ethPitch
    Assert.assertEquals("ETH Row 2 Col 0-1 X spacing", ethPitch, teensy.getControlPoint(57).getX() - teensy.getControlPoint(56).getX(), 0.01);
    Assert.assertEquals("ETH Row 2 Col 1-2 X spacing", ethPitch, teensy.getControlPoint(58).getX() - teensy.getControlPoint(57).getX(), 0.01);
    // Row-to-row Y spacing is ethPitch
    Assert.assertEquals("ETH Col 0 Y spacing", ethPitch, teensy.getControlPoint(56).getY() - teensy.getControlPoint(53).getY(), 0.01);
    Assert.assertEquals("ETH Col 1 Y spacing", ethPitch, teensy.getControlPoint(57).getY() - teensy.getControlPoint(54).getY(), 0.01);
    Assert.assertEquals("ETH Col 2 Y spacing", ethPitch, teensy.getControlPoint(58).getY() - teensy.getControlPoint(55).getY(), 0.01);

    // USB Host Header: vertical at 0.1" pitch, spaced 3.05mm from left column, Y = 2.5 * spacing
    double expectedUsbHostX = pLeft.getX() + new Size(3.05d, SizeUnit.mm).convertToPixels();
    double expectedUsbHostY0 = pLeft.getY() + 2.5 * spacing;
    Assert.assertEquals("USB host pin 0 X offset", expectedUsbHostX, teensy.getControlPoint(59).getX(), 0.01);
    Assert.assertEquals("USB host pin 0 Y offset", expectedUsbHostY0, teensy.getControlPoint(59).getY(), 0.01);
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
  public void testHeadersProperty() {
    Teensy teensy = new Teensy();
    Assert.assertFalse("Headers should be false by default", teensy.getHeaders());

    teensy.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", teensy.getHeaders());

    teensy.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", teensy.getHeaders());
  }
}
