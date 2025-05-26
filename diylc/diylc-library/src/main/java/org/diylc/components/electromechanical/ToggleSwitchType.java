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
package org.diylc.components.electromechanical;

public enum ToggleSwitchType {

  SPST("SPST", 2),
  SPDT("SPDT (On/On)", 2),
  SPDT_off("SPDT (On/Off/On)", 3),
  DPDT("DPDT (On/On)", 2),
  DPDT_off("DPDT (On/Off/On)", 3),
  DPDT_ononon_1("DPDT (On/On/On Type 1)", 3),
  DPDT_ononon_2("DPDT (On/On/On Type 2)", 3),
  _3PDT("3PDT", 2),
  _3PDT_off("3PDT (On/Off/On)", 3),
  _4PDT("4PDT", 2),
  _4PDT_off("4PDT (On/Off/On)", 3),
  _4PDT_ononon_1("4PDT (On/On/On Type 1)", 3),
  _4PDT_ononon_2("4PDT (On/On/On Type 2)", 3),
  _5PDT("5PDT", 2),
  _5PDT_off("5PDT (On/Off/On)", 3);
  
  private String label;
  private final int positionCount;

  private ToggleSwitchType(String label, int positionCount) {
    this.label = label;
    this.positionCount = positionCount;
  }

  public int getPositionCount() {
    return positionCount;
  }

  @Override
  public String toString() {
    return label;
  }
}
