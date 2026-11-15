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

import java.util.Set;

import org.diylc.common.ComponentType;
import org.diylc.common.PCBLayer;
import org.diylc.components.composite.CompositeComponent;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ILayeredComponent;
import org.diylc.core.LayerIds;

/**
 * Decides which layer of the Layers menu a component is on, and with that whether it is hidden or
 * locked. A component is on the layer of its rounded z-order, except in the trace band, which is
 * split by board side: copper and other components with a side are on the bottom or top trace
 * layer, a component without a side is on the top one like the rendering assumes, and a component
 * that goes through the board, reporting layer id 0, is on both.
 *
 * @author Branislav Stojkovic
 */
public class ComponentLayers {

  // the component is on both trace layers
  private static final int BOTH_TRACES = -1;
  // internal components without a descriptor are on no layer
  private static final int NONE = 0;

  private ComponentLayers() {}

  /**
   * @return true when every layer the component is on is hidden
   */
  public static boolean isHidden(Set<Integer> hiddenLayers, IDIYComponent<?> component) {
    int layer = getLayer(component);
    if (layer == BOTH_TRACES) {
      return hiddenLayers.contains(LayerIds.BOTTOM_TRACES)
          && hiddenLayers.contains(LayerIds.TOP_TRACES);
    }
    return layer != NONE && hiddenLayers.contains(layer);
  }

  /**
   * @return true when any layer the component is on is locked; moving a component that is on both
   *         trace layers changes the routing of both sides
   */
  public static boolean isLocked(Set<Integer> lockedLayers, IDIYComponent<?> component) {
    int layer = getLayer(component);
    if (layer == BOTH_TRACES) {
      return lockedLayers.contains(LayerIds.BOTTOM_TRACES)
          || lockedLayers.contains(LayerIds.TOP_TRACES);
    }
    return layer != NONE && lockedLayers.contains(layer);
  }

  /**
   * @return true when the component is on the layer, including a component on both trace layers
   *         when the layer is one of them
   */
  public static boolean isOnLayer(IDIYComponent<?> component, int layerId) {
    int layer = getLayer(component);
    if (layer == BOTH_TRACES) {
      return layerId == LayerIds.BOTTOM_TRACES || layerId == LayerIds.TOP_TRACES;
    }
    return layer != NONE && layer == layerId;
  }

  @SuppressWarnings("unchecked")
  private static int getLayer(IDIYComponent<?> component) {
    ComponentProcessor processor = ComponentProcessor.getInstance();
    ComponentType componentType = processor
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
    if (componentType == null) {
      return NONE;
    }
    int layer = (int) Math.round(processor.getZOrder(component));
    if (layer != IDIYComponent.TRACE) {
      return layer;
    }
    // a building block may hold copper of both sides
    if (component instanceof CompositeComponent
        || (component instanceof ILayeredComponent layered && layered.getLayerId() == 0)) {
      return BOTH_TRACES;
    }
    return IBoardSideComponent.sideOf(component) == PCBLayer._1 ? LayerIds.BOTTOM_TRACES
        : LayerIds.TOP_TRACES;
  }
}
