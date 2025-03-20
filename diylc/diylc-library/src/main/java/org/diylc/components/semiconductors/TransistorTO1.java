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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.transform.TO1Transformer;
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

@ComponentDescriptor(name = "Transistor (TO-1)", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "Q", description = "Transistor with small metal body",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = TO1Transformer.class,
    enableCache = true)
public class TransistorTO1 extends AbstractTransistorPackage {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.lightGray;
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.black;
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
  public static Size PIN_SPACING = new Size(0.05d, SizeUnit.in);
  public static Size BODY_DIAMETER = new Size(0.24d, SizeUnit.in);
  public static Size BODY_LENGTH = new Size(0.4d, SizeUnit.in);
  public static Size EDGE_RADIUS = new Size(2d, SizeUnit.mm);
  
  private Size pinSpacing = PIN_SPACING;

  public TransistorTO1() {
    super();
    updateControlPoints();
    alpha = (byte) 100;
    bodyColor = BODY_COLOR;
    borderColor = BORDER_COLOR;
  }

  @Override
  protected void updateControlPoints() {
    int pinSpacing = (int) getPinSpacing().convertToPixels();
    // Update control points.
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(x - (folded ? 0 : pinSpacing), y + pinSpacing);
        controlPoints[2].setLocation(x, y + 2 * pinSpacing);
        break;
      case _90:
        controlPoints[1].setLocation(x - pinSpacing, y - (folded ? 0 : pinSpacing));
        controlPoints[2].setLocation(x - 2 * pinSpacing, y);
        break;
      case _180:
        controlPoints[1].setLocation(x + (folded ? 0 : pinSpacing), y - pinSpacing);
        controlPoints[2].setLocation(x, y - 2 * pinSpacing);
        break;
      case _270:
        controlPoints[1].setLocation(x + pinSpacing, y + (folded ? 0 : pinSpacing));
        controlPoints[2].setLocation(x + 2 * pinSpacing, y);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      double x = (controlPoints[0].getX() + controlPoints[1].getX() + controlPoints[2].getX()) / 3;
      double y = (controlPoints[0].getY() + controlPoints[1].getY() + controlPoints[2].getY()) / 3;
      int bodyDiameter = getClosestOdd(BODY_DIAMETER.convertToPixels());
      int bodyLength = getClosestOdd(BODY_LENGTH.convertToPixels());
      int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      
      Area newBody = null;

      if (folded) {
        switch (orientation) {
          case DEFAULT:
            newBody =
            new Area(new RoundRectangle2D.Double(x, y - bodyDiameter / 2, bodyLength, bodyDiameter, edgeRadius,
                edgeRadius));
            newBody.add(new Area(new Rectangle2D.Double(x, y - bodyDiameter / 2, bodyLength / 2, bodyDiameter)));
            break;
          case _90:
            newBody =
            new Area(new RoundRectangle2D.Double(x - bodyDiameter / 2, y, bodyDiameter, bodyLength, edgeRadius,
                edgeRadius));
            newBody.add(new Area(new Rectangle2D.Double(x - bodyDiameter / 2, y, bodyDiameter, bodyLength / 2)));
            break;
          case _180:
            newBody =
            new Area(new RoundRectangle2D.Double(x - bodyLength, y - bodyDiameter / 2, bodyLength, bodyDiameter,
                edgeRadius, edgeRadius));
            newBody.add(new Area(new Rectangle2D.Double(x - bodyLength / 2, y - bodyDiameter / 2, bodyLength / 2,
                bodyDiameter)));

            break;
          case _270:
            newBody =
            new Area(new RoundRectangle2D.Double(x - bodyDiameter / 2, y - bodyLength, bodyDiameter, bodyLength,
                edgeRadius, edgeRadius));
            newBody.add(new Area(new Rectangle2D.Double(x - bodyDiameter / 2, y - bodyLength / 2, bodyDiameter,
                bodyLength / 2)));            
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      } else {
        newBody = new Area(new Ellipse2D.Double(x - bodyDiameter / 2, y - bodyDiameter / 2, bodyDiameter, bodyDiameter));
      }

      body = new Area[] { newBody };
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
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    for (Point2D point : controlPoints) {
      Shape shape = new Ellipse2D.Double(point.getX() - pinSize / 2, point.getY() - pinSize / 2, pinSize, pinSize);
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fill(shape);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);      
      g2d.draw(shape);
    }
    
    Area mainArea = getBody()[0];
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);
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
    if (getDisplay() == TransistorDisplay.PINOUT) {
      drawPinout(g2d);
    } else {
      String label = "";
      label = (getDisplay() == TransistorDisplay.NAME) ? getName() : getValue();
      if (getDisplay() == TransistorDisplay.NONE) {
        label = "";
      }
      if (getDisplay() == TransistorDisplay.BOTH) {
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
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = (int) (2f * width / 32);
    Area area = new Area(new Ellipse2D.Double(margin / 2, margin, width - 2 * margin, width - 2 * margin));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(area);
    g2d.setColor(PIN_COLOR);
    int pinSize = 2 * width / 32;
    for (int i = 0; i < 3; i++) {
      g2d.fillOval((i == 1 ? width * 3 / 8 : width / 2) - pinSize / 2, (height / 4) * (i + 1), pinSize, pinSize);
    }
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
  
  @Override
  protected boolean flipPinoutLabel() {    
    return true;
  }
}
