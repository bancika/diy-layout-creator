package org.diylc.components;

import java.awt.geom.GeneralPath;

public class RoundedPath {

  private GeneralPath path;

  private double prevX = Double.NaN;
  private double prevY = Double.NaN;
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
    this.prevX = Double.NaN;
    this.prevY = Double.NaN;
  }

  public final void lineTo(double x, double y) {
    if (Double.isNaN(prevX)) {
      double theta = Math.atan2(y - this.y, x - this.x);
      double r = Math.sqrt((y - this.y) * (y - this.y) + (x - this.x) * (x - this.x));
      path.lineTo(this.x + Math.cos(theta) * (r - radius), this.y + Math.sin(theta) * (r - radius));      
    } else {
      double theta = Math.atan2(y - this.y, x - this.x);      
      path.curveTo(this.x, this.y, this.x, this.y, this.x + Math.cos(theta) * radius, this.y + Math.sin(theta) * radius);
    }
    
    this.prevX = this.x;
    this.prevY = this.y;
    this.x = x;
    this.y = y;
  }
  
  public GeneralPath getPath() {
    return path;
  }
}
