package org.diylc.core.gerber;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.diylc.core.GerberLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.measures.SizeUnit;
import org.diylc.lang.LangUtil;
import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GenerationSoftware;
import com.bancika.gerberwriter.path.Path;

public class GerberExporter {

  public static final double OUTLINE_THICKNESS = 0.3;
  private static final Logger LOG = Logger.getLogger(GerberExporter.class);

  public static void exportGerber(String fileNameBase, Project currentProject, IView view,
      String diylcVersion) {
    List<IGerberBoard> gerberBoards =
        currentProject.getComponents().stream().filter(c -> c instanceof IGerberBoard)
            .map(x -> (IGerberBoard) x).collect(Collectors.toList());

    Map<IGerberBoard, List<IGerberComponent>> boardComponentMap =
        new HashMap<IGerberBoard, List<IGerberComponent>>();

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
            boardComponentMap.computeIfAbsent(board, k -> new ArrayList<IGerberComponent>()).add((IGerberComponent) c);
          }
        });

    // defaultGerberBoard.setComponents(boardComponentMap.get(defaultGerberBoard));
    
    Map<GerberLayer, Set<IGerberComponent>> layerComponentMap =
        new HashMap<GerberLayer, Set<IGerberComponent>>();

        currentProject.getComponents().stream().filter(c -> c instanceof IGerberComponent)
            .forEach(c -> { IGerberComponent gerberComponent = (IGerberComponent) c;
            List<GerberLayer> gerberLayers = gerberComponent.getGerberLayers();
            gerberLayers.forEach(l -> layerComponentMap
                .computeIfAbsent(l, k -> new HashSet<IGerberComponent>()).add(gerberComponent));});
            
    if (layerComponentMap.isEmpty() || boardComponentMap.isEmpty()) {
      view.showMessage(LangUtil.translate("Nothing to export."),
          LangUtil.translate("Gerber Export"), IView.WARNING_MESSAGE);
      return;
    }

    if (componentsWithoutBoard.size() > 0) {
      if (view.showConfirmDialog(String.format(LangUtil.translate(
          "There are some components that are outside of the bounds of boards.%sThey will be ignored. Do you want to continue?"),
          "\n"), LangUtil.translate("Gerber Export"), IView.YES_NO_OPTION,
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
      Path path = buildOutlinePath(b);
      outlineLayer.addTracesPath(path, OUTLINE_THICKNESS, "Profile", false);
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

      layerComponentMap.entrySet().forEach(entry -> {
        GerberLayer layer = entry.getKey();
        Set<IGerberComponent> layerComponents = entry.getValue();
        DataLayer dataLayer = new DataLayer(layer.getFunction(), false, genSoftware);
        String fileName = fileNameBase + (fileNameBase.endsWith(".") ? "" : ".")
            + boardComponent.getName() + "." + layer.getExtension();
        boardComponentMap.get(b)
            .forEach(c -> {
              IGerberComponent gerberComponent = (IGerberComponent) c;
              if (layerComponents.contains(c)) {
                gerberComponent.drawToGerber(dataLayer);
              }
            });
        try {
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

  private static Path buildOutlinePath(IGerberBoard b) {
    Path path = new Path();
    PathIterator pathIterator = b.getBoardRectangle().getPathIterator(
        AffineTransform.getScaleInstance(-SizeUnit.px.getFactor(), -SizeUnit.px.getFactor()));
    while (!pathIterator.isDone()) {
      double[] coords = new double[6];
      int operation = pathIterator.currentSegment(coords);
      switch (operation) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(new com.bancika.gerberwriter.Point(coords[0], coords[1]));
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(new com.bancika.gerberwriter.Point(coords[0], coords[1]));
          break;
      }
      pathIterator.next();
    }
    return path;
  }
}
