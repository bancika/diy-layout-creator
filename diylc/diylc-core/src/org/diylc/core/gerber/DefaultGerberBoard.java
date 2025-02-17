package org.diylc.core.gerber;

import java.awt.geom.Rectangle2D;
import java.util.List;
import org.diylc.core.IDIYComponent;

public class DefaultGerberBoard implements IGerberBoard {
  
  private List<IDIYComponent<?>> components;
  
  public void setComponents(List<IDIYComponent<?>> components) {
    this.components = components;
  }

  @Override
  public Rectangle2D getBoardRectangle() {
    return null;
  }
}
