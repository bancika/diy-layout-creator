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
package org.diylc.testcomponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Mock", author = "bancika", category = "Sample", instanceNamePrefix = "M",
    description = "test", zOrder = IDIYComponent.COMPONENT)
public class MockDIYComponent extends AbstractComponent<Capacitance> {

  private static final long serialVersionUID = 1L;

  private String testField = "Hello World";
  private Capacitance c = new Capacitance(100d, CapacitanceUnit.uF);
  private Resistance r = new Resistance(123d, ResistanceUnit.K);
  private Size s = new Size(1d, SizeUnit.cm);
  private Color color = Color.green;
  private Point2D leftTopCorner = new Point(0, 0);
  private String name = "something";

  public MockDIYComponent(String testField, Capacitance c, Resistance r, Size s, Color color, Point leftTopCorner) {
    super();
    this.testField = testField;
    this.c = c;
    this.r = r;
    this.s = s;
    this.color = color;
    this.leftTopCorner = leftTopCorner;
  }

  public MockDIYComponent() {
    super();
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return false;
  }

  @EditableProperty
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int getControlPointCount() {
    return 1;
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
  public Point2D getControlPoint(int index) {
    return leftTopCorner;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    leftTopCorner.setLocation(point);
  }

  @EditableProperty(name = "Test Field")
  public String getTestField() {
    return testField;
  }

  public void setTestField(String testField) {
    this.testField = testField;
  }

  @EditableProperty(defaultable = true)
  public Capacitance getValue() {
    return c;
  }

  public void setValue(Capacitance c) {
    this.c = c;
  }

  @EditableProperty(defaultable = true)
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty
  public Resistance getR() {
    return r;
  }

  // Setter is commented out, ClassProcessor should ignore this property.
  // public void setR(Resistance r) {
  // this.r = r;
  // }

  // No annotation, so this property should be ignored.
  public Size getS() {
    return s;
  }

  public void setS(Size s) {
    this.s = s;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    g2d.setColor(componentState.equals(ComponentState.SELECTED) ? color : color.darker());
    g2d.fillRect((int)leftTopCorner.getX(), (int)leftTopCorner.getY(), 200, 50);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.drawString("X", 10, 10);
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
}
