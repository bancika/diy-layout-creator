package org.diylc.presenter;

import java.awt.geom.Point2D;

public class Connection {

  private Point2D p1;
  private Point2D p2;

  public Connection(Point2D p1, Point2D p2) {
    this.p1 = p1.getX() < p2.getX() || p1.getX() == p2.getX() && p1.getY() < p2.getY() ? p1 : p2;
    this.p2 = p1.getX() < p2.getX() || p1.getX() == p2.getX() && p1.getY() < p2.getY() ? p2 : p1;
  }
  
  public Point2D getP1() {
    return p1;
  }
  
  public Point2D getP2() {
    return p2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
    result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
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
    Connection other = (Connection) obj;
    if (p1 == null) {
      if (other.p1 != null)
        return false;
    } else if (!p1.equals(other.p1))
      return false;
    if (p2 == null) {
      if (other.p2 != null)
        return false;
    } else if (!p2.equals(other.p2))
      return false;
    return true;
  }
}
