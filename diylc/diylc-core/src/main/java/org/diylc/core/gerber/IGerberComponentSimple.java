package org.diylc.core.gerber;

import org.diylc.common.PCBLayer;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;

public interface IGerberComponentSimple extends IGerberComponent {

  /**
   * @return the silkscreen on the side of the board the component is mounted on
   */
  default GerberLayer getGerberLayer() {
    return this instanceof IDIYComponent<?> component
        && IBoardSideComponent.sideOf(component) == PCBLayer._1
            ? GerberLayer.SilkscreenBot : GerberLayer.SilkscreenTop;
  }

  default String getGerberFunction() {
    return "ComponentOutline,Footprint";
  }

  default boolean isGerberNegative() {
    return false;
  }
}
