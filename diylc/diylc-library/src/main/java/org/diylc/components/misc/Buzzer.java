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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.components.passive.AbstractRadialComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;

@ComponentDescriptor(name = "PCB Buzzer", author = "M0JXD", category = "Misc",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "BZ",
    description = "Vertically mounted Buzzer, active or passive", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class Buzzer extends AbstractRadialComponent<Voltage> {
  // This component was forked from the Radial Capacitor, massive thanks to Bancika!
  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_SIZE = new Size(12d, SizeUnit.mm);
  public static Color BODY_COLOR = Color.decode("#333333");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color MARKER_COLOR = Color.decode("#8CACEA");
  public static Color HOLE_COLOR = Color.decode("#9a97a8");
  public static Size HEIGHT = new Size(8.5d, SizeUnit.mm);
  public static Size EDGE_RADIUS = new Size(1d, SizeUnit.mm);
  public static Size PIN_SPACING = new Size(0.3d, SizeUnit.in);
  
  private Voltage value = null;
  private Color markerColor = MARKER_COLOR;
  private Color holeColor = HOLE_COLOR;
  private boolean polarized = false;
  private boolean folded = false;
  private Size height = HEIGHT;
  private boolean invert = false;

  public Buzzer() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.labelColor = Color.white;
    setPinSpacing(PIN_SPACING);
  }
  
  public Buzzer(String[] model) {
    this();
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Voltage getValue() {
    return value;
  }

  public void setValue(Voltage value) {
    this.value = value;
  }

  @Override
  public String getValueForDisplay() {
    return (getValue() == null ? "" : getValue().toString()) + (getValue() == null ? "" : " " + getValue().toString());
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    int margin = 3;
    Ellipse2D body =
        new Ellipse2D.Double(margin, margin, getClosestOdd(width - 2 * margin), getClosestOdd(width - 2 * margin));
    g2d.fill(body);
    g2d.draw(body);
      
    // Add centre hole
    
    g2d.setColor(HOLE_COLOR);
    double start_point = (width / 2) - (margin / 4) - 0.4;
    Ellipse2D hole =
            new Ellipse2D.Double(start_point, start_point, margin * 1.4, margin * 1.4);
    g2d.fill(hole);
    g2d.draw(hole);
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
	  
    double width = getLength().convertToPixels();
    
	  // Add the hole in the middle
	  if (!folded) {
	    g2d.setColor(HOLE_COLOR);
	    double start_point = (width / 2) - width * 0.1;
	    Ellipse2D hole =
	            new Ellipse2D.Double(start_point, start_point, width * 0.2, width * 0.2);
	    g2d.fill(hole);
	    g2d.draw(hole);
	  }
	  
	  // Add polarity mark if desired
	  if (polarized && !folded) {
      g2d.setColor(Color.white);
      
      // Vertical
      g2d.draw(new Line2D.Double(width * 0.19, width * 0.4, width * 0.19, width * 0.6));
      // Horizontal
      g2d.draw(new Line2D.Double(width * 0.08, width * 0.5, width * 0.30, width * 0.5));

	  }
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  protected Size getDefaultLength() {
    // We'll reuse width property to set the diameter.
    return DEFAULT_SIZE;
  }

  @EditableProperty(name = "Diameter")
  @Override
  public Size getLength() {
    return super.getLength();
  }

  @EditableProperty(name = "Marker")
  public Color getMarkerColor() {
    return markerColor;
  }

  public void setMarkerColor(Color coverColor) {
    this.markerColor = coverColor;
  }

  @EditableProperty(name = "Hole")
  public Color getHoleColor() {
    return holeColor;
  }

  public void setHoleColor(Color holeColor) {
    this.holeColor = holeColor;
  }

  @EditableProperty(name = "Polarized")
  public boolean getPolarized() {
    return polarized;
  }

  public void setPolarized(boolean polarized) {
    this.polarized = polarized;
  }

  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
  }

  @EditableProperty
  public Size getHeight() {
    if (height == null) {
      height = HEIGHT;
    }
    return height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  @EditableProperty(name = "Invert Polarity")
  public boolean getInvert() {
    return invert;
  }

  public void setInvert(boolean invert) {
    this.invert = invert;
  }

  @Override
  protected Shape getBodyShape() {
    double height = (int) getHeight().convertToPixels();
    double diameter = (int) getLength().convertToPixels();
    if (folded) {
      return new RoundRectangle2D.Double(0f, -height / 2 - LEAD_THICKNESS.convertToPixels() / 2,
        getClosestOdd(diameter), getClosestOdd(height), EDGE_RADIUS.convertToPixels(), EDGE_RADIUS.convertToPixels());
    }
    return new Ellipse2D.Double(0f, 0f, getClosestOdd(diameter), getClosestOdd(diameter));
  }
}
