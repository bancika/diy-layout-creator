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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Hookup Wire", author = "Branislav Stojkovic", category = "Connectivity",
    instanceNamePrefix = "W", description = "Flexible wire with two control points", zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class, continuity = true)
public class HookupWire extends AbstractCurvedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.green;
  public static double INSULATION_THICKNESS_PCT = 0.3;

  protected AWG gauge = AWG._22;

  @Override
  protected Color getDefaultColor() {
    return COLOR;
  }

  @Override
  protected void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState, IDrawingObserver drawingObserver) {
    int thickness =
        (int) (Math.pow(Math.E, -1.12436 - 0.11594 * gauge.getValue()) * Constants.PIXELS_PER_INCH * (1 + 2 * INSULATION_THICKNESS_PCT)) - 1;
    Color curveColor =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : color;
    g2d.setColor(curveColor);
    Stroke stroke = null;
    switch (getStyle()) {
      case SOLID:
        stroke = ObjectCache.getInstance().fetchBasicStroke(thickness);
        break;
      case DASHED:
        stroke =
            ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness * 2, thickness * 3}, thickness * 4, BasicStroke.CAP_SQUARE);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness / 4, thickness * 3}, 0, BasicStroke.CAP_ROUND);
        break;
    }
    Shape s = stroke.createStrokedShape(curve);    
    g2d.fill(s);
    if (componentState == ComponentState.NORMAL) {
      g2d.setColor(color.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
//      switch (getStyle()) {
//        case SOLID:
//          stroke = ObjectCache.getInstance().fetchBasicStroke(thickness - 2);
//          break;
//        case DASHED:
//          stroke =
//              ObjectCache.getInstance().fetchStroke(thickness - 2, new float[] {thickness * 2, thickness * 3},
//                  thickness * 4);
//          break;
//        case DOTTED:
//          stroke = ObjectCache.getInstance().fetchStroke(thickness - 2, new float[] {thickness / 4, thickness * 3}, 0);
//      }
//      g2d.setStroke(stroke);
      g2d.draw(s);
    }
  }

  @EditableProperty(name = "AWG")
  public AWG getGauge() {
    return gauge;
  }

  public void setGauge(AWG gauge) {
    this.gauge = gauge;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
}
