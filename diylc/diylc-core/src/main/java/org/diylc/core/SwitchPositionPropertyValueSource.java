package org.diylc.core;

import java.util.*;

public class SwitchPositionPropertyValueSource implements IDynamicPropertySource {

  private IDIYComponent<?> component;

  @Override
  public void setComponent(IDIYComponent<?> component) {
    this.component = component;
  }

  @Override
  public List<Object> getAvailableValues() {
    if (component == null) {
      throw new RuntimeException("Component not set");
    }
    if (!(component instanceof ISwitch sw)) {
      return List.of();
    }
    int positionCount = sw.getPositionCount();
    List<Object> res = new ArrayList<>();
    res.add(null);
    for (int i = 0; i < positionCount; i++) {
      res.add(i);
    }
    return res;
  }

  @Override
  public String getDisplayValue(Object value) {
    if (value == null) {
      return "";
    }
    return "#" + (((Integer)value) + 1);
  }
}
