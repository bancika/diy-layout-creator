package org.diylc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.core.IView;
import org.diylc.presenter.Presenter;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;

public class RegressionTestRunner {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("No path and command provided");
      return;
    }

    ConfigurationManager.getInstance().initialize("diylc-test");

    String basePath = args[0];
    String command = args[1];
    String filter = null;
    if (args.length > 2) {
      filter = args[2];
    }

    System.out.println("Running command " + command + " in: " + basePath);
    File inputsDir = new File(basePath + File.separator + "input");
    List<Boolean> collect =
        visit(inputsDir, basePath, command, filter).collect(Collectors.toList());

    long okCount = collect.stream().filter(x -> x).count();
    long failCount = collect.size() - okCount;
    System.out.println("Finished! OK files: " + okCount + "; Failed files: " + failCount);
  }

  private static Stream<Boolean> visit(File inputsDir, String basePath, String command,
      String filter) {
    File[] files = inputsDir.listFiles();
    return Arrays.stream(files)
        // .filter(x -> x.getAbsolutePath().contains("405"))
        .parallel()
        .flatMap(file -> {
          if (file.isDirectory()) {
            return visit(file, basePath, command, filter);
          } else {
            if (!file.getName().endsWith(".diy")) {
              return Stream.empty();
            }
            if (filter != null) {
              Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
              Matcher matcher = pattern.matcher(file.getAbsolutePath());
              boolean matchFound = matcher.find();
              if (!matchFound) {
                return Stream.empty();
              }
            }
            return Stream.of(processFile(file, command));
          }
        });
  }

  private static boolean processFile(File file, String command) {
    String prefix = "File: " + file.getName() + " status: ";
    try {
      IView view = new MockView();
      Presenter presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
      ProjectDrawingProvider drawingProvider =
          new ProjectDrawingProvider(presenter, false, true, false);
      presenter.loadProjectFromFile(file.getAbsolutePath());
      if ("PREPARE".equalsIgnoreCase(command)) {
        // generate PNG input image for comparison
        prepareInputs(file, drawingProvider);
        System.out.println(prefix + "DONE");
        return true;
      } else if ("TEST".equalsIgnoreCase(command)) {
        return testOutputs(file, prefix, presenter, drawingProvider);
      }
      return false;
    } catch (Exception e) {
      System.out.println(prefix + "ERROR: " + e.getMessage());
      return false;
    }    
  }

  private static boolean testOutputs(File file, String prefix, Presenter presenter,
      ProjectDrawingProvider drawingProvider) {
    File baseDir = file.getParentFile().getParentFile().getParentFile().getParentFile();
    File outputDir = new File(baseDir.getAbsolutePath() + File.separator + "output" + File.separator + 
        file.getParentFile().getParentFile().getName());
    File inputDir = file.getParentFile().getParentFile();
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    // test save
    File outputFile = new File(outputDir.getAbsolutePath() + File.separator + "diy" + File.separator + file.getName());
    outputFile.getParentFile().mkdirs();
    if (outputFile.exists()) {
      outputFile.delete();
    }
    presenter.saveProjectToFile(outputFile.getAbsolutePath(), false);

    // test export png
    File pngOutputFile = new File(outputDir.getAbsolutePath() + File.separator + "png" + File.separator + 
        file.getName().replace(".diy", ".png"));
    pngOutputFile.getParentFile().mkdirs();
    if (pngOutputFile.exists()) {
      pngOutputFile.delete();
    }
    DrawingExporter.getInstance().exportPNG(drawingProvider, pngOutputFile);
    File pngInputFile = new File(inputDir.getAbsolutePath() + File.separator + "png" + File.separator + (file.getName().replace(".diy", ".png")));
    if (!pngInputFile.exists()) {
      System.out.println(prefix + "Input image does not exist!");
      return false;
    }

    BufferedImage expectedImage =
        ImageComparisonUtil.readImageFromResources(pngInputFile.getAbsolutePath());
    BufferedImage actualImage =
        ImageComparisonUtil.readImageFromResources(pngOutputFile.getAbsolutePath());

    File pngDiffFile = new File(outputDir.getAbsolutePath() + File.separator + "diff" + File.separator + 
        file.getName().replace(".diy", ".png"));
    pngDiffFile.getParentFile().mkdirs();

    ImageComparisonResult imageComparisonResult =
        new ImageComparison(expectedImage, actualImage, pngDiffFile).compareImages();

    if (ImageComparisonState.MATCH != imageComparisonResult.getImageComparisonState()) {
      System.out.println(prefix + "Images do not match!");
      return false;
    }
    System.out.println(prefix + "OK");
    return true;
  }

  private static void prepareInputs(File file, ProjectDrawingProvider drawingProvider) {
    File pngDir = new File(file.getParentFile().getParentFile().getAbsolutePath() + File.separator + "png");    
    File pngFile =
        new File(pngDir.getAbsolutePath() + File.separator + file.getName().replace(".diy", ".png"));
    pngFile.getParentFile().mkdirs();
    if (!pngFile.exists()) {
      DrawingExporter.getInstance().exportPNG(drawingProvider, pngFile);
    }
  }
}
