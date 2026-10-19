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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.diylc.common.AlignmentMode;
import org.diylc.common.DistributionMode;

/**
 * Calculates how far each unit of a selection needs to move to be aligned or distributed. Units are
 * given by their bounding rectangles and the result holds one delta per rectangle, in input order.
 * Applying the deltas is left to the caller.
 * 
 * @author Branislav Stojkovic
 */
public class AlignmentManager {

  public static final int MIN_ALIGN_UNITS = 2;
  public static final int MIN_DISTRIBUTE_UNITS = 3;

  public List<Point2D> align(List<Rectangle2D> unitBounds, AlignmentMode mode, Double gridStep) {
    List<Point2D> deltas = createZeroDeltas(unitBounds.size());
    if (unitBounds.size() < MIN_ALIGN_UNITS) {
      return deltas;
    }

    Rectangle2D bounds = new Rectangle2D.Double();
    bounds.setRect(unitBounds.get(0));
    for (Rectangle2D r : unitBounds) {
      bounds.add(r);
    }

    for (int i = 0; i < unitBounds.size(); i++) {
      Rectangle2D r = unitBounds.get(i);
      double dx = 0;
      double dy = 0;
      switch (mode) {
        case LEFT:
          dx = bounds.getMinX() - r.getMinX();
          break;
        case RIGHT:
          dx = bounds.getMaxX() - r.getMaxX();
          break;
        case HORIZONTAL_CENTER:
          dx = bounds.getCenterX() - r.getCenterX();
          break;
        case TOP:
          dy = bounds.getMinY() - r.getMinY();
          break;
        case BOTTOM:
          dy = bounds.getMaxY() - r.getMaxY();
          break;
        case VERTICAL_CENTER:
          dy = bounds.getCenterY() - r.getCenterY();
          break;
      }
      deltas.get(i).setLocation(snap(dx, gridStep), snap(dy, gridStep));
    }
    return deltas;
  }

  public List<Point2D> distribute(List<Rectangle2D> unitBounds, DistributionMode mode,
      Double gridStep) {
    List<Point2D> deltas = createZeroDeltas(unitBounds.size());
    int count = unitBounds.size();
    if (count < MIN_DISTRIBUTE_UNITS) {
      return deltas;
    }

    boolean horizontal = mode.isHorizontal();
    // the sort is stable, so units that tie on both keys keep their input order
    List<Integer> order = new ArrayList<Integer>();
    for (int i = 0; i < count; i++) {
      order.add(i);
    }
    order.sort(Comparator.<Integer>comparingDouble(i -> getCenter(unitBounds.get(i), horizontal))
        .thenComparingDouble(i -> getMin(unitBounds.get(i), horizontal)));

    Rectangle2D first = unitBounds.get(order.get(0));
    Rectangle2D last = unitBounds.get(order.get(count - 1));

    if (mode.isSpacing()) {
      double totalSize = 0;
      for (Rectangle2D r : unitBounds) {
        totalSize += getSize(r, horizontal);
      }
      // negative when the units do not fit between the outer edges, which makes them overlap evenly
      double gap = (getMax(last, horizontal) - getMin(first, horizontal) - totalSize) / (count - 1);
      double position = getMin(first, horizontal);
      for (int index : order) {
        Rectangle2D r = unitBounds.get(index);
        setDelta(deltas.get(index), position - getMin(r, horizontal), horizontal, gridStep);
        position += getSize(r, horizontal) + gap;
      }
    } else {
      double firstCenter = getCenter(first, horizontal);
      double step = (getCenter(last, horizontal) - firstCenter) / (count - 1);
      for (int k = 0; k < count; k++) {
        int index = order.get(k);
        double target = firstCenter + k * step;
        setDelta(deltas.get(index), target - getCenter(unitBounds.get(index), horizontal),
            horizontal, gridStep);
      }
    }
    return deltas;
  }

  private static List<Point2D> createZeroDeltas(int count) {
    List<Point2D> deltas = new ArrayList<Point2D>(count);
    for (int i = 0; i < count; i++) {
      deltas.add(new Point2D.Double());
    }
    return deltas;
  }

  private static void setDelta(Point2D delta, double d, boolean horizontal, Double gridStep) {
    if (horizontal) {
      delta.setLocation(snap(d, gridStep), 0);
    } else {
      delta.setLocation(0, snap(d, gridStep));
    }
  }

  private static double snap(double d, Double gridStep) {
    if (gridStep == null || gridStep <= 0) {
      return d;
    }
    return Math.round(d / gridStep) * gridStep;
  }

  private static double getMin(Rectangle2D r, boolean horizontal) {
    return horizontal ? r.getMinX() : r.getMinY();
  }

  private static double getMax(Rectangle2D r, boolean horizontal) {
    return horizontal ? r.getMaxX() : r.getMaxY();
  }

  private static double getCenter(Rectangle2D r, boolean horizontal) {
    return horizontal ? r.getCenterX() : r.getCenterY();
  }

  private static double getSize(Rectangle2D r, boolean horizontal) {
    return horizontal ? r.getWidth() : r.getHeight();
  }
}
