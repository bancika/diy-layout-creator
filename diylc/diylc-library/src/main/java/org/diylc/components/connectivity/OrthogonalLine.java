/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.connectivity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractOrthogonalComponent;
import org.diylc.components.transform.OrthogonalComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Right Angle Line", author = "Branislav Stojkovic", category = "Schematic Symbols",
    instanceNamePrefix = "LN", description = "Line with rounded right angle turns and optional arrows",
    zOrder = IDIYComponent.WIRING, flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Schematic",
    transformer = OrthogonalComponentTransformer.class, enableCache = false)
public class OrthogonalLine extends AbstractOrthogonalComponent<Void> implements IContinuity {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;
  public static Size THICKNESS = new Size(1d, SizeUnit.px);
  public static Size ARROW_SIZE = new Size(5d, SizeUnit.px);

  private Size thickness = THICKNESS;
  private Size arrowSize = ARROW_SIZE;
  private boolean arrowStart = false;
  private boolean arrowEnd = false;

  private transient Polygon arrow;

  @Override
  protected Color getDefaultColor() {
    return COLOR;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR);
    g2d.draw(createIconPath(width, height));
  }

  @Override
  protected void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState,
      IDrawingObserver drawingObserver) {
    float thickness = (float) getThickness().convertToPixels();
    Stroke stroke = null;
    switch (getStyle()) {
      case SOLID:
        stroke = ObjectCache.getInstance().fetchBasicStroke(thickness);
        break;
      case DASHED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness * 2, thickness * 4},
            thickness * 4, BasicStroke.CAP_SQUARE);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness, thickness * 5}, 0,
            BasicStroke.CAP_ROUND);
        break;
    }
    g2d.setStroke(stroke);
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
        ? SELECTION_COLOR : color);
    g2d.draw(curve);

    if (getArrowStart() || getArrowEnd()) {
      List<Point2D> vertices = buildVertices();
      // a route that collapses onto a single point has no direction to point an arrow along
      if (vertices.size() > 1) {
        if (getArrowStart()) {
          drawArrow(g2d, vertices.get(0), vertices.get(1));
        }
        if (getArrowEnd()) {
          drawArrow(g2d, vertices.getLast(), vertices.get(vertices.size() - 2));
        }
      }
    }
  }

  /**
   * The arrow head is filled in the line color with its tip on the end point, so the line running
   * underneath it does not have to be trimmed.
   */
  private void drawArrow(Graphics2D g2d, Point2D tip, Point2D towards) {
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(tip.getX(), tip.getY());
    g2d.rotate(Math.atan2(towards.getY() - tip.getY(), towards.getX() - tip.getX()) + Math.PI / 2);
    g2d.fill(getArrow());
    g2d.setTransform(oldTx);
  }

  public Polygon getArrow() {
    if (arrow == null) {
      arrow = new Polygon();
      int t = (int) getArrowSize().convertToPixels();
      arrow.addPoint(0, 0);
      arrow.addPoint(-t, -t * 2);
      arrow.addPoint(t, -t * 2);
    }
    return arrow;
  }

  @EditableProperty
  public Size getThickness() {
    if (thickness == null) {
      thickness = THICKNESS;
    }
    return thickness;
  }

  public void setThickness(Size thickness) {
    this.thickness = thickness;
  }

  @EditableProperty(name = "Arrow Size")
  public Size getArrowSize() {
    if (arrowSize == null) {
      arrowSize = ARROW_SIZE;
    }
    return arrowSize;
  }

  public void setArrowSize(Size arrowSize) {
    this.arrowSize = arrowSize;
    arrow = null;
  }

  @EditableProperty(name = "Start Arrow")
  public boolean getArrowStart() {
    return arrowStart;
  }

  public void setArrowStart(boolean arrowStart) {
    this.arrowStart = arrowStart;
  }

  @EditableProperty(name = "End Arrow")
  public boolean getArrowEnd() {
    return arrowEnd;
  }

  public void setArrowEnd(boolean arrowEnd) {
    this.arrowEnd = arrowEnd;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  public boolean arePointsConnected(int index1, int index2) {
    return Math.abs(index1 - index2) == getControlPointCount() - 1;
  }
}
