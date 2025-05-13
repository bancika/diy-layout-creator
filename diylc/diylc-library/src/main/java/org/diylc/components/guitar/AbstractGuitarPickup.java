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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractGuitarPickup extends AbstractLabeledComponent<String> {

  private static final long serialVersionUID = 1L;
  
  protected static Size POINT_SPACING = new Size(0.1d, SizeUnit.in);
  
  protected static final int TERMINAL_FONT_SIZE = 11;  
  
  protected String value = "";
  protected Orientation orientation = Orientation.DEFAULT;
  protected transient Shape[] body;  
  
  protected Point2D controlPoint = new Point2D.Double(0, 0);
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  
  protected Polarity polarity = Polarity.North;  
  
  protected Color labelColor;
  
  public AbstractGuitarPickup() {
    updateControlPoints();
  }
  
  protected void drawTerminalLabels(Graphics2D g2d, Color color, Project project) {
    Point2D[] points = getControlPoints();    
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

    StringUtils.drawCenteredText(g2d, "N", (points[0].getX() + points[1].getX()) / 2 + dx, (points[0].getY() + points[1].getY()) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "S", (points[2].getX() + points[3].getX()) / 2 + dx, (points[2].getY() + points[3].getY()) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);   
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
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Rectangle bounds = getBody()[0].getBounds();
    
    AffineTransform originalTx = g2d.getTransform();
    g2d.translate(bounds.getX() + bounds.width / 2, bounds.getY() + bounds.height / 2);
    if (orientation == Orientation._90)
      g2d.rotate(Math.PI / 2);
    else if (orientation == Orientation._270){
      g2d.rotate(-Math.PI / 2);
    }
    g2d.translate(0, getMainLabelYOffset());
    
    StringUtils.drawCenteredText(g2d, getName(), 0, 0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
   
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
  
  protected Point2D[] getControlPoints() {
    if (controlPoints == null) {
      controlPoints =
          new Point2D[] {controlPoint, new Point2D.Double(controlPoint.getX(), controlPoint.getY()),
              new Point2D.Double(controlPoint.getX(), controlPoint.getY()), new Point2D.Double(controlPoint.getX(), controlPoint.getY())};
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
  public Point2D getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    getControlPoints()[index].setLocation(point);
    // Invalidate the body
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @EditableProperty(name = "Coil(s)")
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
    Point2D[] points = getControlPoints();
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
    points[1].setLocation(points[0].getX() + dx * pointSpacing, points[0].getY() + dy * pointSpacing);
    points[2]
        .setLocation(points[0].getX() + 2 * dx * pointSpacing, points[0].getY() + 2 * dy * pointSpacing);
    points[3]
        .setLocation(points[0].getX() + 3 * dx * pointSpacing, points[0].getY() + 3 * dy * pointSpacing);
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] body = getBody();
    int margin = 20;
    double minX = 0;
    double minY = 0;
    double maxX = 0;
    double maxY = 0;
    for (Shape a : body) {
      if (a != null) {
        Rectangle2D bounds2d = a.getBounds2D();
        if (bounds2d.getMinX() < minX)
          minX = bounds2d.getMinX();
        if (bounds2d.getMinY() < minY)
          minY = bounds2d.getMinY();
        if (bounds2d.getMaxX() > maxX)
          maxX = bounds2d.getMaxX();
        if (bounds2d.getMaxY() > maxY)
          maxY = bounds2d.getMaxY();
      }
    }
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }
  
  public enum Polarity {
    North("Single - North"), South("Single - South"), Humbucking("Humbucking - 4 leads");
    
    private String label;

    private Polarity(String label) {
      this.label = label;
    }
    
    @Override
    public String toString() {
      return label;
    }
  }
}
