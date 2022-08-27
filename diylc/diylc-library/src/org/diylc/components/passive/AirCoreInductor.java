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
package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Inductance;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

//@ComponentDescriptor(name = "Air-Core Inductor", author = "Branislav Stojkovic", category = "Passive",
//    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "L",
//    description = "Air-core inductor mounted horizontally or vertically", zOrder = IDIYComponent.COMPONENT,
//    transformer = SimpleComponentTransformer.class)
public class AirCoreInductor extends AbstractLeadedComponent<Inductance> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(8d, SizeUnit.mm);
  public static Size DEFAULT_HEIGHT = new Size(14d, SizeUnit.mm);
  public static Color BODY_COLOR = new Color(0, 0, 0, 0);
  public static Color LABEL_COLOR = Color.white;
  public static Color BORDER_COLOR = BODY_COLOR;
  public static int BAND_SPACING = 5;
  public static int FIRST_BAND = -4;

  private Inductance value = null;
  private Resistance resistance = null;

  public AirCoreInductor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.leadColor = COPPER_COLOR;
    this.labelColor = LABEL_COLOR;
  }

  @Override
  protected boolean supportsStandingMode() {
    return false;
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Inductance getValue() {
    return value;
  }

  public void setValue(Inductance value) {
    this.value = value;
  }

  @Override
  public String getValueForDisplay() {
    return getValue() == null ? "" : getValue().toString();
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(COPPER_COLOR);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    Rectangle2D a = new Rectangle2D.Double(width / 2 - 5, 6, 10, height - 14);
    g2d.fill(a);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(a);
    g2d.setColor(COPPER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    for (int i = 0; i < 9; i++) {
      g2d.drawLine(width / 2 - 5, 7 + 2 * i, width / 2 + 5, 7 + 2 * i);
    }        
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_HEIGHT;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_WIDTH;
  }


  @Override
  protected Shape getBodyShape() {
    double width = getWidth().convertToPixels();
    double length = getLength().convertToPixels();
    return new Rectangle2D.Double(0f, 0f, length, getClosestOdd(width));
  }
  
  @Override
  protected boolean decorateAboveBorder() {
    return true;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (outlineMode)
      return;
    Area body = new Area(getBodyShape());
    int leadThickness = (int) getLeadThickness();
    Stroke stroke = ObjectCache.getInstance().fetchBasicStroke(leadThickness / 2);
    Rectangle rect = body.getBounds();
    Area copper = new Area();
    for (double x = leadThickness / 2; x < rect.width; x += leadThickness * 0.7d) {
      double margin = 0;     
      Line2D line = new Line2D.Double(x, -margin, x, rect.height + margin);
      Shape s = stroke.createStrokedShape(line);
      copper.add(new Area(s));
    }
//    copper.intersect(body);
    g2d.setColor(COPPER_COLOR);
    g2d.fill(copper);
    g2d.setColor(COPPER_COLOR.darker());
    g2d.draw(copper);
  }

  @Deprecated
  public boolean getFlipStanding() {
    return super.getFlipStanding();
  }
  
  @EditableProperty
  public Resistance getResistance() {
    return resistance;
  }
  
  public void setResistance(Resistance resistance) {
    this.resistance = resistance;
  }
}
