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
package org.diylc.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.LabelPosition;
import org.diylc.core.ComponentState;
import org.diylc.core.annotations.EditableProperty;

public abstract class AbstractSchematicLeadedSymbol<T> extends AbstractLeadedComponent<T> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.blue;
  public static Color LEAD_COLOR = Color.black;

  protected LabelPosition labelPosition = LabelPosition.ABOVE;

  public AbstractSchematicLeadedSymbol() {
    super();
    // We don't want to fill the body, so use null.
    this.bodyColor = null;
    this.leadColor = LEAD_COLOR;
    this.borderColor = COLOR;
  }

  @Override
  public Color getBodyColor() {
    return super.getBodyColor();
  }

  @Override
  protected boolean shouldShadeLeads() {
    return false;
  }

  @Override
  protected float getLeadThickness() {
    return 1;
  }
  
  @Override
  protected int calculateLabelYOffset(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics) {
    if (labelPosition == LabelPosition.ABOVE) {
      return -5;
    } else {
      return (int) (shapeRect.getHeight() + textRect.getHeight());
    }
  }
  
  @Override
  protected Point2D calculateLabelPosition(Point2D point1, Point2D point2) {
    double x = (point1.getX() + point2.getX()) / 2.0;
    double y = (point1.getY() + point2.getY()) / 2.0;
    double theta = Math.atan2(point2.getY() - point1.getY(), point2.getX() - point2.getX()) - Math.PI / 2;
    double r = width.convertToPixels() / 2 + LABEL_FONT.getSize() / 2;
    return new Point2D.Double(x + Math.cos(theta) * r, y + Math.sin(theta) * r);
  }

  @EditableProperty(name = "Label Position")
  public LabelPosition getLabelPosition() {
    return labelPosition;
  }

  @Override
  @EditableProperty(name = "Color")
  public Color getBorderColor() {
    return super.getBorderColor();
  }

  public void setLabelPosition(LabelPosition labelPosition) {
    this.labelPosition = labelPosition;
  }

  @Override
  protected Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : getLeadColor();
  }
}
