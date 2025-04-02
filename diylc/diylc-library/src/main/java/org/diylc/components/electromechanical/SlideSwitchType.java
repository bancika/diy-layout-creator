package org.diylc.components.electromechanical;

public enum SlideSwitchType {

  SPDT(2), DPDT(2), DP3T(3);

  private final int positionCount;

  SlideSwitchType(int positionCount) {
    this.positionCount = positionCount;
  }

  public int getPositionCount() {
    return positionCount;
  }
}
