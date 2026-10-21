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
package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.passive.AbstractRadialComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@AiNote("'Size' is the lens diameter seen from above; 'Width' is used only when 'Shape' is RECTANGLE")
@ComponentDescriptor(name = "LED", author = "Branislav Stojkovic", category = "Semiconductors",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D", description = "Light Emitting Diode",
    zOrder = IDIYComponent.COMPONENT, transformer = SimpleComponentTransformer.class)
public class LED extends AbstractRadialComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_SIZE = new Size(5d, SizeUnit.mm);
  public static Size DEFAULT_WIDTH = new Size(2d, SizeUnit.mm);
  public static Size PIN_SPACING = new Size(2.54d, SizeUnit.mm);
  public static Color BODY_COLOR = Color.decode("#5DFC0A");
  public static Color BORDER_COLOR = BODY_COLOR.darker();

  private String value = "";
  private BodyShape shape = BodyShape.ROUND;

  public LED() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.pinSpacing = PIN_SPACING;
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public BodyShape getShape() {
    if (shape == null) {
      shape = BodyShape.ROUND;
    }
    return shape;
  }

  public void setShape(BodyShape shape) {
    this.shape = shape;
  }

  @Override
  public String getControlPointNodeName(int index) {
    switch (index) {
      case 0:
        return "A";
      case 1:
        return "K";
      default:
        return null;
    }
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);

    int margin = 4 * width / 32;
    Area area = new Area(new Ellipse2D.Double(margin, margin, width - 2 * margin, width - 2 * margin));
    area.intersect(new Area(new Rectangle2D.Double(margin, margin, width - 5 * margin / 2, width - 2 * margin)));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(area);
    g2d.drawOval(margin * 2 - 1, margin * 2 - 1, width - 4 * margin + 2, width - 4 * margin + 2);
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_SIZE;
  }

  @Override
  protected Shape getBodyShape() {
    if (getShape() == BodyShape.RECTANGLE) {
      return new Rectangle2D.Double(0, 0, getClosestOdd(getLength().convertToPixels()),
          getClosestOdd(getWidth().convertToPixels()));
    }
    int size = getClosestOdd((int) (getLength().convertToPixels() * 1.2));
    Area area = new Area(new Ellipse2D.Double(0, 0, size, size));
    area.intersect(new Area(new Rectangle2D.Double(0, 0, getLength().convertToPixels() * 1.15, size)));
    return area;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (!outlineMode && getShape() == BodyShape.ROUND) {
      int size = getClosestOdd((int) (getLength().convertToPixels() * 1.2));
      int innerSize = getClosestOdd(getLength().convertToPixels());
      int x = (size - innerSize) / 2;
      Shape s = new Ellipse2D.Double(x, x, innerSize, innerSize);
      g2d.setColor(getBorderColor());
      g2d.draw(s);
    }
  }

  // the border always follows the body color; the inherited borderColor field stays only so
  // that older files and older builds keep reading it
  @Override
  public Color getBorderColor() {
    Color bodyColor = getBodyColor();
    return bodyColor == null ? null : bodyColor.darker();
  }

  @Deprecated
  @Override
  public void setBorderColor(Color borderColor) {
    super.setBorderColor(borderColor);
  }

  @EditableProperty(name = "Size")
  @Override
  public Size getLength() {
    return super.getLength();
  }

  // only rectangular LEDs use the width; a round body is sized by its diameter alone
  @EditableProperty(name = "Width", validatorClass = PositiveNonZeroMeasureValidator.class,
      sortOrder = 102)
  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  public String getType() {
    // Override to prevent editing.
    return super.getType();
  }

  public enum BodyShape {

    ROUND("Round"), RECTANGLE("Rectangle");

    private String label;

    private BodyShape(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
