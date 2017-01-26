package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.ResistorColorCode;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Resistor", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "R", description = "Resistor layout symbol",
    zOrder = IDIYComponent.COMPONENT, transformer = SimpleComponentTransformer.class)
public class Resistor extends AbstractLeadedComponent<Resistance> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(1d / 2, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
  public static Color BODY_COLOR = Color.decode("#82CFFD");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static int BAND_SPACING = 5;
  public static int FIRST_BAND = -4;

  private Resistance value = null;
  @Deprecated
  private Power power = Power.HALF;
  private org.diylc.core.measures.Power powerNew = null;
  private ResistorColorCode colorCode = ResistorColorCode._5_BAND;
  private ResistorShape shape = ResistorShape.Standard;

  public Resistor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  @Override
  protected boolean supportsStandingMode() {
    return true;
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Resistance getValue() {
    return value;
  }

  public void setValue(Resistance value) {
    this.value = value;
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString() + (getPowerNew() == null ? "" : " " + getPowerNew().toString());
  }

  @Deprecated
  public Power getPower() {
    return power;
  }

  @Deprecated
  public void setPower(Power power) {
    this.power = power;
  }

  @EditableProperty(name = "Power rating")
  public org.diylc.core.measures.Power getPowerNew() {
    return powerNew;
  }

  public void setPowerNew(org.diylc.core.measures.Power powerNew) {
    this.powerNew = powerNew;
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
    g2d.setColor(Color.red);
    g2d.drawLine(11, height / 2 - 3, 11, height / 2 + 3);
    g2d.setColor(Color.orange);
    g2d.drawLine(14, height / 2 - 3, 14, height / 2 + 3);
    g2d.setColor(Color.black);
    g2d.drawLine(17, height / 2 - 3, 17, height / 2 + 3);
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

  @EditableProperty(name = "Color code")
  public ResistorColorCode getColorCode() {
    return colorCode;
  }

  public void setColorCode(ResistorColorCode colorCode) {
    this.colorCode = colorCode;
  }

  @EditableProperty
  public ResistorShape getShape() {
    if (shape == null)
      shape = ResistorShape.Standard;
    return shape;
  }

  public void setShape(ResistorShape shape) {
    this.shape = shape;
  }

  @Override
  protected Shape getBodyShape() {
    if (shape == ResistorShape.Standard) {
      double length = getLength().convertToPixels();
      double width = getClosestOdd(getWidth().convertToPixels());
      Rectangle2D rect = new Rectangle2D.Double(width / 2, width / 10, length - width, width * 8 / 10);
      Area a = new Area(rect);
      a.add(new Area(new Ellipse2D.Double(0, 0, width, width)));
      a.add(new Area(new Ellipse2D.Double(length - width, 0, width, width)));
      return a;
    }
    return new Rectangle2D.Double(0f, 0f, getLength().convertToPixels(), getClosestOdd(getWidth().convertToPixels()));
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    // int width = getWidth().convertToPixels();
    if (colorCode == ResistorColorCode.NONE || outlineMode || value == null) {
      return;
    }
    if (shape == ResistorShape.Standard) {
      int width = getClosestOdd(getWidth().convertToPixels());
      Color[] bands = value.getColorCode(colorCode);
      int x = width + FIRST_BAND;
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
      for (int i = 0; i < bands.length; i++) {
        g2d.setColor(bands[i]);
        g2d.drawLine(x, width / 10 + 2, x, width * 8 / 10 + 1);
        x += BAND_SPACING;
      }
    } else {
      int height = getClosestOdd(getWidth().convertToPixels());
      Color[] bands = value.getColorCode(colorCode);
      int x = FIRST_BAND;
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
      for (int i = 0; i < bands.length; i++) {
        g2d.setColor(bands[i]);
        g2d.drawLine(x, 1, x, height - 1);
        x += BAND_SPACING;
      }
    }
  }

  @Override
  protected int getLabelOffset(int bodyWidth, int labelWidth) {
    if (value == null)
      return 0;
    int width = getClosestOdd(getWidth().convertToPixels());
    Color[] bands = value.getColorCode(colorCode);
    int bandArea = width + FIRST_BAND + BAND_SPACING * (bands.length - 1);
    // Only offset the label if overlapping with the band area.
    if (labelWidth > bodyWidth - 2 * bandArea)
      return BAND_SPACING * (bands.length - 1);
    return 0;
  }

  @EditableProperty(name = "Reverse (standing)")
  public boolean getFlipStanding() {
    return super.getFlipStanding();
  }

  public enum ResistorShape {
    Tubular, Standard
  }
}
