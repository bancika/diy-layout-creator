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
package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.PCBLayer;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.transform.DIL_ICTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@AiNote("The first control point is the anchor; with 'Orientation' DEFAULT the first row of pins runs down from it 'Pin Spacing' apart, the second row sits 'Row Spacing' to the right and the notch is at the anchor's end. The other orientations rotate this around the anchor")
@AiNote("'Pin Names' is a comma-separated list of pin names in pin-number order, starting with pin 1 next to the notch; a pin without a name is called by its number. Pins named alike up to a trailing '_1', '_2' are one net inside the package, e.g. GND_1 and GND_2, while 1IN- and 2IN- are inputs of two separate sections")
@AiNote("A datasheet model is a part number such as TL072 or NE555 and sets 'Pins', 'Row Spacing', 'Pin Names' and 'Value'. Set 'Value' to the same part number together with the model")
@ComponentDescriptor(name = "DIP IC", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "IC", description = "Dual-in-line package IC",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = DIL_ICTransformer.class, enableCache = true, enableDatasheet = true,
    datasheetCreationStepCount = 2, datasheetModelColumn = 1)
public class DIL_IC extends AbstractLabeledComponent<String>
    implements IGerberComponentSimple, IDatasheetSupport, IBoardSideComponent {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.gray;
  public static Color BORDER_COLOR = Color.gray.darker();
  public static Color INDENT_COLOR = Color.gray.darker();
  public static Color LABEL_COLOR = Color.white;
  public static int EDGE_RADIUS = 6;
  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size INDENT_SIZE = new Size(0.07d, SizeUnit.in);
  public static DisplayNumbers DISPLAY_NUMBERS = DisplayNumbers.NAMES;
  // the smaller font lets names sit closer to their pins than on a maker board
  public static Size PIN_NAME_OFFSET = new Size(1.35d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private PinCount pinCount = PinCount._8;
  private Size pinSpacing = new Size(0.1d, SizeUnit.in);
  private Size rowSpacing = new Size(0.3d, SizeUnit.in);
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Display display = Display.BOTH;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color indentColor = INDENT_COLOR;
  private DisplayNumbers displayNumbers = DISPLAY_NUMBERS;
  private String pinNames = null;
  private PCBLayer boardSide;
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
  transient private String[] parsedPinNames;

  public DIL_IC() {
    super();
    updateControlPoints();
  }

  public DIL_IC(String[] model) {
    this();

    applyModel(model);
  }

  // columns: group, part number, row spacing, then one name per pin in pin-number order, so the
  // pin count is the number of names
  @Override
  public void applyModel(String[] model) {
    setValue(model[1]);
    setRowSpacing(Size.parseSize(model[2]));
    setPinCount(PinCount.valueOf("_" + (model.length - 3)));
    setPinNames(String.join(",", Arrays.copyOfRange(model, 3, model.length)));
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

  @EditableProperty(name = "Pins")
  public PinCount getPinCount() {
    return pinCount;
  }

  public void setPinCount(PinCount pinCount) {
    this.pinCount = pinCount;
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
  public Point2D getControlPoint(int index) {
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    controlPoints = new Point2D[pinCount.getValue()];
    controlPoints[0] = firstPoint;
    double pinSpacing = this.pinSpacing.convertToPixels();
    double rowSpacing = this.rowSpacing.convertToPixels();
    // Update control points.
    double dx1;
    double dy1;
    double dx2;
    double dy2;
    for (int i = 0; i < pinCount.getValue() / 2; i++) {
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
      controlPoints[i] =
          new Point2D.Double((int) Math.round(firstPoint.getX() + dx1), (int) Math.round(firstPoint.getY() + dy1));
      controlPoints[i + pinCount.getValue() / 2] =
          new Point2D.Double((int) Math.round(firstPoint.getX() + dx2), (int) Math.round(firstPoint.getY() + dy2));
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      double width;
      double height;
      double pinSize = PIN_SIZE.convertToPixels();
      double pinSpacing = this.pinSpacing.convertToPixels();
      double rowSpacing = this.rowSpacing.convertToPixels();
      Area indentation = null;
      int indentationSize = getClosestOdd(INDENT_SIZE.convertToPixels());
      switch (orientation) {
        case DEFAULT:
          width = rowSpacing - pinSize;
          height = (pinCount.getValue() / 2) * pinSpacing;
          x += pinSize / 2;
          y -= pinSpacing / 2;
          indentation = new Area(new Ellipse2D.Double(x + width / 2 - indentationSize / 2,
              y - indentationSize / 2, indentationSize, indentationSize));
          break;
        case _90:
          width = (pinCount.getValue() / 2) * pinSpacing;
          height = rowSpacing - pinSize;
          x -= (pinSpacing / 2) + width - pinSpacing;
          y += pinSize / 2;
          indentation = new Area(new Ellipse2D.Double(x + width - indentationSize / 2,
              y + height / 2 - indentationSize / 2, indentationSize, indentationSize));
          break;
        case _180:
          width = rowSpacing - pinSize;
          height = (pinCount.getValue() / 2) * pinSpacing;
          x -= rowSpacing - pinSize / 2;
          y -= (pinSpacing / 2) + height - pinSpacing;
          indentation = new Area(new Ellipse2D.Double(x + width / 2 - indentationSize / 2,
              y + height - indentationSize / 2, indentationSize, indentationSize));
          break;
        case _270:
          width = (pinCount.getValue() / 2) * pinSpacing;
          height = rowSpacing - pinSize;
          x -= pinSpacing / 2;
          y += pinSize / 2 - rowSpacing;
          indentation = new Area(new Ellipse2D.Double(x - indentationSize / 2,
              y + height / 2 - indentationSize / 2, indentationSize, indentationSize));
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body[0] =
          new Area(new RoundRectangle2D.Double(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS));
      body[1] = indentation;
      if (indentation != null) {
        indentation.intersect(body[0]);
      }
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area mainArea = getBody()[0];
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point2D point : controlPoints) {
        Rectangle2D rect = new Rectangle2D.Double(point.getX() - pinSize / 2,
            point.getY() - pinSize / 2, pinSize, pinSize);
        g2d.setColor(PIN_COLOR);
        drawingObserver.startTracking();
        g2d.fill(rect);
        drawingObserver.stopTracking();
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(rect);
      }
    }
    Composite oldComposite = applyAlpha(g2d, componentState);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    drawingObserver.startTracking();
    g2d.fill(mainArea);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
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

    // Draw label.
    g2d.setFont(project.getFont());

    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));

    Color finalLabelColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? LABEL_COLOR_SELECTED
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
        double x = bounds.getX() + (bounds.width - textWidth) / 2;
        double y = bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();

        AffineTransform oldTransform = g2d.getTransform();

        if (getOrientation() == Orientation.DEFAULT || getOrientation() == Orientation._180) {
          double centerX = bounds.getX() + bounds.width / 2;
          double centerY = bounds.getY() + bounds.height / 2;
          g2d.rotate(-Math.PI / 2, centerX, centerY);
        }

        if (label.length == 2) {
          if (i == 0)
            g2d.translate(0, -textHeight / 2);
          else if (i == 1)
            g2d.translate(0, textHeight / 2);
        }

        g2d.drawString(l, (int) x, (int) y);

        g2d.setTransform(oldTransform);
      }
    }

    if (getDisplayNumbers() == DisplayNumbers.NAMES) {
      drawPinNames(g2d);
    }

    // draw pin numbers
    if (getDisplayNumbers() != DisplayNumbers.NO && getDisplayNumbers() != DisplayNumbers.NAMES) {
      int pinSize = (int) PIN_SIZE.convertToPixels();
      g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 0.66)));

      for (int i = 0; i < controlPoints.length; i++) {
        Point2D point = controlPoints[i];

        // determine points relative to rotation
        int textX1 = (int) Math.round(point.getX() - 2 * pinSize);
        int textY1 = (int) Math.round(point.getY() + pinSize / 2);
        int textX2 = (int) Math.round(point.getX() + pinSize);
        int textY2 = (int) Math.round(point.getY() + pinSize / 2);
        if (orientation == Orientation._90) {
          textX2 = textX2 - pinSize - pinSize / 2;
          textY2 = textY2 + pinSize;
          textX1 = textX1 + 2 * pinSize - pinSize / 2;
          textY1 = textY1 - pinSize;
        }
        if (orientation == Orientation._180) {
          textX1 = textX1 + 3 * pinSize;
          textX2 = textX2 - 3 * pinSize;
        }
        if (orientation == Orientation._270) {
          textX1 = textX1 + pinSize + pinSize / 2;
          textY1 = textY1 + pinSize;
          textX2 = textX2 - pinSize - pinSize / 2;
          textY2 = textY2 - pinSize;
        }

        String pinNumber = Integer.toString(getPinNumber(i));
        if (i >= pinCount.getValue() / 2) {
          g2d.drawString(pinNumber, textX1, textY1);
        } else {
          g2d.drawString(pinNumber, textX2, textY2);
        }
      }
    }
  }

  // Names are printed the way maker boards print theirs: centred just inside the body from their
  // pin and running along the row, which leaves the middle of even a 0.3 in body to the name/value
  // label. Each name has one pin pitch along the row, so a name that would touch the previous one
  // in its row or run past the end of the body is left out rather than overprinted.
  private void drawPinNames(Graphics2D g2d) {
    g2d.setFont(ICPinNames.PIN_NAME_FONT);
    FontMetrics fontMetrics = g2d.getFontMetrics(ICPinNames.PIN_NAME_FONT);
    double labelOffset = PIN_NAME_OFFSET.convertToPixels();
    double pitch = pinSpacing.convertToPixels();
    int half = pinCount.getValue() / 2;
    Point2D firstRow = controlPoints[0];
    Point2D secondRow = controlPoints[half];
    double rowDistance = firstRow.distance(secondRow);
    double acrossX = (secondRow.getX() - firstRow.getX()) / rowDistance;
    double acrossY = (secondRow.getY() - firstRow.getY()) / rowDistance;
    boolean rowsVertical = Math.abs(acrossX) > Math.abs(acrossY);
    // along the row the body reaches half a pitch past the first and the last pin
    double bodyStart = -pitch / 2 + 1;
    double bodyEnd = (half - 0.5) * pitch - 1;
    double previousEnd = bodyStart - ICPinNames.PIN_NAME_GAP;

    for (int i = 0; i < controlPoints.length; i++) {
      if (i == half) {
        previousEnd = bodyStart - ICPinNames.PIN_NAME_GAP;
      }
      String name = getPinName(getPinNumber(i));
      if (name == null) {
        continue;
      }
      String label = AbstractMakerBoard.getDisplayPinLabel(name);
      double alongRow = (i % half) * pitch;
      double halfWidth = fontMetrics.stringWidth(label) / 2d;
      if (alongRow - halfWidth < previousEnd + ICPinNames.PIN_NAME_GAP || alongRow + halfWidth > bodyEnd) {
        continue;
      }
      previousEnd = alongRow + halfWidth;
      double dirX = i < half ? acrossX : -acrossX;
      double dirY = i < half ? acrossY : -acrossY;
      double x = controlPoints[i].getX() + dirX * labelOffset;
      double y = controlPoints[i].getY() + dirY * labelOffset;
      AffineTransform oldTransform = g2d.getTransform();
      // the two rows are mirrored so each name reads away from its pin, as on a maker board; rows
      // that run horizontally keep their names level so that neither reads upside down
      if (rowsVertical) {
        g2d.rotate(dirX > 0 ? Math.PI / 2 : -Math.PI / 2, x, y);
      }
      StringUtils.drawCenteredText(g2d, label, x, y, HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
      g2d.setTransform(oldTransform);
    }
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
      g2d.fillRect(width / 6 - pinSize, (height / 5) * (i + 1) - 1, pinSize, pinSize);
      g2d.fillRect(5 * width / 6 + 1, (height / 5) * (i + 1) - 1, pinSize, pinSize);
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

  @EditableProperty(name = "Pin Labels")
  public DisplayNumbers getDisplayNumbers() {
    if (displayNumbers == null) {
      displayNumbers = DisplayNumbers.NO;
    }
    return displayNumbers;
  }

  public void setDisplayNumbers(DisplayNumbers numbers) {
    this.displayNumbers = numbers;
  }

  @EditableProperty(name = "Pin Names")
  public String getPinNames() {
    return pinNames;
  }

  public void setPinNames(String pinNames) {
    this.pinNames = pinNames;
    parsedPinNames = null;
  }

  // the physical pin number of a control point; control points run down the first row and then
  // down the second, whereas DIP numbering comes back up the second row
  private int getPinNumber(int index) {
    int pinCount = this.pinCount.getValue();
    int half = pinCount / 2;
    int position = index + 1;
    boolean secondRow = position > half;
    switch (getDisplayNumbers()) {
      case CONNECTOR:
        return secondRow ? 2 * (position - half) : 2 * position - 1;
      case DIP_MIRROR:
        return secondRow ? position - half : pinCount - position + 1;
      case CONNECTOR_MIRROR:
        return secondRow ? 2 * (position - half) - 1 : 2 * position;
      default:
        return secondRow ? pinCount + half - position + 1 : position;
    }
  }

  private String getPinName(int pinNumber) {
    if (parsedPinNames == null) {
      parsedPinNames = ICPinNames.parse(getPinNames());
    }
    return ICPinNames.get(parsedPinNames, pinNumber);
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    int margin = 50;
    for (int i = 0; i < getControlPointCount(); i++) {
      Point2D p = getControlPoint(i);
      if (p.getX() < minX)
        minX = p.getX();
      if (p.getX() > maxX)
        maxX = p.getX();
      if (p.getY() < minY)
        minY = p.getY();
      if (p.getY() > maxY)
        maxY = p.getY();
    }

    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin,
        maxY - minY + 2 * margin);
  }

  @Override
  public String getControlPointNodeName(int index) {
    int pinNumber = getPinNumber(index);
    String name = getPinName(pinNumber);
    return name == null ? Integer.toString(pinNumber) : name;
  }

  @EditableProperty(name = "Board Side")
  @Override
  public PCBLayer getBoardSide() {
    return boardSide == null ? PCBLayer._2 : boardSide;
  }

  public void setBoardSide(PCBLayer boardSide) {
    this.boardSide = boardSide;
  }

  public static enum PinCount {

    _4, _6, _8, _10, _12, _14, _16, _18, _20, _22, _24, _26, _28, _30, _32, _34, _36, _38, _40, _42, _44, _46, _48, _50;

    @Override
    public String toString() {
      return name().replace("_", "");
    }

    public int getValue() {
      return Integer.parseInt(toString());
    }
  }

  public enum DisplayNumbers {

    NO("None"), NAMES("Names"), DIP("DIP"), CONNECTOR("Connector"), DIP_MIRROR("DIP (Mirrored)"),
    CONNECTOR_MIRROR("Connector (Mirrored)");

    private String label;

    private DisplayNumbers(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
