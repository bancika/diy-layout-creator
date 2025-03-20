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
import org.diylc.components.boards.TerminalStrip;
import org.diylc.core.IDIYComponent;

public class TerminalStripTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(TerminalStrip.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(TerminalStrip.class);
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

    TerminalStrip strip = (TerminalStrip) component;
    Orientation o = strip.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    strip.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    TerminalStrip strip = (TerminalStrip) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      double dx = 2 * (center.getX() - strip.getControlPoint(1).getX());
      double dy = 0;
      Orientation o = strip.getOrientation();
      switch (o) {
        case DEFAULT:
          dx += strip.getControlPoint(0).getX() - strip.getControlPoint(strip.getControlPointCount() - 1).getX();
          break;
        case _90:
          o = Orientation._270;
          dx -= 2 * (strip.getControlPoint(0).getX() - strip.getControlPoint(1).getX());
          dy -= strip.getControlPoint(0).getY() - strip.getControlPoint(strip.getControlPointCount() - 1).getY();
          break;
        case _180:
          dx += strip.getControlPoint(0).getX() - strip.getControlPoint(strip.getControlPointCount() - 1).getX();
          break;
        case _270:
          dx -= 2 * (strip.getControlPoint(0).getX() - strip.getControlPoint(1).getX());
          dy -= strip.getControlPoint(0).getY() - strip.getControlPoint(strip.getControlPointCount() - 1).getY();
          o = Orientation._90;
          break;
      }

      for (int i = 0; i < strip.getControlPointCount(); i++) {
        Point2D p = strip.getControlPoint(i);
        strip.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      strip.setOrientation(o);
    } else {
      double dx = 0;
      double dy = 2 * (center.getY() - strip.getControlPoint(1).getY());
      Orientation o = strip.getOrientation();
      switch (o) {
        case DEFAULT:
          dx -= strip.getControlPoint(0).getX() - strip.getControlPoint(strip.getControlPointCount() - 1).getX();
          dy -= 2 * (strip.getControlPoint(0).getY() - strip.getControlPoint(1).getY());
          o = Orientation._180;
          break;
        case _90:
          dy += strip.getControlPoint(0).getY() - strip.getControlPoint(strip.getControlPointCount() - 1).getY();
          break;
        case _180:
          dx -= strip.getControlPoint(0).getX() - strip.getControlPoint(strip.getControlPointCount() - 1).getX();
          dy -= 2 * (strip.getControlPoint(0).getY() - strip.getControlPoint(1).getY());
          o = Orientation.DEFAULT;
          break;
        case _270:
          dy += strip.getControlPoint(0).getY() - strip.getControlPoint(strip.getControlPointCount() - 1).getY();
          break;
      }

      for (int i = 0; i < strip.getControlPointCount(); i++) {
        Point2D p = strip.getControlPoint(i);
        strip.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }

      strip.setOrientation(o);
    }
  }
}
