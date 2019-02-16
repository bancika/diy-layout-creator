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

import java.util.List;

/**
 * Represents a full switch configuration of the whole {@link Netlist}, containing one position of each switch in the {@link Netlist}.
 * 
 * @author Branislav Stojkovic
 */
public class SwitchSetup {
  
  private List<Position> positions;

  public SwitchSetup(List<Position> positions) {
    super();
    this.positions = positions;
  }

  public List<Position> getPositions() {
    return positions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((positions == null) ? 0 : positions.hashCode());
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
    SwitchSetup other = (SwitchSetup) obj;
    if (positions == null) {
      if (other.positions != null)
        return false;
    } else if (!positions.equals(other.positions))
      return false;
    return true;
  }
  
  @Override
  public String toString() {   
    StringBuilder sb = new StringBuilder();
    for (Position s : positions)
    {
      if (sb.length() > 0)
        sb.append(":");
      sb.append(s.toString());      
    }
    return sb.toString();
  }
}
