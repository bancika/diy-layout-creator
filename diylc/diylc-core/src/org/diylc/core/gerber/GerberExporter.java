package org.diylc.core.gerber;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.lang.LangUtil;
import com.bancika.gerberwriter.DataLayer;

public class GerberExporter {

  public static final double OUTLINE_THICKNESS = new Size(0.3d, SizeUnit.mm).convertToPixels();
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

    List<String> generatedFiles = new ArrayList<String>();

    gerberBoards.forEach(b -> {
      IDIYComponent<?> boardComponent = (IDIYComponent<?>) b;

      GerberLayer gerberLayer = GerberLayer.Outline;
      DataLayer outlineLayer = gerberLayer.buildLayer(diylcVersion);
      Rectangle2D boardRect = b.getBoardRectangle();

      final AffineTransform boardTx = AffineTransform.getScaleInstance(1, -1);
      boardTx.translate(-boardRect.getX(), -boardRect.getMaxY());
      final PathIterator pathIterator = boardRect.getPathIterator(boardTx);
      GerberUtils.outputPathOutline(pathIterator, outlineLayer, 1d, false, "Profile", OUTLINE_THICKNESS);
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

      GerberG2DWrapper g2d = new GerberG2DWrapper(graphics2d, diylcVersion, boardRect);

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
}
