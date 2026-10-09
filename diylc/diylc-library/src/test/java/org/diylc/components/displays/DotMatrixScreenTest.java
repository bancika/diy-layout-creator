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
package org.diylc.components.displays;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.diylc.common.Display;
import org.junit.Assert;
import org.junit.Test;

/**
 * Folding the label into the character grid, which the character LCD and both graphic panels now
 * share. A panel shows what fits and nothing else, so the rules about what gets dropped matter.
 */
public class DotMatrixScreenTest {

  private static List<String> lines(String name, String value, int columns, int rows) {
    return DotMatrixScreen.layOutText(Display.BOTH, name, value, columns, rows);
  }

  @Test
  public void textThatFitsIsLeftAlone() {
    Assert.assertEquals(List.of("LCD1", "16x2", "Parallel HD44780"),
        lines("LCD1", "16x2, Parallel HD44780", 16, 4));
  }

  /** The value arrives as one string per property, and each starts its own row. */
  @Test
  public void everyPropertyStartsItsOwnRow() {
    Assert.assertEquals(List.of("0.96\" SSD1306", "128x64", "I2C"),
        DotMatrixScreen.layOutText(Display.VALUE, "OLED1", "0.96\" SSD1306, 128x64, I2C", 21, 8));
  }

  @Test
  public void aLongLineFoldsAtASpace() {
    Assert.assertEquals(List.of("LCD1", "Parallel", "HD44780"),
        lines("LCD1", "Parallel HD44780", 8, 4));
  }

  /** A word with no space in it has to be cut, or it would run off the glass. */
  @Test
  public void aWordTooWideIsCut() {
    Assert.assertEquals(List.of("ABCDE", "FGHIJ", "KL"),
        DotMatrixScreen.layOutText(Display.NAME, "ABCDEFGHIJKL", null, 5, 4));
  }

  /** What runs past the last row is dropped rather than shrunk or scrolled. */
  @Test
  public void whatRunsPastTheLastRowIsDropped() {
    Assert.assertEquals(List.of("LCD1", "16x2"), lines("LCD1", "16x2, Parallel HD44780", 16, 2));
  }

  /**
   * Renders onto a one-pixel-per-pixel grid and reports the margins the text leaves either side,
   * which is the only way to see where it actually landed.
   */
  private static int[] margins(List<String> lines, int pixelsX, int pixelsY) {
    BufferedImage image = new BufferedImage(pixelsX, pixelsY, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(Color.WHITE);
    DotMatrixScreen.drawText(g2d, lines, pixelsX, pixelsY, 0, 0, 1, 1);
    g2d.dispose();

    int first = -1;
    int last = -1;
    for (int x = 0; x < pixelsX; x++) {
      for (int y = 0; y < pixelsY; y++) {
        if ((image.getRGB(x, y) & 0xFFFFFF) != 0) {
          first = first == -1 ? x : first;
          last = x;
          break;
        }
      }
    }
    Assert.assertNotEquals("nothing was drawn", -1, first);
    return new int[] {first, pixelsX - 1 - last};
  }

  /**
   * A line that nearly fills the panel used to be pinned against the left edge: centring rounded to
   * whole cells, and a cell is a glyph plus a blank column, so the slack all ended up on the right.
   * The 0.96" OLED's own label is exactly that case, 20 characters on a 21-column panel.
   */
  @Test
  public void aNearlyFullLineIsStillCentred() {
    int[] m = margins(List.of("0.96\" SSD1306 128x64"), 128, 64);
    Assert.assertTrue("left " + m[0] + " vs right " + m[1], Math.abs(m[0] - m[1]) <= 1);
    Assert.assertTrue("should not touch the edge, left margin was " + m[0], m[0] > 0);
  }

  /** The Nokia's label is 13 characters on a 14-column panel, the same case one column narrower. */
  @Test
  public void theNokiaLabelIsCentred() {
    int[] m = margins(List.of("84x48 PCD8544"), 84, 48);
    Assert.assertTrue("left " + m[0] + " vs right " + m[1], Math.abs(m[0] - m[1]) <= 1);
    Assert.assertTrue("should not touch the edge, left margin was " + m[0], m[0] > 0);
  }

  /** A short line has whole cells to spare, and must come out centred just the same. */
  @Test
  public void aShortLineIsCentred() {
    int[] m = margins(List.of("I2C"), 128, 64);
    Assert.assertTrue("left " + m[0] + " vs right " + m[1], Math.abs(m[0] - m[1]) <= 1);
  }

  @Test
  public void aScreenTurnedOffPrintsNothing() {
    Assert.assertTrue(
        DotMatrixScreen.layOutText(Display.NONE, "LCD1", "16x2", 16, 2).isEmpty());
  }

  /** A grid with no room at all must not throw its way out of a repaint. */
  @Test
  public void anEmptyGridPrintsNothing() {
    Assert.assertTrue(lines("LCD1", "16x2", 0, 2).isEmpty());
    Assert.assertTrue(lines("LCD1", "16x2", 16, 0).isEmpty());
  }
}
