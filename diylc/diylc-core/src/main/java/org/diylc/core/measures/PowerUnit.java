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

public enum PowerUnit implements Unit {

  MW(1e-1, "mW"), W(1, "W"), KW(1e3, "KW");

  double factor;
  String display;

  private PowerUnit(double factor, String display) {
    this.factor = factor;
    this.display = display;
  }

  @Override
  public double getFactor() {
    return factor;
  }

  @Override
  public String toString() {
    return display;
  }
}
