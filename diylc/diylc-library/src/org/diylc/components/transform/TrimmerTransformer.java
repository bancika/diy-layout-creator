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
import org.diylc.components.passive.TrimmerPotentiometer;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.core.IDIYComponent;

public class TrimmerTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(TrimmerPotentiometer.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(TrimmerPotentiometer.class);
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

    TrimmerPotentiometer potentiometer = (TrimmerPotentiometer) component;
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
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    TrimmerPotentiometer pot = (TrimmerPotentiometer) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      double dx = 2 * (center.getX() - pot.getControlPoint(0).getX());
      double dy = 0;
      Orientation o = pot.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          dy -= pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY();
          break;
        case _90:
          dx += (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          break;
        case _180:
          o = Orientation.DEFAULT;
          dy -= pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY();
          break;
        case _270:
          dx += (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          break;
      }

      for (int i = 0; i < pot.getControlPointCount(); i++) {
        Point2D p = pot.getControlPoint(i);
        pot.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      pot.setOrientation(o);
    } else {
      double dx = 0;
      double dy = 2 * (center.getY() - pot.getControlPoint(0).getY());
      Orientation o = pot.getOrientation();
      switch (o) {
        case DEFAULT:
          dy += pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY();
          break;
        case _90:          
          dx -= (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          o = Orientation._270;
          break;
        case _180:
          dy += pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY();
          break;
        case _270:
          dx -= (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          o = Orientation._90;
          break;
      }

      for (int i = 0; i < pot.getControlPointCount(); i++) {
        Point2D p = pot.getControlPoint(i);
        pot.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      pot.setOrientation(o);    
    }
  }
}
