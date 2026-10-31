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
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a full switch configuration of the whole {@link Netlist}, containing one position of each switch in the {@link Netlist}.
 * 
 * @author Branislav Stojkovic
 */
public class SwitchSetup implements Comparable<SwitchSetup> {
  
  private List<Position> positions;

  public SwitchSetup(List<Position> positions) {
    super();
    this.positions = positions;
    Collections.sort(this.positions);
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

  @Override
  public int compareTo(SwitchSetup o) {
    List<Position> left = new ArrayList<>(positions);
    List<Position> right = new ArrayList<>(o.positions);
    Collections.sort(left);
    Collections.sort(right);

    int i = 0;
    int j = 0;

    while (i < left.size() || j < right.size()) {
      int comparison;

      if (i == left.size()) {
        // This setup omits the next switch: use its lowest position.
        comparison = Integer.compare(0, right.get(j++).getPosition());
      } else if (j == right.size()) {
        comparison = Integer.compare(left.get(i++).getPosition(), 0);
      } else {
        Position a = left.get(i);
        Position b = right.get(j);
        int nameComparison =
                a.getSwitchName().compareToIgnoreCase(b.getSwitchName());

        if (nameComparison < 0) {
          comparison = Integer.compare(a.getPosition(), 0);
          i++;
        } else if (nameComparison > 0) {
          comparison = Integer.compare(0, b.getPosition());
          j++;
        } else {
          comparison = Integer.compare(a.getPosition(), b.getPosition());
          i++;
          j++;
        }
      }

      if (comparison != 0) {
        return comparison;
      }
    }

    // Same earliest full configuration: use the description as a tie-breaker.
    return toString().compareToIgnoreCase(o.toString());
  }
}
