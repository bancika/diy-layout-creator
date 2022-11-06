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
import java.awt.geom.Rectangle2D;
import org.diylc.components.passive.CapacitorDatasheetService.CapacitorDatasheet;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.annotations.ComponentDescriptor;

@ComponentDescriptor(name = "Film Capacitor (Axial)", author = "Branislav Stojkovic",
    category = "Passive", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C",
    description = "Axial film capacitor, similar to Mallory 150s", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class, enableDatasheet = true,
    datasheetCreationStepCount = 3)
public class AxialFilmCapacitor extends AbstractFilmCapacitor implements IDatasheetSupport {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#FFE303");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color OUTER_FOIL_COLOR = Color.white;

  public AxialFilmCapacitor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  public AxialFilmCapacitor(String[] model) {
    this();

    applyModel(model);
  }
  
  @Override
  public void applyModel(String[] model) {
    CapacitorDatasheet d = CapacitorDatasheetService.parseAxialCapacitorDatasheet(model);

    setType(d.getType());
    if (d.getCapacitance() != null)
      setValue(d.getCapacitance());
    if (d.getVoltage() != null)
      setVoltageNew(d.getVoltage());
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
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(4, height / 2 - 3, width - 8, 6);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(4, height / 2 - 3, width - 8, 6);
  }

  @Override
  protected Shape getBodyShape() {
    double lengthFinal = getLength().convertToPixels();
    double widthFinal = getWidth().convertToPixels();

    return new Rectangle2D.Double(0f, 0f, lengthFinal, getClosestOdd(widthFinal));
  }

  @Override
  protected boolean supportsStandingMode() {
    return true;
  }
}
