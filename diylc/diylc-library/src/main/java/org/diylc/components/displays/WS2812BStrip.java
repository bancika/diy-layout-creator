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
import java.awt.FontMetrics;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAddressableLedBoard;
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
import org.diylc.presenter.CalcUtils;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "NeoPixel Strip", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Addressable RGB WS2812B LED Strip (30, 60 or 144 LEDs per metre)",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BStrip extends AbstractAddressableLedBoard {

  private static final long serialVersionUID = 1L;

  public static Color TAPE_WHITE = Color.decode("#F2F2F2");

  // White tape is the one light-coloured board in this family, so the ink follows the board
  // colour rather than being fixed.
  public static Color SILK_DARK_COLOR = Color.decode("#333333");

  // The uncut pad is a pill lying along the tape with the cut through its middle, so a cut end
  // keeps half of one. Along the tape it is this nominal where there is room and otherwise as
  // much as the density leaves, which at 144/m brings it down to its own width.
  public static Size PAD_WIDTH = new Size(1.5d, SizeUnit.mm);
  public static Size PAD_LENGTH = new Size(3.0d, SizeUnit.mm);

  // Air left between a pad and the nearest package, and between a pad and its printed name.
  public static Size PAD_CLEARANCE = new Size(0.2d, SizeUnit.mm);
  public static Size LABEL_GAP = new Size(0.5d, SizeUnit.mm);

  /**
   * A cut length of tape rather than a board, so the count is a free number. The ceiling is about
   * the shape rather than the control-point array, which stays at six: 144 LEDs of 30/m tape is
   * already most of five metres.
   */
  public static final int MIN_LED_COUNT = 1;
  public static final int MAX_LED_COUNT = 144;
  public static final int DEFAULT_LED_COUNT = 8;

  // Both cuts carry all three lines, which is what makes tape chainable: the rails run the whole
  // length as continuous copper and only the data pad changes sense. The rails are numbered to
  // keep the node names distinct; the tape prints them +5V and GND at both ends.
  public static final String[] PIN_NAMES =
      new String[] {"+5V_1", "DIN", "GND_1", "+5V_2", "DOUT", "GND_2"};

  // The bare designations the tape prints: the rails are not numbered on the part, and the data
  // pad is printed two letters whichever end it is.
  public static final String[] SILK_NAMES =
      new String[] {"+5V", "DI", "GND", "+5V", "DO", "GND"};

  private Density density = Density._60;
  private int ledCount = DEFAULT_LED_COUNT;

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
    updateControlPoints();
    invalidateCache();
  }

  @Override
  protected String getVariantLabel() {
    return getDensity() + ", " + getLedCount() + " LEDs, " + getLedType();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  @Override
  protected String getSilkPinLabel(int index) {
    if (index >= 0 && index < SILK_NAMES.length) {
      return SILK_NAMES[index];
    }
    return super.getSilkPinLabel(index);
  }

  /** The lettering that contrasts with the tape colour the user has chosen. */
  private Color getSilkInk() {
    return CalcUtils.calculateLuminance(bodyColor) < 128d ? SILK_COLOR : SILK_DARK_COLOR;
  }

  private double getPitch() {
    return new Size(getDensity().getPitchMm(), SizeUnit.mm).convertToPixels();
  }

  private double getTapeWidth() {
    return new Size(getDensity().getWidthMm(), SizeUnit.mm).convertToPixels();
  }

  /** Linear in the count rather than proportional to it, since the two cut ends do not scale. */
  private double getTapeLength() {
    return (getLedCount() - 1) * getPitch() + 2 * getLeadIn();
  }

  /** The span a column of three pads occupies across the tape. */
  private static double getPadRunLength() {
    return 2 * PIN_SPACING.convertToPixels();
  }

  /**
   * Tape is a repeating cell one pitch long with its package in the middle and a cut falling on a
   * cell boundary, so this is half a pitch. That is what makes a length exactly as many pitches
   * long as it has LEDs, and keeps the pitch across the joint when two are butted together.
   */
  double getLeadIn() {
    return getPitch() / 2.0;
  }

  /**
   * Half of the pill it was cut from: the nominal length where the density leaves room, otherwise
   * as much as fits between the cut and the first package, never less than its own width.
   */
  double getPadDepth() {
    return getPadLength() / 2.0;
  }

  double getPadLength() {
    double room = 2 * (getLeadIn() - RGB_LED_SIZE.convertToPixels() / 2.0
        - PAD_CLEARANCE.convertToPixels());
    return Math.max(PAD_WIDTH.convertToPixels(),
        Math.min(PAD_LENGTH.convertToPixels(), room));
  }

  /** Control point zero sits here, on the cut, which runs through the pad. */
  private double getBoardX(double x) {
    return x;
  }

  /** Each column of pads is centred across the width, so the first sits above centre. */
  private double getBoardY(double y) {
    return y - (getTapeWidth() - getPadRunLength()) / 2.0;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // Spacing the three rows by the pad pitch rather than by a fraction of the width keeps them
    // apart: fractions of a 10 mm tape would leave them almost edge to edge.
    //
    // The cut runs through the middle of a pad, so a control point sits on the cut itself and the
    // two columns are a whole tape length apart. That is also what makes two lengths chain.
    double columnSpacing = getTapeLength();

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

  /**
   * Names go in the bare strip between the pad and the first package, which the density decides:
   * 12.7 mm at 30/m, 4.3 mm at 60/m and 0.2 mm at 144/m. They are left off where they do not fit
   * rather than shrunk to suit the worst case.
   */
  private void drawCutEndLabels(Graphics2D g2d, double boardX, double boardY) {
    double room = getLeadIn() - RGB_LED_SIZE.convertToPixels() / 2.0 - getPadDepth();
    double gap = LABEL_GAP.convertToPixels();
    double spacing = PIN_SPACING.convertToPixels();
    double firstRow = boardY + (getTapeWidth() - getPadRunLength()) / 2.0;

    g2d.setFont(PIN_ROW_FLAT_FONT);
    g2d.setColor(getSilkInk());
    FontMetrics metrics = g2d.getFontMetrics();

    for (int i = 0; i < PIN_NAMES.length; i++) {
      String label = getSilkPinLabel(i);
      if (label == null || label.isEmpty() || metrics.stringWidth(label) + gap > room) {
        continue;
      }

      boolean input = i < 3;
      double x = input ? boardX + getPadDepth() + gap
          : boardX + getTapeLength() - getPadDepth() - gap;
      StringUtils.drawCenteredText(g2d, label, x, firstRow + (i % 3) * spacing,
          input ? HorizontalAlignment.LEFT : HorizontalAlignment.RIGHT,
          VerticalAlignment.CENTER);
    }
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
      drawCutEndLabels(g2d, boardX, boardY);

      double pitch = getPitch();
      double ledSize = RGB_LED_SIZE.convertToPixels();
      double firstCenter = boardX + getLeadIn();
      int count = getLedCount();
      Color[] colors = getLedColors(count);
      LedType ledType = getLedType();
      for (int i = 0; i < count; i++) {
        MakerBoardPainter.drawAddressableLed(g2d, firstCenter + i * pitch, boardY + tapeH / 2.0,
            ledSize, colors[i], 0, ledType);
      }
    }

    g2d.setTransform(oldTx);

    double padWidth = PAD_WIDTH.convertToPixels();
    drawBisectedPads(g2d, 0, 3, padWidth, getPadLength(), true, outlineMode, drawingObserver);
    drawBisectedPads(g2d, 3, 3, padWidth, getPadLength(), false, outlineMode, drawingObserver);

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
   * Tape is sold by LEDs per metre, so the pitch is derived rather than stored. Width does not
   * follow and is a stated norm rather than a measurement: 10 mm at 30 and 60, 12 mm at 144,
   * where the package leaves no room for the conductor beside it.
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
