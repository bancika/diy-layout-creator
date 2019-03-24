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
package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractBoard extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BOARD_COLOR = Color.decode("#F8EBB3");
  public static Color BORDER_COLOR = BOARD_COLOR.darker();
  public static Color COORDINATE_COLOR = Color.gray.brighter();
  public static float COORDINATE_FONT_SIZE = 9f;
  public static Size DEFAULT_WIDTH = new Size(1.5d, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(1.2d, SizeUnit.in);

  protected String value = "";
  protected Point[] controlPoints = new Point[] {new Point(0, 0),
      new Point((int) DEFAULT_WIDTH.convertToPixels(), (int) DEFAULT_HEIGHT.convertToPixels())};
  protected Point firstPoint = new Point();
  protected Point secondPoint = new Point();

  protected Color boardColor = BOARD_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Color coordinateColor = COORDINATE_COLOR;
  protected Boolean drawCoordinates = null;
  protected CoordinateType xType = CoordinateType.Numbers;
  protected CoordinateOrigin coordinateOrigin = CoordinateOrigin.Top_Left;
  protected CoordinateDisplay coordinateDisplay = CoordinateDisplay.One_Side;
  protected CoordinateType yType = CoordinateType.Letters;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Composite oldComposite = g2d.getComposite();
    // render as transparent when dragging
    int alpha = componentState == ComponentState.DRAGGING ? 0 : this.alpha;
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(boardColor);
    g2d.fillRect(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
    g2d.setComposite(oldComposite);
    
    // Do not track any changes that follow because the whole board has been
    // tracked so far.
    drawingObserver.stopTracking();
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : borderColor);
    g2d.drawRect(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
  }

  protected void drawCoordinates(Graphics2D g2d, int spacing, Project project) {
    g2d.setColor(coordinateColor);
    g2d.setFont(project.getFont().deriveFont(COORDINATE_FONT_SIZE));
    
    // The half space is used to do rounding when calculating the range.
    int halfSpace = spacing / 2;
    CoordinateOrigin origin = getCoordinateOrigin();
    
    if (getCoordinateDisplay() != CoordinateDisplay.None) {
      int range;
      int yOffset;
      CoordinateType yType = getyType();
      Point drawPoint = new Point(firstPoint);
      
      if (origin == CoordinateOrigin.Top_Left || origin == CoordinateOrigin.Top_Right) {
    	range = (int) ((secondPoint.y - firstPoint.y + halfSpace) / spacing);
    	yOffset = spacing;
      } else {
    	range = (int) ((secondPoint.y - firstPoint.y + halfSpace) / spacing);
    	yOffset = -spacing;
    	drawPoint.y = secondPoint.y;
      }
      
      for (int c = 1; c < range; c++) {
    	int xOffset = (yType == CoordinateType.Numbers && c >= 10) || (yType == CoordinateType.Letters && c >= 27) ? 0 : 2;
    	String label = yType == CoordinateType.Letters ? getCoordinateLabel(c) : Integer.toString(c);
    	
    	drawPoint.y += yOffset;
    	StringUtils.drawCenteredText(g2d, label, 
    		firstPoint.x + xOffset, drawPoint.y, 
    		HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    	if (getCoordinateDisplay() == CoordinateDisplay.Both_Sides) {
    	  StringUtils.drawCenteredText(g2d, label, 
    		  secondPoint.x - xOffset, drawPoint.y, 
    		  HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    	}
      }
    }

    if (getCoordinateDisplay() != CoordinateDisplay.None) {
      int range;
      int xOffset;
      CoordinateType xType = getxType();
      Point drawPoint = new Point(firstPoint);
      
      if (origin == CoordinateOrigin.Top_Left || origin == CoordinateOrigin.Bottom_Left) {
        range = (int) ((secondPoint.x - firstPoint.x + halfSpace) / spacing);
        xOffset = spacing;
      } else {
        range = (int) ((secondPoint.x - firstPoint.x + halfSpace) / spacing);
        xOffset = -spacing;
        drawPoint.x = secondPoint.x;
      }
      
      for (int c = 1; c < range; c++) {
        String label = xType == CoordinateType.Letters ? getCoordinateLabel(c) : Integer.toString(c);
        
        drawPoint.x += xOffset;
        StringUtils.drawCenteredText(g2d, label, 
            drawPoint.x, firstPoint.y - 2, 
            HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        if (getCoordinateDisplay() == CoordinateDisplay.Both_Sides) {
          StringUtils.drawCenteredText(g2d, label, 
              drawPoint.x, (int) (secondPoint.y - COORDINATE_FONT_SIZE), 
              HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
        }
      }
    }
  }

  private String getCoordinateLabel(int coordinate) {
    String result = "";
    while (coordinate > 0) {
      int digit = coordinate % 26;
      coordinate /= 26;
      if (digit == 0) {
        result = 'Z' + result;
        coordinate--;
      } else {
        result = (char) ((int) 'A' + digit - 1) + result;
      }
    }
    return result;
  }

  @EditableProperty(name = "Color")
  public Color getBoardColor() {
    return boardColor;
  }

  public void setBoardColor(Color boardColor) {
    this.boardColor = boardColor;
  }

  @EditableProperty(name = "Coordinate Color")
  public Color getCoordinateColor() {
    // Null protection for older files
    return coordinateColor == null ? COORDINATE_COLOR : coordinateColor;
  }

  public void setCoordinateColor(Color coordinateColor) {
    this.coordinateColor = coordinateColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
  
  @EditableProperty(name = "X")
  public CoordinateType getxType() {
    if (xType == null)
      xType = CoordinateType.Numbers;
    return xType;
  }

  public void setxType(CoordinateType xType) {
    this.xType = xType;
  }

  @EditableProperty(name = "Coordinates")
  public CoordinateDisplay getCoordinateDisplay() {
    if (coordinateDisplay == null)
      coordinateDisplay = CoordinateDisplay.One_Side;
    return coordinateDisplay;
  }

  public void setCoordinateDisplay(CoordinateDisplay coordinateDisplay) {
    this.coordinateDisplay = coordinateDisplay;
  }

  @EditableProperty(name = "Coordinate Origin")
  public CoordinateOrigin getCoordinateOrigin() {
	  if (coordinateOrigin == null)
		  coordinateOrigin = CoordinateOrigin.Top_Left;
	  return coordinateOrigin;
  }
  
  public void setCoordinateOrigin(CoordinateOrigin coordinateOrigin) {
	  this.coordinateOrigin = coordinateOrigin;
  }
  
  @EditableProperty(name = "Y")
  public CoordinateType getyType() {
    if (yType == null)
      yType = CoordinateType.Letters;
    return yType;
  }

  public void setyType(CoordinateType yType) {
    this.yType = yType;
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
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    firstPoint.setLocation(Math.min(controlPoints[0].x, controlPoints[1].x),
        Math.min(controlPoints[0].y, controlPoints[1].y));
    secondPoint.setLocation(Math.max(controlPoints[0].x, controlPoints[1].x),
        Math.max(controlPoints[0].y, controlPoints[1].y));
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
  
  public static enum CoordinateType {
    Letters, Numbers
  }
  
  public static enum CoordinateDisplay {
    None, One_Side, Both_Sides;
    
    @Override
    public String toString() {
      return super.toString().replace('_', ' ');
    };
  }
  
  public static enum CoordinateOrigin {
    Top_Left, Top_Right, Bottom_Right, Bottom_Left;

    @Override
    public String toString() {
      return super.toString().replace('_', ' ');
    };
  }
}
