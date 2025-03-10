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
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.components.passive.CapacitorDatasheetService.CapacitorDatasheet;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;

@ComponentDescriptor(name = "Mica Capacitor (Radial)", author = "Branislav Stojkovic",
    category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C",
    description = "Standard radial mica capacitor", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class, enableDatasheet = true,
    datasheetCreationStepCount = 3)
public class RadialMicaCapacitor extends AbstractRadialComponent<Capacitance> implements IDatasheetSupport {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(11.4, SizeUnit.mm);
  public static Size DEFAULT_HEIGHT = new Size(4.3, SizeUnit.mm);
  public static Color BODY_COLOR = Color.decode("#876A6C");
  public static Color BORDER_COLOR = BODY_COLOR.darker();

  private Capacitance value = null;
  private Voltage voltage = null;

  public RadialMicaCapacitor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  public RadialMicaCapacitor(String[] model) {
    this();
    applyModel(model);
  }

  @Override
  public void applyModel(String[] model) {
    CapacitorDatasheet d = CapacitorDatasheetService.parseRadialCapacitorDatasheet(model);

    setType(d.getType());
    if (d.getCapacitance() != null)
      setValue(d.getCapacitance());
    if (d.getVoltage() != null)
      setVoltage(d.getVoltage());
    if (d.getBodyColor() != null)
      setBodyColor(d.getBodyColor());
    if (d.getBorderColor() != null)
      setBorderColor(d.getBorderColor());
    if (d.getLabelColor() != null)
      setLabelColor(d.getLabelColor());
    if (d.getLength() != null)
      setLength(d.getLength());
    if (d.getWidth() != null)
      setWidth(d.getWidth());
    if (d.getLeadSpacing() != null)
      setPinSpacing(d.getLeadSpacing());
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Capacitance getValue() {
    return value;
  }

  public void setValue(Capacitance value) {
    this.value = value;
  }

  @Override
  public String getValueForDisplay() {
    return (getValue() == null ? "" : getValue().toString())
        + (getVoltage() == null ? "" : " " + getVoltage().toString());
  }

  @EditableProperty(name = "Voltage")
  public Voltage getVoltage() {
    return voltage;
  }

  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    Area a = new Area(new Rectangle2D.Double(6, height / 2 - 3, width - 14, 6));
    a.add(new Area(new Ellipse2D.Double(4, height / 2 - 4, 8, 8)));
    a.add(new Area(new Ellipse2D.Double(width - 12, height / 2 - 4, 8, 8)));
    g2d.fill(a);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(a);
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
    double length = getLength().convertToPixels();
    double width = getClosestOdd(getWidth().convertToPixels());
    Rectangle2D rect =
        new Rectangle2D.Double(width / 2, width / 10, length - width, width * 8 / 10);
    Area a = new Area(rect);
    a.add(new Area(new Ellipse2D.Double(0, 0, width, width)));
    a.add(new Area(new Ellipse2D.Double(length - width, 0, width, width)));
    return a;
  }
}
