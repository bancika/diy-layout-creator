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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Plastic DC Jack", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Panel mount plastic DC jack", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "J", autoEdit = false, enableCache = true)
public class PlasticDCJack extends AbstractMultiPartComponent<String> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  private static Size LUG_WIDTH = new Size(0.08d, SizeUnit.in);
  private static Size LUG_THICKNESS = new Size(0.02d, SizeUnit.in);
  private static Size SPACING = new Size(0.1d, SizeUnit.in);
  private static Size DIAMETER = new Size(0.5d, SizeUnit.in);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color BORDER_COLOR = Color.black;
  private static Color MARKING_COLOR = Color.lightGray;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  private String value = "";
  private DCPolarity polarity = DCPolarity.CENTER_NEGATIVE;
  transient private Area[] body;

  public PlasticDCJack() {
    updateControlPoints();
  }

  private void updateControlPoints() {
    // invalidate body shape
    body = null;

    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();

    int spacing = (int) SPACING.convertToPixels();
    controlPoints[1] = new Point2D.Double(x + spacing, y + spacing);
    controlPoints[2] = new Point2D.Double(x - spacing, y + spacing * 2);
  }

  @Override
  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      int spacing = (int) SPACING.convertToPixels();
      int diameter = getClosestOdd(DIAMETER.convertToPixels());
      body[0] = new Area(new Ellipse2D.Double(x - diameter / 2, y + spacing - diameter / 2, diameter, diameter));

      int rectWidth = (int) (diameter / Math.sqrt(2)) - 2;
      body[1] = new Area(new Rectangle2D.Double(x - rectWidth / 2, y + spacing - rectWidth / 2, rectWidth, rectWidth));

      int lugWidth = getClosestOdd(LUG_WIDTH.convertToPixels());
      int lugThickness = getClosestOdd(LUG_THICKNESS.convertToPixels());

      Point2D groundPoint = controlPoints[controlPoints.length - 1];
      Area groundLug =
          new Area(new Ellipse2D.Double(groundPoint.getX() + spacing - lugWidth / 2, groundPoint.getY() - lugWidth / 2, lugWidth,
              lugWidth));
      groundLug.add(new Area(new Rectangle2D.Double(groundPoint.getX(), groundPoint.getY() - lugWidth / 2, spacing, lugWidth)));
      groundLug.subtract(new Area(new Ellipse2D.Double(groundPoint.getX() + spacing - lugWidth / 6, groundPoint.getY() - lugWidth
          / 6, lugWidth / 3, lugWidth / 3)));
      body[2] = groundLug;

      Area lugArea = new Area();
      for (int i = 0; i < controlPoints.length; i++) {
        Point2D point = controlPoints[i];
        if (i == getControlPointCount() - 1) {
          lugArea.add(new Area(
              new Rectangle2D.Double(point.getX() - lugThickness / 2, point.getY() - lugWidth / 2, lugThickness, lugWidth)));
        } else {
          lugArea.add(new Area(
              new Rectangle2D.Double(point.getX() - lugWidth / 2, point.getY() - lugThickness / 2, lugWidth, lugThickness)));
        }
      }
      body[3] = lugArea;
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
//    if (componentState != ComponentState.DRAGGING) {
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    g2d.fill(body[0]);
    drawingObserver.stopTracking();
    if (!outlineMode) {
      g2d.setColor(PHENOLIC_DARK_COLOR);
      g2d.fill(body[1]);
    }
    g2d.setComposite(oldComposite);
//    }

    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    Color finalBorderColor;
    if (outlineMode) {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : BORDER_COLOR;
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    if (!outlineMode) {
      g2d.setColor(PHENOLIC_DARK_COLOR.darker());
      g2d.draw(body[1]);

      g2d.setColor(METAL_COLOR);
      g2d.fill(body[2]);
      g2d.setColor(METAL_COLOR.darker());
      g2d.draw(body[2]);

      g2d.setColor(METAL_COLOR);
      g2d.fill(body[3]);
    }

    g2d.setColor(outlineMode ? theme.getOutlineColor() : METAL_COLOR.darker());
    g2d.draw(body[3]);

    if (!outlineMode && getPolarity() != DCPolarity.NONE) {
      int spacing = (int) SPACING.convertToPixels();
      g2d.setColor(MARKING_COLOR);
      g2d.setFont(project.getFont().deriveFont(12f));     
      StringUtils.drawCenteredText(g2d, getPolarity() == DCPolarity.CENTER_NEGATIVE ? "+" : "-", controlPoints[0].getX(),
          controlPoints[0].getY() - spacing * 7 / 16, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, getPolarity() == DCPolarity.CENTER_NEGATIVE ? "_" : "+", controlPoints[2].getX(),
          controlPoints[2].getY() - spacing * 3 / 4, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 2 * 32 / width;
    int diameter = getClosestOdd(width - margin);
    g2d.setColor(BODY_COLOR);
    g2d.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
    int rectWidth = getClosestOdd(((width - 2 * margin) / Math.sqrt(2)) - margin / 2);
    g2d.setColor(PHENOLIC_DARK_COLOR);
    g2d.fillRect((width - rectWidth) / 2, (height - rectWidth) / 2, rectWidth, rectWidth);
    g2d.setColor(PHENOLIC_DARK_COLOR.darker());
    g2d.drawRect((width - rectWidth) / 2, (height - rectWidth) / 2, rectWidth, rectWidth);
    int lugWidth = 4 * 32 / width;
    g2d.setColor(METAL_COLOR);
    g2d.drawLine((width - lugWidth) / 2, height / 3, (width + lugWidth) / 2, height / 3);
    g2d.drawLine(width * 2 / 3, (height - lugWidth) / 2, width * 2 / 3, (height + lugWidth) / 2);
    g2d.fillOval((width - lugWidth) / 2, height * 2 / 3 - lugWidth / 2, lugWidth, lugWidth);
    g2d.fillRect(width / 2 - lugWidth * 3 / 2, height * 2 / 3 - lugWidth / 2, lugWidth * 3 / 2, lugWidth);
    g2d.setColor(PHENOLIC_DARK_COLOR);
    g2d.fillOval((width - margin) / 2, height * 2 / 3 - margin / 2, margin, margin);
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    this.body = null;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public DCPolarity getPolarity() {
    if (polarity == null) {
      polarity = DCPolarity.CENTER_NEGATIVE;
    }
    return polarity;
  }

  public void setPolarity(DCPolarity polarity) {
    this.polarity = polarity;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {    
    int margin = 20;    
    Rectangle2D bounds = getBody()[0].getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin, bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
  
  @Deprecated
  @Override
  public Integer getFontSizeOverride() {
    return super.getFontSizeOverride();
  }
}
