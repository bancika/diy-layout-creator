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
package org.diylc.components.passive;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Inductance;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Inductor (Radial)", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "L",
    description = "Vertically mounted ferrite core inductor", zOrder = IDIYComponent.COMPONENT,
    transformer = SimpleComponentTransformer.class)
public class RadialInductor extends AbstractRadialComponent<Inductance> {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_SIZE = new Size(1d / 4, SizeUnit.in);
  public static Color BODY_COLOR = Color.darkGray;
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color MARKER_COLOR = Color.decode("#8CACEA");
  public static Color TICK_COLOR = Color.white;
  public static Size HEIGHT = new Size(0.4d, SizeUnit.in);
  public static Size EDGE_RADIUS = new Size(0.5d, SizeUnit.mm);
  public static Size LIP = new Size(0.05d, SizeUnit.in);

  private Inductance value = null;
  private Resistance resistance = null;

  private boolean folded = false;
  private Size height = HEIGHT;
  private Size lip = LIP;

  public RadialInductor() {
    super();
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
    this.labelColor = TICK_COLOR;
    this.leadColor = COPPER_COLOR;
  }
 
  @Override
  public String getValueForDisplay() {
    return getValue() == null ? null : getValue().toString();
  }
  
  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    if (!getFolded())
      return;
    
    Area body = new Area(getBodyShape());
    int leadThickness = (int) getLeadThickness();
    double lip = getLip().convertToPixels();
    Stroke stroke = ObjectCache.getInstance().fetchBasicStroke(leadThickness / 2);
    Rectangle rect = body.getBounds();
    Area copper = new Area();
    for (double y = lip + leadThickness / 2; y < rect.height - lip - leadThickness; y += leadThickness * 0.9d) {
      double margin;
      if (y < leadThickness || y > rect.height - leadThickness)
        margin = -leadThickness / 4;
      else 
        margin = leadThickness / 4;
      Line2D line = new Line2D.Double(-margin + lip, y - rect.height / 2, rect.width - lip + margin, y - rect.height / 2);
      Shape s = stroke.createStrokedShape(line);
      copper.add(new Area(s));
    }
//    copper.intersect(body);
    g2d.setColor(COPPER_COLOR);
    g2d.fill(copper);
    g2d.setColor(COPPER_COLOR.darker());
    g2d.draw(copper);    
  }
  
  @Override
  protected boolean decorateAboveBorder() {
    return true;
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(COPPER_COLOR);
    g2d.drawLine(0, height * 2 / 5, width / 2, height * 2 / 5);
    g2d.drawLine(0, height * 3 / 5, width / 2, height * 3 / 5);
    g2d.rotate(Math.PI / 2, width / 2, height / 2);
    g2d.setColor(BODY_COLOR);
    Rectangle2D a = new Rectangle2D.Double(width / 2 - 5, 6, 10, height - 14);
    g2d.fill(a);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(a);
    g2d.setColor(COPPER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    for (int i = 0; i < 5; i++) {
      g2d.drawLine(width / 2 - 5, 11 + 2 * i, width / 2 + 5, 11 + 2 * i);
    }
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  protected Size getDefaultLength() {
    // We'll reuse width property to set the diameter.
    return DEFAULT_SIZE;
  }

  @EditableProperty(name = "Diameter")
  @Override
  public Size getLength() {
    return super.getLength();
  }

  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
  }

  @EditableProperty
  public Size getHeight() {
    if (height == null) {
      height = HEIGHT;
    }
    return height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  @Override
  protected Shape getBodyShape() {
    double height = (int) getHeight().convertToPixels();
    double diameter = (int) getLength().convertToPixels();
    double lip = getLip().convertToPixels();
    if (folded) {
      Area body = new Area(new RoundRectangle2D.Double(0f, -height / 2 - LEAD_THICKNESS.convertToPixels() / 2,
          getClosestOdd(diameter), getClosestOdd(height), EDGE_RADIUS.convertToPixels(), EDGE_RADIUS.convertToPixels()));
      body.subtract(new Area(new Rectangle2D.Double(0f, -height / 2 + lip - LEAD_THICKNESS.convertToPixels() / 2,
          lip, height - 2 * lip)));
      body.subtract(new Area(new Rectangle2D.Double(diameter - lip, -height / 2 + lip - LEAD_THICKNESS.convertToPixels() / 2,
          lip * 2, height - 2 * lip)));
      return body;
    }
    return new Ellipse2D.Double(0f, 0f, getClosestOdd(diameter), getClosestOdd(diameter));
  }

  @EditableProperty
  @Override
  public Inductance getValue() {
    return value;
  }

  @Override
  public void setValue(Inductance value) {
    this.value = value;
  }
  
  @EditableProperty
  public Resistance getResistance() {
    return resistance;
  }
  
  public void setResistance(Resistance resistance) {
    this.resistance = resistance;
  }
  
  @EditableProperty(name = "Rim")
  public Size getLip() {
    return lip;
  }
  
  public void setLip(Size lip) {
    this.lip = lip;
  }

  @Override
  public boolean isPolarized() {
    return false;
  }
}
