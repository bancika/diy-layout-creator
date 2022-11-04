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
 * 
 */
package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import org.diylc.components.passive.CapacitorDimensionService.CapacitorDimensions;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.VoltageUnit;

@ComponentDescriptor(name = "Film Capacitor (Radial)", author = "Branislav Stojkovic",
    category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C",
    description = "Radial film capacitor, similar to Sprague Orange Drop",
    zOrder = IDIYComponent.COMPONENT, transformer = SimpleComponentTransformer.class, 
    enableDatasheet = true, datasheetCreationStepCount = 3)
public class RadialFilmCapacitor extends AbstractFilmCapacitor {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#FF8000");
  public static Color BORDER_COLOR = BODY_COLOR.darker();

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);

  private Size pinSpacing = PIN_SPACING;

  private AutoSize autoSize = AutoSize.OFF;

  public RadialFilmCapacitor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }
  
  public RadialFilmCapacitor(String[] parameters) {
    this();
    String autoSizeValue = parameters[0];
    Double voltageValue = Double.parseDouble(parameters[1].split(" ")[0]);
    Double capacitanceValue = Double.parseDouble(parameters[2].split(" ")[0]);
    setValue(new Capacitance(capacitanceValue, CapacitanceUnit.uF));
    setVoltageNew(new org.diylc.core.measures.Voltage(voltageValue, VoltageUnit.V));

    for (AutoSize size : AutoSize.values()) {
      if (size.toString().equalsIgnoreCase(autoSizeValue)) {
        setAutoSize(size);
      }
    }
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(4, height / 2 - 3, width - 8, 6, 5, 5);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(4, height / 2 - 3, width - 8, 6, 5, 5);
  }

  @Override
  protected Shape getBodyShape() {
    double lengthFinal = getLength().convertToPixels();
    double widthFinal = getWidth().convertToPixels();

    double radius = widthFinal * 0.7;
    return new RoundRectangle2D.Double(0f, 0f, lengthFinal, getClosestOdd(widthFinal), radius,
        radius);
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

  @EditableProperty(name = "Auto Size")
  public AutoSize getAutoSize() {
    if (autoSize == null) {
      autoSize = AutoSize.OFF;
    }
    return autoSize;
  }

  public void setAutoSize(AutoSize autoSize) {
    this.autoSize = autoSize;
  }
  
  @EditableProperty
  @Override
  public Size getLength() {    
    AutoSize autoSize = getAutoSize();
    if (autoSize != null && autoSize != AutoSize.OFF && getVoltageNew() != null
        && getVoltageNew().getNormalizedValue() != null && getValue() != null
        && getValue().getNormalizedValue() != null) {

      CapacitorDimensions d = CapacitorDimensionService.getInstance().lookup(this.getClass(),
          autoSize.toString(), getVoltageNew(), getValue());

      if (d != null) {
        return d.getLength();
      }
    }

    return super.getLength();
  }
  
  @EditableProperty
  @Override
  public Size getWidth() { 
    AutoSize autoSize = getAutoSize();
    if (autoSize != null && autoSize != AutoSize.OFF && getVoltageNew() != null
        && getVoltageNew().getNormalizedValue() != null && getValue() != null
        && getValue().getNormalizedValue() != null) {

      CapacitorDimensions d = CapacitorDimensionService.getInstance().lookup(this.getClass(),
          autoSize.toString(), getVoltageNew(), getValue());

      if (d != null) {
        return d.getDiameter();
      }
    }
    return super.getWidth();
  }

  private static enum AutoSize {
    OFF("Off"), ORANGE_DROP_715P("Orange Drop 715P"), ORANGE_DROP_716P("Orange Drop 716P");

    private String label;

    AutoSize(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
