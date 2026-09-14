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
package org.diylc.testcomponents;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;

/**
 * Minimal single-point, sticky component with a settable node name, standing in for a solder pad
 * or turret when testing how {@code CompositeComponent} resolves the terminal names of a custom
 * board. A {@code null} node name marks the point as not being a netlist node, like a pad whose
 * Node Name is left blank.
 */
@ComponentDescriptor(name = "One Point Test", category = "Test", author = "Test",
    description = "test", instanceNamePrefix = "OP", zOrder = IDIYComponent.COMPONENT)
public class OnePointTestComponent extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  private Point2D point;
  private String nodeName;

  public OnePointTestComponent() {
    this("", null, new Point2D.Double(0, 0));
  }

  public OnePointTestComponent(String name, String nodeName, Point2D point) {
    setName(name);
    this.nodeName = nodeName;
    this.point = point;
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point = point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return nodeName;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
  }
}
