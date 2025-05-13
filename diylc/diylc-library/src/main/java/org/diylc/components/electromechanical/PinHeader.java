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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.PinHeaderTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Pin Header", author = "Branislav Stojkovic", category = "Electro-Mechanical",
    instanceNamePrefix = "PH", description = "PCB mount male pin header with editable number or pins and pin spacing",
    zOrder = IDIYComponent.COMPONENT, enableCache = true, transformer = PinHeaderTransformer.class)
public class PinHeader extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.gray;
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color INDENT_COLOR = Color.gray.darker();
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
  public static Size INDENT_SIZE = new Size(0.1d, SizeUnit.in); 
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);

  private Orientation orientation = Orientation.DEFAULT;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private int rowCount = 2;
  private int columnCount = 5;
  private Size spacing = PIN_SPACING;
  private Boolean shrouded = false;
  
  private Color bodyColor = BODY_COLOR;
  private Color shroudColor = BODY_COLOR.darker();
  
  transient private Area[] body;

  public PinHeader() {
    super();
    updateControlPoints();
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
    int pinCount = rowCount * columnCount;
    controlPoints = new Point2D[pinCount];
    controlPoints[0] = firstPoint;
    double pinSpacing = spacing.convertToPixels();   
    
    for (int i = 0; i < columnCount; i++)
      for (int j = 0; j < rowCount; j++) 
        controlPoints[i + j * columnCount] = new Point2D.Double(Math.round(firstPoint.getX() + j * pinSpacing), 
            Math.round(firstPoint.getY() + i * pinSpacing));
    
    // Apply rotation if necessary
    double angle = getAngle();
    if (angle != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(angle, controlPoints[0].getX(), controlPoints[0].getY());
      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }
  
  private double getAngle() {
    // Apply rotation if necessary
    double angle;
    switch (orientation) {
      case _90:
        angle = Math.PI / 2;
        break;
      case _180:
        angle = Math.PI;
        break;
      case _270:
        angle = Math.PI * 3 / 2;
        break;
      default:
        angle = 0;
    }

    return angle;
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double centerX = (controlPoints[0].getX() + controlPoints[controlPoints.length - 1].getX()) / 2;
      double centerY = (controlPoints[0].getY() + controlPoints[controlPoints.length - 1].getY()) / 2;
      double pinSpacing = spacing.convertToPixels();
      double width = Math.abs((controlPoints[0].getX() - controlPoints[controlPoints.length - 1].getX())) + pinSpacing;
      double height = Math.abs((controlPoints[0].getY() - controlPoints[controlPoints.length - 1].getY())) + pinSpacing;
      if (shrouded) {
        if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
          height += 2 * pinSpacing;
          width += 0.5 * pinSpacing;          
        } else {
          height += 0.5 * pinSpacing;
          width += 2 * pinSpacing;
        }
        double shroudWidth = width + 0.5 * pinSpacing;
        double shroudHeight = height + 0.5 * pinSpacing;
        double indentSize = INDENT_SIZE.convertToPixels();
        
        body[1] =
            new Area(new Rectangle2D.Double(centerX - shroudWidth / 2, centerY - shroudHeight / 2, shroudWidth, shroudHeight));
        Shape indent = null;
        switch (orientation) {
          case DEFAULT:
            indent = new Rectangle2D.Double(centerX - shroudWidth, centerY - indentSize / 2, shroudWidth, indentSize);
            break;
          case _180:
            indent = new Rectangle2D.Double(centerX, centerY - indentSize / 2, shroudWidth, indentSize);            
            break;
          case _270:
            indent = new Rectangle2D.Double(centerX - indentSize / 2, centerY, indentSize, shroudHeight);            
            break;
          case _90:
            indent = new Rectangle2D.Double(centerX - indentSize / 2, centerY - shroudHeight, indentSize, shroudHeight);
            break;
          default:
            break;          
        }
        if (indent != null)
          body[1].subtract(new Area(indent));
      }
      body[0] =
          new Area(new Rectangle2D.Double(centerX - width / 2, centerY - height / 2, width, height));
      
      if (body[1] != null)
        body[1].subtract(body[0]);
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area[] body = getBody();
    Area mainArea = body[0];
    Area shroudArea = body[1];
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);
    if (shroudArea != null) {
      g2d.setColor(shroudColor);
      g2d.fill(shroudArea);
    }
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    for (Point2D point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fillRect((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
      g2d.drawRect((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
    }

    Color finalBorderColor;
    if (outlineMode) {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : bodyColor.darker();
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);
    if (shroudArea != null) {
      g2d.setColor(shroudColor.darker());
      g2d.draw(shroudArea);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(width / 3, 1, width / 3 + 1, height - 4);
    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRect(width / 3, 1, width / 3 + 1, height - 4);
    int pinSize = 2 * width / 32;
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {      
      g2d.fillRect(width / 2 - pinSize - 1, (height / 5) * (i + 1) - 1, pinSize, pinSize);
      g2d.fillRect(width / 2 + pinSize, (height / 5) * (i + 1) - 1, pinSize, pinSize);
    }
  }
  
  @EditableProperty(name = "Rows")
  public int getRowCount() {
    return rowCount;
  }
  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }
  
  @EditableProperty(name = "Columns")
  public int getColumnCount() {
    return columnCount;
  }
  
  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }
  
  @EditableProperty
  public Size getSpacing() {
    return spacing;
  }
  
  public void setSpacing(Size spacing) {
    this.spacing = spacing;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }
  
  @EditableProperty
  public Boolean getShrouded() {
    return shrouded;
  }
  
  public void setShrouded(Boolean shrouded) {
    this.shrouded = shrouded;
    // Reset body shape.
    body = null;
  }
  
  @EditableProperty(name = "Color")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Shroud")
  public Color getShroudColor() {
    return shroudColor;
  }

  public void setShroudColor(Color shroudColor) {
    this.shroudColor = shroudColor;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @Deprecated
  public Void getValue() {
    return null;
  }

  @Deprecated
  public void setValue(Void value) {
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
    
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }
}
