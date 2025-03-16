package org.diylc.core;

import org.diylc.common.PropertyWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDynamicPropertyValueSource implements IDynamicPropertySource {

  protected List<PropertyWrapper> properties;
  protected IDIYComponent<?> component;

  @Override
  public void setProperties(List<PropertyWrapper> properties) {
    this.properties = properties;
  }

  @Override
  public void setComponent(IDIYComponent<?> component) {
    this.component = component;
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
