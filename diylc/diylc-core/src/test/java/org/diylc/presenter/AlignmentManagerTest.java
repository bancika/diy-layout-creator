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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.diylc.common.AlignmentMode;
import org.diylc.common.DistributionMode;
import org.junit.Test;

public class AlignmentManagerTest {

  private static final double EPS = 1e-9;

  private AlignmentManager manager = new AlignmentManager();

  private List<Rectangle2D> createUnits() {
    return Arrays.asList(new Rectangle2D.Double(10, 0, 20, 10),
        new Rectangle2D.Double(50, 30, 40, 20), new Rectangle2D.Double(0, 100, 10, 40));
  }

  @Test
  public void testAlignLeft() {
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.LEFT, null);
    assertDelta(-10, 0, deltas.get(0));
    assertDelta(-50, 0, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testAlignRight() {
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.RIGHT, null);
    assertDelta(60, 0, deltas.get(0));
    assertDelta(0, 0, deltas.get(1));
    assertDelta(80, 0, deltas.get(2));
  }

  @Test
  public void testAlignHorizontalCenter() {
    // the selection spans x = 0..90, so every unit is centered on x = 45
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.HORIZONTAL_CENTER, null);
    assertDelta(25, 0, deltas.get(0));
    assertDelta(-25, 0, deltas.get(1));
    assertDelta(40, 0, deltas.get(2));
  }

  @Test
  public void testAlignTop() {
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.TOP, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(0, -30, deltas.get(1));
    assertDelta(0, -100, deltas.get(2));
  }

  @Test
  public void testAlignBottom() {
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.BOTTOM, null);
    assertDelta(0, 130, deltas.get(0));
    assertDelta(0, 90, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testAlignVerticalCenter() {
    // the selection spans y = 0..140, so every unit is centered on y = 70
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.VERTICAL_CENTER, null);
    assertDelta(0, 65, deltas.get(0));
    assertDelta(0, 30, deltas.get(1));
    assertDelta(0, -50, deltas.get(2));
  }

  @Test
  public void testAlignAlreadyAligned() {
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(10, 0, 20, 10),
        new Rectangle2D.Double(10, 50, 5, 10));
    List<Point2D> deltas = manager.align(units, AlignmentMode.LEFT, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(0, 0, deltas.get(1));
  }

  @Test
  public void testAlignSnapsToGrid() {
    List<Point2D> deltas = manager.align(createUnits(), AlignmentMode.HORIZONTAL_CENTER, 20d);
    assertDelta(20, 0, deltas.get(0));
    assertDelta(-20, 0, deltas.get(1));
    assertDelta(40, 0, deltas.get(2));
  }

  @Test
  public void testAlignSingleUnitDoesNothing() {
    List<Point2D> deltas = manager.align(
        Collections.<Rectangle2D>singletonList(new Rectangle2D.Double(10, 10, 5, 5)),
        AlignmentMode.LEFT, null);
    assertEquals(1, deltas.size());
    assertDelta(0, 0, deltas.get(0));
  }

  @Test
  public void testDistributeCentersHorizontally() {
    // centers at 5, 15 and 105, given out of order; the outer two stay put
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(100, 0, 10, 10),
        new Rectangle2D.Double(0, 0, 10, 10), new Rectangle2D.Double(5, 0, 20, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.HORIZONTAL_CENTERS, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(0, 0, deltas.get(1));
    assertDelta(40, 0, deltas.get(2));
  }

  @Test
  public void testDistributeCentersVertically() {
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 10, 10),
        new Rectangle2D.Double(0, 10, 10, 10), new Rectangle2D.Double(0, 20, 10, 10),
        new Rectangle2D.Double(0, 90, 10, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.VERTICAL_CENTERS, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(0, 20, deltas.get(1));
    assertDelta(0, 40, deltas.get(2));
    assertDelta(0, 0, deltas.get(3));
  }

  @Test
  public void testDistributeSpacingHorizontally() {
    // widths 10 + 30 + 20 across a span of 0..100 leave two gaps of 20
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 10, 10),
        new Rectangle2D.Double(15, 0, 30, 10), new Rectangle2D.Double(80, 0, 20, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.HORIZONTAL_SPACING, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(15, 0, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testDistributeSpacingVertically() {
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 10, 10),
        new Rectangle2D.Double(0, 15, 10, 30), new Rectangle2D.Double(0, 80, 10, 20));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.VERTICAL_SPACING, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(0, 15, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testDistributeSpacingOverlapsWhenUnitsDoNotFit() {
    // widths 20 + 40 + 20 across a span of 0..50 leave two gaps of -15
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 20, 10),
        new Rectangle2D.Double(10, 0, 40, 10), new Rectangle2D.Double(30, 0, 20, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.HORIZONTAL_SPACING, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(-5, 0, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testDistributeTiesKeepInputOrder() {
    // the last two units share both their center and their left edge
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 10, 10),
        new Rectangle2D.Double(40, 0, 10, 10), new Rectangle2D.Double(40, 0, 10, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.HORIZONTAL_CENTERS, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(-20, 0, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testDistributeSnapsToGrid() {
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 10, 10),
        new Rectangle2D.Double(10, 0, 10, 10), new Rectangle2D.Double(70, 0, 10, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.HORIZONTAL_CENTERS, 20d);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(20, 0, deltas.get(1));
    assertDelta(0, 0, deltas.get(2));
  }

  @Test
  public void testDistributeTwoUnitsDoesNothing() {
    List<Rectangle2D> units = Arrays.asList(new Rectangle2D.Double(0, 0, 10, 10),
        new Rectangle2D.Double(70, 0, 10, 10));
    List<Point2D> deltas = manager.distribute(units, DistributionMode.HORIZONTAL_SPACING, null);
    assertDelta(0, 0, deltas.get(0));
    assertDelta(0, 0, deltas.get(1));
  }

  private static void assertDelta(double dx, double dy, Point2D delta) {
    assertEquals(dx, delta.getX(), EPS);
    assertEquals(dy, delta.getY(), EPS);
  }
}
