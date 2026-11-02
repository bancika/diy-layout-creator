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
package org.diylc.components.semiconductors;

import org.junit.Assert;
import org.junit.Test;

public class TransistorPinoutTest {

  @Test
  public void bjtPinsMapToCollectorBaseEmitter() {
    assertSpiceNodes(TransistorPinout.BJT_EBC, 2, 1, 0);
    assertSpiceNodes(TransistorPinout.BJT_CBE, 0, 1, 2);
    assertSpiceNodes(TransistorPinout.BJT_BCE, 1, 0, 2);
    assertSpiceNodes(TransistorPinout.BJT_ECB, 2, 0, 1);
  }

  @Test
  public void fetPinsMapToDrainGateSource() {
    assertSpiceNodes(TransistorPinout.JFET_DSG, 0, 2, 1);
    assertSpiceNodes(TransistorPinout.JFET_GSD, 1, 2, 0);
    assertSpiceNodes(TransistorPinout.JFET_SGD, 2, 1, 0);
    assertSpiceNodes(TransistorPinout.MOSFET_GDS, 1, 0, 2);
  }

  @Test
  public void regulatorPinsKeepTheirOrder() {
    assertSpiceNodes(TransistorPinout.REGULATOR_IGO, 0, 1, 2);
  }

  /** Every value names three distinct pins with letters its type knows. */
  @Test
  public void everyPinoutIsAPermutationOfItsRoles() {
    for (TransistorPinout pinout : TransistorPinout.values()) {
      String pins = pinout.toPinout();
      Assert.assertEquals(pinout.name(), 3, pins.length());
      Assert.assertEquals(pinout.name(), 3, pins.chars().distinct().count());
      String roles = pinout.name().startsWith("BJT") ? "BCE"
          : pinout.name().startsWith("REGULATOR") ? "IOGA" : "DGS";
      for (char c : pins.toCharArray()) {
        Assert.assertTrue(pinout.name(), roles.indexOf(c) >= 0);
      }
    }
  }

  private static void assertSpiceNodes(TransistorPinout pinout, int... expected) {
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(pinout + " pin " + i, expected[i], pinout.mapToSpiceNode(i));
    }
  }
}
