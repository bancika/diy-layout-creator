package org.diylc.components;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;


public abstract class AbstractMultiPartComponent<T> extends AbstractLabeledComponent<T> {

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
  
  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] body = getBody();
    int margin = 20;
    Rectangle2D bounds = null;
    for (Shape a : body) {
      if (a != null) {
        bounds = bounds == null ? a.getBounds2D() : bounds.createUnion(a.getBounds2D());
      }
    }
    if (bounds == null) {
      bounds = new Rectangle2D.Double(getControlPoint(0).getX(), getControlPoint(0).getY(), 0, 0);
    }
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
        bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
}
