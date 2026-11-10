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

public class Inductance extends AbstractMeasure<InductanceUnit> {

  private static final long serialVersionUID = 1L;

  // public Inductance() {
  // super();
  // // TODO Auto-generated constructor stub
  // }

  public Inductance(Double value, InductanceUnit unit) {
    super(value, unit);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Inductance clone() throws CloneNotSupportedException {
    return new Inductance(value, unit);
  }

  public static Inductance parseCapacitance(String value) {
    ValueNotation.ParsedMeasure<InductanceUnit> parsed;
    try {
      parsed = ValueNotation.parse(value, InductanceUnit::fromSymbol);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Could not parse inductance: " + value, e);
    }
    if (parsed.unit() == null) {
      throw new IllegalArgumentException("Could not parse inductance: " + value);
    }
    return new Inductance(parsed.value(), parsed.unit());
  }

  @Override
  public String toString() {
    if (ValueNotation.getCurrent() == ValueNotation.RKM && value != null && unit != null
        && unit.getRkmSymbol() != null) {
      return ValueNotation.formatRkm(value, unit.getRkmSymbol());
    }
    return super.toString();
  }
}
