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
package org.diylc.netlist;

import java.util.List;

import org.diylc.core.IDIYComponent;

public class TreeLeaf implements ITree {

  private IDIYComponent<?> component;
  private int pointIndex1;
  private int pointIndex2;

  public TreeLeaf(IDIYComponent<?> component, int pointIndex1, int pointIndex2) {
    this.component = component;
    this.pointIndex1 = pointIndex1;
    this.pointIndex2 = pointIndex2;
  }

  public IDIYComponent<?> getComponent() {
    return component;
  }

  public int getPointIndex1() {
    return pointIndex1;
  }

  public int getPointIndex2() {
    return pointIndex2;
  }

  @Override
  public List<ITree> getChildren() {
    return null;
  }

  @Override
  public TreeConnectionType getConnectionType() {
    return null;
  }  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((component == null) ? 0 : component.hashCode());
    result = prime * result + pointIndex1;
    result = prime * result + pointIndex2;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TreeLeaf other = (TreeLeaf) obj;
    if (component == null) {
      if (other.component != null)
        return false;
    } else if (!component.equals(other.component))
      return false;
    if (pointIndex1 != other.pointIndex1)
      return false;
    if (pointIndex2 != other.pointIndex2)
      return false;
    return true;
  }

  @Override
  public String toString() {   
    return component.getName() + "." + component.getInternalLinkName(pointIndex1, pointIndex2);
  }
  
  public String toHTML() {
    return toString().replace("<", "&lt;").replace(">", "&gt");
  }
}
