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
import org.diylc.components.maker.MakerBoardTestSupport;
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

    // right row in array order, which runs bottom to top on the board; the ground pad prints "G"
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

  /**
   * The right row runs bottom to top, so its first pin is level with the left row's last. This is
   * the fact the silkscreen has to follow, and the reason the labels are taken from each control
   * point rather than from a parallel array.
   */
  @Test
  public void theRightRowRunsBottomToTop() {
    WemosD1Mini mcu = new WemosD1Mini();
    Assert.assertEquals("the rows should start and end level", mcu.getControlPoint(7).getY(),
        mcu.getControlPoint(8).getY(), 0.01);
    Assert.assertTrue("pin 8 should sit below pin 15",
        mcu.getControlPoint(8).getY() > mcu.getControlPoint(15).getY());
  }

  /**
   * The silkscreen is the node names with their annotations stripped. Two hand-maintained arrays
   * used to carry them, the right one written in reverse to compensate for the row direction
   * above -- a duplicate of PIN_NAMES that had to be kept in step by hand and would have drifted
   * silently if a pin were ever renamed.
   */
  @Test
  public void silkLabelsComeFromTheNodeNames() {
    WemosD1Mini mcu = new WemosD1Mini();
    String[] expected = new String[] {"RST", "A0", "D0", "D5", "D6", "D7", "D8", "3V3", "5V", "G",
        "D4", "D3", "D2", "D1", "RX", "TX"};
    Assert.assertEquals(expected.length, mcu.getControlPointCount());
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i],
          AbstractMakerBoard.getDisplayPinLabel(mcu.getControlPointNodeName(i)));
    }
  }
}
