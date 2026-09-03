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

import java.util.List;

import org.diylc.core.IDIYComponent;

/**
 * Strategy that converts a physical layout component into one or more schematic symbols.
 *
 * <p>
 * Implementations are declared per component type through
 * {@code @ComponentDescriptor(schematicFactory = ...)}. When a type declares no factory, the
 * schematic generator falls back to a generic box symbol.
 * </p>
 *
 * <p>
 * Implementations must have a public no-argument constructor. They are instantiated once and reused,
 * so they must be stateless.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public interface ISchematicFactory {

  /**
   * Creates fully instantiated and configured schematic symbol instances for the given physical
   * component. The returned symbols only need to be positioned by the caller.
   *
   * @param physicalComponent the physical layout component
   * @return list of {@link SchematicSymbolMapping} entries, one per schematic symbol (1:1 for simple
   *         components, 1:N for multi-section components). An empty list means the component produces
   *         no symbol and should be treated as excluded.
   */
  List<SchematicSymbolMapping> createSchematicSymbols(IDIYComponent<?> physicalComponent);
}
