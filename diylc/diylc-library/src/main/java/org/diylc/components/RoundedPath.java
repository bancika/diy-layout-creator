/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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

import java.awt.geom.GeneralPath;

/***
 * 
 * Creates a {@link GeneralPath} with rounded edges. To create a rounded path, follow these steps:
 * 
 * <ul>
 * <li>instantiate a {@link RoundedPath}</li>
 * <li>place a starting point using {@link RoundedPath#moveTo(double, double)}</li>
 * <li>create a polygon using a series of {@link RoundedPath#lineTo(double, double)} calls</li>
 * <li>call {@link RoundedPath#getPath()} to get a rounded path</li>
 * </ul>
 * 
 * Note that the starting point shouldn't lie on a corner of the polygon if you want that corner to
 * get rounded too. Instead, place the starting/ending point on one of the polygon sides.
 * 
 * @author bancika
 */
public class RoundedPath {

  private GeneralPath path;

  boolean isFirst = true;
  private double x = Double.NaN;
  private double y = Double.NaN;

  private double radius;

  public RoundedPath(double radius) {
    this.radius = radius;
    path = new GeneralPath();
  }

  public final void moveTo(double x, double y) {
    path.moveTo(x, y);
    this.x = x;
    this.y = y;
    isFirst = true;
  }

  public final void lineTo(double x, double y) {
    if (isFirst) {
      double theta = Math.atan2(y - this.y, x - this.x);
      double r = Math.sqrt((y - this.y) * (y - this.y) + (x - this.x) * (x - this.x));
      path.lineTo(this.x + Math.cos(theta) * (r - radius), this.y + Math.sin(theta) * (r - radius));
    } else {
      double theta = Math.atan2(y - this.y, x - this.x);
      path.curveTo(this.x, this.y, this.x, this.y, this.x + Math.cos(theta) * radius, this.y + Math.sin(theta) * radius);
    }

    isFirst = false;
    this.x = x;
    this.y = y;
  }

  public GeneralPath getPath() {
    return path;
  }
}
