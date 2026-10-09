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

import java.util.ArrayList;
import java.util.List;

import org.diylc.core.AbstractDynamicPropertyValueSource;
import org.diylc.core.ISwitch;

/**
 * Offers the positions of a switch whose position count does not depend on any editable property,
 * by asking the switch itself. Switches whose count follows a property need their own source, so
 * that the list tracks an edit that has not been applied yet.
 *
 * @author Branislav Stojkovic
 */
public class SwitchPositionPropertyValueSource extends AbstractDynamicPropertyValueSource {

  @Override
  public List<Object> getAvailableValues() {
    if (!(component instanceof ISwitch switchComponent)) {
      return List.of();
    }
    return positions(switchComponent.getPositionCount());
  }

  /** The positions zero to count, preceded by null for "no position selected". */
  static List<Object> positions(int positionCount) {
    List<Object> values = new ArrayList<Object>();
    values.add(null);
    for (int i = 0; i < positionCount; i++) {
      values.add(i);
    }
    return values;
  }
}
