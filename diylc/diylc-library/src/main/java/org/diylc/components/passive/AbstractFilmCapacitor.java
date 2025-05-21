/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.Theme;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractFilmCapacitor extends AbstractLeadedComponent<Capacitance> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
  public static Color OUTER_FOIL_COLOR = Color.white;

  protected Capacitance value = null;
  @Deprecated
  protected Voltage voltage = Voltage._63V;
  protected org.diylc.core.measures.Voltage voltageNew = null;
  protected Boolean showOuterFoil = false;
  protected Color outerFoilColor = OUTER_FOIL_COLOR;
  protected String type;

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Capacitance getValue() {
    return value;
  }

  public void setValue(Capacitance value) {
    this.value = value;
  }
  
  @EditableProperty
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }

  @Deprecated
  public Voltage getVoltage() {
    return voltage;
  }

  @Deprecated
  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }

  @Override
  public String getValueForDisplay() {
    return (getValue() == null ? "" : getValue().toString())
        + (getVoltageNew() == null ? "" : " " + getVoltageNew().toString());
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
    return DEFAULT_HEIGHT;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_WIDTH;
  }

  @EditableProperty(name = "Outer Foil Mark")
  public Boolean getShowOuterFoil() {
    if (showOuterFoil == null)
      showOuterFoil = false;
    return showOuterFoil;
  }

  public void setShowOuterFoil(Boolean showOuterFoil) {
    this.showOuterFoil = showOuterFoil;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (!getShowOuterFoil())
      return;
    int width = (int) getWidth().convertToPixels();
    int offset = (int) (getLength().convertToPixels() * 0.08);
    Color finalTickColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalTickColor = theme.getOutlineColor();
    } else {
      finalTickColor = getOuterFoilColor();
    }
    g2d.setColor(finalTickColor);
    g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke((float) (width * 0.1)));
    g2d.drawLine((int) getLength().convertToPixels() - offset,
        (int) (width / 2 - width * 0.35), (int) getLength().convertToPixels() - offset,
        (int) (width / 2 + width * 0.35));
  }

  @EditableProperty(name = "Outer Foil")
  public Color getOuterFoilColor() {
    if (outerFoilColor == null)
      outerFoilColor = OUTER_FOIL_COLOR;
    return outerFoilColor;
  }

  public void setOuterFoilColor(Color outerFoilColor) {
    this.outerFoilColor = outerFoilColor;
  }
}
