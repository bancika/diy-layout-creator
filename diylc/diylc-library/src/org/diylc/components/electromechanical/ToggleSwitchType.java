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

  SPST, SPDT, DPDT, _DP3T_mustang, _3PDT, _4PDT, _5PDT, SPDT_off, DPDT_off, _3PDT_off, _4PDT_off, _5PDT_off, DPDT_ononon_1, DPDT_ononon_2;

  @Override
  public String toString() {
    String name = name();
    if (name.startsWith("_"))
      name = name.substring(1);
    name = name.replace("_", " ");
    name = name.replace("mustang", "");
    name = name.replace("off", " (Center OFF)");
    name = name.replace("ononon_1", " On/On/On (Type 1)");
    name = name.replace("ononon_2", " On/On/On (Type 2)");
    return name;
  }
}
