package org.diylc;

import java.awt.Point;
import java.io.IOException;
import java.util.List;
import org.diylc.common.ComponentType;
import org.diylc.core.Template;
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
  
  @Test
  public void testLoadVariants() {
    try {
      ComponentType resistorType = presenter.getComponentTypes().get("Passive").stream().filter(x -> x.getName().equals("Resistor")).findAny().get();
      List<Template> variantsFor = presenter.getVariantsFor(resistorType);
      Assert.assertEquals(0, variantsFor.size());
      presenter.importVariants("./test/resources/variants_test.xml");
      variantsFor = presenter.getVariantsFor(resistorType);
      Assert.assertTrue(!variantsFor.isEmpty());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
  
  @Test
  public void testLoadBlocks() {
    try {                  
      int blocks = presenter.importBlocks("./test/resources/blocks_test.xml");
      Assert.assertTrue(blocks > 0);     
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
