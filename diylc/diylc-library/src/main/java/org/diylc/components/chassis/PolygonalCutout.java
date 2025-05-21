/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
package org.diylc.components.chassis;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

import org.diylc.common.ObjectCache;
import org.diylc.components.shapes.AbstractShape;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "Polygonal Cutout", author = "Branislav Stojkovic", category = "Electro-Mechanical",
    instanceNamePrefix = "POLYC", description = "Polygonal chassis cutout", zOrder = IDIYComponent.CHASSIS + 0.1,
    bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false, transformer = SimpleComponentTransformer.class)
public class PolygonalCutout extends AbstractShape {

  private static final long serialVersionUID = 1L;

  protected PointCount pointCount = PointCount._4;

  public PolygonalCutout() {
    super();
    this.borderColor = LIGHT_METAL_COLOR.darker();
    this.controlPoints =
        new Point2D[] {new Point2D.Double(0, 0), 
            new Point2D.Double(0, (int) DEFAULT_HEIGHT.convertToPixels()),
            new Point2D.Double((int) DEFAULT_WIDTH.convertToPixels(), (int) DEFAULT_HEIGHT.convertToPixels()),
            new Point2D.Double((int) DEFAULT_WIDTH.convertToPixels(), 0)};
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((int) borderThickness.convertToPixels()));
    g2d.setColor(color);
    int[] xPoints = new int[controlPoints.length];
    int[] yPoints = new int[controlPoints.length];
    for (int i = 0; i < controlPoints.length; i++) {
      xPoints[i] = (int)controlPoints[i].getX();
      yPoints[i] = (int)controlPoints[i].getY();
    }

    Composite oldComposite = g2d.getComposite();
    if (this.alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(3, 1.0F * this.alpha / MAX_ALPHA));
    }
    g2d.fillPolygon(xPoints, yPoints, controlPoints.length);
    g2d.setComposite(oldComposite);

    // Do not track any changes that follow because the whole board has been
    // tracked so far.
    drawingObserver.stopTracking();
    Color lineColor =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : borderColor;
    g2d.setColor(lineColor);
    g2d.drawPolygon(xPoints, yPoints, controlPoints.length);
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
            new Point2D.Double((this.controlPoints[i - 1].getX() + this.controlPoints[newPointCount - 1].getX()) / 2,
                (this.controlPoints[i - 1].getY() + this.controlPoints[newPointCount - 1].getY()) / 2);
      }
    }
    this.pointCount = pointCount;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(COLOR);
    int[] x = {2 / factor, width - 2 / factor, width - 4 / factor, 3 / factor};
    int[] y = {4 / factor, 2 / factor, height - 5 / factor, height - 2 / factor};
    g2d.fillPolygon(x, y, 4);
    g2d.setColor(LIGHT_METAL_COLOR.darker());
    g2d.drawPolygon(x, y, 4);
  }

  public enum PointCount {
    _3, _4, _5, _6, _7, _8;

    public String toString() {
      return name().substring(1);
    };
  }
}
