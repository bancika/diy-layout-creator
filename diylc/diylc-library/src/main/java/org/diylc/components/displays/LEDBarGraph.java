/*
 *
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.components.displays;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.transform.LEDBarGraphTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * A segmented LED bar in a dual-in-line package, as used for VU and level meters.
 *
 * <p>Not an {@code AbstractMakerBoard} like the rest of this category: it is a DIP-outline part,
 * so it follows {@code DIL_IC} on {@link AbstractLabeledComponent} and carries its own
 * transformer.
 *
 * <p>It differs from a DIP IC in the way that governs the whole geometry: an IC's leads bend out
 * of the sides of the plastic, so its body sits <em>between</em> the pin rows, while a bar
 * graph's pins leave the underside within the footprint and the package spans the rows. The pins
 * are nonetheless drawn on top, because a pin that cannot be seen cannot be lined up against a
 * board.
 *
 * @author Branislav Stojkovic
 */
@ComponentDescriptor(name = "LED Bar Graph", author = "Branislav Stojkovic",
    category = "Displays & Outputs", instanceNamePrefix = "BAR",
    description = "Segmented LED bar in a DIP package, for VU and level meters",
    zOrder = IDIYComponent.COMPONENT, bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME,
    keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = LEDBarGraphTransformer.class,
    enableCache = true)
public class LEDBarGraph extends AbstractLabeledComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#2B2B2B");
  public static Color BORDER_COLOR = Color.decode("#1A1A1A");
  public static Color PIN_COLOR = METAL_COLOR;
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  // A bar graph is a scale, not a lamp: green most of the way up, yellow for the last two and
  // red for the topmost, as on a level or VU meter.
  public static Color SEGMENT_GREEN = Color.decode("#43A047");
  public static Color SEGMENT_YELLOW = Color.decode("#FDD835");
  public static Color SEGMENT_RED = Color.decode("#E53935");
  public static Color SEGMENT_BLUE = Color.decode("#1E88E5");
  public static Color SEGMENT_ORANGE = Color.decode("#FB8C00");
  public static Color SEGMENT_WHITE = Color.decode("#EEEEEE");
  public static int EDGE_RADIUS = 4;

  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  // The package being wider than the row spacing is the whole difference from a DIP IC: 10.3
  // against 7.62 leaves about 1.34 mm of body outboard of each row, so no pin shows from above.
  public static Size ROW_SPACING = new Size(7.62d, SizeUnit.mm);
  public static Size PACKAGE_WIDTH = new Size(10.3d, SizeUnit.mm);
  // Sizes of their own rather than the package less a margin, so a segment stays the size it is
  // when the package or the pin pitch changes.
  public static Size SEGMENT_WIDTH = new Size(4.8d, SizeUnit.mm);
  public static Size SEGMENT_LENGTH = new Size(1.7d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Segments segments = Segments._10;
  private BarColor barColor = BarColor.GREEN_YELLOW_RED;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  transient private Area body;

  public LEDBarGraph() {
    super();
    updateControlPoints();
  }

  /**
   * Nothing draws this -- unlike {@code DIL_IC}, which renders it as the chip's part number -- so
   * it reaches only the BOM and keyword search.
   */
  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    body = null;
  }

  @EditableProperty(name = "Segments")
  public Segments getSegments() {
    if (segments == null) {
      segments = Segments._10;
    }
    return segments;
  }

  /** Changing this changes the pin count, so the control points are rebuilt with it. */
  public void setSegments(Segments segments) {
    this.segments = segments;
    updateControlPoints();
    body = null;
  }

  @EditableProperty(name = "Color")
  public BarColor getBarColor() {
    if (barColor == null) {
      barColor = BarColor.GREEN_YELLOW_RED;
    }
    return barColor;
  }

  public void setBarColor(BarColor barColor) {
    this.barColor = barColor;
  }

  /**
   * The colour of one segment, counted from the top of the scale. Delegates to the selected
   * {@link BarColor}.
   */
  private Color getSegmentColor(int index) {
    return getBarColor().getSegmentColor(index, getSegments().getCount());
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null) {
      bodyColor = BODY_COLOR;
    }
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null) {
      borderColor = BORDER_COLOR;
    }
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  /**
   * Puts the segment count and colour in the BOM's value column, which otherwise groups every
   * variant into one row: {@code BomMaker} groups on type name and value, and the inherited value
   * is the part number and usually blank. {@code AbstractMakerBoard.getVariantLabel} does this for
   * the boards in this category; the bar graph is on a different base class, so it is written out
   * here. A part number the user has typed is kept and appended.
   */
  @Override
  public String getValueForDisplay() {
    String variant = getSegments().toString();
    if (getBarColor() != BarColor.GREEN_YELLOW_RED) {
      variant += ", " + getBarColor();
    }
    String partNumber = getValue();
    return partNumber == null || partNumber.trim().isEmpty() ? variant
        : variant + ", " + partNumber.trim();
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  /**
   * Mutates the existing point rather than replacing it, as {@code DIL_IC} does, since callers may
   * hold the instance.
   */
  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  /** Pin 1 is first on the near row and numbering runs round the package, as on any DIP outline. */
  @Override
  public String getControlPointNodeName(int index) {
    int pinCount = getSegments().getPinCount();
    int pin = index + 1;
    if (pin > pinCount / 2) {
      return Integer.toString(pinCount * 3 / 2 - pin + 1);
    }
    return Integer.toString(pin);
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int pinCount = getSegments().getPinCount();
    Point2D[] points = new Point2D[pinCount];
    points[0] = firstPoint;
    double pinSpacing = PIN_SPACING.convertToPixels();
    double rowSpacing = ROW_SPACING.convertToPixels();

    for (int i = 0; i < pinCount / 2; i++) {
      double dx1;
      double dy1;
      double dx2;
      double dy2;
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;
          dx2 = rowSpacing;
          dy2 = i * pinSpacing;
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          dx2 = -i * pinSpacing;
          dy2 = rowSpacing;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          dx2 = -rowSpacing;
          dy2 = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          dx2 = i * pinSpacing;
          dy2 = -rowSpacing;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      points[i] = new Point2D.Double(Math.round(firstPoint.getX() + dx1),
          Math.round(firstPoint.getY() + dy1));
      points[i + pinCount / 2] = new Point2D.Double(Math.round(firstPoint.getX() + dx2),
          Math.round(firstPoint.getY() + dy2));
    }
    controlPoints = points;
  }

  /** How far the plastic reaches past each pin row. */
  private static double getSideOverhang() {
    return (PACKAGE_WIDTH.convertToPixels() - ROW_SPACING.convertToPixels()) / 2.0;
  }

  /**
   * The package outline. Unlike a DIP IC this spans the pin rows rather than sitting between them,
   * and runs half a pin pitch past the end pins at each end, so the pins fall entirely underneath.
   */
  public Area getBody() {
    if (body == null) {
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      double width;
      double height;
      double pinSpacing = PIN_SPACING.convertToPixels();
      double rowSpacing = ROW_SPACING.convertToPixels();
      double packageWidth = PACKAGE_WIDTH.convertToPixels();
      double overhang = getSideOverhang();
      double runLength = getSegments().getCount() * pinSpacing;

      switch (orientation) {
        case DEFAULT:
          width = packageWidth;
          height = runLength;
          x -= overhang;
          y -= pinSpacing / 2;
          break;
        case _90:
          width = runLength;
          height = packageWidth;
          x -= runLength - pinSpacing / 2;
          y -= overhang;
          break;
        case _180:
          width = packageWidth;
          height = runLength;
          x -= rowSpacing + overhang;
          y -= runLength - pinSpacing / 2;
          break;
        case _270:
          width = runLength;
          height = packageWidth;
          x -= pinSpacing / 2;
          y -= rowSpacing + overhang;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body = new Area(new RoundRectangle2D.Double(x, y, width, height, EDGE_RADIUS, EDGE_RADIUS));
    }
    return body;
  }

  /**
   * Each segment is centred on the pin pair that drives it, as on the part. Taking the centre from
   * the two control points rather than dividing the body also makes it orientation-proof: the
   * midpoint of a pin pair is the package centreline at that segment whichever way it is turned.
   */
  private Rectangle2D[] getSegmentRects() {
    int count = getSegments().getCount();
    double across = SEGMENT_WIDTH.convertToPixels();
    double along = SEGMENT_LENGTH.convertToPixels();
    boolean vertical = orientation == Orientation.DEFAULT || orientation == Orientation._180;

    Rectangle2D[] rects = new Rectangle2D[count];
    for (int i = 0; i < count; i++) {
      Point2D near = controlPoints[i];
      Point2D far = controlPoints[i + count];
      double cx = (near.getX() + far.getX()) / 2.0;
      double cy = (near.getY() + far.getY()) / 2.0;
      if (vertical) {
        rects[i] = new Rectangle2D.Double(cx - across / 2.0, cy - along / 2.0, across, along);
      } else {
        rects[i] = new Rectangle2D.Double(cx - along / 2.0, cy - across / 2.0, along, across);
      }
    }
    return rects;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Area mainArea = getBody();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

    Composite oldComposite = applyAlpha(g2d, componentState);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    drawingObserver.startTracking();
    g2d.fill(mainArea);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);

    Color finalBorderColor =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : getBorderColor();
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);

    if (!outlineMode) {
      // Drawn lit, as the Ring and the Strip are: an unlit bar graph is a black block with nothing
      // to say what it is.
      Rectangle2D[] segments = getSegmentRects();
      for (int i = 0; i < segments.length; i++) {
        g2d.setColor(getSegmentColor(i));
        g2d.fill(segments[i]);
        g2d.setColor(getSegmentColor(i).darker());
        g2d.draw(segments[i]);
      }

      // Over the package and the segments: on the real part they are underneath and invisible from
      // above, but a pin you cannot see is a pin you cannot line up. Round, because a bar graph's
      // leads are drawn wire rather than an IC's stamped leadframe.
      double pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      for (Point2D point : controlPoints) {
        Ellipse2D pin = new Ellipse2D.Double(point.getX() - pinSize / 2,
            point.getY() - pinSize / 2, pinSize, pinSize);
        g2d.setColor(PIN_COLOR);
        drawingObserver.startTracking();
        g2d.fill(pin);
        drawingObserver.stopTracking();
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.draw(pin);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(width / 4, 2, width / 2, height - 4, 2, 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(width / 4, 2, width / 2, height - 4, 2, 2);

    // compressed to five bands so the icon reads at toolbox size
    for (int i = 0; i < 5; i++) {
      g2d.setColor(i == 0 ? SEGMENT_RED : i == 1 ? SEGMENT_YELLOW : SEGMENT_GREEN);
      g2d.fillRect(width / 4 + 2, 4 + i * (height - 8) / 5, width / 2 - 3, (height - 8) / 5 - 1);
    }
  }

  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    int margin = 50;
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
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin,
        maxY - minY + 2 * margin);
  }

  /**
   * Decides the rest of the package: each segment has its own anode and cathode, so the pin count
   * is twice the segment count and the body lengthens by one pin pitch per segment.
   */
  public enum Segments {
    _4("4 Segment", 4),
    _5("5 Segment", 5),
    _6("6 Segment", 6),
    _8("8 Segment", 8),
    _10("10 Segment", 10),
    _12("12 Segment", 12);

    private final String label;
    private final int count;

    Segments(String label, int count) {
      this.label = label;
      this.count = count;
    }

    @Override public String toString() { return label; }
    public int getCount() { return count; }

    /** Anode and cathode per segment, split evenly between the two rows. */
    public int getPinCount() { return count * 2; }
  }

  public enum BarColor {
    GREEN_YELLOW_RED("Green-Yellow-Red"),
    BLUE_GREEN_YELLOW_RED("Blue-Green-Yellow-Red"),
    RED("Red"),
    GREEN("Green"),
    YELLOW("Yellow"),
    BLUE("Blue"),
    ORANGE("Orange"),
    WHITE("White");

    private final String label;

    BarColor(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }

    public Color getSegmentColor(int index, int totalSegments) {
      switch (this) {
        case RED:
          return SEGMENT_RED;
        case GREEN:
          return SEGMENT_GREEN;
        case YELLOW:
          return SEGMENT_YELLOW;
        case BLUE:
          return SEGMENT_BLUE;
        case ORANGE:
          return SEGMENT_ORANGE;
        case WHITE:
          return SEGMENT_WHITE;
        case BLUE_GREEN_YELLOW_RED:
          int redCount;
          int yellowCount;
          int blueCount;
          if (totalSegments >= 12) {
            redCount = 2;
            yellowCount = 3;
            blueCount = 2;
          } else if (totalSegments >= 10) {
            redCount = 2;
            yellowCount = 3;
            blueCount = 1;
          } else if (totalSegments >= 8) {
            redCount = 1;
            yellowCount = 2;
            blueCount = 1;
          } else {
            redCount = 1;
            yellowCount = 1;
            blueCount = 1;
          }
          if (index < redCount) {
            return SEGMENT_RED;
          }
          if (index < redCount + yellowCount) {
            return SEGMENT_YELLOW;
          }
          if (index >= totalSegments - blueCount) {
            return SEGMENT_BLUE;
          }
          return SEGMENT_GREEN;
        case GREEN_YELLOW_RED:
        default:
          if (index == 0) {
            return SEGMENT_RED;
          }
          if (index <= 2) {
            return SEGMENT_YELLOW;
          }
          return SEGMENT_GREEN;
      }
    }
  }
}
