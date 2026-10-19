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
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Node;

/**
 * Last resort that reads the direction off pin names, for the many modules and sensors that label
 * their terminals {@code IN+}, {@code OUT-} and the like.
 *
 * <p>
 * It runs after every type aware rule, so a component that one of those recognises is never subject
 * to a name that happens to look directional. Names that carry no direction, such as {@code VCC},
 * {@code GND} or the battery terminals of a charger module, are left neutral, and a component whose
 * pins are merely numbered produces no answer at all.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class NamedPinFlowRule implements ISignalFlowRule {

  @Override
  public FlowHints evaluate(IDIYComponent<?> component, ComponentType type) {
    FlowHints.Builder builder = FlowHints.builder();
    for (int i = 0; i < component.getControlPointCount(); i++) {
      builder.role(i, roleOf(component.getControlPointNodeName(i)));
    }
    FlowHints hints = builder.build();
    return hints.hasDirection() ? hints : null;
  }

  private static FlowRole roleOf(String pinName) {
    String name = Node.sanitizeNodeName(pinName);
    if (name == null) {
      return FlowRole.NEUTRAL;
    }
    String label = name.trim().toUpperCase();
    if (label.equals("IN") || label.startsWith("IN+") || label.startsWith("IN-")
        || label.equals("INPUT")) {
      return FlowRole.INPUT;
    }
    if (label.equals("OUT") || label.startsWith("OUT+") || label.startsWith("OUT-")
        || label.equals("OUTPUT")) {
      return FlowRole.OUTPUT;
    }
    return FlowRole.NEUTRAL;
  }
}
