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
package org.diylc.core.measures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;

public class ValueNotationTest {

  private static final double DELTA = 1e-9;

  @After
  public void resetNotation() {
    ValueNotation.setCurrent(ValueNotation.DECIMAL);
  }

  // --- formatting ---

  @Test
  public void decimalResistanceIsUnchanged() {
    assertEquals("4.7\u03A9", new Resistance(4.7, ResistanceUnit.R).toString());
    assertEquals("0.22\u03A9", new Resistance(0.22, ResistanceUnit.R).toString());
    assertEquals("4.7K", new Resistance(4.7, ResistanceUnit.K).toString());
    assertEquals("1M", new Resistance(1d, ResistanceUnit.M).toString());
  }

  @Test
  public void decimalCapacitanceIsUnchanged() {
    assertEquals("0.22uF", new Capacitance(0.22, CapacitanceUnit.uF).toString());
    assertEquals("100nF", new Capacitance(100d, CapacitanceUnit.nF).toString());
  }

  @Test
  public void rkmResistance() {
    ValueNotation.setCurrent(ValueNotation.RKM);
    assertEquals("4R7", new Resistance(4.7, ResistanceUnit.R).toString());
    assertEquals("0R22", new Resistance(0.22, ResistanceUnit.R).toString());
    assertEquals("100R", new Resistance(100d, ResistanceUnit.R).toString());
    assertEquals("4K7", new Resistance(4.7, ResistanceUnit.K).toString());
    assertEquals("47K", new Resistance(47d, ResistanceUnit.K).toString());
    assertEquals("1M", new Resistance(1d, ResistanceUnit.M).toString());
    assertEquals("4700R", new Resistance(4700d, ResistanceUnit.R).toString());
    assertEquals("0R", new Resistance(0d, ResistanceUnit.R).toString());
    assertEquals("4K7501", new Resistance(4.75012, ResistanceUnit.K).toString());
  }

  @Test
  public void rkmCapacitance() {
    ValueNotation.setCurrent(ValueNotation.RKM);
    assertEquals("0u22", new Capacitance(0.22, CapacitanceUnit.uF).toString());
    assertEquals("2n2", new Capacitance(2.2, CapacitanceUnit.nF).toString());
    assertEquals("100n", new Capacitance(100d, CapacitanceUnit.nF).toString());
    assertEquals("4p7", new Capacitance(4.7, CapacitanceUnit.pF).toString());
    assertEquals("1000u", new Capacitance(1000d, CapacitanceUnit.uF).toString());
  }

  // --- parsing ---

  @Test
  public void parsesDecimalResistance() {
    assertResistance(4.7, ResistanceUnit.K, "4.7K");
    assertResistance(4.7, ResistanceUnit.K, "4.7k");
    assertResistance(4.7, ResistanceUnit.K, "4.7 K");
    assertResistance(4.7, ResistanceUnit.K, "4,7K");
    assertResistance(4.7, ResistanceUnit.K, "4.7kohm");
    assertResistance(4.7, ResistanceUnit.R, "4.7ohm");
    assertResistance(4.7, ResistanceUnit.R, "4.7 ohms");
    assertResistance(4.7, ResistanceUnit.R, "4.7\u03A9");
    assertResistance(4.7, ResistanceUnit.R, "4.7\u2126");
    assertResistance(4.7, ResistanceUnit.K, "4.7K\u03A9");
    assertResistance(1, ResistanceUnit.M, "1M");
    assertResistance(1, ResistanceUnit.M, "1m");
    assertResistance(2.2, ResistanceUnit.M, "2.2Meg");
    assertResistance(100, ResistanceUnit.R, "100R");
    assertResistance(100, ResistanceUnit.R, "100r");
    assertResistance(0.5, ResistanceUnit.K, ".5K");
    assertResistance(10, ResistanceUnit.K, "10K*");
  }

  @Test
  public void parsesRkmResistance() {
    assertResistance(4.7, ResistanceUnit.R, "4R7");
    assertResistance(0.22, ResistanceUnit.R, "0R22");
    assertResistance(0.22, ResistanceUnit.R, "R22");
    assertResistance(4.7, ResistanceUnit.K, "4K7");
    assertResistance(4.7, ResistanceUnit.K, "4k7");
    assertResistance(4.75, ResistanceUnit.K, "4K75");
    assertResistance(2.2, ResistanceUnit.M, "2M2");
  }

  @Test
  public void parsesDecimalCapacitance() {
    assertCapacitance(0.22, CapacitanceUnit.uF, "0.22uF");
    assertCapacitance(0.22, CapacitanceUnit.uF, "0.22UF");
    assertCapacitance(0.22, CapacitanceUnit.uF, "0.22u");
    assertCapacitance(220, CapacitanceUnit.nF, "220 nF");
    assertCapacitance(220, CapacitanceUnit.nF, "220n");
    assertCapacitance(47, CapacitanceUnit.pF, "47pf");
    assertCapacitance(4.7, CapacitanceUnit.uF, "4.7\u00B5F");
    assertCapacitance(1, CapacitanceUnit.F, "1F");
    assertCapacitance(1, CapacitanceUnit.mF, "1mF");
    assertCapacitance(1, CapacitanceUnit.mF, "1MF");
    assertCapacitance(1, CapacitanceUnit.mF, "1m");
  }

  @Test
  public void parsesRkmCapacitance() {
    assertCapacitance(0.22, CapacitanceUnit.uF, "0u22");
    assertCapacitance(0.22, CapacitanceUnit.uF, "u22");
    assertCapacitance(2.2, CapacitanceUnit.nF, "2n2");
    assertCapacitance(2.2, CapacitanceUnit.nF, "2n2F");
    assertCapacitance(4.7, CapacitanceUnit.uF, "4u7");
    assertCapacitance(4.7, CapacitanceUnit.uF, "4\u00B57");
    assertCapacitance(4.7, CapacitanceUnit.uF, "4\u03BC7");
    assertCapacitance(4.7, CapacitanceUnit.pF, "4p7");
  }

  @Test
  public void bareNumberHasNoUnit() {
    ValueNotation.ParsedMeasure<ResistanceUnit> parsed =
        ValueNotation.parse("4.7", ResistanceUnit::fromSymbol);
    assertEquals(4.7, parsed.value(), DELTA);
    assertNull(parsed.unit());
  }

  @Test
  public void rejectsInvalidText() {
    assertRejected("4u7", ResistanceUnit.class);
    assertRejected("4K7K", ResistanceUnit.class);
    assertRejected("K", ResistanceUnit.class);
    assertRejected("", ResistanceUnit.class);
    assertRejected("100", ResistanceUnit.class);
    assertRejected("4R7", CapacitanceUnit.class);
    assertRejected("1M", CapacitanceUnit.class);
    assertRejected("4M7", CapacitanceUnit.class);
  }

  @Test
  public void roundTripsInBothNotations() {
    double[] values = {0.22, 1, 4.7, 47, 100, 4700, 0.0047};
    for (ValueNotation notation : ValueNotation.values()) {
      ValueNotation.setCurrent(notation);
      for (double value : values) {
        for (ResistanceUnit unit : ResistanceUnit.values()) {
          Resistance resistance = new Resistance(value, unit);
          assertEquals(resistance, Resistance.parseResistance(resistance.toString()));
        }
        for (CapacitanceUnit unit : CapacitanceUnit.values()) {
          Capacitance capacitance = new Capacitance(value, unit);
          assertEquals(capacitance, Capacitance.parseCapacitance(capacitance.toString()));
        }
      }
    }
  }

  private static void assertResistance(double value, ResistanceUnit unit, String text) {
    Resistance parsed = Resistance.parseResistance(text);
    assertEquals(text, value, parsed.getValue(), DELTA);
    assertEquals(text, unit, parsed.getUnit());
  }

  private static void assertCapacitance(double value, CapacitanceUnit unit, String text) {
    Capacitance parsed = Capacitance.parseCapacitance(text);
    assertEquals(text, value, parsed.getValue(), DELTA);
    assertEquals(text, unit, parsed.getUnit());
  }

  private static void assertRejected(String text, Class<?> unitType) {
    try {
      if (unitType == ResistanceUnit.class) {
        Resistance.parseResistance(text);
      } else {
        Capacitance.parseCapacitance(text);
      }
      fail("Expected " + text + " to be rejected");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
