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

public enum ResistanceUnit implements Unit {

  R(1, "\u03A9", "R"), K(1e3, "K", "K"), M(1e6, "M", "M");

  double factor;
  String display;
  String rkmSymbol;

  private ResistanceUnit(double factor, String display, String rkmSymbol) {
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
   * Resolves the unit written after a resistance value, e.g. "K", "kohm", "R", "\u03A9" or "Meg".
   * A lower-case "m" means mega, because there is no milliohm unit and parts lists commonly write
   * "1m" for one megohm.
   * 
   * @param symbol unit text with no surrounding whitespace
   * @return the unit, or null when the text is not a resistance unit
   */
  public static ResistanceUnit fromSymbol(String symbol) {
    String prefix = symbol;
    String lower = symbol.toLowerCase();
    for (String ohm : new String[] {"ohms", "ohm", "\u03A9", "\u2126"}) {
      if (lower.endsWith(ohm.toLowerCase())) {
        prefix = symbol.substring(0, symbol.length() - ohm.length());
        if (prefix.isEmpty()) {
          return R;
        }
        break;
      }
    }
    switch (prefix.toLowerCase()) {
      case "r":
        return R;
      case "k":
        return K;
      case "m":
      case "meg":
        return M;
      default:
        return null;
    }
  }
}
