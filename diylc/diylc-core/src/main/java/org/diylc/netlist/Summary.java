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

public class Summary implements Comparable<Summary> {

  private Netlist netlist;
  private String summaryHtml;

  public Summary(Netlist netlist, String summaryHtml) {
    super();
    this.netlist = netlist;
    this.summaryHtml = summaryHtml;
  }

  public Netlist getNetlist() {
    return netlist;
  }
  
  public void append(Netlist netlist) {
    this.netlist.getSwitchSetup().addAll(netlist.getSwitchSetup());
  }

  public String getSummaryHtml() {
    return summaryHtml;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((netlist == null) ? 0 : netlist.hashCode());
    result = prime * result + ((summaryHtml == null) ? 0 : summaryHtml.hashCode());
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
    if (summaryHtml == null) {
      if (other.summaryHtml != null)
        return false;
    } else if (!summaryHtml.equals(other.summaryHtml))
      return false;
    return true;
  }

  @Override
  public String toString() {   
    return summaryHtml.toString();
  }

  @Override
  public int compareTo(Summary o) {
    return netlist.compareTo(o.netlist);
  }
}
