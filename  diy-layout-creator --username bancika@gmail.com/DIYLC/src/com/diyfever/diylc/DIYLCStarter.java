package com.diyfever.diylc;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.UIManager;

import org.apache.log4j.PropertyConfigurator;

import com.diyfever.diylc.gui.MainFrame;
import com.diyfever.gui.miscutils.PropertyInjector;

/**
 * 
 * @author Branislav Stojkovic
 */
public class DIYLCStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		URL url = DIYLCStarter.class.getResource("log4j.properties");
		Properties properties = new Properties();
		try {
			properties.load(url.openStream());
			PropertyConfigurator.configure(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		url = DIYLCStarter.class.getResource("config.properties");
		properties = new Properties();
		try {
			properties.load(url.openStream());
			PropertyInjector.injectProperties(properties);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		// project.getComponents().add(new MockComponentInstance());
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
