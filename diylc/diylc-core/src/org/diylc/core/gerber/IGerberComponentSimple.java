package org.diylc.core.gerber;

public interface IGerberComponentSimple extends IGerberComponent {
  
  GerberLayer getGerberLayer();
  
  String getGerberFunction();
  
  boolean isGerberNegative();
}
