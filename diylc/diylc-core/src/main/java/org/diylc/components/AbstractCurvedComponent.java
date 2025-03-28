/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
package org.diylc.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;

import com.google.common.collect.Streams;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.diylc.common.IPlugInPort;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractCurvedComponent<T> extends AbstractTransparentComponent<T> {

  private static final long serialVersionUID = 1L;
  
  private static final Logger LOG = Logger.getLogger(AbstractCurvedComponent.class);

  public static Color GUIDELINE_COLOR = Color.blue;
  public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

  // for backward compatibility
  protected Point2D[] controlPoints = null;

  protected Point2D[] controlPoints2 = new Point2D[] {new Point2D.Double(0, 0),
      new Point2D.Double(DEFAULT_SIZE.convertToPixels() / 2, 0),
      new Point2D.Double(DEFAULT_SIZE.convertToPixels() / 2, DEFAULT_SIZE.convertToPixels()),
      new Point2D.Double(DEFAULT_SIZE.convertToPixels(), DEFAULT_SIZE.convertToPixels())};

  protected Color color = getDefaultColor();
  protected PointCount pointCount = PointCount.FOUR;
  protected LineStyle style = LineStyle.SOLID;
  protected Boolean smooth = true;

  private int lastUpdatePointIndex = -1;

  /**
   * Draws the specified curve onto graphics.
   * 
   * @param curve
   * @param g2d
   * @param componentState
   * @param drawingObserver
   */
  protected abstract void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState,
      IDrawingObserver drawingObserver);

  /**
   * @return default color.
   */
  protected abstract Color getDefaultColor();

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(getDefaultColor().darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
    CubicCurve2D curve =
        new CubicCurve2D.Double(1, height - 1, width / 4, height / 3, 3 * width / 4, 2 * height / 3, width - 1, 1);
    g2d.draw(curve);
    g2d.setColor(getDefaultColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(curve);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Point2D[] p = getControlPoints();

    // smoothen the curve if needed
    if (getSmooth() && lastUpdatePointIndex >= 0) {
      if (getPointCount() == PointCount.FIVE && (lastUpdatePointIndex == 1 || lastUpdatePointIndex == 2)) {
        p[3] = findThirdPoint(p[2], p[1]);
      }
      if (getPointCount() == PointCount.FIVE && lastUpdatePointIndex == 3) {
        p[1] = findThirdPoint(p[2], p[3]);
      }
      if (getPointCount() == PointCount.SEVEN && (lastUpdatePointIndex == 2 || lastUpdatePointIndex == 3)) {
        p[4] = findThirdPoint(p[3], p[2]);
      }
      if (getPointCount() == PointCount.SEVEN && lastUpdatePointIndex == 4) {
        p[2] = findThirdPoint(p[3], p[4]);
      }
      lastUpdatePointIndex = -1;
    }

    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      // Do not track guidelines.
      drawingObserver.stopTracking();
      g2d.setStroke(Constants.DASHED_STROKE);
      g2d.setColor(GUIDELINE_COLOR);
      for (int i = 0; i < p.length - 1; i++) {
        g2d.drawLine((int)p[i].getX(), (int)p[i].getY(), (int)p[i + 1].getX(), (int)p[i + 1].getY());
      }
      drawingObserver.startTracking();
    }

    Path2D path = new Path2D.Double();
    path.moveTo(p[0].getX(), p[0].getY());
    if (getPointCount() == PointCount.TWO) {
      path.lineTo(p[1].getX(), p[1].getY());
    } else if (getPointCount() == PointCount.THREE) {
      path.curveTo(p[1].getX(), p[1].getY(), p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY());
    } else if (getPointCount() == PointCount.FOUR) {
      path.curveTo(p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY(), p[3].getX(), p[3].getY());
    } else if (getPointCount() == PointCount.FIVE) {
      path.quadTo(p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY());
      path.quadTo(p[3].getX(), p[3].getY(), p[4].getX(), p[4].getY());
    } else if (getPointCount() == PointCount.SEVEN) {
      path.curveTo(p[1].getX(), p[1].getY(), p[2].getX(), p[2].getY(), p[3].getX(), p[3].getY());
      path.curveTo(p[4].getX(), p[4].getY(), p[5].getX(), p[5].getY(), p[6].getX(), p[6].getY());
    }

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    drawCurve(path, g2d, componentState, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @EditableProperty(name = "Point Count")
  public PointCount getPointCount() {
    if (pointCount == null) {
      pointCount = PointCount.FOUR;
    }
    return pointCount;
  }

  protected Point2D[] getControlPoints() {
    if (this.controlPoints != null) {
      // ensure backward compatibility by copying points from the old structure to the new one
      switch (getPointCount()) {
        case TWO:
          this.controlPoints2 = new Point2D[2];
          this.controlPoints2[0] = this.controlPoints[0];
          this.controlPoints2[1] = this.controlPoints[3];
          break;
        case THREE:
          this.controlPoints2 = new Point2D[3];
          this.controlPoints2[0] = this.controlPoints[0];
          this.controlPoints2[1] = this.controlPoints[1];
          this.controlPoints2[2] = this.controlPoints[3];
          break;
        case FOUR:
          this.controlPoints2 = new Point2D[4];
          this.controlPoints2[0] = this.controlPoints[0];
          this.controlPoints2[1] = this.controlPoints[1];
          this.controlPoints2[2] = this.controlPoints[2];
          this.controlPoints2[3] = this.controlPoints[3];
          break;
        default: // shouldn't happen
          this.controlPoints2 = this.controlPoints;
      }
      // we don't need old points anymore
      this.controlPoints = null;
    }
    return this.controlPoints2;
  }

  public void setPointCount(PointCount pointCount) {
    if (this.pointCount == pointCount)
      return;

    Point2D[] p = getControlPoints();
    Point2D[] newPoints = new Point2D[pointCount.count];
    newPoints[0] = p[0];
    newPoints[pointCount.count - 1] = p[p.length - 1];

    if (pointCount == PointCount.THREE) {
      newPoints[1] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[0].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[0].getY()) / 2);
    } else if (pointCount == PointCount.FOUR) {
      newPoints[1] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[0].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[0].getY()) / 2);
      newPoints[2] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[0].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[0].getY()) / 2);
    } else if (pointCount == PointCount.FIVE) {
      newPoints[2] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[0].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[0].getY()) / 2);
      newPoints[1] = new Point2D.Double((newPoints[2].getX() + newPoints[0].getX()) / 2, (newPoints[2].getY() + newPoints[0].getY()) / 2);
      newPoints[3] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[2].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[2].getY()) / 2);
    } else if (pointCount == PointCount.SEVEN) {
      newPoints[3] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[0].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[0].getY()) / 2);
      newPoints[2] = new Point2D.Double((newPoints[3].getX() + newPoints[0].getX()) / 2, (newPoints[3].getY() + newPoints[0].getY()) / 2);
      newPoints[1] = new Point2D.Double((newPoints[3].getX() + newPoints[0].getX()) / 2, (newPoints[3].getY() + newPoints[0].getY()) / 2);
      newPoints[4] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[3].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[3].getY()) / 2);
      newPoints[5] =
          new Point2D.Double((newPoints[pointCount.count - 1].getX() + newPoints[3].getX()) / 2,
              (newPoints[pointCount.count - 1].getY() + newPoints[3].getY()) / 2);
    }

    this.controlPoints2 = newPoints;
    this.pointCount = pointCount;
  }

  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index == 0 || index == getControlPointCount() - 1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return index > 0 && index < getControlPointCount() - 1;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    Point2D[] p = getControlPoints();
    p[index].setLocation(point);
    this.lastUpdatePointIndex = index;
    // if (getSmooth()) {
    // if (getPointCount() == PointCount.FIVE && (index == 1 || index == 2)) {
    // p[3] = findThirdPoint(p[2], p[1]);
    // }
    // if (getPointCount() == PointCount.FIVE && index == 3) {
    // p[1] = findThirdPoint(p[2], p[3]);
    // }
    // if (getPointCount() == PointCount.SEVEN && (index == 2 || index == 3)) {
    // p[4] = findThirdPoint(p[3], p[2]);
    // }
    // if (getPointCount() == PointCount.SEVEN && index == 4) {
    // p[2] = findThirdPoint(p[3], p[4]);
    // }
    // }
  }

  private Point2D findThirdPoint(Point2D p0, Point2D p) {
    return new Point2D.Double(2 * p0.getX() - p.getX(), 2 * p0.getY() - p.getY());
  }

  @EditableProperty(name = "Color")
  public Color getLeadColor() {
    return color;
  }

  public void setLeadColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Style")
  public LineStyle getStyle() {
    if (style == null)
      style = LineStyle.SOLID;
    return style;
  }

  public void setStyle(LineStyle style) {
    this.style = style;
  }  

  @EditableProperty
  public Boolean getSmooth() {
    if (smooth == null)
      this.smooth = true;
    return this.smooth;
  }

  public void setSmooth(Boolean smooth) {
    this.smooth = smooth;
    // make the current curve smooth if needed
    // Point[] p = getControlPoints();
    // if (smooth) {
    // if (getPointCount() == PointCount.FIVE) {
    // p[3] = findThirdPoint(p[2], p[1]);
    // }
    // if (getPointCount() == PointCount.SEVEN) {
    // p[4] = findThirdPoint(p[3], p[2]);
    // }
    // }
  }
  
  @EditableProperty
  public Size getLength() {
    double d = 0;
      try {
      Point2D[] p = getControlPoints();
      if (getPointCount() == PointCount.TWO) {
        d = p[0].distance(p[1]);
      } else if (getPointCount() == PointCount.THREE) {
        d = calculateLength(p[0], p[1], p[1], p[2]);
      } else if (getPointCount() == PointCount.FOUR) {
        d = calculateLength(p[0], p[1], p[2], p[3]);
      } else if (getPointCount() == PointCount.FIVE) {
        d = calculateLength(p[0], p[1], p[2]) + calculateLength(p[2], p[3], p[4]);      
      } else if (getPointCount() == PointCount.SEVEN) {
        d = calculateLength(p[0], p[1], p[2], p[3]) + calculateLength(p[3], p[4], p[5], p[6]);      
      }
      
      SizeUnit unit;
      if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.METRIC_KEY, true))
        unit = SizeUnit.mm;
      else
        unit = SizeUnit.in;    
      
      d = d * SizeUnit.px.getFactor() / unit.getFactor();    
      return new Size(d, unit);
    } catch (Exception e) {
      LOG.error("Error calculating length of " + getName(), e);
      return null;
    }
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    double margin = 50; // hard coded, replace with something meaningful
    for (int i = 0; i < getControlPointCount(); i++) {
      Point2D p = getControlPoint(i);
      if (p.getX() < minX)
        minX = p.getX();
      if (p.getX() > maxX)
        maxX = p.getX();
      if (p.getY() < minY)
        minY = p.getY();
      if (p.getY() > maxY)
        maxY = p.getY();
    }
    
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }

  public enum PointCount {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SEVEN(7);

    private int count;

    private PointCount(int count) {
      this.count = count;
    }

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
  
  // calculate length of quad curve
  private double calculateLength(Point2D a, Point2D b, Point2D c) {
    double vx = 2 * (b.getX() - a.getX());
    double vy = 2 * (b.getY() - a.getY());
    double wx = c.getX() - 2 * b.getX() + a.getX();
    double wy = c.getY() - 2 * b.getY() + a.getY();

    double uu = 4 * (wx * wx + wy * wy);

    if (uu < 0.00001) {
      return (float) Math.sqrt((c.getX() - a.getX()) * (c.getX() - a.getX()) + (c.getY() - a.getY()) * (c.getY() - a.getY()));
    }

    double vv = 4 * (vx * wx + vy * wy);
    double ww = vx * vx + vy * vy;

    double t1 = (float) (2 * Math.sqrt(uu * (uu + vv + ww)));
    double t2 = 2 * uu + vv;
    double t3 = vv * vv - 4 * uu * ww;
    double t4 = (float) (2 * Math.sqrt(uu * ww));

    return (float) ((t1 * t2 - t3 * Math.log(t2 + t1) - (vv * t4 - t3 * Math.log(vv + t4)))
        / (8 * Math.pow(uu, 1.5)));
  }
  
  // do not use this property
  @XStreamOmitField
  @Deprecated
  private double curveThreshold;
  
  // tweak this value if needed
  private static final double curveDivisionThreshold = 5d;
   
  // approximately calculate length of Bezier curves by subdivision
  private double calculateLength(Point2D a, Point2D b, Point2D c, Point2D d) {
    double distance = a.distance(d);
    if (distance < curveDivisionThreshold)
      return distance;
    CubicCurve2D curve = new CubicCurve2D.Double(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY(), d.getX(), d.getY());
    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);
    return calculateLength(left.getP1(), left.getCtrlP1(), left.getCtrlP2(), left.getP2()) + 
        calculateLength(right.getP1(), right.getCtrlP1(), right.getCtrlP2(), right.getP2());
  }
  
  protected List<CubicCurve2D> subdivide(double d) {
    Point2D[] p = getControlPoints();
    Path2D path = new Path2D.Double();
    path.moveTo(p[0].getX(), p[0].getY());
    if (getPointCount() == PointCount.TWO) {
      List<CubicCurve2D> result = new ArrayList<CubicCurve2D>();
      result.add(new CubicCurve2D.Double(p[0].getX(), p[0].getY(), 
          (p[0].getX() + p[1].getX()) / 2,
          (p[0].getY() + p[1].getY()) / 2,
          (p[0].getX() + p[1].getX()) / 2,
          (p[0].getY() + p[1].getY()) / 2,
          p[1].getX(), p[1].getY()));
      return result;
    } else if (getPointCount() == PointCount.THREE) {
      CubicCurve2D curve = new CubicCurve2D.Double(p[0].getX(), p[0].getY(), 
          (p[0].getX() + 2 * p[1].getX()) / 3,
          (p[0].getY() + 2 * p[1].getY()) / 3,
          (p[2].getX() + 2 * p[1].getX()) / 3,
          (p[2].getY() + 2 * p[1].getY()) / 3,
          p[2].getX(), p[2].getY());
      return subdivide(curve, d);      
    } else if (getPointCount() == PointCount.FOUR) {
      CubicCurve2D curve = new CubicCurve2D.Double(p[0].getX(), p[0].getY(),           
          p[1].getX(), p[1].getY(),
          p[2].getX(), p[2].getY(),
          p[3].getX(), p[3].getY());
      return subdivide(curve, d);      
    } else if (getPointCount() == PointCount.FIVE) {
      CubicCurve2D curve1 = new CubicCurve2D.Double(p[0].getX(), p[0].getY(), 
          (p[0].getX() + 2 * p[1].getX()) / 3,
          (p[0].getY() + 2 * p[1].getY()) / 3,
          (p[2].getX() + 2 * p[1].getX()) / 3,
          (p[2].getY() + 2 * p[1].getY()) / 3,
          p[2].getX(), p[2].getY());
      CubicCurve2D curve2 = new CubicCurve2D.Double(p[2].getX(), p[2].getY(), 
          (p[2].getX() + 2 * p[3].getX()) / 3,
          (p[2].getY() + 2 * p[3].getY()) / 3,
          (p[4].getX() + 2 * p[3].getX()) / 3,
          (p[4].getY() + 2 * p[3].getY()) / 3,
          p[4].getX(), p[4].getY());
      return Streams.concat(subdivide(curve1, d).stream(), subdivide(curve2, d).stream()).collect(Collectors.toList());     
    } else if (getPointCount() == PointCount.SEVEN) {
      CubicCurve2D curve1 = new CubicCurve2D.Double(p[0].getX(), p[0].getY(),           
          p[1].getX(), p[1].getY(),
          p[2].getX(), p[2].getY(),
          p[3].getX(), p[3].getY());
      CubicCurve2D curve2 = new CubicCurve2D.Double(p[3].getX(), p[3].getY(),           
          p[4].getX(), p[4].getY(),
          p[5].getX(), p[5].getY(),
          p[6].getX(), p[6].getY());
      return Streams.concat(subdivide(curve1, d).stream(), subdivide(curve2, d).stream()).collect(Collectors.toList());
    }
    return null;
  }
  
  private List<CubicCurve2D> subdivide(CubicCurve2D curve, double d) {
    if (curve.getFlatness() < d || new Point2D.Double(curve.getX1(), curve.getY1()).distance(curve.getX2(), curve.getY2()) < d) {
      List<CubicCurve2D> result = new ArrayList<CubicCurve2D>();
      result.add(curve);
      return result;
    }
    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);
    return Streams.concat(subdivide(left, d).stream(), subdivide(right, d).stream()).collect(Collectors.toList());
  }
}
