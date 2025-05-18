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
package org.diylc.components.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.semiconductors.SIL_IC;
import org.diylc.core.IDIYComponent;

public class SIL_ICTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(SIL_IC.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(SIL_IC.class);
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return false;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.getX(), center.getY());
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point2D p = new Point2D.Double();
      rotate.transform(component.getControlPoint(index), p);
      component.setControlPoint(p, index);
    }

    SIL_IC ic = (SIL_IC) component;
    Orientation o = ic.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    ic.setOrientation(o);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    SIL_IC ic = (SIL_IC) component;
    double dx = center.getX() - ic.getControlPoint(0).getX();
    double dy = center.getY() - ic.getControlPoint(0).getY();
    if (direction == IComponentTransformer.HORIZONTAL) {
      Orientation o = ic.getOrientation();
      switch (o) {
        case _90:
          o = Orientation._270;
          break;
        case _270:
          o = Orientation._90;
      }

      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point2D p = ic.getControlPoint(i);
        ic.setControlPoint(new Point2D.Double(p.getX() + 2 * dx, p.getY()), i);
      }

      ic.setOrientation(o);
    } else {
      Orientation o = ic.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          break;
        case _180:
          o = Orientation.DEFAULT;
      }

      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point2D p = ic.getControlPoint(i);
        ic.setControlPoint(new Point2D.Double(p.getX(), p.getY() + 2 * dy), i);
      }

      ic.setOrientation(o);
    }
  }
}
