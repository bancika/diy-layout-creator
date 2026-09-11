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

public class WemosD1MiniTest {

  @Test
  public void testPinout() {
    WemosD1Mini mcu = new WemosD1Mini();
    Assert.assertEquals(16, mcu.getControlPointCount());

    // left row, top to bottom
    Assert.assertEquals("RST", mcu.getControlPointNodeName(0));
    Assert.assertEquals("A0", mcu.getControlPointNodeName(1));
    Assert.assertEquals("D0 (GPIO16)", mcu.getControlPointNodeName(2));
    Assert.assertEquals("3V3", mcu.getControlPointNodeName(7));

    // right row, top to bottom; the board prints the ground pad as "G"
    Assert.assertEquals("5V", mcu.getControlPointNodeName(8));
    Assert.assertEquals("G (GND)", mcu.getControlPointNodeName(9));
    Assert.assertEquals("D1 (GPIO5)", mcu.getControlPointNodeName(13));
    Assert.assertEquals("TX (GPIO1)", mcu.getControlPointNodeName(15));
  }

  @Test
  public void testGeometry() {
    WemosD1Mini mcu = new WemosD1Mini();

    MakerBoardTestSupport.assertRow(mcu, 0, 7);
    MakerBoardTestSupport.assertRow(mcu, 8, 15);
    MakerBoardTestSupport.assertBoardSize(mcu, new Size(1.0d, SizeUnit.in),
        new Size(1.34d, SizeUnit.in));

    // the right row runs bottom to top, so it is the last pin that lines up with the first
    Assert.assertEquals("Row spacing", new Size(0.9d, SizeUnit.in).convertToPixels(),
        mcu.getControlPoint(0).distance(mcu.getControlPoint(15)), 0.01);

    Rectangle2D bounds = mcu.getBodyShape().getBounds2D();
    Assert.assertEquals("Margin above the first pin",
        new Size(0.275d, SizeUnit.in).convertToPixels(),
        mcu.getControlPoint(0).getY() - bounds.getY(), 0.1);
  }
}
