package org.diylc.graph;

import java.util.List;

public class GraphKey {
  
  private List<Position> positions;

  public GraphKey(List<Position> positions) {
    super();
    this.positions = positions;
  }

  public List<Position> getPositions() {
    return positions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((positions == null) ? 0 : positions.hashCode());
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
    GraphKey other = (GraphKey) obj;
    if (positions == null) {
      if (other.positions != null)
        return false;
    } else if (!positions.equals(other.positions))
      return false;
    return true;
  }
  
  @Override
  public String toString() {   
    StringBuilder sb = new StringBuilder();
    for (Position s : positions)
    {
      if (sb.length() > 0)
        sb.append(":");
      sb.append(s.toString());      
    }
    return sb.toString();
  }
}
