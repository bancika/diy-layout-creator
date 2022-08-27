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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
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
  // two control points used for controlling the position
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0),
      new Point2D.Double(DEFAULT_WIDTH.convertToPixels(), DEFAULT_HEIGHT.convertToPixels())};
  // top-left and bottom-right points
  protected Point2D.Double firstPoint = new Point2D.Double();
  protected Point2D.Double secondPoint = new Point2D.Double();

  protected Color boardColor = BOARD_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Color coordinateColor = COORDINATE_COLOR;
  protected Boolean drawCoordinates = null;
  protected CoordinateType xType = CoordinateType.Numbers;
  protected CoordinateOrigin coordinateOrigin = CoordinateOrigin.Top_Left;
  protected CoordinateDisplay coordinateDisplay = CoordinateDisplay.One_Side;
  protected CoordinateType yType = CoordinateType.Letters;
  
  protected Size length;
  protected Size width;
  protected BoardSizingMode mode = BoardSizingMode.TwoPoints;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    
    Point2D finalSecondPoint = getFinalSecondPoint();    

    Composite oldComposite = g2d.getComposite();
    // render as transparent when dragging
    int alpha = componentState == ComponentState.DRAGGING ? 0 : this.alpha;
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(boardColor);
    g2d.fillRect((int)firstPoint.getX(), (int)firstPoint.getY(), (int)(finalSecondPoint.getX() - firstPoint.getX()), (int)(finalSecondPoint.getY() - firstPoint.getY()));
    g2d.setComposite(oldComposite);
    
    // Do not track any changes that follow because the whole board has been
    // tracked so far.
    drawingObserver.stopTracking();
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : borderColor);
    g2d.drawRect((int)firstPoint.getX(), (int)firstPoint.getY(), (int)(finalSecondPoint.getX() - firstPoint.getX()), (int)(finalSecondPoint.getY() - firstPoint.getY()));
  }
  
  protected Point2D getFinalSecondPoint() {
    Point2D finalSecondPoint;
    if (getMode() == BoardSizingMode.TwoPoints)
      finalSecondPoint = secondPoint;
    else
      finalSecondPoint = new Point2D.Double(firstPoint.getX() + getLength().convertToPixels(), 
         firstPoint.getY() + getWidth().convertToPixels());
    return finalSecondPoint;
  }

  protected void drawCoordinates(Graphics2D g2d, double spacing, Project project) {
    g2d.setColor(coordinateColor);
    g2d.setFont(project.getFont().deriveFont(COORDINATE_FONT_SIZE));
    
    // The half space is used to do rounding when calculating the range.
    double halfSpace = spacing / 2;
    CoordinateOrigin origin = getCoordinateOrigin();
    
    Point2D finalSecondPoint = getFinalSecondPoint();
    
    if (getCoordinateDisplay() != CoordinateDisplay.None) {
      int range;
      double yOffset;
      CoordinateType yType = getyType();
      double y = firstPoint.getY();
            
      if (origin == CoordinateOrigin.Top_Left || origin == CoordinateOrigin.Top_Right) {
    	range = (int) ((finalSecondPoint.getY() - firstPoint.getY() + halfSpace) / spacing);
    	yOffset = spacing;
      } else {
    	range = (int) ((finalSecondPoint.getY() - firstPoint.getY() + halfSpace) / spacing);
    	yOffset = -spacing;
    	y = finalSecondPoint.getY();
      }
      
      for (int c = 1; c < range; c++) {
    	int xOffset = (yType == CoordinateType.Numbers && c >= 10) || (yType == CoordinateType.Letters && c >= 27) ? 0 : 2;
    	String label = yType == CoordinateType.Letters ? getCoordinateLabel(c) : Integer.toString(c);
    	
    	y += yOffset;
    	
    	StringUtils.drawCenteredText(g2d, label, 
    		firstPoint.getX() + xOffset, y, 
    		HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    	if (getCoordinateDisplay() == CoordinateDisplay.Both_Sides) {
    	  StringUtils.drawCenteredText(g2d, label, 
    	      finalSecondPoint.getX() - xOffset, y, 
    		  HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    	}
      }
    }

    if (getCoordinateDisplay() != CoordinateDisplay.None) {
      int range;
      double xOffset;
      CoordinateType xType = getxType();
      double x = firstPoint.getX();   
      
      if (origin == CoordinateOrigin.Top_Left || origin == CoordinateOrigin.Bottom_Left) {
        range = (int) ((finalSecondPoint.getX() - firstPoint.getX() + halfSpace) / spacing);
        xOffset = spacing;
      } else {
        range = (int) ((finalSecondPoint.getX() - firstPoint.getX() + halfSpace) / spacing);
        xOffset = -spacing;
        x = finalSecondPoint.getX();
      }
      
      for (int c = 1; c < range; c++) {
        String label = xType == CoordinateType.Letters ? getCoordinateLabel(c) : Integer.toString(c);
        
        x += xOffset;
        
        StringUtils.drawCenteredText(g2d, label, 
            x, firstPoint.getY() + 2, 
            HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);
        if (getCoordinateDisplay() == CoordinateDisplay.Both_Sides) {
          StringUtils.drawCenteredText(g2d, label, 
              x, (int) (finalSecondPoint.getY() - COORDINATE_FONT_SIZE), 
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
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return getMode() == BoardSizingMode.TwoPoints ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    firstPoint.setLocation(Math.min(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.min(controlPoints[0].getY(), controlPoints[1].getY()));
    secondPoint.setLocation(Math.max(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.max(controlPoints[0].getY(), controlPoints[1].getY()));
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
  
  private SizeUnit getDefaultUnit() {
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.METRIC_KEY, true))
      return SizeUnit.mm;
    return SizeUnit.in;      
  }
    
  @EditableProperty(name = "Explicit Length", validatorClass = BoardModeValidator.class)
  public Size getLength() {
    if (getMode() == BoardSizingMode.TwoPoints) {
      double lengthPx = Math.abs(this.firstPoint.getX() - this.secondPoint.getX());
      this.length = Size.fromPixels(lengthPx, length == null ? getDefaultUnit() : length.getUnit());
    }
    return this.length;
  }

  public void setLength(Size length) {
    if (getMode() == BoardSizingMode.Explicit) {
      setControlPoint(firstPoint, 0);
      Point2D second = new Point2D.Double(firstPoint.getX() + length.convertToPixels(), firstPoint.getY() + width.convertToPixels());
      setControlPoint(second, 1);
    }
    this.length = length;    
  }

  @EditableProperty(name = "Explicit Width", validatorClass = BoardModeValidator.class)
  public Size getWidth() {
    if (getMode() == BoardSizingMode.TwoPoints) {
      double widthPx = Math.abs(this.firstPoint.getY() - this.secondPoint.getY());
      this.width = Size.fromPixels(widthPx, width == null ? getDefaultUnit() : width.getUnit());
    }
    return this.width;
  }

  public void setWidth(Size width) {
    if (getMode() == BoardSizingMode.Explicit) {
      setControlPoint(firstPoint, 0);
      Point2D second = new Point2D.Double(firstPoint.getX() + length.convertToPixels(), firstPoint.getY() + width.convertToPixels());
      setControlPoint(second, 1);
    }
    this.width = width;
  }
  
  @EditableProperty(name = "Dimension Mode")
  public BoardSizingMode getMode() {
    if (mode == null)
      mode = BoardSizingMode.TwoPoints;
    return mode;
  }

  public void setMode(BoardSizingMode mode) {
    this.mode = mode;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Point2D finalSecondPoint = getFinalSecondPoint();    
    return new Rectangle2D.Double(Math.min(firstPoint.getX(), finalSecondPoint.getX()) - 2, Math.min(firstPoint.getY(), finalSecondPoint.getY()) - 2, 
        Math.abs(finalSecondPoint.getX() - firstPoint.getX()) + 4, Math.abs(finalSecondPoint.getY() - firstPoint.getY()) + 4);
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
  
  public static enum BoardSizingMode {
    TwoPoints("Opposing Points"), Explicit("Explicit Dimensions");
    
    private String label;

    private BoardSizingMode(String label) {
      this.label = label;
    }
    
    @Override
    public String toString() {
      return label;
    }
  }  
}
