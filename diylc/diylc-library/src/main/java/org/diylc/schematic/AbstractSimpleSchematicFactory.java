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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.diylc.core.IDIYComponent;

/**
 * Base class for the common case of a single-symbol, straight pin-for-pin mapping between a physical
 * component and its schematic symbol (resistors, capacitors, inductors, diodes, two- and
 * three-terminal parts, ...). The n-th sticky control point of the physical component is mapped to
 * the n-th electrical control point of the symbol.
 *
 * @author Branislav Stojkovic
 */
public abstract class AbstractSimpleSchematicFactory implements ISchematicFactory {

  /** @return a fresh, unconfigured schematic symbol instance. */
  protected abstract IDIYComponent<?> createSymbol();

  /** @return number of electrical (net-carrying) control points to map, in index order. */
  protected int electricalPinCount() {
    return 2;
  }

  @Override
  public List<SchematicSymbolMapping> createSchematicSymbols(IDIYComponent<?> physicalComponent) {
    IDIYComponent<?> symbol = createSymbol();
    symbol.setId(UUID.randomUUID());
    symbol.setName(physicalComponent.getName());
    copyValue(physicalComponent, symbol);

    List<Integer> physicalPins = new ArrayList<Integer>();
    for (int i = 0; i < physicalComponent.getControlPointCount()
        && physicalPins.size() < electricalPinCount(); i++) {
      if (physicalComponent.isControlPointSticky(i)
          && physicalComponent.getControlPointNodeName(i) != null) {
        physicalPins.add(i);
      }
    }

    Map<Integer, Integer> pinMapping = new HashMap<Integer, Integer>();
    for (int schematicIndex = 0; schematicIndex < physicalPins.size()
        && schematicIndex < symbol.getControlPointCount(); schematicIndex++) {
      pinMapping.put(physicalPins.get(schematicIndex), schematicIndex);
    }

    List<SchematicSymbolMapping> result = new ArrayList<SchematicSymbolMapping>();
    result.add(new SchematicSymbolMapping(symbol, pinMapping, null));
    return result;
  }

  @SuppressWarnings("unchecked")
  protected void copyValue(IDIYComponent<?> physical, IDIYComponent<?> symbol) {
    try {
      ((IDIYComponent<Object>) symbol).setValue(physical.getValue());
    } catch (Throwable t) {
      // symbol value type differs from the physical component's; skip
    }
  }
}
