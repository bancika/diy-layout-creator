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

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.GridType;
import org.diylc.common.ObjectCache;
import org.diylc.core.Project;
import org.diylc.core.Theme;

/**
 * Draws the canvas grid lines and dots directly, limited to the visible part of the canvas.
 */
public class GridRenderer {

  // closest that grid lines are drawn together, in canvas pixels at the current zoom, before a
  // dense grid is thinned out
  public static double MIN_GRID_LINE_SPACING = 3;

  /**
   * Draws the grid in zoomed canvas coordinates, before the zoom transform is applied.
   *
   * @param g2d
   * @param project
   * @param theme
   * @param zoom
   * @param extraSpace extra space around the project, already multiplied by the zoom
   * @param canvasDimension canvas size including the extra space
   * @param innerCanvasDimension canvas size without the extra space
   * @param visibleRect part of the canvas to draw, or null to draw all of it as exports and
   *        printing do
   */
  public static void draw(Graphics2D g2d, Project project, Theme theme, double zoom,
      double extraSpace, Dimension canvasDimension, Dimension innerCanvasDimension,
      Rectangle2D visibleRect) {
    GridType gridType = GridType.LINES;
    if (gridType == GridType.NONE) {
      return;
    }

    Rectangle2D area =
        new Rectangle2D.Double(0, 0, canvasDimension.width, canvasDimension.height);
    if (visibleRect != null) {
      Rectangle2D.intersect(area, visibleRect, area);
    }

    double zoomStep = project.getGridSpacing().convertToPixels() * zoom;
    // a dense grid drawn zoomed out is reduced to every 5th line, every 25th and so on, once its
    // lines are too close together to be told apart
    int lineMultiple = 1;
    while (zoomStep * lineMultiple < MIN_GRID_LINE_SPACING) {
      lineMultiple *= 5;
    }
    double lineStep = zoomStep * lineMultiple;
    float gridThickness = (float) (1f * (zoom > 1 ? 1 : zoom));

    // grid lines are axis-aligned, so without antialiasing each one is snapped to whole pixels
    // instead of being smeared across two
    Object antialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    try {
      if (gridType == GridType.CROSSHAIR) {
        g2d.setStroke(new BasicStroke(gridThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10f, new float[] {(float) zoomStep / 2, (float) zoomStep / 2}, (float) zoomStep / 4));
      } else if (gridType == GridType.DOT) {
        g2d.setStroke(new BasicStroke(gridThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            10f, new float[] {1f, (float) zoomStep - 1}, 0f));
      } else {
        g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(gridThickness));
      }

      g2d.setColor(theme.getGridColor());
      Line2D line = new Line2D.Double();
      double maxX = Math.min(area.getMaxX(), canvasDimension.width - 1);
      double maxY = Math.min(area.getMaxY(), canvasDimension.height - 1);
      // positions are computed from the line index rather than accumulated, so they do not drift
      for (long k = Math.max(1, (long) Math.ceil(area.getMinX() / lineStep));
          k * lineStep <= maxX; k++) {
        line.setLine(k * lineStep, area.getMinY(), k * lineStep, maxY);
        g2d.draw(line);
      }
      for (long k = Math.max(1, (long) Math.ceil(area.getMinY() / lineStep));
          k * lineStep <= maxY; k++) {
        line.setLine(area.getMinX(), k * lineStep, maxX, k * lineStep);
        g2d.draw(line);
      }

      if (project.getDotSpacing() > 1 && zoomStep > 8) {
        g2d.setColor(theme.getDotColor());
        double dotStep = zoomStep * project.getDotSpacing();
        // dots are 3 pixels wide, so the ones just outside the visible rect still show partly
        double dotMinX = area.getMinX() - extraSpace - 2;
        double dotMinY = area.getMinY() - extraSpace - 2;
        double dotMaxX = Math.min(area.getMaxX() - extraSpace + 2, innerCanvasDimension.width);
        double dotMaxY = Math.min(area.getMaxY() - extraSpace + 2, innerCanvasDimension.height);
        for (long i = Math.max(1, (long) Math.ceil(dotMinX / dotStep)); i * dotStep < dotMaxX;
            i++) {
          for (long j = Math.max(1, (long) Math.ceil(dotMinY / dotStep)); j * dotStep < dotMaxY;
              j++) {
            g2d.fillOval((int) Math.round(extraSpace + i * dotStep - 1),
                (int) Math.round(extraSpace + j * dotStep - 1), 3, 3);
          }
        }
      }
    } finally {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);
    }
  }
}
