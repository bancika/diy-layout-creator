/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.diylc.common.Display;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Line", author = "Branislav Stojkovic", category = "Shapes",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "LN", description = "Line with optional arrows",
    zOrder = IDIYComponent.COMPONENT, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class Line extends AbstractLeadedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;  

  private Color color = COLOR;
  protected LineStyle style = LineStyle.SOLID; 
  private Size thickness = new Size(1d, SizeUnit.px);
  private Size arrowSize = new Size(5d, SizeUnit.px);
  private Polygon arrow = null;  
  private boolean arrowStart = false;
  private boolean arrowEnd = false;
  
  @SuppressWarnings("unused")
  @Deprecated
  private transient AffineTransform arrowTx;

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR);
    g2d.drawLine(1, height - 2, width - 2, 1);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    float thickness = (float) getThickness().convertToPixels();
    Stroke stroke = null;
    switch (getStyle()) {
      case SOLID:
        stroke = ObjectCache.getInstance().fetchZoomableStroke(thickness);
        break;
      case DASHED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness * 2, thickness * 4}, thickness * 4, BasicStroke.CAP_SQUARE);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness, thickness * 5}, 0, BasicStroke.CAP_ROUND);
        break;
    }
    g2d.setStroke(stroke);
    g2d.setColor(componentState == ComponentState.SELECTED ? SELECTION_COLOR : color);
    
    Point2D first = getControlPoint(0);
    Point2D second = getControlPoint(1);
    Point2D startPoint = new Point2D.Double(first.getX(), first.getY());
    Point2D endPoint = new Point2D.Double(second.getX(), second.getY());
    
    AffineTransform arrowPosTx = new AffineTransform();
    
    if (arrowStart) {
      arrowPosTx.setToIdentity();
      double angle = Math.atan2(getControlPoint(1).getY() - getControlPoint(0).getY(), getControlPoint(1).getX() - getControlPoint(0).getX());
      arrowPosTx.translate(getControlPoint(0).getX(), getControlPoint(0).getY());
      arrowPosTx.rotate((angle + Math.PI / 2d));
      AffineTransform oldTx = g2d.getTransform();
      g2d.transform(arrowPosTx);         
      g2d.fill(getArrow());
      g2d.setTransform(oldTx);
      
      // make the line slightly shorter so line end doesn't overlap with the arrow
      double distance = distance(startPoint, endPoint);
      interpolate(startPoint, endPoint, getArrowSize().convertToPixels() * 0.9 / distance, startPoint);
    }
    if (arrowEnd) {
      arrowPosTx.setToIdentity();
      double angle = Math.atan2(getControlPoint(1).getY() - getControlPoint(0).getY(), getControlPoint(1).getX() - getControlPoint(0).getX());
      arrowPosTx.translate(getControlPoint(1).getX(), getControlPoint(1).getY());
      arrowPosTx.rotate((angle - Math.PI / 2d));
      AffineTransform oldTx = g2d.getTransform();
      g2d.transform(arrowPosTx);   
      g2d.fill(getArrow());
      g2d.setTransform(oldTx);
      
      // make the line slightly shorter so line end doesn't overlap with the arrow
      double distance = distance(startPoint, endPoint);
      interpolate(endPoint, startPoint, getArrowSize().convertToPixels() * 0.9 / distance, endPoint);
    }
    
    g2d.drawLine((int)startPoint.getX(), (int)startPoint.getY(), (int)endPoint.getX(), (int)endPoint.getY());
  }
  
  private void interpolate(Point2D p1, Point2D p2, double t, Point2D p) {
    p.setLocation((int)Math.round(p1.getX() * (1-t) + p2.getX() * t), (int)Math.round(p1.getY() * (1-t) + p2.getY() * t));
  }
  
  private double distance(Point2D p1, Point2D p2) {
    return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
  }

  @Override
  public Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : color;
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
    if (thickness == null)
      thickness = new Size(1d, SizeUnit.px);
    return thickness;
  }
  
  public void setThickness(Size thickness) {
    this.thickness = thickness;   
  }
  
  @EditableProperty(name = "Arrow Size")
  public Size getArrowSize() {
    if (arrowSize == null)
      arrowSize = thickness = new Size(1d, SizeUnit.px); 
    return arrowSize;
  }
  
  public void setArrowSize(Size arrowSize) {
    this.arrowSize = arrowSize;
    arrow = null;
  }

  @Override
  public Color getLeadColor() {
    return super.getLeadColor();
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @EditableProperty(name = "Style")
  public LineStyle getStyle() {
    if (style == null)
      style = LineStyle.SOLID;
    return style;
  }

  public void setStyle(LineStyle style) {
    this.style = style;
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

  public Color getBodyColor() {
    return super.getBodyColor();
  }

  @Override
  public Color getBorderColor() {
    return super.getBorderColor();
  }

  @Override
  public Byte getAlpha() {
    return super.getAlpha();
  }

  @Override
  public Size getLength() {
    return super.getLength();
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  protected Shape getBodyShape() {
    return null;
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  protected Size getDefaultLength() {
    return null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }

  @Deprecated
  @Override
  public Color getLabelColor() {
    return super.getLabelColor();
  }

  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }

  @Deprecated
  @Override
  public Display getDisplay() {
    return super.getDisplay();
  }
  
  @Deprecated
  @Override
  public org.diylc.components.AbstractLeadedComponent.LabelOriantation getLabelOriantation() {
    return super.getLabelOriantation();
  }
}
