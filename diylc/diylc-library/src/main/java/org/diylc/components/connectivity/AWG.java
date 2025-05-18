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
package org.diylc.components.connectivity;

public enum AWG {

  _8, _10, _12, _14, _16, _18, _20, _22, _24, _26, _28, _30, _32, _34, _36;

  @Override
  public String toString() {
    return "#" + name().replace("_", "") + " / " + String.format("%1$,.2f", diameterIn() * 25.4) + "mm";
  }

  public double diameterIn() {
    return Math.pow(Math.E, -1.12436 - 0.11594 * getValue());
  }

  public int getValue() {
    return Integer.parseInt(name().replace("_", ""));
  }
}
