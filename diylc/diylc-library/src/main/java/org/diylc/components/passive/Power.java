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
package org.diylc.components.passive;

import org.diylc.core.measures.PowerUnit;

public enum Power {

  EIGHT("1/8W"), QUARTER("1/4W"), HALF("1/2W"), ONE("1W"), TWO("2W"), FIVE("5W"), TEN("10W"), FIFTY("50W"), HUNDRED(
      "100W");

  String label;

  Power(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }

  public org.diylc.core.measures.Power convertToNewFormat() {
    org.diylc.core.measures.Power powerNew;
    switch (this) {
      case EIGHT:
        powerNew = new org.diylc.core.measures.Power(1 / 8d, PowerUnit.W);
        break;
      case FIVE:
        powerNew = new org.diylc.core.measures.Power(5d, PowerUnit.W);
        break;
      case FIFTY:
        powerNew = new org.diylc.core.measures.Power(50d, PowerUnit.W);
        break;
      case HALF:
        powerNew = new org.diylc.core.measures.Power(1 / 2d, PowerUnit.W);
        break;
      case HUNDRED:
        powerNew = new org.diylc.core.measures.Power(100d, PowerUnit.W);
        break;
      case ONE:
        powerNew = new org.diylc.core.measures.Power(1d, PowerUnit.W);
        break;
      case QUARTER:
        powerNew = new org.diylc.core.measures.Power(1 / 4d, PowerUnit.W);
        break;
      case TEN:
        powerNew = new org.diylc.core.measures.Power(10d, PowerUnit.W);
        break;
      case TWO:
        powerNew = new org.diylc.core.measures.Power(2d, PowerUnit.W);
        break;
      default:
        powerNew = new org.diylc.core.measures.Power(1 / 2d, PowerUnit.W);
    }
    return powerNew;
  }
}
