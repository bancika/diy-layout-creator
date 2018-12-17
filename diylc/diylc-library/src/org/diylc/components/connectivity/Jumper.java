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
import java.awt.Shape;

import org.diylc.common.Display;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

@ComponentDescriptor(name = "Jumper", author = "Branislav Stojkovic", category = "Connectivity",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "J", description = "",
    zOrder = IDIYComponent.COMPONENT, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class, continuity = true)
public class Jumper extends AbstractLeadedComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.blue;

  @Deprecated
  private Color color;
  protected LineStyle style = LineStyle.SOLID; 

  public Jumper() {
    super();
    this.leadColor = COLOR;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
    g2d.setColor(COLOR.darker());
    g2d.drawLine(1, height - 2, width - 2, 1);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR);
    g2d.drawLine(1, height - 2, width - 2, 1);
  }

  @Override
  public Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : getLeadColor();
  }

  @Override
  @EditableProperty(name = "Color")
  public Color getLeadColor() {
    if (color != null) {
      this.leadColor = color;
    }
    return super.getLeadColor();
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
}
