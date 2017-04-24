package org.diylc.core.measures;

import org.diylc.core.annotations.EditableProperty;

public class Nudge {

  private Size xOffset;
  private Size yOffset;
  private boolean affectStuckComponents;
  
  @EditableProperty(name = "X-axis", sortOrder = 1)
  public Size getxOffset() {
    return xOffset;
  }
  
  public void setxOffset(Size xOffset) {
    this.xOffset = xOffset;
  }
  
  @EditableProperty(name = "Y-axis", sortOrder = 2)
  public Size getyOffset() {
    return yOffset;
  }
  
  public void setyOffset(Size yOffset) {
    this.yOffset = yOffset;
  }
  
  @EditableProperty(name = "Include stuck", sortOrder = 10)
  public boolean getAffectStuckComponents() {
    return affectStuckComponents;
  }
  
  public void setAffectStuckComponents(boolean affectStuckComponents) {
    this.affectStuckComponents = affectStuckComponents;
  }
}
