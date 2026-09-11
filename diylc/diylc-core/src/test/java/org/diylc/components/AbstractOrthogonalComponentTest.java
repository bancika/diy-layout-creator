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
package org.diylc.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.diylc.common.OrientationHV;
import org.diylc.components.AbstractOrthogonalComponent.PointCount;
import org.diylc.components.transform.OrthogonalComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;

public class AbstractOrthogonalComponentTest {

  private static final double DELTA = 1e-6;

  // --- routing ---

  @Test
  public void buildVertices_bendOnEndRow_producesHorizontalVerticalHorizontal() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 50);
    assertRoute(c, 0, 0, 60, 0, 60, 50, 100, 50);
  }

  @Test
  public void buildVertices_bendOnStartColumn_producesVerticalHorizontalVertical() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 0, 30);
    assertRoute(c, 0, 0, 0, 30, 100, 30, 100, 50);
  }

  @Test
  public void buildVertices_bendInsideTheBox_producesThreeBends() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 20);
    assertRoute(c, 0, 0, 60, 0, 60, 20, 100, 20, 100, 50);
  }

  @Test
  public void buildVertices_bendOnBoxCorner_producesSingleBend() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 100, 0);
    assertRoute(c, 0, 0, 100, 0, 100, 50);
  }

  @Test
  public void buildVertices_bendOnOppositeBoxCorner_leavesVerticallyAndArrivesHorizontally() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 0, 50);
    assertRoute(c, 0, 0, 0, 50, 100, 50);
  }

  @Test
  public void buildVertices_bendOutsideTheBox_detoursAround() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, -40, 90);
    assertRoute(c, 0, 0, -40, 0, -40, 90, 100, 90, 100, 50);
  }

  @Test
  public void buildVertices_verticalStart_swapsTheAxes() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 20);
    c.setStartDirection(OrientationHV.VERTICAL);
    assertRoute(c, 0, 0, 0, 20, 60, 20, 60, 50, 100, 50);
  }

  @Test
  public void buildVertices_collapsedRoute_keepsASingleVertex() {
    OrthogonalTestComponent c = component(10, 10, 10, 10, 10, 10);
    assertRoute(c, 10, 10);
  }

  @Test
  public void buildVertices_everySegmentIsAxisAligned() {
    for (OrientationHV startDirection : OrientationHV.values()) {
      for (double x = -50; x <= 150; x += 25) {
        for (double y = -50; y <= 150; y += 25) {
          OrthogonalTestComponent c = component(0, 0, 100, 50, x, y);
          c.setStartDirection(startDirection);
          assertAxisAligned(c);
        }
      }
    }
  }

  // --- point count ---

  @Test
  public void setPointCount_keepsTheEndsAndStaysAxisAligned() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 50);
    c.setPointCount(PointCount.FIVE);
    assertEquals(5, c.getControlPointCount());
    assertPoint(0, 0, c.getControlPoint(0));
    assertPoint(100, 50, c.getControlPoint(4));
    assertAxisAligned(c);
  }

  @Test
  public void setPointCount_backAndForth_restoresTheEnds() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 50);
    c.setPointCount(PointCount.FOUR);
    c.setPointCount(PointCount.THREE);
    assertEquals(3, c.getControlPointCount());
    assertPoint(0, 0, c.getControlPoint(0));
    assertPoint(100, 50, c.getControlPoint(2));
  }

  // --- transformer ---

  @Test
  public void rotate_producesTheRotatedRoute() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 20);
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2, 0, 0);
    List<Point2D> expected = new ArrayList<Point2D>();
    for (Point2D p : c.buildVertices()) {
      expected.add(rotate.transform(p, null));
    }

    new OrthogonalComponentTransformer().rotate(c, new Point2D.Double(0, 0), 1);

    assertEquals(OrientationHV.VERTICAL, c.getStartDirection());
    List<Point2D> actual = c.buildVertices();
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertPoint(expected.get(i).getX(), expected.get(i).getY(), actual.get(i));
    }
  }

  @Test
  public void mirror_keepsTheStartDirection() {
    OrthogonalTestComponent c = component(0, 0, 100, 50, 60, 20);
    new OrthogonalComponentTransformer().mirror(c, new Point2D.Double(50, 25), 1);
    assertEquals(OrientationHV.HORIZONTAL, c.getStartDirection());
  }

  // --- helpers ---

  private static OrthogonalTestComponent component(double startX, double startY, double endX, double endY,
      double bendX, double bendY) {
    OrthogonalTestComponent c = new OrthogonalTestComponent();
    c.setControlPoint(new Point2D.Double(startX, startY), 0);
    c.setControlPoint(new Point2D.Double(bendX, bendY), 1);
    c.setControlPoint(new Point2D.Double(endX, endY), 2);
    return c;
  }

  private static void assertRoute(OrthogonalTestComponent c, double... expected) {
    List<Point2D> vertices = c.buildVertices();
    assertEquals(expected.length / 2, vertices.size());
    for (int i = 0; i < vertices.size(); i++) {
      assertPoint(expected[2 * i], expected[2 * i + 1], vertices.get(i));
    }
  }

  private static void assertPoint(double x, double y, Point2D actual) {
    assertEquals(x, actual.getX(), DELTA);
    assertEquals(y, actual.getY(), DELTA);
  }

  private static void assertAxisAligned(OrthogonalTestComponent c) {
    List<Point2D> vertices = c.buildVertices();
    for (int i = 1; i < vertices.size(); i++) {
      Point2D previous = vertices.get(i - 1);
      Point2D current = vertices.get(i);
      boolean aligned = Math.abs(previous.getX() - current.getX()) < DELTA
          || Math.abs(previous.getY() - current.getY()) < DELTA;
      assertTrue("segment " + previous + " to " + current + " is not axis aligned", aligned);
    }
  }

  private static class OrthogonalTestComponent extends AbstractOrthogonalComponent<Void> {

    private static final long serialVersionUID = 1L;

    @Override
    protected void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState,
        IDrawingObserver drawingObserver) {}

    @Override
    protected Color getDefaultColor() {
      return Color.black;
    }

    @Override
    public Void getValue() {
      return null;
    }

    @Override
    public void setValue(Void value) {}
  }
}
