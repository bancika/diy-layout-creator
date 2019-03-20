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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
--- import java Ellipse2d;

import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
//import org.diylc.core.measures.PowerUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Photo Resistor (symbol)", author = "N9XYP", category = "Schematics",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "R",
    description = "Photo Resistor schematic symbol", zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Schematic", transformer = SimpleComponentTransformer.class)
public class PhotoResistorSymbol extends AbstractSchematicLeadedSymbol<Resistance> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_LENGTH = new Size(0.3, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.08, SizeUnit.in);

  private Resistance value = null;
  
 // @Deprecated
 // private Power power = Power.HALF;
 // private org.diylc.core.measures.Power powerNew = new org.diylc.core.measures.Power(0.5, PowerUnit.W);

  @EditableProperty(name = "Dark Res.")
  public Resistance getValue() {
    return value;
  }

  public void setValue(Resistance value) {
    this.value = value;
  }

  /*@Override
  public String getValueForDisplay() {
    return getValue().toString() + (getPowerNew() == null ? "" : " " + getPowerNew().toString());
  }

  @Deprecated
  public Power getPower() {
    return power;
  }

  @Deprecated
  public void setPower(Power power) {
    this.power = power;
  }

  @EditableProperty(name = "Power rating")
  public org.diylc.core.measures.Power getPowerNew() {
    // Backward compatibility
    if (powerNew == null) {
      powerNew = power.convertToNewFormat();
      // Clear old value, don't need it anymore
      power = null;
    }
    return powerNew;
  }

  public void setPowerNew(org.diylc.core.measures.Power powerNew) {
    this.powerNew = powerNew;
  }
*/
  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_LENGTH;
  }

  @Override
  protected Shape getBodyShape() {
    GeneralPath polyline = new GeneralPath();
    double length = getLength().convertToPixels();
    double width = getWidth().convertToPixels();
    polyline.moveTo(0, width / 2);
    polyline.lineTo(length / 16, width);
    polyline.lineTo(3 * length / 16, 0);
    polyline.lineTo(5 * length / 16, width);
    polyline.lineTo(7 * length / 16, 0);
    polyline.lineTo(9 * length / 16, width);
    polyline.lineTo(11 * length / 16, 0);
    polyline.lineTo(13 * length / 16, width);
    polyline.lineTo(15 * length / 16, 0);
    polyline.lineTo(length, width / 2);
    return polyline;
//draw circle r=length/2 at length/2,width/2
//draw light arrows
  }


  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(0, height / 2, 4, height / 2);
    g2d.drawLine(width - 4, height / 2, width, height / 2);
    g2d.setColor(COLOR);
    g2d.drawPolyline(new int[] {4, 6, 10, 14, 18, 22, 26, 28}, new int[] {height / 2, height / 2 + 2, height / 2 - 2,
        height / 2 + 2, height / 2 - 2, height / 2 + 2, height / 2 - 2, height / 2}, 8);
//draw circle
//draw light arrows
  }

}
