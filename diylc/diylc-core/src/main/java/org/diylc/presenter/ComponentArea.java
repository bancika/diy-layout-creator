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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * What a component drew while its areas were being recorded: the shapes making up its outline and
 * its continuity areas, and the bounds of everything it drew. The shapes are merged into areas only
 * the first time an area is asked for, because recording happens on every drag step for the
 * components being moved, while the merged areas are needed far less often.
 */
public class ComponentArea implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private int zOrder;
  private List<Shape> outlineParts;
  // one list per continuity area; its shapes are merged into that area
  private Collection<List<Shape>> continuityPositiveParts;
  private Collection<List<Shape>> continuityNegativeParts;
  // everything the component drew, including parts left out of the outline area such as labels;
  // null when it was not recorded
  private Rectangle2D drawnBounds;

  private transient Rectangle2D outlineBounds;
  private transient Area outlineArea;
  private transient Collection<Area> continuityPositiveAreas;
  private transient Collection<Area> continuityNegativeAreas;
  
  public ComponentArea() {   
  }

  public ComponentArea(int zOrder, List<Shape> outlineParts,
      Collection<List<Shape>> continuityPositiveParts,
      Collection<List<Shape>> continuityNegativeParts, Rectangle2D drawnBounds) {
    super();
    this.zOrder = zOrder;
    this.outlineParts = outlineParts;
    this.continuityPositiveParts = continuityPositiveParts;
    this.continuityNegativeParts = continuityNegativeParts;
    this.drawnBounds = drawnBounds;
  }
  
  public int getZOrder() {
    return zOrder;
  }

  public boolean hasOutline() {
    return !outlineParts.isEmpty();
  }

  /**
   * @return bounds of the outline area, worked out from the recorded shapes without merging them.
   *         A new rectangle on every call.
   */
  public synchronized Rectangle2D getOutlineBounds() {
    // kept, because the cursor check asks every component on every mouse move
    if (outlineBounds == null) {
      Rectangle2D bounds = new Rectangle2D.Double();
      for (int i = 0; i < outlineParts.size(); i++) {
        if (i == 0) {
          bounds = outlineParts.get(i).getBounds2D();
        } else {
          bounds.add(outlineParts.get(i).getBounds2D());
        }
      }
      outlineBounds = bounds;
    }
    return (Rectangle2D) outlineBounds.clone();
  }

  // synchronized because the netlist may be built away from the EDT
  public synchronized Area getOutlineArea() {
    if (outlineArea == null) {
      outlineArea = merge(outlineParts);
    }
    return outlineArea;
  }

  public synchronized Collection<Area> getContinuityPositiveAreas() {
    if (continuityPositiveAreas == null) {
      continuityPositiveAreas = mergeEach(continuityPositiveParts);
    }
    return continuityPositiveAreas;
  }

  public synchronized Collection<Area> getContinuityNegativeAreas() {
    if (continuityNegativeAreas == null) {
      continuityNegativeAreas = mergeEach(continuityNegativeParts);
    }
    return continuityNegativeAreas;
  }

  public Rectangle2D getDrawnBounds() {
    return drawnBounds;
  }

  private static Area merge(List<Shape> parts) {
    List<Area> areas = new ArrayList<Area>(parts.size());
    for (Shape part : parts) {
      // a new Area each time, because merging changes the areas it is given
      areas.add(new Area(part));
    }
    return AreaUtils.union(areas);
  }

  private static Collection<Area> mergeEach(Collection<List<Shape>> parts) {
    List<Area> areas = new ArrayList<Area>(parts.size());
    for (List<Shape> area : parts) {
      areas.add(merge(area));
    }
    return areas;
  }
}
