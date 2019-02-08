package org.diylc.graph;

public class Vertex {
  
  private Node node1;
  private Node node2;

  public Vertex() {
  }

  public Node getNode1() {
    return node1;
  }

  public Node getNode2() {
    return node2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((node1 == null) ? 0 : node1.hashCode());
    result = prime * result + ((node2 == null) ? 0 : node2.hashCode());
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
    if (node1 == null) {
      if (other.node1 != null)
        return false;
    } else if (!node1.equals(other.node1))
      return false;
    if (node2 == null) {
      if (other.node2 != null)
        return false;
    } else if (!node2.equals(other.node2))
      return false;
    return true;
  }

  @Override
  public String toString() {   
    return node1.toString() + " -> " + node2.toString();
  }
}
