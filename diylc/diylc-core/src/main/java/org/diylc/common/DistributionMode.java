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

public enum DistributionMode {

  HORIZONTAL_CENTERS("Distribute Centers Horizontally"),
  HORIZONTAL_SPACING("Distribute Spacing Horizontally"),
  VERTICAL_CENTERS("Distribute Centers Vertically"),
  VERTICAL_SPACING("Distribute Spacing Vertically");

  private String label;

  DistributionMode(String label) {
    this.label = label;
  }

  public boolean isHorizontal() {
    return this == HORIZONTAL_CENTERS || this == HORIZONTAL_SPACING;
  }

  public boolean isSpacing() {
    return this == HORIZONTAL_SPACING || this == VERTICAL_SPACING;
  }

  @Override
  public String toString() {
    return label;
  }
}
