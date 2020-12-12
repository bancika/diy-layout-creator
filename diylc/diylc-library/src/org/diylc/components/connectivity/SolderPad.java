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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.PCBLayer;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Solder Pad", category = "Connectivity", author = "Branislav Stojkovic",
    description = "Copper solder pad, round or square", instanceNamePrefix = "Pad",
    zOrder = IDIYComponent.TRACE + 0.1, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "PCB", transformer = SimpleComponentTransformer.class, enableCache = true)
public class SolderPad extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Size SIZE = new Size(0.09d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(0.8d, SizeUnit.mm);
  public static Color COLOR = Color.black;

  private Size size = SIZE;
  private Color color = COLOR;
  private Point2D.Double point = new Point2D.Double(0, 0);
  private Type type = Type.ROUND;
  private Size holeSize = HOLE_SIZE;
  private PCBLayer layer = PCBLayer._1;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    double diameter = getSize().convertToPixels();
    double holeDiameter = getHoleSize().convertToPixels();
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : color);
    drawingObserver.startTrackingContinuityArea(true);
    if (type == Type.ROUND) {
      g2d.fill(new Ellipse2D.Double(point.getX() - diameter / 2, point.getY() - diameter / 2, diameter, diameter));
    } else if (type == Type.OVAL_HORIZONTAL) {
      g2d.fill(new Ellipse2D.Double(point.getX() - diameter / 2, point.getY() - diameter * 3 / 8, diameter, diameter * 3 / 4));
    } else if (type == Type.OVAL_VERTICAL) {
      g2d.fill(new Ellipse2D.Double(point.getX() - diameter * 3 / 8, point.getY() - diameter / 2, diameter * 3 / 4, diameter));
    } else {
      g2d.fill(new Rectangle2D.Double(point.getX() - diameter / 2, point.getY() - diameter / 2, diameter, diameter));
    }
    drawingObserver.stopTrackingContinuityArea();
    if (getHoleSize().getValue() > 0) {
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(point.getX() - holeDiameter / 2, point.getY() - holeDiameter / 2, holeDiameter, holeDiameter));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int diameter = getClosestOdd(width / 2);
    int holeDiameter = 5;
    g2d.setColor(COLOR);
    g2d.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval((width - holeDiameter) / 2, (height - holeDiameter) / 2, holeDiameter, holeDiameter);
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @EditableProperty(name = "Hole", validatorClass = PositiveMeasureValidator.class)
  public Size getHoleSize() {
    if (holeSize == null) {
      holeSize = HOLE_SIZE;
    }
    return holeSize;
  }

  public void setHoleSize(Size holeSize) {
    this.holeSize = holeSize;
  }

  @EditableProperty
  public PCBLayer getLayer() {
    if (layer == null) {
      layer = PCBLayer._1;
    }
    return layer;
  }

  public void setLayer(PCBLayer layer) {
    this.layer = layer;
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
    return true;
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
  public Color getLeadColor() {
    return color;
  }

  public void setLeadColor(Color color) {
    this.color = color;
  }

  @EditableProperty
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public Void getValue() {
    return null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }

  @Override
  public void setValue(Void value) {}
  
  @Override
  public Rectangle2D getCachingBounds() {
    double size = getSize().convertToPixels();
    return new Rectangle2D.Double(point.getX() - size, point.getY() - size, size * 2, size * 2);
  }

  public static enum Type {
    ROUND, SQUARE, OVAL_HORIZONTAL, OVAL_VERTICAL;

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase().replace('_', ' ');
    }
  }
}
