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
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import java.lang.Math;
import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Buzzer Symbol", author = "JD", category = "Schematic Symbols",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "BZ", description = "Buzzer schematic symbol",
    zOrder = IDIYComponent.COMPONENT)
public class BuzzerSymbol extends AbstractSchematicLeadedSymbol<String> {
  private static final long serialVersionUID = 1L;

  private String value = "Buzzer Symbol";

  public static Size DEFAULT_LENGTH = new Size(48.0, SizeUnit.px);
  public static Size DEFAULT_WIDTH = new Size(120.0, SizeUnit.px);

  private org.diylc.core.measures.Voltage voltageNew = new org.diylc.core.measures.Voltage(12d, VoltageUnit.V);

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
	  // Divide by two as the whole thing sits "above" zero
	  // see useShapeRectAsPosition to understand
	  double length = getLength().convertToPixels();
	  double width  = getWidth().convertToPixels() / 2;
	  
	  Arc2D.Double arc = new Arc2D.Double(0, 0, length, length, 180, 180, Arc2D.CHORD);
	  
	  // width * 0.75 is an approx. Sqrt method seems a bit off? (maybe my maths sucks...)
	  Line2D.Double left_leads  = new Line2D.Double(length * 0.25, width * 0.75, length * 0.25, width);
	  Line2D.Double right_leads = new Line2D.Double(length * 0.75, width * 0.75, length * 0.75, width);
	  
	  Line2D.Double left_leads_bottom  = new Line2D.Double(length * 0.25, width, 0, width);
	  Line2D.Double right_leads_bottom = new Line2D.Double(length * 0.75, width, length, width);
	  
	  GeneralPath buzzer = new GeneralPath();
	  buzzer.append(arc, false);
	  buzzer.append(left_leads, false);
	  buzzer.append(right_leads, false);
	  buzzer.append(right_leads_bottom, false);
	  buzzer.append(left_leads_bottom, false);
	  return buzzer;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    // Could maybe add a Plus Sign for active buzzers?
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
	  g2d.rotate(-Math.PI / 4, width / 2, height / 2);
	  g2d.setColor(COLOR);
	  
	  Arc2D.Double arc = new Arc2D.Double(width * 0.2, height * 0.1, width * 0.6, height * 0.6, 180, 180, Arc2D.CHORD);
	  g2d.draw(arc);
	  
	  Line2D.Double left_leads = new Line2D.Double(width * 0.35, height * 0.681, width * 0.35, height * 0.8);
	  Line2D.Double right_leads = new Line2D.Double(width * 0.65, width * 0.681, width * 0.65, height * 0.8);
	  g2d.draw(left_leads);
	  g2d.draw(right_leads);
	  
	  Line2D.Double left_leads_bottom = new Line2D.Double(width * 0.2, height * 0.8, width * 0.35, height * 0.8);
	  Line2D.Double right_leads_bottom = new Line2D.Double(width * 0.65, height * 0.8, width * 0.8, height * 0.8);
	  g2d.draw(left_leads_bottom);
	  g2d.draw(right_leads_bottom);
	  
    // extensions in lead colour
	  g2d.setColor(LEAD_COLOR);
	  Line2D.Double left_lead_ext = new Line2D.Double(0, height * 0.8, width * 0.2, height * 0.8);
	  Line2D.Double right_lead_ext = new Line2D.Double(width * 0.8, height * 0.8, width, height * 0.8);
	  g2d.draw(left_lead_ext);
	  g2d.draw(right_lead_ext);
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + (index == 0 ? "+" : "-");
  }
  
  @Override
  protected boolean useShapeRectAsPosition() {
	  return false;
  } 
}
