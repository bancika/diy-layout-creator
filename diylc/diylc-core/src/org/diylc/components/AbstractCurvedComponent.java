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
import java.awt.Point;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;

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

  public static Color GUIDELINE_COLOR = Color.blue;
  public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);

  // for backward compatibility
  protected Point[] controlPoints = null;

  protected Point[] controlPoints2 = new Point[] {new Point(0, 0),
      new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), 0),
      new Point((int) (DEFAULT_SIZE.convertToPixels() / 2), (int) (DEFAULT_SIZE.convertToPixels())),
      new Point((int) DEFAULT_SIZE.convertToPixels(), (int) DEFAULT_SIZE.convertToPixels())};

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
   * @param theme
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

    Point[] p = getControlPoints();

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
        g2d.drawLine(p[i].x, p[i].y, p[i + 1].x, p[i + 1].y);
      }
      drawingObserver.startTracking();
    }

    Path2D path = new Path2D.Double();
    path.moveTo(p[0].x, p[0].y);
    if (getPointCount() == PointCount.TWO) {
      path.lineTo(p[1].x, p[1].y);
    } else if (getPointCount() == PointCount.THREE) {
      path.curveTo(p[1].x, p[1].y, p[1].x, p[1].y, p[2].x, p[2].y);
    } else if (getPointCount() == PointCount.FOUR) {
      path.curveTo(p[1].x, p[1].y, p[2].x, p[2].y, p[3].x, p[3].y);
    } else if (getPointCount() == PointCount.FIVE) {
      path.quadTo(p[1].x, p[1].y, p[2].x, p[2].y);
      path.quadTo(p[3].x, p[3].y, p[4].x, p[4].y);
    } else if (getPointCount() == PointCount.SEVEN) {
      path.curveTo(p[1].x, p[1].y, p[2].x, p[2].y, p[3].x, p[3].y);
      path.curveTo(p[4].x, p[4].y, p[5].x, p[5].y, p[6].x, p[6].y);
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

  protected Point[] getControlPoints() {
    if (this.controlPoints != null) {
      // ensure backward compatibility by copying points from the old structure to the new one
      switch (getPointCount()) {
        case TWO:
          this.controlPoints2 = new Point[2];
          this.controlPoints2[0] = this.controlPoints[0];
          this.controlPoints2[1] = this.controlPoints[3];
          break;
        case THREE:
          this.controlPoints2 = new Point[3];
          this.controlPoints2[0] = this.controlPoints[0];
          this.controlPoints2[1] = this.controlPoints[1];
          this.controlPoints2[2] = this.controlPoints[3];
          break;
        case FOUR:
          this.controlPoints2 = new Point[4];
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
    Point[] p = getControlPoints();
    Point[] newPoints = new Point[pointCount.count];
    newPoints[0] = p[0];
    newPoints[pointCount.count - 1] = p[p.length - 1];

    if (pointCount == PointCount.THREE) {
      newPoints[1] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[0].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[0].y) / 2);
    } else if (pointCount == PointCount.FOUR) {
      newPoints[1] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[0].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[0].y) / 2);
      newPoints[2] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[0].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[0].y) / 2);
    } else if (pointCount == PointCount.FIVE) {
      newPoints[2] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[0].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[0].y) / 2);
      newPoints[1] = new Point((newPoints[2].x + newPoints[0].x) / 2, (newPoints[2].y + newPoints[0].y) / 2);
      newPoints[3] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[2].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[2].y) / 2);
    } else if (pointCount == PointCount.SEVEN) {
      newPoints[3] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[0].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[0].y) / 2);
      newPoints[2] = new Point((newPoints[3].x + newPoints[0].x) / 2, (newPoints[3].y + newPoints[0].y) / 2);
      newPoints[1] = new Point((newPoints[3].x + newPoints[0].x) / 2, (newPoints[3].y + newPoints[0].y) / 2);
      newPoints[4] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[3].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[3].y) / 2);
      newPoints[5] =
          new Point((newPoints[pointCount.count - 1].x + newPoints[3].x) / 2,
              (newPoints[pointCount.count - 1].y + newPoints[3].y) / 2);
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
  public Point getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    Point[] p = getControlPoints();
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

  private Point findThirdPoint(Point p0, Point p) {
    return new Point(2 * p0.x - p.x, 2 * p0.y - p.y);
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

  enum PointCount {
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
}
