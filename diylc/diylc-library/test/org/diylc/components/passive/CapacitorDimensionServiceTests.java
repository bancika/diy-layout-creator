package org.diylc.components.passive;

import org.diylc.components.passive.CapacitorDatasheetService.CapacitorDatasheet;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;
import org.diylc.core.measures.VoltageUnit;
import org.junit.Assert;
import org.junit.Test;

public class CapacitorDimensionServiceTests {

  @Test
  public void testMissingType() {
    Assert.assertNull(CapacitorDatasheetService.getInstance().lookup(AxialFilmCapacitor.class,
        "bla", new Voltage(100d, VoltageUnit.V), new Capacitance(10d, CapacitanceUnit.uF)));
  }

  @Test
  public void testMissingCategory() {
    Assert.assertNull(CapacitorDatasheetService.getInstance().lookup(AxialFilmCapacitor.class,
        "Mallory 150", new Voltage(500d, VoltageUnit.V), new Capacitance(10d, CapacitanceUnit.nF)));
  }

  @Test
  public void testMissingValue() {
    Assert.assertNull(CapacitorDatasheetService.getInstance().lookup(AxialFilmCapacitor.class,
        "Mallory 150", new Voltage(600d, VoltageUnit.V), new Capacitance(10d, CapacitanceUnit.uF)));
  }

  @Test
  public void testExactValue63v() {       
    CapacitorDatasheet dim = CapacitorDatasheetService.getInstance().lookup(AxialFilmCapacitor.class,
        "Mallory 150", new Voltage(63d, VoltageUnit.V), new Capacitance(0.22d, CapacitanceUnit.uF));
    Assert.assertNotNull(dim);
    Assert.assertEquals(new Size(16.5, SizeUnit.mm).convertToPixels(), dim.getLength(), 0.0001);
    Assert.assertEquals(new Size(6d, SizeUnit.mm).convertToPixels(), dim.getWidth(), 0.0001);
  }

  @Test
  public void testCloseValue() {
    CapacitorDatasheet dim = CapacitorDatasheetService.getInstance().lookup(AxialFilmCapacitor.class,
        "Mallory 150", new Voltage(63d, VoltageUnit.V), new Capacitance(0.23d, CapacitanceUnit.uF));
    Assert.assertNotNull(dim);
    Assert.assertEquals(new Size(16.5, SizeUnit.mm).convertToPixels(), dim.getLength(), 0.0001);
    Assert.assertEquals(new Size(6d, SizeUnit.mm).convertToPixels(), dim.getWidth(), 0.0001);
  }
}
