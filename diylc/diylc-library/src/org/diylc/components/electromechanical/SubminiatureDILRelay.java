package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Subminiature DIL Relay", author = "Branislav Stojkovic", category = "Electromechanical",
    instanceNamePrefix = "RY", description = "Subminiature DIL relay with one or two poles", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE)
public class SubminiatureDILRelay extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#EDEDD5");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color INDENT_COLOR = Color.gray.darker();
  public static Color LABEL_COLOR = Color.darkGray;
  public static int EDGE_RADIUS = 6;
  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size INDENT_SIZE = new Size(0.05d, SizeUnit.in);

  public static Size PIN_SPACING = new Size(0.2d, SizeUnit.in);
  public static Size ROW_SPACING = new Size(0.3d, SizeUnit.in);
  public static Size SECTION_SPACING = new Size(0.3d, SizeUnit.in);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private PinCount pinCount = PinCount._4;
  private Size pinSpacing = PIN_SPACING;
  private Size rowSpacing = ROW_SPACING;
  private Size sectionSpacing = SECTION_SPACING;
  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  protected Display display = Display.NAME;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color indentColor = INDENT_COLOR;
  transient private Area[] body;

  public SubminiatureDILRelay() {
    super();
    updateControlPoints();
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

  @EditableProperty(name = "Contacts")
  public PinCount getPinCount() {
    return pinCount;
  }

  public void setPinCount(PinCount pinCount) {
    this.pinCount = pinCount;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Pin spacing")
  public Size getPinSpacing() {
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Row spacing")
  public Size getRowSpacing() {
    return rowSpacing;
  }

  public void setRowSpacing(Size rowSpacing) {
    this.rowSpacing = rowSpacing;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @EditableProperty(name = "Section spacing")
  public Size getSectionSpacing() {
    return sectionSpacing;
  }

  public void setSectionSpacing(Size sectionSpacing) {
    this.sectionSpacing = sectionSpacing;
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
    controlPoints = new Point[pinCount.getValue() * 2];
    controlPoints[0] = firstPoint;
    int pinSpacing = (int) this.pinSpacing.convertToPixels();
    int sectionSpacing = (int) this.sectionSpacing.convertToPixels();
    int rowSpacing = (int) this.rowSpacing.convertToPixels();
    // Update control points.
    int dx1;
    int dy1;
    int dx2;
    int dy2;
    for (int i = 0; i < pinCount.getValue(); i++) {
      int spacing = i == pinCount.getValue() - 1 ? sectionSpacing + (i - 1) * pinSpacing : i * pinSpacing;
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = spacing;
          dx2 = rowSpacing;
          dy2 = spacing;
          break;
        case _90:
          dx1 = -spacing;
          dy1 = 0;
          dx2 = -spacing;
          dy2 = rowSpacing;
          break;
        case _180:
          dx1 = 0;
          dy1 = -spacing;
          dx2 = -rowSpacing;
          dy2 = -spacing;
          break;
        case _270:
          dx1 = spacing;
          dy1 = 0;
          dx2 = spacing;
          dy2 = -rowSpacing;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point(firstPoint.x + dx1, firstPoint.y + dy1);
      controlPoints[i + pinCount.getValue()] = new Point(firstPoint.x + dx2, firstPoint.y + dy2);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];

      int pinSize = (int) PIN_SIZE.convertToPixels();

      int minX = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxY = Integer.MIN_VALUE;
      for (int i = 0; i < this.controlPoints.length; i++) {
        if (this.controlPoints[i].x > maxX)
          maxX = this.controlPoints[i].x;
        if (this.controlPoints[i].x < minX)
          minX = this.controlPoints[i].x;
        if (this.controlPoints[i].y > maxY)
          maxY = this.controlPoints[i].y;
        if (this.controlPoints[i].y < minY)
          minY = this.controlPoints[i].y;
      }
      int width = maxX - minX;
      int height = maxY - minY;

      Area indentation = null;
      int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
      switch (orientation) {
        case DEFAULT:
          indentation =
              new Area(new Rectangle2D.Double(minX + width / 2 - indentationSize / 2, minY - indentationSize / 2
                  - pinSize, indentationSize, indentationSize));
          break;
        case _90:
          indentation =
              new Area(new Rectangle2D.Double(minX + width - indentationSize / 2 + pinSize, minY + height / 2
                  - indentationSize / 2, indentationSize, indentationSize));
          break;
        case _180:
          indentation =
              new Area(new Rectangle2D.Double(minX + width / 2 - indentationSize / 2, minY + height - indentationSize
                  / 2 + pinSize, indentationSize, indentationSize));
          break;
        case _270:
          indentation =
              new Area(new Rectangle2D.Double(minX - indentationSize / 2 - pinSize, minY + height / 2 - indentationSize
                  / 2, indentationSize, indentationSize));
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      body[0] =
          new Area(new RoundRectangle2D.Double(minX - pinSize, minY - pinSize, width + 2 * pinSize, height + 2
              * pinSize, EDGE_RADIUS, EDGE_RADIUS));
      body[1] = indentation;
      if (indentation != null) {
        indentation.intersect(body[0]);
      }
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
    } else {
      g2d.draw(mainArea);
      if (getBody()[1] != null) {
        g2d.setColor(getIndentColor());
        g2d.fill(getBody()[1]);
      }
    }

    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point point : controlPoints) {
        g2d.setColor(PIN_COLOR);
        g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
      }
    }

    // Draw label.
    g2d.setFont(LABEL_FONT);
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
    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : getValue();
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = mainArea.getBounds();
    int x = bounds.x + (bounds.width - textWidth) / 2;
    int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();
    g2d.drawString(label, x, y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int radius = 6 * width / 32;
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(width / 6, 1, 4 * width / 6, height - 4, radius, radius);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(width / 6, 1, 4 * width / 6, height - 4, radius, radius);
    int pinSize = 2 * width / 32;
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      if (i == 1)
        continue;
      g2d.fillOval(width / 3 - pinSize, (height / 5) * (i + 1) - 1, pinSize, pinSize);
      g2d.fillOval(2 * width / 3 + 1, (height / 5) * (i + 1) - 1, pinSize, pinSize);
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

  @EditableProperty(name = "Indent")
  public Color getIndentColor() {
    if (indentColor == null) {
      indentColor = INDENT_COLOR;
    }
    return indentColor;
  }

  public void setIndentColor(Color indentColor) {
    this.indentColor = indentColor;
  }

  public static enum PinCount {

    _3, _4, _5;

    @Override
    public String toString() {
      return name().replace("_", "").concat(" per side");
    }

    public int getValue() {
      return Integer.parseInt(name().replace("_", ""));
    }
  }
}
