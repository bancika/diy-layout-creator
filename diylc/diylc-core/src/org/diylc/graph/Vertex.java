package org.diylc.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Vertex {
  
  private Set<Node> nodes = new HashSet<Node>();

  public Vertex(Node node1, Node node2) {
    nodes.add(node1);
    nodes.add(node2);
  }

  public Node getNode1() {
    Iterator<Node> i = nodes.iterator();
    return i.next();
  }

  public Node getNode2() {
    Iterator<Node> i = nodes.iterator();
    i.next();
    return i.next();
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
    Vertex other = (Vertex) obj;
    if (nodes == null) {
      if (other.nodes != null)
        return false;
    } else if (!nodes.equals(other.nodes))
      return false;
    return true;
  }

  @Override
  public String toString() {   
    return getNode1().toString() + " <-> " + getNode2().toString();
  }
}
