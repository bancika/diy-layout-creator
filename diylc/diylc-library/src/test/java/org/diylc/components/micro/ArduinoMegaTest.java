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

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Test;

public class ArduinoMegaTest {

  @Test
  public void testPinout() {
    ArduinoMega mega = new ArduinoMega();
    Assert.assertEquals(98, mega.getControlPointCount());

    // power, analog low, analog high, digital low, digital high, communication, the 2x18 block,
    // then the two 2x3 ICSP blocks
    Assert.assertEquals("NC", mega.getControlPointNodeName(0));
    Assert.assertEquals("VIN", mega.getControlPointNodeName(7));
    Assert.assertEquals("A0", mega.getControlPointNodeName(8));
    Assert.assertEquals("A7", mega.getControlPointNodeName(15));
    Assert.assertEquals("A8", mega.getControlPointNodeName(16));
    Assert.assertEquals("A15", mega.getControlPointNodeName(23));
    Assert.assertEquals("D0 (RX0)", mega.getControlPointNodeName(24));
    Assert.assertEquals("D7 (~)", mega.getControlPointNodeName(31));
    Assert.assertEquals("D8 (~)", mega.getControlPointNodeName(32));
    Assert.assertEquals("SCL", mega.getControlPointNodeName(41));
    Assert.assertEquals("D14 (TX3)", mega.getControlPointNodeName(42));
    Assert.assertEquals("D21 (SCL)", mega.getControlPointNodeName(49));
    Assert.assertEquals("D22", mega.getControlPointNodeName(50));
    Assert.assertEquals("D53 (SS)", mega.getControlPointNodeName(81));
    Assert.assertEquals("GND_EXT1", mega.getControlPointNodeName(82));
    Assert.assertEquals("5V_EXT2", mega.getControlPointNodeName(85));
    Assert.assertEquals("MISO", mega.getControlPointNodeName(86));
    Assert.assertEquals("GND_ICSP", mega.getControlPointNodeName(91));
    Assert.assertEquals("MISO_16U2", mega.getControlPointNodeName(92));
    Assert.assertEquals("GND_16U2", mega.getControlPointNodeName(97));

    // the silkscreen prints what is on the board rather than the node name: every ground says
    // "GND", the reserved first pin says nothing, and the 2x18 block's supply rows are printed
    // once for the pair, on the outer column
    Assert.assertEquals("GND", mega.getSilkPinLabel(5));
    Assert.assertEquals("GND", mega.getSilkPinLabel(6));
    Assert.assertEquals("", mega.getSilkPinLabel(0));
    Assert.assertEquals("", mega.getSilkPinLabel(82));
    Assert.assertEquals("GND", mega.getSilkPinLabel(83));
    Assert.assertEquals("", mega.getSilkPinLabel(84));
    Assert.assertEquals("5V", mega.getSilkPinLabel(85));
  }

  @Test
  public void testShieldFootprintAndDoubleDigitalBlock() {
    ArduinoMega mega = new ArduinoMega();
    double spacing = MakerBoardTestSupport.PIN_SPACING;

    MakerBoardTestSupport.assertRow(mega, 0, 7);
    MakerBoardTestSupport.assertRow(mega, 8, 15);
    MakerBoardTestSupport.assertRow(mega, 16, 23);
    MakerBoardTestSupport.assertRow(mega, 24, 31);
    MakerBoardTestSupport.assertRow(mega, 32, 41);
    MakerBoardTestSupport.assertRow(mega, 42, 49);

    // the Mega keeps the Uno's shield geometry: 0.2" between the power and analog headers and the
    // 0.16" offset between the two digital headers
    Assert.assertEquals("Gap between VIN and A0", 2 * spacing,
        mega.getControlPoint(7).distance(mega.getControlPoint(8)), 0.01);
    Assert.assertEquals("Offset between D7 and D8", 32.0,
        mega.getControlPoint(31).distance(mega.getControlPoint(32)), 0.01);

    // the 2x18 block is powered from its top row, level with the digital header, and grounded at
    // the bottom one, with D22..D53 filling the sixteen rows in between; even numbers run down the
    // inner column and odd ones down the outer, power and ground included
    Point2D d0 = mega.getControlPoint(24);
    Point2D fiveVoltInner = mega.getControlPoint(84);
    Point2D fiveVoltOuter = mega.getControlPoint(85);
    Point2D d22 = mega.getControlPoint(50);
    Point2D groundInner = mega.getControlPoint(82);
    Point2D groundOuter = mega.getControlPoint(83);

    Assert.assertEquals(d0.getY(), fiveVoltInner.getY(), 0.01);
    Assert.assertEquals(d0.getY(), fiveVoltOuter.getY(), 0.01);
    Assert.assertEquals(spacing, d22.getY() - fiveVoltInner.getY(), 0.01);
    Assert.assertEquals(17 * spacing, groundInner.getY() - fiveVoltInner.getY(), 0.01);
    Assert.assertEquals(groundInner.getY(), groundOuter.getY(), 0.01);
    Assert.assertEquals(fiveVoltInner.getX(), d22.getX(), 0.01);
    Assert.assertEquals(fiveVoltInner.getX(), groundInner.getX(), 0.01);
    Assert.assertEquals(fiveVoltOuter.getX(), groundOuter.getX(), 0.01);
    Assert.assertEquals(spacing, fiveVoltOuter.getX() - fiveVoltInner.getX(), 0.01);
  }
}
