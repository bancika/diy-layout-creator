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
 * Minimal two-point component used to exercise {@code CompositeComponent} and
 * {@code BuildingBlockManager} in tests: two control points, individually configurable
 * stickiness, node naming left at the {@link AbstractComponent} default (the 1-based point
 * index), so a composite's per-child node name delegation can be verified precisely.
 */
@ComponentDescriptor(name = "Two Point Test", category = "Test", author = "Test",
    description = "test", instanceNamePrefix = "TP", zOrder = IDIYComponent.COMPONENT)
public class TwoPointTestComponent extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  private Point2D p0;
  private Point2D p1;
  private boolean sticky0 = true;
  private boolean sticky1 = true;

  public TwoPointTestComponent() {
    this("", new Point2D.Double(0, 0), new Point2D.Double(10, 0));
  }

  public TwoPointTestComponent(String name, Point2D p0, Point2D p1) {
    setName(name);
    this.p0 = p0;
    this.p1 = p1;
  }

  public void setSticky(int index, boolean sticky) {
    if (index == 0)
      sticky0 = sticky;
    else
      sticky1 = sticky;
  }

  @Override
  public int getControlPointCount() {
    return 2;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return index == 0 ? p0 : p1;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    if (index == 0)
      p0 = point;
    else
      p1 = point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index == 0 ? sticky0 : sticky1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
  }
}
