package org.diylc.core;

public enum BoardUndersideDisplay {
  NONE, ABOVE, BELOW, LEFT, RIGHT;
  
  @Override
  public String toString() {
    return super.toString().charAt(0) + super.toString().substring(1).toLowerCase();
  }
}
