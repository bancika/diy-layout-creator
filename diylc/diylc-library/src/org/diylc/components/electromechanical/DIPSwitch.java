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
package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.DIL_ICTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "DIP Switch", author = "Branislav Stojkovic", category = "Electro-Mechanical",
    instanceNamePrefix = "SW", description = "Dual-in-line package switch", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = DIL_ICTransformer.class)
public class DIPSwitch extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#E84E46");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color TICK_COLOR = Color.white;
  public static Color LABEL_COLOR = Color.white;
  public static int EDGE_RADIUS = 2;
  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.4d, SizeUnit.in);
  public static Size INDENT_SIZE = new Size(0.07d, SizeUnit.in);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private SwitchCount switchCount = SwitchCount._8;
  private Size pinSpacing = new Size(0.1d, SizeUnit.in);
  private Size rowSpacing = new Size(0.3d, SizeUnit.in);
  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  protected Display display = Display.NONE;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color tickColor = TICK_COLOR;
  private Size width = DEFAULT_WIDTH;
  // new Point(0, pinSpacing.convertToPixels()),
  // new Point(0, 2 * pinSpacing.convertToPixels()),
  // new Point(0, 3 * pinSpacing.convertToPixels()),
  // new Point(3 * pinSpacing.convertToPixels(), 0),
  // new Point(3 * pinSpacing.convertToPixels(),
  // pinSpacing.convertToPixels()),
  // new Point(3 * pinSpacing.convertToPixels(), 2 *
  // pinSpacing.convertToPixels()),
  // new Point(3 * pinSpacing.convertToPixels(), 3 *
  // pinSpacing.convertToPixels()) };
  transient private Area[] body;

  public DIPSwitch() {
    super();
    updateControlPoints();
    alpha = (byte) 100;
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
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Switches")
  public SwitchCount getSwitchCount() {
    return switchCount;
  }

  public void setSwitchCount(SwitchCount switchCount) {
    this.switchCount = switchCount;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Pin Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getPinSpacing() {
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Row Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getRowSpacing() {
    return rowSpacing;
  }

  public void setRowSpacing(Size rowSpacing) {
    this.rowSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.VALUE;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
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
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    controlPoints = new Point[switchCount.getValue() * 2];
    controlPoints[0] = firstPoint;
    double pinSpacing = this.pinSpacing.convertToPixels();
    double rowSpacing = this.rowSpacing.convertToPixels();
    // Update control points.
    double dx1;
    double dy1;
    double dx2;
    double dy2;
    for (int i = 0; i < switchCount.getValue(); i++) {
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;
          dx2 = rowSpacing;
          dy2 = i * pinSpacing;
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          dx2 = -i * pinSpacing;
          dy2 = rowSpacing;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          dx2 = -rowSpacing;
          dy2 = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          dx2 = i * pinSpacing;
          dy2 = -rowSpacing;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point((int) (firstPoint.x + dx1), (int) (firstPoint.y + dy1));
      controlPoints[i + switchCount.getValue()] = new Point((int) (firstPoint.x + dx2), (int) (firstPoint.y + dy2));
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[3];
      double x = controlPoints[0].x;
      double y = controlPoints[0].y;
      double width;
      double height;
      double pinSpacing = this.pinSpacing.convertToPixels();
      double rowSpacing = this.rowSpacing.convertToPixels();    
      double bodyWidth = getClosestOdd(getWidth().convertToPixels());
      double tickHoleLength = rowSpacing * 2 / 3;
      double tickSize = PIN_SIZE.convertToPixels();
      Area tickArea = new Area();
      Area tickHoleArea = new Area();
      switch (orientation) {
        case DEFAULT:
          width = bodyWidth;
          height = switchCount.getValue() * pinSpacing;
          x -= (bodyWidth - rowSpacing) / 2;
          y -= pinSpacing / 2;
          for (int i = 0; i < getSwitchCount().getValue(); i++) {
            tickHoleArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x + (rowSpacing - tickHoleLength) / 2, controlPoints[i].y - tickSize / 2, tickHoleLength, tickSize)));
            tickArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x + (rowSpacing - tickHoleLength) / 2, controlPoints[i].y - tickSize / 2, tickSize, tickSize)));
          }
          break;
        case _90:
          width = switchCount.getValue() * pinSpacing;
          height = bodyWidth;
          x -= (pinSpacing / 2) + width - pinSpacing;
          y -= (bodyWidth - rowSpacing) / 2;
          for (int i = 0; i < getSwitchCount().getValue(); i++) {
            tickHoleArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x - tickSize / 2, controlPoints[i].y + (rowSpacing - tickHoleLength) / 2, tickSize, tickHoleLength)));
            tickArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x - tickSize / 2, controlPoints[i].y + (rowSpacing - tickHoleLength) / 2, tickSize, tickSize)));
          }
          break;
        case _180:
          width = bodyWidth;
          height = switchCount.getValue() * pinSpacing;
          x -= rowSpacing + (bodyWidth - rowSpacing) / 2;
          y -= (pinSpacing / 2) + height - pinSpacing;
          for (int i = getSwitchCount().getValue(); i < controlPoints.length; i++) {
            tickHoleArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x + (rowSpacing - tickHoleLength) / 2, controlPoints[i].y - tickSize / 2, tickHoleLength, tickSize)));
            tickArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x + (rowSpacing + tickHoleLength) / 2 - tickSize, controlPoints[i].y - tickSize / 2, tickSize, tickSize)));
          }
          break;
        case _270:
          width = (switchCount.getValue()) * pinSpacing;
          height = bodyWidth;
          x -= pinSpacing / 2;
          y -= rowSpacing + (bodyWidth - rowSpacing) / 2;
          for (int i = getSwitchCount().getValue(); i < controlPoints.length; i++) {
            tickHoleArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x - tickSize / 2, controlPoints[i].y + (rowSpacing - tickHoleLength) / 2, tickSize, tickHoleLength)));
            tickArea.add(new Area(new Rectangle2D.Double(controlPoints[i].x - tickSize / 2, controlPoints[i].y + (rowSpacing - tickHoleLength) / 2 + tickHoleLength - tickSize, tickSize, tickSize)));
          }
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body[0] = new Area(new RoundRectangle2D.Double(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS));
      body[1] = tickHoleArea;
      body[2] = tickArea;
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area mainArea = getBody()[0];
    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point point : controlPoints) {
        g2d.setColor(PIN_COLOR);
        int pinWidth = getClosestOdd(pinSize / (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180 ? 2f : 1f));
        int pinHeight = getClosestOdd(pinSize / (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180 ? 1f : 2f));
        g2d.fillRect(point.x - pinWidth / 2, point.y - pinHeight / 2, pinWidth, pinHeight);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.drawRect(point.x - pinWidth / 2, point.y - pinHeight / 2, pinWidth, pinHeight);
      }
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : getBorderColor();
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (outlineMode) {
      Area area = new Area(mainArea);
      area.subtract(getBody()[1]);
      g2d.draw(area);
      if (getBody()[1] != null)
        g2d.draw(getBody()[1]);
      if (getBody()[2] != null)
        g2d.draw(getBody()[2]);
    } else {
      g2d.draw(mainArea);
      if (getBody()[1] != null) {
        g2d.setColor(getTickColor().darker());
        g2d.fill(getBody()[1]);
      }
      if (getBody()[2] != null) {
        g2d.setColor(getTickColor());
        g2d.fill(getBody()[2]);
      }
    }

    drawingObserver.stopTracking();

    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    String[] label = null;

    if (getDisplay() == Display.NAME) {
      label = new String[] {getName()};
    } else if (getDisplay() == Display.VALUE) {
      label = new String[] {getValue().toString()};
    } else if (getDisplay() == Display.BOTH) {
      String value = getValue().toString();
      label = value.isEmpty() ? new String[] {getName()} : new String[] {getName(), value};
    }

    if (label != null) {
      for (int i = 0; i < label.length; i++) {
        String l = label[i];
        Rectangle2D rect = fontMetrics.getStringBounds(l, g2d);
        int textHeight = (int) (rect.getHeight());
        int textWidth = (int) (rect.getWidth());
        // Center text horizontally and vertically
        Rectangle bounds = mainArea.getBounds();
        int x = bounds.x + (bounds.width - textWidth) / 2;
        int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        AffineTransform oldTransform = g2d.getTransform();

        if (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180) {
          int centerX = bounds.x + bounds.width / 2;
          int centerY = bounds.y + bounds.height / 2;
          g2d.rotate(-Math.PI / 2, centerX, centerY);
        }

        if (label.length == 2) {
          if (i == 0)
            g2d.translate(0, -textHeight / 2);
          else if (i == 1)
            g2d.translate(0, textHeight / 2);
        }

        g2d.drawString(l, x, y);

        g2d.setTransform(oldTransform);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(width / 6, 1, 4 * width / 6, height - 4);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(width / 6, 1, 4 * width / 6, height - 4);
    int tickSize = (int) (2f * width / 32);
    int tickHoleLength = (int) (width * 0.4);
    for (int i = 0; i < 4; i++) {
      g2d.setColor(TICK_COLOR.darker());
      g2d.fillRect((width - tickHoleLength) / 2, (height / 5) * (i + 1) - tickSize / 2, tickHoleLength, tickSize);
      g2d.setColor(TICK_COLOR);
      g2d.fillRect((width - tickHoleLength) / 2, (height / 5) * (i + 1) - tickSize / 2, tickSize, tickSize);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null) {
      bodyColor = BODY_COLOR;
    }
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null) {
      borderColor = BORDER_COLOR;
    }
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null) {
      labelColor = LABEL_COLOR;
    }
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Tick")
  public Color getTickColor() {
    return tickColor;
  }
  
  public void setTickColor(Color tickColor) {
    this.tickColor = tickColor;
  }
  
  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getWidth() {
    return width;
  }
  
  public void setWidth(Size width) {
    this.width = width;
    body = null;
  }

  public static enum SwitchCount {

    _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12;

    @Override
    public String toString() {
      return name().replace("_", "");
    }

    public int getValue() {
      return Integer.parseInt(toString());
    }
  }
}
