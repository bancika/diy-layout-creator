package org.diylc.common;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.log4j.Logger;

import org.diylc.core.Angle;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComponentProcessor;

public class DefaultComponentTransformer implements IComponentTransformer {

  private static final Logger LOG = Logger.getLogger(DefaultComponentTransformer.class);

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    List<PropertyWrapper> properties =
        ComponentProcessor.getInstance().extractProperties(component.getClass());

    for (PropertyWrapper property : properties) {
      if (property.getType().isAssignableFrom(Orientation.class)
          || property.getType().isAssignableFrom(OrientationHV.class)
          || property.getType().isAssignableFrom(Angle.class))
        return true;
    }
    return false;
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return false;
  }

  @Override
  public boolean mirroringChangesCircuit() {
    return false;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    boolean canRotate = false;
    List<PropertyWrapper> properties =
        ComponentProcessor.getInstance().extractProperties(component.getClass());

    for (PropertyWrapper property : properties) {
      try {
        property.readFrom(component);
      } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
          | SecurityException | NoSuchMethodException e) {
        LOG.warn("Error while rotating component " + component.getName(), e);
      }

      try {
        if (property.getType().isAssignableFrom(Orientation.class)) {
          canRotate = true;
          
          Orientation o = (Orientation) property.getValue();
          int oValue = o.ordinal();
          oValue += direction;
          if (oValue < 0)
            oValue = Orientation.values().length - 1;
          if (oValue >= Orientation.values().length)
            oValue = 0;
          o = Orientation.values()[oValue];
          property.setValue(o);
        }

        if (property.getType().isAssignableFrom(OrientationHV.class)) {
          canRotate = true;
          
          OrientationHV o = (OrientationHV) property.getValue();
          int oValue = o.ordinal();
          oValue += direction;
          if (oValue < 0)
            oValue = OrientationHV.values().length - 1;
          if (oValue >= OrientationHV.values().length)
            oValue = 0;
          o = OrientationHV.values()[oValue];
          property.setValue(o);
        }

        if (property.getType().isAssignableFrom(Angle.class)) {
          canRotate = true;

          Angle angle = (Angle) property.getValue();
          Angle newAngle = angle.rotate(direction);
          property.setValue(newAngle);
        }
      } catch (Exception e) {
        LOG.warn("Error while rotating component " + component.getName(), e);
      }

      if (canRotate) {
        AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction,
            center.getX(), center.getY());
        for (int index = 0; index < component.getControlPointCount(); index++) {
          Point2D p = new Point2D.Double();
          rotate.transform(component.getControlPoint(index), p);
          component.setControlPoint(p, index);
        }

        try {
          property.writeTo(component);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
            | SecurityException | NoSuchMethodException e) {
          LOG.warn("Error while rotating component " + component.getName(), e);
        }
        break;
      }
    }
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {}
}
