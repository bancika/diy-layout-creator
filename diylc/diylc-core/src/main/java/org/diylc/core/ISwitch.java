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
package org.diylc.core;

import java.awt.*;

/**
 * Interface for all component that can act as switches. They must have at least one position.
 * 
 * @author Branislav Stojkovic
 */
public interface ISwitch {

  Color[] POLE_COLORS = new Color[] { Color.decode("#FFD1DC"),
      Color.decode("#FFE5B4"),
      Color.decode("#FFFACD"),
      Color.decode("#B8F2E6"),
      Color.decode("#AEC6CF"),
      Color.decode("#E6E6FA")};

  /**
   * @return a positive number of positions.
   */
  int getPositionCount();
  
  /**   
   * @param position
   * @return name of switch positions, e.g. "ON", "OFF"...
   */
  String getPositionName(int position);
  
  /**
   * Tests control point connectivity.
   *  
   * @param index1
   * @param index2
   * @param position
   * @return true if the two points are connected in the given switch position, false otherwise.
   */
  boolean arePointsConnected(int index1, int index2, int position);

  /**
   * @return currently selected switch position, or null if none
   */
  default Integer getSelectedPosition() {
    return null;
  }

  default Boolean getRenderConnectedTerminals() {
    return false;
  }
}
