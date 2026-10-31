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

import java.util.HashMap;
import java.util.Map;

import org.diylc.core.IDIYComponent;

/**
 * Describes a single schematic symbol that an {@link ISchematicFactory} spawned from a physical
 * layout component. For multi-section physical components (dual triodes, dual op-amps, etc.) a
 * factory returns more than one of these.
 *
 * @author Branislav Stojkovic
 */
public class SchematicSymbolMapping {

  private IDIYComponent<?> schematicSymbol;
  private Map<Integer, Integer> pinMapping;
  private String sectionLabel;

  public SchematicSymbolMapping() {
    this.pinMapping = new HashMap<Integer, Integer>();
  }

  public SchematicSymbolMapping(IDIYComponent<?> schematicSymbol) {
    this();
    this.schematicSymbol = schematicSymbol;
  }

  public SchematicSymbolMapping(IDIYComponent<?> schematicSymbol, Map<Integer, Integer> pinMapping,
      String sectionLabel) {
    this.schematicSymbol = schematicSymbol;
    this.pinMapping = pinMapping == null ? new HashMap<Integer, Integer>() : pinMapping;
    this.sectionLabel = sectionLabel;
  }

  /**
   * @return the fully configured schematic symbol instance. It only needs to be positioned.
   */
  public IDIYComponent<?> getSchematicSymbol() {
    return schematicSymbol;
  }

  public void setSchematicSymbol(IDIYComponent<?> schematicSymbol) {
    this.schematicSymbol = schematicSymbol;
  }

  /**
   * Maps a physical component control point index to the control point index on
   * {@link #getSchematicSymbol()}. Physical control points that are not present in this map have no
   * counterpart on the symbol and any net attached to them is dropped for this symbol.
   *
   * @return physical control point index -&gt; schematic control point index
   */
  public Map<Integer, Integer> getPinMapping() {
    if (pinMapping == null) {
      pinMapping = new HashMap<Integer, Integer>();
    }
    return pinMapping;
  }

  public void setPinMapping(Map<Integer, Integer> pinMapping) {
    this.pinMapping = pinMapping;
  }

  public SchematicSymbolMapping mapPin(int physicalIndex, int schematicIndex) {
    getPinMapping().put(physicalIndex, schematicIndex);
    return this;
  }

  /**
   * @return an optional label identifying the section of a multi-section component, e.g.
   *         "Section A". May be {@code null}.
   */
  public String getSectionLabel() {
    return sectionLabel;
  }

  public void setSectionLabel(String sectionLabel) {
    this.sectionLabel = sectionLabel;
  }
}
