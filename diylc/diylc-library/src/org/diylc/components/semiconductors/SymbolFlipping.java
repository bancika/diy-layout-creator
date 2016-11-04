package org.diylc.components.semiconductors;

public enum SymbolFlipping {

  NONE, X, Y;

  @Override
  public String toString() {
    switch (this) {
      case Y:
        return "Y-axis";
      case X:
        return "X-axis";
      case NONE:
        return "None";
      default:
        return name();
    }
  }
}
