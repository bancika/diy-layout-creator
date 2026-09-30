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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
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

@ComponentDescriptor(name = "7-Segment Display", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "7-Segment LED Display (1-Digit, 4-Digit, or TM1637 I2C Driver Module)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class SevenSegmentDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  /**
   * The packages this component can draw, each carrying its own dimensions in millimetres.
   *
   * <p>Provenance differs between them and is worth keeping straight. The digit heights are
   * definitional, since 0.36 and 0.56 inches are what the parts are named for. The 0.56 inch body,
   * digit width and pitch are the figures this class has always carried and have never been checked
   * against a datasheet. The 0.36 inch equivalents are **placeholders**, scaled from the digit
   * height, and are not measurements at all. See the plan's section 11.10; none of this is settled
   * until someone reads a datasheet or measures a part.
   */
  public enum DisplayType {
    SingleDigit_10Pin("1-Digit 0.56\" (10-Pin DIP)", 12.6d, 19.0d, 8.1d, 14.2d, 0d, 15.24d, true),
    FourDigit_0_36_12Pin("4-Digit 0.36\" Bare (12-Pin DIP)", 30.0d, 14.0d, 5.2d, 9.14d, 7.5d,
        10.16d, true),
    FourDigit_0_56_12Pin("4-Digit 0.56\" Bare (12-Pin DIP)", 50.3d, 19.0d, 8.1d, 14.2d, 12.7d,
        15.24d, true),
    TM1637_Module_4Pin("4-Digit TM1637 Module (4-Pin)", 42.0d, 24.0d, 5.5d, 9.2d, 7.62d, 0d, false);

    private final String label;
    private final double bodyWidthMm;
    private final double bodyLengthMm;
    private final double digitWidthMm;
    private final double digitHeightMm;
    private final double digitPitchMm;
    private final double rowSpacingMm;
    private final boolean dualRow;

    DisplayType(String label, double bodyWidthMm, double bodyLengthMm, double digitWidthMm,
        double digitHeightMm, double digitPitchMm, double rowSpacingMm, boolean dualRow) {
      this.label = label;
      this.bodyWidthMm = bodyWidthMm;
      this.bodyLengthMm = bodyLengthMm;
      this.digitWidthMm = digitWidthMm;
      this.digitHeightMm = digitHeightMm;
      this.digitPitchMm = digitPitchMm;
      this.rowSpacingMm = rowSpacingMm;
      this.dualRow = dualRow;
    }

    /**
     * Distance between the two pin rows. It cannot be one shared constant: a package only works if
     * its rows sit within its body, and the 0.36 inch part is shorter than the 0.6 inch spacing the
     * larger packages use.
     */
    public double getRowSpacingMm() { return rowSpacingMm; }

    @Override public String toString() { return label; }
    public double getBodyWidthMm() { return bodyWidthMm; }
    public double getBodyLengthMm() { return bodyLengthMm; }
    public double getDigitWidthMm() { return digitWidthMm; }
    public double getDigitHeightMm() { return digitHeightMm; }
    public double getDigitPitchMm() { return digitPitchMm; }

    /** True for the DIP packages, which carry their pins in two rows rather than one. */
    public boolean isDualRow() { return dualRow; }
  }

  public static Color BODY_BLACK = Color.decode("#1C1C1C");
  public static Color FACE_BLACK = Color.decode("#111111");
  public static Color FACE_BORDER = Color.decode("#333333");
  public static Color LED_RED = Color.decode("#E53935");
  public static Color LED_OFF = Color.decode("#2E2E2E");
  public static Color LED_OFF_BORDER = Color.decode("#222222");

  // Per-package dimensions live on DisplayType; these apply to every package.
  public static Size SEGMENT_THICKNESS = new Size(1.4d, SizeUnit.mm);
  // How far a module's header column sits in from the right edge. The DIP packages straddle their
  // pins with a row either side; a module carries its header down one edge instead.
  public static Size HEADER_EDGE_OFFSET = new Size(2.5d, SizeUnit.mm);
  /**
   * The digit is drawn at this fraction of the dimensions above. On the real part the digit window
   * runs almost to the pin rows -- at full size the two clear each other by around a hundredth of a
   * millimetre -- so a faithful drawing has the pins sitting on the digit. This is a deliberate
   * departure from the measurements for legibility, which is why it is a separate factor rather
   * than smaller digit sizes on {@link DisplayType}: those stay as the package measures.
   */
  public static double DIGIT_DRAW_SCALE = 0.88d;

  public static final String[] PIN_NAMES_1DIGIT = new String[] {
      "e", "d", "COM1", "c", "DP", "b", "a", "COM2", "f", "g"
  };

  public static final String[] PIN_NAMES_4DIGIT = new String[] {
      "D1", "a", "f", "D2", "D3", "b", "e", "d", "DP", "c", "g", "D4"
  };

  public static final String[] PIN_NAMES_TM1637 = new String[] {"CLK", "DIO", "VCC", "GND"};

  private DisplayType displayType = DisplayType.SingleDigit_10Pin;
  private Color ledColor = LED_RED;

  public SevenSegmentDisplay() {
    super();
    this.bodyColor = BODY_BLACK;
    updateControlPoints();
  }

  @EditableProperty(name = "Type")
  public DisplayType getDisplayType() {
    return displayType;
  }

  public void setDisplayType(DisplayType displayType) {
    this.displayType = displayType;
    updateControlPoints();
    invalidateCache();
  }

  @Override
  protected String getVariantLabel() {
    DisplayType displayType = getDisplayType();
    return displayType == null ? null : displayType.toString();
  }

  @EditableProperty(name = "LED Color")
  public Color getLedColor() {
    return ledColor;
  }

  public void setLedColor(Color ledColor) {
    this.ledColor = ledColor;
    invalidateCache();
  }

  /**
   * True for the two bare four-digit packages. They differ in size but not in pin function, so they
   * share a pin array and differ only in what they measure.
   */
  private boolean isFourDigitBare() {
    return displayType == DisplayType.FourDigit_0_36_12Pin
        || displayType == DisplayType.FourDigit_0_56_12Pin;
  }

  private String[] getPinNames() {
    if (displayType == DisplayType.SingleDigit_10Pin) {
      return PIN_NAMES_1DIGIT;
    }
    return isFourDigitBare() ? PIN_NAMES_4DIGIT : PIN_NAMES_TM1637;
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] names = getPinNames();
    if (index >= 0 && index < names.length) {
      return names[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    int pinCount = getPinNames().length;
    double[][] relativeOffsets = new double[pinCount][2];

    if (displayType.isDualRow()) {
      // DIP numbering: pin 1 at the bottom left, along the bottom row, then back along the top
      int perRow = pinCount / 2;
      double rowSpacing =
          new Size(displayType.getRowSpacingMm(), SizeUnit.mm).convertToPixels();
      for (int i = 0; i < perRow; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
        relativeOffsets[perRow + i][0] = (perRow - 1 - i) * spacing;
        relativeOffsets[perRow + i][1] = -rowSpacing;
      }
    } else {
      // a module carries its header as a single column down one edge, not a row across the face
      for (int i = 0; i < pinCount; i++) {
        relativeOffsets[i][0] = 0;
        relativeOffsets[i][1] = i * spacing;
      }
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double boardW = new Size(displayType.getBodyWidthMm(), SizeUnit.mm).convertToPixels();
    double boardH = new Size(displayType.getBodyLengthMm(), SizeUnit.mm).convertToPixels();
    int pinCount = getPinNames().length;
    double boardX;
    double boardY;

    if (displayType.isDualRow()) {
      double rowSpacing =
          new Size(displayType.getRowSpacingMm(), SizeUnit.mm).convertToPixels();
      boardX = p0.getX() - (boardW - (pinCount / 2 - 1) * spacing) / 2.0;
      boardY = p0.getY() - rowSpacing - (boardH - rowSpacing) / 2.0;
    } else {
      boardX = p0.getX() - boardW + HEADER_EDGE_OFFSET.convertToPixels();
      boardY = p0.getY() - (boardH - (pinCount - 1) * spacing) / 2.0;
    }

    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 6, 6);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
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

    Shape boardShape = getBodyShape();
    Rectangle2D bounds = boardShape.getBounds2D();
    double boardX = bounds.getX();
    double boardY = bounds.getY();
    double boardW = bounds.getWidth();
    double boardH = bounds.getHeight();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      if (displayType == DisplayType.SingleDigit_10Pin) {
        // Dark display face
        g2d.setColor(FACE_BLACK);
        g2d.fill(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));
        g2d.setColor(FACE_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));

        // one digit, drawn slightly under size so it keeps clear of the pins
        double dw = new Size(displayType.getDigitWidthMm(), SizeUnit.mm).convertToPixels()
            * DIGIT_DRAW_SCALE;
        double dh = new Size(displayType.getDigitHeightMm(), SizeUnit.mm).convertToPixels()
            * DIGIT_DRAW_SCALE;
        double dx = boardX + (boardW - dw) / 2.0 - 0.5 * SEGMENT_THICKNESS.convertToPixels();
        double dy = boardY + (boardH - dh) / 2.0;
        drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8.", ledColor);

      } else if (isFourDigitBare()) {
        // Dark display face
        g2d.setColor(FACE_BLACK);
        g2d.fill(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));
        g2d.setColor(FACE_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));

        // Four digits on the package's real centre-to-centre pitch, each drawn slightly under size
        // so they keep clear of the pins. The pitch is unscaled, so the digits stay on their true
        // centres and only the gaps between them widen, and the row is centred on the body rather
        // than measured from its left edge, so it stays put across packages of different widths.
        double dw = new Size(displayType.getDigitWidthMm(), SizeUnit.mm).convertToPixels()
            * DIGIT_DRAW_SCALE;
        double dh = new Size(displayType.getDigitHeightMm(), SizeUnit.mm).convertToPixels()
            * DIGIT_DRAW_SCALE;
        double pitch = new Size(displayType.getDigitPitchMm(), SizeUnit.mm).convertToPixels();
        double firstCenterX = boardX + boardW / 2.0 - 1.5 * pitch;
        double dy = boardY + (boardH - dh) / 2.0;

        for (int d = 0; d < 4; d++) {
          double centerX = firstCenterX + d * pitch;
          double dx = centerX - dw / 2.0 - 0.5 * SEGMENT_THICKNESS.convertToPixels();
          drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8.", ledColor);
        }

        // Colon between the middle pair, sized off the glyph grid like the decimal point is, so it
        // tracks the digit instead of staying a fixed few pixels across.
        double colonX = boardX + boardW / 2.0;
        double dotD = dw / GLYPH_WIDTH * 2.0;
        g2d.setColor(ledColor);
        g2d.fill(new Ellipse2D.Double(colonX - dotD / 2.0, dy + dh * 0.35 - dotD / 2.0, dotD, dotD));
        g2d.fill(new Ellipse2D.Double(colonX - dotD / 2.0, dy + dh * 0.65 - dotD / 2.0, dotD, dotD));

      } else if (displayType == DisplayType.TM1637_Module_4Pin) {
        MakerBoardPainter.drawMountingHole(g2d, boardX + 14, boardY + 14, 12);
        MakerBoardPainter.drawMountingHole(g2d, boardX + 14, boardY + boardH - 14, 12);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - 14, boardY + 14, 12);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - 14, boardY + boardH - 14, 12);

        // Display Bezel (Center)
        double bezelW = new Size(30.0d, SizeUnit.mm).convertToPixels();
        double bezelH = new Size(14.0d, SizeUnit.mm).convertToPixels();
        double bezelX = boardX + (boardW - bezelW) / 2.0;
        double bezelY = boardY + (boardH - bezelH) / 2.0;

        g2d.setColor(FACE_BLACK);
        g2d.fill(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 4, 4));
        g2d.setColor(FACE_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 4, 4));

        // 4 Digits inside 0.36" bezel
        double dw = new Size(5.5d, SizeUnit.mm).convertToPixels();
        double dh = new Size(9.2d, SizeUnit.mm).convertToPixels();
        double pitch = new Size(7.62d, SizeUnit.mm).convertToPixels();
        double firstCenterX = bezelX + (bezelW - 3 * pitch) / 2.0;
        double dy = bezelY + (bezelH - dh) / 2.0;

        for (int d = 0; d < 4; d++) {
          double centerX = firstCenterX + d * pitch;
          double dx = centerX - dw / 2.0 - 0.5 * SEGMENT_THICKNESS.convertToPixels() * 0.65;
          drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8.", ledColor);
        }

        // same colon treatment as the bare packages: sized and placed off the digit, not in pixels
        double colonX = bezelX + bezelW / 2.0;
        double dotD = dw / GLYPH_WIDTH * 2.0;
        g2d.setColor(ledColor);
        g2d.fill(new Ellipse2D.Double(colonX - dotD / 2.0, dy + dh * 0.35 - dotD / 2.0, dotD, dotD));
        g2d.fill(new Ellipse2D.Double(colonX - dotD / 2.0, dy + dh * 0.65 - dotD / 2.0, dotD, dotD));
      }
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  private static final int SEG_A = 1 << 0;
  private static final int SEG_B = 1 << 1;
  private static final int SEG_C = 1 << 2;
  private static final int SEG_D = 1 << 3;
  private static final int SEG_E = 1 << 4;
  private static final int SEG_F = 1 << 5;
  private static final int SEG_G = 1 << 6;
  private static final int SEG_DP = 1 << 7;

  private static int getSegmentMask(char c) {
    switch (c) {
      case '0': return SEG_A | SEG_B | SEG_C | SEG_D | SEG_E | SEG_F;
      case '1': return SEG_B | SEG_C;
      case '2': return SEG_A | SEG_B | SEG_G | SEG_E | SEG_D;
      case '3': return SEG_A | SEG_B | SEG_G | SEG_C | SEG_D;
      case '4': return SEG_F | SEG_G | SEG_B | SEG_C;
      case '5': return SEG_A | SEG_F | SEG_G | SEG_C | SEG_D;
      case '6': return SEG_A | SEG_F | SEG_E | SEG_D | SEG_C | SEG_G;
      case '7': return SEG_A | SEG_B | SEG_C;
      case '8': return SEG_A | SEG_B | SEG_C | SEG_D | SEG_E | SEG_F | SEG_G;
      case '9': return SEG_A | SEG_B | SEG_C | SEG_D | SEG_F | SEG_G;
      case 'A': case 'a': return SEG_A | SEG_B | SEG_C | SEG_E | SEG_F | SEG_G;
      case 'B': case 'b': return SEG_C | SEG_D | SEG_E | SEG_F | SEG_G;
      case 'C': case 'c': return SEG_A | SEG_D | SEG_E | SEG_F;
      case 'D': case 'd': return SEG_B | SEG_C | SEG_D | SEG_E | SEG_G;
      case 'E': case 'e': return SEG_A | SEG_D | SEG_E | SEG_F | SEG_G;
      case 'F': case 'f': return SEG_A | SEG_E | SEG_F | SEG_G;
      case '-': return SEG_G;
      case '.': return SEG_DP;
      default: return SEG_A | SEG_B | SEG_C | SEG_D | SEG_E | SEG_F | SEG_G;
    }
  }

  /**
   * The seven segments as a unit-grid glyph: a 10 x 18 box with segments two units thick, mitred
   * at 45 degrees. Neighbours meet at a shared vertex rather than being held apart by a gap, and
   * the visible separation between them comes from outlining each segment in the background
   * colour. Index order is A through G, matching the SEG_* bits.
   */
  private static final double[][][] SEGMENT_POINTS = new double[][][] {
      {{1, 1}, {2, 0}, {8, 0}, {9, 1}, {8, 2}, {2, 2}},        // A, top
      {{9, 1}, {10, 2}, {10, 8}, {9, 9}, {8, 8}, {8, 2}},      // B, upper right
      {{9, 9}, {10, 10}, {10, 16}, {9, 17}, {8, 16}, {8, 10}}, // C, lower right
      {{9, 17}, {8, 18}, {2, 18}, {1, 17}, {2, 16}, {8, 16}},  // D, bottom
      {{1, 17}, {0, 16}, {0, 10}, {1, 9}, {2, 10}, {2, 16}},   // E, lower left
      {{1, 9}, {0, 8}, {0, 2}, {1, 1}, {2, 2}, {2, 8}},        // F, upper left
      {{1, 9}, {2, 8}, {8, 8}, {9, 9}, {8, 10}, {2, 10}}       // G, middle
  };

  /** Width of the glyph grid above, in its own units. */
  private static final double GLYPH_WIDTH = 10.0d;

  /** Height of the glyph grid above, in its own units. */
  private static final double GLYPH_HEIGHT = 18.0d;

  /**
   * Draws one upright digit into the box at {@code x, y, w, h}, with the segment shapes taken from
   * {@link #SEGMENT_POINTS} scaled to that box. Unlit segments are drawn first as a dim
   * silhouette, then the lit ones, and finally every segment is outlined in the face colour so
   * that neighbours which share a vertex still read as separate segments.
   */
  private void drawSevenSegmentDigit(Graphics2D g2d, double x, double y, double w, double h, String charToDisplay, Color onColor) {
    AffineTransform orig = g2d.getTransform();

    g2d.translate(x, y);

    double sx = w / GLYPH_WIDTH;
    double sy = h / GLYPH_HEIGHT;

    Shape[] segments = new Shape[SEGMENT_POINTS.length];
    for (int i = 0; i < SEGMENT_POINTS.length; i++) {
      double[][] points = SEGMENT_POINTS[i];
      Path2D.Double path = new Path2D.Double();
      path.moveTo(points[0][0] * sx, points[0][1] * sy);
      for (int p = 1; p < points.length; p++) {
        path.lineTo(points[p][0] * sx, points[p][1] * sy);
      }
      path.closePath();
      segments[i] = path;
    }

    // the decimal point is not part of the glyph grid, so it is placed in the same units: one
    // segment thickness across, sitting on the baseline just clear of the digit
    double dpD = 2.0 * sx;
    Shape dpShape = new Ellipse2D.Double(GLYPH_WIDTH * sx + 0.5 * sx, 18.0 * sy - dpD, dpD, dpD);

    int mask = getSegmentMask(charToDisplay.isEmpty() ? '8' : charToDisplay.charAt(0));

    // 1. Draw unlit segment silhouettes (dim background)
    g2d.setColor(LED_OFF);
    for (int i = 0; i < 7; i++) {
      if ((mask & (1 << i)) == 0) {
        g2d.fill(segments[i]);
      }
    }
    if ((mask & SEG_DP) == 0 && !charToDisplay.contains(".")) {
      g2d.fill(dpShape);
    }

    // 2. Draw lit active segments
    g2d.setColor(onColor);
    for (int i = 0; i < 7; i++) {
      if ((mask & (1 << i)) != 0) {
        g2d.fill(segments[i]);
      }
    }
    if ((mask & SEG_DP) != 0 || charToDisplay.contains(".")) {
      g2d.fill(dpShape);
    }

    // The source drawing outlines every segment in its own background colour, which is what stops
    // neighbours that share a vertex from fusing into one shape. The background here is the
    // display face, so the outline takes that colour rather than the white the drawing uses.
    g2d.setColor(FACE_BLACK);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((float) (0.25 * (sx + sy) / 2.0)));
    for (Shape segment : segments) {
      g2d.draw(segment);
    }

    g2d.setTransform(orig);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BODY_BLACK);
    g2d.fill(new RoundRectangle2D.Double(6, 3, width - 12, height - 6, 3, 3));
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(new RoundRectangle2D.Double(6, 3, width - 12, height - 6, 3, 3));

    // Draw single stylized '8'
    drawSevenSegmentDigit(g2d, 9, 5, width - 18, height - 10, "8.", LED_RED);
  }
}
