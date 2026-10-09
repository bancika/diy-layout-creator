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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.diylc.common.Display;
import org.diylc.components.MakerBoardPainter;

/**
 * Text as a dot-matrix screen actually shows it: folded into the character grid the panel has room
 * for, then painted one lit pixel at a time out of {@link DotMatrixFont}.
 *
 * <p>Laying the text out is shared by every such screen; painting it is not. A graphic panel like
 * the Nokia's addresses one unbroken grid of pixels, and {@link #drawText} serves those. A
 * character module instead has gaps between its cells, so it walks its own cells and only borrows
 * the layout.
 *
 * @author Branislav Stojkovic
 */
public class DotMatrixScreen {

  /** A glyph and the blank column that separates it from the next, as the panel fonts are drawn. */
  public static final int CELL_WIDTH = DotMatrixFont.GLYPH_WIDTH + 1;
  /** Rows butt against each other; the glyph's own last row is the gap. */
  public static final int CELL_HEIGHT = DotMatrixFont.GLYPH_HEIGHT;

  private DotMatrixScreen() {}

  /**
   * What the screen spells out, broken into grid rows. A line too wide for the grid is folded at a
   * space where there is one and cut where there is not, and whatever runs past the last row is
   * dropped: a real panel shows what fits and nothing more.
   */
  public static List<String> layOutText(Display display, String name, String value, int columns,
      int rows) {
    String text = MakerBoardPainter.screenText(display, name, value);
    if (text == null || columns <= 0 || rows <= 0) {
      return Collections.emptyList();
    }

    List<String> lines = new ArrayList<String>();
    for (String paragraph : text.split("\n")) {
      String remaining = paragraph.trim();
      while (!remaining.isEmpty() && lines.size() < rows) {
        if (remaining.length() <= columns) {
          lines.add(remaining);
          break;
        }
        int fold = remaining.lastIndexOf(' ', columns);
        if (fold <= 0) {
          fold = columns;
        }
        lines.add(remaining.substring(0, fold).trim());
        remaining = remaining.substring(fold).trim();
      }
    }
    return lines;
  }

  /**
   * Paints laid-out text onto an unbroken pixel grid, centred in it. The caller has already set the
   * ink colour and placed the grid; this only decides which pixels light.
   *
   * <p>Centring is worked out in pixels rather than whole cells. A panel addresses single pixels,
   * and a line that nearly fills the grid has no whole cell to spare, so rounding to one would pin
   * it against the left edge and leave the slack -- the trailing cell's blank column, plus whatever
   * the cell width does not divide into the panel -- piled up on the right.
   *
   * @param lines text from {@link #layOutText}
   * @param pixelsX pixels the panel is wide
   * @param pixelsY pixels the panel is tall
   * @param x left edge of the pixel grid
   * @param y top edge of the pixel grid
   * @param pixelWidth width of one display pixel
   * @param pixelHeight height of one display pixel, which need not match its width
   */
  public static void drawText(Graphics2D g2d, List<String> lines, int pixelsX, int pixelsY,
      double x, double y, double pixelWidth, double pixelHeight) {
    if (lines.isEmpty()) {
      return;
    }

    // a panel runs to thousands of pixels, so the rectangle is reused rather than allocated per dot
    Rectangle2D.Double pixel = new Rectangle2D.Double(0, 0, pixelWidth, pixelHeight);

    // the offsets land on whole pixels because that is all the controller can address
    double firstRow = Math.floor((pixelsY - lines.size() * CELL_HEIGHT) / 2.0);
    for (int line = 0; line < lines.size(); line++) {
      String text = lines.get(line);
      // the spacer column trailing the last glyph is not part of the text, so it is not centred
      // with it
      int lineWidth = text.length() * CELL_WIDTH - (CELL_WIDTH - DotMatrixFont.GLYPH_WIDTH);
      double firstColumn = Math.floor((pixelsX - lineWidth) / 2.0);
      double cellY = y + (firstRow + line * CELL_HEIGHT) * pixelHeight;
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        double cellX = x + (firstColumn + i * CELL_WIDTH) * pixelWidth;
        for (int dotRow = 0; dotRow < DotMatrixFont.GLYPH_HEIGHT; dotRow++) {
          for (int dotColumn = 0; dotColumn < DotMatrixFont.GLYPH_WIDTH; dotColumn++) {
            if (DotMatrixFont.isDotLit(c, dotRow, dotColumn)) {
              pixel.x = cellX + dotColumn * pixelWidth;
              pixel.y = cellY + dotRow * pixelHeight;
              g2d.fill(pixel);
            }
          }
        }
      }
    }
  }
}
