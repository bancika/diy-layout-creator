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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Computes Manhattan-style (axis-aligned) routes between two schematic pins. The router decides
 * whether a straight horizontal or vertical segment is enough, or whether the connection needs a
 * single bend (L shape) or a double bend (horizontal-vertical-horizontal or
 * vertical-horizontal-vertical). Among the candidate routes it picks the one that overlaps and
 * crosses existing wires the least.
 *
 * @author Branislav Stojkovic
 */
public class ManhattanRouter {

  /** Coordinates closer than this are considered aligned. */
  public static final double EPS = 1.5;

  public enum Direction {
    LEFT, RIGHT, UP, DOWN, NONE
  }

  private ManhattanRouter() {}

  public static List<Point2D> route(Point2D start, Point2D end) {
    return route(start, end, Direction.NONE, Direction.NONE, Collections.<Line2D>emptyList(), 0);
  }

  /**
   * @param start    start pin location
   * @param end      end pin location
   * @param startDir direction the wire should leave the start pin, or {@link Direction#NONE}
   * @param endDir   direction the wire should leave the end pin, or {@link Direction#NONE}
   * @param obstacles existing wire segments to avoid overlapping/crossing
   * @param grid     grid step used to generate alternative bend positions; 0 disables it
   * @return ordered list of way-points, first equal to {@code start}, last equal to {@code end}
   */
  public static List<Point2D> route(Point2D start, Point2D end, Direction startDir, Direction endDir,
      Collection<Line2D> obstacles, double grid) {
    double dx = end.getX() - start.getX();
    double dy = end.getY() - start.getY();

    if (Math.abs(dx) < EPS && Math.abs(dy) < EPS) {
      return list(start, end);
    }
    if (Math.abs(dy) < EPS) {
      return list(start, new Point2D.Double(end.getX(), start.getY()));
    }
    if (Math.abs(dx) < EPS) {
      return list(start, new Point2D.Double(start.getX(), end.getY()));
    }

    List<List<Point2D>> candidates = new ArrayList<List<Point2D>>();

    // L shapes
    candidates.add(list(start, new Point2D.Double(end.getX(), start.getY()), end));
    candidates.add(list(start, new Point2D.Double(start.getX(), end.getY()), end));

    // HVH with a vertical middle leg at x = bx
    for (double bx : bendCoordinates(start.getX(), end.getX(), grid)) {
      candidates.add(list(start, new Point2D.Double(bx, start.getY()), new Point2D.Double(bx, end.getY()), end));
    }
    // VHV with a horizontal middle leg at y = by
    for (double by : bendCoordinates(start.getY(), end.getY(), grid)) {
      candidates.add(list(start, new Point2D.Double(start.getX(), by), new Point2D.Double(end.getX(), by), end));
    }

    List<Point2D> best = null;
    double bestCost = Double.MAX_VALUE;
    for (List<Point2D> candidate : candidates) {
      double cost = cost(candidate, startDir, endDir, obstacles);
      if (cost < bestCost) {
        bestCost = cost;
        best = candidate;
      }
    }
    return best == null ? list(start, new Point2D.Double(end.getX(), start.getY()), end) : dedupe(best);
  }

  private static List<Double> bendCoordinates(double a, double b, double grid) {
    List<Double> result = new ArrayList<Double>();
    result.add((a + b) / 2);
    result.add((a * 3 + b) / 4);
    result.add((a + b * 3) / 4);
    if (grid > 0) {
      double mid = (a + b) / 2;
      for (int k = 1; k <= 3; k++) {
        result.add(mid + k * grid);
        result.add(mid - k * grid);
      }
    }
    return result;
  }

  private static double cost(List<Point2D> path, Direction startDir, Direction endDir,
      Collection<Line2D> obstacles) {
    double length = 0;
    int bends = Math.max(0, path.size() - 2);
    double crossings = 0;
    double overlap = 0;
    for (int i = 0; i < path.size() - 1; i++) {
      Point2D p1 = path.get(i);
      Point2D p2 = path.get(i + 1);
      length += p1.distance(p2);
      Line2D seg = new Line2D.Double(p1, p2);
      for (Line2D obs : obstacles) {
        if (segmentsOverlap(seg, obs)) {
          overlap += overlapLength(seg, obs);
        } else if (seg.intersectsLine(obs)) {
          crossings++;
        }
      }
    }
    double dirPenalty = 0;
    if (startDir != Direction.NONE && path.size() >= 2
        && !matchesDirection(path.get(0), path.get(1), startDir)) {
      dirPenalty += 40;
    }
    if (endDir != Direction.NONE && path.size() >= 2
        && !matchesDirection(path.get(path.size() - 1), path.get(path.size() - 2), endDir)) {
      dirPenalty += 40;
    }
    return length * 0.01 + bends * 6 + crossings * 15 + overlap * 0.5 + dirPenalty;
  }

  private static boolean matchesDirection(Point2D from, Point2D to, Direction dir) {
    double dx = to.getX() - from.getX();
    double dy = to.getY() - from.getY();
    switch (dir) {
      case LEFT:
        return dx < -EPS && Math.abs(dy) < EPS;
      case RIGHT:
        return dx > EPS && Math.abs(dy) < EPS;
      case UP:
        return dy < -EPS && Math.abs(dx) < EPS;
      case DOWN:
        return dy > EPS && Math.abs(dx) < EPS;
      default:
        return true;
    }
  }

  private static boolean isHorizontal(Line2D l) {
    return Math.abs(l.getY1() - l.getY2()) < EPS;
  }

  private static boolean isVertical(Line2D l) {
    return Math.abs(l.getX1() - l.getX2()) < EPS;
  }

  private static boolean segmentsOverlap(Line2D a, Line2D b) {
    if (isHorizontal(a) && isHorizontal(b) && Math.abs(a.getY1() - b.getY1()) < EPS) {
      return rangesOverlap(a.getX1(), a.getX2(), b.getX1(), b.getX2());
    }
    if (isVertical(a) && isVertical(b) && Math.abs(a.getX1() - b.getX1()) < EPS) {
      return rangesOverlap(a.getY1(), a.getY2(), b.getY1(), b.getY2());
    }
    return false;
  }

  private static double overlapLength(Line2D a, Line2D b) {
    if (isHorizontal(a)) {
      return rangeOverlap(a.getX1(), a.getX2(), b.getX1(), b.getX2());
    }
    return rangeOverlap(a.getY1(), a.getY2(), b.getY1(), b.getY2());
  }

  private static boolean rangesOverlap(double a1, double a2, double b1, double b2) {
    return rangeOverlap(a1, a2, b1, b2) > EPS;
  }

  private static double rangeOverlap(double a1, double a2, double b1, double b2) {
    double lo = Math.max(Math.min(a1, a2), Math.min(b1, b2));
    double hi = Math.min(Math.max(a1, a2), Math.max(b1, b2));
    return Math.max(0, hi - lo);
  }

  private static List<Point2D> list(Point2D... points) {
    List<Point2D> result = new ArrayList<Point2D>();
    for (Point2D p : points) {
      result.add(new Point2D.Double(p.getX(), p.getY()));
    }
    return result;
  }

  private static List<Point2D> dedupe(List<Point2D> points) {
    List<Point2D> result = new ArrayList<Point2D>();
    for (Point2D p : points) {
      if (result.isEmpty() || result.get(result.size() - 1).distance(p) > EPS) {
        result.add(p);
      }
    }
    // drop collinear middle points
    int i = 1;
    while (i < result.size() - 1) {
      Point2D a = result.get(i - 1);
      Point2D b = result.get(i);
      Point2D c = result.get(i + 1);
      boolean collinearH = Math.abs(a.getY() - b.getY()) < EPS && Math.abs(b.getY() - c.getY()) < EPS;
      boolean collinearV = Math.abs(a.getX() - b.getX()) < EPS && Math.abs(b.getX() - c.getX()) < EPS;
      if (collinearH || collinearV) {
        result.remove(i);
      } else {
        i++;
      }
    }
    return result;
  }
}
