/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.core.gerber;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GerberLayerTest {

  @Test
  public void testFlipSwapsSides() {
    assertEquals(GerberLayer.CopperBot, GerberLayer.CopperTop.flip());
    assertEquals(GerberLayer.CopperTop, GerberLayer.CopperBot.flip());
    assertEquals(GerberLayer.SilkscreenBot, GerberLayer.SilkscreenTop.flip());
    assertEquals(GerberLayer.SilkscreenTop, GerberLayer.SilkscreenBot.flip());
    assertEquals(GerberLayer.SolderMaskBot, GerberLayer.SolderMaskTop.flip());
    assertEquals(GerberLayer.SolderMaskTop, GerberLayer.SolderMaskBot.flip());
  }

  @Test
  public void testFlipKeepsLayersThroughTheBoard() {
    assertEquals(GerberLayer.Outline, GerberLayer.Outline.flip());
    assertEquals(GerberLayer.DrillPlated, GerberLayer.DrillPlated.flip());
    assertEquals(GerberLayer.DrillNonPlated, GerberLayer.DrillNonPlated.flip());
  }

  @Test
  public void testFlipTwiceReturnsLayer() {
    for (GerberLayer layer : GerberLayer.values()) {
      assertEquals(layer, layer.flip().flip());
    }
  }
}
