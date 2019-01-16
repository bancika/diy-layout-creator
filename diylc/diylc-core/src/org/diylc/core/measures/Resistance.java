/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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

import java.awt.Color;

import org.diylc.common.ResistorColorCode;

public class Resistance extends AbstractMeasure<ResistanceUnit> {

  private static final long serialVersionUID = 1L;

  private static final Color[] COLOR_DIGITS = new Color[] {Color.black, Color.decode("#8B4513"), Color.red,
      Color.orange, Color.yellow, Color.decode("#76EE00"), Color.blue, Color.decode("#91219E"), Color.lightGray,
      Color.white};
  private static final Color[] COLOR_MULTIPLIER = new Color[] {Color.lightGray.brighter(), Color.decode("#FFB90F"),
      Color.black, Color.decode("#8B4513"), Color.red, Color.orange, Color.yellow, Color.decode("#76EE00"), Color.blue};

  // public Resistance() {
  // super();
  // // TODO Auto-generated constructor stub
  // }

  public Resistance(Double value, ResistanceUnit multiplier) {
    super(value, multiplier);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Resistance clone() throws CloneNotSupportedException {
    return new Resistance(value, unit);
  }

  public Color[] getColorCode(ResistorColorCode resistorColorCode) {
    Color[] bands;
    if (getValue() == 0) {
      switch (resistorColorCode) {
        case _4_BAND:
          bands = new Color[3];
          bands[0] = COLOR_DIGITS[0];
          bands[1] = COLOR_DIGITS[0];
          bands[2] = COLOR_MULTIPLIER[2];
          break;
        case _5_BAND:
          bands = new Color[4];
          bands[0] = COLOR_DIGITS[0];
          bands[1] = COLOR_DIGITS[0];
          bands[2] = COLOR_DIGITS[0];
          bands[3] = COLOR_MULTIPLIER[2];
          break;
        default:
          bands = new Color[] {};
      }
    } else {
      if (getValue() == null || getUnit() == null)
        return new Color[] {};
      double base = getValue() * getUnit().getFactor();
      int multiplier = 0;
      while (base > (resistorColorCode == ResistorColorCode._4_BAND ? 99 : 999)) {
        multiplier += 1;
        base /= 10;
      }
      while (base < (resistorColorCode == ResistorColorCode._4_BAND ? 10 : 100)) {
        multiplier -= 1;
        base *= 10;
      }
      if (multiplier > 6 || multiplier < -2) {
        // Out of range.
        return new Color[] {};
      }
      switch (resistorColorCode) {
        case _4_BAND:
          bands = new Color[3];
          bands[0] = COLOR_DIGITS[(int) (base / 10)];
          bands[1] = COLOR_DIGITS[(int) (base % 10)];
          bands[2] = COLOR_MULTIPLIER[multiplier + 2];
          break;
        case _5_BAND:
          bands = new Color[4];
          bands[0] = COLOR_DIGITS[(int) (base / 100)];
          bands[1] = COLOR_DIGITS[(int) (base / 10 % 10)];
          bands[2] = COLOR_DIGITS[(int) (base % 10)];
          bands[3] = COLOR_MULTIPLIER[multiplier + 2];
          break;
        default:
          bands = new Color[] {};
      }
    }
    return bands;
  }

  public static Resistance parseResistance(String value) {
    value = value.replace("*", "").replace("R", "Ω");
    for (ResistanceUnit unit : ResistanceUnit.values()) {
      if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
        value = value.substring(0, value.length() - unit.toString().length()).trim();
        return new Resistance(parse(value), unit);
      }
    }
    throw new IllegalArgumentException("Could not parse resistance: " + value);
  }
}
