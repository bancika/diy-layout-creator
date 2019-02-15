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
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Vertex v : vertices) {
      sb.append("\t").append(v.getNode1()).append(" - ").append(v.getNode2()).append("\n");
    }
    return sb.toString();
  }
}
