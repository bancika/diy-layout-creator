/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.presenter;

import java.awt.geom.Point2D;

/**
 * Represents a continuity connection between two points.
 * 
 * @author bancika
 */
public class Connection implements Comparable<Connection> {

  private Point2D p1;
  private Point2D p2;
  private int zIndex;

  public Connection(Point2D p1, Point2D p2, int zIndex) {
    this.p1 = p1.getX() < p2.getX() || p1.getX() == p2.getX() && p1.getY() < p2.getY() ? p1 : p2;
    this.p2 = p1.getX() < p2.getX() || p1.getX() == p2.getX() && p1.getY() < p2.getY() ? p2 : p1;
    this.zIndex = zIndex;
  }
  
  public Point2D getP1() {
    return p1;
  }
  
  public Point2D getP2() {
    return p2;
  }
  
  public int getZIndex() {
    return zIndex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
    result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
    result = prime * result + zIndex;
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
    if (zIndex != other.zIndex)
      return false;
    return true;
  }

  @Override
  public int compareTo(Connection o) {
    int compare = Double.compare(p1.getX(), o.p1.getX());
    if (compare != 0)
      return compare;
    compare = Double.compare(p1.getY(), o.p1.getY());
    if (compare != 0)
      return compare;
    compare = Double.compare(p2.getX(), o.p2.getX());
    if (compare != 0)
      return compare;
    compare = Double.compare(p2.getY(), o.p2.getY());
    if (compare != 0)
      return compare;
    
    return 0;
  }
}
