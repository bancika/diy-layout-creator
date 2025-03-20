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
package org.diylc.components;

import java.awt.Shape;

import org.diylc.common.Orientation;
import org.diylc.core.Angle;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractAngledComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;
  
  @Deprecated
  protected Orientation orientation;
  protected int angle;
  
  transient protected Shape[] body;
  
  protected abstract void updateControlPoints();
  
  @EditableProperty
  @SuppressWarnings("incomplete-switch")
  public Angle getAngle() {
    if (orientation != null) {
      switch (orientation) {
        case _90:
          angle = 90;
          break;
        case _180:
          angle = 180;
          break;
        case _270:
          angle = 270;
          break;
      }
      orientation = null;
    }
    return Angle.of(angle);
  }

  public void setAngle(Angle angle) {
    this.angle = angle.getValue();
    updateControlPoints();
    // Reset body shape
    body = null;
  }
}
