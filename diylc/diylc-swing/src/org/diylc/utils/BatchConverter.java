package org.diylc.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.DIYLCStarter;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DummyView;

public class BatchConverter {
  
  private static final Logger LOG = Logger.getLogger(BatchConverter.class);

  public static void main(String[] args) {
    URL url = DIYLCStarter.class.getResource("log4j.properties");
    Properties properties = new Properties();
    try {
      properties.load(url.openStream());
      PropertyConfigurator.configure(properties);
    } catch (Exception e) {
      LOG.error("Could not initialize log4j configuration", e);
    }

    File dir = new File("C:\\Users\\Branislav Stojkovic\\Documents\\layouts_v1");
    File[] matchingFiles = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return true;
      }
    });
    Presenter presenter = new Presenter(new DummyView());
    for (File file : matchingFiles) {
      if (file.getName() != "" && file.getName() != "." && file.getName() != ".." && !file.isDirectory())
      presenter.loadProjectFromFile(file.getAbsolutePath());
      presenter.saveProjectToFile(file.getParentFile().getAbsolutePath() + "\\converted\\" + file.getName(), false);
    }
  }

}
