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
package org.diylc.components.passive;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Current;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Fuse", author = "Branislav Stojkovic", category = "Schematic Symbols",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "F",
    description = "Fuse schematic symbol", zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE, keywordTag = "Schematic", transformer = SimpleComponentTransformer.class)
public class FuseSymbol extends AbstractSchematicLeadedSymbol<Current> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_LENGTH = new Size(0.3, SizeUnit.in);
  public static Size DEFAULT_WIDTH = new Size(0.12, SizeUnit.in);

  private Current value = null;

  @EditableProperty
  public Current getValue() {
    return value;
  }

  public void setValue(Current value) {
    this.value = value;
  }

  @Override
  public String getValueForDisplay() {
    return getValue().toString();// + (getPowerNew() == null ? "" : " " + getPowerNew().toString());
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR);
    g2d.drawLine(0, height / 2, 4, height / 2);
    g2d.drawLine(width - 4, height / 2, width, height / 2);
    int d = 3;
    g2d.setColor(COLOR);
    g2d.drawOval(4, height / 2 - 1, d, d);
    g2d.drawOval(width - 4 - d, height / 2 - 1, d, d);
    Path2D path = new Path2D.Double();
    path.moveTo(4 + d, height / 2);
    double w = 5;   
    path.curveTo(4 + 2 * d, height / 2 + w, width / 2 - d, height / 2 + w, width / 2, height / 2);
    path.curveTo(width / 2 + d, height / 2 - w, width - 4 - 2 * d, height / 2 - w, width - 4 - d, height / 2);
    g2d.draw(path);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_LENGTH;
  }

  @Override
  protected Shape getBodyShape() {
    Path2D polyline = new Path2D.Double();
    double length = getLength().convertToPixels();
    double width = getWidth().convertToPixels();
    double radius = width / 6;
    polyline.moveTo(2 * radius, width / 2);
    polyline.curveTo(3 * radius, width, length / 2 - radius, width, length / 2, width / 2);
    polyline.curveTo(length / 2 + radius, 0, length - 3 * radius, 0, length - 2 * radius, width / 2);
    polyline.append(new Ellipse2D.Double(0, width / 2 - radius, radius * 2, radius * 2), false);
    polyline.append(new Ellipse2D.Double(length - radius * 2, width / 2 - radius, radius * 2, radius * 2), false);
    return polyline;
  }
  
  @Override
  protected boolean useShapeRectAsPosition() {
    return false;
  }
}
