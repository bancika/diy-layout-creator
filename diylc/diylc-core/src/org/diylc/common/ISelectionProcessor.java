/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.common;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Size;

public interface ISelectionProcessor {

  /**
   * Returns the current {@link ComponentTransferable}.
   * 
   * @return
   */
  Collection<IDIYComponent<?>> getSelectedComponents();

  /**
   * Updates the selection with the specified list of component. Also, updates control point map
   * with all components that are stuck to the newly selected components.
   * 
   * @param newSelection
   */
  void updateSelection(Collection<IDIYComponent<?>> newSelection);

  /**
   * Finds all components at the specified location, sorted by z-index from top to bottom. Location
   * depends on the current zoom level.
   * 
   * @param point
   * @param includeLocked
   * @return
   */
  List<IDIYComponent<?>> findComponentsAt(Point2D point, boolean includeLocked);

  /**
   * Expands the current selection to include surrounding components. Options are controlled with
   * <code>expansionMode</code> flag.
   * 
   * @param expansionMode
   */
  void expandSelection(ExpansionMode expansionMode);

  /**
   * Selects all components in the project.
   * 
   * @param int layer if > 0, designates which layer to select. If <= 0 we should select all
   *        regardless of layer
   */
  void selectAll(int layer);

  /**
   * Rotates selection for 90 degrees.
   * 
   * @param direction 1 for clockwise, -1 for counter-clockwise
   */
  void rotateSelection(int direction);

  /**
   * Groups all selected components.
   */
  void groupSelectedComponents();

  /**
   * Ungroups all selected components.
   */
  void ungroupSelectedComponents();

  /**
   * Mirrors selected components in the given axis.
   * 
   * @param direction
   */
  void mirrorSelection(int direction);

  /**
   * Returns the minimum rectangle containing all selected components, or null if none exists.
   * Rectangle is scaled by the current zoom level.
   * 
   * @param applyZoom
   * 
   * @return
   */
  Rectangle2D getSelectionBounds(boolean applyZoom);

  /**
   * Moves selection for the specified offset.
   * 
   * @param xOffset
   * @param yOffset
   * @param includeStuckComponents
   */
  void nudgeSelection(Size xOffset, Size yOffset, boolean includeStuckComponents);
  
  /**
   * Selects components that match the criteria.
   * 
   * @param criteria
   */
  void selectMatching(String criteria);
}
