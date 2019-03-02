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
package org.diylc.components.guitar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractGuitarPickup extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;
  
  protected static Size POINT_SPACING = new Size(0.1d, SizeUnit.in);
  
  protected static final int TERMINAL_FONT_SIZE = 11;  
  
  protected String value = "";
  protected Orientation orientation = Orientation.DEFAULT;
  protected transient Shape[] body;  
  
  protected Point controlPoint = new Point(0, 0);
  protected Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  
  protected Polarity polarity = Polarity.North;  
  
  protected Color labelColor;
  
  public AbstractGuitarPickup() {
    updateControlPoints();
  }
  
  protected void drawlTerminalLabels(Graphics2D g2d, Color color, Project project) {
    Point[] points = getControlPoints();    
    g2d.setColor(color);
      
    g2d.setFont(project.getFont().deriveFont(TERMINAL_FONT_SIZE * 1f));
    int dx = 0;
    int dy = 0;
    switch (orientation) {
      case DEFAULT:        
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : (int) (TERMINAL_FONT_SIZE * 0.8);
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? -TERMINAL_FONT_SIZE : 0;  
        break;
      case _90:
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? TERMINAL_FONT_SIZE : 0;
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : (int) (TERMINAL_FONT_SIZE * 0.8);
        break;
      case _180:
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : -(int) (TERMINAL_FONT_SIZE * 0.8);
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? TERMINAL_FONT_SIZE  : 0;       
        break;
      case _270:
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? -TERMINAL_FONT_SIZE : 0;
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : -(int) (TERMINAL_FONT_SIZE * 0.8);
        break;     
    }   

    drawCenteredText(g2d, "N", (points[0].x + points[1].x) / 2 + dx, (points[0].y + points[1].y) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    drawCenteredText(g2d, "S", (points[2].x + points[3].x) / 2 + dx, (points[2].y + points[3].y) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);   
  }
  
  protected void drawMainLabel(Graphics2D g2d, Project project, boolean outlineMode, ComponentState componentState) {
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
    g2d.setFont(project.getFont().deriveFont(Font.BOLD));
    Rectangle bounds = getBody()[0].getBounds();
    
    AffineTransform originalTx = g2d.getTransform();
    g2d.translate(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    if (orientation == Orientation._90)
      g2d.rotate(Math.PI / 2);
    else if (orientation == Orientation._270){
      g2d.rotate(-Math.PI / 2);
    }
    g2d.translate(0, getMainLabelYOffset());
    
    drawCenteredText(g2d, getName(), 0, 0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
   
    g2d.setTransform(originalTx);
  }
  
  protected abstract Shape[] getBody();
  
  public abstract boolean isHumbucker();
  
  protected int getMainLabelYOffset() {
    return 0;
  }
  
  @EditableProperty(name = "Model")
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    // Invalidate the body
    body = null;
    updateControlPoints();
  }
  
  protected Point[] getControlPoints() {
    if (controlPoints == null) {
      controlPoints =
          new Point[] {controlPoint, new Point(controlPoint.x, controlPoint.y),
              new Point(controlPoint.x, controlPoint.y), new Point(controlPoint.x, controlPoint.y)};
      updateControlPoints();
    }
    return controlPoints;
  }
  
  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }


  @Override
  public Point getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    getControlPoints()[index].setLocation(point);
    // Invalidate the body
    body = null;
  }
  
  @EditableProperty
  public Polarity getPolarity() {
    if (polarity == null)
      polarity = Polarity.North;
    return polarity;
  }
  
  public void setPolarity(Polarity polarity) {
    this.polarity = polarity;
    // Invalidate the body
    body = null;
  }
  
  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null)
      labelColor = LABEL_COLOR;
    return labelColor;
  }
  
  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }
  
  protected abstract OrientationHV getControlPointDirection();

  @SuppressWarnings("incomplete-switch")
  protected void updateControlPoints() {
    Point[] points = getControlPoints();
    int pointSpacing = (int) POINT_SPACING.convertToPixels();
    int dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 1 : 0;
    int dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : 1;
    if (orientation != Orientation.DEFAULT) {
      switch (orientation) {
        case _90:
          dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : -1;
          dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? -1 : 0;
          break;
        case _180:
          dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? -1 : 0;
          dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : -1;
          break;
        case _270:
          dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : 1;
          dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 1 : 0;
          break;
      }
    }
    points[1].setLocation(points[0].x + dx * pointSpacing, points[0].y + dy * pointSpacing);
    points[2]
        .setLocation(points[0].x + 2 * dx * pointSpacing, points[0].y + 2 * dy * pointSpacing);
    points[3]
        .setLocation(points[0].x + 3 * dx * pointSpacing, points[0].y + 3 * dy * pointSpacing);
  }
  
  public enum Polarity {
    North, South, Humbucking;
  }
}
