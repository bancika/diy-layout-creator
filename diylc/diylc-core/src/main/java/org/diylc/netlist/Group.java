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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.diylc.core.IDIYComponent;

/**
 * Represents a group of {@link Node}s that are connected together.
 * 
 * @author Branislav Stojkovic
 */
public class Group implements Comparable<Group> {
  
  private Set<Node> nodes = new HashSet<Node>();
  
  public Group() {
    
  }
  
  public Group(Collection<Node> nodes) {
    this.nodes = new HashSet<Node>(nodes);
  }

  public Group(Node node1, Node node2) {
    nodes.add(node1);
    nodes.add(node2);
  }
  
  public Group connect(IDIYComponent<?> component, int index) {
    return connect(component, index, 0);
  }
  
  public Group connect(IDIYComponent<?> component, int index, int zOrder) {
    getNodes().add(new Node(component, index, zOrder));
    return this;
  }

  public Set<Node> getNodes() {
    return nodes;
  }
  
  public List<Node> getSortedNodes() {
    List<Node> list = new ArrayList<Node>(nodes);
    Collections.sort(list);
    return list;
  }
 
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
    Group other = (Group) obj;
    if (nodes == null) {
      if (other.nodes != null)
        return false;
    } else if (!nodes.equals(other.nodes))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    List<Node> list = getSortedNodes();
    for (Node n : list) {
      if (sb.length() > 0)
        sb.append(" <-> ");
      sb.append(n);
    }
    return sb.toString();
  }

  @Override
  public int compareTo(Group o) {
    return toString().compareToIgnoreCase(o.toString());
  }
  
  @Override
  public Group clone() {
    Group g = new Group();
    g.nodes.addAll(nodes);
    return g;
  }
}
