package org.diylc.core.gerber;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.measures.SizeUnit;
import org.diylc.lang.LangUtil;
import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GenerationSoftware;
import com.bancika.gerberwriter.Point;
import com.bancika.gerberwriter.path.MoveTo;
import com.bancika.gerberwriter.path.Path;

public class GerberExporter {

  public static final double OUTLINE_THICKNESS = 0.3;
  private static final Logger LOG = Logger.getLogger(GerberExporter.class);

  public static void exportGerber(String fileNameBase, Project currentProject, IView view,
      Graphics2D graphics2d, String diylcVersion) {
    List<IGerberBoard> gerberBoards =
        currentProject.getComponents().stream().filter(c -> c instanceof IGerberBoard)
            .map(x -> (IGerberBoard) x).collect(Collectors.toList());

    Map<IGerberBoard, List<IDIYComponent<?>>> boardComponentMap =
        new HashMap<IGerberBoard, List<IDIYComponent<?>>>();

    // DefaultGerberBoard defaultGerberBoard = new DefaultGerberBoard();

    List<IDIYComponent<?>> componentsWithoutBoard = new ArrayList<IDIYComponent<?>>();

    currentProject.getComponents().stream().filter(c -> c instanceof IGerberComponent)
        .forEach(c -> {
          Point2D firstPoint = c.getControlPoint(0);
          IGerberBoard board = gerberBoards.stream()
              .filter(b -> b.getBoardRectangle().contains(firstPoint)).findFirst().orElse(null);
          if (board == null) {
            componentsWithoutBoard.add(c);
          } else {
            boardComponentMap.computeIfAbsent(board, k -> new ArrayList<IDIYComponent<?>>()).add(c);
          }
        });

    // defaultGerberBoard.setComponents(boardComponentMap.get(defaultGerberBoard));

    if (boardComponentMap.isEmpty()) {
      view.showMessage(LangUtil.translate("Nothing to export."),
          LangUtil.translate("Gerber Export"), IView.WARNING_MESSAGE);
      return;
    }

    if (componentsWithoutBoard.size() > 0) {
      if (view.showConfirmDialog(String.format(
          LangUtil.translate("There are some components that are outside of the bounds of boards.%s"
              + "They will be ignored unless you add a board component underneath them.%sDo you want to continue?"),
          "\n", "\n"), LangUtil.translate("Gerber Export"), IView.YES_NO_OPTION,
          IView.WARNING_MESSAGE) != IView.YES_OPTION) {
        return;
      }
    }

    GenerationSoftware genSoftware =
        new GenerationSoftware("bancika", "DIY Layout Creator", diylcVersion);

    List<String> generatedFiles = new ArrayList<String>();

    gerberBoards.forEach(b -> {
      IDIYComponent<?> boardComponent = (IDIYComponent<?>) b;

      DataLayer outlineLayer = new DataLayer("Profile,NP", false, genSoftware);
      final PathIterator pathIterator = b.getBoardRectangle().getPathIterator(null);
      outputPathOutline(pathIterator, outlineLayer, 1d, false, "Profile", OUTLINE_THICKNESS);
      // Path path = buildOutlinePath(b);
      // outlineLayer.addTracesPath(path, OUTLINE_THICKNESS, "Profile", false);
      String fileNameOutline = fileNameBase + (fileNameBase.endsWith(".") ? "" : ".")
          + boardComponent.getName() + ".gko";
      try {
        LOG.info(String.format(LangUtil.translate("Exporting outline for board %s to file %s"),
            boardComponent.getName(), fileNameOutline));
        outlineLayer.dumpGerberToFile(fileNameOutline);
        generatedFiles.add(fileNameOutline);
      } catch (IOException e) {
        LOG.error("Error writing gerber file: " + e.getMessage());
        view.showMessage(
            LangUtil.translate(
                "Failed to export the project to gerber. Please check the log for details"),
            LangUtil.translate("Gerber Export"), IView.ERROR_MESSAGE);
        return;
      }

      GerberG2DWrapper g2d = new GerberG2DWrapper(graphics2d, diylcVersion);

      boardComponentMap.get(b).forEach(c -> {
        IGerberComponent gerberComponent = (IGerberComponent) c;
        Set<GerberRenderMode> supportedRenderModes = gerberComponent.getGerberRenderModes();
        if (supportedRenderModes.contains(GerberRenderMode.Normal)) {
          drawComponent(currentProject, g2d, c, false);
        }
        if (supportedRenderModes.contains(GerberRenderMode.Outline)) {
          drawComponent(currentProject, g2d, c, true);
        }
      });

      g2d.getLayerMap().entrySet().forEach(entry -> {
        org.diylc.core.gerber.GerberLayer layer = entry.getKey();
        DataLayer dataLayer = entry.getValue();
        try {
          String fileName = fileNameBase + (fileNameBase.endsWith(".") ? "" : ".")
              + boardComponent.getName() + "." + layer.getExtension();
          LOG.info("Exporting layer: " + layer + " for board " + boardComponent.getName()
              + " to file: " + fileName);
          dataLayer.dumpGerberToFile(fileName);
          generatedFiles.add(fileName);
        } catch (IOException e) {
          LOG.error("Error writing gerber file: " + e.getMessage());
          view.showMessage(
              LangUtil.translate(
                  "Failed to export the project to gerber. Please check the log for details"),
              LangUtil.translate("Gerber Export"), IView.ERROR_MESSAGE);
          return;
        }
      });
    });
    view.showMessage(
        String.format(
            LangUtil.translate(
                "Gerber export completed successfully. The following files were created:%s"),
            "\n\n" + String.join("\n", generatedFiles)),
        LangUtil.translate("Gerber Export"), IView.INFORMATION_MESSAGE);
    LOG.info("Completed export to gerber");
  }

  private static void drawComponent(Project currentProject, GerberG2DWrapper g2d,
      IDIYComponent<?> c, boolean outlineMode) {
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
          path.moveTo(new Point(-coords[0] * SizeUnit.px.getFactor(),
              -coords[1] * SizeUnit.px.getFactor()));
          x = coords[0];
          y = coords[1];
          break;
        case PathIterator.SEG_LINETO:
          lastPath.lineTo(coords[0], coords[1]);
          path.lineTo(new Point(-coords[0] * SizeUnit.px.getFactor(),
              -coords[1] * SizeUnit.px.getFactor()));
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
    if (/* curve.getFlatness() < d || */new Point2D.Double(curve.getX1(), curve.getY1())
        .distance(curve.getX2(), curve.getY2()) < d) {
      // path.lineTo(new Point(curve.getX1() * SizeUnit.px.getFactor(), curve.getY1() *
      // SizeUnit.px.getFactor()));
      path.lineTo(new Point(-curve.getX2() * SizeUnit.px.getFactor(),
          -curve.getY2() * SizeUnit.px.getFactor()));
      return;
    }
    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);
    subdivide(left, path, d);
    subdivide(right, path, d);
  }

  private static void subdivide(QuadCurve2D curve, Path path, double d) {
    if (/* curve.getFlatness() < d || */new Point2D.Double(curve.getX1(), curve.getY1())
        .distance(curve.getX2(), curve.getY2()) < d) {
      // path.lineTo(new Point(curve.getX1() * SizeUnit.px.getFactor(), curve.getY1() *
      // SizeUnit.px.getFactor()));
      path.lineTo(new Point(-curve.getX2() * SizeUnit.px.getFactor(),
          -curve.getY2() * SizeUnit.px.getFactor()));
      return;
    }
    QuadCurve2D left = new QuadCurve2D.Double();
    QuadCurve2D right = new QuadCurve2D.Double();
    curve.subdivide(left, right);
    subdivide(left, path, d);
    subdivide(right, path, d);
  }

  // private static Path buildOutlinePath(IGerberBoard b) {
  // Path path = new Path();
  // PathIterator pathIterator = b.getBoardRectangle().getPathIterator(
  // AffineTransform.getScaleInstance(-SizeUnit.px.getFactor(), -SizeUnit.px.getFactor()));
  // while (!pathIterator.isDone()) {
  // double[] coords = new double[6];
  // int operation = pathIterator.currentSegment(coords);
  // switch (operation) {
  // case PathIterator.SEG_MOVETO:
  // path.moveTo(new com.bancika.gerberwriter.Point(coords[0], coords[1]));
  // break;
  // case PathIterator.SEG_LINETO:
  // path.lineTo(new com.bancika.gerberwriter.Point(coords[0], coords[1]));
  // break;
  // }
  // pathIterator.next();
  // }
  // return path;
  // }
}
