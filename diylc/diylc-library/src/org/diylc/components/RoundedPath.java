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
