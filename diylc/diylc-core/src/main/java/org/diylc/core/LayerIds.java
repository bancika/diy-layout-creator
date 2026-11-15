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
package org.diylc.core;

import java.util.Set;

/**
 * Ids of the layers that can be hidden or locked in a project. A layer is a rounded z-order, such
 * as {@link IDIYComponent#BOARD}, except that the traces are split into a bottom and a top layer.
 * <p>
 * The traces used to be one layer, {@link IDIYComponent#TRACE}, and older DIYLC versions still
 * know only that one. Project files therefore keep {@link #LEGACY_TRACES} in a hidden or locked
 * set exactly when both trace layers are in it, so that an older version hides or locks all
 * traces when the new one has both sides hidden or locked.
 *
 * @author Branislav Stojkovic
 */
public final class LayerIds {

  public static final int LEGACY_TRACES = IDIYComponent.TRACE;
  public static final int BOTTOM_TRACES = IDIYComponent.TRACE * 10 + 1;
  public static final int TOP_TRACES = IDIYComponent.TRACE * 10 + 2;

  private LayerIds() {}

  /**
   * Brings a hidden or locked layer set read from a file up to date. The legacy trace layer, from
   * an old file or written by an older version, stands for both trace layers. Both trace layers
   * without it can only mean that an older version showed or unlocked its single trace layer, so
   * neither side is hidden or locked any more.
   */
  public static void migrateLoaded(Set<Integer> layers) {
    if (layers.contains(LEGACY_TRACES)) {
      layers.add(BOTTOM_TRACES);
      layers.add(TOP_TRACES);
    } else if (layers.contains(BOTTOM_TRACES) && layers.contains(TOP_TRACES)) {
      layers.remove(BOTTOM_TRACES);
      layers.remove(TOP_TRACES);
    }
  }

  /**
   * Keeps the legacy trace layer in a hidden or locked set after it has been changed: present
   * exactly when both trace layers are.
   */
  public static void syncLegacy(Set<Integer> layers) {
    if (layers.contains(BOTTOM_TRACES) && layers.contains(TOP_TRACES)) {
      layers.add(LEGACY_TRACES);
    } else {
      layers.remove(LEGACY_TRACES);
    }
  }
}
