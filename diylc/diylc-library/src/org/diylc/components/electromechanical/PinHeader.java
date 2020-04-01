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
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
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
    zOrder = IDIYComponent.COMPONENT)
public class PinHeader extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.gray;
  public static Color BORDER_COLOR = Color.gray.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color INDENT_COLOR = Color.gray.darker();
  public static Color LABEL_COLOR = Color.white;
  public static int EDGE_RADIUS = 6;
  public static Size PIN_SIZE = new Size(0.03d, SizeUnit.in);
  public static Size INDENT_SIZE = new Size(0.07d, SizeUnit.in);
  public static Size BODY_MARGIN = new Size(0.05d, SizeUnit.in);

  public static Size MINI_PIN_SPACING = new Size(0.2d, SizeUnit.in);
  public static Size MINI_ROW_SPACING = new Size(0.3d, SizeUnit.in);
  public static Size MINI_WIDTH = new Size(20.1d, SizeUnit.mm);
  public static Size MINI_HEIGHT = new Size(9.9d, SizeUnit.mm);
  public static Size MINI_GAP = new Size(0.1d, SizeUnit.in);

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;

  private Point[] controlPoints = new Point[] {new Point(0, 0)};
  private int rowCount = 2;
  private int columnCount = 5;
  private Size spacing = PIN_SPACING;
  
  transient private Area[] body;

  public PinHeader() {
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

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point getControlPoint(int index) {
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
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    int pinCount = rowCount * columnCount;
    controlPoints = new Point[pinCount];
    controlPoints[0] = firstPoint;
    double pinSpacing = spacing.convertToPixels();   
    
    for (int i = 0; i < columnCount; i++)
      for (int j = 0; j < rowCount; j++) 
        controlPoints[i + j * columnCount] = new Point((int)Math.round(firstPoint.x + j * pinSpacing), (int)Math.round(firstPoint.y + i * pinSpacing));
    
    // Apply rotation if necessary
    double angle = getAngle();
    if (angle != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(angle, controlPoints[0].x, controlPoints[0].y);
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
      int centerX = (controlPoints[0].x + controlPoints[controlPoints.length - 1].x) / 2;
      int centerY = (controlPoints[0].y + controlPoints[controlPoints.length - 1].y) / 2;
      double pinSpacing = spacing.convertToPixels();
      double width = Math.abs((controlPoints[0].x - controlPoints[controlPoints.length - 1].x)) + pinSpacing;
      double height = Math.abs((controlPoints[0].y - controlPoints[controlPoints.length - 1].y)) + pinSpacing;
      body[0] =
          new Area(new RoundRectangle2D.Double(centerX - width / 2, centerY - height / 2, width, height, EDGE_RADIUS,
              EDGE_RADIUS));      
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area mainArea = getBody()[0];
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    for (Point point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fillRect(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
      g2d.drawRect(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
    }

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
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (outlineMode) {
      Area area = new Area(mainArea);
      area.subtract(getBody()[1]);
      g2d.draw(area);
    } else {
      g2d.draw(mainArea);
      if (getBody()[1] != null) {
        g2d.setColor(INDENT_COLOR);
        g2d.fill(getBody()[1]);
      }
    }    
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(width / 3, 1, width / 3 + 1, height - 4);
    g2d.setColor(BORDER_COLOR);
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
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
