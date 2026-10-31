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
package org.diylc.presenter;

import static org.junit.Assert.assertEquals;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.junit.Test;

public class StuckPointFollowerTest {

  private static final double TOLERANCE = 7;

  @Test
  public void testFullyIncludedComponentsAreTransformed() {
    TestPart part = new TestPart(new Point2D.Double(0, 0), new Point2D.Double(40, 0));
    TestPart wire = new TestPart(new Point2D.Double(40, 0), new Point2D.Double(100, 0));
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap =
        new HashMap<IDIYComponent<?>, Set<Integer>>();
    controlPointMap.put(part, Set.of(0, 1));
    controlPointMap.put(wire, Set.of(0));

    StuckPointFollower follower = new StuckPointFollower(controlPointMap, TOLERANCE);

    assertEquals(Set.of(part), follower.getTransformedComponents());
  }

  @Test
  public void testStuckPointFollowsAnchorByIndex() {
    TestPart part = new TestPart(new Point2D.Double(0, 0), new Point2D.Double(40, 0));
    TestPart wire = new TestPart(new Point2D.Double(40, 0), new Point2D.Double(100, 0));
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap =
        new HashMap<IDIYComponent<?>, Set<Integer>>();
    controlPointMap.put(part, Set.of(0, 1));
    controlPointMap.put(wire, Set.of(0));
    StuckPointFollower follower = new StuckPointFollower(controlPointMap, TOLERANCE);

    // a transformer that does not map points through the plain reflection, the way mirroring an
    // IC moves its pins
    part.setControlPoint(new Point2D.Double(-20, 30), 1);
    follower.follow(new AffineTransform(-1, 0, 0, 1, 40, 0));

    assertEquals(new Point2D.Double(-20, 30), wire.getControlPoint(0));
    assertEquals(new Point2D.Double(100, 0), wire.getControlPoint(1));
  }

  @Test
  public void testPointWithoutAnchorIsTransformed() {
    TestPart part = new TestPart(new Point2D.Double(0, 0), new Point2D.Double(40, 0));
    TestPart wire = new TestPart(new Point2D.Double(40, 20), new Point2D.Double(100, 0));
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap =
        new HashMap<IDIYComponent<?>, Set<Integer>>();
    controlPointMap.put(part, Set.of(0, 1));
    controlPointMap.put(wire, Set.of(0));
    StuckPointFollower follower = new StuckPointFollower(controlPointMap, TOLERANCE);

    follower.follow(AffineTransform.getRotateInstance(Math.PI / 2, 20, 0));

    assertEquals(0, wire.getControlPoint(0).distance(0, 20), 1e-9);
  }

  private static class TestPart extends AbstractComponent<Void> {

    private static final long serialVersionUID = 1L;

    private final Point2D[] points;

    public TestPart(Point2D... points) {
      this.points = points;
    }

    @Override
    public int getControlPointCount() {
      return points.length;
    }

    @Override
    public Point2D getControlPoint(int index) {
      return points[index];
    }

    @Override
    public void setControlPoint(Point2D point, int index) {
      points[index].setLocation(point);
    }

    @Override
    public boolean isControlPointSticky(int index) {
      return true;
    }

    @Override
    public boolean canControlPointOverlap(int index) {
      return true;
    }

    @Override
    public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
      return VisibilityPolicy.WHEN_SELECTED;
    }

    @Override
    public String getControlPointNodeName(int index) {
      return null;
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
}
