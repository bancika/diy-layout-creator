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
package org.diylc.components.transform;

import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.diylc.common.IComponentTransformer;
import org.diylc.core.IDIYComponent;

public class LeadedComponentTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return true;
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return true;
  }
  
  @Override
  public boolean mirroringChangesCircuit() {   
    return false;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point center, int direction) {
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.x, center.y);
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point p = new Point(component.getControlPoint(index));
      rotate.transform(p, p);
      component.setControlPoint(p, index);
    }
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    for (int i = 0; i < component.getControlPointCount(); i++) {
      Point p = component.getControlPoint(i);
      if (direction == IComponentTransformer.HORIZONTAL) {
        component.setControlPoint(new Point(p.x - 2 * (p.x - center.x), p.y), i);
      } else {
        component.setControlPoint(new Point(p.x, p.y - 2 * (p.y - center.y)), i);
      }
    }
  }

}
