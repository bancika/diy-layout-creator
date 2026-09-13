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
  public void stretchBetween_movesTheIntermediatePointsOffTheChord() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    assertTrue("first handle should not sit on the chord",
        Math.abs(c.getControlPoint(1).getY()) > 1);
    assertTrue("second handle should not sit on the chord",
        Math.abs(c.getControlPoint(2).getY()) > 1);
  }

  @Test
  public void stretchBetween_putsConsecutivePointsOnOppositeSides() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    assertTrue("the first excursion should point down the screen", c.getControlPoint(1).getY() > 0);
    assertTrue("the second excursion should point the other way", c.getControlPoint(2).getY() < 0);
  }

  @Test
  public void stretchBetween_alternatesSidesForEveryPointCount() {
    for (PointCount pointCount : PointCount.values()) {
      CurvedTestComponent c = new CurvedTestComponent();
      c.setPointCount(pointCount);
      c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
      double previous = 0;
      for (int i = 1; i < c.getControlPointCount() - 1; i++) {
        double current = c.getControlPoint(i).getY();
        // points that lie on the curve stay on the chord and do not take part in the alternation
        if (Math.abs(current) < DELTA) {
          continue;
        }
        if (previous != 0) {
          assertTrue(pointCount + " handle " + i + " should sit opposite the previous one",
              previous * current < 0);
        }
        previous = current;
      }
    }
  }

  @Test
  public void stretchBetween_keepsTheOnCurvePointsOnTheStraightLine() {
    for (PointCount pointCount : new PointCount[] {PointCount.FIVE, PointCount.SEVEN}) {
      CurvedTestComponent c = new CurvedTestComponent();
      c.setPointCount(pointCount);
      c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
      assertEquals(pointCount + " should keep its middle point on the chord", 0,
          c.getControlPoint(middleIndex(pointCount)).getY(), DELTA);
    }
  }

  /**
   * The smoothing in {@code draw} mirrors the handles around a point that lies on the curve, and
   * rewrites one of them when they are not already mirrored. Placing them mirrored to begin with
   * is what keeps that pass from pulling the freshly created wave out of shape.
   */
  @Test
  public void stretchBetween_placesHandlesTheWayTheSmoothingRuleWants() {
    for (PointCount pointCount : new PointCount[] {PointCount.FIVE, PointCount.SEVEN}) {
      CurvedTestComponent c = new CurvedTestComponent();
      c.setPointCount(pointCount);
      c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 60));
      int middle = middleIndex(pointCount);
      Point2D before = c.getControlPoint(middle - 1);
      Point2D on = c.getControlPoint(middle);
      Point2D after = c.getControlPoint(middle + 1);
      assertEquals(pointCount + " handles should mirror horizontally", 2 * on.getX() - before.getX(),
          after.getX(), DELTA);
      assertEquals(pointCount + " handles should mirror vertically", 2 * on.getY() - before.getY(),
          after.getY(), DELTA);
    }
  }

  private static int middleIndex(PointCount pointCount) {
    return pointCount == PointCount.FIVE ? 2 : 3;
  }

  @Test
  public void stretchBetween_waveIsSymmetricAboutTheMiddle() {
    CurvedTestComponent c = new CurvedTestComponent();
    c.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    assertEquals(-c.getControlPoint(1).getY(), c.getControlPoint(2).getY(), DELTA);
    assertEquals(100 - c.getControlPoint(2).getX(), c.getControlPoint(1).getX(), DELTA);
  }

  @Test
  public void stretchBetween_wavesTheSameWayRegardlessOfDrawingDirection() {
    CurvedTestComponent forward = new CurvedTestComponent();
    forward.stretchBetween(new Point2D.Double(0, 0), new Point2D.Double(100, 0));
    CurvedTestComponent backward = new CurvedTestComponent();
    backward.stretchBetween(new Point2D.Double(100, 0), new Point2D.Double(0, 0));
    for (int i = 1; i < forward.getControlPointCount() - 1; i++) {
      assertEquals(forward.getControlPoint(i).getY(), backward.getControlPoint(i).getY(), DELTA);
    }
  }

  @Test
  public void stretchBetween_scalesTheWaveWithTheDistance() {
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
