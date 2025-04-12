package org.diylc.components.guitar;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.AbstractDynamicPropertyValueSource;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDynamicPropertySource;
import org.diylc.core.ISwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Freeway3x4_03SwitchPositionPropertyValueSource extends
    AbstractDynamicPropertyValueSource {

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
}
