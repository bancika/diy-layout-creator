package org.diylc.graph;

import java.util.HashSet;
import java.util.Set;

public class Graph {
  
  private Set<Vertex> vertices = new HashSet<Vertex>();

  public Graph() {    
  }

  public Set<Vertex> getVertices() {
    return vertices;
  }
  
  public boolean verticesMatch(Graph other) {
    return this.vertices.equals(other.vertices);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((vertices == null) ? 0 : vertices.hashCode());
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
    Graph other = (Graph) obj;
    if (vertices == null) {
      if (other.vertices != null)
        return false;
    } else if (!vertices.equals(other.vertices))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Vertex v : vertices) {
      sb.append("\t").append(v).append("\n");
    }
    return sb.toString();
  }
}
