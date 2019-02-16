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

import java.util.HashSet;
import java.util.Set;

public class Netlist {
  
  private Set<Group> groups = new HashSet<Group>();

  public Netlist() {    
  }

  public Set<Group> getGroups() {
    return groups;
  }
  
  public boolean verticesMatch(Netlist other) {
    return this.groups.equals(other.groups);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((groups == null) ? 0 : groups.hashCode());
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
    Netlist other = (Netlist) obj;
    if (groups == null) {
      if (other.groups != null)
        return false;
    } else if (!groups.equals(other.groups))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Group v : groups) {
      sb.append("\t").append(v).append("\n");
    }
    return sb.toString();
  }
}
