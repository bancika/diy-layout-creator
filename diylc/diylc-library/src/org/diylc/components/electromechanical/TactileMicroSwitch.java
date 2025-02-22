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
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
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
public class TactileMicroSwitch extends AbstractLabeledComponent<String> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.lightGray;
  public static Color BORDER_COLOR = Color.gray.darker();
  public static Color PIN_COLOR = METAL_COLOR;
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color INDENT_COLOR = Color.gray.darker();
  public static Color LABEL_COLOR = Color.white;
  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static int PIN_COUNT = 4;
  public static Size CASE_SIZE = new Size(6d, SizeUnit.mm);
  public static Size INDENT_SIZE = new Size(3d, SizeUnit.mm);
  private static Size PIN_SPACING = new Size(4.5d, SizeUnit.mm);
  private static Size ROW_SPACING = new Size(6.5d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Display display = Display.BOTH;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  transient private Area[] body;

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
    controlPoints = new Point2D[PIN_COUNT];
    controlPoints[0] = firstPoint;
    double pinSpacing = PIN_SPACING.convertToPixels();
    double rowSpacing = ROW_SPACING.convertToPixels();
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
      double caseSize = CASE_SIZE.convertToPixels();
      double indentSize = INDENT_SIZE.convertToPixels();

      width = height = caseSize;
      x = (controlPoints[0].getX() + controlPoints[3].getX() - width) / 2;
      y = (controlPoints[0].getY() + controlPoints[3].getY() - height) / 2;

      body[0] = new Area(new Rectangle2D.Double(x, y, width, height));

      width = height = indentSize;
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
        g2d.fill(rect);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(rect);
      }
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);

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
}
