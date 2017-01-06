package org.diylc.common;

import java.awt.Point;

import org.diylc.core.IDIYComponent;

public interface IComponentTransformer {

  public static final int CLOCKWISE = 1;
  public static final int COUNTER_CLOCKWISE = 1;

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;
  
  boolean canRotate(IDIYComponent<?> component);
  
  boolean canMirror(IDIYComponent<?> component);

  void rotate(IDIYComponent<?> component, Point center, int direction);

  void mirror(IDIYComponent<?> component, Point center, int direction);
}
