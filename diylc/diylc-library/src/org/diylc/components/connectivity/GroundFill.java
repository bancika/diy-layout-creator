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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;

import org.diylc.common.ObjectCache;
import org.diylc.common.PCBLayer;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractComponent;
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

@ComponentDescriptor(name = "Ground Fill", author = "Branislav Stojkovic", category = "Connectivity",
    instanceNamePrefix = "GF", description = "Polygonal ground fill area", zOrder = IDIYComponent.TRACE,
    bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false, transformer = SimpleComponentTransformer.class)
public class GroundFill extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;
  public static Size DEFAULT_WIDTH = new Size(1.5d, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(1.2d, SizeUnit.in);

  protected Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, (int) DEFAULT_HEIGHT.convertToPixels()),
      new Point((int) DEFAULT_WIDTH.convertToPixels(), (int) DEFAULT_HEIGHT.convertToPixels()),
      new Point((int) DEFAULT_WIDTH.convertToPixels(), 0)};

  protected Color color = COLOR;
  protected PointCount pointCount = PointCount._4;
  private PCBLayer layer = PCBLayer._1;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Color fillColor =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : color;
    g2d.setColor(fillColor);
    int[] xPoints = new int[controlPoints.length];
    int[] yPoints = new int[controlPoints.length];
    for (int i = 0; i < controlPoints.length; i++) {
      xPoints[i] = controlPoints[i].x;
      yPoints[i] = controlPoints[i].y;
    }
    drawingObserver.startTrackingContinuityArea(true);
    g2d.fillPolygon(xPoints, yPoints, controlPoints.length);
    drawingObserver.stopTrackingContinuityArea();
    // Do not track any changes that follow because the whole board has been
    // tracked so far.
    drawingObserver.stopTracking();
  }

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
  }

  @EditableProperty(name = "Edges")
  public PointCount getPointCount() {
    return pointCount;
  }

  public void setPointCount(PointCount pointCount) {
    if (this.pointCount == pointCount)
      return;
    int oldPointCount = Integer.parseInt(this.pointCount.toString());
    int newPointCount = Integer.parseInt(pointCount.toString());
    this.controlPoints = Arrays.copyOf(this.controlPoints, newPointCount);
    if (oldPointCount < newPointCount) {
      this.controlPoints[newPointCount - 1] = this.controlPoints[oldPointCount - 1];
      for (int i = oldPointCount - 1; i < newPointCount - 1; i++) {
        this.controlPoints[i] =
            new Point((this.controlPoints[i - 1].x + this.controlPoints[newPointCount - 1].x) / 2,
                (this.controlPoints[i - 1].y + this.controlPoints[newPointCount - 1].y) / 2);
      }
    }
    this.pointCount = pointCount;
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


  @Deprecated
  @Override
  public Void getValue() {
    return null;
  }

  @Deprecated
  @Override
  public void setValue(Void value) {}
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(COLOR);
    int[] x = {2 / factor, width - 2 / factor, width - 4 / factor, 3 / factor};
    int[] y = {4 / factor, 2 / factor, height - 5 / factor, height - 2 / factor};
    g2d.fillPolygon(x, y, 4);
  }

  public enum PointCount {
    _3, _4, _5, _6, _7, _8;

    public String toString() {
      return name().substring(1);
    };
  }
}
