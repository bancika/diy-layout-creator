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

import java.util.HashSet;
import java.util.Set;

import org.diylc.components.semiconductors.DIL_IC.DisplayNumbers;
import org.diylc.components.semiconductors.DIL_IC.PinCount;
import org.diylc.netlist.Node;
import org.junit.Assert;
import org.junit.Test;

/**
 * Pin numbering and pin names of the DIP IC. Control points run down the first row and then down
 * the second, so on an 8-pin part control point 4 is pin 8.
 */
public class DIL_ICTest {

  private static final String TL072 = "1OUT,1IN-,1IN+,VCC-,2IN+,2IN-,2OUT,VCC+";

  @Test
  public void unnumberedPartReportsDipPinNumbers() {
    DIL_IC ic = ic(PinCount._8, DisplayNumbers.NO, null);
    assertNodeNames(ic, "1", "2", "3", "4", "8", "7", "6", "5");
  }

  @Test
  public void numberingSchemes() {
    assertNodeNames(ic(PinCount._8, DisplayNumbers.NAMES, null), "1", "2", "3", "4", "8", "7", "6",
        "5");
    assertNodeNames(ic(PinCount._8, DisplayNumbers.DIP, null), "1", "2", "3", "4", "8", "7", "6",
        "5");
    assertNodeNames(ic(PinCount._8, DisplayNumbers.DIP_MIRROR, null), "8", "7", "6", "5", "1",
        "2", "3", "4");
    assertNodeNames(ic(PinCount._8, DisplayNumbers.CONNECTOR, null), "1", "3", "5", "7", "2", "4",
        "6", "8");
    assertNodeNames(ic(PinCount._8, DisplayNumbers.CONNECTOR_MIRROR, null), "2", "4", "6", "8",
        "1", "3", "5", "7");
  }

  @Test
  public void everySchemeNumbersEachPinOnce() {
    for (PinCount pinCount : PinCount.values()) {
      for (DisplayNumbers numbers : DisplayNumbers.values()) {
        DIL_IC ic = ic(pinCount, numbers, null);
        Set<String> seen = new HashSet<String>();
        for (int i = 0; i < ic.getControlPointCount(); i++) {
          String name = ic.getControlPointNodeName(i);
          int number = Integer.parseInt(name);
          Assert.assertTrue(pinCount + " " + numbers + " pin " + name,
              number >= 1 && number <= pinCount.getValue());
          Assert.assertTrue(pinCount + " " + numbers + " repeats " + name, seen.add(name));
        }
      }
    }
  }

  @Test
  public void newPartShowsNames() {
    Assert.assertEquals(DisplayNumbers.NAMES, new DIL_IC().getDisplayNumbers());
  }

  @Test
  public void namesFollowPinNumbers() {
    assertNodeNames(ic(PinCount._8, DisplayNumbers.NO, TL072), "1OUT", "1IN-", "1IN+", "VCC-",
        "VCC+", "2OUT", "2IN-", "2IN+");
  }

  /** Names belong to pins, so drawing the part from the solder side moves them with the numbers. */
  @Test
  public void namesFollowMirroredNumbering() {
    assertNodeNames(ic(PinCount._8, DisplayNumbers.DIP_MIRROR, TL072), "VCC+", "2OUT", "2IN-",
        "2IN+", "1OUT", "1IN-", "1IN+", "VCC-");
  }

  @Test
  public void missingAndBlankNamesFallBackToNumbers() {
    assertNodeNames(ic(PinCount._8, DisplayNumbers.NO, " TRIG , ,OUT"), "TRIG", "2", "OUT", "4",
        "8", "7", "6", "5");
    assertNodeNames(ic(PinCount._4, DisplayNumbers.NO, ""), "1", "2", "4", "3");
  }

  /** Surplus names are kept, so going back to the larger package restores them. */
  @Test
  public void surplusNamesSurvivePinCountChange() {
    DIL_IC ic = ic(PinCount._8, DisplayNumbers.NO, TL072);
    ic.setPinCount(PinCount._4);
    assertNodeNames(ic, "1OUT", "1IN-", "VCC-", "1IN+");
    ic.setPinCount(PinCount._8);
    Assert.assertEquals("VCC+", ic.getControlPointNodeName(4));
  }

  @Test
  public void numberedSuffixNamesOneNet() {
    DIL_IC ic = ic(PinCount._4, DisplayNumbers.NO, "GND_1,GND_2,1IN-,2IN-");
    Assert.assertEquals(Node.railName(ic.getControlPointNodeName(0)),
        Node.railName(ic.getControlPointNodeName(1)));
    Assert.assertNotEquals(Node.railName(ic.getControlPointNodeName(2)),
        Node.railName(ic.getControlPointNodeName(3)));
  }

  private static DIL_IC ic(PinCount pinCount, DisplayNumbers numbers, String pinNames) {
    DIL_IC ic = new DIL_IC();
    ic.setPinCount(pinCount);
    ic.setDisplayNumbers(numbers);
    ic.setPinNames(pinNames);
    return ic;
  }

  private static void assertNodeNames(DIL_IC ic, String... expected) {
    String[] actual = new String[ic.getControlPointCount()];
    for (int i = 0; i < actual.length; i++) {
      actual[i] = ic.getControlPointNodeName(i);
    }
    Assert.assertArrayEquals(expected, actual);
  }
}
