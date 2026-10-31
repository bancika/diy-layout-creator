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
package org.diylc.components.connectivity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractCurvedComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IConnective;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.annotations.AiNote.Scope;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.presenter.AreaUtils;
import org.diylc.utils.Constants;

@AiNote(value = "Electrically a single conductor between its two ends, exactly like Hookup Wire, even though two twisted leads are drawn; use it only for the look of a twisted pair, and where the circuit must be electrically correct and analyzable, draw each conductor as its own Hookup Wire or Right Angle Wire instead", scope = Scope.SELECTION)
@AiNote("The points between the first and last are curve handles the leads do not necessarily pass through")
@ComponentDescriptor(name = "Twisted Leads", author = "Branislav Stojkovic",
    category = "Connectivity", instanceNamePrefix = "W",
    description = "A pair of flexible leads twisted tightly together", zOrder = IDIYComponent.WIRING,
    flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW,
    transformer = SimpleComponentTransformer.class, enableCache = true,
    creationMethod = CreationMethod.POINT_BY_POINT)
public class TwistedWire extends AbstractCurvedComponent<Void> implements IConnective, IContinuity {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.green;
  public static Color COLOR2 = Color.blue;
  public static Color STRIPE_COLOR = Color.yellow;
  public static Color STRIPE_COLOR2 = Color.decode("#FF00FF");
  public static double INSULATION_THICKNESS_PCT = 0.3;

  protected AWG gauge = AWG._22;

  private Color color2 = COLOR2;
  private Color stripeColor = STRIPE_COLOR;
  private Color stripeColor2 = STRIPE_COLOR2;
  private boolean stripe1 = false;
  private boolean stripe2 = false;

  // cached areas
  transient private Area firstLeadArea = null;
  transient private Area secondLeadArea = null;

  transient private Area firstLeadStripeArea = null;
  transient private Area secondLeadStripeArea = null;

  transient private LeadGeometry leadGeometry = null;

  @Override
  protected Color getDefaultColor() {
    return COLOR;
  }

  private int getThickness() {
    return (int) (gauge.diameterIn() * Constants.PIXELS_PER_INCH * (1 + 2 * INSULATION_THICKNESS_PCT))
        - 1;
  }

  // the two leads swing out to either side of the curve as they twist, see recalculate
  @Override
  protected double getClipMargin() {
    return getThickness() * 2 + 1;
  }

  @Override
  protected void drawCurve(Path2D curve, Graphics2D g2d, ComponentState componentState,
      IDrawingObserver drawingObserver) {
    // stroking paints some pixels more than once, which would show through on a translucent wire,
    // so that case builds each lead as an area and paints it once instead
    if (getAlpha().getValue().doubleValue() < MAX_ALPHA) {
      drawCurveWithAreas(curve, g2d, componentState, drawingObserver);
    } else {
      drawCurveWithStrokes(curve, g2d, componentState, drawingObserver);
    }
  }

  private void drawCurveWithStrokes(Path2D curve, Graphics2D g2d, ComponentState componentState,
      IDrawingObserver drawingObserver) {
    int thickness = getThickness();
    if (leadGeometry == null) {
      leadGeometry = buildLeadGeometry(curve, thickness);
    }
    boolean selected =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING;
    boolean normal = componentState == ComponentState.NORMAL;
    Color firstColor = selected ? SELECTION_COLOR : color;
    Color secondColor = selected ? SELECTION_COLOR : color2;

    float outlineWidth = 0;
    if (normal) {
      // as wide as the 1px outline of a regular wire: the canvas narrows a thin stroke to keep it
      // one pixel wide when zoomed in, so the stroke it ends up with gives that width
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      outlineWidth = ((BasicStroke) g2d.getStroke()).getLineWidth();
    }

    drawLead(g2d, leadGeometry.firstLead, firstColor, normal && stripe1, stripeColor,
        outlineWidth, thickness, drawingObserver);
    drawLead(g2d, leadGeometry.secondLead, secondColor, normal && stripe2, stripeColor2,
        outlineWidth, thickness, drawingObserver);

    // the second lead was drawn last and lies on top everywhere; redrawing the first lead inside
    // its own crossings puts it on top there. Both leads share one colour when selected.
    if (!selected) {
      Shape oldClip = g2d.getClip();
      g2d.clip(leadGeometry.firstOnTop);
      drawLead(g2d, leadGeometry.firstLead, firstColor, normal && stripe1, stripeColor,
          outlineWidth, thickness, null);
      g2d.setClip(oldClip);
    }
  }

  // drawingObserver is null for a redraw that must not be recorded again; outlineWidth is 0 for
  // no outline
  private void drawLead(Graphics2D g2d, Path2D lead, Color leadColor, boolean stripe,
      Color leadStripeColor, float outlineWidth, int thickness, IDrawingObserver drawingObserver) {
    boolean outline = outlineWidth > 0;
    float innerWidth = Math.max(thickness - outlineWidth, 1);
    // the first stroke covers the whole lead, so it is the one recorded as the component's area
    if (drawingObserver != null) {
      drawingObserver.startTracking();
    }
    if (outline) {
      // a dark band centred on the lead's edge, like an outline drawn around an area
      g2d.setColor(leadColor.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness + outlineWidth));
      g2d.draw(lead);
      if (drawingObserver != null) {
        drawingObserver.stopTracking();
      }
      g2d.setColor(leadColor);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(innerWidth));
      g2d.draw(lead);
    } else {
      g2d.setColor(leadColor);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(thickness));
      g2d.draw(lead);
      if (drawingObserver != null) {
        drawingObserver.stopTracking();
      }
    }
    if (stripe) {
      g2d.setColor(leadStripeColor);
      g2d.setStroke(getStripeStroke(thickness, outline ? innerWidth : thickness));
      g2d.draw(lead);
    }
  }

  private void drawCurveWithAreas(Path2D curve, Graphics2D g2d, ComponentState componentState,
      IDrawingObserver drawingObserver) {
    int thickness = getThickness();
    Color curveColor1 =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : color;
    Color curveColor2 =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : color2;

    if (firstLeadArea == null || secondLeadArea == null) {
      firstLeadArea = new Area();
      secondLeadArea = new Area();
      recalculate(curve, thickness, firstLeadArea, secondLeadArea, false);

      if (stripe1 || stripe2) {
        firstLeadStripeArea = new Area();
        secondLeadStripeArea = new Area();
        recalculate(curve, thickness, firstLeadStripeArea, secondLeadStripeArea, true);
        firstLeadStripeArea.intersect(firstLeadArea);
        secondLeadStripeArea.intersect(secondLeadArea);
      }
    }

    g2d.setColor(curveColor1);
    drawingObserver.startTracking();
    g2d.fill(firstLeadArea);
    drawingObserver.stopTracking();

    if (stripe1 && componentState == ComponentState.NORMAL) {
      drawingObserver.stopTracking();
      g2d.setColor(stripeColor);
      g2d.fill(firstLeadStripeArea);
    }

    g2d.setColor(curveColor2);
    drawingObserver.startTracking();
    g2d.fill(secondLeadArea);
    drawingObserver.stopTracking();

    if (stripe2 && componentState == ComponentState.NORMAL) {
      g2d.setColor(stripeColor2);
      g2d.fill(secondLeadStripeArea);
    }

    if (componentState == ComponentState.NORMAL) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.setColor(color.darker());
      g2d.draw(firstLeadArea);
      g2d.setColor(color2.darker());
      g2d.draw(secondLeadArea);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Stroke stroke = ObjectCache.getInstance().fetchBasicStroke(3);
    CubicCurve2D curve1 =
        new CubicCurve2D.Double(2, height - 2, width - 4, height - 4, 4, 4, width - 2, 2);
    CubicCurve2D curve2 =
        new CubicCurve2D.Double(2, height - 2, 4, 4, width - 4, height - 4, width - 2, 2);

    Area area1 = new Area(stroke.createStrokedShape(curve1));
    Area area2 = new Area(stroke.createStrokedShape(curve2));
    area2.subtract(area1);

    g2d.setColor(COLOR2);
    g2d.fill(area2);
    g2d.setColor(COLOR);
    g2d.fill(area1);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR2.darker());
    g2d.draw(area2);
    g2d.setColor(COLOR.darker());
    g2d.draw(area1);
  }

  @EditableProperty(name = "AWG")
  public AWG getGauge() {
    return gauge;
  }

  public void setGauge(AWG gauge) {
    this.gauge = gauge;

    // invalidate cached areas
    this.firstLeadArea = null;
    this.secondLeadArea = null;
    this.firstLeadStripeArea = null;
    this.secondLeadStripeArea = null;
    this.leadGeometry = null;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    super.setControlPoint(point, index);

    // invalidate cached areas
    this.firstLeadArea = null;
    this.secondLeadArea = null;
    this.firstLeadStripeArea = null;
    this.secondLeadStripeArea = null;
    this.leadGeometry = null;
  }

  @Override
  public void setPointCount(PointCount pointCount) {
    super.setPointCount(pointCount);

    // invalidate cached areas
    this.firstLeadArea = null;
    this.secondLeadArea = null;
    this.firstLeadStripeArea = null;
    this.secondLeadStripeArea = null;
    this.leadGeometry = null;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @EditableProperty(name = "Color 1")
  public Color getLeadColor() {
    return color;
  }

  @EditableProperty(name = "Color 2")
  public Color getColor2() {
    return color2;
  }

  public void setColor2(Color color2) {
    this.color2 = color2;
  }

  @Override
  public LineStyle getStyle() {
    // prevent from editing
    return super.getStyle();
  }

  @EditableProperty(name = "Stripe Color 1")
  public Color getStripeColor() {
    return stripeColor;
  }

  public void setStripeColor(Color stripeColor) {
    this.stripeColor = stripeColor;
  }

  @EditableProperty(name = "Stripe Color 2")
  public Color getStripeColor2() {
    return stripeColor2;
  }

  public void setStripeColor2(Color stripeColor2) {
    this.stripeColor2 = stripeColor2;
  }

  @EditableProperty(name = "Stripe 1")
  public boolean getStripe1() {
    return stripe1;
  }

  public void setStripe1(boolean stripe1) {
    this.stripe1 = stripe1;

    if (stripe1) {
      // invalidate cached areas
      this.firstLeadArea = null;
      this.secondLeadArea = null;
      this.firstLeadStripeArea = null;
      this.secondLeadStripeArea = null;
      this.leadGeometry = null;
    }
  }

  @EditableProperty(name = "Stripe 2")
  public boolean getStripe2() {
    return stripe2;
  }

  public void setStripe2(boolean stripe2) {
    this.stripe2 = stripe2;

    if (stripe2) {
      // invalidate cached areas
      this.firstLeadArea = null;
      this.secondLeadArea = null;
      this.firstLeadStripeArea = null;
      this.secondLeadStripeArea = null;
      this.leadGeometry = null;
    }
  }

  // hard-core math below :)

  public void recalculate(Path2D path, float thickness, Area firstLeadArea, Area secondLeadArea,
      boolean stripe) {
    LeadGeometry geometry = buildLeadGeometry(path, thickness);
    Stroke stroke = stripe ? getStripeStroke(thickness, thickness)
        : ObjectCache.getInstance().fetchBasicStroke(thickness);

    Area[] outputAreas = new Area[2];
    List<List<Path2D>> segments = List.of(geometry.firstSegments, geometry.secondSegments);
    for (int j = 0; j < 2; j++) {
      List<Area> segmentAreas = new ArrayList<Area>();
      for (Path2D p : segments.get(j)) {
        segmentAreas.add(new Area(stroke.createStrokedShape(p)));
      }
      outputAreas[j] = AreaUtils.union(segmentAreas);
    }

    // at overlapping points, decide which lead goes on top and clear the overlapping area from the
    // lead below; all squares of one kind are cut in a single operation, because cutting them one
    // at a time costs an operation over the whole lead for each
    Area firstOnTopArea = new Area(geometry.firstOnTop);
    firstOnTopArea.intersect(outputAreas[0]);
    outputAreas[1].subtract(firstOnTopArea);
    // taken after the first cut, so where squares of both kinds overlap only one lead loses it
    Area secondOnTopArea = new Area(geometry.secondOnTop);
    secondOnTopArea.intersect(outputAreas[1]);
    outputAreas[0].subtract(secondOnTopArea);

    // dump to the output areas
    firstLeadArea.add(outputAreas[0]);
    secondLeadArea.add(outputAreas[1]);
  }

  private Stroke getStripeStroke(float thickness, float width) {
    return ObjectCache.getInstance().fetchStroke(width,
        new float[] {thickness / 2, thickness * 2}, thickness * 10, BasicStroke.CAP_BUTT);
  }

  // works out the path of each lead as it winds around the curve, and the squares around the
  // crossings where each lead goes on top
  private LeadGeometry buildLeadGeometry(Path2D path, float thickness) {
    PathIterator iterator = path.getPathIterator(null);
    float[] coords = new float[6];
    Point2D current = new Point2D.Double();

    // convert all segments to cubic curves
    List<CubicCurve2D> curves = new ArrayList<CubicCurve2D>();
    while (!iterator.isDone()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          current.setLocation(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          curves.add(new CubicCurve2D.Double(current.getX(), current.getY(),
              (current.getX() + coords[0]) / 2, (current.getY() + coords[1]) / 2,
              (current.getX() + coords[0]) / 2, (current.getY() + coords[1]) / 2, coords[0],
              coords[1]));
          current.setLocation(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          curves.add(new CubicCurve2D.Double(current.getX(), current.getY(), coords[0], coords[1],
              coords[0], coords[1], coords[2], coords[3]));
          current.setLocation(coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          curves.add(new CubicCurve2D.Double(current.getX(), current.getY(), coords[0], coords[1],
              coords[2], coords[3], coords[4], coords[5]));
          current.setLocation(coords[4], coords[5]);
          break;
      }
      iterator.next();
    }

    double segmentLength = thickness * 6; // chosen empirically

    // Convert to polygon
    List<Line2D> polygon = new ArrayList<Line2D>();
    for (CubicCurve2D curve : curves)
      polygon.addAll(split(curve, segmentLength));

    List<List<Path2D>> segments = new ArrayList<List<Path2D>>();
    segments.add(new ArrayList<Path2D>());
    segments.add(new ArrayList<Path2D>());

    Path2D[] currentPaths = new Path2D[2];
    currentPaths[0] = new Path2D.Double();
    currentPaths[1] = new Path2D.Double();

    double offset = thickness * 1.5; // chosen empirically
    double rectSize = thickness * 3.5; // chosen empirically

    currentPaths[0].moveTo(polygon.get(0).getX1(), polygon.get(0).getY1());
    currentPaths[1].moveTo(polygon.get(0).getX1(), polygon.get(0).getY1());

    for (int i = 0; i < polygon.size(); i++) {
      // create curved paths for each segment of the twisted pair
      Line2D line = polygon.get(i);
      double centerX = (line.getX1() + line.getX2()) / 2;
      double centerY = (line.getY1() + line.getY2()) / 2;
      double theta = Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1());

      double sign = i % 2 == 0 ? 1 : -1;
      double[] thetas = new double[] {theta - sign * Math.PI / 2, theta + sign * Math.PI / 2};

      for (int j = 0; j < 2; j++) {
        currentPaths[j].quadTo(centerX + offset * Math.cos(thetas[j]),
            centerY + offset * Math.sin(thetas[j]), line.getX2(), line.getY2());
        if (i % 2 == 0 || i == polygon.size() - 1) {
          segments.get(j).add(currentPaths[j]);
          currentPaths[j] = new Path2D.Double();
          currentPaths[j].moveTo(line.getX2(), line.getY2());
        }
      }
    }

    Path2D firstOnTop = new Path2D.Double();
    Path2D secondOnTop = new Path2D.Double();
    for (int i = 0; i < polygon.size(); i++) {
      Line2D line = polygon.get(i);
      Rectangle2D pointRect1 = new Rectangle2D.Double(line.getX1() - rectSize / 2,
          line.getY1() - rectSize / 2, rectSize, rectSize);
      if (i % 2 == 1) {
        firstOnTop.append(pointRect1, false);
      } else {
        secondOnTop.append(pointRect1, false);
      }

      if (i == polygon.size() - 1) {
        Rectangle2D pointRect2 = new Rectangle2D.Double(line.getX2() - rectSize / 2,
            line.getY2() - rectSize / 2, rectSize, rectSize);
        if (i % 2 == 0) {
          firstOnTop.append(pointRect2, false);
        } else {
          secondOnTop.append(pointRect2, false);
        }
      }
    }

    // each lead also as a single path, with its segments as separate sub-paths, for stroking
    Path2D[] leads = new Path2D[2];
    for (int j = 0; j < 2; j++) {
      leads[j] = new Path2D.Double();
      for (Path2D p : segments.get(j)) {
        leads[j].append(p, false);
      }
    }

    return new LeadGeometry(leads[0], leads[1], segments.get(0), segments.get(1), firstOnTop,
        secondOnTop);
  }

  // splits a curve into a series of lines
  private List<Line2D> split(CubicCurve2D curve, double segmentLength) {
    Point2D p1 = curve.getP1();
    Point2D p2 = curve.getP2();
    List<Line2D> res = new ArrayList<Line2D>();

    double length = Double.MAX_VALUE;

    if (p1.distance(p2) <= segmentLength)
      length = calculateLength(curve, segmentLength / 10);

    if (length <= segmentLength) {
      res.add(new Line2D.Double(p1, p2));
    } else {
      CubicCurve2D left = new CubicCurve2D.Double();
      CubicCurve2D right = new CubicCurve2D.Double();
      curve.subdivide(left, right);
      res.addAll(split(left, segmentLength));
      res.addAll(split(right, segmentLength));
    }
    return res;
  }

  // calculates length of a curve, given the precision
  private double calculateLength(CubicCurve2D curve, double precision) {
    Point2D p1 = curve.getP1();
    Point2D p2 = curve.getP2();

    double d = p1.distance(p2);
    if (d <= precision) {
      return d;
    }

    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);

    return calculateLength(left, precision) + calculateLength(right, precision);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2) {
    return Math.abs(index1 - index2) == getControlPointCount() - 1;
  }

  private static class LeadGeometry {

    final Path2D firstLead;
    final Path2D secondLead;
    final List<Path2D> firstSegments;
    final List<Path2D> secondSegments;
    // squares around the crossings where the first or the second lead goes on top
    final Path2D firstOnTop;
    final Path2D secondOnTop;

    LeadGeometry(Path2D firstLead, Path2D secondLead, List<Path2D> firstSegments,
        List<Path2D> secondSegments, Path2D firstOnTop, Path2D secondOnTop) {
      this.firstLead = firstLead;
      this.secondLead = secondLead;
      this.firstSegments = firstSegments;
      this.secondSegments = secondSegments;
      this.firstOnTop = firstOnTop;
      this.secondOnTop = secondOnTop;
    }
  }
}
