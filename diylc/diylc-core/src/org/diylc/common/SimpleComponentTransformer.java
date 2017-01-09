package org.diylc.common;

import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.diylc.core.IDIYComponent;

public class SimpleComponentTransformer implements IComponentTransformer {

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
