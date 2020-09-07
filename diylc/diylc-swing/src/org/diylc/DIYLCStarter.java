/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.PropertyInjector;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.lang.LangUtil;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;
import org.diylc.swingframework.FontChooserComboBox;
import org.diylc.utils.ResourceLoader;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;

/**
 * Main class that runs DIYLC.
 * 
 * @author Branislav Stojkovic
 * 
 * @see Presenter
 * @see MainFrame
 */
public class DIYLCStarter {

  private static final Logger LOG = Logger.getLogger(DIYLCStarter.class);

  private static final String SCRIPT_RUN = "org.diylc.scriptRun";

  /**
   * @param args
   * @throws InvalidActivityException
   */
  public static void main(String[] args) {
    // Initialize splash screen
    DIYLCSplash splash = null;
    Exception splashException = null;

    try {
      splash = new DIYLCSplash();
    } catch (Exception e) {
      System.out.println("Splash screen could not be initialized: " + e.getMessage());
      splashException = e;
    }

    URL url = DIYLCStarter.class.getResource("log4j.properties");
    Properties properties = new Properties();
    try {
      properties.load(url.openStream());
      PropertyConfigurator.configure(properties);
    } catch (Exception e) {
      LOG.error("Could not initialize log4j configuration", e);
    }

    ConfigurationManager.initialize("diylc");

    // disable HIGHLIGHT_CONTINUITY_AREA config, keep it transient
    ConfigurationManager.getInstance().writeValue(IPlugInPort.HIGHLIGHT_CONTINUITY_AREA, false);

    LOG.info("Loading languages...");

    LangUtil.configure();

    LOG.debug("Java version: " + System.getProperty("java.runtime.version") + " by "
        + System.getProperty("java.vm.vendor"));
    LOG.debug("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

    LOG.info("Starting DIYLC with working directory " + System.getProperty("user.dir"));

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      LOG.error("Could not set Look&Feel", e);
    }

    Thread fontThread = new Thread(new Runnable() {

      @Override
      public void run() {
        LOG.debug("Starting font pre-loading");

        File[] fonts = ResourceLoader.getFiles("fonts");

        if (fonts != null)
          for (int i = 0; i < fonts.length; i++) {
            try {
              LOG.info("Dynamically loading font: " + fonts[i].getName());
              Font customFont =
                  Font.createFont(Font.TRUETYPE_FONT, new File(fonts[i].getAbsolutePath()))
                      .deriveFont(12f);
              GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
              // register the font
              ge.registerFont(customFont);
            } catch (IOException e) {
              LOG.error("Could not load font", e);
            } catch (FontFormatException e) {
              LOG.error("Font format error", e);
            }
          }

        FontChooserComboBox box = new FontChooserComboBox();
        box.getPreferredSize();
        JPanel p = new JPanel();
        box.paint(p.getGraphics());
        LOG.debug("Finished font pre-loading");
      }
    });
    fontThread.start();

    String val = System.getProperty(SCRIPT_RUN);
    if (!"true".equals(val)) {
      int response = JOptionPane.showConfirmDialog(null,
          "It is not recommended to run DIYLC by clicking on the diylc.jar file.\n"
              + "Please use diylc.exe on Windows or run.sh on OSX/Linux to ensure the best\n"
              + "performance and reliability. Do you want to continue?",
          "DIYLC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (response != JOptionPane.YES_OPTION) {
        System.exit(0);
      }
    }

    MainFrame mainFrame = new MainFrame();
    Presenter presenter = mainFrame.getPresenter();
    // mainFrame.setLocationRelativeTo(null);

    if (splash == null) {
      LOG.warn("Splash screen could not be initialized", splashException);
    } else {
      splash.setVisible(false);
      splash.dispose();
    }

    mainFrame.setVisible(true);

    // assign open file handler for Mac
    if (Utils.isMac()) {
      SwingUtilities.invokeLater(() -> {
        Application.getApplication().setOpenFileHandler((AppEvent.OpenFilesEvent ofe) -> {
          List<File> files = ofe.getFiles();
          if (files != null && files.size() > 0) {
            String filePath = files.get(0).getAbsolutePath();
            if (presenter.allowFileAction()) {
              presenter.loadProjectFromFile(filePath);
            }
          }
        });
      });
    }

    if (args.length > 0) {
      presenter.loadProjectFromFile(args[0]);
    } else {
      boolean showTemplates =
          ConfigurationManager.getInstance().readBoolean(TemplateDialog.SHOW_TEMPLATES_KEY, false);
      if (showTemplates) {
        TemplateDialog templateDialog = new TemplateDialog(mainFrame, mainFrame.getPresenter());
        if (!templateDialog.getFiles().isEmpty()) {
          templateDialog.setVisible(true);
        }
      }
    }

    properties = new Properties();
    try {
      LOG.info("Injecting default properties.");
      URL resource = Presenter.class.getResource("config.properties");
      if (resource != null) {
        properties.load(resource.openStream());
        PropertyInjector.injectProperties(properties);
      }
    } catch (Exception e) {
      LOG.error("Could not read config.properties file", e);
    }

    if (ConfigurationManager.getInstance().isFileWithErrors())
      mainFrame.showMessage("<html>"
          + "There was an error reading the configuration file and it was replaced by a default configuration.<br>"
          + "The backup file is created and placed in user directory under '.diylc' sub-directory with '~' at the end.<br>"
          + "This can happen when running two versions of DIYLC on the same machine at the same time.<br>"
          + "Replace the main 'config.xml' file with the backup when running the latest version of DIYLC."
          + "</html>", "Warning", IView.WARNING_MESSAGE);
  }
}
