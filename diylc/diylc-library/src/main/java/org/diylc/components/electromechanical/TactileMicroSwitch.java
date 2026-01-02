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
package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.guitar.LeverSwitchPositionPropertyValueSource;
import org.diylc.core.*;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.DynamicEditableProperty;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Tactile Micro-Switch", author = "Branislav Stojkovic",
    category = "Electro-Mechanical", instanceNamePrefix = "SW",
    description = "4-pin tactile momentary switch", zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true)
public class TactileMicroSwitch extends AbstractLabeledComponent<String> implements IGerberComponentSimple,
    ISwitch {

  private static final long serialVersionUID = 1L;

  private static final Color BODY_COLOR = Color.lightGray;
  private static final Color BORDER_COLOR = Color.gray.darker();
  private static final Color PIN_COLOR = METAL_COLOR;
  private static final Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  private static final Color INDENT_COLOR = Color.gray.darker();
  private static final Color LABEL_COLOR = Color.white;
  private static final Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  private static final Size CASE_SIZE = new Size(6d, SizeUnit.mm);
  private static final Size PIN_SPACING = new Size(4.5d, SizeUnit.mm);
  private static final Size ROW_SPACING = new Size(6.5d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Display display = Display.BOTH;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  transient private Area[] body;

  private Size caseSize;
  private Size pinSpacing;
  private Size rowSpacing;
  private PinPairingMode pinPairingMode;
  private int selectedPosition;

  public TactileMicroSwitch() {
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
    int PIN_COUNT = 4;
    controlPoints = new Point2D[PIN_COUNT];
    controlPoints[0] = firstPoint;
    double pinSpacing = getPinSpacing().convertToPixels();
    double rowSpacing = getRowSpacing().convertToPixels();
    // Update control points.
    double dx1;
    double dy1;
    double dx2;
    double dy2;
    for (int i = 0; i < PIN_COUNT / 2; i++) {
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
          new Point2D.Double((int) (firstPoint.getX() + dx1), (int) (firstPoint.getY() + dy1));
      controlPoints[i + PIN_COUNT / 2] =
          new Point2D.Double((int) (firstPoint.getX() + dx2), (int) (firstPoint.getY() + dy2));
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      double width;
      double height;
      double caseSize = getCaseSize().convertToPixels();
      double actuatorSize = caseSize / 2;

      width = height = caseSize;
      x = (controlPoints[0].getX() + controlPoints[3].getX() - width) / 2;
      y = (controlPoints[0].getY() + controlPoints[3].getY() - height) / 2;

      body[0] = new Area(new Rectangle2D.Double(x, y, width, height));

      width = height = actuatorSize;
      x = (controlPoints[0].getX() + controlPoints[3].getX() - width) / 2;
      y = (controlPoints[0].getY() + controlPoints[3].getY() - height) / 2;

      body[1] = new Area(new Ellipse2D.Double(x, y, width, height));
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area[] body = getBody();
    Area mainArea = body[0];
    Area indentArea = body[1];
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point2D point : controlPoints) {
        Rectangle2D rect = new Rectangle2D.Double(point.getX() - pinSize / 2,
            point.getY() - pinSize / 2, pinSize, pinSize);
        g2d.setColor(PIN_COLOR);
        drawingObserver.startTracking();
        g2d.fill(rect);
        drawingObserver.stopTrackingContinuityArea();
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(rect);
      }
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    drawingObserver.startTracking();
    g2d.fill(mainArea);
    drawingObserver.stopTracking();

    g2d.setColor(INDENT_COLOR);
    g2d.fill(indentArea);
    g2d.setColor(INDENT_COLOR.darker());
    g2d.draw(indentArea);

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

    g2d.draw(mainArea);

    drawingObserver.stopTracking();

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

        if (label.length == 2) {
          if (i == 0)
            g2d.translate(0, -textHeight / 2);
          else if (i == 1)
            g2d.translate(0, textHeight / 2);
        }

        g2d.drawString(l, (int) x, (int) y);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int pinSize = 4 * width / 32;
    g2d.setColor(PIN_COLOR);

    g2d.fillRect(width / 6 - pinSize + 1, height / 6 + 1, pinSize, pinSize);
    g2d.fillRect(5 * width / 6, height / 6 + 1, pinSize, pinSize);

    g2d.fillRect(width / 6 - pinSize + 1, 5 * height / 6 - pinSize - 1, pinSize, pinSize);
    g2d.fillRect(5 * width / 6, 5 * height / 6 - pinSize - 1, pinSize, pinSize);

    g2d.setColor(BODY_COLOR);
    g2d.fillRect(width / 6, width / 6, 4 * width / 6, 4 * width / 6);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(width / 6, width / 6, 4 * width / 6, 4 * width / 6);


    g2d.setColor(INDENT_COLOR);
    int indentSize = 9 * width / 32;
    g2d.fillOval(width / 2 - indentSize / 2, height / 2 - indentSize / 2, indentSize, indentSize);
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

  @EditableProperty(name = "Case Size")
  public Size getCaseSize() {
    if (caseSize == null) {
      caseSize = CASE_SIZE;
    }
    return caseSize;
  }

  public void setCaseSize(Size caseSize) {
    this.caseSize = caseSize;
    this.body = null;
  }

  @EditableProperty(name = "Pin Spacing")
  public Size getPinSpacing() {
    if (pinSpacing == null) {
      pinSpacing = PIN_SPACING;
    }
    return pinSpacing;
  }

  public void setPinSpacing(Size pinSpacing) {
    this.pinSpacing = pinSpacing;
    this.body = null;
    updateControlPoints();
  }

  @EditableProperty(name = "Row Spacing")
  public Size getRowSpacing() {
    if (rowSpacing == null) {
      rowSpacing = ROW_SPACING;
    }
    return rowSpacing;
  }

  public void setRowSpacing(Size rowSpacing) {
    this.rowSpacing = rowSpacing;
    this.body = null;
    updateControlPoints();
  }

  @EditableProperty(name = "Pin Pairing Mode")
  public PinPairingMode getPinPairingMode() {
    if (pinPairingMode == null) {
      pinPairingMode = PinPairingMode.Opposite;
    }
    return pinPairingMode;
  }

  public void setPinPairingMode(PinPairingMode pinPairingMode) {
    this.pinPairingMode = pinPairingMode;
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
  public int getPositionCount() {
    return 2;
  }

  @Override
  public String getPositionName(int position) {
    return position == 0 ? "Released" : "Pressed";
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    // Only connect pins when switch is pressed (position = 1)
    if (position != 1) {
      return false;
    }
    
    PinPairingMode mode = getPinPairingMode();
    
    switch (mode) {
      case Opposite:
        // Pins on opposite sides (same row): 0-2 and 1-3
        return (index1 == 0 && index2 == 2) || (index1 == 2 && index2 == 0) ||
               (index1 == 1 && index2 == 3) || (index1 == 3 && index2 == 1);
      case Diagonal:
        // Diagonally opposite pins: 0-3 and 1-2
        return (index1 == 0 && index2 == 3) || (index1 == 3 && index2 == 0) ||
               (index1 == 1 && index2 == 2) || (index1 == 2 && index2 == 1);
      case Adjacent:
        // Pins next to each other (same column): 0-1 and 2-3
        return (index1 == 0 && index2 == 1) || (index1 == 1 && index2 == 0) ||
               (index1 == 2 && index2 == 3) || (index1 == 3 && index2 == 2);
      default:
        return false;
    }
  }

  @DynamicEditableProperty(source = TactileMicroSwitchPositionPropertyValueSource.class)
  @EditableProperty(name = "Selected Position")
  @Override
  public Integer getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(Integer selectedPosition) {
    this.selectedPosition = selectedPosition;
  }

  public enum PinPairingMode {
    Opposite, Diagonal, Adjacent;
  }
}
