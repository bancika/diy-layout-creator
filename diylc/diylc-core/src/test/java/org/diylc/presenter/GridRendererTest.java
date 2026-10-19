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
package org.diylc.presenter;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;
import org.junit.Test;

public class GridRendererTest {

  @Test
  public void testEveryLineDrawnWhenSpacedApart() {
    // 0.5 mm is 3.94 px at 100%, so 101 lines fit in 400 px
    assertEquals(101, countVerticalLines(1, new Rectangle2D.Double(0, 0, 400, 300)));
  }

  @Test
  public void testDenseGridThinnedToEveryFifthLine() {
    // 0.5 mm is 2.95 px at 75%, below the minimum, so lines are 14.76 px apart
    assertEquals(27, countVerticalLines(0.75, new Rectangle2D.Double(0, 0, 400, 300)));
  }

  @Test
  public void testOnlyVisibleRectDrawn() {
    assertEquals(25, countVerticalLines(1, new Rectangle2D.Double(0, 0, 100, 300)));
  }

  private int countVerticalLines(double zoom, Rectangle2D visibleRect) {
    Project project = new Project();
    project.setGridSpacing(new Size(0.5, SizeUnit.mm));
    BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    GridRenderer.draw(g2d, project, Constants.DEFAULT_THEME, zoom, 0, new Dimension(1000, 1000),
        new Dimension(1000, 1000), visibleRect);
    g2d.dispose();

    // the first horizontal line is further down than this row
    int lines = 0;
    for (int x = 0; x < img.getWidth(); x++) {
      if ((img.getRGB(x, 1) >>> 24) != 0) {
        lines++;
      }
    }
    return lines;
  }
}
