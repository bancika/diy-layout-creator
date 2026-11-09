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

public enum CapacitanceUnit implements Unit {

  pF(1e-6, "pF", "p"), nF(1e-3, "nF", "n"), uF(1, "uF", "u"), mF(1e3, "mF", "m"), F(1e6, "F", "F");

  double factor;
  String display;
  String rkmSymbol;

  private CapacitanceUnit(double factor, String display, String rkmSymbol) {
    this.factor = factor;
    this.display = display;
    this.rkmSymbol = rkmSymbol;
  }

  @Override
  public double getFactor() {
    return factor;
  }

  public String getRkmSymbol() {
    return rkmSymbol;
  }

  @Override
  public String toString() {
    return display;
  }

  /**
   * Resolves the unit written after a capacitance value, e.g. "nF", "n", "u", "\u00B5F" or "F".
   * Case does not matter when the trailing "F" is present, which is how values have always been
   * parsed. Without it, "M" is rejected so that it is never mistaken for mega.
   * 
   * @param symbol unit text with no surrounding whitespace
   * @return the unit, or null when the text is not a capacitance unit
   */
  public static CapacitanceUnit fromSymbol(String symbol) {
    if (symbol.equalsIgnoreCase("F")) {
      return F;
    }
    String prefix = symbol;
    if (symbol.length() == 2 && (symbol.endsWith("F") || symbol.endsWith("f"))) {
      prefix = symbol.substring(0, 1).toLowerCase();
    } else if (symbol.equals("M")) {
      return null;
    }
    switch (prefix) {
      case "p":
      case "P":
        return pF;
      case "n":
      case "N":
        return nF;
      case "u":
      case "U":
      case "\u00B5":
      case "\u03BC":
        return uF;
      case "m":
        return mF;
      default:
        return null;
    }
  }
}
