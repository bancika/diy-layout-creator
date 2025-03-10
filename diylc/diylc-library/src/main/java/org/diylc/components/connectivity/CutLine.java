/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components.connectivity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Cut Line", category = "Connectivity", author = "Branislav Stojkovic",
    description = "Cut line", instanceNamePrefix = "CL", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false, transformer = SimpleComponentTransformer.class)
public class CutLine extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Size WIDTH = new Size(0.125d, SizeUnit.in);
  public static Size LENGTH = new Size(3.125d, SizeUnit.in);
  public static Color COLOR = Color.black;

  private Size width = WIDTH;
  private Size length = LENGTH;
  private Color color = COLOR;
  private Point2D.Double point = new Point2D.Double(0, 0);
  private OrientationHV orientation = OrientationHV.VERTICAL;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    int w = getClosestOdd((int) getWidth().convertToPixels());
    int l = getClosestOdd((int) getLength().convertToPixels());
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : color);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(w));

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    if (getOrientation() == OrientationHV.HORIZONTAL)
      g2d.drawLine((int)point.getX(), (int)point.getY(), (int)point.getX() + l, (int)point.getY());
    else
      g2d.drawLine((int)point.getX(), (int)point.getY(), (int)point.getX(), (int)point.getY() + l);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.setColor(COLOR);
    g2d.drawLine(width / 2, height - 2, width / 2, 1);
  }

  @EditableProperty
  public Size getWidth() {
    return width;
  }

  public void setWidth(Size width) {
    this.width = width;
  }

  @EditableProperty
  public Size getLength() {
    return length;
  }

  public void setLength(Size length) {
    this.length = length;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point.setLocation(point);
  }

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }
}
