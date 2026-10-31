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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.common.OrientationHV;

/**
 * Chooses the bend point of a generated schematic wire.
 *
 * <p>
 * A {@code Right Angle Line} derives its whole route from one interior handle plus the axis it leaves
 * the first pin on, so routing here means picking those two rather than emitting a polyline. The
 * router proposes a handful of handles - the two L shapes, the mid and quarter points, and a few
 * positions outside the box spanned by the two pins that let a wire detour - and keeps the one that
 * scores best.
 * </p>
 *
 * <p>
 * Scoring is what the previous bend-point rule lacked: it had no idea what else was on the drawing,
 * so wires ran through symbols and along each other. Here a route is charged for its length, for
 * every corner, for crossing a wire already placed, for running on top of one, and heavily for
 * passing through the body of a symbol.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class WireRouter {

  /** Grid the handle is snapped to. */
  public static final double GRID = 10d;
  /** How far outside the endpoints a detour candidate is offered. */
  private static final double DETOUR = 40d;

  private static final double LENGTH_COST = 0.01;
  private static final double BEND_COST = 6;
  private static final double CROSSING_COST = 15;
  private static final double OVERLAP_COST = 0.5;
  private static final double BODY_COST = 40;
  private static final double WRONG_AXIS_COST = 8;
  private static final double EPS = 0.5;

  /** The chosen route: the axis the wire leaves the first pin on, and its single bend point. */
  public static class Route {

    private final OrientationHV startDirection;
    private final Point2D bendPoint;

    Route(OrientationHV startDirection, Point2D bendPoint) {
      this.startDirection = startDirection;
      this.bendPoint = bendPoint;
    }

    public OrientationHV getStartDirection() {
      return startDirection;
    }

    public Point2D getBendPoint() {
      return bendPoint;
    }
  }

  /**
   * @param start    the first pin
   * @param startAxis the axis the wire ought to leave the first pin on
   * @param end      the second pin
   * @param endAxis  the axis the wire ought to arrive at the second pin on
   * @param bodies   boxes the route should stay out of, the two pins' own symbols excluded
   * @param placed   segments of the wires already routed
   */
  public Route route(Point2D start, OrientationHV startAxis, Point2D end, OrientationHV endAxis,
      List<Rectangle2D> bodies, List<Line2D> placed) {
    // pins that already share a row or a column want a straight run, which a handle sitting on the
    // first pin produces whichever axis the template starts on
    if (Math.abs(start.getX() - end.getX()) < 1) {
      return new Route(OrientationHV.VERTICAL, copyOf(start));
    }
    if (Math.abs(start.getY() - end.getY()) < 1) {
      return new Route(OrientationHV.HORIZONTAL, copyOf(start));
    }

    Route best = null;
    double bestCost = Double.MAX_VALUE;
    for (OrientationHV direction : OrientationHV.values()) {
      for (Point2D handle : candidates(start, end)) {
        double cost = cost(start, handle, end, direction, startAxis, endAxis, bodies, placed);
        if (cost < bestCost) {
          bestCost = cost;
          best = new Route(direction, handle);
        }
      }
    }
    return best;
  }

  private static List<Point2D> candidates(Point2D start, Point2D end) {
    double midX = snap((start.getX() + end.getX()) / 2);
    double midY = snap((start.getY() + end.getY()) / 2);
    double leftX = snap(Math.min(start.getX(), end.getX()) - DETOUR);
    double rightX = snap(Math.max(start.getX(), end.getX()) + DETOUR);
    double topY = snap(Math.min(start.getY(), end.getY()) - DETOUR);
    double bottomY = snap(Math.max(start.getY(), end.getY()) + DETOUR);

    List<Point2D> result = new ArrayList<Point2D>();
    // the two L shapes
    result.add(new Point2D.Double(snap(end.getX()), snap(start.getY())));
    result.add(new Point2D.Double(snap(start.getX()), snap(end.getY())));
    // a leg down the middle, which is the shape most schematics use
    result.add(new Point2D.Double(midX, snap(start.getY())));
    result.add(new Point2D.Double(midX, snap(end.getY())));
    result.add(new Point2D.Double(snap(start.getX()), midY));
    result.add(new Point2D.Double(snap(end.getX()), midY));
    result.add(new Point2D.Double(midX, midY));
    // detours around whatever sits between the two pins
    result.add(new Point2D.Double(leftX, midY));
    result.add(new Point2D.Double(rightX, midY));
    result.add(new Point2D.Double(midX, topY));
    result.add(new Point2D.Double(midX, bottomY));
    return result;
  }

  private double cost(Point2D start, Point2D handle, Point2D end, OrientationHV direction,
      OrientationHV startAxis, OrientationHV endAxis, List<Rectangle2D> bodies,
      List<Line2D> placed) {
    List<Point2D> vertices = vertices(start, handle, end, direction);
    List<Line2D> segments = segments(vertices);
    if (segments.isEmpty()) {
      return Double.MAX_VALUE;
    }

    double cost = BEND_COST * Math.max(0, vertices.size() - 2);
    for (Line2D segment : segments) {
      cost += LENGTH_COST * segment.getP1().distance(segment.getP2());
      for (Rectangle2D body : bodies) {
        if (body.intersectsLine(segment)) {
          cost += BODY_COST;
        }
      }
      for (Line2D other : placed) {
        if (crosses(segment, other)) {
          cost += CROSSING_COST;
        }
        cost += OVERLAP_COST * overlapLength(segment, other);
      }
    }
    if (axisOf(segments.get(0)) != startAxis) {
      cost += WRONG_AXIS_COST;
    }
    if (axisOf(segments.get(segments.size() - 1)) != endAxis) {
      cost += WRONG_AXIS_COST;
    }
    return cost;
  }

  /**
   * Expands a start, a bend point and an end into the vertices of the route, the same way
   * {@code AbstractOrthogonalComponent} does: every control point is reached by a move along one axis
   * followed by a move along the other. Zero length steps are dropped as they are produced.
   */
  public static List<Point2D> vertices(Point2D start, Point2D handle, Point2D end,
      OrientationHV startDirection) {
    boolean horizontalFirst = startDirection == OrientationHV.HORIZONTAL;
    List<Point2D> vertices = new ArrayList<Point2D>();
    add(vertices, start);
    Point2D previous = start;
    for (Point2D next : new Point2D[] {handle, end}) {
      if (horizontalFirst) {
        add(vertices, new Point2D.Double(next.getX(), previous.getY()));
      } else {
        add(vertices, new Point2D.Double(previous.getX(), next.getY()));
      }
      add(vertices, next);
      previous = next;
    }
    return vertices;
  }

  private static void add(List<Point2D> vertices, Point2D p) {
    if (!vertices.isEmpty() && vertices.get(vertices.size() - 1).distance(p) < EPS) {
      return;
    }
    vertices.add(new Point2D.Double(p.getX(), p.getY()));
  }

  private static List<Line2D> segments(List<Point2D> vertices) {
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

  /** @return true when the two segments genuinely cross, rather than merely touching end to end. */
  private static boolean crosses(Line2D a, Line2D b) {
    if (axisOf(a) == axisOf(b)) {
      return false;
    }
    if (!a.intersectsLine(b)) {
      return false;
    }
    // a shared endpoint is a junction, not a crossing
    return !(near(a.getP1(), b.getP1()) || near(a.getP1(), b.getP2()) || near(a.getP2(), b.getP1())
        || near(a.getP2(), b.getP2()));
  }

  /** @return the length the two segments run along one another, which reads as a single thick line. */
  private static double overlapLength(Line2D a, Line2D b) {
    if (axisOf(a) != axisOf(b)) {
      return 0;
    }
    if (axisOf(a) == OrientationHV.HORIZONTAL) {
      if (Math.abs(a.getY1() - b.getY1()) > EPS) {
        return 0;
      }
      return span(a.getX1(), a.getX2(), b.getX1(), b.getX2());
    }
    if (Math.abs(a.getX1() - b.getX1()) > EPS) {
      return 0;
    }
    return span(a.getY1(), a.getY2(), b.getY1(), b.getY2());
  }

  private static double span(double a1, double a2, double b1, double b2) {
    double low = Math.max(Math.min(a1, a2), Math.min(b1, b2));
    double high = Math.min(Math.max(a1, a2), Math.max(b1, b2));
    return Math.max(0, high - low);
  }

  private static boolean near(Point2D a, Point2D b) {
    return a.distance(b) < EPS;
  }

  private static Point2D copyOf(Point2D p) {
    return new Point2D.Double(p.getX(), p.getY());
  }

  private static double snap(double value) {
    return Math.round(value / GRID) * GRID;
  }
}
