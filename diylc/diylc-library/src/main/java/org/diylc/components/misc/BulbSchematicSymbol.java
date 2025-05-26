/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.components.misc;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Bulb", author = "JD", category = "Schematic Symbols", creationMethod = CreationMethod.POINT_BY_POINT, 
                     instanceNamePrefix = "LMP", description = "Bulb schematic symbol", zOrder = IDIYComponent.COMPONENT)

/**
 * Component type for Pilot Light/Bulb Schematic Symbol
 * Modified from the Current Source Symbol component. Author: JD
 * @author JD
 */
public class BulbSchematicSymbol extends AbstractSchematicLeadedSymbol<String> {

  private static final long serialVersionUID = 1L;
  private String value = "Bulb / Pilot Light / Lamp Symbol";

  // Overall size
  public static Size DEFAULT_LENGTH = new Size(0.2, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.2, SizeUnit.in);

  // Sets filament voltage to 6.3V by default
  private org.diylc.core.measures.Voltage voltageNew = new org.diylc.core.measures.Voltage(6.3d, VoltageUnit.V);

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getVoltageNew() == null ? "" : " " + getVoltageNew().toString());
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty(name = "Filament Voltage")
  public org.diylc.core.measures.Voltage getVoltageNew() {
    return voltageNew;
  }

  public void setVoltageNew(org.diylc.core.measures.Voltage voltageNew) {
    this.voltageNew = voltageNew;
  }

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
    double width = getWidth().convertToPixels();
    double length = getLength().convertToPixels();
    GeneralPath body = new GeneralPath();
    // Main body is a circle, drawn with an Ellipse
    body.append(new Ellipse2D.Double(0d, 0d, width, length), true);
    return (Shape) body;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    double width  = getWidth().convertToPixels();
    double height = getLength().convertToPixels();
    g2d.setColor(getBorderColor());
    // Draw cross inside main circle - 0.707 seems to have under-shoot on first coordinate? Maybe just me.
    g2d.draw(new Line2D.Double(width / 2 - (0.707106 * width / 2), height / 2 - 0.707106 * (height / 2), 
                               width / 2 + (0.707106 * width / 2), height / 2 + 0.707106 * height / 2));
    
    g2d.draw(new Line2D.Double(width / 2 + (0.707106 * width / 2), height / 2 - (0.707106 * height / 2),
                               width / 2 - (0.707106 * width / 2), height / 2 + 0.707106 * height / 2));
                               
  }
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);

    // Draw Leads
    g2d.draw(new Line2D.Double(0, height / 2, width / 4, height / 2));
    g2d.draw(new Line2D.Double(width, height / 2, width * 0.75, height / 2));
    
    // Draw Ellipse
    g2d.setColor(COLOR);
    double startx = width / 4;
    double starty = height / 4;
    g2d.draw(new Ellipse2D.Double(startx, starty, 0.5 * width, 0.5 * height));

    // Draw Cross
    // g2d.setStroke();
    
    g2d.draw(new Line2D.Double(startx + width / 4 - 0.707 * (width / 4), starty + height / 4 - 0.707 * (height / 4), 
                               startx + width / 4 + 0.707 * (width / 4), starty + height / 4 + 0.707 * height / 4));
    
    g2d.draw(new Line2D.Double(startx + width / 4 + 0.707 * width / 4, starty + height / 4 - 0.707 * height / 4,
                               startx + width / 4 - 0.707 * width / 4, starty + height / 4 + 0.707 * height / 4));
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + (index == 0 ? "+" : "-");
  }
}
