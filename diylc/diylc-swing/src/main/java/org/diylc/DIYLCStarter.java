/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.PropertyInjector;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.swingframework.fonts.FontOptimizer;
import org.diylc.utils.DPIScalingUtils;
import com.thoughtworks.xstream.XStream;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.lang.LangUtil;
import org.diylc.presenter.Presenter;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;

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
	 */
	public static void main(String[] args) {
		try {
			runDIYLC(args);
		} catch (Throwable t) {
			LOG.error("Major error while starting DIYLC", t);
			System.out.println("Major error while starting DIYLC");
			t.printStackTrace(System.out);
		}
	}

	public static void runDIYLC(String[] args) {
		// Initialize splash screen
		DIYLCSplash splash = null;
		Exception splashException = null;
		
		boolean exportMode = Arrays.stream(args).anyMatch(a -> a.equalsIgnoreCase("--export"));

		if (!exportMode) {		  
    		try {
    			splash = new DIYLCSplash();
    		} catch (Exception e) {
    			System.out.println("Splash screen could not be initialized: " + e.getMessage());
    			splashException = e;
    		}
		}

		Properties properties = new Properties();
		try (InputStream inputStream = DIYLCStarter.class.getResourceAsStream("/log4j.properties")) {
			properties.load(inputStream);
			PropertyConfigurator.configure(properties);
		} catch (Exception e) {
			LOG.error("Could not initialize log4j configuration", e);
		}

		initializeConfiguration();

      	if (exportMode) {
		  LOG.info("Running in export mode with params: " + String.join(", ", args));
		}

		LOG.info("Loading languages...");

		LangUtil.configure();

		LOG.debug("Java version: " + System.getProperty("java.runtime.version") + " by "
				+ System.getProperty("java.vm.vendor"));
		LOG.debug("Java home: " + System.getProperty("java.home"));
		LOG.debug("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
//		LOG.debug("Classpath: " + System.getProperty("java.class.path"));
		
		// set OS name as HTTP agent, java will append java version
		Properties props = System.getProperties();
		props.setProperty("http.agent", System.getProperty("os.name"));

		LOG.info("Starting DIYLC with working directory " + System.getProperty("user.dir"));

		LOG.info("Configuration dump start.");
		// log configuration
		Field[] fields = IPlugInPort.class.getFields();
		for (Field f : fields) {
			if (f.getType() != String.class || !f.getName().toUpperCase().equals(f.getName()))
				continue;
			try {
				String key = (String) f.get(null);
				Object configValue = ConfigurationManager.getInstance().readObject(key, null);
				if (configValue != null
						&& (configValue.getClass() == Boolean.class || configValue.getClass() == String.class)) {
					LOG.info(key + " = " + configValue);
				}
			} catch (Exception e) {
				LOG.info("Error logging for field: " + f.getName());
			}
		}
		LOG.info("Configuration dump end.");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// Scale all UIManager font defaults for high DPI displays
			// This applies globally to all Swing components, fixing font scaling
			// issues on Windows with high DPI displays where HTML rendering may
			// not properly scale fonts.
			DPIScalingUtils.scaleUIManagerFonts();
		} catch (Exception e) {
			LOG.error("Could not set Look&Feel", e);
		}

		Thread fontThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					FontOptimizer.run(ConfigurationManager.getInstance());
				} catch (Exception e) {
					LOG.error("Could not ", e);
				}
			}
		});
		fontThread.setPriority(Thread.MIN_PRIORITY);
		fontThread.start();

		String val = System.getProperty(SCRIPT_RUN);
		if (!"true".equals(val)) {
			LOG.info("Detected no scriptRun setting!");
			int response = JOptionPane.showConfirmDialog(splash,
					"It is not recommended to run DIYLC by clicking on the diylc.jar file.\n"
							+ "Please use diylc.exe on Windows or run.sh on OSX/Linux to ensure the best\n"
							+ "performance and reliability. Do you want to continue?",
					"DIYLC", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (response != JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}

		LOG.info("Creating the main frame...");

		MainFrame mainFrame = new MainFrame();
		Presenter presenter = mainFrame.getPresenter();

		LOG.info("Main frame created.");
		// mainFrame.setLocationRelativeTo(null);

		if (!exportMode) {
    		if (splash == null) {
    			LOG.warn("Splash screen could not be initialized", splashException);
    		} else {
    			LOG.info("Hiding splash screen...");
    			splash.setVisible(false);
    			splash.dispose();
    			LOG.info("Done hiding splash screen.");
    		}
		}

		if (!exportMode) {
		  mainFrame.setVisible(true);
		}

		// assign open file handler for Mac
		if (Utils.isMac()) {
			LOG.info("Setting up open file handler for Mac...");
			SwingUtilities.invokeLater(() -> {
			  // On macOS this function is called when a new file is opened.
		      Desktop.getDesktop().setOpenFileHandler(new OpenDIYFilesHandler(presenter));
			});
		}

		if (args.length > 0) {
			presenter.loadProjectFromFile(args[0]);
		} else {
			boolean showTemplates = ConfigurationManager.getInstance().readBoolean(TemplateDialog.SHOW_TEMPLATES_KEY,
					false);
			if (showTemplates) {
				TemplateDialog templateDialog = new TemplateDialog(mainFrame, mainFrame.getPresenter());
				if (!templateDialog.getFiles().isEmpty()) {
					templateDialog.setVisible(true);
				}
			}
		}

		properties = new Properties();
		try (InputStream inputStream = Presenter.class.getResourceAsStream("/config.properties")) {
			properties.load(inputStream);
			PropertyInjector.injectProperties(properties);
		} catch (Exception e) {
			LOG.error("Could not read config.properties file", e);
		}

		if (!exportMode && ConfigurationManager.getInstance().isFileWithErrors())
			mainFrame.showMessage("<html>"
					+ "There was an error reading the configuration file and it was replaced by a default configuration.<br>"
					+ "The backup file is created and placed in user directory under 'diylc' sub-directory with the current timestamp at the end.<br>"					
					+ "</html>", "Warning", IView.WARNING_MESSAGE);
		
		if (exportMode) {
		  String exportFile = args[args.length - 1];
		  if (exportFile == null || exportFile.startsWith("--")) {
		    LOG.error("No output file provided");
		    System.exit(-1);
		  }
		  boolean forceMode = Arrays.stream(args).anyMatch(x -> "--force".equalsIgnoreCase(x));
		  if (new File(exportFile).exists()) {
		    if (forceMode) {
		      LOG.info("Replacing existing file.");
		    } else {
		      LOG.error("File already exists. Use --force flag to overwrite existing files or change export file name.");
		      System.exit(-1);
		    }
		  }
		  if (exportFile.toLowerCase().endsWith(".png")) {
		    try {
    		    ProjectDrawingProvider drawingProvider = new ProjectDrawingProvider(presenter, false, false, false);
    		    DrawingExporter.getInstance().exportPNG(drawingProvider, new File(exportFile));
    		    LOG.info("Done.");
    		    System.exit(0);
		    } catch (Exception e) {
		      LOG.error(e.getMessage());
		        System.exit(-1);
		    }
		  }
		  if (exportFile.toLowerCase().endsWith(".pdf")) {
            try {
                ProjectDrawingProvider drawingProvider = new ProjectDrawingProvider(presenter, false, false, false);
                DrawingExporter.getInstance().exportPDF(drawingProvider, new File(exportFile));
                LOG.info("Done.");
                System.exit(0);
            } catch (Exception e) {
                LOG.error(e.getMessage());
                System.exit(-1);
            }
          }
		  System.err.println("Unsupported output file type. Only PDF and PNG are currently supported");
		  System.exit(-1);
		}
	}

	private static void initializeConfiguration() {
		// initialize
		ConfigurationManager configurationManager = ConfigurationManager.getInstance();
		XStream xStream = configurationManager.getSerializer();
		ProjectFileManager.configure(xStream);
		configurationManager.initialize("diylc");
	}
}
