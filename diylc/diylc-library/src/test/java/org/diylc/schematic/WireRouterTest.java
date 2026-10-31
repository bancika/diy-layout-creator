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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.diylc.common.OrientationHV;
import org.junit.Test;

public class WireRouterTest {

  private static final List<Rectangle2D> NO_BODIES = new ArrayList<Rectangle2D>();
  private static final List<Line2D> NO_WIRES = new ArrayList<Line2D>();

  private final WireRouter router = new WireRouter();

  @Test
  public void pinsOnTheSameRowGetAStraightRun() {
    List<Point2D> vertices = routeBetween(new Point2D.Double(100, 100),
        OrientationHV.HORIZONTAL, new Point2D.Double(400, 100), OrientationHV.HORIZONTAL,
        NO_BODIES, NO_WIRES);

    assertEquals("a straight run needs no corner", 2, vertices.size());
    assertEquals(100.0, vertices.get(0).getY(), 0.001);
    assertEquals(100.0, vertices.get(1).getY(), 0.001);
  }

  @Test
  public void pinsOnTheSameColumnGetAStraightRun() {
    List<Point2D> vertices = routeBetween(new Point2D.Double(100, 100), OrientationHV.VERTICAL,
        new Point2D.Double(100, 400), OrientationHV.VERTICAL, NO_BODIES, NO_WIRES);

    assertEquals(2, vertices.size());
    assertEquals(100.0, vertices.get(0).getX(), 0.001);
    assertEquals(100.0, vertices.get(1).getX(), 0.001);
  }

  @Test
  public void theRouteStartsAndEndsOnTheTwoPins() {
    Point2D start = new Point2D.Double(120, 140);
    Point2D end = new Point2D.Double(430, 360);

    List<Point2D> vertices = routeBetween(start, OrientationHV.HORIZONTAL, end,
        OrientationHV.VERTICAL, NO_BODIES, NO_WIRES);

    assertEquals(start.getX(), vertices.get(0).getX(), 0.001);
    assertEquals(start.getY(), vertices.get(0).getY(), 0.001);
    assertEquals(end.getX(), vertices.get(vertices.size() - 1).getX(), 0.001);
    assertEquals(end.getY(), vertices.get(vertices.size() - 1).getY(), 0.001);
  }

  @Test
  public void everySegmentRunsAlongAnAxis() {
    List<Point2D> vertices = routeBetween(new Point2D.Double(120, 140), OrientationHV.HORIZONTAL,
        new Point2D.Double(430, 360), OrientationHV.VERTICAL, NO_BODIES, NO_WIRES);

    for (Line2D segment : segmentsOf(vertices)) {
      boolean axisAligned = Math.abs(segment.getX1() - segment.getX2()) < 0.001
          || Math.abs(segment.getY1() - segment.getY2()) < 0.001;
      assertTrue("segment " + segment.getP1() + " -> " + segment.getP2() + " is diagonal",
          axisAligned);
    }
  }

  @Test
  public void theRequestedExitAxisIsHonouredWhenNothingIsInTheWay() {
    List<Point2D> vertices = routeBetween(new Point2D.Double(100, 100), OrientationHV.VERTICAL,
        new Point2D.Double(400, 300), OrientationHV.HORIZONTAL, NO_BODIES, NO_WIRES);

    List<Line2D> segments = segmentsOf(vertices);
    assertEquals("the wire should leave the pin on the axis it was asked for",
        OrientationHV.VERTICAL, axisOf(segments.get(0)));
    assertEquals("and arrive on the axis it was asked for", OrientationHV.HORIZONTAL,
        axisOf(segments.get(segments.size() - 1)));
  }

  @Test
  public void aSymbolBodyBetweenThePinsIsRoutedAround() {
    Rectangle2D body = new Rectangle2D.Double(180, 180, 140, 140);

    List<Point2D> vertices = routeBetween(new Point2D.Double(100, 100), OrientationHV.HORIZONTAL,
        new Point2D.Double(400, 400), OrientationHV.HORIZONTAL, Arrays.asList(body), NO_WIRES);

    for (Line2D segment : segmentsOf(vertices)) {
      assertFalse("the wire runs straight through a symbol", body.intersectsLine(segment));
    }
  }

  @Test
  public void aWireAlreadyPlacedChangesTheRouteChosen() {
    Point2D start = new Point2D.Double(100, 100);
    Point2D end = new Point2D.Double(400, 400);

    List<Point2D> clear = routeBetween(start, OrientationHV.HORIZONTAL, end,
        OrientationHV.HORIZONTAL, NO_BODIES, NO_WIRES);
    // lay a wire along the whole of the route the router picked when the drawing was empty
    List<Line2D> placed = segmentsOf(clear);
    List<Point2D> crowded = routeBetween(start, OrientationHV.HORIZONTAL, end,
        OrientationHV.HORIZONTAL, NO_BODIES, placed);

    assertFalse("the router ignored the wire already on the drawing", samePath(clear, crowded));
  }

  private List<Point2D> routeBetween(Point2D start, OrientationHV startAxis, Point2D end,
      OrientationHV endAxis, List<Rectangle2D> bodies, List<Line2D> placed) {
    WireRouter.Route route = router.route(start, startAxis, end, endAxis, bodies, placed);
    return WireRouter.vertices(start, route.getBendPoint(), end, route.getStartDirection());
  }

  private static List<Line2D> segmentsOf(List<Point2D> vertices) {
    List<Line2D> segments = new ArrayList<Line2D>();
    for (int i = 1; i < vertices.size(); i++) {
      segments.add(new Line2D.Double(vertices.get(i - 1), vertices.get(i)));
    }
    return segments;
  }

  private static OrientationHV axisOf(Line2D segment) {
    return Math.abs(segment.getX2() - segment.getX1()) >= Math.abs(segment.getY2() - segment.getY1())
        ? OrientationHV.HORIZONTAL
        : OrientationHV.VERTICAL;
  }

  private static boolean samePath(List<Point2D> a, List<Point2D> b) {
    if (a.size() != b.size()) {
      return false;
    }
    for (int i = 0; i < a.size(); i++) {
      if (a.get(i).distance(b.get(i)) > 0.001) {
        return false;
      }
    }
    return true;
  }
}
