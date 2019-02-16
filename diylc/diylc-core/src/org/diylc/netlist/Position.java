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
package org.diylc.netlist;

import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;

/**
 * Represents a single position of a switch, uniquely defined by the switch and the position.
 * 
 * @author Branislav Stojkovic
 */
public class Position {

  private ISwitch theSwitch;
  private int position;

  public Position(ISwitch theSwitch, int position) {
    super();
    this.theSwitch = theSwitch;
    this.position = position;
  }

  public ISwitch getSwitch() {
    return theSwitch;
  }

  public int getPosition() {
    return position;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + position;
    result = prime * result + ((theSwitch == null) ? 0 : theSwitch.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Position other = (Position) obj;
    if (position != other.position)
      return false;
    if (theSwitch == null) {
      if (other.theSwitch != null)
        return false;
    } else if (!theSwitch.equals(other.theSwitch))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return (theSwitch instanceof IDIYComponent<?> ? ((IDIYComponent<?>) theSwitch).getName() + "." : "") + theSwitch.getPositionName(position);
  }
}
