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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.junit.Test;

import org.diylc.components.AbstractCurvedComponent.PointCount;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;

public class AbstractCurvedComponentTest {

  private static final double DELTA = 1e-6;

  @Test
  public void stretchBetween_putsTheEndsExactlyOnTheSuppliedPoints() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(10, 20), new Point2D.Double(110, 20));
    assertPoint(10, 20, c.getControlPoint(0));
    assertPoint(110, 20, c.getControlPoint(3));
  }

  @Test
  public void stretchBetween_bowsTheIntermediatePointsOffTheChord() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    assertTrue("first handle should not sit on the chord", c.getControlPoint(1).getY() > 1);
    assertTrue("second handle should not sit on the chord", c.getControlPoint(2).getY() > 1);
  }

  @Test
  public void stretchBetween_bowIsSymmetric() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    assertEquals(c.getControlPoint(1).getY(), c.getControlPoint(2).getY(), DELTA);
    assertEquals(100 - c.getControlPoint(2).getX(), c.getControlPoint(1).getX(), DELTA);
  }

  @Test
  public void stretchBetween_sagsTheSameWayRegardlessOfDrawingDirection() {
    CurvedTestComponent forward = new CurvedTestComponent();
    forward.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    CurvedTestComponent backward = new CurvedTestComponent();
    backward.stretchBetween(new Point2D.Double(100, 0), new Point2D.Double(0, 0));
    assertEquals(forward.getControlPoint(1).getY(), backward.getControlPoint(2).getY(), DELTA);
    assertEquals(forward.getControlPoint(2).getY(), backward.getControlPoint(1).getY(), DELTA);
  }

  @Test
  public void stretchBetween_scalesTheBowWithTheDistance() {
    CurvedTestComponent shortWire = new CurvedTestComponent();
    shortWire.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(50, 0));
    CurvedTestComponent longWire = new CurvedTestComponent();
    longWire.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(200, 0));
    assertEquals(4 * shortWire.getControlPoint(1).getY(), longWire.getControlPoint(1).getY(), DELTA);
  }

  @Test
  public void stretchBetween_collapsesOntoASinglePointWithoutDividingByZero() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(40, 40), new Point2D.Double(40, 40));
    for (int i = 0; i < c.getControlPointCount(); i++) {
      assertPoint(40, 40, c.getControlPoint(i));
    }
  }

  @Test
  public void stretchBetween_keepsTheEndsForEveryPointCount() {
    for (PointCount pointCount : PointCount.values()) {
      CurvedTestComponent c = new CurvedTestComponent();
      c.setPointCount(pointCount);
      c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 60));
      assertPoint(0, 0, c.getControlPoint(0));
      assertPoint(100, 60, c.getControlPoint(c.getControlPointCount() - 1));
    }
  }

  private static void assertPoint(double x, double y, Point2D actual) {
    assertEquals(x, actual.getX(), DELTA);
    assertEquals(y, actual.getY(), DELTA);
  }

  private static class CurvedTestComponent extends AbstractCurvedComponent<Void> {

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
