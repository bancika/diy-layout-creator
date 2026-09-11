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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.IHaveLength;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

/**
 * Base class for connectors that run only horizontally and vertically and turn at rounded
 * 90-degree corners.
 * <p>
 * The first and the last control point are the sticky ends. Each control point in between is a free
 * bend vertex that the route reaches with a horizontal move followed by a vertical one, or the
 * other way around when {@link #getStartDirection()} is vertical. Both coordinates of a bend point
 * therefore carry meaning and the arrangement never has to be guessed: an orthogonal route of
 * <code>s</code> segments has <code>s - 2</code> degrees of freedom, and this template spends
 * exactly two per bend point. Segments of zero length collapse silently, which is how the simple L,
 * horizontal-vertical-horizontal and vertical-horizontal-vertical shapes are expressed: a bend
 * point sharing a column with the start swallows the leading segment, one sharing a row with the
 * end swallows the trailing one. All four combinations of leaving and arriving on either axis are
 * therefore reachable by dragging alone, which is why the start direction is not offered as a
 * property.
 *
 * @author Branislav Stojkovic
 */
public abstract class AbstractOrthogonalComponent<T> extends AbstractTransparentComponent<T>
    implements IHaveLength {

  private static final long serialVersionUID = 1L;

  public static Size DEFAULT_SIZE = new Size(1d, SizeUnit.in);
  public static Size DEFAULT_CORNER_RADIUS = new Size(0.05d, SizeUnit.in);

  // rounding is skipped below this radius in pixels, where a quadratic corner is indistinguishable
  // from a sharp one
  private static final double MIN_CORNER_RADIUS = 1d;

  private static final double EPSILON = 1e-6;

  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0),
      new Point2D.Double(DEFAULT_SIZE.convertToPixels() / 2, DEFAULT_SIZE.convertToPixels()),
      new Point2D.Double(DEFAULT_SIZE.convertToPixels(), DEFAULT_SIZE.convertToPixels())};

  protected Color color = getDefaultColor();
  protected PointCount pointCount = PointCount.THREE;
  protected OrientationHV startDirection = OrientationHV.HORIZONTAL;
  protected LineStyle style = LineStyle.SOLID;
  protected Size cornerRadius = DEFAULT_CORNER_RADIUS;

  /**
   * Draws the specified route onto graphics.
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
    Path2D path = createIconPath(width, height);
    g2d.setColor(getDefaultColor().darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
    g2d.draw(path);
    g2d.setColor(getDefaultColor());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(path);
  }

  /**
   * @param width
   * @param height
   * @return the elbow drawn in the toolbox, so that subclasses that stroke it differently do not
   *         have to restate the shape.
   */
  protected Path2D createIconPath(int width, int height) {
    Path2D path = new Path2D.Double();
    path.moveTo(1, height - 3);
    path.lineTo(width / 2d - 2, height - 3);
    path.quadTo(width / 2d, height - 3, width / 2d, height - 5);
    path.lineTo(width / 2d, 5);
    path.quadTo(width / 2d, 3, width / 2d + 2, 3);
    path.lineTo(width - 1, 3);
    return path;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Composite oldComposite = applyAlpha(g2d);

    drawCurve(buildPath(), g2d, componentState, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  /**
   * Expands the control points into the vertices of the route. Every control point is reached by a
   * move along one axis followed by a move along the other, the end point included, so the loop
   * below is the whole routing rule. Zero-length segments are dropped as they are produced.
   * 
   * @return route vertices, starting with the first and ending with the last control point.
   */
  protected List<Point2D> buildVertices() {
    Point2D[] p = getControlPoints();
    boolean horizontalFirst = getStartDirection() == OrientationHV.HORIZONTAL;
    List<Point2D> vertices = new ArrayList<Point2D>();
    addVertex(vertices, p[0]);
    for (int i = 1; i < p.length; i++) {
      Point2D previous = p[i - 1];
      Point2D next = p[i];
      if (horizontalFirst) {
        addVertex(vertices, new Point2D.Double(next.getX(), previous.getY()));
      } else {
        addVertex(vertices, new Point2D.Double(previous.getX(), next.getY()));
      }
      addVertex(vertices, next);
    }
    return vertices;
  }

  /**
   * Builds the route as a path with rounded corners. The radius is clamped to half of each adjacent
   * segment so that short segments bend rather than overshoot.
   * 
   * @return path following the route.
   */
  protected Path2D buildPath() {
    List<Point2D> vertices = buildVertices();
    double radius = getCornerRadius().convertToPixels();
    Path2D path = new Path2D.Double();
    Point2D first = vertices.get(0);
    path.moveTo(first.getX(), first.getY());
    for (int i = 1; i < vertices.size() - 1; i++) {
      Point2D previous = vertices.get(i - 1);
      Point2D corner = vertices.get(i);
      Point2D next = vertices.get(i + 1);
      double r = Math.min(radius, Math.min(previous.distance(corner), corner.distance(next)) / 2);
      if (r < MIN_CORNER_RADIUS) {
        path.lineTo(corner.getX(), corner.getY());
        continue;
      }
      Point2D in = interpolate(corner, previous, r);
      Point2D out = interpolate(corner, next, r);
      path.lineTo(in.getX(), in.getY());
      path.quadTo(corner.getX(), corner.getY(), out.getX(), out.getY());
    }
    Point2D last = vertices.get(vertices.size() - 1);
    path.lineTo(last.getX(), last.getY());
    return path;
  }

  private static void addVertex(List<Point2D> vertices, Point2D p) {
    if (!vertices.isEmpty() && vertices.get(vertices.size() - 1).distance(p) < EPSILON) {
      return;
    }
    vertices.add(p);
  }

  private static Point2D interpolate(Point2D from, Point2D towards, double distance) {
    double t = distance / from.distance(towards);
    return new Point2D.Double(from.getX() + (towards.getX() - from.getX()) * t, from.getY()
        + (towards.getY() - from.getY()) * t);
  }

  protected Point2D[] getControlPoints() {
    return this.controlPoints;
  }

  @EditableProperty(name = "Point Count")
  public PointCount getPointCount() {
    if (pointCount == null) {
      pointCount = PointCount.THREE;
    }
    return pointCount;
  }

  public void setPointCount(PointCount pointCount) {
    if (getPointCount() == pointCount) {
      return;
    }

    Point2D[] p = getControlPoints();
    Point2D start = p[0];
    Point2D end = p[p.length - 1];
    Point2D[] newPoints = new Point2D[pointCount.count];
    newPoints[0] = start;
    newPoints[pointCount.count - 1] = end;

    // spread the bend points so that the route comes out as an even staircase: they advance evenly
    // along the axis the route travels last, and sit halfway between those steps on the other axis
    int bendCount = pointCount.count - 2;
    boolean horizontalFirst = getStartDirection() == OrientationHV.HORIZONTAL;
    for (int i = 1; i <= bendCount; i++) {
      double along = (double) i / bendCount;
      double across = (2d * i - 1) / (2d * bendCount);
      double tx = horizontalFirst ? across : along;
      double ty = horizontalFirst ? along : across;
      newPoints[i] =
          new Point2D.Double(start.getX() + (end.getX() - start.getX()) * tx, start.getY()
              + (end.getY() - start.getY()) * ty);
    }

    this.controlPoints = newPoints;
    this.pointCount = pointCount;
  }

  /**
   * Not an editable property on purpose. Every route shape is reachable by dragging the bend
   * points, so exposing this would only give the user a second, less discoverable way to do the
   * same thing. It is kept as state because a quarter-turn rotation swaps the two axes and the
   * transformer has to record that.
   *
   * @return axis the route travels along when it leaves the first control point.
   */
  public OrientationHV getStartDirection() {
    if (startDirection == null) {
      startDirection = OrientationHV.HORIZONTAL;
    }
    return startDirection;
  }

  public void setStartDirection(OrientationHV startDirection) {
    this.startDirection = startDirection;
  }

  @EditableProperty(name = "Corner Radius")
  public Size getCornerRadius() {
    if (cornerRadius == null) {
      cornerRadius = DEFAULT_CORNER_RADIUS;
    }
    return cornerRadius;
  }

  public void setCornerRadius(Size cornerRadius) {
    this.cornerRadius = cornerRadius;
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
    if (style == null) {
      style = LineStyle.SOLID;
    }
    return style;
  }

  public void setStyle(LineStyle style) {
    this.style = style;
  }

  @EditableProperty(name = "Length")
  public Size getLength() {
    return calculateLength();
  }

  @Override
  public Size calculateLength() {
    // corner rounding shaves a fraction of the radius off each bend, which is well below the
    // precision this readout is used at
    List<Point2D> vertices = buildVertices();
    double d = 0;
    for (int i = 1; i < vertices.size(); i++) {
      d += vertices.get(i - 1).distance(vertices.get(i));
    }

    SizeUnit unit;
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.METRIC_KEY, true)) {
      unit = SizeUnit.mm;
    } else {
      unit = SizeUnit.in;
    }

    return new Size(d * SizeUnit.px.getFactor() / unit.getFactor(), unit);
  }

  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    getControlPoints()[index].setLocation(point);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index == 0 || index == getControlPointCount() - 1;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return index > 0 && index < getControlPointCount() - 1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    double margin = 50; // hard coded, replace with something meaningful
    // every route vertex borrows both of its coordinates from a control point, so the control
    // points alone bound the whole route
    for (int i = 0; i < getControlPointCount(); i++) {
      Point2D p = getControlPoint(i);
      if (p.getX() < minX) {
        minX = p.getX();
      }
      if (p.getX() > maxX) {
        maxX = p.getX();
      }
      if (p.getY() < minY) {
        minY = p.getY();
      }
      if (p.getY() > maxY) {
        maxY = p.getY();
      }
    }

    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY
        + 2 * margin);
  }

  public enum PointCount {
    THREE(3), FOUR(4), FIVE(5);

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
