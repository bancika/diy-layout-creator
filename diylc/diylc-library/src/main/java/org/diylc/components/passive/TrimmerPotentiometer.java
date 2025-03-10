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
 */
package org.diylc.components.passive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.transform.TrimmerTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Trimmer Potentiometer", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.SINGLE_CLICK, instanceNamePrefix = "VR",
    description = "Various types of board mounted trimmer potentiometers", zOrder = IDIYComponent.COMPONENT,
    transformer = TrimmerTransformer.class)
public class TrimmerPotentiometer extends AbstractPotentiometer {

  private static final long serialVersionUID = 1L;

  protected static Size FLAT_BODY_SIZE = new Size(9.5d, SizeUnit.mm);
  protected static Size FLAT_LARGE_BODY_SIZE = new Size(13d, SizeUnit.mm);
  protected static Size FLAT_SMALL_BODY_SIZE = new Size(5d, SizeUnit.mm);
  protected static Size FLAT_SMALL2_BODY_SIZE = new Size(9.5d, SizeUnit.mm);
  protected static Size FLAT_SHAFT_SIZE = new Size(4.5d, SizeUnit.mm);
  protected static Size VERTICAL_BODY_LENGTH = new Size(9.5d, SizeUnit.mm);
  protected static Size VERTICAL_BODY_WIDTH = new Size(4.5d, SizeUnit.mm);
  protected static Size ROUNDED_EDGE = new Size(1d, SizeUnit.mm);
  protected static Size SPACING = new Size(0.1d, SizeUnit.in);
  private static Color BODY_COLOR = Color.decode("#FFFFE0");
  private static Color BORDER_COLOR = Color.decode("#8E8E38");
  private static Color SHAFT_COLOR = Color.decode("#FFFFE0");
  private static Color SHAFT_BORDER_COLOR = Color.decode("#8E8E38");
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
  protected static Display DISPLAY = Display.NAME;

  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Display display = DISPLAY;
  // Array of 7 elements: 3 lug connectors, 1 pot body and 3 lugs
  transient protected Shape[] body = null;

  protected TrimmerType type = TrimmerType.FLAT_SMALL;

  public TrimmerPotentiometer() {
    controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
    updateControlPoints();
  }

  protected void updateControlPoints() {
    int spacing = (int) SPACING.convertToPixels();
    int dx1 = 0;
    int dy1 = 0;
    int dx2 = 0;
    int dy2 = 0;
    switch (getOrientation()) {
      case DEFAULT:
        switch (getType()) {
          case FLAT_SMALL:
          case FLAT_XSMALL:
            dx1 = 2 * spacing;
            dy1 = spacing;
            dx2 = 0;
            dy2 = 2 * spacing;
            break;
          case FLAT_SMALL2:
            dx1 = 4 * spacing;
            dy1 = spacing;
            dx2 = 0;
            dy2 = 2 * spacing;
            break;
          case FLAT_LARGE:
            dx1 = 4 * spacing;
            dy1 = spacing;
            dx2 = 0;
            dy2 = 2 * spacing;
            break;
          case FLAT_XLARGE:
            dx1 = 5 * spacing;
            dy1 = 2 * spacing;
            dx2 = 0;
            dy2 = 4 * spacing;
            break;
          case VERTICAL_INLINE:
            dx1 = 0;
            dy1 = spacing;
            dx2 = 0;
            dy2 = 2 * spacing;
            break;
          case VERTICAL_OFFSET:
            dx1 = spacing;
            dy1 = spacing;
            dx2 = 0;
            dy2 = 2 * spacing;
            break;
          case VERTICAL_OFFSET_BIG_GAP:
            dx1 = 2 * spacing;
            dy1 = spacing;
            dx2 = 0;
            dy2 = 2 * spacing;
            break;
        }
        break;
      case _90:
        switch (getType()) {
          case FLAT_SMALL:
          case FLAT_XSMALL:
            dx1 = -spacing;
            dy1 = 2 * spacing;
            dx2 = -2 * spacing;
            dy2 = 0;
            break;
          case FLAT_SMALL2:
            dx1 = -spacing;
            dy1 = 4 * spacing;
            dx2 = -2 * spacing;
            dy2 = 0;
            break;
          case FLAT_LARGE:
            dx1 = -spacing;
            dy1 = 4 * spacing;
            dx2 = -2 * spacing;
            dy2 = 0;
            break;
          case FLAT_XLARGE:
            dx1 = -2 * spacing;
            dy1 = 5 * spacing;
            dx2 = -4 * spacing;
            dy2 = 0;
            break;
          case VERTICAL_INLINE:
            dx1 = -spacing;
            dy1 = 0;
            dx2 = -2 * spacing;
            dy2 = 0;
            break;
          case VERTICAL_OFFSET:
            dx1 = -spacing;
            dy1 = spacing;
            dx2 = -2 * spacing;
            dy2 = 0;
            break;
          case VERTICAL_OFFSET_BIG_GAP:
            dx1 = -spacing;
            dy1 = 2 * spacing;
            dx2 = -2 * spacing;
            dy2 = 0;
            break;
        }
        break;
      case _180:
        switch (getType()) {
          case FLAT_SMALL:
          case FLAT_XSMALL:
            dx1 = -2 * spacing;
            dy1 = -spacing;
            dx2 = 0;
            dy2 = -2 * spacing;
            break;
          case FLAT_SMALL2:
            dx1 = -4 * spacing;
            dy1 = -spacing;
            dx2 = 0;
            dy2 = -2 * spacing;
            break;
          case FLAT_LARGE:
            dx1 = -4 * spacing;
            dy1 = -spacing;
            dx2 = 0;
            dy2 = -2 * spacing;
            break;
          case FLAT_XLARGE:
            dx1 = -5 * spacing;
            dy1 = -2 * spacing;
            dx2 = 0;
            dy2 = -4 * spacing;
            break;
          case VERTICAL_INLINE:
            dx1 = 0;
            dy1 = -spacing;
            dx2 = 0;
            dy2 = -2 * spacing;
            break;
          case VERTICAL_OFFSET:
            dx1 = -spacing;
            dy1 = -spacing;
            dx2 = 0;
            dy2 = -2 * spacing;
            break;
          case VERTICAL_OFFSET_BIG_GAP:
            dx1 = -2 * spacing;
            dy1 = -spacing;
            dx2 = 0;
            dy2 = -2 * spacing;
            break;
        }
        break;
      case _270:
        switch (getType()) {
          case FLAT_SMALL:
          case FLAT_XSMALL:
            dx1 = spacing;
            dy1 = -2 * spacing;
            dx2 = 2 * spacing;
            dy2 = 0;
            break;
          case FLAT_SMALL2:
            dx1 = spacing;
            dy1 = -4 * spacing;
            dx2 = 2 * spacing;
            dy2 = 0;
            break;
          case FLAT_LARGE:
            dx1 = spacing;
            dy1 = -4 * spacing;
            dx2 = 2 * spacing;
            dy2 = 0;
            break;
          case FLAT_XLARGE:
            dx1 = 2 * spacing;
            dy1 = -5 * spacing;
            dx2 = 4 * spacing;
            dy2 = 0;
            break;
          case VERTICAL_INLINE:
            dx1 = spacing;
            dy1 = 0;
            dx2 = 2 * spacing;
            dy2 = 0;
            break;
          case VERTICAL_OFFSET:
            dx1 = spacing;
            dy1 = -spacing;
            dx2 = 2 * spacing;
            dy2 = 0;
            break;
          case VERTICAL_OFFSET_BIG_GAP:
            dx1 = spacing;
            dy1 = -2 * spacing;
            dx2 = 2 * spacing;
            dy2 = 0;
            break;
        }
        break;
      default:
        break;
    }
    controlPoints[1].setLocation(controlPoints[0].getX() + dx1, controlPoints[0].getY() + dy1);
    controlPoints[2].setLocation(controlPoints[0].getX() + dx2, controlPoints[0].getY() + dy2);
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[2];

      // Calculate the center point as center of the minimum bounding
      // rectangle.
      double centerX =
          (Math.max(Math.max(controlPoints[0].getX(), controlPoints[1].getX()), controlPoints[2].getX()) + Math.min(
              Math.min(controlPoints[0].getX(), controlPoints[1].getX()), controlPoints[2].getX())) / 2;
      double centerY =
          (Math.max(Math.max(controlPoints[0].getY(), controlPoints[1].getY()), controlPoints[2].getY()) + Math.min(
              Math.min(controlPoints[0].getY(), controlPoints[1].getY()), controlPoints[2].getY())) / 2;

      // Calculate body dimensions based on the selected type.
      int length = 0;
      int width = 0;
      switch (getType()) {
        case FLAT_LARGE:
        case FLAT_SMALL:
        case FLAT_XSMALL:
        case FLAT_XLARGE:
        case FLAT_SMALL2:
          if (getType() == TrimmerType.FLAT_XSMALL)
            length = getClosestOdd(FLAT_SMALL_BODY_SIZE.convertToPixels());
          else if (getType() == TrimmerType.FLAT_XLARGE)
            length = getClosestOdd(FLAT_LARGE_BODY_SIZE.convertToPixels());
          else if (getType() == TrimmerType.FLAT_SMALL2)
            length = getClosestOdd(FLAT_SMALL2_BODY_SIZE.convertToPixels());
          else
            length = getClosestOdd(FLAT_BODY_SIZE.convertToPixels());
          width = length;
          int shaftSize = getClosestOdd(FLAT_SHAFT_SIZE.convertToPixels());
          Area shaft =
              new Area(new Ellipse2D.Double(centerX - shaftSize / 2, centerY - shaftSize / 2, shaftSize, shaftSize));
          Area slot =
              new Area(new Rectangle2D.Double(centerX - shaftSize / 2, centerY - shaftSize / 8, shaftSize,
                  shaftSize / 4));
          slot.transform(AffineTransform.getRotateInstance(Math.PI / 4, centerX, centerY));
          shaft.subtract(slot);
          body[1] = shaft;
          break;
        case VERTICAL_INLINE:
        case VERTICAL_OFFSET:
        case VERTICAL_OFFSET_BIG_GAP:
          length = getClosestOdd(VERTICAL_BODY_LENGTH.convertToPixels());
          width = getClosestOdd(VERTICAL_BODY_WIDTH.convertToPixels());
          break;
      }
      if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
        int p = length;
        length = width;
        width = p;
      }
      double edge = ROUNDED_EDGE.convertToPixels();
      if (getType() == TrimmerType.FLAT_SMALL2)
        body[0] = new Ellipse2D.Double(centerX - length / 2, centerY - width / 2, length, width);
      else
        body[0] = new RoundRectangle2D.Double(centerX - length / 2, centerY - width / 2, length, width, edge, edge);
    }
    return body;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    super.setControlPoint(point, index);
    body = null;
  }

  @Override
  public void setOrientation(Orientation orientation) {
    super.setOrientation(orientation);
    updateControlPoints();
    body = null;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Shape mainShape = getBody()[0];
    Shape shaftShape = getBody()[1];
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    if (mainShape != null) {
      g2d.setColor(bodyColor);
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      if (!outlineMode) {
        g2d.fill(mainShape);
      }
      if (!outlineMode && shaftShape != null) {
        g2d.setColor(SHAFT_COLOR);
        g2d.fill(shaftShape);
        g2d.setColor(SHAFT_BORDER_COLOR);
        g2d.draw(shaftShape);
      }
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
      g2d.draw(mainShape);
    }

    // Draw pins.
    int pinSize = getClosestOdd(PIN_SIZE.convertToPixels());
    for (Point2D point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fillOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
      g2d.drawOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
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
              : LABEL_COLOR;
    }

    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : (getValue() == null ? "" : getValue().toString());
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }

    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D bodyRect = getBody()[0].getBounds2D();
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);

    int textHeight = (int) rect.getHeight();
    int textWidth = (int) rect.getWidth();
    int panelHeight = (int) bodyRect.getHeight();
    int panelWidth = (int) bodyRect.getWidth();

    int x = (panelWidth - textWidth) / 2;
    int y = (panelHeight - textHeight) / 2 + fontMetrics.getAscent();

    g2d.drawString(label, (int) (bodyRect.getX() + x), (int) (bodyRect.getY() + y));
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 4;
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(margin, margin, width - 2 * margin, width - 2 * margin);
    g2d.setColor(BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawRect(margin, margin, width - 2 * margin, width - 2 * margin);
    int shaftSize = 11;
    int slotSize = 2;
    Area area =
        new Area(new Ellipse2D.Double(width / 2 - shaftSize / 2, width / 2 - shaftSize / 2, shaftSize, shaftSize));
    Area slot = new Area(new Rectangle2D.Double(0, width / 2 - slotSize / 2, width, slotSize));
    slot.transform(AffineTransform.getRotateInstance(Math.PI / 4, width / 2, width / 2));
    area.subtract(slot);
    g2d.setColor(SHAFT_COLOR);
    g2d.fill(area);
    g2d.setColor(SHAFT_BORDER_COLOR);
    g2d.draw(area);

    int pinSize = 3;
    g2d.setColor(PIN_COLOR);
    g2d.fillOval(margin - pinSize / 2, 10 - pinSize / 2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawOval(margin - pinSize / 2, 10 - pinSize / 2, pinSize, pinSize);

    g2d.setColor(PIN_COLOR);
    g2d.fillOval(margin - pinSize / 2, 21 - pinSize / 2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawOval(margin - pinSize / 2, 21 - pinSize / 2, pinSize, pinSize);

    g2d.setColor(PIN_COLOR);
    g2d.fillOval(width - margin - pinSize / 2, width / 2 - pinSize / 2, pinSize, pinSize);
    g2d.setColor(PIN_BORDER_COLOR);
    g2d.drawOval(width - margin - pinSize / 2, width / 2 - pinSize / 2, pinSize, pinSize);
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = DISPLAY;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty
  public TrimmerType getType() {
    return type;
  }

  public void setType(TrimmerType type) {
    this.type = type;
    updateControlPoints();
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  public static enum TrimmerType {
    FLAT_SMALL("Horizontal Small 1"), FLAT_SMALL2("Horizontal Small 2"), FLAT_XSMALL("Horizontal X-Small"), FLAT_LARGE(
        "Horizontal Medium"), FLAT_XLARGE("Horizontal Large"), VERTICAL_INLINE("Vertical Inline"), VERTICAL_OFFSET(
        "Vertical Offset 1"), VERTICAL_OFFSET_BIG_GAP("Vertical Offset 2");

    String label;

    private TrimmerType(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;// name().substring(0, 1) + name().substring(1).toLowerCase().replace("_", " ");
    }
  }
}
