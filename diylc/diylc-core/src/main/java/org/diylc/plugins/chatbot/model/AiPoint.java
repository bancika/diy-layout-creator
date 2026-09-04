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
