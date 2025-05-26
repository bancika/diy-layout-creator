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
import org.diylc.components.guitar.Freeway3x3_03;
import org.diylc.core.IDIYComponent;

public class Freeway3x3_03Transformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(Freeway3x3_03.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(Freeway3x3_03.class);
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return true;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.getX(), center.getY());
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point2D p = new Point2D.Double();
      rotate.transform(component.getControlPoint(index), p);
      component.setControlPoint(p, index);
    }

    Freeway3x3_03 sw = (Freeway3x3_03) component;
    Orientation o = sw.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    sw.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    Freeway3x3_03 sw = (Freeway3x3_03) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      double dx = 2 * (center.getX() - sw.getControlPoint(1).getX());
      double dy = 0;
      Orientation o = sw.getOrientation();
      switch (o) {
        case DEFAULT:
          dx += sw.getControlPoint(0).getX() - sw.getControlPoint(sw.getControlPointCount() - 1).getX();
          break;
        case _90:
          o = Orientation._270;
          dx -= 2 * (sw.getControlPoint(0).getX() - sw.getControlPoint(1).getX());
          dy -= sw.getControlPoint(0).getY() - sw.getControlPoint(sw.getControlPointCount() - 1).getY();
          break;
        case _180:
          dx += sw.getControlPoint(0).getX() - sw.getControlPoint(sw.getControlPointCount() - 1).getX();
          break;
        case _270:
          dx -= 2 * (sw.getControlPoint(0).getX() - sw.getControlPoint(1).getX());
          dy -= sw.getControlPoint(0).getY() - sw.getControlPoint(sw.getControlPointCount() - 1).getY();
          o = Orientation._90;
          break;
      }

      for (int i = 0; i < sw.getControlPointCount(); i++) {
        Point2D p = sw.getControlPoint(i);
        sw.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      sw.setOrientation(o);
    } else {
      double dx = 0;
      double dy = 2 * (center.getY() - sw.getControlPoint(1).getY());
      Orientation o = sw.getOrientation();
      switch (o) {
        case DEFAULT:
          dx -= sw.getControlPoint(0).getX() - sw.getControlPoint(sw.getControlPointCount() - 1).getX();
          dy -= 2 * (sw.getControlPoint(0).getY() - sw.getControlPoint(1).getY());
          o = Orientation._180;
          break;
        case _90:
          dy += sw.getControlPoint(0).getY() - sw.getControlPoint(sw.getControlPointCount() - 1).getY();
          break;
        case _180:
          dx -= sw.getControlPoint(0).getX() - sw.getControlPoint(sw.getControlPointCount() - 1).getX();
          dy -= 2 * (sw.getControlPoint(0).getY() - sw.getControlPoint(1).getY());
          o = Orientation.DEFAULT;
          break;
        case _270:
          dy += sw.getControlPoint(0).getY() - sw.getControlPoint(sw.getControlPointCount() - 1).getY();
          break;
      }

      for (int i = 0; i < sw.getControlPointCount(); i++) {
        Point2D p = sw.getControlPoint(i);
        sw.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      sw.setOrientation(o);
    }
  }
}
