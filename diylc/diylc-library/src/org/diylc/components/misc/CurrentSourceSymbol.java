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
package org.diylc.components.misc;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.CurrentUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Current Source", author = "JD", category = "Schematic Symbols", creationMethod = CreationMethod.POINT_BY_POINT, 
                     instanceNamePrefix = "I", description = "Current Source schematic symbol", zOrder = IDIYComponent.COMPONENT)

/**
 * Component type for Current Source Symbol, options for 2 drawing styles.
 * Modified from the Voltage Source Symbol component. Author: JD
 * 
 * @author JD
 */
public class CurrentSourceSymbol extends AbstractSchematicLeadedSymbol<String> {

  // FIELDS
  private static final long serialVersionUID = 1L;
  private String value = "Current Source";
  private SourceStyle source_Style = SourceStyle.Standard;

  // Overall size
  public static Size DEFAULT_LENGTH = new Size(0.2, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.2, SizeUnit.in);

  // Sets current to 100mA by default
  private org.diylc.core.measures.Current currentNew = new org.diylc.core.measures.Current(100d, CurrentUnit.mA);

  // METHODS

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getCurrentNew() == null ? "" : " " + getCurrentNew().toString());
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty(name = "Current")
  public org.diylc.core.measures.Current getCurrentNew() {
    return currentNew;
  }

  public void setVoltageNew(org.diylc.core.measures.Current currentNew) {
    this.currentNew = currentNew;
  }

  /**
   * Returns whether the source drawing style is Standard or Alternate. Uses Enum
   * "SourceStyle" Author: JD (Modified Bancika's code, credit to Bancika)
   * @return
   */
  @EditableProperty(name = "Source Style")
  public SourceStyle getSourceStyle() {
    if (source_Style == null)
      source_Style = SourceStyle.Standard;
    return source_Style;
  }

  /**
   * Sets whether the source is standard or alternate. Uses Enum "SourceStyle"
   * Author: JD (Modified Bancika's code, credit to Bancika)
   * @param sourceStyle
   */
  public void setSourceStyle(SourceStyle source_Style) {
    this.source_Style = source_Style;
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

    switch (getSourceStyle()) {
    case Standard:
      // Main body is a circle, drawn with an Ellipse
      body.append(new Ellipse2D.Double(0d, 0d, width, length), true);
      break;
    case Alternate:
      // Main body is two circles
      body.append(new Ellipse2D.Double(0d, 0d, length, length), true);
      body.moveTo(length * 1.5 , length / 2); // Prevents a closing line being drawn
      body.append(new Ellipse2D.Double(length / 2, 0d, length, length), true);
      break;
    }
    return (Shape) body;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    double width  = getWidth().convertToPixels();
    double height = getLength().convertToPixels();
  
    g2d.setColor(getBorderColor());
    switch (getSourceStyle()) {
    case Standard: // Draw arrow inside main circle
      g2d.draw(new Line2D.Double(0.25 * width, height / 2, 
                                 0.75 * width, height / 2));

      g2d.draw(new Line2D.Double(0.75 * width, height / 2,
                                (0.75 * width) - 0.45 * (0.25 * width), (height / 2) + height / 5));
          
      g2d.draw(new Line2D.Double(0.75 * width, height / 2,
                                (0.75 * width) - 0.45 * (0.25 * width), (height / 2) - height / 5));

      break;
    case Alternate: // Draw arrow below the two circles
      g2d.draw(new Line2D.Double(0.25 * width * 1.5, height * 1.5, 
                                 0.75 * width * 1.5, height * 1.5));

      g2d.draw(new Line2D.Double(0.75 * width * 1.5, height * 1.5,
                                (0.75 * width * 1.5) - 0.45 * (0.25 * width * 1.5), (height  * 1.5) + height / 5));

      g2d.draw(new Line2D.Double(0.75 * width * 1.5, height  * 1.5,
                                (0.75 * width * 1.5) - 0.45 * (0.25 * width * 1.5), (height  * 1.5) - height / 5));
      

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

    // Draw arrow
    
    // g2d.setStroke();
    g2d.draw(new Line2D.Double(startx + 0.25 * width / 2, starty + height / 4, 
                               startx + 0.75 * width / 2, starty + height / 4));
    
    g2d.draw(new Line2D.Double(startx + 0.25 * width / 2, starty + height / 4,
                               startx + (0.25 * width / 2) + 0.3 * (startx + (0.25 * width / 2)), (starty + height / 4) + height / 9));
                               
    g2d.draw(new Line2D.Double(startx + 0.25 * width / 2, starty + height / 4,
                               startx + (0.25 * width / 2) + 0.3 * (startx + (0.25 * width / 2)), (starty + height / 4) - height / 9));
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + (index == 0 ? "+" : "-");
  }

  public enum SourceStyle {
    Standard, Alternate
  }
}