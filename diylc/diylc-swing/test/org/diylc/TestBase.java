package org.diylc;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.core.IView;
import org.diylc.presenter.Presenter;

public class TestBase {

  protected Presenter presenter;
  protected IView view;
  
  public TestBase() {
    ConfigurationManager.initialize("diylc-test");
    view = new MockView();
    presenter = new Presenter(view, InMemoryConfigurationManager.getInstance());
  }
  
  protected void instantiateOneClick(String category, String name, Point point1) {
    Map<String, List<ComponentType>> componentTypes = presenter.getComponentTypes();
    ComponentType resistorType = componentTypes.get(category).stream().filter(x -> x.getName().equals(name)).findFirst().get();
    presenter.setNewComponentTypeSlot(resistorType, null, false);
    presenter.mouseClicked(point1, MouseEvent.BUTTON1, false, false, false, 1);
  }
  
  protected void instantiateTwoClick(String category, String name, Point point1, Point point2) {
    Map<String, List<ComponentType>> componentTypes = presenter.getComponentTypes();
    ComponentType resistorType = componentTypes.get(category).stream().filter(x -> x.getName().equals(name)).findFirst().get();
    presenter.setNewComponentTypeSlot(resistorType, null, false);
    presenter.mouseClicked(point1, MouseEvent.BUTTON1, false, false, false, 1);
    presenter.mouseMoved(point2, false, false, false);
    presenter.mouseClicked(point2, MouseEvent.BUTTON1, false, false, false, 1);
  }
}
