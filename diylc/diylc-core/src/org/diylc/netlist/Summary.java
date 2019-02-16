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

public class Summary {

  private Netlist netlist;
  private List<String> notes;

  public Summary(Netlist netlist, List<String> notes) {
    super();
    this.netlist = netlist;
    this.notes = notes;
  }

  public Netlist getNetlist() {
    return netlist;
  }

  public List<String> getNotes() {
    return notes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((netlist == null) ? 0 : netlist.hashCode());
    result = prime * result + ((notes == null) ? 0 : notes.hashCode());
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
    Summary other = (Summary) obj;
    if (netlist == null) {
      if (other.netlist != null)
        return false;
    } else if (!netlist.equals(other.netlist))
      return false;
    if (notes == null) {
      if (other.notes != null)
        return false;
    } else if (!notes.equals(other.notes))
      return false;
    return true;
  }

  @Override
  public String toString() {   
    return notes.toString();
  }
}
