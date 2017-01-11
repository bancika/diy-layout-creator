package org.diylc.components.transform;

import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.electromechanical.ClosedJack1_4;
import org.diylc.core.IDIYComponent;

public class ClosedJackTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(ClosedJack1_4.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(ClosedJack1_4.class);
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

    ClosedJack1_4 jack = (ClosedJack1_4) component;
    Orientation o = jack.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    jack.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    ClosedJack1_4 jack = (ClosedJack1_4) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      int dx = 2 * (center.x - jack.getControlPoint(0).x);
      int dy = 0;

      Orientation o = jack.getOrientation();
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

      for (int i = 0; i < jack.getControlPointCount(); i++) {
        Point p = jack.getControlPoint(i);
        jack.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      jack.setOrientation(o);
    } else {
      int dx = 0;
      int dy = 2 * (center.y - jack.getControlPoint(0).y);

      Orientation o = jack.getOrientation();
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

      for (int i = 0; i < jack.getControlPointCount(); i++) {
        Point p = jack.getControlPoint(i);
        jack.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      jack.setOrientation(o);
    }
  }
}
