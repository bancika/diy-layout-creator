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

  public static Voltage parseCapacitance(String value) {
    value = value.replace("*", "");
    for (VoltageUnit unit : VoltageUnit.values()) {
      if (value.toLowerCase().endsWith(unit.toString().toLowerCase())) {
        value = value.substring(0, value.length() - unit.toString().length()).trim();
        return new Voltage(parse(value), unit);
      }
    }
    throw new IllegalArgumentException("Could not parse voltage: " + value);
  }
}
