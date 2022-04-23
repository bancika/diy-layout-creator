package org.diylc.components.electromechanical;

public enum SlideSwitchType {

  SPDT, DPDT, DP3T;

  @Override
  public String toString() {
    String name = name();
    if (name.startsWith("_"))
      name = name.substring(1);
    name = name.replace("_", " ");
    name = name.replace("mustang", "");
    name = name.replace("off", " (Center OFF)");
    return name;
  }
}