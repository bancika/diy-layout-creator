package org.diylc.components.guitar;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDynamicPropertySource;
import org.diylc.core.ISwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Freeway3x4_03SwitchPositionPropertyValueSource implements IDynamicPropertySource {

  private List<PropertyWrapper> properties;
  private IDIYComponent<?> component;

  @Override
  public void setProperties(List<PropertyWrapper> properties) {
    this.properties = properties;
  }

  @Override
  public void setComponent(IDIYComponent<?> component) {
    this.component = component;
  }

  @Override
  public List<Object> getAvailableValues() {
    int positionCount = 6;
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
      return "None";
    }
    if (component instanceof ISwitch sw) {
      return sw.getPositionName((Integer)value);
    }
    return "#" + (((Integer)value) + 1);
  }
}
