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
package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import org.diylc.components.AbstractSchematicLeadedSymbol;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractDiodeSymbol extends AbstractSchematicLeadedSymbol<String> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_SIZE = new Size(0.1, SizeUnit.in);

  private String value = "";

  public AbstractDiodeSymbol() {
    super();
    this.bodyColor = COLOR;
    this.borderColor = null;
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getValueForDisplay() {
    return getValue();
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_SIZE;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_SIZE;
  }

  @Override
  protected Shape getBodyShape() {
    double width = getWidth().convertToPixels();
    Polygon p =
        new Polygon(new int[] {0, 0, (int) (width / Math.sqrt(2))}, new int[] {0, (int) (width), (int) (width / 2)}, 3);
    // Area a = new Area(p);
    // int bandSize = (int) BAND_SIZE.convertToPixels();
    // a.add(new Area(new Rectangle2D.Double((int) (width / Math.sqrt(2)) +
    // 1,
    // 0, bandSize, (int) width)));
    return p;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {}

  @Deprecated
  @Override
  public Size getLength() {
    return super.getLength();
  }

  @Deprecated
  @Override
  public Color getBorderColor() {
    return super.getBorderColor();
  }

  @Override
  @EditableProperty(name = "Color")
  public Color getBodyColor() {
    return super.getBodyColor();
  }
}
