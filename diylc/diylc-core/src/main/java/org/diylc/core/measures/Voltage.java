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

public class Voltage extends AbstractMeasure<VoltageUnit> {

  private static final long serialVersionUID = 1L;

  // public Voltage() {
  // super();
  // // TODO Auto-generated constructor stub
  // }

  public Voltage(Double value, VoltageUnit unit) {
    super(value, unit);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Voltage clone() throws CloneNotSupportedException {
    return new Voltage(value, unit);
  }

  public static Voltage parseVoltage(String value) {
    ValueNotation.ParsedMeasure<VoltageUnit> parsed;
    try {
      parsed = ValueNotation.parse(value, VoltageUnit::fromSymbol);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Could not parse voltage: " + value, e);
    }
    if (parsed.unit() == null) {
      throw new IllegalArgumentException("Could not parse voltage: " + value);
    }
    return new Voltage(parsed.value(), parsed.unit());
  }
  
  public Double getNormalizedValue() {
    if (value == null || unit == null)
      return null;
    return value * unit.getFactor(); 
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
