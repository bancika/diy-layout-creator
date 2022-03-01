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
import java.awt.geom.Path2D;

import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.PCBLayer;
import org.diylc.components.AbstractCurvedComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Curved Trace", author = "Branislav Stojkovic", category = "Connectivity",
    instanceNamePrefix = "Trace", description = "Curved copper trace with two control points",
    zOrder = IDIYComponent.TRACE, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "PCB", transformer = SimpleComponentTransformer.class, 
    enableCache = true)
public class CurvedTrace extends AbstractCurvedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;
  public static Size SIZE = new Size(1d, SizeUnit.mm);

  protected Size size = SIZE;
  private PCBLayer layer = PCBLayer._1;

  @Override
  protected Color getDefaultColor() {
    return COLOR;
  }

  @Override
  protected void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState, IDrawingObserver drawingObserver) {
    float thickness = (float) size.convertToPixels();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness));
    Color curveColor =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
            : color;
    g2d.setColor(curveColor);
    drawingObserver.startTrackingContinuityArea(true);
    g2d.draw(curve);
    drawingObserver.stopTrackingContinuityArea();
  }

  @EditableProperty(name = "Width")
  public Size getThickness() {
    return size;
  }

  public void setThickness(Size size) {
    this.size = size;
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
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  public Byte getAlpha() {
    return super.getAlpha();
  }

  @Override
  public void setAlpha(Byte alpha) {
    super.setAlpha(alpha);
  }
  
  @Override
  public LineStyle getStyle() {   
    return super.getStyle();
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }
}
