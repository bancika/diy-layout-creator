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
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "NeoPixel Strip", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Addressable RGB WS2812B LED Strip (30, 60 or 144 LEDs per metre)",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BStrip extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color TAPE_WHITE = Color.decode("#F2F2F2");

  // Surface copper flush with the cut rather than plated holes set in from it: tape is cut
  // through the middle of a pad pair, so each cut end carries half a pad on its very edge. The
  // 3 x 1.5 mm footprint is the stick's, taken from that part rather than measured on tape, so it
  // is the figure to correct first if the drawing looks wrong.
  public static Size PAD_WIDTH = new Size(3.0d, SizeUnit.mm);
  public static Size PAD_LENGTH = new Size(1.5d, SizeUnit.mm);

  // The air left between a pad and the nearest package.
  public static Size PAD_CLEARANCE = new Size(0.5d, SizeUnit.mm);

  /**
   * A cut length of tape, not a board, so the count is a free number rather than an enum: the part
   * is whatever the builder cuts. The ceiling is not about the control-point array, which stays at
   * six whatever the count, but about the shape -- 144 LEDs of 30/m tape is already most of five
   * metres, and past that the drawing stops being useful before the maths does.
   */
  public static final int MIN_LED_COUNT = 1;
  public static final int MAX_LED_COUNT = 144;
  public static final int DEFAULT_LED_COUNT = 8;

  // Both cuts carry all three lines, which is what makes tape chainable: the supply rails run the
  // whole length as continuous copper and only the data pad changes sense from one end to the
  // other. The rails are numbered to keep the node names distinct, as the stick's grounds are;
  // the tape prints them +5V and GND at both ends.
  public static final String[] PIN_NAMES =
      new String[] {"+5V_1", "DIN", "GND_1", "+5V_2", "DOUT", "GND_2"};

  private Density density = Density._60;
  private int ledCount = DEFAULT_LED_COUNT;

  // Rebuilt only when the count changes, because draw() runs on every repaint.
  private transient Color[] ledColors;

  public WS2812BStrip() {
    super();
    this.bodyColor = TAPE_WHITE;
    updateControlPoints();
  }

  @EditableProperty(name = "Density")
  public Density getDensity() {
    if (density == null) {
      density = Density._60;
    }
    return density;
  }

  public void setDensity(Density density) {
    this.density = density;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "LEDs")
  public int getLedCount() {
    if (ledCount < MIN_LED_COUNT) {
      ledCount = DEFAULT_LED_COUNT;
    }
    return ledCount;
  }

  public void setLedCount(int ledCount) {
    this.ledCount = Math.max(MIN_LED_COUNT, Math.min(MAX_LED_COUNT, ledCount));
    this.ledColors = null;
    updateControlPoints();
    invalidateCache();
  }

  // Both decide what you buy and how much of it, so the BOM carries them together.
  @Override
  protected String getVariantLabel() {
    return getDensity() + ", " + getLedCount() + " LEDs";
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  private double getPitch() {
    return new Size(getDensity().getPitchMm(), SizeUnit.mm).convertToPixels();
  }

  private double getTapeWidth() {
    return new Size(getDensity().getWidthMm(), SizeUnit.mm).convertToPixels();
  }

  /**
   * One pitch of tape per LED, plus a lead-in at each cut end for the pads. The length is linear in
   * the count rather than proportional to it, because those two ends do not scale.
   */
  private double getTapeLength() {
    return (getLedCount() - 1) * getPitch() + 2 * getLeadIn();
  }

  private Color[] getLedColors() {
    int count = getLedCount();
    if (ledColors == null || ledColors.length != count) {
      ledColors = buildLedGradient(count);
    }
    return ledColors;
  }

  /** The span a column of three pads occupies across the tape, at the standard pad pitch. */
  private static double getPadRunLength() {
    return 2 * PIN_SPACING.convertToPixels();
  }

  /**
   * How far the first package's centre sits in from a cut end. The pads come first and the
   * packages must clear them, so this is derived from the pad footprint rather than chosen: the
   * whole pad, a little air, then a half package. Deriving it is what stops a dense tape drawing
   * its first LED on top of its own input pad.
   */
  private double getLeadIn() {
    return PAD_WIDTH.convertToPixels() + PAD_CLEARANCE.convertToPixels()
        + RGB_LED_SIZE.convertToPixels() / 2.0;
  }

  /** Left end of the tape; the pads are flush with it, so their centres sit half a pad inboard. */
  private double getBoardX(double x) {
    return x - PAD_WIDTH.convertToPixels() / 2.0;
  }

  /** Top edge. Each column of pads is centred across the width, so the first sits above centre. */
  private double getBoardY(double y) {
    return y - (getTapeWidth() - getPadRunLength()) / 2.0;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // Each cut end carries +5V, data and GND in the same three rows, because those rows are
    // continuous copper running the length of the tape. Spacing them by the pad pitch rather than
    // by a fraction of the width keeps them from touching: fractions of a 10 mm tape would leave
    // them almost edge to edge.
    //
    // Both columns stand flush against their own cut, so what separates them is the tape less one
    // whole pad. The offsets are measured from control point 0, which is itself half a pad inboard
    // of the left cut; an earlier version inset the pads instead, and reaching a point one inset
    // inboard of the *right* cut then cost two insets rather than one, which put the far pad's
    // centre on the tape edge with half of it hanging off.
    double columnSpacing = getTapeLength() - PAD_WIDTH.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < 3; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
      relativeOffsets[3 + i][0] = columnSpacing;
      relativeOffsets[3 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()), getTapeLength(),
        getTapeWidth(), 4, 4);
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

    double boardX = getBoardX(x);
    double boardY = getBoardY(y);
    double tapeH = getTapeWidth();

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // The LED field starts after the input pads and ends before the output pad, so the first and
      // last packages cannot sit on top of them -- at 144/m the pitch is only 6.94 mm, so a 5 mm
      // package placed half a pitch from the cut would overlap the pad it is meant to clear.
      // Between those ends the LEDs keep the real pitch, which is what the density means.
      double pitch = getPitch();
      double ledSize = RGB_LED_SIZE.convertToPixels();
      double firstCenter = boardX + getLeadIn();
      Color[] colors = getLedColors();
      int count = getLedCount();
      for (int i = 0; i < count; i++) {
        MakerBoardPainter.drawAddressableLed(g2d, firstCenter + i * pitch, boardY + tapeH / 2.0,
            ledSize, colors[i]);
      }
    }

    g2d.setTransform(oldTx);

    drawSurfacePads(g2d, 0, controlPoints.length, PAD_WIDTH, PAD_LENGTH, outlineMode,
        drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(TAPE_WHITE);
    g2d.fill(new RoundRectangle2D.Double(1, height / 2.0 - 5, width - 2, 10, 2, 2));
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(1, height / 2.0 - 5, width - 2, 10, 2, 2));

    Color[] colors = new Color[] {Color.decode("#FFEE00"), Color.decode("#FF1122"),
        Color.decode("#2255FF"), Color.decode("#22DD44")};
    for (int i = 0; i < 4; i++) {
      g2d.setColor(colors[i]);
      g2d.fillRect(3 + i * (width - 6) / 4, (int) (height / 2.0 - 3), 5, 6);
    }
  }

  /**
   * Tape is sold by LEDs per metre, and everything else follows from that: the pitch is the metre
   * divided by the count, so it is derived rather than stored. Width does not follow, and is the
   * one figure here that is a stated norm rather than a measurement -- the 30 and 60 tapes are
   * usually 10 mm and the 144 usually 12 mm, because at that pitch the package leaves no room for
   * the conductor beside it.
   */
  public enum Density {
    _30("30 LED/m", 30, 10.0d),
    _60("60 LED/m", 60, 10.0d),
    _144("144 LED/m", 144, 12.0d);

    private final String label;
    private final int ledsPerMetre;
    private final double widthMm;

    Density(String label, int ledsPerMetre, double widthMm) {
      this.label = label;
      this.ledsPerMetre = ledsPerMetre;
      this.widthMm = widthMm;
    }

    @Override public String toString() { return label; }
    public int getLedsPerMetre() { return ledsPerMetre; }
    public double getWidthMm() { return widthMm; }

    /** Centre-to-centre LED spacing: one metre shared out between that many LEDs. */
    public double getPitchMm() { return 1000.0d / ledsPerMetre; }
  }
}
