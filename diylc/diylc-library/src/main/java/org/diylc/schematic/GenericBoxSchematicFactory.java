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
package org.diylc.schematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.common.Display;
import org.diylc.components.schematic.SchematicBox;
import org.diylc.core.IDIYComponent;

/**
 * Default {@link ISchematicFactory} used for every physical component type that does not declare a
 * dedicated factory. It inspects the sticky control points of the component, creates a
 * {@link SchematicBox} with one labeled terminal per node and copies the component's name and value.
 *
 * @author Branislav Stojkovic
 */
public class GenericBoxSchematicFactory implements ISchematicFactory {

  @Override
  public List<SchematicSymbolMapping> createSchematicSymbols(IDIYComponent<?> physicalComponent) {
    List<Integer> pinIndices = new ArrayList<Integer>();
    List<String> labels = new ArrayList<String>();
    for (int i = 0; i < physicalComponent.getControlPointCount(); i++) {
      if (!physicalComponent.isControlPointSticky(i)) {
        continue;
      }
      String nodeName = physicalComponent.getControlPointNodeName(i);
      if (nodeName == null) {
        continue;
      }
      pinIndices.add(i);
      labels.add(nodeName);
    }
    if (pinIndices.isEmpty()) {
      return Collections.emptyList();
    }

    SchematicBox box = new SchematicBox();
    box.setName(physicalComponent.getName());
    String value = physicalComponent.getValueForDisplay();
    box.setValue(value == null ? "" : value);
    box.setDisplay(Display.BOTH);
    box.setTopNodes("");
    box.setBottomNodes("");

    int half = (labels.size() + 1) / 2;
    List<String> left = labels.subList(0, half);
    List<String> right = labels.subList(half, labels.size());
    box.setLeftNodes(String.join(",", left));
    box.setRightNodes(String.join(",", right));

    // SchematicBox control point order is left (top to bottom) then right (top to bottom), which is
    // exactly the order we appended the labels in, so the k-th physical pin maps to schematic
    // control point k.
    Map<Integer, Integer> pinMapping = new HashMap<Integer, Integer>();
    for (int k = 0; k < pinIndices.size(); k++) {
      pinMapping.put(pinIndices.get(k), k);
    }

    List<SchematicSymbolMapping> result = new ArrayList<SchematicSymbolMapping>();
    result.add(new SchematicSymbolMapping(box, pinMapping, null));
    return result;
  }
}
