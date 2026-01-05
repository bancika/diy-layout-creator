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
import org.diylc.components.electromechanical.OpenJack1_4;
import org.diylc.components.electromechanical.OpenJack1_8;
import org.diylc.core.IDIYComponent;

public class OpenJackTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(OpenJack1_4.class) || component.getClass().equals(OpenJack1_8.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(OpenJack1_4.class) || component.getClass().equals(OpenJack1_8.class);
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

    Integer angle;
    if (component instanceof OpenJack1_4) {
      angle = ((OpenJack1_4) component).getAngle();
    } else if (component instanceof OpenJack1_8) {
      angle = ((OpenJack1_8) component).getAngle();
    } else {
      return;
    }
    
    if (angle == null) {
      angle = 0;
    }
    
    int newAngle = angle + direction * 90;
    // Normalize to 0-360 range
    while (newAngle < 0) {
      newAngle += 360;
    }
    while (newAngle >= 360) {
      newAngle -= 360;
    }
    
    if (component instanceof OpenJack1_4) {
      ((OpenJack1_4) component).setAngle(newAngle);
    } else if (component instanceof OpenJack1_8) {
      ((OpenJack1_8) component).setAngle(newAngle);
    }
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    if (!(component instanceof OpenJack1_4) && !(component instanceof OpenJack1_8)) {
      return;
    }

    IDIYComponent<?> ic = component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      // Mirror control points horizontally
      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point2D p = ic.getControlPoint(i);
        double dx = 2 * (center.getX() - p.getX());
        ic.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY()), i);
      }

      Integer angle;
      if (component instanceof OpenJack1_4) {
        angle = ((OpenJack1_4) component).getAngle();
      } else {
        angle = ((OpenJack1_8) component).getAngle();
      }
      
      if (angle == null) {
        angle = 0;
      }
      
      // Mirror horizontally: reflect angle across vertical axis
      int newAngle = 360 - angle;
      while (newAngle >= 360) {
        newAngle -= 360;
      }
      
      if (component instanceof OpenJack1_4) {
        ((OpenJack1_4) component).setAngle(newAngle);
      } else {
        ((OpenJack1_8) component).setAngle(newAngle);
      }
    } else {
      // Mirror control points vertically
      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Point2D p = ic.getControlPoint(i);
        double dy = 2 * (center.getY() - p.getY());
        ic.setControlPoint(new Point2D.Double(p.getX(), p.getY() + dy), i);
      }

      Integer angle;
      if (component instanceof OpenJack1_4) {
        angle = ((OpenJack1_4) component).getAngle();
      } else {
        angle = ((OpenJack1_8) component).getAngle();
      }
      
      if (angle == null) {
        angle = 0;
      }
      
      // Mirror vertically: reflect angle across horizontal axis
      int newAngle = 180 - angle;
      while (newAngle < 0) {
        newAngle += 360;
      }
      while (newAngle >= 360) {
        newAngle -= 360;
      }
      
      if (component instanceof OpenJack1_4) {
        ((OpenJack1_4) component).setAngle(newAngle);
      } else {
        ((OpenJack1_8) component).setAngle(newAngle);
      }
    }
  }
}

