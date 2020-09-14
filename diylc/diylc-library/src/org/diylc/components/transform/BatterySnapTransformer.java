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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.electromechanical.BatterySnap9V;
import org.diylc.core.IDIYComponent;

public class BatterySnapTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(BatterySnap9V.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(BatterySnap9V.class);
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

    BatterySnap9V snap = (BatterySnap9V) component;
    Orientation o = snap.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    snap.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    BatterySnap9V snap = (BatterySnap9V) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      double dx = 2 * (center.getX() - snap.getControlPoint(0).getX());
      double dy = 0;
      Orientation o = snap.getOrientation();
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

      for (int i = 0; i < snap.getControlPointCount(); i++) {
        Point2D p = snap.getControlPoint(i);
        snap.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      snap.setOrientation(o);
    } else {
      double dx = 0;
      double dy = 2 * (center.getY() - snap.getControlPoint(1).getY());
      Orientation o = snap.getOrientation();
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

      for (int i = 0; i < snap.getControlPointCount(); i++) {
        Point2D p = snap.getControlPoint(i);
        snap.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      snap.setOrientation(o);
    }
  }
}
