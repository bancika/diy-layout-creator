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
package org.diylc.core.measures;

import org.diylc.core.annotations.EditableProperty;

public class Nudge {

  private Size xOffset;
  private Size yOffset;
  private boolean affectStuckComponents;
  
  @EditableProperty(name = "X-axis", sortOrder = 1)
  public Size getxOffset() {
    return xOffset;
  }
  
  public void setxOffset(Size xOffset) {
    this.xOffset = xOffset;
  }
  
  @EditableProperty(name = "Y-axis", sortOrder = 2)
  public Size getyOffset() {
    return yOffset;
  }
  
  public void setyOffset(Size yOffset) {
    this.yOffset = yOffset;
  }
  
  @EditableProperty(name = "Include stuck", sortOrder = 10)
  public boolean getAffectStuckComponents() {
    return affectStuckComponents;
  }
  
  public void setAffectStuckComponents(boolean affectStuckComponents) {
    this.affectStuckComponents = affectStuckComponents;
  }
}
