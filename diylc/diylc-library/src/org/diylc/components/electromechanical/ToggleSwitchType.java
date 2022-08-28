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
package org.diylc.components.electromechanical;

public enum ToggleSwitchType {

  SPST("SPST"), SPDT("SPDT (On/On)"), SPDT_off("SPDT (On/Off/On"), DPDT("DPDT (On/On)"),DPDT_off("DPDT (On/Off/On"), 
  DPDT_ononon_1("DPDT (On/On/On Type 1)"), DPDT_ononon_2("DPDT (On/On/On Type 2)"),
  _DP3T_mustang("DP3T"), _3PDT("3PDT"),_3PDT_off("3PDT (On/Off/On)"), _4PDT("4PDT"), _4PDT_off("4PDT (On/Off/On)"), 
  _5PDT("5PDT"), _5PDT_off("5PDT (On/Off/On)");
  
  private String label;
  
  private ToggleSwitchType(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
