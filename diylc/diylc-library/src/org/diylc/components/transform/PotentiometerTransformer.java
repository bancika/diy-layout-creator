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
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.core.IDIYComponent;

public class PotentiometerTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(PotentiometerPanel.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(PotentiometerPanel.class);
  }
  
  @Override
  public boolean mirroringChangesCircuit() {   
    return true;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point center, int direction) {
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.x, center.y);
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point p = new Point(component.getControlPoint(index));
      rotate.transform(p, p);
      component.setControlPoint(p, index);
    }

    PotentiometerPanel potentiometer = (PotentiometerPanel) component;
    Orientation o = potentiometer.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    potentiometer.setOrientation(o);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    PotentiometerPanel potentiometer = (PotentiometerPanel) component;
    int dx = center.x - potentiometer.getControlPoint(1).x;
    int dy = center.y - potentiometer.getControlPoint(1).y;
    if (direction == IComponentTransformer.HORIZONTAL) {
      Orientation o = potentiometer.getOrientation();
      switch (o) {
        case _90:
          o = Orientation._270;
          break;
        case _270:
          o = Orientation._90;
      }

      for (int i = 0; i < potentiometer.getControlPointCount(); i++) {
        Point p = potentiometer.getControlPoint(i);
        potentiometer
            .setControlPoint(
                new Point(p.x + 2 * dx, p.y + (potentiometer.getControlPoint(2).y - potentiometer.getControlPoint(0).y)),
                i);
      }

      potentiometer.setOrientation(o);
    } else {
      Orientation o = potentiometer.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          break;
        case _180:
          o = Orientation.DEFAULT;
      }

      for (int i = 0; i < potentiometer.getControlPointCount(); i++) {
        Point p = potentiometer.getControlPoint(i);
        potentiometer
            .setControlPoint(new Point(p.x + (potentiometer.getControlPoint(2).x - potentiometer.getControlPoint(0).x),
                p.y + 2 * dy), i);
      }

      potentiometer.setOrientation(o);
    }
  }
}
