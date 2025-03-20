package org.diylc.core.gerber;

public interface IGerberComponentSimple extends IGerberComponent {
  
  default GerberLayer getGerberLayer() {
    return GerberLayer.SilkscreenTop;
  }

  default String getGerberFunction() {
    return "ComponentOutline,Footprint";
  }

  default boolean isGerberNegative() {
    return false;
  }
}
