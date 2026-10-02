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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.MakerBoardPainter;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

// A sibling of WS2812BRing rather than another RingSize constant. It shares the part family and the
// pad footprint but not the footprint family, which is what decides this: the Jewel is a solid disc
// with no inner rim, six of its seven LEDs sit on a circle with the seventh in the middle, its pads
// sit inside that circle instead of out by the rim, and it carries two mounting holes no ring has.
// Expressing that through RingSize would have taken a centre-LED flag plus three per-variant
// dimensions the rings derive from their rims, so it goes its own way.
@ComponentDescriptor(name = "NeoPixel Jewel", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Addressable RGB WS2812B NeoPixel Jewel (7 LEDs)",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BJewel extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NEO_BLACK = Color.decode("#111111");

  public static Size OUTER_DIAMETER = new Size(23.0d, SizeUnit.mm);
  // Measured, not derived: with no inner rim there is no midpoint to put the LEDs on, which is how
  // the rings place theirs.
  public static Size LED_CIRCLE_DIAMETER = new Size(16.0d, SizeUnit.mm);
  // The pads sit inside the LED circle, where the rings put theirs just in from the outer rim.
  public static Size PAD_CIRCLE_DIAMETER = new Size(9.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SPACING = new Size(19.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(3.0d, SizeUnit.mm);

  // The same 5050-family pads the rings carry.
  public static Size PAD_WIDTH = new Size(1.6d, SizeUnit.mm);
  public static Size PAD_LENGTH = new Size(1.3d, SizeUnit.mm);
  public static Size PAD_HOLE = new Size(0.7d, SizeUnit.mm);

  /** The LEDs on the circle. The seventh sits in the middle and is not one of them. */
  public static final int RING_LED_COUNT = 6;

  private static final String[] PAD_NAMES = new String[] {"OUT", "G_1", "G_2", "PWR", "IN"};

  // Which gap between two consecutive ring LEDs each pad sits in, counted from the first pad.
  // OUT is in the gap anticlockwise of the top LED and the rest follow it clockwise one LED apart,
  // so the sixth gap carries no pad -- it is the one due left, where the left mounting hole sits.
  private static final int[] PAD_GAP_INDEX = new int[] {0, 1, 2, 3, 4};

  // Drawn lit, as the rings are: unlit it is a black disc with white specks on it, which reads
  // as no particular part.
  public static Color[] LED_COLORS = buildLedGradient(RING_LED_COUNT + 1);

  public WS2812BJewel() {
    super();
    this.bodyColor = NEO_BLACK;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PAD_NAMES.length) {
      return PAD_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  @Override
  protected Size getSolderPadWidth() {
    return PAD_WIDTH;
  }

  @Override
  protected Size getSolderPadLength() {
    return PAD_LENGTH;
  }

  @Override
  protected Size getSolderPadHoleSize() {
    return PAD_HOLE;
  }

  private double getOuterRadius() {
    return OUTER_DIAMETER.convertToPixels() / 2.0;
  }

  /**
   * This has to be the one place the pad radius is decided: {@link #updateControlPoints()} offsets
   * every pad from the first one by it, and {@link #getCenter()} works backwards from the first pad
   * to find the middle of the disc, so the two disagreeing would leave the body drawn away from its
   * own control points.
   */
  private double getPadRadius() {
    return PAD_CIRCLE_DIAMETER.convertToPixels() / 2.0;
  }

  /**
   * Pads are not grouped: each sits in a gap between two consecutive ring LEDs, the gaps being the
   * ones {@link #PAD_GAP_INDEX} records. Where the sequence starts is arbitrary, since the
   * component rotates.
   */
  private double getPadAngle(int index) {
    double gap = PAD_GAP_INDEX[index] + RING_LED_COUNT - 0.5;
    return 2 * Math.PI * gap / RING_LED_COUNT - Math.PI / 2.0;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double padR = getPadRadius();
    double theta0 = getPadAngle(0);

    double[][] relativeOffsets = new double[PAD_NAMES.length][2];
    for (int i = 0; i < PAD_NAMES.length; i++) {
      double theta = getPadAngle(i);
      relativeOffsets[i][0] = padR * (Math.cos(theta) - Math.cos(theta0));
      relativeOffsets[i][1] = padR * (Math.sin(theta) - Math.sin(theta0));
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  private Point2D getCenter() {
    Point2D p0 = controlPoints[0];
    double padR = getPadRadius();
    double theta0 = getPadAngle(0);
    // unrotated, the centre sits one pad radius back along the first pad's angle
    return new Point2D.Double(p0.getX() - padR * Math.cos(theta0),
        p0.getY() - padR * Math.sin(theta0));
  }

  @Override
  public Shape getBodyShape() {
    Point2D center = getCenter();
    double outerR = getOuterRadius();
    return new Ellipse2D.Double(center.getX() - outerR, center.getY() - outerR, outerR * 2,
        outerR * 2);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    AffineTransform oldTx = g2d.getTransform();
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.toRadians(), x, y);
    }

    Point2D center = getCenter();
    double cx = center.getX();
    double cy = center.getY();
    double outerR = getOuterRadius();

    Shape disc = new Ellipse2D.Double(cx - outerR, cy - outerR, outerR * 2, outerR * 2);

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(disc);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(disc);

    if (!outlineMode) {
      double holeOffset = MOUNTING_HOLE_SPACING.convertToPixels() / 2.0;
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, cx - holeOffset, cy, holeSize);
      MakerBoardPainter.drawMountingHole(g2d, cx + holeOffset, cy, holeSize);

      double ledR = LED_CIRCLE_DIAMETER.convertToPixels() / 2.0;
      double ledSize = RGB_LED_SIZE.convertToPixels();

      for (int i = 0; i < RING_LED_COUNT; i++) {
        double angle = 2 * Math.PI * i / RING_LED_COUNT - Math.PI / 2.0;
        MakerBoardPainter.drawAddressableLed(g2d, cx + ledR * Math.cos(angle),
            cy + ledR * Math.sin(angle), ledSize, LED_COLORS[i % LED_COLORS.length],
            angle + Math.PI / 2.0);
      }

      // the centre package has no radius to face along, so it stays square to the board
      MakerBoardPainter.drawAddressableLed(g2d, cx, cy, ledSize,
          LED_COLORS[RING_LED_COUNT % LED_COLORS.length]);

      // Pad names are left to the node tooltips and the netlist: a pad sits in the gap between two
      // LEDs, which is nowhere near enough room for its name.
    }

    g2d.setTransform(oldTx);

    drawSolderPads(g2d, 0, PAD_NAMES.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int cx = width / 2;
    int cy = height / 2;
    int outerR = width / 2 - 2;

    Ellipse2D disc = new Ellipse2D.Double(cx - outerR, cy - outerR, outerR * 2, outerR * 2);
    g2d.setColor(NEO_BLACK);
    g2d.fill(disc);
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(disc);

    Color[] rainbow = new Color[] {
        Color.decode("#FF3333"),
        Color.decode("#FFD700"),
        Color.decode("#00E676"),
        Color.decode("#00E5FF"),
        Color.decode("#E040FB"),
        Color.decode("#FF6B35")
    };

    double ledR = outerR * 0.6;
    int dotR = Math.max(2, (int) (outerR * 0.22));

    for (int i = 0; i < RING_LED_COUNT; i++) {
      double angle = 2 * Math.PI * i / RING_LED_COUNT - Math.PI / 2.0;
      double lx = cx + ledR * Math.cos(angle);
      double ly = cy + ledR * Math.sin(angle);
      g2d.setColor(rainbow[i % rainbow.length]);
      g2d.fill(new Ellipse2D.Double(lx - dotR, ly - dotR, dotR * 2, dotR * 2));
    }

    // The filled centre is what separates this icon from the ring's at toolbox size.
    g2d.setColor(Color.WHITE);
    g2d.fill(new Ellipse2D.Double(cx - dotR, cy - dotR, dotR * 2, dotR * 2));
  }
}
