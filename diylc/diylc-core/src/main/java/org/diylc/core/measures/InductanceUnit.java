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

public enum InductanceUnit implements Unit {

  pH(1, "pH", "p"), nH(1e3, "nH", "n"), uH(1e6, "uH", "u"), mH(1e9, "mH", "m"), H(1e12, "H", "H");

  double factor;
  String display;
  String rkmSymbol;

  private InductanceUnit(double factor, String display, String rkmSymbol) {
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
   * Resolves the unit written after an inductance value, e.g. "uH", "u", "\u00B5H" or "H".
   * Case does not matter when the trailing "H" is present. Without it, "M" is rejected so that it
   * is never mistaken for mega. "R" is not accepted: inductor makers print 4R7 for 4.7uH, while
   * in RKM it would mean 4.7H.
   * 
   * @param symbol unit text with no surrounding whitespace
   * @return the unit, or null when the text is not an inductance unit
   */
  public static InductanceUnit fromSymbol(String symbol) {
    if (symbol.equalsIgnoreCase("H")) {
      return H;
    }
    String prefix = symbol;
    if (symbol.length() == 2 && (symbol.endsWith("H") || symbol.endsWith("h"))) {
      prefix = symbol.substring(0, 1).toLowerCase();
    } else if (symbol.equals("M")) {
      return null;
    }
    switch (prefix) {
      case "p":
      case "P":
        return pH;
      case "n":
      case "N":
        return nH;
      case "u":
      case "U":
      case "\u00B5":
      case "\u03BC":
        return uH;
      case "m":
        return mH;
      default:
        return null;
    }
  }
}
