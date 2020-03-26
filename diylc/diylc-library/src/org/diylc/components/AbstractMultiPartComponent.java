package org.diylc.components;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;

import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;


public abstract class AbstractMultiPartComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;

  public abstract Shape[] getBody();
  
  public void drawSelectionOutline(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(getBodyOutline());
    } 
  }
  
  public Area getBodyOutline() {
    Shape[] body = getBody();
    Area outline = new Area();
    for (Shape b : body)
      if (b != null && b instanceof Area)
        outline.add((Area)b);
    return outline;
  }
}
