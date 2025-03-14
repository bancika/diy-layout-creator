<<<<<<< Updated upstream:diylc/diylc-swing/test/org/diylc/TestBase.java
package org.diylc;

import java.awt.Point;
import java.awt.event.MouseEvent;
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
    URL url = DIYLCStarter.class.getResource("log4j.properties");
    Properties properties = new Properties();
    try {
        properties.load(url.openStream());
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
=======
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
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;

import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.ComponentType;
import org.diylc.config.DummyViewDIYLCConfig;
import org.diylc.core.IView;
import org.diylc.presenter.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
    presenter = getPresenter(view);
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

  static Presenter getPresenter(IView view) {
    ConfigurableApplicationContext context = new SpringApplicationBuilder(DummyViewDIYLCConfig.class, DIYLCSwingConfig.class)
            .web(WebApplicationType.NONE)
            .headless(false)
            .run();

    Presenter presenter = new Presenter(view,
            context.getBean(IConfigurationManager.class),
            context.getBean(MessageDispatcher.class),
            context.getBean(DrawingService.class),
            context.getBean(ProjectFileService.class),
            context.getBean(InstantiationService.class),
            context.getBean(VariantService.class),
            context.getBean(BuildingBlockService.class),
            false);
    return presenter;
  }
}
>>>>>>> Stashed changes:diylc/diylc-swing/src/test/java/org/diylc/TestBase.java
