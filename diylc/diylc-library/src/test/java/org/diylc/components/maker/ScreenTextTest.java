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
package org.diylc.components.maker;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.diylc.common.Display;
import org.diylc.components.MakerBoardPainter;
import org.junit.Assert;
import org.junit.Test;

/**
 * What {@code MakerBoardPainter.drawScreenText} promises, independently of the four displays that
 * call it. The rule worth pinning down is that it prints nothing at all rather than a crop: a
 * panel too small for its own description is left dark, the same way a pin name with nowhere to go
 * is left off the silkscreen.
 */
public class ScreenTextTest {

  private static final String VALUE = "0.96\" SSD1306 128x64, I2C";

  private static int inkedPixels(Rectangle2D area, Display display, String name, String value) {
    BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    try {
      MakerBoardPainter.drawScreenText(g2d, area, Color.WHITE, display, name, value);
    } finally {
      g2d.dispose();
    }
    int count = 0;
    for (int rgb : image.getRGB(0, 0, 600, 400, null, 0, 600)) {
      if ((rgb >>> 24) != 0) {
        count++;
      }
    }
    return count;
  }

  @Test
  public void aPanelWithRoomPrintsItsDescription() {
    Assert.assertTrue(
        inkedPixels(new Rectangle2D.Double(10, 10, 400, 200), Display.VALUE, "DISP1", VALUE) > 0);
  }

  @Test
  public void aPanelWithoutRoomPrintsNothing() {
    Assert.assertEquals(0,
        inkedPixels(new Rectangle2D.Double(10, 10, 24, 10), Display.VALUE, "DISP1", VALUE));
  }

  @Test
  public void aPanelTooShallowForTheBlockPrintsNothing() {
    // wide enough for every line, but with room for one of the two
    Assert.assertEquals(0,
        inkedPixels(new Rectangle2D.Double(10, 10, 400, 16), Display.VALUE, "DISP1", VALUE));
  }

  @Test
  public void noneLeavesTheScreenDark() {
    Assert.assertEquals(0,
        inkedPixels(new Rectangle2D.Double(10, 10, 400, 200), Display.NONE, "DISP1", VALUE));
  }

  @Test
  public void aPartWithNoVariantPrintsNothingForItsValue() {
    Assert.assertEquals(0,
        inkedPixels(new Rectangle2D.Double(10, 10, 400, 200), Display.VALUE, "DISP1", ""));
  }

  @Test
  public void eachSettingPrintsSomethingDifferent() {
    Rectangle2D area = new Rectangle2D.Double(10, 10, 400, 200);
    int name = inkedPixels(area, Display.NAME, "DISP1", VALUE);
    int value = inkedPixels(area, Display.VALUE, "DISP1", VALUE);
    int both = inkedPixels(area, Display.BOTH, "DISP1", VALUE);

    Assert.assertTrue(name > 0);
    Assert.assertTrue(value > name);
    Assert.assertTrue(both > value);
  }
}
