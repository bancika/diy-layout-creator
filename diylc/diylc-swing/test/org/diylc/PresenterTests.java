package org.diylc;

import java.awt.Point;

import org.junit.Test;

import junit.framework.Assert;

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
}
