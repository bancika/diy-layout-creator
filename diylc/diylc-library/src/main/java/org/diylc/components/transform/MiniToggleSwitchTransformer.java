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
import org.diylc.common.OrientationHV;
import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.core.IDIYComponent;

public class MiniToggleSwitchTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(MiniToggleSwitch.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(MiniToggleSwitch.class);
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return true;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    MiniToggleSwitch sw = (MiniToggleSwitch) component;
    
    // Calculate center of all control points
    double sumX = 0, sumY = 0;
    for (int i = 0; i < sw.getControlPointCount(); i++) {
      Point2D p = sw.getControlPoint(i);
      sumX += p.getX();
      sumY += p.getY();
    }
    Point2D controlCenter = new Point2D.Double(
      sumX / sw.getControlPointCount(),
      sumY / sw.getControlPointCount()
    );
    
    // Calculate where control center should end up after rotation
    Point2D targetCenter = new Point2D.Double();
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.getX(), center.getY());
    rotate.transform(controlCenter, targetCenter);
    
    // Update orientation first - this will rotate points around first point
    OrientationHV o = sw.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = OrientationHV.values().length - 1;
    if (oValue >= OrientationHV.values().length)
      oValue = 0;
    o = OrientationHV.values()[oValue];
    sw.setOrientation(o);
    
    // Recalculate current control center after orientation change
    sumX = 0;
    sumY = 0;
    for (int i = 0; i < sw.getControlPointCount(); i++) {
      Point2D p = sw.getControlPoint(i);
      sumX += p.getX();
      sumY += p.getY();
    }
    Point2D newControlCenter = new Point2D.Double(
      sumX / sw.getControlPointCount(),
      sumY / sw.getControlPointCount()
    );
    
    // Calculate and apply translation to move control center to target position
    double dx = targetCenter.getX() - newControlCenter.getX();
    double dy = targetCenter.getY() - newControlCenter.getY();
    
    // Apply translation to all points
    for (int i = 0; i < sw.getControlPointCount(); i++) {
      Point2D p = sw.getControlPoint(i);
      sw.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
    }
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    MiniToggleSwitch sw = (MiniToggleSwitch) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      double dx = 2 * (center.getX() - sw.getControlPoint(1).getX());
      double dy = 0;
     
      for (int i = 0; i < sw.getControlPointCount(); i++) {
        Point2D p = sw.getControlPoint(i);
        sw.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }
    } else {
      double dx = 0;
      double dy = 2 * (center.getY() - sw.getControlPoint(1).getY());
    
      for (int i = 0; i < sw.getControlPointCount(); i++) {
        Point2D p = sw.getControlPoint(i);
        sw.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
      }
    }
  }
}
