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
package org.diylc.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

/**
 * Schematic representation of a physical layout {@link Project}. It is derived from the layout via
 * the netlist infrastructure and rendered using the regular DIYLC drawing pipeline. It is embedded
 * in {@link Project} and serialized alongside the rest of the project data, but only after the user
 * opens the schematic view for the first time (the field stays {@code null} until then, keeping
 * files unchanged for users who never use the feature).
 *
 * @author Branislav Stojkovic
 */
public class SchematicView implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_WIDTH = new Size(42d, SizeUnit.cm);
  public static Size DEFAULT_HEIGHT = new Size(30d, SizeUnit.cm);
  public static Size DEFAULT_GRID_SPACING = new Size(0.1d, SizeUnit.in);

  /** Schematic components: symbols and auto-routed wires, sorted by z-order ascending. */
  private List<IDIYComponent<?>> components;

  /**
   * Maps a physical component id ({@link IDIYComponent#getId()}) to the ids of the schematic
   * symbol(s) that were spawned from it. The relation is 1:N to support multi-section components
   * such as dual triodes or dual op-amps.
   */
  private Map<UUID, List<UUID>> physicalToSchematicMap;

  private Size width;
  private Size height;
  private Size gridSpacing;

  public SchematicView() {
    this.components = new ArrayList<IDIYComponent<?>>();
    this.physicalToSchematicMap = new LinkedHashMap<UUID, List<UUID>>();
    this.width = DEFAULT_WIDTH;
    this.height = DEFAULT_HEIGHT;
    this.gridSpacing = DEFAULT_GRID_SPACING;
  }

  public List<IDIYComponent<?>> getComponents() {
    if (components == null) {
      components = new ArrayList<IDIYComponent<?>>();
    }
    return components;
  }

  public void setComponents(List<IDIYComponent<?>> components) {
    this.components = components;
  }

  public Map<UUID, List<UUID>> getPhysicalToSchematicMap() {
    if (physicalToSchematicMap == null) {
      physicalToSchematicMap = new LinkedHashMap<UUID, List<UUID>>();
    }
    return physicalToSchematicMap;
  }

  public void setPhysicalToSchematicMap(Map<UUID, List<UUID>> physicalToSchematicMap) {
    this.physicalToSchematicMap = physicalToSchematicMap;
  }

  public Size getWidth() {
    return width == null ? DEFAULT_WIDTH : width;
  }

  public void setWidth(Size width) {
    this.width = width;
  }

  public Size getHeight() {
    return height == null ? DEFAULT_HEIGHT : height;
  }

  public void setHeight(Size height) {
    this.height = height;
  }

  public Size getGridSpacing() {
    return gridSpacing == null ? DEFAULT_GRID_SPACING : gridSpacing;
  }

  public void setGridSpacing(Size gridSpacing) {
    this.gridSpacing = gridSpacing;
  }

  /**
   * @return {@code true} if the schematic has already been generated at least once.
   */
  public boolean isGenerated() {
    return components != null && !components.isEmpty();
  }

  @Override
  public SchematicView clone() {
    SchematicView clone = new SchematicView();
    clone.setWidth(this.getWidth());
    clone.setHeight(this.getHeight());
    clone.setGridSpacing(this.getGridSpacing());
    for (IDIYComponent<?> component : this.getComponents()) {
      try {
        clone.getComponents().add(component.clone());
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
    for (Map.Entry<UUID, List<UUID>> entry : this.getPhysicalToSchematicMap().entrySet()) {
      clone.getPhysicalToSchematicMap().put(entry.getKey(), new ArrayList<UUID>(entry.getValue()));
    }
    return clone;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((components == null) ? 0 : components.hashCode());
    result = prime * result + ((width == null) ? 0 : width.hashCode());
    result = prime * result + ((height == null) ? 0 : height.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    SchematicView other = (SchematicView) obj;
    if (components == null) {
      if (other.components != null)
        return false;
    } else if (other.components == null || components.size() != other.components.size()) {
      return false;
    } else {
      for (int i = 0; i < components.size(); i++) {
        if (!components.get(i).equalsTo(other.components.get(i)))
          return false;
      }
    }
    if (width == null ? other.width != null : !width.equals(other.width))
      return false;
    if (height == null ? other.height != null : !height.equals(other.height))
      return false;
    if (gridSpacing == null ? other.gridSpacing != null : !gridSpacing.equals(other.gridSpacing))
      return false;
    return new HashMap<UUID, List<UUID>>(getPhysicalToSchematicMap())
        .equals(new HashMap<UUID, List<UUID>>(other.getPhysicalToSchematicMap()));
  }
}
