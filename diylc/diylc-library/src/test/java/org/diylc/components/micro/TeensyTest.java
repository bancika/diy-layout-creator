/*
 * 
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.micro;

import org.diylc.components.micro.Teensy.TeensyVersion;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class TeensyTest {

  @Test
  public void testPinoutPerVersion() {
    // 14 left + 14 right + a five-hole end cluster + the VUSB pad
    Teensy teensy40 = new Teensy();
    Assert.assertEquals(TeensyVersion.Teensy_4_0, teensy40.getVersion());
    Assert.assertEquals(34, teensy40.getControlPointCount());
    Assert.assertEquals("GND", teensy40.getControlPointNodeName(0));
    Assert.assertEquals("0 (RX1/CS1/CRX2)", teensy40.getControlPointNodeName(1));
    Assert.assertEquals("12 (MISO/MQSL)", teensy40.getControlPointNodeName(13));
    Assert.assertEquals("VIN (3.6-5.5V)", teensy40.getControlPointNodeName(14));
    Assert.assertEquals("13 (SCK/CRX1/LED)", teensy40.getControlPointNodeName(27));
    Assert.assertEquals("VBAT", teensy40.getControlPointNodeName(28));
    Assert.assertEquals("On/Off", teensy40.getControlPointNodeName(32));
    Assert.assertEquals("VUSB", teensy40.getControlPointNodeName(33));

    // the 3.2 has the same pin field but its own functions, starting with the analog ground
    Teensy teensy32 = version(TeensyVersion.Teensy_3_2);
    Assert.assertEquals(34, teensy32.getControlPointCount());
    Assert.assertEquals("Vin (3.6-6.0V)", teensy32.getControlPointNodeName(14));
    Assert.assertEquals("AGND", teensy32.getControlPointNodeName(15));
    Assert.assertEquals("VUSB", teensy32.getControlPointNodeName(33));

    // 24 left + 24 right + the five-hole cluster + the 3x2 Ethernet and 5-pin USB host headers
    // + the VUSB pad
    Teensy teensy41 = version(TeensyVersion.Teensy_4_1);
    Assert.assertEquals(65, teensy41.getControlPointCount());
    Assert.assertEquals("32 (OUT1B)", teensy41.getControlPointNodeName(23));
    Assert.assertEquals("VIN (3.6-5.5V)", teensy41.getControlPointNodeName(24));
    Assert.assertEquals("33 (MCLK2)", teensy41.getControlPointNodeName(47));
    Assert.assertEquals("VBAT", teensy41.getControlPointNodeName(48));
    Assert.assertEquals("ETH_TX-", teensy41.getControlPointNodeName(53));
    Assert.assertEquals("USB_5V", teensy41.getControlPointNodeName(59));
    Assert.assertEquals("VUSB", teensy41.getControlPointNodeName(64));

    // the silkscreen covers the two pin rows only; the end cluster and the VUSB pad are printed on
    // the underside. PJRC abbreviates the supply pads and drops the alternate functions, and the
    // compact boards have no room for the three pads level with the cluster and the VUSB pad
    Assert.assertEquals("0", teensy40.getSilkPinLabel(1));
    Assert.assertEquals("G", teensy40.getSilkPinLabel(0));
    Assert.assertEquals("5V", teensy40.getSilkPinLabel(14));
    Assert.assertEquals("3V", teensy40.getSilkPinLabel(16));
    Assert.assertEquals("", teensy40.getSilkPinLabel(13));
    Assert.assertEquals("", teensy40.getSilkPinLabel(27));
    Assert.assertEquals("", teensy32.getSilkPinLabel(15));

    // printing the names cornerwise leaves the 4.1 room for every pad of both rows, including the
    // ones the compact boards have to drop; only the ground level with the VUSB pad stays blank
    Assert.assertEquals("12", teensy41.getSilkPinLabel(13));
    Assert.assertEquals("33", teensy41.getSilkPinLabel(47));
    Assert.assertEquals("", teensy41.getSilkPinLabel(25));
  }

  @Test
  public void testGeometryPerVersion() {
    // the 3.2 shares the 4.0's outline and pin field; the 4.1 is the same width and longer
    for (TeensyVersion version : new TeensyVersion[] {TeensyVersion.Teensy_4_0,
        TeensyVersion.Teensy_3_2}) {
      Teensy teensy = version(version);
      MakerBoardTestSupport.assertRow(teensy, 0, 13);
      MakerBoardTestSupport.assertRow(teensy, 14, 27);
      MakerBoardTestSupport.assertRowSpacing(teensy, 0, 14, new Size(15.24d, SizeUnit.mm));
      MakerBoardTestSupport.assertRow(teensy, 28, 32);
      MakerBoardTestSupport.assertBoardSize(teensy, new Size(17.78d, SizeUnit.mm),
          new Size(35.56d, SizeUnit.mm));
      assertPin1Inset(teensy);
    }

    Teensy teensy41 = version(TeensyVersion.Teensy_4_1);
    MakerBoardTestSupport.assertRow(teensy41, 0, 23);
    MakerBoardTestSupport.assertRow(teensy41, 24, 47);
    MakerBoardTestSupport.assertRowSpacing(teensy41, 0, 24, new Size(15.24d, SizeUnit.mm));
    MakerBoardTestSupport.assertRow(teensy41, 48, 52);
    MakerBoardTestSupport.assertRow(teensy41, 59, 63);
    MakerBoardTestSupport.assertBoardSize(teensy41, new Size(17.78d, SizeUnit.mm),
        new Size(60.96d, SizeUnit.mm));
    assertPin1Inset(teensy41);

    // the Ethernet header is a 3x2 block on 2 mm pitch rather than the 0.1" the rest of the board
    // uses, so it gets its own rows
    double ethPitch = new Size(2.0d, SizeUnit.mm).convertToPixels();
    MakerBoardTestSupport.assertRow(teensy41, 53, 55, ethPitch);
    MakerBoardTestSupport.assertRow(teensy41, 56, 58, ethPitch);
    Assert.assertEquals("Ethernet row spacing", ethPitch,
        teensy41.getControlPoint(56).getY() - teensy41.getControlPoint(53).getY(), 0.01);
  }

  @Test
  public void testEveryVersionDraws() {
    for (TeensyVersion version : TeensyVersion.values()) {
      Teensy teensy = version(version);
      for (boolean headers : new boolean[] {false, true}) {
        teensy.setHeaders(headers);
        MakerBoardTestSupport.assertDrawsCleanly(teensy);
      }
    }
  }

  /** Every Teensy sets its first pad 0.05" in from the left and top edges. */
  private static void assertPin1Inset(Teensy teensy) {
    double inset = new Size(1.27d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(teensy.getVersion() + " pin 1 inset from the left edge", inset,
        teensy.getControlPoint(0).getX() - teensy.getBodyShape().getBounds2D().getX(), 0.1);
    Assert.assertEquals(teensy.getVersion() + " pin 1 inset from the top edge", inset,
        teensy.getControlPoint(0).getY() - teensy.getBodyShape().getBounds2D().getY(), 0.1);
  }

  private static Teensy version(TeensyVersion version) {
    Teensy teensy = new Teensy();
    teensy.setVersion(version);
    return teensy;
  }
}
