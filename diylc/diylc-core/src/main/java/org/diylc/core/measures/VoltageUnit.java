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

public enum VoltageUnit implements Unit {

  // only volts have an RKM form (6V3, 3V3); 1m5 or 1k5 would not say that the value is a voltage
  MV(1e-3, "mV", null), V(1, "V", "V"), KV(1e3, "KV", null);

  double factor;
  String display;
  String rkmSymbol;

  private VoltageUnit(double factor, String display, String rkmSymbol) {
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
   * Resolves the unit written after a voltage value: "V", "mV" or "kV", case-insensitive.
   * 
   * @param symbol unit text with no surrounding whitespace
   * @return the unit, or null when the text is not a voltage unit
   */
  public static VoltageUnit fromSymbol(String symbol) {
    switch (symbol.toLowerCase()) {
      case "v":
        return V;
      case "mv":
        return MV;
      case "kv":
        return KV;
      default:
        return null;
    }
  }
}
