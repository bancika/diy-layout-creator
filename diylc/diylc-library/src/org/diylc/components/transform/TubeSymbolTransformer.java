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
import org.diylc.common.Orientation;
import org.diylc.components.tube.AbstractTubeSymbol;
import org.diylc.core.IDIYComponent;

public class TubeSymbolTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return AbstractTubeSymbol.class.isAssignableFrom(component.getClass());
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return AbstractTubeSymbol.class.isAssignableFrom(component.getClass());
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

    AbstractTubeSymbol tube = (AbstractTubeSymbol) component;
    Orientation o = tube.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    tube.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    AbstractTubeSymbol tube = (AbstractTubeSymbol) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      int dx = 2 * (center.x - tube.getControlPoint(0).x);
      int dy = 0;

      Orientation o = tube.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          break;
        case _90:
          break;
        case _180:
          o = Orientation.DEFAULT;
          break;
        case _270: 
          break;
      }

      for (int i = 0; i < tube.getControlPointCount(); i++) {
        Point p = tube.getControlPoint(i);
        tube.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      tube.setOrientation(o);
    } else {
      int dx = 0;
      int dy = 2 * (center.y - tube.getControlPoint(0).y);

      Orientation o = tube.getOrientation();
      switch (o) {
        case DEFAULT:
          break;
        case _90:
          o = Orientation._270;
          break;
        case _180:
          break;
        case _270:
          o = Orientation._90;
          break;
      }

      for (int i = 0; i < tube.getControlPointCount(); i++) {
        Point p = tube.getControlPoint(i);
        tube.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      tube.setOrientation(o);
    }
  }
}
