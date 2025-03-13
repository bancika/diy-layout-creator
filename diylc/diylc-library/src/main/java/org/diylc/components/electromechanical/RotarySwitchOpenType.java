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

public enum RotarySwitchOpenType {
  _2P5T("2P5T", 2, 5, false), _2P6T("2P6T", 2, 6, false), _4P5T("4P5T", 4, 5, true), _4P6T("4P6T", 4, 6, true);

  private String name;
  private int positionCount;
  private int poleCount;
  private boolean needsSecondLevel;

  private RotarySwitchOpenType(String name, int poleCount, int positionCount, boolean needsSecondLevel) {
    this.name = name;
    this.poleCount = poleCount;
    this.positionCount = positionCount;
    this.needsSecondLevel = needsSecondLevel;
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
  
  public boolean getNeedsSecondLevel() {
    return needsSecondLevel;
  }
}
