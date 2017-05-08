package org.diylc.core.measures;

import java.util.ArrayList;

public class MeasureList<T extends Enum<? extends Unit>> extends ArrayList<AbstractMeasure<T>> {

  private static final long serialVersionUID = 1L;

  private String name;
  
  public MeasureList(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
}
