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
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Voltage Source", author = "JD", category = "Schematic Symbols", creationMethod = CreationMethod.POINT_BY_POINT, 
		     instanceNamePrefix = "V", description = "Voltage Source schematic symbol", zOrder = IDIYComponent.COMPONENT)

/**
 * Component type for Voltage Source Symbol, options for both AC and DC.
 * Modified from the Battery Symbol component. Author: JD
 * 
 * @author JD
 */
public class VoltageSourceSymbol extends AbstractSchematicLeadedSymbol<String> {

  private static final long serialVersionUID = 1L;
  private String value = "Voltage Source";
  private SourceType source_Type = SourceType.DC;

  // Overall size
  public static Size DEFAULT_LENGTH = new Size(0.2, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.2, SizeUnit.in);

  // Sets voltage to 5V by default
  private org.diylc.core.measures.Voltage voltageNew = new org.diylc.core.measures.Voltage(5d, VoltageUnit.V);

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

  @EditableProperty(name = "Voltage")
  public org.diylc.core.measures.Voltage getVoltageNew() {
    return voltageNew;
  }

  public void setVoltageNew(org.diylc.core.measures.Voltage voltageNew) {
    this.voltageNew = voltageNew;
  }

  /**
   * Returns whether the source is AC or DC. Uses Enum "SourceType" Author: JD
   * (Modified Bancika's code, credit to Bancika)
   * 
   * @return
   */
  @EditableProperty(name = "Source Type")
  public SourceType getSourceType() {
    if (source_Type == null)
      source_Type = SourceType.DC;
    return source_Type;
  }

  /**
   * Sets whether the source is AC or DC. Uses Enum "SourceType" Author: JD
   * (Modified Bancika's code, credit to Bancika)
   * 
   * @param source_Type
   */
  public void setSourceType(SourceType source_Type) {
    this.source_Type = source_Type;
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
    // Main body is a circle, drawn with an Ellipse
    double width = getWidth().convertToPixels();
    double length = getLength().convertToPixels();
    return new Ellipse2D.Double(0d, 0d, width, length);
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    g2d.setColor(getBorderColor());
    double arbritaryLength = getWidth().convertToPixels();
    switch (getSourceType()) {
    
    case AC: // AC Source, inside will be a sine wave
      // Cubic curve format is: (x1, y1, cx1, cy1, cx2, cy2, x2, y2)
      g2d.draw(new CubicCurve2D.Double(0.2 * arbritaryLength, arbritaryLength / 2,                     // x1, y1
                                       0.5 * arbritaryLength, -arbritaryLength * 0.3,                  // cx1, cy1
                                       0.5 * arbritaryLength, arbritaryLength + arbritaryLength * 0.3, // cx2, cy2
                                       0.8 * arbritaryLength, arbritaryLength / 2));                   // x2, y2
      break;
      
    case DC: // DC Source, inside will have "+" & "-" signs
      double plusSize = arbritaryLength / 6; // Plus sign is 1/6 of overall size
      // Draw "+" sign
      g2d.draw(new Line2D.Double(arbritaryLength * 0.1, arbritaryLength / 2, 
                                 arbritaryLength * 0.1 + plusSize, arbritaryLength / 2));

      g2d.draw(new Line2D.Double(arbritaryLength * 0.1 + plusSize / 2, arbritaryLength / 2 + plusSize / 2,
                                 arbritaryLength * 0.1 + plusSize / 2, arbritaryLength / 2 - plusSize / 2));
      // Draw "-" Sign
      g2d.draw(new Line2D.Double(arbritaryLength * 0.76 + plusSize / 2, arbritaryLength / 2 + plusSize / 2,
                                 arbritaryLength * 0.76 + plusSize / 2, arbritaryLength / 2 - plusSize / 2));
      break;
    default:
    }
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    // g2d.rotate(-Math.PI, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);

    // Draw Leads
    g2d.draw(new Line2D.Double(0, height / 2, width / 4, height / 2));
    g2d.draw(new Line2D.Double(width, height / 2, width * 0.75, height / 2));

    g2d.setColor(COLOR);
    // Draw Ellipse
    double startx = width / 4;
    double starty = height / 4;

    g2d.draw(new Ellipse2D.Double(startx, starty, 0.5 * width, 0.5 * height));

    // Draw signs
    double arbritaryLength = width * 0.5;
    double plusSize = arbritaryLength / 5; // Plus sign is 1/5 of overall size

    // Draw "+" sign
    g2d.draw(new Line2D.Double(startx + arbritaryLength * 0.12, starty + arbritaryLength / 2,
                               startx + arbritaryLength * 0.12 + plusSize, starty + arbritaryLength / 2));

    g2d.draw(
        new Line2D.Double(startx + arbritaryLength * 0.12 + plusSize / 2, starty + arbritaryLength / 2 + plusSize / 2,
                          startx + arbritaryLength * 0.12 + plusSize / 2, starty + arbritaryLength / 2 - 1 * (plusSize / 2)));
    // Draw "-" Sign
    g2d.draw(
        new Line2D.Double(startx + arbritaryLength * 0.72 + plusSize / 2, starty + arbritaryLength / 2 + plusSize / 2,
                          startx + arbritaryLength * 0.72 + plusSize / 2, starty + arbritaryLength / 2 - 1 * (plusSize / 2)));
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + (index == 0 ? "+" : "-");
  }

  public enum SourceType {
    AC, DC
  }
}
