/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.diylc.common.PropertyWrapper;
import org.diylc.components.electromechanical.DIPSwitch;
import org.diylc.components.electromechanical.RotarySwitchSealed;
import org.diylc.components.electromechanical.SwitchLatchingSymbol;
import org.diylc.components.guitar.LPSwitch;
import org.diylc.components.guitar.S1Switch;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDynamicPropertySource;
import org.diylc.core.ISwitch;
import org.diylc.presenter.ComponentProcessor;

/**
 * Covers the switches that gained a selected position, which until now always reported none and so
 * never closed a connection outside an explicit position walk.
 *
 * @author Branislav Stojkovic
 */
public class SelectedPositionTests {

  @Test
  public void switchLatchingSymbolRemembersItsPosition() {
    assertPositionIsSelectable(new SwitchLatchingSymbol());
  }

  @Test
  public void dipSwitchRemembersItsPosition() {
    assertPositionIsSelectable(new DIPSwitch());
  }

  @Test
  public void rotarySwitchSealedRemembersItsPosition() {
    assertPositionIsSelectable(new RotarySwitchSealed());
  }

  @Test
  public void lpSwitchRemembersItsPosition() {
    assertPositionIsSelectable(new LPSwitch());
  }

  @Test
  public void s1SwitchRemembersItsPosition() {
    assertPositionIsSelectable(new S1Switch());
  }

  /**
   * A switch starts with no position selected and closes nothing. Once a position is set, the
   * connections it reports without a position argument are the ones that position makes.
   */
  private void assertPositionIsSelectable(ISwitch component) {
    assertNull("a switch starts with no position selected", component.getSelectedPosition());

    IDIYComponent<?> instance = (IDIYComponent<?>) component;
    int pointCount = instance.getControlPointCount();
    for (int i = 0; i < pointCount - 1; i++) {
      for (int j = i + 1; j < pointCount; j++) {
        assertFalse("nothing is connected before a position is picked",
            component.arePointsConnected(i, j));
      }
    }

    for (int position = 0; position < component.getPositionCount(); position++) {
      component.setSelectedPosition(position);
      assertEquals(Integer.valueOf(position), component.getSelectedPosition());
      for (int i = 0; i < pointCount - 1; i++) {
        for (int j = i + 1; j < pointCount; j++) {
          assertEquals("position " + position + " between " + i + " and " + j,
              component.arePointsConnected(i, j, position), component.arePointsConnected(i, j));
        }
      }
    }

    assertOffersEveryPosition(component);
  }

  /** The property editor must offer "none" plus each position the switch reports. */
  private void assertOffersEveryPosition(ISwitch component) {
    List<PropertyWrapper> properties =
        ComponentProcessor.getInstance().extractProperties(component.getClass());
    PropertyWrapper property = properties.stream()
        .filter(p -> "Selected Position".equals(p.getName())).findFirst().orElse(null);
    assertNotNull("the switch should expose a Selected Position property", property);

    IDynamicPropertySource source = property.getDynamicPropertySource();
    assertNotNull("the property should offer its values dynamically", source);
    source.setProperties(properties);
    source.setComponent((IDIYComponent<?>) component);
    for (PropertyWrapper wrapper : properties) {
      try {
        wrapper.readFrom(component);
      } catch (Exception e) {
        // a property we cannot read is not one the source keys off
      }
    }

    List<Object> values = source.getAvailableValues();
    assertEquals("none plus every position", component.getPositionCount() + 1, values.size());
    assertNull("the first offer is no position at all", values.getFirst());
    assertTrue(values.contains(component.getPositionCount() - 1));
    assertFalse(values.contains(component.getPositionCount()));
  }
}
