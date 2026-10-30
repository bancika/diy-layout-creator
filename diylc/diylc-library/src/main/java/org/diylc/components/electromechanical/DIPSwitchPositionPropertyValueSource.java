/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.electromechanical;

import java.util.List;
import java.util.Optional;

import org.diylc.common.PropertyWrapper;
import org.diylc.components.electromechanical.DIPSwitch.SwitchCount;
import org.diylc.core.AbstractDynamicPropertyValueSource;

/**
 * A DIP switch position is one combination of all its switches, so the list grows as two to the
 * power of the switch count.
 */
public class DIPSwitchPositionPropertyValueSource extends AbstractDynamicPropertyValueSource {

  @Override
  public List<Object> getAvailableValues() {
    if (properties == null) {
      return List.of();
    }
    Optional<PropertyWrapper> optionalPropertyWrapper = this.properties.stream().filter(p -> {
      try {
        return p.getGetter().getName().equals("getSwitchCount");
      } catch (NoSuchMethodException e) {
        return false;
      }
    }).findFirst();
    if (optionalPropertyWrapper.isEmpty()) {
      return List.of();
    }
    SwitchCount switchCount = (SwitchCount) optionalPropertyWrapper.get().getValue();
    return SwitchPositionPropertyValueSource.positions((int) Math.pow(2, switchCount.getValue()));
  }
}
