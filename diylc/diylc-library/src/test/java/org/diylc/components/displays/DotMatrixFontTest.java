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

import org.junit.Assert;
import org.junit.Test;

/**
 * The glyph table is 475 hand-entered figures, and a drawing that renders a character wrong looks
 * like a rendering bug rather than a bad byte, so the table is checked here instead.
 */
public class DotMatrixFontTest {

  // where a glyph without a descender sits; the row below it is the descender and cursor row
  private static final int BASELINE_ROW = DotMatrixFont.GLYPH_HEIGHT - 2;

  private static boolean[][] glyphOf(char c) {
    boolean[][] dots = new boolean[DotMatrixFont.GLYPH_HEIGHT][DotMatrixFont.GLYPH_WIDTH];
    for (int row = 0; row < DotMatrixFont.GLYPH_HEIGHT; row++) {
      for (int column = 0; column < DotMatrixFont.GLYPH_WIDTH; column++) {
        dots[row][column] = DotMatrixFont.isDotLit(c, row, column);
      }
    }
    return dots;
  }

  private static boolean isBlank(char c) {
    for (boolean[] row : glyphOf(c)) {
      for (boolean lit : row) {
        if (lit) {
          return false;
        }
      }
    }
    return true;
  }

  /** A hole in the table would read as a blank cell on the glass, which is what this catches. */
  @Test
  public void everyPrintableCharacterHasAGlyph() {
    for (char c = '!'; c <= '~'; c++) {
      Assert.assertFalse("'" + c + "' has no glyph", isBlank(c));
    }
    Assert.assertTrue("a space must light nothing", isBlank(' '));
  }

  /**
   * Spot-check against a glyph whose shape is unambiguous: a capital H is two uprights from the top
   * row down to the baseline, joined by a bar across the middle. A table shifted by one entry fails
   * here.
   */
  @Test
  public void theGlyphsAreTheRightWayUpAndRound() {
    boolean[][] h = glyphOf('H');
    for (int row = 0; row <= BASELINE_ROW; row++) {
      Assert.assertTrue("left upright, row " + row, h[row][0]);
      Assert.assertTrue("right upright, row " + row, h[row][4]);
      // the bar joins them across the middle row only
      Assert.assertEquals("middle column, row " + row, row == 3, h[row][2]);
    }
  }

  /**
   * The eighth row belongs to the descenders and to the cursor. A capital letter or a digit that
   * reached into it would collide with the cursor on a real module, and one of the five lower-case
   * descenders that did not reach it would be the squashed seven-row font this replaced.
   */
  @Test
  public void onlyTheDescendersReachTheBottomRow() {
    String descenders = "gjpqy,;_";
    for (char c = ' '; c <= '~'; c++) {
      boolean reaches = false;
      for (int column = 0; column < DotMatrixFont.GLYPH_WIDTH; column++) {
        reaches |= DotMatrixFont.isDotLit(c, BASELINE_ROW + 1, column);
      }
      Assert.assertEquals("'" + c + "' in the bottom row", descenders.indexOf(c) >= 0, reaches);
    }
  }

  @Test
  public void nothingIsLitOutsideTheGlyph() {
    for (char c : new char[] {' ', 'A', 'g', '0', '~'}) {
      Assert.assertFalse("row past the cell",
          DotMatrixFont.isDotLit(c, DotMatrixFont.GLYPH_HEIGHT, 0));
      Assert.assertFalse("row above", DotMatrixFont.isDotLit(c, -1, 0));
      Assert.assertFalse("column past the glyph",
          DotMatrixFont.isDotLit(c, 0, DotMatrixFont.GLYPH_WIDTH));
      Assert.assertFalse("column before the glyph", DotMatrixFont.isDotLit(c, 0, -1));
    }
  }

  /** Anything the table has no glyph for still has to print something. */
  @Test
  public void charactersOutsideTheTableFallBack() {
    for (char c : new char[] {'\u00e9', '\u0416', (char) 0x1F, (char) 0x7F}) {
      Assert.assertArrayEquals("'" + c + "' should print as a question mark", glyphOf('?'),
          glyphOf(c));
    }
  }
}
