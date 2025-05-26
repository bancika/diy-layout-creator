/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.components.shapes;

import java.awt.geom.Point2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.components.boards.ShapeModeValidator;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractShapeWithDimensions extends AbstractShape {

  private static final long serialVersionUID = 1L;

  protected Size length;
  protected Size width;
  protected ShapeSizingMode mode = ShapeSizingMode.TwoPoints;

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return getMode() == ShapeSizingMode.TwoPoints ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
  }
  
  protected Point2D getFinalSecondPoint() {
    Point2D finalSecondPoint;
    if (getMode() == ShapeSizingMode.TwoPoints)
      finalSecondPoint = secondPoint;
    else
      finalSecondPoint = new Point2D.Double(firstPoint.getX() + getLength().convertToPixels(), 
         firstPoint.getY() + getWidth().convertToPixels());
    return finalSecondPoint;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    firstPoint.setLocation(Math.min(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.min(controlPoints[0].getY(), controlPoints[1].getY()));
    secondPoint.setLocation(Math.max(controlPoints[0].getX(), controlPoints[1].getX()),
        Math.max(controlPoints[0].getY(), controlPoints[1].getY()));
  }
  
  
  @EditableProperty(name = "Dimension Mode")
  public ShapeSizingMode getMode() {
    if (mode == null)
      mode = ShapeSizingMode.TwoPoints;
    return mode;
  }

  public void setMode(ShapeSizingMode mode) {
    this.mode = mode;
  }
  
  @EditableProperty(name = "Explicit Length", validatorClass = ShapeModeValidator.class)
  public Size getLength() {
    if (getMode() == ShapeSizingMode.TwoPoints) {
      double lengthPx = Math.abs(this.firstPoint.getX() - this.secondPoint.getX());
      this.length = Size.fromPixels(lengthPx, length == null ? getDefaultUnit() : length.getUnit());
    }
    return this.length;
  }

  public void setLength(Size length) {
    if (getMode() == ShapeSizingMode.Explicit && length != null && width != null) {
      setControlPoint(firstPoint, 0);      
      Point2D second = new Point2D.Double(firstPoint.getX() + length.convertToPixels(),
          firstPoint.getY() + width.convertToPixels());
      setControlPoint(second, 1);    
    }
    this.length = length;    
  }

  @EditableProperty(name = "Explicit Width", validatorClass = ShapeModeValidator.class)
  public Size getWidth() {
    if (getMode() == ShapeSizingMode.TwoPoints) {
      double widthPx = Math.abs(this.firstPoint.getY() - this.secondPoint.getY());
      this.width = Size.fromPixels(widthPx, width == null ? getDefaultUnit() : width.getUnit());
    }
    return this.width;
  }

  public void setWidth(Size width) {
    if (getMode() == ShapeSizingMode.Explicit && length != null && width != null) {
      setControlPoint(firstPoint, 0);
      Point2D second = new Point2D.Double(firstPoint.getX() + length.convertToPixels(), firstPoint.getY() + width.convertToPixels());
      setControlPoint(second, 1);
    }
    this.width = width;
  }
  
  private SizeUnit getDefaultUnit() {
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.METRIC_KEY, true))
      return SizeUnit.mm;
    return SizeUnit.in;      
  }
  
  public static enum ShapeSizingMode {
    TwoPoints("Opposing Points"), Explicit("Explicit Dimensions");
    
    private String label;

    private ShapeSizingMode(String label) {
      this.label = label;
    }
    
    @Override
    public String toString() {
      return label;
    }
  }  
}
