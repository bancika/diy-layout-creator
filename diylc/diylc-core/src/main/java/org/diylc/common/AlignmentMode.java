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
package org.diylc.common;

public enum AlignmentMode {

  LEFT("Align Left"), RIGHT("Align Right"), TOP("Align Top"), BOTTOM("Align Bottom"),
  HORIZONTAL_CENTER("Align Horizontal Centers"), VERTICAL_CENTER("Align Vertical Centers");

  private String label;

  AlignmentMode(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
