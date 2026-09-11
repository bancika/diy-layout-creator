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

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

import org.junit.Assert;
import org.junit.Test;

public class ESP8266NodeMCUTest {

  @Test
  public void testPinout() {
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();
    Assert.assertEquals(30, mcu.getControlPointCount());

    // left row, top to bottom
    Assert.assertEquals("A0 (ADC0)", mcu.getControlPointNodeName(0));
    Assert.assertEquals("CLK (GPIO6, flash)", mcu.getControlPointNodeName(8));
    Assert.assertEquals("VIN", mcu.getControlPointNodeName(14));

    // right row, top to bottom
    Assert.assertEquals("D0 (GPIO16, WAKE)", mcu.getControlPointNodeName(15));
    Assert.assertEquals("3V3_3", mcu.getControlPointNodeName(29));

    // the silkscreen prints the bare board label while the node name carries the GPIO mapping,
    // which on this board is the thing people most often get wrong
    Assert.assertEquals("D1 (GPIO5, SCL)", mcu.getControlPointNodeName(16));
    Assert.assertEquals("D2 (GPIO4, SDA)", mcu.getControlPointNodeName(17));
    Assert.assertEquals("D7 (GPIO13, HSPI MOSI)", mcu.getControlPointNodeName(24));
    Assert.assertEquals("D1", mcu.getSilkPinLabel(16));
    Assert.assertEquals("RSV", mcu.getSilkPinLabel(1));

    // the four grounds and three supply pads are separate nets, so they cannot share a node name
    Assert.assertEquals("GND_1", mcu.getControlPointNodeName(9));
    Assert.assertEquals("GND_4", mcu.getControlPointNodeName(28));
    Assert.assertEquals("3V3_1", mcu.getControlPointNodeName(10));
    Assert.assertEquals("GND", mcu.getSilkPinLabel(9));
    Assert.assertEquals("3V3", mcu.getSilkPinLabel(10));
  }

  @Test
  public void testGeometry() {
    ESP8266NodeMCU mcu = new ESP8266NodeMCU();

    MakerBoardTestSupport.assertRow(mcu, 0, 14);
    MakerBoardTestSupport.assertRow(mcu, 15, 29);
    MakerBoardTestSupport.assertRowSpacing(mcu, 0, 15, new Size(0.9d, SizeUnit.in));
    MakerBoardTestSupport.assertBoardSize(mcu, new Size(25.7d, SizeUnit.mm),
        new Size(48.0d, SizeUnit.mm));

    Rectangle2D bounds = mcu.getBodyShape().getBounds2D();
    Assert.assertEquals("Margin above the first pin",
        new Size(6.22d, SizeUnit.mm).convertToPixels(),
        mcu.getControlPoint(0).getY() - bounds.getY(), 0.1);
  }
}
