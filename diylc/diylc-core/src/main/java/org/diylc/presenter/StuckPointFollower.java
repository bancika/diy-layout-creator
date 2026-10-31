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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.core.IDIYComponent;

/**
 * Keeps stuck control points attached while a selection is rotated or mirrored. It takes a control
 * point map expanded with the stuck components: components with all of their control points in the
 * map are transformed as a whole, while the remaining points, typically the free end of a wire or
 * a lead, are captured before the transformation and moved onto the point they were stuck to
 * afterwards.
 * 
 * @author Branislav Stojkovic
 */
public class StuckPointFollower {

  private final Set<IDIYComponent<?>> transformedComponents = new HashSet<IDIYComponent<?>>();
  private final List<StuckPoint> stuckPoints = new ArrayList<StuckPoint>();

  public StuckPointFollower(Map<IDIYComponent<?>, Set<Integer>> controlPointMap,
      double tolerance) {
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      if (entry.getValue().size() >= entry.getKey().getControlPointCount()) {
        transformedComponents.add(entry.getKey());
      }
    }
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      IDIYComponent<?> component = entry.getKey();
      if (transformedComponents.contains(component)) {
        continue;
      }
      for (Integer index : entry.getValue()) {
        stuckPoints.add(findAnchor(component, index, tolerance));
      }
    }
  }

  public Set<IDIYComponent<?>> getTransformedComponents() {
    return transformedComponents;
  }

  /**
   * Moves every stuck point onto the current location of its anchor. A point that is not stuck
   * directly to a transformed component, only to another stuck point, has no anchor and is mapped
   * through {@code transform} instead.
   */
  public void follow(AffineTransform transform) {
    for (StuckPoint stuckPoint : stuckPoints) {
      Point2D p;
      if (stuckPoint.anchor() == null) {
        p = transform.transform(stuckPoint.originalLocation(), null);
      } else {
        p = new Point2D.Double();
        p.setLocation(stuckPoint.anchor().getControlPoint(stuckPoint.anchorIndex()));
      }
      stuckPoint.component().setControlPoint(p, stuckPoint.index());
    }
  }

  // the anchor is remembered by index rather than by location, so the point stays on the same
  // pin even when a transformer renumbers positions, as mirroring an IC does
  private StuckPoint findAnchor(IDIYComponent<?> component, int index, double tolerance) {
    Point2D location = new Point2D.Double();
    location.setLocation(component.getControlPoint(index));
    IDIYComponent<?> anchor = null;
    int anchorIndex = -1;
    double bestDistance = tolerance;
    for (IDIYComponent<?> candidate : transformedComponents) {
      for (int i = 0; i < candidate.getControlPointCount(); i++) {
        if (candidate.isControlPointSticky(i)) {
          double distance = location.distance(candidate.getControlPoint(i));
          if (distance < bestDistance) {
            bestDistance = distance;
            anchor = candidate;
            anchorIndex = i;
          }
        }
      }
    }
    return new StuckPoint(component, index, location, anchor, anchorIndex);
  }

  private record StuckPoint(IDIYComponent<?> component, int index, Point2D originalLocation,
      IDIYComponent<?> anchor, int anchorIndex) {
  }
}
