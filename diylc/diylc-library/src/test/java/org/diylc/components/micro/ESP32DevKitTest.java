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

import java.awt.geom.Rectangle2D;

import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.micro.ESP32DevKit.DevKitVersion;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ESP32DevKitTest {

  @Test
  public void testPinoutPerVersion() {
    ESP32DevKit devKit = new ESP32DevKit();
    Assert.assertEquals(DevKitVersion.DevKit_V1_30Pin, devKit.getVersion());
    Assert.assertEquals(30, devKit.getControlPointCount());
    Assert.assertEquals(38, version(DevKitVersion.DevKitC_V4_38Pin).getControlPointCount());
    Assert.assertEquals(44, version(DevKitVersion.ESP32_S3_DevKitC_44Pin).getControlPointCount());
    Assert.assertEquals(30, version(DevKitVersion.ESP32_C3_DevKitM_1).getControlPointCount());
    Assert.assertEquals(32, version(DevKitVersion.ESP32_C6_DevKitC_1).getControlPointCount());

    // the silkscreen carries the bare board label while the node name carries the GPIO mapping,
    // which on these boards is the thing people most often get wrong
    Assert.assertEquals("EN", devKit.getSilkPinLabel(0));
    Assert.assertEquals("D34", devKit.getSilkPinLabel(3));
    Assert.assertEquals("GPIO34 (ADC1_CH6, VDET_1)", devKit.getControlPointNodeName(3));
    Assert.assertEquals("TX0 (GPIO1, U0TXD)", devKit.getControlPointNodeName(17));
    Assert.assertEquals("VIN", devKit.getControlPointNodeName(14));
    Assert.assertEquals("3V3", devKit.getControlPointNodeName(29));

    // the two grounds of the 30-pin board are one net apiece, so they cannot share a node name
    Assert.assertEquals("GND_1", devKit.getControlPointNodeName(13));
    Assert.assertEquals("GND_2", devKit.getControlPointNodeName(28));
    Assert.assertEquals("GND", devKit.getSilkPinLabel(13));
    Assert.assertEquals("GND", devKit.getSilkPinLabel(28));

    // the row ends of the remaining versions, taken from the Espressif user guides' header tables
    ESP32DevKit devKitC = version(DevKitVersion.DevKitC_V4_38Pin);
    Assert.assertEquals("3V3", devKitC.getControlPointNodeName(0));
    Assert.assertEquals("5V", devKitC.getSilkPinLabel(18));
    Assert.assertEquals("D0 (GPIO7, flash)", devKitC.getControlPointNodeName(36));
    Assert.assertEquals("CLK", devKitC.getSilkPinLabel(37));

    ESP32DevKit s3 = version(DevKitVersion.ESP32_S3_DevKitC_44Pin);
    Assert.assertEquals("3V3_1", s3.getControlPointNodeName(0));
    Assert.assertEquals("3V3", s3.getSilkPinLabel(0));
    Assert.assertEquals("GND_1", s3.getControlPointNodeName(21));
    Assert.assertEquals("GPIO20 (U1CTS, ADC2_CH9, USB_D+)", s3.getControlPointNodeName(40));
    Assert.assertEquals("GND_4", s3.getControlPointNodeName(43));

    ESP32DevKit c3 = version(DevKitVersion.ESP32_C3_DevKitM_1);
    Assert.assertEquals("GND_1", c3.getControlPointNodeName(0));
    Assert.assertEquals("GPIO8 (RGB LED)", c3.getControlPointNodeName(20));
    Assert.assertEquals("GND_10", c3.getControlPointNodeName(29));

    ESP32DevKit c6 = version(DevKitVersion.ESP32_C6_DevKitC_1);
    Assert.assertEquals("3V3", c6.getControlPointNodeName(0));
    Assert.assertEquals("GPIO13 (USB_D+)", c6.getControlPointNodeName(28));
    Assert.assertEquals("NC_2", c6.getControlPointNodeName(31));
  }

  @Test
  public void testGeometryPerVersion() {
    // the classic DevKits are 1.0" wide over the pin rows, the RISC-V and S3 boards 0.9"
    Size classicRowSpacing = new Size(1.0d, SizeUnit.in);
    Size narrowRowSpacing = new Size(22.86d, SizeUnit.mm);
    assertTwoRows(new ESP32DevKit(), classicRowSpacing);
    assertTwoRows(version(DevKitVersion.DevKitC_V4_38Pin), classicRowSpacing);
    assertTwoRows(version(DevKitVersion.ESP32_S3_DevKitC_44Pin), narrowRowSpacing);
    assertTwoRows(version(DevKitVersion.ESP32_C3_DevKitM_1), narrowRowSpacing);
    assertTwoRows(version(DevKitVersion.ESP32_C6_DevKitC_1), narrowRowSpacing);

    MakerBoardTestSupport.assertBoardSize(new ESP32DevKit(), new Size(28.33d, SizeUnit.mm),
        new Size(51.45d, SizeUnit.mm));
    MakerBoardTestSupport.assertBoardSize(version(DevKitVersion.DevKitC_V4_38Pin),
        new Size(27.9d, SizeUnit.mm), new Size(54.4d, SizeUnit.mm));

    // the S3's antenna sticks out past the end of the board
    ESP32DevKit s3 = version(DevKitVersion.ESP32_S3_DevKitC_44Pin);
    Assert.assertEquals("S3 outline including the antenna",
        new Size(62.74d + 6.0d, SizeUnit.mm).convertToPixels(),
        s3.getBodyShape().getBounds2D().getHeight(), 1.0);

    // the RISC-V boards share the DevKitC form factor and hang their pin field centred on it
    for (DevKitVersion version : new DevKitVersion[] {DevKitVersion.ESP32_C3_DevKitM_1,
        DevKitVersion.ESP32_C6_DevKitC_1}) {
      ESP32DevKit riscV = version(version);
      Rectangle2D bounds = riscV.getBodyShape().getBounds2D();
      Assert.assertEquals(version + " board width",
          new Size(25.40d, SizeUnit.mm).convertToPixels(), bounds.getWidth(), 0.1);
      Assert.assertEquals(version + " pin field should be centred",
          riscV.getControlPoint(0).getX() + narrowRowSpacing.convertToPixels() / 2.0,
          bounds.getCenterX(), 0.1);
    }
  }

  @Test
  public void testEveryVersionDraws() {
    for (DevKitVersion version : DevKitVersion.values()) {
      ESP32DevKit devKit = version(version);
      for (boolean headers : new boolean[] {false, true}) {
        devKit.setHeaders(headers);
        MakerBoardTestSupport.assertDrawsCleanly(devKit);
      }
    }
  }

  @Test
  public void testDisplayPinLabelStripsAnnotations() {
    // the silkscreen defaults to the node name without the parenthesized function list and
    // without the trailing disambiguator that keeps two grounds apart in the netlist
    Assert.assertEquals("GPIO34", AbstractMakerBoard.getDisplayPinLabel("GPIO34 (ADC1_CH6)"));
    Assert.assertEquals("3V3", AbstractMakerBoard.getDisplayPinLabel("3V3_1"));
    Assert.assertEquals("EN", AbstractMakerBoard.getDisplayPinLabel("EN"));
    Assert.assertEquals("", AbstractMakerBoard.getDisplayPinLabel(null));
    Assert.assertEquals("", AbstractMakerBoard.getDisplayPinLabel(""));
  }

  private static void assertTwoRows(ESP32DevKit devKit, Size rowSpacing) {
    int pinsPerRow = devKit.getControlPointCount() / 2;
    MakerBoardTestSupport.assertRow(devKit, 0, pinsPerRow - 1);
    MakerBoardTestSupport.assertRow(devKit, pinsPerRow, devKit.getControlPointCount() - 1);
    MakerBoardTestSupport.assertRowSpacing(devKit, 0, pinsPerRow, rowSpacing);
  }

  private static ESP32DevKit version(DevKitVersion version) {
    ESP32DevKit devKit = new ESP32DevKit();
    devKit.setVersion(version);
    return devKit;
  }
}
