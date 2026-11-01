/*
 *
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.components.semiconductors;

import org.diylc.components.semiconductors.SIL_IC.PinCount;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class SIL_ICTest {

  @Test
  public void unnamedPinsAreNumberedFromTheAnchor() {
    SIL_IC ic = new SIL_IC();
    ic.setPinCount(PinCount._4);
    assertNodeNames(ic, "1", "2", "3", "4");
  }

  @Test
  public void missingAndBlankNamesFallBackToNumbers() {
    SIL_IC ic = new SIL_IC();
    ic.setPinCount(PinCount._4);
    ic.setPinNames("1IN, ,GND");
    assertNodeNames(ic, "1IN", "2", "GND", "4");
  }

  @Test
  public void modelSetsPitchCountAndNames() {
    SIL_IC ic = new SIL_IC(new String[] {"Power Amp", "TEST", "2 mm", "IN", "GND", "OUT"});
    Assert.assertEquals("TEST", ic.getValue());
    Assert.assertEquals(PinCount._3, ic.getPinCount());
    Assert.assertEquals(new Size(2d, SizeUnit.mm), ic.getPinSpacing());
    assertNodeNames(ic, "IN", "GND", "OUT");
  }

  @Test
  public void newPartShowsNames() {
    Assert.assertEquals(PinLabels.NAMES, new SIL_IC().getPinLabels());
  }

  private static void assertNodeNames(SIL_IC ic, String... expected) {
    String[] actual = new String[ic.getControlPointCount()];
    for (int i = 0; i < actual.length; i++) {
      actual[i] = ic.getControlPointNodeName(i);
    }
    Assert.assertArrayEquals(expected, actual);
  }
}
