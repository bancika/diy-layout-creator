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

import java.awt.Color;
import java.io.Serializable;

public class Theme implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;
  private Color bgColor;
  private Color gridColor;
  private Color outlineColor;
  private Color dotColor;

  public Theme(String name, Color bgColor, Color gridColor, Color dotColor, Color outlineColor) {
    super();
    this.name = name;
    this.bgColor = bgColor;
    this.gridColor = gridColor;
    this.dotColor = dotColor;    
    this.outlineColor = outlineColor;
  }

  public String getName() {
    return name;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public Color getGridColor() {
    return gridColor;
  }
  
  public Color getDotColor() {
    if (dotColor == null)
      dotColor = gridColor;
    return dotColor;
  }

  public Color getOutlineColor() {
    return outlineColor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Theme other = (Theme) obj;  
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return name;
  }
}
