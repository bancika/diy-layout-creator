package org.diylc.graph;

import java.util.Dictionary;
import java.util.Set;

public class Graph {
  
  private Dictionary<String, Position> positions;
  private Set<Node> nodes;

  public Graph() {
  }

  public Dictionary<String, Position> getPositions() {
    return positions;
  }

  public Set<Node> getNodes() {
    return nodes;
  }
  
  public boolean nodesMatch(Graph other) {
    return this.nodes.equals(other.nodes);
  }
}
