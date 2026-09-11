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
import java.awt.geom.Rectangle2D;

import org.diylc.components.micro.ArduinoUno.ArduinoUnoVersion;
import org.junit.Assert;
import org.junit.Test;

public class ArduinoUnoTest {

  @Test
  public void testPinoutPerVersion() {
    ArduinoUno r3 = new ArduinoUno();
    Assert.assertEquals(ArduinoUnoVersion.REV3, r3.getVersion());
    Assert.assertEquals(44, r3.getControlPointCount());

    // power, analog, digital low, digital high, main ICSP, then the 16U2 ICSP block
    Assert.assertEquals("NC", r3.getControlPointNodeName(0));
    Assert.assertEquals("VIN", r3.getControlPointNodeName(7));
    Assert.assertEquals("A0", r3.getControlPointNodeName(8));
    Assert.assertEquals("A5", r3.getControlPointNodeName(13));
    Assert.assertEquals("D0 (RX)", r3.getControlPointNodeName(14));
    Assert.assertEquals("D7", r3.getControlPointNodeName(21));
    Assert.assertEquals("D8", r3.getControlPointNodeName(22));
    Assert.assertEquals("SCL", r3.getControlPointNodeName(31));
    Assert.assertEquals("MISO", r3.getControlPointNodeName(32));
    Assert.assertEquals("GND_ICSP", r3.getControlPointNodeName(37));
    Assert.assertEquals("MISO_16U2", r3.getControlPointNodeName(38));
    Assert.assertEquals("GND_16U2", r3.getControlPointNodeName(43));

    // the silkscreen prints what is on the board rather than the node name: both grounds of the
    // power header say "GND" and the reserved first pin says nothing at all
    Assert.assertEquals("GND1", r3.getControlPointNodeName(5));
    Assert.assertEquals("GND2", r3.getControlPointNodeName(6));
    Assert.assertEquals("GND", r3.getSilkPinLabel(5));
    Assert.assertEquals("GND", r3.getSilkPinLabel(6));
    Assert.assertEquals("", r3.getSilkPinLabel(0));

    // the Leonardo has no ATmega16U2, so its second 2x3 block is gone, and I2C moves to D2 / D3
    ArduinoUno leonardo = version(ArduinoUnoVersion.LEONARDO);
    Assert.assertEquals(38, leonardo.getControlPointCount());
    Assert.assertEquals("NC", leonardo.getControlPointNodeName(0));
    Assert.assertEquals("D2 (SDA)", leonardo.getControlPointNodeName(16));
    Assert.assertEquals("D3 (~, SCL)", leonardo.getControlPointNodeName(17));

    // both R4 boards use the R3's reserved power header pin for boot mode selection; the WiFi adds
    // OFF / GND / VRTC on top of the 44, while the Minima drops the second ICSP block for a single
    // SWD connector: 44 - 6 + 1 = 39
    ArduinoUno wifi = version(ArduinoUnoVersion.R4_WIFI);
    ArduinoUno minima = version(ArduinoUnoVersion.R4_MINIMA);
    Assert.assertEquals(47, wifi.getControlPointCount());
    Assert.assertEquals(39, minima.getControlPointCount());
    Assert.assertEquals("BOOT", wifi.getControlPointNodeName(0));
    Assert.assertEquals("BOOT", minima.getControlPointNodeName(0));
    Assert.assertEquals("OFF", wifi.getControlPointNodeName(44));
    Assert.assertEquals("VRTC", wifi.getSilkPinLabel(46));
    Assert.assertEquals("SWD", minima.getControlPointNodeName(38));
  }

  @Test
  public void testShieldFootprint() {
    ArduinoUno r3 = new ArduinoUno();
    double spacing = MakerBoardTestSupport.PIN_SPACING;

    MakerBoardTestSupport.assertRow(r3, 0, 7);
    MakerBoardTestSupport.assertRow(r3, 8, 13);
    MakerBoardTestSupport.assertRow(r3, 14, 21);
    MakerBoardTestSupport.assertRow(r3, 22, 31);

    // the two gaps the shield standard fixes: 0.2" between the power and analog headers, and the
    // notorious 0.16" offset between the two digital headers
    Assert.assertEquals("Gap between VIN and A0", 2 * spacing,
        r3.getControlPoint(7).distance(r3.getControlPoint(8)), 0.01);
    Assert.assertEquals("Offset between D7 and D8", 32.0,
        r3.getControlPoint(21).distance(r3.getControlPoint(22)), 0.01);

    // every version drops onto the same shield, so the 38 header pins they share never move
    for (ArduinoUnoVersion version : ArduinoUnoVersion.values()) {
      MakerBoardTestSupport.assertSharedFootprint(r3, version(version), 38);
    }

    // the Minima's SWD connector sits on the board, in the top right quadrant below the digital
    // header where the second ICSP block used to be
    ArduinoUno minima = version(ArduinoUnoVersion.R4_MINIMA);
    Point2D swd = minima.getControlPoint(38);
    Rectangle2D bounds = minima.getBodyShape().getBounds2D();
    Assert.assertTrue("SWD should be on the board", bounds.contains(swd));
    Assert.assertTrue("SWD should be on the right half", swd.getX() > bounds.getCenterX());
    Assert.assertTrue("SWD should be below the digital header",
        swd.getY() > minima.getControlPoint(14).getY() && swd.getY() < bounds.getCenterY());
  }

  @Test
  public void testEveryVersionDraws() {
    for (ArduinoUnoVersion version : ArduinoUnoVersion.values()) {
      MakerBoardTestSupport.assertDrawsCleanly(version(version));
    }
  }

  private static ArduinoUno version(ArduinoUnoVersion version) {
    ArduinoUno uno = new ArduinoUno();
    uno.setVersion(version);
    return uno;
  }
}
