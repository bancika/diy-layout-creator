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
package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.transform.TO220Transformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Transistor (TO-126)", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "Q", description = "Transistors with a hole for heat sink mounting",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = TO220Transformer.class,
    enableCache = true)
public class TransistorTO126 extends AbstractTransistorPackage {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.gray;
  public static Color BORDER_COLOR = Color.gray.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.white;
  public static Size PIN_SIZE = new Size(0.8d, SizeUnit.mm);
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Size BODY_WIDTH = new Size(8d, SizeUnit.mm);
  public static Size BODY_THICKNESS = new Size(3.25d, SizeUnit.mm);
  public static Size BODY_HEIGHT = new Size(11d, SizeUnit.mm);
  public static Size HOLE_DIAMETER = new Size(3.2d, SizeUnit.mm);
  public static Size HOLE_OFFSET = new Size(8.1d, SizeUnit.mm);
  public static Size LEAD_LENGTH = new Size(3.5d, SizeUnit.mm);
  public static Size LEAD_THICKNESS = new Size(0.8d, SizeUnit.mm);

  private boolean folded = false;
  private Size leadLength = LEAD_LENGTH;

  public TransistorTO126() {
    super();
    updateControlPoints();
    alpha = (byte) 100;
    bodyColor = BODY_COLOR;
    borderColor = BORDER_COLOR;
  }

  @Override
  protected void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(x, y + pinSpacing);
        controlPoints[2].setLocation(x, y + 2 * pinSpacing);
        break;
      case _90:
        controlPoints[1].setLocation(x - pinSpacing, y);
        controlPoints[2].setLocation(x - 2 * pinSpacing, y);
        break;
      case _180:
        controlPoints[1].setLocation(x, y - pinSpacing);
        controlPoints[2].setLocation(x, y - 2 * pinSpacing);
        break;
      case _270:
        controlPoints[1].setLocation(x + pinSpacing, y);
        controlPoints[2].setLocation(x + 2 * pinSpacing, y);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int pinSpacing = (int) PIN_SPACING.convertToPixels();
      int bodyWidth = getClosestOdd(BODY_WIDTH.convertToPixels());
      int bodyThickness = getClosestOdd(BODY_THICKNESS.convertToPixels());
      int bodyHeight = getClosestOdd(BODY_HEIGHT.convertToPixels());
    
      int tabHoleDiameter = (int) HOLE_DIAMETER.convertToPixels();
      int holeOffset = (int) HOLE_OFFSET.convertToPixels();
      double leadLength = getLeadLength().convertToPixels();

      switch (orientation) {
        case DEFAULT:
          if (folded) {
            body[0] = new Area(new Rectangle2D.Double(x + leadLength, y + pinSpacing - bodyWidth / 2, bodyHeight, bodyWidth));
            ((Area) body[0]).subtract(new Area(new Ellipse2D.Double(x + leadLength + holeOffset
                - tabHoleDiameter / 2, y + pinSpacing - tabHoleDiameter / 2, tabHoleDiameter, tabHoleDiameter)));
          } else {
            body[0] = new Area(
                new Rectangle2D.Double(x - bodyThickness / 2, y + pinSpacing - bodyWidth / 2, bodyThickness, bodyWidth));
          }
          break;
        case _90:
          if (folded) {
            body[0] = new Area(new Rectangle2D.Double(x - pinSpacing - bodyWidth / 2, y + leadLength, bodyWidth, bodyHeight));
            ((Area) body[0]).subtract(new Area(new Ellipse2D.Double(x - pinSpacing - tabHoleDiameter / 2, y
                + leadLength + holeOffset - tabHoleDiameter / 2, tabHoleDiameter, tabHoleDiameter)));
          } else {
            body[0] = new Area(
                new Rectangle2D.Double(x - pinSpacing - bodyWidth / 2, y - bodyThickness / 2, bodyWidth, bodyThickness));
          }
          break;
        case _180:
          if (folded) {
            body[0] = new Area(
                new Rectangle2D.Double(x - leadLength - bodyHeight, y - pinSpacing - bodyWidth / 2, bodyHeight,
                    bodyWidth));
            ((Area) body[0]).subtract(new Area(new Ellipse2D.Double(x - leadLength - holeOffset
                - tabHoleDiameter / 2, y - pinSpacing - tabHoleDiameter / 2, tabHoleDiameter, tabHoleDiameter)));
          } else {
            body[0] = new Area(
                new Rectangle2D.Double(x - bodyThickness / 2, y - pinSpacing - bodyWidth / 2, bodyThickness, bodyWidth));
          }
          break;
        case _270:
          if (folded) {
            body[0] = new Area(
                new Rectangle2D.Double(x + pinSpacing - bodyWidth / 2, y - leadLength - bodyHeight, bodyWidth,
                    bodyHeight));
            ((Area) body[0]).subtract(new Area(new Ellipse2D.Double(x + pinSpacing - tabHoleDiameter / 2, y
                - leadLength - holeOffset - tabHoleDiameter / 2, tabHoleDiameter, tabHoleDiameter)));
          } else {
            body[0] = new Area(
                new Rectangle2D.Double(x + pinSpacing - bodyWidth / 2, y - bodyThickness / 2, bodyWidth, bodyThickness));
          }
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
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
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    
    // Draw pins.

    if (folded) {
      int leadThickness = getClosestOdd(LEAD_THICKNESS.convertToPixels());
      int leadLength = (int) getLeadLength().convertToPixels();
      Color finalPinColor;
      Color finalPinBorderColor;
      if (outlineMode) {
        finalPinColor = new Color(0, 0, 0, 0);
        finalPinBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalPinColor = METAL_COLOR;
        finalPinBorderColor = METAL_COLOR.darker();
      }
      for (Point point : controlPoints) {
        switch (orientation) {
          case DEFAULT:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x + leadLength - leadThickness / 2, point.y);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x + leadLength - leadThickness / 2, point.y);
            break;
          case _90:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x, point.y + leadLength - leadThickness / 2);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x, point.y + leadLength - leadThickness / 2);
            break;
          case _180:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x - leadLength - leadThickness / 2, point.y);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x - leadLength - leadThickness / 2, point.y);
            break;
          case _270:
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness));
            g2d.setColor(finalPinBorderColor);
            g2d.drawLine(point.x, point.y, point.x, point.y - leadLength);
            g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(leadThickness - 2));
            g2d.setColor(finalPinColor);
            g2d.drawLine(point.x, point.y, point.x, point.y - leadLength);
            break;
        }
      }
    } else {
      if (!outlineMode) {
        for (Point point : controlPoints) {

          g2d.setColor(PIN_COLOR);
          g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
          g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
          g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
        }
      }
    }

    
    Shape mainArea = getBody()[0];
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);    
    
    g2d.setComposite(oldComposite);
    if (!outlineMode) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    }
  
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
    g2d.draw(mainArea);
    
    g2d.setComposite(oldComposite);

    // Draw label.
    g2d.setFont(project.getFont());
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
    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : getValue();
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
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
    int margin = 2 * width / 32;
    int bodyWidth = width * 5 / 10;
    int bodyHeight = width * 7 / 10;
    int holeSize = 6 * width / 32;
    Area a = new Area(new Rectangle2D.Double((width - bodyWidth) / 2, margin, bodyWidth, bodyHeight));
    a.subtract(new Area(new Ellipse2D.Double(width / 2 - holeSize / 2, margin + bodyHeight / 3 - holeSize / 2, holeSize,
        holeSize)));
    g2d.setColor(BODY_COLOR);
    g2d.fill(a);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(a);

    g2d.setColor(METAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.drawLine(width / 2, margin + bodyHeight, width / 2, height - margin);
    g2d.drawLine(width / 2 - bodyWidth / 3, margin + bodyHeight + 1, width / 2 - bodyWidth / 3, height - margin);
    g2d.drawLine(width / 2 + bodyWidth / 3, margin + bodyHeight + 1, width / 2 + bodyWidth / 3, height - margin);
  }

  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
    // Invalidate the body
    this.body = null;
  }

  @EditableProperty(name = "Lead Length")
  public Size getLeadLength() {
    if (leadLength == null) {
      leadLength = LEAD_LENGTH;
    }
    return leadLength;
  }

  public void setLeadLength(Size leadLength) {
    this.leadLength = leadLength;
    // Invalidate the body
    this.body = null;
  }
}
