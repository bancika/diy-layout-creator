/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.components;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class RoundedPolygon extends Path2D.Double {

  private static final long serialVersionUID = 1L;

  public RoundedPolygon(Point2D[] points, double[] radiuses) {
    super();
    moveTo(points[0].getX(), points[0].getY());
    for (int i = 0; i < points.length - 1; i++) {
      Point2D first = points[i];
      Point2D second = points[i + 1 >= points.length ? i + 1 - points.length : i + 1];
      Point2D third = points[i + 2 >= points.length ? i + 2 - points.length : i + 2];
      
      Point2D p1 = new Point2D.Double(0, 0);
      Point2D p2 = new Point2D.Double(0, 0);
      locateTwoPoints(first, second, third, radiuses.length <= i ? radiuses[radiuses.length - 1] : radiuses[i], p1, p2);
      lineTo(p1.getX(), p1.getY());
      quadTo(second.getX(), second.getY(), p2.getX(), p2.getY());      
    }
    closePath();
  }
  
  private void locateTwoPoints(Point2D first, Point2D second, Point2D third, double radius, Point2D p1, Point2D p2) {
    double d1 = distance(first, second);
    double d2 = distance(second, third);
    double t1 = 1 - radius / d1;
    double t2 = radius / d2;
    interpolate(first, second, t1, p1);
    interpolate(second, third, t2, p2);
  }
  
  private void interpolate(Point2D p1, Point2D p2, double t, Point2D p) {
    p.setLocation((int)Math.round(p1.getX() * (1-t) + p2.getX() * t), (int)Math.round(p1.getY() * (1-t) + p2.getY() * t));
  }
  
  private double distance(Point2D p1, Point2D p2) {
    return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
  }
}
