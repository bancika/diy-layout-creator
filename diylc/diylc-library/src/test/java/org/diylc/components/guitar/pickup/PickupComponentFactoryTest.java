package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.JazzBassPickup;
import org.diylc.components.guitar.PBassPickup;
import org.diylc.components.guitar.P90Pickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.StingrayBassPickup;

public class PickupComponentFactoryTest {

  @Test
  public void testGetFormatForEveryComponentClass() {
    assertEquals(PickupFormat.SINGLE_COIL, PickupComponentFactory.getFormat(new SingleCoilPickup()));
    assertEquals(PickupFormat.HUMBUCKER, PickupComponentFactory.getFormat(new HumbuckerPickup()));
    assertEquals(PickupFormat.P90, PickupComponentFactory.getFormat(new P90Pickup()));
    assertEquals(PickupFormat.JAZZ_BASS, PickupComponentFactory.getFormat(new JazzBassPickup()));
    assertEquals(PickupFormat.PRECISION_BASS, PickupComponentFactory.getFormat(new PBassPickup()));
    assertEquals(PickupFormat.STINGRAY_BASS, PickupComponentFactory.getFormat(new StingrayBassPickup()));
  }

  @Test
  public void testGetComponentClassIsTheInverseOfGetFormat() {
    for (PickupFormat format : PickupFormat.values()) {
      Class<?> componentClass = PickupComponentFactory.getComponentClass(format);
      assertEquals(format, PickupComponentFactory.getFormat(componentClass.asSubclass(
          org.diylc.components.guitar.AbstractGuitarPickup.class)));
    }
  }

  @Test
  public void testNullInputsReturnNull() {
    assertNull(PickupComponentFactory.getFormat((org.diylc.components.guitar.AbstractGuitarPickup) null));
    assertNull(PickupComponentFactory.getComponentClass(null));
  }

  @Test
  public void testIsSameType() {
    HumbuckerPickup humbucker = new HumbuckerPickup();
    assertTrue(PickupComponentFactory.isSameType(humbucker, PickupFormat.HUMBUCKER));
    assertFalse(PickupComponentFactory.isSameType(humbucker, PickupFormat.SINGLE_COIL));
    assertFalse(PickupComponentFactory.isSameType(null, PickupFormat.HUMBUCKER));
  }
}
