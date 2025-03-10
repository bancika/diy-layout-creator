
package org.diylc.core.gerber;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.Point;
import com.bancika.gerberwriter.path.MoveTo;
import com.bancika.gerberwriter.path.Path;

import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.SizeUnit;

public class GerberPathRenderer {

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
    List<PathWithNegative> outputPath =
        approximatePathWithPolygonPaths(pathIterator, d, isNegative);
    outputPath.forEach(p -> dataLayer.addRegion(p.path, function, p.negative));
  }

  public static void outputPathOutline(PathIterator pathIterator, DataLayer dataLayer, double d,
      boolean isNegative, String function, double width) {
    List<PathWithNegative> outputPath =
        approximatePathWithPolygonPaths(pathIterator, d, isNegative);
    outputPath.forEach(p -> dataLayer.addTracesPath(p.path, width * SizeUnit.px.getFactor(),
        function, p.negative));
  }

  public static List<PathWithNegative> approximatePathWithPolygonPaths(PathIterator pathIterator,
      double d, boolean isNegative) {
    // List<PathWithNegative> convert = PathIteratorConverter.convert(pathIterator, d, isNegative);
    // convert.forEach(c -> outputAction.write(c.path, c.negative));
    // if (true)
    // return;
    List<PathWithNegative> results = new ArrayList<PathWithNegative>();
    double x = 0;
    double y = 0;
    Path path = null;
    Path2D lastPath = null;
    Area lastArea = null;
    while (!pathIterator.isDone()) {
      double[] coords = new double[6];
      int operation = pathIterator.currentSegment(coords);
      // int windingRule = pathIterator.getWindingRule();
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
            results.add(new PathWithNegative(path, lastPath));
          } else {
            lastArea.intersect(new Area(lastPath));
            results.add(new PathWithNegative(path, lastPath));
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
      results.add(new PathWithNegative(path, lastPath));
    }

    results.sort(Comparator.comparing(a -> a.getArea(), Comparator.reverseOrder()));

    assignNegativeFlags(results, isNegative);

    return results;
  }

  public static void assignNegativeFlags(List<PathWithNegative> paths, boolean initialNegative) {
    if (paths == null || paths.isEmpty())
      return;

    // Assume the list is already sorted with larger (outer) paths first.
    // Set the first one to the given initial value.
    paths.get(0).negative = initialNegative;

    // For each subsequent path...
    for (int i = 1; i < paths.size(); i++) {
      boolean foundParent = false;
      // Check larger paths (starting from the closest; i.e. immediately before i)
      for (int j = i - 1; j >= 0; j--) {
        if (paths.get(j).contains(paths.get(i).currentPoint)) {
          // If candidate is inside parent, assign the opposite polarity.
          paths.get(i).negative = !paths.get(j).negative;
          foundParent = true;
          break; // stop after the first containing parent is found
        }
      }
      // If no parent was found, assign the initial value.
      if (!foundParent) {
        paths.get(i).negative = initialNegative;
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

  public static void renderShapeToPNG(Shape shape, String outputPath) throws IOException {
    // 1. Determine the bounding box of the shape.
    Rectangle2D bounds = shape.getBounds2D();

    // Ensure we have a positive width/height.
    // (You may want to add padding or checks if shape is empty or negative sized.)
    int width = (int) Math.ceil(bounds.getWidth());
    int height = (int) Math.ceil(bounds.getHeight());
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Shape has invalid bounds: " + bounds);
    }

    // 2. Create a BufferedImage (RGB) with the required dimensions.
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    // 3. Draw the shape.
    Graphics2D g2d = image.createGraphics();
    try {
      // Enable anti-aliasing if desired
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // White background
      g2d.setColor(Color.WHITE);
      g2d.fillRect(0, 0, width, height);

      // Translate the graphics context so the shape appears at (0,0)
      g2d.translate(-bounds.getX(), -bounds.getY());

      // Draw the shape in black
      g2d.setColor(Color.BLACK);
      g2d.fill(shape);
    } finally {
      g2d.dispose();
    }

    // 4. Write the result to a PNG file
    ImageIO.write(image, "png", new File(outputPath));
  }
  
  private static class PathWithNegative {
    
    Path path;
    boolean negative;
    Area lastPath;
    Point2D currentPoint;

    public PathWithNegative(Path path, Path2D lastPath) {
        this.path = path;
        this.lastPath = new Area(lastPath);
        currentPoint = lastPath.getCurrentPoint();
    }
    
    double getArea() {
      return lastPath.getBounds2D().getWidth() * lastPath.getBounds2D().getHeight();
    }
    
    boolean contains(Point2D point) {
      return lastPath.contains(point);
    }
  }
}
