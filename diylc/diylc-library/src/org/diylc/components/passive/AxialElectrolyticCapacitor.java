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
package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Electrolytic Capacitor (Axial)", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "C",
    description = "Axial electrolytic capacitor, similar to Sprague Atom, F&T, etc", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class, enableCache = true)
public class AxialElectrolyticCapacitor extends AbstractLeadedComponent<Capacitance> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
  public static Color BODY_COLOR = Color.decode("#6B6DCE");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color MARKER_COLOR = Color.decode("#8CACEA");
  public static Color TICK_COLOR = Color.white;

  private Capacitance value = null;
  @Deprecated
  private Voltage voltage = Voltage._63V;
  private org.diylc.core.measures.Voltage voltageNew = null;

  private Color markerColor = MARKER_COLOR;
  private Color tickColor = TICK_COLOR;
  private boolean polarized = true;

  public AxialElectrolyticCapacitor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.labelColor = TICK_COLOR;
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
    return getValue().toString() + (getVoltageNew() == null ? "" : " " + getVoltageNew().toString());
  }

  @Deprecated
  public Voltage getVoltage() {
    return voltage;
  }

  @Deprecated
  public void setVoltage(Voltage voltage) {
    this.voltage = voltage;
  }

  @EditableProperty(name = "Voltage")
  public org.diylc.core.measures.Voltage getVoltageNew() {
    return voltageNew;
  }

  public void setVoltageNew(org.diylc.core.measures.Voltage voltageNew) {
    this.voltageNew = voltageNew;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(4, height / 2 - 3, width - 8, 6);
    g2d.setColor(MARKER_COLOR);
    g2d.fillRect(width - 9, height / 2 - 3, 5, 6);
    g2d.setColor(TICK_COLOR);
    g2d.drawLine(width - 6, height / 2 - 1, width - 6, height / 2 + 1);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(4, height / 2 - 3, width - 8, 6);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_HEIGHT;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_WIDTH;
  }

  @EditableProperty(name = "Marker")
  public Color getMarkerColor() {
    return markerColor;
  }

  public void setMarkerColor(Color coverColor) {
    this.markerColor = coverColor;
  }

  @EditableProperty(name = "Tick")
  public Color getTickColor() {
    return tickColor;
  }

  public void setTickColor(Color tickColor) {
    this.tickColor = tickColor;
  }

  @EditableProperty(name = "Polarized")
  public boolean getPolarized() {
    return polarized;
  }

  public void setPolarized(boolean polarized) {
    this.polarized = polarized;
  }

  @Override
  protected Shape getBodyShape() {
    double length = getLength().convertToPixels();
    double width = getWidth().convertToPixels();
    RoundRectangle2D rect = new RoundRectangle2D.Double(0f, 0f, length, width, width / 6, width / 6);
    Area a = new Area(rect);
    double notchDiameter = width / 4;
    a.subtract(new Area(new Ellipse2D.Double(notchDiameter, -notchDiameter * 3 / 4, notchDiameter, notchDiameter)));
    a.subtract(new Area(new Ellipse2D.Double(notchDiameter, width - notchDiameter / 4, notchDiameter, notchDiameter)));

    if (!getPolarized()) {
      a.subtract(new Area(new Ellipse2D.Double(length - notchDiameter * 2, -notchDiameter * 3 / 4, notchDiameter,
          notchDiameter)));
      a.subtract(new Area(new Ellipse2D.Double(length - notchDiameter * 2, width - notchDiameter / 4, notchDiameter,
          notchDiameter)));
    }
    return a;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    int width = (int) getWidth().convertToPixels();
    int length = (int) getLength().convertToPixels();
    g2d.setColor(blend(getBorderColor(), getBodyColor()));
    int notchDiameter = width / 4;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawLine(notchDiameter, 0, notchDiameter, width);
    g2d.drawLine(notchDiameter * 2, 0, notchDiameter * 2, width);
    if (polarized) {
      int markerLength = (int) (getLength().convertToPixels() * 0.2);
      if (!outlineMode) {
        g2d.setColor(markerColor);
        Rectangle2D markerRect = new Rectangle2D.Double(length - markerLength, 0, markerLength + 2, width);
        Area markerArea = new Area(markerRect);
        markerArea.intersect((Area) getBodyShape());
        g2d.fill(markerArea);
      }
      Color finalTickColor;
      if (outlineMode) {
        Theme theme =
            (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
        finalTickColor = theme.getOutlineColor();
      } else {
        finalTickColor = tickColor;
      }
      g2d.setColor(finalTickColor);
      g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(2));
      g2d.drawLine((int) getLength().convertToPixels() - markerLength / 2, (int) (width / 2 - width * 0.15),
          (int) getLength().convertToPixels() - markerLength / 2, (int) (width / 2 + width * 0.15));
    } else {
      g2d.drawLine(length - notchDiameter, 0, length - notchDiameter, width);
      g2d.drawLine(length - notchDiameter * 2, 0, length - notchDiameter * 2, width);
    }
  }

  public static Color blend(Color c0, Color c1) {
    double totalAlpha = c0.getAlpha() + c1.getAlpha();
    double weight0 = c0.getAlpha() / totalAlpha;
    double weight1 = c1.getAlpha() / totalAlpha;

    double r = weight0 * c0.getRed() + weight1 * c1.getRed();
    double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
    double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
    double a = Math.max(c0.getAlpha(), c1.getAlpha());

    return new Color((int) r, (int) g, (int) b, (int) a);
  }
}
