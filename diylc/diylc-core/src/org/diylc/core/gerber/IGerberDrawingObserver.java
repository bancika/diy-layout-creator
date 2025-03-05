package org.diylc.core.gerber;

public interface IGerberDrawingObserver {

  void startGerberOutput(GerberLayer layer, String function, boolean negative);

  void setGerberFunction(GerberLayer layer, String function);

  void setGerberNegative(GerberLayer layer, boolean negative);

  void stopGerberOutput(GerberLayer layer);

  void setApproximationToleranceOverride(double tolerance);

  void stopGerberOutput();
}
