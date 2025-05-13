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

import java.awt.geom.Point2D;
import java.util.Objects;

import org.diylc.core.ICommonNode;
import org.diylc.core.IDIYComponent;

/**
 * Represents a single node in a {@link Netlist}, uniquely defined by a component and a control point.
 * 
 * @author Branislav Stojkovic
 */
public class Node implements Comparable<Node> {

  private IDIYComponent<?> component;
  private int pointIndex;

  public Node(IDIYComponent<?> component, int pointIndex) {
    super();
    this.component = component;
    this.pointIndex = pointIndex;
  }

  public IDIYComponent<?> getComponent() {
    return component;
  }

  public int getPointIndex() {
    return pointIndex;
  }
  
  public String getDisplayName() {
    String[] section = component.getSectionNames(pointIndex);
    return (section == null || section.length > 1 ? "" : section[0] + ".") + component.getControlPointNodeName(pointIndex);
  }
  
  public Point2D getPoint2D() {
    return component.getControlPoint(pointIndex);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Node)) return false;
    Node other = (Node) obj;
    return Objects.equals(component, other.component) && pointIndex == other.pointIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(component, pointIndex);
  }

  @Override
  public String toString() {
    if (component instanceof ICommonNode) {
      return ((ICommonNode)component).getCommonNodeLabel();
    }
    if (component.getControlPointCount() == 1)
      return component.getName();
    return component.getName() + "." + getDisplayName() /*+ " @ (" + component.getControlPoint(pointIndex).getX() + ":" + component.getControlPoint(pointIndex).getY() + ")"*/;
  }

  @Override
  public int compareTo(Node o) {
    return toString().compareToIgnoreCase(o.toString());
  }
}
