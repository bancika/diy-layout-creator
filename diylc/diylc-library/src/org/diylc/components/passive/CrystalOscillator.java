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
package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Frequency;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Crystal Oscillator", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "X",
    description = "Radial crystal oscillator", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class CrystalOscillator extends  AbstractLeadedComponent<Frequency>  {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.lightGray;
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  
  public static Size DEFAULT_LENGTH = new Size(0.4d, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.15d, SizeUnit.in);
  
  public static Size PIN_SPACING = new Size(0.2d, SizeUnit.in);
  public static Size INNER_RING_MARGIN = new Size(0.5d, SizeUnit.mm);

  private Size pinSpacing = PIN_SPACING;
  
  private Frequency value;

  public CrystalOscillator() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(0, height / 2 - 5, width, 10, 10, 10);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(0, height / 2 - 5, width, 10, 10, 10);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(2, height / 2 - 3, width - 4, 6, 6, 6);
  }

  @Override
  protected Shape getBodyShape() {
    double radius = getWidth().convertToPixels();
    return new RoundRectangle2D.Double(0f, 0f, getLength().convertToPixels(), getClosestOdd(getWidth()
        .convertToPixels()), radius, radius);
  }
  
  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    // draw inner ring
    double margin = INNER_RING_MARGIN.convertToPixels();
    double width = getWidth().convertToPixels() - 2 * margin;
    double length = getLength().convertToPixels() - 2 * margin;
    double radius = width;
    RoundRectangle2D rect = new RoundRectangle2D.Double(margin, margin, length, getClosestOdd(width), radius, radius);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(getBorderColor());
    g2d.draw(rect);
  }

  @Override
  protected int calculatePinSpacing(Rectangle shapeRect) {
    return (int) getPinSpacing().convertToPixels();
  }

  @EditableProperty(name = "Pin Spacing")
  public Size getPinSpacing() {
    if (pinSpacing == null) {
      pinSpacing = PIN_SPACING;
    }
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
  }

  @EditableProperty
  @Override
  public Frequency getValue() {
    return value;
  }

  @Override
  public void setValue(Frequency value) {
    this.value = value;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_LENGTH;
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_WIDTH;
  }
}
