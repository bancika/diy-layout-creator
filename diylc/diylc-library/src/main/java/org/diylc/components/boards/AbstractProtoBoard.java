package org.diylc.components.boards;

import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractProtoBoard extends AbstractComponent<Void> {


  private static final long serialVersionUID = 1L;
  
  protected Orientation orientation;

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }
}
