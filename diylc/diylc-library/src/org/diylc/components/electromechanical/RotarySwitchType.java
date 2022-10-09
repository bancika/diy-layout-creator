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

public enum RotarySwitchType {
  _1P12T("1P12T", 1, 12), _2P6T("2P6T", 2, 6), _3P4T("3P4T", 3, 4), _4P3T("4P3T", 4, 3);

  private String name;
  private int positionCount;
  private int poleCount;
  

  private RotarySwitchType(String name, int poleCount, int positionCount) {
    this.name = name;
    this.poleCount = poleCount;
    this.positionCount = positionCount;
  }

  @Override
  public String toString() {
    return name;
  }
  
  public int getPoleCount() {
    return poleCount;
  }
  
  public int getPositionCount() {
    return positionCount;
  }
}
