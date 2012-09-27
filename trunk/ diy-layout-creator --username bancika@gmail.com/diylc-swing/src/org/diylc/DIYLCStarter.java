package org.diylc;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.PropertyInjector;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;

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
		URL url = DIYLCStarter.class.getResource("log4j.properties");
		Properties properties = new Properties();
		try {
			properties.load(url.openStream());
			PropertyConfigurator.configure(properties);
		} catch (Exception e) {
			LOG.error("Could not initialize log4j configuration", e);
		}

		LOG.debug("Java version: " + System.getProperty("java.runtime.version")
				+ " by " + System.getProperty("java.vm.vendor"));
		LOG.debug("OS: " + System.getProperty("os.name") + " "
				+ System.getProperty("os.version"));

		LOG.info("Starting DIYLC with working directory "
				+ System.getProperty("user.dir"));

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOG.error("Could not set Look&Feel", e);
		}
		
		String val = System.getProperty(SCRIPT_RUN);
		if (!"true".equals(val)) {
			int response = JOptionPane
					.showConfirmDialog(
							null,
							"It is not recommended to run DIYLC by clicking on the diylc.jar file.\n" +
							"Please use diylc.exe on Windows or run.sh on OSX/Linux to ensure the best\n" +
							"performance and reliability. Do you want to continue?",
							"DIYLC", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
			if (response != JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}

		MainFrame mainFrame = new MainFrame();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		if (args.length > 0) {
			mainFrame.getPresenter().loadProjectFromFile(args[0]);
		} else {
			boolean showTemplates = ConfigurationManager.getInstance()
					.readBoolean(TemplateDialog.SHOW_TEMPLATES_KEY, true);
			if (showTemplates) {
				TemplateDialog templateDialog = new TemplateDialog(mainFrame,
						mainFrame.getPresenter());
				if (!templateDialog.getFiles().isEmpty()) {
					templateDialog.setVisible(true);
				}
			}
		}

		properties = new Properties();
		try {
			LOG.info("Injecting default properties.");
			properties.load(new FileInputStream("config.properties"));
			PropertyInjector.injectProperties(properties);
		} catch (Exception e) {
			LOG.error("Could not read config.properties file", e);
		}
	}
}
