package org.diylc.core.gerber;

import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.SizeUnit;
import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.Point;
import com.bancika.gerberwriter.path.MoveTo;
import com.bancika.gerberwriter.path.Path;

public class GerberUtils {

  public static void drawComponent(Project currentProject, GerberG2DWrapper g2d, IDIYComponent<?> c,
      boolean outlineMode) {
    IGerberComponent gerberComponent = (IGerberComponent) c;
    g2d.startedDrawingComponent();
    if (gerberComponent instanceof IGerberComponentCustom) {
      IGerberComponentCustom gerberComponentCustom = (IGerberComponentCustom) gerberComponent;
      gerberComponentCustom.draw(g2d, ComponentState.NORMAL, outlineMode, currentProject, g2d, g2d);
    } else if (gerberComponent instanceof IGerberComponentSimple) {
      IGerberComponentSimple gerberComponentSimple = (IGerberComponentSimple) gerberComponent;
      g2d.startGerberOutput(gerberComponentSimple.getGerberLayer(),
          gerberComponentSimple.getGerberFunction(), gerberComponentSimple.isGerberNegative());
      c.draw(g2d, ComponentState.NORMAL, outlineMode, currentProject, g2d);
      g2d.stopGerberOutput();
    }
  }

  public static void outputPathArea(PathIterator pathIterator, DataLayer dataLayer, double d,
      boolean isNegative, String function) {
    outputPath(pathIterator, d, isNegative,
        (path, negative) -> dataLayer.addRegion(path, function, negative));
  }

  public static void outputPathOutline(PathIterator pathIterator, DataLayer dataLayer, double d,
      boolean isNegative, String function, double width) {
    outputPath(pathIterator, d, isNegative, (path, negative) -> dataLayer.addTracesPath(path,
        width * SizeUnit.px.getFactor(), function, negative));
  }

  private static void outputPath(PathIterator pathIterator, double d, boolean isNegative,
      IGerberOutput outputAction) {
    double x = 0;
    double y = 0;
    Path path = null;
    Path2D lastPath = null;
    Area lastArea = null;
    boolean currentIsNegative = isNegative;
    while (!pathIterator.isDone()) {
      double[] coords = new double[6];
      int operation = pathIterator.currentSegment(coords);
      switch (operation) {
        case PathIterator.SEG_MOVETO:
          path = new Path();
          lastPath = new Path2D.Double();
          lastPath.moveTo(coords[0], coords[1]);
          path.moveTo(
              new Point(coords[0] * SizeUnit.px.getFactor(), coords[1] * SizeUnit.px.getFactor()));
          x = coords[0];
          y = coords[1];
          break;
        case PathIterator.SEG_LINETO:
          lastPath.lineTo(coords[0], coords[1]);
          path.lineTo(
              new Point(coords[0] * SizeUnit.px.getFactor(), coords[1] * SizeUnit.px.getFactor()));
          x = coords[0];
          y = coords[1];
          break;
        case PathIterator.SEG_CLOSE:
          // safety check, do it better
          if (!path.isContour()) {
            path.lineTo(((MoveTo) path.getOperators().get(0)).getTo());
          }
          lastPath.closePath();
          if (lastArea == null) {
            outputAction.write(path, currentIsNegative);
          } else {
            lastArea.intersect(new Area(lastPath));
            if (lastArea.isEmpty()) {
              currentIsNegative = isNegative;
            } else {
              currentIsNegative = !currentIsNegative;
            }
            outputAction.write(path, currentIsNegative);
          }
          lastArea = new Area(lastPath);
          path = null;
          break;
        case PathIterator.SEG_CUBICTO:
          lastPath.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          CubicCurve2D curve1 = new CubicCurve2D.Double(x, y, coords[0], coords[1], coords[2],
              coords[3], coords[4], coords[5]);
          subdivide(curve1, path, d);
          x = coords[4];
          y = coords[5];
          break;
        case PathIterator.SEG_QUADTO:
          lastPath.curveTo(coords[0], coords[1], (coords[0] + 2 * coords[2]) / 3,
              (coords[3] + 2 * coords[1]) / 3, coords[2], coords[3]);
          QuadCurve2D curve2 =
              new QuadCurve2D.Double(x, y, coords[0], coords[1], coords[2], coords[3]);
          subdivide(curve2, path, d);
          x = coords[2];
          y = coords[3];
          break;
      }
      pathIterator.next();
    }
    // check any leftover open path
    if (path != null) {
      if (lastArea == null) {
        outputAction.write(path, currentIsNegative);
      } else {
        lastArea.intersect(new Area(lastPath));
        if (lastArea.isEmpty()) {
          currentIsNegative = isNegative;
        } else {
          currentIsNegative = !currentIsNegative;
        }
        outputAction.write(path, currentIsNegative);
      }
    }
  }

  private static void subdivide(CubicCurve2D curve, Path path, double d) {
    if (curve.getFlatness() < d / 3 || new Point2D.Double(curve.getX1(), curve.getY1())
        .distance(curve.getX2(), curve.getY2()) < d) {

      path.lineTo(new Point(curve.getX2() * SizeUnit.px.getFactor(),
          curve.getY2() * SizeUnit.px.getFactor()));
      return;
    }
    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);
    subdivide(left, path, d);
    subdivide(right, path, d);
  }

  private static void subdivide(QuadCurve2D curve, Path path, double d) {
    if (curve.getFlatness() < d / 3 || new Point2D.Double(curve.getX1(), curve.getY1())
        .distance(curve.getX2(), curve.getY2()) < d) {

      path.lineTo(new Point(curve.getX2() * SizeUnit.px.getFactor(),
          curve.getY2() * SizeUnit.px.getFactor()));
      return;
    }
    QuadCurve2D left = new QuadCurve2D.Double();
    QuadCurve2D right = new QuadCurve2D.Double();
    curve.subdivide(left, right);
    subdivide(left, path, d);
    subdivide(right, path, d);
  }
}
