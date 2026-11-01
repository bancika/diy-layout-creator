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

import org.diylc.components.semiconductors.MultiwattIC.MultiwattType;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Pin numbering, pin names and lead pitch of the staggered power packages. Control points run down
 * the forward row and then down the row under the body, while the pins are numbered alternately
 * between them.
 */
public class MultiwattICTest {

  @Test
  public void pinsAreNumberedAlternatelyBetweenRows() {
    assertNodeNames(ic(MultiwattType.MULTIWATT_11, null), "1", "3", "5", "7", "9", "11", "2", "4",
        "6", "8", "10");
  }

  @Test
  public void namesFollowPinNumbers() {
    assertNodeNames(ic(MultiwattType.MULTIWATT_11, "A,B,C,D,E,F,G,H,I,J,K"), "A", "C", "E", "G",
        "I", "K", "B", "D", "F", "H", "J");
  }

  /** Consecutive leads alternate rows, so pin 2 sits one lead pitch along from pin 1. */
  @Test
  public void leadPitchFollowsThePackage() {
    assertLeadPitch(MultiwattType.MULTIWATT_11, new Size(1.7d, SizeUnit.mm));
    assertLeadPitch(MultiwattType.MULTIWATT_15, new Size(1.27d, SizeUnit.mm));
  }

  /** Multiwatt 15 keeps the 0.1 in row pitch it always had, so existing layouts do not move. */
  @Test
  public void multiwatt15KeepsItsRowPitch() {
    MultiwattIC ic = ic(MultiwattType.MULTIWATT_15, null);
    Assert.assertEquals(new Size(0.1d, SizeUnit.in).convertToPixels(),
        ic.getControlPoint(1).getY() - ic.getControlPoint(0).getY(), 1d);
  }

  @Test
  public void modelPicksThePackageByPinCount() {
    MultiwattIC ic = new MultiwattIC(new String[] {"Power Amp", "TEST", "A", "B", "C", "D", "E",
        "F", "G", "H", "I", "J", "K"});
    Assert.assertEquals(MultiwattType.MULTIWATT_11, ic.getType());
    Assert.assertEquals("TEST", ic.getValue());
    // control point 6 opens the second row, which holds pin 2
    Assert.assertEquals("B", ic.getControlPointNodeName(6));
  }

  private static void assertLeadPitch(MultiwattType type, Size expected) {
    MultiwattIC ic = ic(type, null);
    // with the default orientation the rows run down, so the lead pitch is a y distance
    double pitch = ic.getControlPoint(type.getPinsLeft()).getY() - ic.getControlPoint(0).getY();
    Assert.assertEquals(type.toString(), expected.convertToPixels(), pitch, 1d);
  }

  private static MultiwattIC ic(MultiwattType type, String pinNames) {
    MultiwattIC ic = new MultiwattIC();
    ic.setType(type);
    ic.setPinNames(pinNames);
    return ic;
  }

  private static void assertNodeNames(MultiwattIC ic, String... expected) {
    String[] actual = new String[ic.getControlPointCount()];
    for (int i = 0; i < actual.length; i++) {
      actual[i] = ic.getControlPointNodeName(i);
    }
    Assert.assertArrayEquals(expected, actual);
  }
}
