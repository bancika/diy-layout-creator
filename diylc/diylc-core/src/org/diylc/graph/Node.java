package org.diylc.graph;

import org.diylc.core.IDIYComponent;


public class Node {

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((component == null) ? 0 : component.hashCode());
    result = prime * result + pointIndex;
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
    Node other = (Node) obj;
    if (component == null) {
      if (other.component != null)
        return false;
    } else if (!component.equals(other.component))
      return false;
    if (pointIndex != other.pointIndex)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return component.getName() + "." + pointIndex + " @ (" + component.getControlPoint(pointIndex).getX() + ":" + component.getControlPoint(pointIndex).getY() + ")";
  }
}
