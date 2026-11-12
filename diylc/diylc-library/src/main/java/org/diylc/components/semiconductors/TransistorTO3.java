/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.awt.TwoCircleTangent;
import org.diylc.common.*;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.semiconductors.AbstractTransistorPackage.TransistorDisplay;
import org.diylc.core.ComponentState;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.ISpiceMapper;
import org.diylc.utils.Constants;

@AiNote("The first control point is pin 1; with 'Orientation' DEFAULT pin 2 is 10.9mm below it and the mounting holes lie left and right of the pins, which sit 1.8mm left of the case centre. The third control point is the case, at the centre of the left mounting hole: connect a wire or solder lug there to reach the collector")
@AiNote("'Pinout' values list pin 1, pin 2, then the case, e.g. B-E-C for a 2N3055 whose case is the collector")
@ComponentDescriptor(name = "Transistor (TO-3)", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "Q", description = "Transistor with large metal body",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true,
    enableDatasheet = true, datasheetCreationStepCount = 2, datasheetModelColumn = 1)
public class TransistorTO3 extends AbstractLabeledComponent<String>
    implements ISpiceMapper, IDatasheetSupport {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.lightGray;
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color LABEL_COLOR = Color.black;

  public static Size LARGE_DIAMETER = new Size(26.2d, SizeUnit.mm);
  public static Size INNER_DIAMETER = new Size(21.3d, SizeUnit.mm);
  public static Size SMALL_DIAMETER = new Size(8d, SizeUnit.mm);
  public static Size HOLE_DISTANCE = new Size(30.1d, SizeUnit.mm);
  public static Size HOLE_SIZE = new Size(4.1d, SizeUnit.mm);
  public static Size PIN_SPACING = new Size(10.9d, SizeUnit.mm);
  public static Size PIN_OFFSET = new Size(1.85d, SizeUnit.mm);
  public static Size PIN_DIAMETER = new Size(1d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  // pin 1, pin 2 and the case, reached through the mounting hole on the side of the pins
  private Point2D[] controlPoints =
      new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient private Area[] body;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  @Deprecated
  protected Display display = Display.NAME;
  private TransistorDisplay transistorDisplay = TransistorDisplay.NAME;
  private TransistorPinout pinout;

  public TransistorTO3() {
    super();
    updateControlPoints();
    this.alphaPercent = new Percentage(100);
  }

  public TransistorTO3(String[] model) {
    this();

    applyModel(model);
  }

  // columns: group, part number, pinout of pin 1, pin 2 and the case
  @Override
  public void applyModel(String[] model) {
    setValue(model[1]);
    setPinout(TransistorPinout.valueOf(model[2]));
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty
  public TransistorPinout getPinout() {
    return pinout;
  }

  public void setPinout(TransistorPinout pinout) {
    this.pinout = pinout;
  }

  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return getControlPoints()[index];
  }

  // files saved before the case point existed hold only the two pins
  private Point2D[] getControlPoints() {
    if (controlPoints.length < 3) {
      controlPoints = new Point2D[] {controlPoints[0], controlPoints[1], new Point2D.Double(0, 0)};
      updateCasePoint();
    }
    return controlPoints;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (getPinout() != null) {
      return getPinout().toPinout().substring(index, index + 1);
    }
    return super.getControlPointNodeName(index);
  }

  @Override
  public int mapToSpiceNode(int index) {
    if (getPinout() == null) {
      return index;
    }
    return getPinout().mapToSpiceNode(index);
  }

  @Override
  public String getComment() {
    return getPinout() == null ? "Pinout not configured, validate this line"
        : (getPinout().toString() + " pinout");
  }

  @Override
  public String getPrefix() {
    if (getPinout() == null) {
      return null;
    }
    return getPinout().name().substring(0, 1);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    getControlPoints()[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point2D[] controlPoints = getControlPoints();
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(x, y + pinSpacing);
        break;
      case _90:
        controlPoints[1].setLocation(x - pinSpacing, y);
        break;
      case _180:
        controlPoints[1].setLocation(x, y - pinSpacing);
        break;
      case _270:
        controlPoints[1].setLocation(x + pinSpacing, y);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
    updateCasePoint();
  }

  // the centre of the mounting hole on the side the pins are offset towards, rounded as getBody()
  // rounds it
  private void updateCasePoint() {
    int pinOffset = (int) PIN_OFFSET.convertToPixels();
    int holeDistance = getClosestOdd(HOLE_DISTANCE.convertToPixels());
    double x = (controlPoints[0].getX() + controlPoints[1].getX()) / 2;
    double y = (controlPoints[0].getY() + controlPoints[1].getY()) / 2;
    switch (orientation) {
      case DEFAULT:
        controlPoints[2].setLocation(x + pinOffset - holeDistance / 2, y);
        break;
      case _90:
        controlPoints[2].setLocation(x, y + pinOffset - holeDistance / 2);
        break;
      case _180:
        controlPoints[2].setLocation(x - pinOffset + holeDistance / 2, y);
        break;
      case _270:
        controlPoints[2].setLocation(x, y - pinOffset + holeDistance / 2);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      Point2D[] controlPoints = getControlPoints();
      body = new Area[2];
      int pinOffset = (int) PIN_OFFSET.convertToPixels();
      double x = (controlPoints[0].getX() + controlPoints[1].getX()) / 2;
      double y = (controlPoints[0].getY() + controlPoints[1].getY()) / 2;

      switch (orientation) {
        case DEFAULT:
          x += pinOffset;
          break;
        case _90:
          y += pinOffset;
          break;
        case _180:
          x -= pinOffset;
          break;
        case _270:
          y -= pinOffset;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      int largeDiameter = getClosestOdd(LARGE_DIAMETER.convertToPixels());
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      int smallDiameter = getClosestOdd(SMALL_DIAMETER.convertToPixels());
      int holeDistance = getClosestOdd(HOLE_DISTANCE.convertToPixels());
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      
      TwoCircleTangent left = new TwoCircleTangent(new Point2D.Double(x, y), new Point2D.Double(x - holeDistance / 2, y), largeDiameter / 2, smallDiameter / 2);
      TwoCircleTangent right = new TwoCircleTangent(new Point2D.Double(x, y), new Point2D.Double(x + holeDistance / 2, y), largeDiameter / 2, smallDiameter / 2);
      
      body[0] = left;
      body[0].add(right);

      body[0].subtract(new Area(new Ellipse2D.Double(x - holeDistance / 2 - holeSize / 2, y - holeSize / 2, holeSize,
          holeSize)));
      body[0].subtract(new Area(new Ellipse2D.Double(x + holeDistance / 2 - holeSize / 2, y - holeSize / 2, holeSize,
          holeSize)));

      switch (orientation) {
        case DEFAULT:
          break;
        case _90:
          body[0].transform(AffineTransform.getRotateInstance(Math.PI / 2, x, y));
          break;
        case _180:
          body[0].transform(AffineTransform.getRotateInstance(Math.PI, x, y));
          break;
        case _270:
          body[0].transform(AffineTransform.getRotateInstance(Math.PI * 3 / 2, x, y));
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      body[1] =
          new Area(new Ellipse2D.Double(x - innerDiameter / 2, y - innerDiameter / 2, innerDiameter, innerDiameter));
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    drawingObserver.stopTracking();
    int pinSize = (int) PIN_DIAMETER.convertToPixels() / 2 * 2;
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    
    Area mainArea = getBody()[0];
    Area innerArea = getBody()[1];
    Composite oldComposite = applyAlpha(g2d, componentState);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    drawingObserver.startTracking();
    g2d.fill(mainArea);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);
    Color finalBorderColor;
    
    if (outlineMode) {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : borderColor;
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);
    g2d.draw(innerArea);

    // the pins leave through the bottom of the case, so they are drawn over it to stay visible
    // whatever its alpha
    if (!outlineMode) {
      for (int i = 0; i < 2; i++) {
        Point2D point = getControlPoint(i);
        g2d.setColor(PIN_COLOR);
        g2d.fillOval((int) Math.round(point.getX() - pinSize / 2),
            (int) Math.round(point.getY() - pinSize / 2), pinSize, pinSize);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.drawOval((int) Math.round(point.getX() - pinSize / 2),
            (int) Math.round(point.getY() - pinSize / 2), pinSize, pinSize);
      }
    }

    // Draw label.
    g2d.setFont(project.getFont());
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    if (getTransistorDisplay() == TransistorDisplay.PINOUT) {
      drawPinout(g2d);
      return;
    }
    String label = "";
    label = (getTransistorDisplay() == TransistorDisplay.NAME) ? getName() : getValue();
    if (getTransistorDisplay() == TransistorDisplay.NONE) {
      label = "";
    }
    if (getTransistorDisplay() == TransistorDisplay.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = mainArea.getBounds();
    int x = (int) (bounds.getX() + (bounds.width - textWidth) / 2);
    int y = (int) (bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent());
    g2d.drawString(label, x, y);
  }

  // each letter sits beside its pin or hole on the side of the body centre
  private void drawPinout(Graphics2D g2d) {
    if (getPinout() == null) {
      return;
    }
    g2d.setFont(g2d.getFont().deriveFont((float) (g2d.getFont().getSize() * 0.7)));
    String pinout = getPinout().toPinout();
    int pinD = (int) PIN_DIAMETER.convertToPixels();
    int holeD = (int) HOLE_SIZE.convertToPixels();
    for (int i = 0; i < pinout.length(); i++) {
      Point2D point = getControlPoint(i);
      int d = (i < 2 ? pinD : holeD) / 2 + 2;
      int dx = 0;
      int dy = 0;
      HorizontalAlignment ha = HorizontalAlignment.CENTER;
      VerticalAlignment va = VerticalAlignment.CENTER;
      switch (getOrientation()) {
        case DEFAULT:
          dx = d;
          ha = HorizontalAlignment.LEFT;
          break;
        case _90:
          dy = d;
          va = VerticalAlignment.BOTTOM;
          break;
        case _180:
          dx = -d;
          ha = HorizontalAlignment.RIGHT;
          break;
        case _270:
          dy = -d;
          va = VerticalAlignment.TOP;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      StringUtils.drawCenteredText(g2d, pinout.substring(i, i + 1), point.getX() + dx,
          point.getY() + dy, ha, va);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    int largeR = getClosestOdd(width * 3d / 8);
    int smallR = getClosestOdd(width / 6d);
    int innerD = getClosestOdd(width / 2d);    
    int hole = 4 * width / 32;
    
    Area area = new TwoCircleTangent(new Point2D.Double(width * 0.5, height * 0.5), new Point2D.Double(width / 2, height / 8d), largeR, smallR);
    area.add((Area)new TwoCircleTangent(new Point2D.Double(width * 0.5, height * 0.5), new Point2D.Double(width / 2, height * 7 / 8d), largeR, smallR));
    
    area.subtract(new Area(new Ellipse2D.Double((width - hole) / 2, height / 8 - hole / 2, hole, hole)));
    area.subtract(new Area(new Ellipse2D.Double((width - hole) / 2, height * 7 / 8 - hole / 2, hole, hole)));
    area.transform(AffineTransform.getRotateInstance(Math.PI / 4, width / 2, height / 2));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(area);
    g2d.drawOval((width - innerD) / 2, (height - innerD) / 2, innerD, innerD);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @Deprecated
  public Display getDisplay() {
    if (display == null) {
      display = Display.NAME;
    }
    return display;
  }

  @Deprecated
  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Display")
  public TransistorDisplay getTransistorDisplay() {
    if (transistorDisplay == null) {
      transistorDisplay = TransistorDisplay.valueOf(getDisplay().name());
    }
    return transistorDisplay;
  }

  public void setTransistorDisplay(TransistorDisplay transistorDisplay) {
    this.transistorDisplay = transistorDisplay;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] body = getBody();
    int margin = 20;
    Rectangle2D bounds = null;
    for (Shape a : body) {
      if (a != null) {
        bounds = bounds == null ? a.getBounds2D() : bounds.createUnion(a.getBounds2D());
      }
    }
    if (bounds == null) {
      bounds = new Rectangle2D.Double(getControlPoint(0).getX(), getControlPoint(0).getY(), 0, 0);
    }
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
        bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
}
