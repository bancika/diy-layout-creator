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
    double minX = 0;
    double minY = 0;
    double maxX = 0;
    double maxY = 0;
    for (Shape a : body) {
      if (a != null) {
        Rectangle2D bounds2d = a.getBounds2D();
        if (bounds2d.getMinX() < minX)
          minX = bounds2d.getMinX();
        if (bounds2d.getMinY() < minY)
          minY = bounds2d.getMinY();
        if (bounds2d.getMaxX() > maxX)
          maxX = bounds2d.getMaxX();
        if (bounds2d.getMaxY() > maxY)
          maxY = bounds2d.getMaxY();
      }
    }
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }
}
