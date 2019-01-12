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
package org.diylc.components.shapes;

import java.awt.Color;
import java.awt.Point;

import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractShape extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.white;
  public static Color BORDER_COLOR = Color.black;
  public static Size DEFAULT_WIDTH = new Size(0.6d, SizeUnit.in);
  public static Size DEFAULT_HEIGHT = new Size(0.4d, SizeUnit.in);

  protected String value = "";
  protected Point[] controlPoints = new Point[] {new Point(0, 0),
      new Point((int) DEFAULT_WIDTH.convertToPixels(), (int) DEFAULT_HEIGHT.convertToPixels())};
  protected Point firstPoint = new Point();
  protected Point secondPoint = new Point();

  protected Color color = COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Size borderThickness = new Size(0.2d, SizeUnit.mm);

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Border thickness", validatorClass = PositiveMeasureValidator.class)
  public Size getBorderThickness() {
    return borderThickness;
  }

  public void setBorderThickness(Size borderThickness) {
    this.borderThickness = borderThickness;
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

  @Deprecated
  @Override
  public Void getValue() {
    return null;
  }

  @Deprecated
  @Override
  public void setValue(Void value) {}
}
