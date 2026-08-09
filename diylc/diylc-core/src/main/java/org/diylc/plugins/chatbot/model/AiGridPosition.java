package org.diylc.plugins.chatbot.model;

import java.awt.geom.Point2D;

import org.diylc.core.measures.Size;

/**
 * Represents a position in grid coordinates (not pixels).
 * Grid coordinates are multiplied by the project's grid spacing to get pixel positions.
 * <p>
 * For example, gridX=5, gridY=10 with a 0.1" grid means 0.5" right, 1.0" down from origin.
 */
public class AiGridPosition {

  private Double gridX;
  private Double gridY;

  public Double getGridX() {
    return gridX;
  }

  public void setGridX(Double gridX) {
    this.gridX = gridX;
  }

  public Double getGridY() {
    return gridY;
  }

  public void setGridY(Double gridY) {
    this.gridY = gridY;
  }

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
        Math.round(gridX * pxPerUnit),
        Math.round(gridY * pxPerUnit)
    );
  }
}
