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
package org.diylc.plugins.chatbot.model;

import java.math.BigDecimal;
import java.awt.geom.Point2D;
import org.diylc.core.measures.Size;

public record AiPoint(BigDecimal gridX, BigDecimal gridY) {

  /**
   * Converts the grid coordinates to a Point2D containing pixel coordinates, rounded to
   * the nearest pixel to ensure components snap to the grid.
   *
   * @param gridSpacing the current project's grid spacing
   * @return the pixel position, rounded
   */
  public Point2D toPixels(Size gridSpacing) {
    if (gridX == null || gridY == null) {
      return new Point2D.Double(0, 0);
    }
    double pxPerUnit = gridSpacing.convertToPixels();
    return new Point2D.Double(
        Math.round(gridX.doubleValue() * pxPerUnit),
        Math.round(gridY.doubleValue() * pxPerUnit)
    );
  }
}
