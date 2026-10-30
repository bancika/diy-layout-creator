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
package org.diylc.plugins.chatbot.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Node;
import org.diylc.plugins.chatbot.model.AiEditOperation;

/**
 * Swaps a component for one of another type and carries the wire ends that sit on its terminals
 * over to the new ones, so the wiring keeps its routing and colours. A wire with an end on a
 * terminal the pin map leaves out has nowhere to go and is removed.
 */
class ReplaceHandler implements IAiEditHandler {

  // the one property a replace carries over unasked, so the new part faces the way the old one did
  private static final String ORIENTATION = "Orientation";

  @Override
  public void apply(AiEditOperation op, AiEditContext context) throws Exception {
    IDIYComponent<?> old = context.findExactComponent(op.getComponentName());
    if (old == null) {
      context.warn("Cannot replace component '" + op.getComponentName() + "' (not found).");
      return;
    }
    ComponentType ct = context.findComponentType(op.getComponentType());
    if (ct == null) {
      context.warn("Cannot replace '" + op.getComponentName() + "' (unknown type: "
          + op.getComponentType() + " - consider updating DIYLC).");
      return;
    }

    // collected before anything moves, while the wire ends still sit on the old terminals
    Set<Node> wireEnds = AiProjectBuilder.getWireEnds(context.getProject());
    List<AttachedEnd> attachedEnds = new ArrayList<>();
    for (IDIYComponent<?> c : context.getProject().getComponents()) {
      if (c == old) {
        continue;
      }
      for (int i = 0; i < c.getControlPointCount(); i++) {
        int pin = WireGeometry.findStuckPin(c, i, old);
        if (pin < 0) {
          continue;
        }
        if (wireEnds.contains(new Node(c, i))) {
          attachedEnds.add(new AttachedEnd(c, i, pin));
        } else {
          context.warn("'" + c.getName() + "' touches " + op.getComponentName() + "." + pin
              + " directly and was left where it is.");
        }
      }
    }

    AiPropertyApplier propertyApplier = context.getPropertyApplier();
    IDIYComponent<?> replacement = context.instantiate(ct, old.getName());
    if (op.getProperties() == null || op.getProperties().keySet().stream()
        .noneMatch(ORIENTATION::equalsIgnoreCase)) {
      propertyApplier.copy(ORIENTATION, old, replacement);
    }
    propertyApplier.apply(replacement, op.getProperties());
    propertyApplier.applyDatasheetModel(replacement, op.getDatasheetModel());
    WireGeometry.moveTo(replacement, old.getControlPoint(0));
    context.substitute(old, replacement);

    Map<Integer, Integer> pinMap = op.getPinMap() == null || op.getPinMap().isEmpty()
        ? matchPinsByLabel(old, replacement) : parsePinMap(op, replacement, context);

    // a wire goes as soon as one of its ends is unmapped, even if the other one could be moved
    Map<IDIYComponent<?>, Integer> unmappedWires = new LinkedHashMap<>();
    for (AttachedEnd end : attachedEnds) {
      if (!pinMap.containsKey(end.oldPin())) {
        unmappedWires.putIfAbsent(end.wire(), end.oldPin());
      }
    }
    for (Map.Entry<IDIYComponent<?>, Integer> entry : unmappedWires.entrySet()) {
      context.removeComponent(entry.getKey());
      context.warn("Removed '" + entry.getKey().getName() + "', attached to "
          + op.getComponentName() + "." + entry.getValue()
          + ", which has no counterpart on the new component.");
    }
    for (AttachedEnd end : attachedEnds) {
      if (!unmappedWires.containsKey(end.wire())) {
        WireGeometry.moveWireEnd(end.wire(), end.pointIndex(),
            replacement.getControlPoint(pinMap.get(end.oldPin())));
      }
    }
  }

  private static Map<Integer, Integer> parsePinMap(AiEditOperation op,
      IDIYComponent<?> replacement, AiEditContext context) {
    Map<Integer, Integer> pinMap = new HashMap<>();
    for (Map.Entry<String, String> entry : op.getPinMap().entrySet()) {
      int oldPin = AiEditContext.parseIndex(entry.getKey());
      int newPin = AiEditContext.parseIndex(entry.getValue());
      if (oldPin < 0) {
        context.warn("Ignoring pinMap entry '" + entry.getKey() + "' of "
            + op.getComponentName() + " (not a terminal index).");
      } else if (newPin < 0 || newPin >= replacement.getControlPointCount()
          || !replacement.isControlPointSticky(newPin)) {
        context.warn("pinMap of " + op.getComponentName() + " maps terminal " + oldPin + " to '"
            + entry.getValue() + "', which the new component does not have.");
      } else {
        pinMap.put(oldPin, newPin);
      }
    }
    return pinMap;
  }

  /**
   * Pairs the terminals of two components by their names, for a replace that came without a pin
   * map. Terminals without a name are left unpaired.
   */
  private static Map<Integer, Integer> matchPinsByLabel(IDIYComponent<?> old,
      IDIYComponent<?> replacement) {
    Map<Integer, Integer> pinMap = new HashMap<>();
    for (int oldPin = 0; oldPin < old.getControlPointCount(); oldPin++) {
      String label = getPinLabel(old, oldPin);
      if (label == null) {
        continue;
      }
      for (int newPin = 0; newPin < replacement.getControlPointCount(); newPin++) {
        if (label.equals(getPinLabel(replacement, newPin))) {
          pinMap.put(oldPin, newPin);
          break;
        }
      }
    }
    return pinMap;
  }

  private static String getPinLabel(IDIYComponent<?> component, int index) {
    if (!component.isControlPointSticky(index)) {
      return null;
    }
    String name = component.getControlPointNodeName(index);
    return name == null || name.isBlank() ? null : name.trim().toLowerCase();
  }

  /**
   * A wire end found on a terminal of a component that is about to be replaced.
   */
  private record AttachedEnd(IDIYComponent<?> wire, int pointIndex, int oldPin) {
  }
}
