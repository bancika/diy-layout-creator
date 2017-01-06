package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Diode (glass)", author = "Branislav Stojkovic", category = "Semiconductors",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D",
    description = "Glass diode, like most small signal diodes.", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class DiodeGlass extends AbstractLeadedComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(0.2, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(0.09, SizeUnit.in);
  public static Size MARKER_WIDTH = new Size(1d, SizeUnit.mm);
  public static Color INSIDE_COLOR = Color.decode("#E66E31");
  public static Color BODY_COLOR = Color.decode("#E1F0FF");
  public static Color MARKER_COLOR = Color.gray;
  public static Color LABEL_COLOR = Color.white;
  public static Color BORDER_COLOR = Color.gray;

  private String value = "";
  private Color markerColor = MARKER_COLOR;
  private Color insideColor = INSIDE_COLOR;

  public DiodeGlass() {
    super();
    this.labelColor = LABEL_COLOR;
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  @Override
  protected boolean supportsStandingMode() {
    return true;
  }

  @Override
  public Color getStandingBodyColor() {
    return getFlipStanding() ? getBodyColor() : getMarkerColor();
  }

  @EditableProperty(name = "Reverse (standing)")
  public boolean getFlipStanding() {
    return super.getFlipStanding();
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(6, height / 2 - 3, width - 12, 6);
    g2d.setColor(INSIDE_COLOR);
    g2d.fillRect(7, height / 2 - 2, width - 14, 4);
    g2d.setColor(MARKER_COLOR);
    int markerWidth = 4 * width / 32;
    g2d.fillRect(width - 6 - markerWidth, height / 2 - 3, markerWidth, 6);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(6, height / 2 - 3, width - 12, 6);
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
    return new Rectangle2D.Double(0f, 0f, getLength().convertToPixels(), getClosestOdd(getWidth().convertToPixels()));
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    Color finalMarkerColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalMarkerColor = theme.getOutlineColor();
    } else {
      finalMarkerColor = markerColor;
    }

    int length = getClosestOdd(getLength().convertToPixels());
    // draw the inside
    if (!outlineMode) {
      g2d.setColor(getInsideColor());
      int width = getClosestOdd(getWidth().convertToPixels());
      int insideLength = getClosestOdd(length - 0.2 * width);
      int insideWidth = getClosestOdd(width * 0.8);
      g2d.fillRect((int) (length - insideLength) / 2, (int) (width - insideWidth) / 2, insideLength, insideWidth);
    }

    g2d.setColor(finalMarkerColor);
    int markerWidth = (int) MARKER_WIDTH.convertToPixels();
    g2d.fillRect((int) (length - markerWidth), 0, markerWidth, getClosestOdd(getWidth().convertToPixels()));
  }

  @EditableProperty(name = "Marker")
  public Color getMarkerColor() {
    return markerColor;
  }

  public void setMarkerColor(Color markerColor) {
    this.markerColor = markerColor;
  }

  @EditableProperty(name = "Inside color")
  public Color getInsideColor() {
    if (insideColor == null)
      insideColor = INSIDE_COLOR;
    return insideColor;
  }

  public void setInsideColor(Color insideColor) {
    this.insideColor = insideColor;
  }
}
