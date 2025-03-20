package org.diylc.netlist;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.FlatteningPathIterator;

public class AreaSimplifier {

  /**
   * Simplifies the given Area into a polygonal Area.
   *
   * @param area the original Area with curves
   * @param flatness the maximum distance that the flattened path may deviate from the original
   *        curve. Lower values yield higher fidelity.
   * @return a new Area approximated as a polygon
   */
  public static Area simplifyArea(Area area, double flatness) {
    if (area == null) {
      throw new IllegalArgumentException("Area cannot be null.");
    }

    // Get the original path and wrap it with a FlatteningPathIterator.
    PathIterator originalPath = area.getPathIterator(null);
    FlatteningPathIterator fpi = new FlatteningPathIterator(originalPath, flatness);

    // Create a new path that will store the flattened (polygonal) approximation.
    Path2D.Double simplifiedPath = new Path2D.Double();
    double[] coords = new double[6];

    while (!fpi.isDone()) {
      int segType = fpi.currentSegment(coords);
      switch (segType) {
        case PathIterator.SEG_MOVETO:
          simplifiedPath.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          simplifiedPath.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_CLOSE:
          simplifiedPath.closePath();
          break;
        default:
          // Since we use a FlatteningPathIterator, curves are already approximated as lines.
          break;
      }
      fpi.next();
    }
    return new Area(simplifiedPath);
  }
}
