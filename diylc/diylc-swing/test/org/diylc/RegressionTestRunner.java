package org.diylc;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.update.VersionNumber;
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
    List<RegressionTestResult> collect =
        visit(inputsDir, basePath, command, filter).collect(Collectors.toList());



    try {
      if ("TEST".equalsIgnoreCase(command)) {
        createReport(collect, basePath);
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    System.out.println("Finished!");
  }

  private static void createReport(List<RegressionTestResult> results, String basePath)
      throws IOException {
    IView view = new MockView();
    Presenter presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
    VersionNumber currentVersionNumber = presenter.getCurrentVersionNumber();

    String fileName = basePath + File.separator + "reports" + File.separator + "V"
        + currentVersionNumber.toString().replace('.', '_') + "-"
        + LocalDateTime.now().toString().replace(":", "_") + ".csv";

    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    
    long okCount = results.stream().filter(x -> x.ok).count();
    long failCount = results.size() - okCount;
    long totalTime = results.stream().map(x -> x.duration).reduce(0l, (a, b) -> a + b);
    long avgTime = totalTime / results.size();
    
    writer.write("OK,Failed,Total Time,Avg Time");
    writer.newLine();
    writer.write(Long.toString(okCount));
    writer.write(",");
    writer.write(Long.toString(failCount));
    writer.write(",");
    writer.write(Long.toString(totalTime));
    writer.write(",");
    writer.write(Long.toString(avgTime));    
    writer.newLine();
    writer.newLine();
    writer.newLine();
    
    writer.write("File,Status,Message,Time");
    writer.newLine();
    results.stream().sorted(Comparator.comparing(x -> x.fileName)).forEach(item -> {
      try {
        writer.write(item.fileName);
        writer.write(",");
        writer.write(item.ok ? "OK" : "FAILED");
        writer.write(",");
        writer.write(item.message == null ? "" : item.message);
        writer.write(",");
        writer.write(Long.toString(item.duration));
        writer.newLine();
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    });

    writer.close();
    
    System.out.println("OK files: " + okCount + "; Failed files: " + failCount
        + "; totalTime: " + (totalTime / 1000) + "s" + "; avgTime: " + avgTime + "ms");

    System.out.println("Saved test report to: " + fileName);
  }

  private static Stream<RegressionTestResult> visit(File inputsDir, String basePath, String command,
      String filter) {
    File[] files = inputsDir.listFiles();
    return Arrays.stream(files).parallel().flatMap(file -> {
      if (file.isDirectory()) {
        return visit(file, basePath, command, filter);
      } else {
        if (!file.getName().endsWith(".diy")) {
          return Stream.empty();
        }
        if (filter != null) {
          Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
          Matcher matcher = pattern.matcher(file.getAbsolutePath().substring(basePath.length()));
          boolean matchFound = matcher.find();
          if (!matchFound) {
            return Stream.empty();
          }
        }
        RegressionTestResult testResult = processFile(file, command);
        System.out
            .println(testResult.fileName + ": " + (testResult.ok ? "OK" : testResult.message));
        return Stream.of(testResult);
      }
    });
  }

  private static RegressionTestResult processFile(File file, String command) {
    String prefix = "File: " + file.getName() + " status: ";
    try {
      IView view = new MockView();
      Presenter presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
      ProjectDrawingProvider drawingProvider =
          new ProjectDrawingProvider(presenter, false, true, false);
      presenter.loadProjectFromFile(file.getAbsolutePath());
      if ("PREPARE".equalsIgnoreCase(command)) {
        // generate PNG input image for comparison
        return prepareInputs(file, drawingProvider);
      } else if ("TEST".equalsIgnoreCase(command)) {
        return testOutputs(file, prefix, presenter, drawingProvider);
      }
      return new RegressionTestResult(file.getName()).failed("Command not recognized");
    } catch (Exception e) {
      System.out.println(prefix + "ERROR: " + e.getMessage());
      return new RegressionTestResult(file.getName()).failed(e.getMessage());
    }
  }

  private static RegressionTestResult testOutputs(File file, String prefix, Presenter presenter,
      ProjectDrawingProvider drawingProvider) {
    RegressionTestResult res = new RegressionTestResult(file.getName());
    File baseDir = file.getParentFile().getParentFile().getParentFile().getParentFile();
    File outputDir = new File(baseDir.getAbsolutePath() + File.separator + "output" + File.separator
        + file.getParentFile().getParentFile().getName());
    File inputDir = file.getParentFile().getParentFile();
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }
    // test save
    File outputFile = new File(
        outputDir.getAbsolutePath() + File.separator + "diy" + File.separator + file.getName());
    outputFile.getParentFile().mkdirs();
    if (outputFile.exists()) {
      outputFile.delete();
    }
    presenter.saveProjectToFile(outputFile.getAbsolutePath(), false);

    // test export png
    File pngOutputFile = new File(outputDir.getAbsolutePath() + File.separator + "png"
        + File.separator + file.getName().replace(".diy", ".png"));
    pngOutputFile.getParentFile().mkdirs();
    if (pngOutputFile.exists()) {
      pngOutputFile.delete();
    }
    DrawingExporter.getInstance().exportPNG(drawingProvider, pngOutputFile);
    File pngInputFile = new File(inputDir.getAbsolutePath() + File.separator + "png"
        + File.separator + (file.getName().replace(".diy", ".png")));
    if (!pngInputFile.exists()) {
      return res.failed("Input image does not exist!");
    }

    BufferedImage expectedImage =
        ImageComparisonUtil.readImageFromResources(pngInputFile.getAbsolutePath());
    BufferedImage actualImage =
        ImageComparisonUtil.readImageFromResources(pngOutputFile.getAbsolutePath());

    File pngDiffFile = new File(outputDir.getAbsolutePath() + File.separator + "diff"
        + File.separator + file.getName().replace(".diy", ".png"));
    pngDiffFile.getParentFile().mkdirs();

    ImageComparisonResult imageComparisonResult =
        new ImageComparison(expectedImage, actualImage, pngDiffFile).compareImages();

    if (ImageComparisonState.MATCH != imageComparisonResult.getImageComparisonState()) {
      return res.failed("Images do not match!");
    }
    return res.succedded();
  }

  private static RegressionTestResult prepareInputs(File file,
      ProjectDrawingProvider drawingProvider) {
    RegressionTestResult res = new RegressionTestResult(file.getName());
    try {
      File pngDir =
          new File(file.getParentFile().getParentFile().getAbsolutePath() + File.separator + "png");
      File pngFile = new File(
          pngDir.getAbsolutePath() + File.separator + file.getName().replace(".diy", ".png"));
      pngFile.getParentFile().mkdirs();
      if (!pngFile.exists()) {
        DrawingExporter.getInstance().exportPNG(drawingProvider, pngFile);
      }
    } catch (Exception e) {
      return res.failed(e.getMessage());
    }
    return res.succedded();
  }

  static class RegressionTestResult {
    boolean ok;
    String message;
    long startTime;
    long duration;
    String fileName;

    public RegressionTestResult(String fileName) {
      this.fileName = fileName;
      startTime = System.currentTimeMillis();
    }

    public RegressionTestResult succedded() {
      this.ok = true;
      this.duration = System.currentTimeMillis() - this.startTime;
      return this;
    }

    public RegressionTestResult failed(String message) {
      this.ok = true;
      this.message = message;
      this.duration = System.currentTimeMillis() - this.startTime;
      return this;
    }
  }
}
