package org.diylc.components.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.passive.MiniaturePotentiometer;
import org.diylc.core.IDIYComponent;

public class MiniaturePotentiometerTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component.getClass().equals(MiniaturePotentiometer.class);
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component.getClass().equals(MiniaturePotentiometer.class);
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

    MiniaturePotentiometer potentiometer = (MiniaturePotentiometer) component;
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
    MiniaturePotentiometer pot = (MiniaturePotentiometer) component;

    if (direction == IComponentTransformer.HORIZONTAL) {
      double dx = 2 * (center.getX() - pot.getControlPoint(0).getX());
      double dy = 0;
      Orientation o = pot.getOrientation();
      switch (o) {
        case DEFAULT:
          o = Orientation._180;
          dx += (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          break;
        case _90:
          dy -= (pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY());
          break;
        case _180:
          o = Orientation.DEFAULT;
          dx += (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          break;
        case _270:
          dy -= (pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY());
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
          dx -= (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          break;
        case _90:          
          dy += (pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY());
          o = Orientation._270;
          break;
        case _180:
          dx -= (pot.getControlPoint(0).getX() - pot.getControlPoint(2).getX());
          break;
        case _270:
          dy += (pot.getControlPoint(0).getY() - pot.getControlPoint(2).getY());
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
