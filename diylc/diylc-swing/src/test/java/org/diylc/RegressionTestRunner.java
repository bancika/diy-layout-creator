package org.diylc;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.diylc.swingframework.export.DrawingExporter;

import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistException;
import org.diylc.presenter.Presenter;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;

public class RegressionTestRunner {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("No path and command provided");
      return;
    }
    //
    // URL url = DIYLCStarter.class.getResource("log4j.properties");
    // Properties properties = new Properties();
    // try {
    // properties.load(url.openStream());
    // PropertyConfigurator.configure(properties);
    // } catch (Exception e) {
    // System.err.println("Could not initialize log4j configuration: " + e.getMessage());
    // }

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

    String fileName = basePath + File.separator + "reports" + File.separator
        + LocalDateTime.now().toString().replace(":", "_") + "-" + "V"
        + currentVersionNumber.toString().replace('.', '_') + ".csv";

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

    System.out.println("OK files: " + okCount + "; Failed files: " + failCount + "; totalTime: "
        + (totalTime / 1000) + "s" + "; avgTime: " + avgTime + "ms");

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
        System.out.println(
            testResult.fileName + ": " + (testResult.message == null ? "OK" : testResult.message));
        return Stream.of(testResult);
      }
    });
  }

  private static RegressionTestResult processFile(File file, String command) {
    String prefix = "File: " + file.getName() + " status: ";
    try {
      MockView view = new MockView();
      Presenter presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
      ProjectDrawingProvider drawingProvider =
          new ProjectDrawingProvider(presenter, false, true, false);
      presenter.loadProjectFromFile(file.getAbsolutePath());
      if ("PREPARE".equalsIgnoreCase(command)) {
        // generate PNG input image for comparison
        return prepareInputs(file, drawingProvider, presenter);
      } else if ("TEST".equalsIgnoreCase(command)) {
        return testOutputs(file, prefix, presenter, drawingProvider, view);
      }
      return new RegressionTestResult(file.getName()).failed("Command not recognized");
    } catch (Exception e) {
      System.out.println(prefix + "ERROR: " + e.getMessage());
      return new RegressionTestResult(file.getName()).failed(e.getMessage());
    }
  }

  private static RegressionTestResult testOutputs(File file, String prefix, Presenter presenter,
      ProjectDrawingProvider drawingProvider, MockView view) throws NetlistException {
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
      res.failed("Input image does not exist!");
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
      res.failed("Images do not match!");
    }

    testNetlist(file, presenter, res, outputDir, inputDir, true);
//    if (netlistResultWithSwitches != null)
//      res.merge(netlistResultWithSwitches);
    
    testNetlist(file, presenter, res, outputDir, inputDir, false);
//    if (netlistResultWithoutSwitches != null)
//      res.merge(netlistResultWithoutSwitches);

    // List<INetlistAnalyzer> summarizers = presenter.getNetlistAnalyzers();
    // if (summarizers != null) {
    // List<Netlist> netlists = presenter.extractNetlists(true);
    //
    // for (INetlistAnalyzer summarizer : summarizers) {
    // File summaryOutputDir =
    // new File(outputDir.getAbsolutePath() + File.separator + summarizer.getShortName());
    // File summaryOutputFile = new File(summaryOutputDir.getAbsolutePath() + File.separator
    // + file.getName().replace(".diy", ".txt"));
    // summaryOutputDir.mkdirs();
    // if (summaryOutputFile.exists()) {
    // summaryOutputFile.delete();
    // }
    //
    // File summaryInputDir = new File(file.getParentFile().getParentFile().getAbsolutePath()
    // + File.separator + summarizer.getShortName());
    // File summaryInputFile = new File(summaryInputDir.getAbsolutePath() + File.separator
    // + file.getName().replace(".diy", ".txt"));
    //
    // if (!summaryInputFile.exists()) {
    // return res.failed("Summary input file not found for: " + summarizer.getShortName());
    // }
    //
    // try {
    // List<String> outputLines = new ArrayList<String>();
    // try {
    // writeSummariesToFile(netlists, summarizer, summaryOutputFile);
    // outputLines = Files.readAllLines(Paths.get(summaryOutputFile.getAbsolutePath()));
    // } catch (Exception e) {
    // try (BufferedWriter writer = new BufferedWriter(
    // new OutputStreamWriter(new FileOutputStream(summaryOutputFile, true), "UTF-8"))) {
    // String line = e.getClass().getCanonicalName() + ": " + e.getMessage();
    // writer.write(line);
    // outputLines.add(line);
    // }
    // }
    //
    // List<String> inputLines =
    // Files.readAllLines(Paths.get(summaryInputFile.getAbsolutePath()));
    //
    // if (inputLines.size() != outputLines.size()) {
    // return res.failed(
    // "Summary input and output files do not match for: " + summarizer.getShortName());
    // }
    // for (int i = 0; i < inputLines.size(); i++) {
    // if (!inputLines.get(i).equals(outputLines.get(i))) {
    // return res.failed(
    // "Summary input and output files do not match for: " + summarizer.getShortName());
    // }
    // }
    // } catch (Exception e) {
    // return res.failed(e.getMessage());
    // }
    // }
    // }

    String message = null;
    if (!view.getMessages().isEmpty()) {
      message = view.getMessages().entrySet().stream().flatMap(x -> x.getValue().stream())
          .reduce("", String::concat);
    }
    
    if (!res.ok)
      return res;

    return res.succedded(message);
  }

  private static RegressionTestResult testNetlist(File file, Presenter presenter, RegressionTestResult res,
      File outputDir, File inputDir, boolean includeSwitches) throws NetlistException {    
    File netlistOutputDir = new File(outputDir.getAbsolutePath() + File.separator + "netlist");
    File netlistOutputFile = new File(netlistOutputDir.getAbsolutePath() + File.separator
        + file.getName().replace(".diy", includeSwitches ? "_incl_switches.txt" : "_excl_switches.txt"));
    File netlistInputFile = new File(inputDir.getAbsolutePath() + File.separator + "netlist"
        + File.separator + (file.getName().replace(".diy", includeSwitches ? "_incl_switches.txt" : "_excl_switches.txt")));

    netlistOutputDir.mkdirs();
    try {
      if (netlistOutputFile.exists()) {
        netlistOutputFile.delete();
      }
      try {
        List<Netlist> netlists = presenter.extractNetlists(includeSwitches);
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(netlistOutputFile, true), "UTF-8"))) {
          for (Netlist netlist : netlists) {
            writer.write(netlist.toString());
            writer.newLine();
          }
        }
      } catch (Exception e) {
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(netlistOutputFile, true), "UTF-8"))) {
          writer.write(e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
      }

      List<String> inputLines = Files.readAllLines(Paths.get(netlistInputFile.getAbsolutePath()));
      List<String> outputLines = Files.readAllLines(Paths.get(netlistOutputFile.getAbsolutePath()));

      if (inputLines.size() != outputLines.size()) {
        return res.failed("Netlists do not match " + (includeSwitches ? "with switches" : "without switches"));
      }
      for (int i = 0; i < inputLines.size(); i++) {
        if (!inputLines.get(i).equals(outputLines.get(i))) {
          return res.failed("Netlists do not match " + (includeSwitches ? "with switches" : "without switches"));
        }
      }
    } catch (IOException e) {
      return res.failed(e.getMessage());
    }
    return null;
  }

  private static RegressionTestResult prepareInputs(File file,
      ProjectDrawingProvider drawingProvider, IPlugInPort plugInPort) {
    RegressionTestResult res = new RegressionTestResult(file.getName());
    try {
      File pngDir =
          new File(file.getParentFile().getParentFile().getAbsolutePath() + File.separator + "png");
      File pngFile = new File(
          pngDir.getAbsolutePath() + File.separator + file.getName().replace(".diy", ".png"));
      pngFile.getParentFile().mkdirs();

      // always export image to ensure that rendering is done and continuity areas are populated
      DrawingExporter.getInstance().exportPNG(drawingProvider, pngFile);

      prepareNetlist(file, plugInPort, true);
      prepareNetlist(file, plugInPort, false);

      // List<INetlistAnalyzer> summarizers = plugInPort.getNetlistAnalyzers();
      // if (summarizers != null) {
      //
      //
      // for (INetlistAnalyzer summarizer : summarizers) {
      // summaryDir = new File(file.getParentFile().getParentFile().getAbsolutePath()
      // + File.separator + summarizer.getShortName());
      // summaryFile = new File(summaryDir.getAbsolutePath() + File.separator
      // + file.getName().replace(".diy", ".txt"));
      // summaryDir.mkdirs();
      // if (!summaryFile.exists()) {
      // try {
      // writeSummariesToFile(netlists, summarizer, summaryFile);
      //
      // } catch (Exception e) {
      // try (BufferedWriter writer = new BufferedWriter(
      // new OutputStreamWriter(new FileOutputStream(summaryFile, true), "UTF-8"))) {
      // writer.write(e.getClass().getCanonicalName() + ": " + e.getMessage());
      // }
      // }
      // }
      // }
      // }
    } catch (Exception e) {
      return res.failed(e.getMessage());
    }
    return res.succedded();
  }

  private static void prepareNetlist(File file, IPlugInPort plugInPort, boolean includeSwitches)
      throws IOException, UnsupportedEncodingException, FileNotFoundException, NetlistException {
    
    File netlistDir = new File(
        file.getParentFile().getParentFile().getAbsolutePath() + File.separator + "netlist");
    File netlistFile = new File(
        netlistDir.getAbsolutePath() + File.separator + file.getName().replace(".diy", includeSwitches ? "_incl_switches.txt" : "_excl_switches.txt"));

    netlistDir.mkdirs();
    if (!netlistFile.exists()) {
      try {
        List<Netlist> netlists = plugInPort.extractNetlists(includeSwitches);
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(netlistFile, true), "UTF-8"))) {
          for (Netlist netlist : netlists) {
            writer.write(netlist.toString());
            writer.newLine();
          }
        }
      } catch (Exception e) {
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(netlistFile, true), "UTF-8"))) {
          writer.write(e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
      }
    }
  }

//  private static void writeSummariesToFile(List<Netlist> netlists, INetlistAnalyzer summarizer,
//      File summaryFile)
//      throws TreeException, IOException, UnsupportedEncodingException, FileNotFoundException {
//    List<Summary> summaries = summarizer.summarize(netlists, null);
//
//    try (BufferedWriter writer = new BufferedWriter(
//        new OutputStreamWriter(new FileOutputStream(summaryFile, true), "UTF-8"))) {
//      for (Summary summary : summaries) {
//        writer.write("<b>Switch configuration: ");
//        writer.write(summary.getNetlist().getSwitchSetup().toString());
//        writer.write("</b><br><br>");
//        writer.write(summary.getSummary());
//        writer.newLine();
//      }
//    }
//  }

  static class RegressionTestResult {
    boolean ok;
    String message;
    long startTime;
    long duration;
    String fileName;

    public RegressionTestResult(String fileName) {
      this.fileName = fileName;
      startTime = System.currentTimeMillis();
      ok = true;
    }

    public RegressionTestResult succedded() {
      return succedded(null);
    }

    public RegressionTestResult succedded(String message) {
      this.ok = true;
      this.message = message;
      this.duration = System.currentTimeMillis() - this.startTime;
      return this;
    }

    public RegressionTestResult failed(String message) {
      this.ok = false;
      if (this.message == null)
        this.message = message;
      else if (message != null)
        this.message = this.message + ";" + message;
      this.duration = System.currentTimeMillis() - this.startTime;
      return this;
    }
    
//    public void merge(RegressionTestResult other) {
//      this.ok = this.ok && other.ok;
//      this.duration = System.currentTimeMillis() - this.startTime;
//      if (this.message == null)
//        this.message = other.message;
//      else if (other.message != null)
//        this.message = this.message + ";" + other.message;
//    }
  }
}
