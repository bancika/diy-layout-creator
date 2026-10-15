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
package org.diylc.schematic.flow;

import org.diylc.common.ComponentType;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.core.IDIYComponent;

/**
 * Treats a guitar pickup as the origin of the signal, so that everything it reaches is downstream of
 * it.
 *
 * <p>
 * This is what gives a guitar wiring diagram a direction at all: those circuits are entirely passive,
 * so no active device is there to say which way the signal runs. Pickups name no pins, so the rule
 * keys on the type and marks every live terminal as an output.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class PickupFlowRule implements ISignalFlowRule {

  @Override
  public FlowHints evaluate(IDIYComponent<?> component, ComponentType type) {
    if (!(component instanceof AbstractGuitarPickup)) {
      return null;
    }
    FlowHints.Builder builder = FlowHints.builder();
    boolean any = false;
    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (component.isControlPointSticky(i)) {
        builder.output(i);
        any = true;
      } else {
        builder.neutral(i);
      }
    }
    return any ? builder.build() : null;
  }
}
