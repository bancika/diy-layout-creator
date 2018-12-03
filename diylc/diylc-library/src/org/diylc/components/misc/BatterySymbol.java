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

import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Battery (symbol)", author = "N9XYP", category = "Schematics",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "B", description = "Battery schematic symbol",
    zOrder = IDIYComponent.COMPONENT)
public class BatterySymbol extends AbstractSchematicLeadedSymbol<String> {

  private static final long serialVersionUID = 1L;

  private String value = "Battery";

  public static Size DEFAULT_LENGTH = new Size(0.05, SizeUnit.in); // plate spacing
  public static Size DEFAULT_WIDTH = new Size(0.15, SizeUnit.in); // plate size

  private org.diylc.core.measures.Voltage voltageNew = new org.diylc.core.measures.Voltage(9d, VoltageUnit.V);

  // sets battery voltage to 9V


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
    double length = getLength().convertToPixels(); // plate spacing
    double width = getWidth().convertToPixels(); // plate size
    polyline.moveTo(0, 0); // start point
    polyline.lineTo(0, width); // draw first plate (positive)
                               // 2nd plate width = w/4 + w/2 + w/4
                               // w - w/4 = w/2 pixels
    polyline.moveTo(length, width / 4); // start 2nd plate
    polyline.lineTo(length, width - width / 4); // draw second plate w/2 pixels (negative)
    return polyline;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    // Draw + sign.
    g2d.setColor(getBorderColor());
    int plusSize = getClosestOdd(getWidth().convertToPixels() / 4); // 1/4 length of plus sign
    int x = -plusSize; // center point
    int y = plusSize; // end point
    g2d.drawLine(x - plusSize / 2, y - plusSize, x + plusSize / 2, y - plusSize); // line from
                                                                                  // X-center point
                                                                                  // +- 1/2 length
                                                                                  // about y-center
    g2d.drawLine(x, y - plusSize / 2, x, y - plusSize * 3 / 2); // line from y-center point +- 1/2
                                                                // length about x-center
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(COLOR);
    g2d.drawLine(0, height / 2, 13 * width / 32, height / 2); // draw leads
    g2d.drawLine(width - 13 * width / 32, height / 2, width, height / 2);
    g2d.setColor(COLOR);
    g2d.drawLine(14 * width / 32, height / 2 - 6 * width / 32, 14 * width / 32, height / 2 + 6 * width / 32); // pos
                                                                                                              // plate
    g2d.drawLine(width - 14 * width / 32, height / 2 - 3, width - 14 * width / 32, height / 2 + 3 * width / 32); // neg
                                                                                                                 // plate
    // plus sign
    g2d.drawLine(height / 2 - 4 * width / 32, height / 2 - 5 * width / 32, height / 2 - 8 * width / 32, height / 2 - 5
        * width / 32);
    g2d.drawLine(height / 2 - 6 * width / 32, height / 2 - 3 * width / 32, height / 2 - 6 * width / 32, height / 2 - 7
        * width / 32);
  }


}
