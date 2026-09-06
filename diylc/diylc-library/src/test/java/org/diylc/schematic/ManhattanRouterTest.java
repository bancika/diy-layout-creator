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
package org.diylc.schematic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.List;

import org.junit.Test;

public class ManhattanRouterTest {

  @Test
  public void straightHorizontalWhenAligned() {
    List<Point2D> route =
        ManhattanRouter.route(new Point2D.Double(0, 10), new Point2D.Double(100, 10));
    assertEquals(2, route.size());
    assertEquals(10d, route.get(1).getY(), 0.001);
  }

  @Test
  public void straightVerticalWhenAligned() {
    List<Point2D> route =
        ManhattanRouter.route(new Point2D.Double(20, 0), new Point2D.Double(20, 80));
    assertEquals(2, route.size());
    assertEquals(20d, route.get(1).getX(), 0.001);
  }

  @Test
  public void bendsForDiagonalOffset() {
    List<Point2D> route =
        ManhattanRouter.route(new Point2D.Double(0, 0), new Point2D.Double(100, 60));
    assertTrue("expected at least one bend", route.size() >= 3);
    // every segment must be axis-aligned
    for (int i = 0; i < route.size() - 1; i++) {
      Point2D a = route.get(i);
      Point2D b = route.get(i + 1);
      boolean axisAligned = Math.abs(a.getX() - b.getX()) < ManhattanRouter.EPS
          || Math.abs(a.getY() - b.getY()) < ManhattanRouter.EPS;
      assertTrue("segment " + i + " is not axis-aligned", axisAligned);
    }
    assertEquals(0d, route.get(0).distance(new Point2D.Double(0, 0)), 0.001);
    assertEquals(0d, route.get(route.size() - 1).distance(new Point2D.Double(100, 60)), 0.001);
  }
}
