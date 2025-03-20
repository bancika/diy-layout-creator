/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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

import java.util.Collection;

import org.diylc.core.IDIYComponent;

public class BuildingBlock {

  private String name;
  private Collection<IDIYComponent<?>> components;

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

  @Override
  public String toString() {
    return name;
  }
}
