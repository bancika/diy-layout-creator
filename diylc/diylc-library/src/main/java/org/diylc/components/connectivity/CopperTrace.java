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
import java.awt.Shape;
import java.util.EnumSet;
import java.util.Set;

import com.bancika.gerberwriter.GerberFunctions;

import org.diylc.common.ObjectCache;
import org.diylc.common.PCBLayer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ILayeredComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.gerber.GerberLayer;
import org.diylc.core.gerber.GerberRenderMode;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Copper Trace", author = "Branislav Stojkovic", category = "Connectivity",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "Trace",
    description = "Straight copper trace", zOrder = IDIYComponent.TRACE, bomPolicy = BomPolicy.NEVER_SHOW,
    autoEdit = false, keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "PCB",
    transformer = SimpleComponentTransformer.class)
public class CopperTrace extends AbstractLeadedComponent<Void> implements ILayeredComponent, IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  public static Size THICKNESS = new Size(1d, SizeUnit.mm);
  public static Color COLOR = Color.black;

  private Size thickness = THICKNESS;
  private PCBLayer layer = PCBLayer._1;

  public CopperTrace() {
    super();
    this.leadColor = COLOR;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
    g2d.setColor(COLOR);
    g2d.drawLine(1, height - 2, width - 2, 1);
  }

  @Override
  protected Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : getLeadColor();
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  @EditableProperty(name = "Color")
  public Color getLeadColor() {
    return leadColor;
  }

  @EditableProperty(name = "Width")
  public Size getThickness() {
    return thickness;
  }

  public void setThickness(Size thickness) {
    this.thickness = thickness;
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
  public int getLayerId() {   
    return getLayer().getId();
  }

  @Override
  protected float getLeadThickness() {
    return (float) getThickness().convertToPixels();
  }

  @Override
  protected boolean shouldShadeLeads() {
    return false;
  }

  public Color getBodyColor() {
    return super.getBodyColor();
  }

  @Override
  public Color getBorderColor() {
    return super.getBorderColor();
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
  protected boolean IsCopperArea() {
    return true;
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }
  
  @Deprecated
  @Override
  public Integer getFontSizeOverride() {
    // TODO Auto-generated method stub
    return super.getFontSizeOverride();
  }

  @Deprecated
  @Override
  public LabelOriantation getLabelOriantation() {
    // TODO Auto-generated method stub
    return super.getLabelOriantation();
  }
  
  @Deprecated
  @Override
	public Color getLabelColor() {
		// TODO Auto-generated method stub
		return super.getLabelColor();
	}
  
  public boolean getMoveLabel() {
    // override to disable edit
    return false;
  }

  @Override
  public Set<GerberRenderMode> getGerberRenderModes() {
    return EnumSet.of(GerberRenderMode.Normal);
  }

  @Override
  public GerberLayer getGerberLayer() {
    return this.getLayer().toGerberCopperLayer();
  }

  @Override
  public String getGerberFunction() {
    return GerberFunctions.CONDUCTOR;
  }

  @Override
  public boolean isGerberNegative() {
    return false;
  }
}
