package org.diylc;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.gui.MainFrame;
import org.diylc.presenter.Presenter;

import com.diyfever.gui.miscutils.PropertyInjector;

/**
 * 
 * @author Branislav Stojkovic
 */
public class DIYLCStarter {
	
	private static final Logger LOG = Logger.getLogger(Presenter.class);

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

		url = DIYLCStarter.class.getResource("/config.properties");
		properties = new Properties();
		try {
			properties.load(new FileInputStream("config.properties"));
			PropertyInjector.injectProperties(properties);
		} catch (Exception e) {
			LOG.error("Could not read config.properties file", e);
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			LOG.error("Could not set Look&Feel", e);
		}

		MainFrame mainFrame = new MainFrame();
		// Presenter p = new Presenter(mainFrame);
		//
		// p.installPlugin(new ToolBox());
		// p.installPlugin(new FileManager());
		// p.installPlugin(new ClipboardManager());
		// p.installPlugin(new StatusBar());
		// p.installPlugin(new CanvasPlugin());
		//
		// Project project = new Project();
		// project.getComponents().add(new MockDIYComponent());
		// p.loadProject(project);
		// panel.setPreferredSize(new Dimension(2000, 1500));
		// RulerScrollPane scrollPane = new RulerScrollPane(panel);
		// JFrame f = new JFrame("Test");
		// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// f.add(scrollPane);
		// f.setPreferredSize(new Dimension(800, 600));
		// f.pack();
		// f.setLocationRelativeTo(null);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);

		// List<Property> properties = ClassProcessor
		// .extractProperties(MockComponent.class);
		// MockComponent component = new MockComponent();
		// for (Property p : properties) {
		// try {
		// p.readFrom(component);
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// PropertyEditorDialog.showFor(null, properties);
		// for (Property p : properties) {
		// try {
		// p.writeTo(component);
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println(p);
		// }
		// System.out.println(p.getComponentTypes());
	}
}
