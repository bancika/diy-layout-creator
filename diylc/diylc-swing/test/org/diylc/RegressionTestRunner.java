package org.diylc;

import java.awt.image.BufferedImage;
import java.io.File;
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
    System.out.println("Running command " + command + " in: " + basePath);
    File inputsDir = new File(basePath + File.separator + "input" + File.separator + "diy");
    if (!inputsDir.exists()) {
      System.out.println("Could not find the input directory: " + inputsDir.getAbsolutePath());
      return;
    }
    File[] files = inputsDir.listFiles();
    int okCount = 0;
    int failCount = 0;
    for (File file : files) {
      boolean fileOk = processFile(file, basePath, command);
      if (fileOk) {
        okCount++;
      } else {
        failCount++;
      }
    }
    System.out.println("Finished! OK files: " + okCount + "; Failed files: " + failCount);
  }
  
  private static boolean processFile(File file, String basePath, String command) {
    System.out.print("Processing file: " + file.getName() + "...");
    try {
      IView view = new MockView();
      Presenter presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
      ProjectDrawingProvider drawingProvider = new ProjectDrawingProvider(presenter, false, true, false);      
      presenter.loadProjectFromFile(file.getAbsolutePath());
      if ("PREPARE".equalsIgnoreCase(command)) {
        // generate PNG input image for comparison
        File pngFile = new File(basePath + File.separator + "input" + File.separator + "png" + File.separator + (file.getName().replace(".diy", ".png")));
        if (!pngFile.exists()) {
          DrawingExporter.getInstance().exportPNG(drawingProvider, pngFile);
        }
      } else if ("TEST".equalsIgnoreCase(command)) {
        // test save
        File outputFile = new File(basePath + File.separator + "output" + File.separator + "diy" + File.separator + file.getName());
        if (outputFile.exists()) {
          outputFile.delete();
        }
        presenter.saveProjectToFile(outputFile.getAbsolutePath(), false);
        
        // test export png
        File pngOutputFile = new File(basePath + File.separator + "output" + File.separator + "png" + File.separator + (file.getName().replace(".diy", ".png")));
        if (pngOutputFile.exists()) {
          pngOutputFile.delete();
        }
        DrawingExporter.getInstance().exportPNG(drawingProvider, pngOutputFile);
        File pngInputFile = new File(basePath + File.separator + "input" + File.separator + "png" + File.separator + (file.getName().replace(".diy", ".png")));
        if (!pngInputFile.exists()) {
          System.out.println("Input image does not exist!");
          return false;
        }
        
        BufferedImage expectedImage = ImageComparisonUtil.readImageFromResources(pngInputFile.getAbsolutePath());
        BufferedImage actualImage = ImageComparisonUtil.readImageFromResources(pngOutputFile.getAbsolutePath());

        //Create ImageComparison object and compare the images.
        ImageComparisonResult imageComparisonResult = new ImageComparison(expectedImage, actualImage).compareImages();
        
        //Check the result
        if (ImageComparisonState.MATCH != imageComparisonResult.getImageComparisonState()) {
          System.out.println("Images do not match!");
          return false;
        }
      }
      System.out.println("OK");
      return true;
    } catch (Exception e) {
      System.out.println("ERROR: " + e.getMessage());
      return false;
    }    
  }
}
