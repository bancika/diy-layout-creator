package org.diylc.components.transform;

import java.awt.Point;

import org.diylc.common.IComponentTransformer;
import org.diylc.core.IDIYComponent;

public class DefaultTransformer implements IComponentTransformer {

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
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
  public void rotate(IDIYComponent<?> component, Point center, int direction) {
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point center, int direction) {
  }

}
