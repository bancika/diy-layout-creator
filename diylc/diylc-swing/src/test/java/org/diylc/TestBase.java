package org.diylc;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;

import org.diylc.common.ComponentType;
import org.diylc.core.IView;
import org.diylc.presenter.Presenter;

public class TestBase {

  protected Presenter presenter;
  protected IView view;
  
  public TestBase() {
    URL url = DIYLCStarter.class.getResource("/log4j.properties");
    Properties properties = new Properties();
    try (InputStream inputStream = url.openStream()) {
        properties.load(inputStream);
        PropertyConfigurator.configure(properties);
    } catch (Exception e) {
        System.err.println("Could not initialize log4j configuration: " + e.getMessage());
    }
    
    ConfigurationManager.getInstance().initialize("diylc-test");
    view = new MockView();
    presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
  }
  
  protected void instantiateOneClick(String category, String name, Point point1) {
    Map<String, List<ComponentType>> componentTypes = presenter.getComponentTypes();
    ComponentType resistorType = componentTypes.get(category).stream().filter(x -> x.getName().equals(name)).findFirst().get();
    presenter.setNewComponentTypeSlot(resistorType, null, null, false);
    presenter.mouseClicked(point1, MouseEvent.BUTTON1, false, false, false, 1);
  }
  
  protected void instantiateTwoClick(String category, String name, Point point1, Point point2) {
    Map<String, List<ComponentType>> componentTypes = presenter.getComponentTypes();
    ComponentType resistorType = componentTypes.get(category).stream().filter(x -> x.getName().equals(name)).findFirst().get();
    presenter.setNewComponentTypeSlot(resistorType, null, null, false);
    presenter.mouseClicked(point1, MouseEvent.BUTTON1, false, false, false, 1);
    presenter.mouseMoved(point2, false, false, false);
    presenter.mouseClicked(point2, MouseEvent.BUTTON1, false, false, false, 1);
  }
}
