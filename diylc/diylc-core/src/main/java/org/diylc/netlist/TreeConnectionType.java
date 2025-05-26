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

public enum TreeConnectionType {

  Series("+", "red"), Parallel("||", "blue");
  
  private String label;
  private String color;

  private TreeConnectionType(String label, String color) {
    this.label = label;
    this.color = color;
  }

  @Override
  public String toString() {
    return label;
  }
  
  public String toHTML() {
    return "<b><font color='" + color + "'>" + label + "</font></b>";
  }
}
