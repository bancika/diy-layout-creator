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

import org.diylc.components.micro.RaspberryPiPico.PicoVersion;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiPicoTest {

  @Test
  public void testPinout() {
    RaspberryPiPico pico = new RaspberryPiPico();
    Assert.assertEquals(PicoVersion.PICO, pico.getVersion());

    // 2x20 castellated pads plus the three debug pads
    Assert.assertEquals(43, pico.getControlPointCount());
    Assert.assertEquals("GP0", pico.getControlPointNodeName(0));
    Assert.assertEquals("GP15", pico.getControlPointNodeName(19));
    Assert.assertEquals("VBUS", pico.getControlPointNodeName(20));
    Assert.assertEquals("VSYS", pico.getControlPointNodeName(21));
    Assert.assertEquals("GP16", pico.getControlPointNodeName(39));
    Assert.assertEquals("SWCLK", pico.getControlPointNodeName(40));
    Assert.assertEquals("GND_SWD", pico.getControlPointNodeName(41));
    Assert.assertEquals("SWDIO", pico.getControlPointNodeName(42));

    // the RP2350 boards are pin- and size-identical to their RP2040 counterparts, so only the chip
    // marking tells them apart
    assertSameBoard(PicoVersion.PICO, PicoVersion.PICO_2);
    assertSameBoard(PicoVersion.PICO_W, PicoVersion.PICO_2_W);
  }

  @Test
  public void testGeometry() {
    RaspberryPiPico pico = new RaspberryPiPico();
    Rectangle2D bounds = pico.getBodyShape().getBounds2D();

    MakerBoardTestSupport.assertBoardSize(pico, new Size(21.0d, SizeUnit.mm),
        new Size(51.0d, SizeUnit.mm));
    MakerBoardTestSupport.assertRow(pico, 0, 19);
    MakerBoardTestSupport.assertRow(pico, 20, 39);
    MakerBoardTestSupport.assertRowSpacing(pico, 0, 20, new Size(0.7d, SizeUnit.in));

    // the pad field is symmetrical: 1.61 mm in from each long edge and 1.37 mm from each end
    Point2D gp0 = pico.getControlPoint(0);
    Point2D gp15 = pico.getControlPoint(19);
    Point2D vbus = pico.getControlPoint(20);
    double insetX = new Size(1.61d, SizeUnit.mm).convertToPixels();
    double insetY = new Size(1.37d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(insetX, gp0.getX() - bounds.getX(), 0.1);
    Assert.assertEquals(insetY, gp0.getY() - bounds.getY(), 0.1);
    Assert.assertEquals(insetX, bounds.getMaxX() - vbus.getX(), 0.1);
    Assert.assertEquals(insetY, bounds.getMaxY() - gp15.getY(), 0.1);

    // the debug pads run along the bottom edge of the original, level with the last row of pads
    // and centred on the board
    MakerBoardTestSupport.assertRow(pico, 40, 42);
    Assert.assertEquals(gp15.getY(), pico.getControlPoint(41).getY(), 0.01);
    Assert.assertEquals(bounds.getCenterX(), pico.getControlPoint(41).getX(), 0.1);

    // the wireless boards move them inboard to make room for the radio module, 19.8 mm up from the
    // bottom edge and 7.38 mm in from the right one
    RaspberryPiPico picoW = version(PicoVersion.PICO_W);
    Rectangle2D wirelessBounds = picoW.getBodyShape().getBounds2D();
    Point2D debugCentre = picoW.getControlPoint(41);
    MakerBoardTestSupport.assertRow(picoW, 40, 42);
    Assert.assertEquals(new Size(19.8d, SizeUnit.mm).convertToPixels(),
        wirelessBounds.getMaxY() - debugCentre.getY(), 0.1);
    Assert.assertEquals(new Size(7.38d, SizeUnit.mm).convertToPixels(),
        wirelessBounds.getMaxX() - debugCentre.getX(), 0.1);
  }

  @Test
  public void testEveryVersionDraws() {
    for (PicoVersion version : PicoVersion.values()) {
      RaspberryPiPico pico = version(version);
      for (boolean headers : new boolean[] {false, true}) {
        pico.setHeaders(headers);
        MakerBoardTestSupport.assertDrawsCleanly(pico);
      }
    }
  }

  private static void assertSameBoard(PicoVersion a, PicoVersion b) {
    RaspberryPiPico first = version(a);
    RaspberryPiPico second = version(b);
    Assert.assertEquals(a + " and " + b + " should have the same pin count",
        first.getControlPointCount(), second.getControlPointCount());
    MakerBoardTestSupport.assertSharedFootprint(first, second, first.getControlPointCount());
    for (int i = 0; i < first.getControlPointCount(); i++) {
      Assert.assertEquals("Pin " + i + " on " + a + " and " + b,
          first.getControlPointNodeName(i), second.getControlPointNodeName(i));
    }
  }

  private static RaspberryPiPico version(PicoVersion version) {
    RaspberryPiPico pico = new RaspberryPiPico();
    pico.setVersion(version);
    return pico;
  }
}
