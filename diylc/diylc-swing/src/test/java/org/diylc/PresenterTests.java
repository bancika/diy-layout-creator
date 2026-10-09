package org.diylc;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import org.junit.Test;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Template;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.actions.ComponentCategoryAction;
import org.diylc.swing.actions.ResetCategoriesAction;
import org.diylc.swing.plugins.tree.CustomTreeModel;

public class PresenterTests extends TestBase {
  
  @Test
  public void testComponentLoad() {
    Assert.assertTrue(presenter.getComponentTypes() != null && presenter.getComponentTypes().size() > 0);
  }

  @Test
  public void testTwoClickAdd() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    Assert.assertEquals(1, presenter.getCurrentProject().getComponents().size());
  }
  
  @Test
  public void testOneClickAdd() {
    instantiateOneClick("Connectivity", "Solder Pad", new Point(1000, 1000));
    Assert.assertEquals(1, presenter.getCurrentProject().getComponents().size());
  }

  @Test
  public void testNodeNameHover() {
    final String[] receivedTooltip = new String[1];
    final Point[] receivedPoint = new Point[1];

    org.diylc.common.IPlugIn testPlugin = new org.diylc.common.IPlugIn() {
      @Override
      public void connect(org.diylc.common.IPlugInPort plugInPort) {}

      @Override
      public java.util.EnumSet<org.diylc.common.EventType> getSubscribedEventTypes() {
        return java.util.EnumSet.of(org.diylc.common.EventType.NODE_NAME_HOVER_TOOLTIP);
      }

      @Override
      public void processMessage(org.diylc.common.EventType eventType, Object... params) {
        if (eventType == org.diylc.common.EventType.NODE_NAME_HOVER_TOOLTIP) {
          receivedTooltip[0] = params.length > 0 ? (String) params[0] : null;
          receivedPoint[0] = params.length > 1 ? (Point) params[1] : null;
        }
      }
    };
    presenter.installPlugin(() -> testPlugin);

    // Add first resistor at (1000, 1000) -> (1200, 1000)
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    Assert.assertEquals(1, presenter.getCurrentProject().getComponents().size());

    // Enable node names
    org.diylc.appframework.miscutils.InMemoryConfigurationManager.getInstance().writeValue(
        org.diylc.common.IPlugInPort.SHOW_NODE_NAME_TOOLTIPS_KEY, true);

    // Hover over point 1 (1000, 1000)
    presenter.mouseMoved(new Point(1000, 1000), false, false, false);
    Assert.assertEquals("<b>R1</b>: 1", receivedTooltip[0]);
    Assert.assertEquals(new Point(1000, 1000), receivedPoint[0]);

    // Hover over point 2 (1200, 1000)
    presenter.mouseMoved(new Point(1200, 1000), false, false, false);
    Assert.assertEquals("<b>R1</b>: 2", receivedTooltip[0]);

    // Add second resistor with pin 1 also at (1000, 1000)
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1000, 1200));
    Assert.assertEquals(2, presenter.getCurrentProject().getComponents().size());

    // Hover over point (1000, 1000) - should show both R2 and R1 separated by semicolon
    presenter.mouseMoved(new Point(1000, 1000), false, false, false);
    Assert.assertEquals("<b>R2</b>: 1; <b>R1</b>: 1", receivedTooltip[0]);

    // Hover away
    presenter.mouseMoved(new Point(0, 0), false, false, false);
    Assert.assertNull(receivedTooltip[0]);

    // Hover back over (1000, 1000)
    presenter.mouseMoved(new Point(1000, 1000), false, false, false);
    Assert.assertNotNull(receivedTooltip[0]);

    // Start drag - should hide tooltip
    presenter.dragStarted(new Point(1000, 1000), java.awt.dnd.DnDConstants.ACTION_MOVE, false);
    Assert.assertNull(receivedTooltip[0]);

    // End drag
    presenter.dragEnded(new Point(1000, 1000));

    // Disable node names config
    org.diylc.appframework.miscutils.InMemoryConfigurationManager.getInstance().writeValue(
        org.diylc.common.IPlugInPort.SHOW_NODE_NAME_TOOLTIPS_KEY, false);
    presenter.mouseMoved(new Point(1000, 1000), false, false, false);
    Assert.assertNull(receivedTooltip[0]);
  }

  @Test
  public void testCategoryVisibilityDefaults() {
    presenter.resetCategoryVisibility();
    Map<String, List<ComponentType>> all = presenter.getComponentTypes();
    Map<String, List<ComponentType>> visible = presenter.getVisibleComponentTypes();
    Assert.assertEquals(all.keySet(), visible.keySet());
    for (String category : all.keySet()) {
      Assert.assertTrue(presenter.isCategoryEnabled(category));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDisableAndEnableCategory() {
    presenter.resetCategoryVisibility();

    final Set<String>[] lastEventDisabled = new Set[1];
    IPlugIn testPlugin = new IPlugIn() {
      @Override
      public void connect(IPlugInPort plugInPort) {}

      @Override
      public EnumSet<EventType> getSubscribedEventTypes() {
        return EnumSet.of(EventType.COMPONENT_CATEGORIES_CHANGED);
      }

      @Override
      public void processMessage(EventType eventType, Object... params) {
        if (eventType == EventType.COMPONENT_CATEGORIES_CHANGED && params.length > 0) {
          lastEventDisabled[0] = (Set<String>) params[0];
        }
      }
    };
    presenter.installPlugin(() -> testPlugin);

    // Disable "Tubes"
    presenter.setCategoryEnabled("Tubes", false);
    Assert.assertFalse(presenter.isCategoryEnabled("Tubes"));
    Assert.assertFalse(presenter.getVisibleComponentTypes().containsKey("Tubes"));
    Assert.assertTrue(presenter.getComponentTypes().containsKey("Tubes"));
    Assert.assertNotNull(lastEventDisabled[0]);
    Assert.assertTrue(lastEventDisabled[0].contains("Tubes"));

    // Also disable "Robotics"
    presenter.setCategoryEnabled("Robotics", false);
    Assert.assertFalse(presenter.isCategoryEnabled("Robotics"));
    Assert.assertFalse(presenter.getVisibleComponentTypes().containsKey("Robotics"));
    Assert.assertTrue(lastEventDisabled[0].contains("Robotics"));
    Assert.assertTrue(lastEventDisabled[0].contains("Tubes"));

    // Check config stores sorted list
    List<String> stored = (List<String>) InMemoryConfigurationManager.getInstance()
        .readObject(IPlugInPort.DISABLED_CATEGORIES_KEY, new ArrayList<String>());
    Assert.assertEquals(2, stored.size());
    Assert.assertEquals("Robotics", stored.get(0));
    Assert.assertEquals("Tubes", stored.get(1));

    // Re-enable "Tubes"
    presenter.setCategoryEnabled("Tubes", true);
    Assert.assertTrue(presenter.isCategoryEnabled("Tubes"));
    Assert.assertTrue(presenter.getVisibleComponentTypes().containsKey("Tubes"));
    Assert.assertFalse(lastEventDisabled[0].contains("Tubes"));
    Assert.assertTrue(lastEventDisabled[0].contains("Robotics"));

    // Reset all
    presenter.resetCategoryVisibility();
    Assert.assertTrue(presenter.isCategoryEnabled("Robotics"));
    Assert.assertTrue(presenter.isCategoryEnabled("Tubes"));
    Assert.assertTrue(lastEventDisabled[0].isEmpty());
  }

  @Test
  public void testComponentCategoryActions() {
    Presenter prodPresenter = new Presenter(view, ConfigurationManager.getInstance());
    prodPresenter.resetCategoryVisibility();

    ComponentCategoryAction catAction =
        ActionFactory.getInstance().createComponentCategoryAction(prodPresenter, "Tubes");
    Assert.assertEquals(true, catAction.getValue(javax.swing.AbstractAction.SELECTED_KEY));

    // Toggle off
    catAction.putValue(javax.swing.AbstractAction.SELECTED_KEY, false);
    catAction.actionPerformed(null);
    Assert.assertFalse(prodPresenter.isCategoryEnabled("Tubes"));

    // Reset via ResetCategoriesAction
    ResetCategoriesAction resetAction =
        ActionFactory.getInstance().createResetCategoriesAction(prodPresenter);
    resetAction.actionPerformed(null);
    Assert.assertTrue(prodPresenter.isCategoryEnabled("Tubes"));
    Assert.assertEquals(true, catAction.getValue(javax.swing.AbstractAction.SELECTED_KEY));
  }

  @Test
  public void testCustomTreeModelRefresh() {
    presenter.resetCategoryVisibility();
    CustomTreeModel treeModel = new CustomTreeModel(presenter);
    Assert.assertTrue(presenter.isCategoryEnabled("Tubes"));

    presenter.setCategoryEnabled("Tubes", false);
    treeModel.refresh();
    Assert.assertFalse(presenter.getVisibleComponentTypes().containsKey("Tubes"));

    presenter.resetCategoryVisibility();
    treeModel.refresh();
    Assert.assertTrue(presenter.getVisibleComponentTypes().containsKey("Tubes"));
  }
  
//  @Test
//  public void testLoadVariants() {
//    try {
//      ComponentType resistorType = presenter.getComponentTypes().get("Passive").stream().filter(x -> x.getName().equals("Resistor")).findAny().get();
//      List<Template> variantsFor = presenter.getVariantsFor(resistorType);
//      Assert.assertEquals(0, variantsFor.size());
//      presenter.importVariants("./test/resources/variants_test.xml");
//      variantsFor = presenter.getVariantsFor(resistorType);
//      Assert.assertTrue(!variantsFor.isEmpty());
//    } catch (IOException e) {
//      Assert.fail(e.getMessage());
//    }
//  }
//
//  @Test
//  public void testLoadBlocks() {
//    try {
//      int blocks = presenter.importBlocks("./test/resources/blocks_test.xml");
//      Assert.assertTrue(blocks > 0);
//    } catch (IOException e) {
//      Assert.fail(e.getMessage());
//    }
//  }
}
