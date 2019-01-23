package org.diylc.components;

import java.awt.Graphics2D;
import java.awt.geom.Area;

import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;


public abstract class AbstractMultiPartComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;

  public abstract Area[] getBody();
  
  public void drawSelectionOutline(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.draw(getBodyOutline());
    } 
  }
  
  public Area getBodyOutline() {
    Area[] body = getBody();
    Area outline = new Area();
    for (Area b : body)
      if (b != null)
        outline.add(b);
    return outline;
  }
}
