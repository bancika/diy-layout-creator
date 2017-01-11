package org.diylc.components.transform;

import java.awt.Point;
import java.awt.geom.AffineTransform;

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
  public void rotate(IDIYComponent<?> component, Point center, int direction) {
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.x, center.y);
    for (int index = 0; index < component.getControlPointCount(); index++) {
      Point p = new Point(component.getControlPoint(index));
      rotate.transform(p, p);
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
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
    TerminalStrip strip = (TerminalStrip) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      int dx = 2 * (center.x - strip.getControlPoint(1).x);
      int dy = 0;
      Orientation o = strip.getOrientation();
      switch (o) {
        case DEFAULT:
          dx += strip.getControlPoint(0).x - strip.getControlPoint(strip.getControlPointCount() - 1).x;
          break;
        case _90:
          o = Orientation._270;
          dx -= 2 * (strip.getControlPoint(0).x - strip.getControlPoint(1).x);
          dy -= strip.getControlPoint(0).y - strip.getControlPoint(strip.getControlPointCount() - 1).y;
          break;
        case _180:
          dx += strip.getControlPoint(0).x - strip.getControlPoint(strip.getControlPointCount() - 1).x;
          break;
        case _270:
          dx -= 2 * (strip.getControlPoint(0).x - strip.getControlPoint(1).x);
          dy -= strip.getControlPoint(0).y - strip.getControlPoint(strip.getControlPointCount() - 1).y;
          o = Orientation._90;
          break;
      }

      for (int i = 0; i < strip.getControlPointCount(); i++) {
        Point p = strip.getControlPoint(i);
        strip.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      strip.setOrientation(o);
    } else {
      int dx = 0;
      int dy = 2 * (center.y - strip.getControlPoint(1).y);
      Orientation o = strip.getOrientation();
      switch (o) {
        case DEFAULT:
          dx -= strip.getControlPoint(0).x - strip.getControlPoint(strip.getControlPointCount() - 1).x;
          dy -= 2 * (strip.getControlPoint(0).y - strip.getControlPoint(1).y);
          o = Orientation._180;
          break;
        case _90:
          dy += strip.getControlPoint(0).y - strip.getControlPoint(strip.getControlPointCount() - 1).y;
          break;
        case _180:
          dx -= strip.getControlPoint(0).x - strip.getControlPoint(strip.getControlPointCount() - 1).x;
          dy -= 2 * (strip.getControlPoint(0).y - strip.getControlPoint(1).y);
          o = Orientation.DEFAULT;
          break;
        case _270:
          dy += strip.getControlPoint(0).y - strip.getControlPoint(strip.getControlPointCount() - 1).y;
          break;
      }

      for (int i = 0; i < strip.getControlPointCount(); i++) {
        Point p = strip.getControlPoint(i);
        strip.setControlPoint(new Point(p.x + dx, p.y + dy), i);
      }

      strip.setOrientation(o);
    }
  }
}
