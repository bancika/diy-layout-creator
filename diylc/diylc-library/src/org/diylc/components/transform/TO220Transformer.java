package org.diylc.components.transform;

import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.semiconductors.TransistorTO220;
import org.diylc.core.IDIYComponent;

public class TO220Transformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(TransistorTO220.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(TransistorTO220.class);
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

    TransistorTO220 transistor = (TransistorTO220) component;
    Orientation o = transistor.getOrientation();
    int oValue = o.ordinal();
    oValue += direction;
    if (oValue < 0)
      oValue = Orientation.values().length - 1;
    if (oValue >= Orientation.values().length)
      oValue = 0;
    o = Orientation.values()[oValue];
    transistor.setOrientation(o);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    TransistorTO220 transistor = (TransistorTO220) component;
    if (direction == IComponentTransformer.HORIZONTAL) {
      int dx = 2 * (center.x - transistor.getControlPoint(0).x);
      int dy = 0;
      Orientation o = transistor.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          dy -= (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
          break;
        case _90:
          dx += (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          break;
        case _180:
          o = Orientation.DEFAULT;
          dy -= (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
          break;
        case _270:
          dx += (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          break;
      }

      for (int i = 0; i < transistor.getControlPointCount(); i++) {
        Point p = transistor.getControlPoint(i);
        transistor.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      transistor.setOrientation(o);
    } else {
      int dx = 0;
      int dy = 2 * (center.y - transistor.getControlPoint(0).y);
      Orientation o = transistor.getOrientation();
      switch (o) {
        case DEFAULT:
          dy += (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
          break;
        case _90:
          dx -= (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          o = Orientation._270;
          break;
        case _180:
          dy += (transistor.getControlPoint(0).y - transistor.getControlPoint(2).y);
          break;
        case _270:
          dx -= (transistor.getControlPoint(0).x - transistor.getControlPoint(2).x);
          o = Orientation._90;
          break;
      }

      for (int i = 0; i < transistor.getControlPointCount(); i++) {
        Point p = transistor.getControlPoint(i);
        transistor.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      transistor.setOrientation(o);
    }
  }
}
