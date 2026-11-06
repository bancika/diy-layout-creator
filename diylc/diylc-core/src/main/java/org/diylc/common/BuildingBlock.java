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
package org.diylc.common;

import java.time.LocalDateTime;
import java.util.Collection;

import org.diylc.core.IDIYComponent;

public class BuildingBlock {

  private String name;
  private Collection<IDIYComponent<?>> components;
  // the dates are set only on factory blocks in import-defaults/blocks.xml, and a withdrawn
  // factory block carries nothing but its name and deletedOn
  private LocalDateTime createdOn;
  private LocalDateTime deletedOn;

  public BuildingBlock(String name, Collection<IDIYComponent<?>> components) {
    super();
    this.name = name;
    this.components = components;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<IDIYComponent<?>> getComponents() {
    return components;
  }

  public void setComponents(Collection<IDIYComponent<?>> components) {
    this.components = components;
  }

  public LocalDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public LocalDateTime getDeletedOn() {
    return deletedOn;
  }

  public void setDeletedOn(LocalDateTime deletedOn) {
    this.deletedOn = deletedOn;
  }

  @Override
  public String toString() {
    return name;
  }
}
