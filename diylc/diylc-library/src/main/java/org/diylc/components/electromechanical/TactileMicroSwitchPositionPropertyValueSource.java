package org.diylc.components.electromechanical;

import org.diylc.common.PropertyWrapper;
import org.diylc.components.guitar.LeverSwitch;
import org.diylc.core.AbstractDynamicPropertyValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TactileMicroSwitchPositionPropertyValueSource extends AbstractDynamicPropertyValueSource {

  @Override
  public List<Object> getAvailableValues() {
    return List.of("Released", "Pressed");
  }
}
