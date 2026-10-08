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
    author = "Branislav Stojkovic", description = "7-Segment LED Display (1-Digit, 2-Digit, 4-Digit, or TM1637 I2C Driver Module)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class SevenSegmentDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color BODY_BLACK = Color.decode("#1C1C1C");
  public static Color FACE_BLACK = Color.decode("#111111");
  public static Color FACE_BORDER = Color.decode("#333333");
  public static Color LED_RED = Color.decode("#E53935");
  public static Color LED_OFF = Color.decode("#2E2E2E");
  public static Color LED_OFF_BORDER = Color.decode("#222222");

  public static Size SEGMENT_THICKNESS = new Size(1.4d, SizeUnit.mm);
  // How far a module's header column sits in from the right edge. The DIP packages straddle their
  // pins with a row either side; a module carries its header down one edge instead.
  public static Size HEADER_EDGE_OFFSET = new Size(2.5d, SizeUnit.mm);
  /**
   * The digit is drawn at this fraction of the package's measured size. On the real part the
   * digit window runs almost to the pin rows, so a faithful drawing has the pins sitting on the
   * digit; this is a deliberate departure for legibility, which is why it is a separate factor
   * rather than smaller digit sizes on {@link DisplayType}. It is shared, so changing it to chase
   * an exact drawn size moves every package.
   */
  public static double DIGIT_DRAW_SCALE = 0.88d;

  /**
   * Pin arrays are in DIP order: index 0 is pin 1 at the bottom left, counting right along the
   * bottom row and then back right-to-left along the top, which is how {@code updateControlPoints}
   * places the control points. The two commons are the same net on the part and are numbered only
   * to keep the node names distinct.
   */
  public static final String[] PIN_NAMES_1DIGIT = new String[] {
      "E", "D", "COM1", "C", "DP", "B", "A", "COM2", "F", "G"
  };

  /** The two-digit member of the same family as the 5641AS, with two digit commons instead of
   * four. */
  public static final String[] PIN_NAMES_2DIGIT = new String[] {
      "E", "D", "DP", "C", "G", "B", "D2", "D1", "F", "A"
  };

  /** Shared by both bare four-digit packages, which differ in size but not in pin function. */
  public static final String[] PIN_NAMES_4DIGIT = new String[] {
      "E", "D", "DP", "C", "G", "D4", "B", "D3", "D2", "F", "A", "D1"
  };

  public static final String[] PIN_NAMES_TM1637 = new String[] {"CLK", "DIO", "VCC", "GND"};

  /** The blue of a bare PCB, matching the board under the OLED module. */
  public static Color PCB_BLUE = Color.decode("#004488");

  private DisplayType displayType = DisplayType.SingleDigit_10Pin;
  private Common common = Common.Cathode;
  private Punctuation punctuation = Punctuation.DecimalPoints;
  private Color ledColor = LED_RED;
  private Color boardColor = PCB_BLUE;

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

  /**
   * No drawing path reads this: the two kinds share a pinout, a package and a set of dimensions,
   * so it exists to split them in the BOM. Defaulted in the getter so that a project saved before
   * the property existed still loads.
   */
  @EditableProperty(name = "Common")
  public Common getCommon() {
    if (common == null) {
      common = Common.Cathode;
    }
    return common;
  }

  public void setCommon(Common common) {
    this.common = common;
  }

  /**
   * Decides whether the decimal points and the colon are drawn. The colon is meaningless on the
   * single-digit package and is ignored there.
   */
  @EditableProperty(name = "Punctuation")
  public Punctuation getPunctuation() {
    if (punctuation == null) {
      punctuation = Punctuation.DecimalPoints;
    }
    return punctuation;
  }

  public void setPunctuation(Punctuation punctuation) {
    this.punctuation = punctuation;
    invalidateCache();
  }

  @Override
  protected String getVariantLabel() {
    DisplayType displayType = getDisplayType();
    if (displayType == null) {
      return null;
    }
    // A module drives the digits itself and brings out no common pins, so polarity is not part of
    // what you order -- but which punctuation it carries still is.
    if (displayType.isModule()) {
      return displayType + ", " + getPunctuation();
    }
    return displayType + ", " + getCommon() + ", " + getPunctuation();
  }

  @EditableProperty(name = "LED Color")
  public Color getLedColor() {
    return ledColor;
  }

  public void setLedColor(Color ledColor) {
    this.ledColor = ledColor;
    invalidateCache();
  }

  /** Only the TM1637 has one; the bare packages are a moulding with pins in it. */
  @EditableProperty(name = "Board Color")
  public Color getBoardColor() {
    return boardColor;
  }

  public void setBoardColor(Color boardColor) {
    this.boardColor = boardColor;
    invalidateCache();
  }

  /**
   * The display device itself: the whole part on a bare package, and the module mounted on the
   * board on a TM1637. Relabelled from the inherited "Board Color", which fits neither.
   */
  @EditableProperty(name = "Body")
  @Override
  public Color getBodyColor() {
    return super.getBodyColor();
  }

  private String[] getPinNames() {
    return displayType.getPinNames();
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] names = getPinNames();
    if (index >= 0 && index < names.length) {
      return names[index];
    }
    return "Pin " + (index + 1);
  }

  private static double px(double millimetres) {
    return new Size(millimetres, SizeUnit.mm).convertToPixels();
  }

  /**
   * The colon sits in the gap between the two middle digits, which exists only on an even count.
   */
  private boolean hasMiddlePair() {
    int digitCount = displayType.getDigitCount();
    return digitCount >= 2 && digitCount % 2 == 0;
  }

  /**
   * Draws the package's digits on their own pitch, centred on {@code rowCentreX}. Bare packages
   * and modules differ only in the figures they pass, which is what lets a two-, six- or
   * eight-digit part be a constant rather than a branch.
   */
  private void drawDigitRow(Graphics2D g2d, double rowCentreX, double dy, double dw, double dh,
      double pitch, double nudge, boolean decimalPoints) {
    int digitCount = displayType.getDigitCount();
    double firstCentreX = rowCentreX - (digitCount - 1) * pitch / 2.0;
    for (int d = 0; d < digitCount; d++) {
      double dx = firstCentreX + d * pitch - dw / 2.0 - nudge;
      drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8.", ledColor, decimalPoints);
    }
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    int pinCount = getPinNames().length;
    double[][] relativeOffsets = new double[pinCount][2];

    if (displayType.isDualRow()) {
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

    Color outlineFill = displayType.isModule() ? boardColor : bodyColor;

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : outlineFill);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      if (!displayType.isModule()) {
        // the recessed window is a shade of the moulding, so it follows the body colour
        g2d.setColor(bodyColor.darker());
        g2d.fill(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));
        g2d.setColor(FACE_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));

        // The pitch is unscaled while the digits are drawn slightly under size, so they keep their
        // true centres and only the gaps widen. The row is centred on the body rather than measured
        // from its left edge, so it stays put across packages of different widths. A single-digit
        // package is the same construction with a count of one.
        double dw = px(displayType.getDigitWidthMm()) * DIGIT_DRAW_SCALE;
        double dh = px(displayType.getDigitHeightMm()) * DIGIT_DRAW_SCALE;
        double pitch = px(displayType.getDigitPitchMm());
        double dy = boardY + (boardH - dh) / 2.0;
        double rowCentreX = boardX + boardW / 2.0;

        Punctuation punctuation = getPunctuation();
        drawDigitRow(g2d, rowCentreX, dy, dw, dh, pitch, 0.5 * SEGMENT_THICKNESS.convertToPixels(),
            punctuation.hasDecimalPoints());

        if (punctuation.hasColon() && hasMiddlePair()) {
          // Colon sized off the glyph grid like the decimal point, so it tracks the digit rather
          // than staying a fixed few pixels across. The row is centred, so the gap between the two
          // middle digits is the body's centre line.
          double dotD = dw / GLYPH_WIDTH * 2.0;
          double colonX = rowCentreX - dotD / 2.0;
          g2d.setColor(ledColor);
          g2d.fill(new Ellipse2D.Double(colonX, dy + dh * 0.35 - dotD / 2.0, dotD, dotD));
          g2d.fill(new Ellipse2D.Double(colonX, dy + dh * 0.65 - dotD / 2.0, dotD, dotD));
        }

      } else {
        Module module = displayType.getModule();
        double holeInset = px(module.getHoleInsetMm());
        double holeSize = px(module.getHoleSizeMm());
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardH - holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
            boardY + boardH - holeInset, holeSize);

        double bezelW = px(module.getBezelWidthMm());
        double bezelH = px(module.getBezelLengthMm());
        double bezelX = boardX + (boardW - bezelW) / 2.0;
        double bezelY = boardY + (boardH - bezelH) / 2.0;

        g2d.setColor(bodyColor);
        g2d.fill(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 4, 4));
        g2d.setColor(FACE_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 4, 4));

        // at full size here: the module's digits sit on a bezel rather than between pin rows
        double dw = px(displayType.getDigitWidthMm());
        double dh = px(displayType.getDigitHeightMm());
        double pitch = px(displayType.getDigitPitchMm());
        double dy = bezelY + (bezelH - dh) / 2.0;

        Punctuation punctuation = getPunctuation();
        drawDigitRow(g2d, bezelX + bezelW / 2.0, dy, dw, dh, pitch,
            0.5 * SEGMENT_THICKNESS.convertToPixels() * 0.65, punctuation.hasDecimalPoints());
        if (punctuation.hasColon() && hasMiddlePair()) {
          double dotD = dw / GLYPH_WIDTH * 2.0;
          double colonX = bezelX + bezelW / 2.0 - dotD / 2.0;
          g2d.setColor(ledColor);
          g2d.fill(new Ellipse2D.Double(colonX, dy + dh * 0.35 - dotD / 2.0, dotD, dotD));
          g2d.fill(new Ellipse2D.Double(colonX, dy + dh * 0.65 - dotD / 2.0, dotD, dotD));
        }
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
   * at 45 degrees. Neighbours meet at a shared vertex rather than being held apart by a gap, so
   * the separation between them comes from outlining each segment in the background colour. Index
   * order is A through G, matching the SEG_* bits.
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
   * Draws one upright digit into the box at {@code x, y, w, h} from {@link #SEGMENT_POINTS}.
   * Unlit segments are drawn first as a dim silhouette, then the lit ones, then every segment is
   * outlined in the face colour so that neighbours sharing a vertex still read as separate.
   */
  private void drawSevenSegmentDigit(Graphics2D g2d, double x, double y, double w, double h,
      String charToDisplay, Color onColor, boolean showDecimalPoint) {
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

    g2d.setColor(LED_OFF);
    for (int i = 0; i < 7; i++) {
      if ((mask & (1 << i)) == 0) {
        g2d.fill(segments[i]);
      }
    }
    if (showDecimalPoint && (mask & SEG_DP) == 0 && !charToDisplay.contains(".")) {
      g2d.fill(dpShape);
    }

    g2d.setColor(onColor);
    for (int i = 0; i < 7; i++) {
      if ((mask & (1 << i)) != 0) {
        g2d.fill(segments[i]);
      }
    }
    if (showDecimalPoint && ((mask & SEG_DP) != 0 || charToDisplay.contains("."))) {
      g2d.fill(dpShape);
    }

    // Outlining every segment in the background colour is what stops neighbours that share a
    // vertex from fusing into one shape.
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

    // the icon stands for the component type, so it keeps its decimal point whatever
    // Punctuation is set to
    drawSevenSegmentDigit(g2d, 9, 5, width - 18, height - 10, "8.", LED_RED, true);
  }

  /**
   * The packages this component can draw, each carrying its own dimensions in millimetres.
   *
   * <p>Digit heights are definitional -- 0.36 and 0.56 inches are what the parts are named for.
   * The 0.56 inch body, digit width and pitch have never been checked against a datasheet, and
   * the 0.36 inch equivalents are placeholders scaled from the digit height rather than
   * measurements.
   */
  public enum DisplayType {
    // 5161AS, 5621AS, 3641AS and 5641AS. The two four-digit parts differ in size but share a
    // pinout, which is why one array serves both.
    SingleDigit_10Pin("1-Digit 0.56\"", 12.7d, 19.0d, 8.1d, 14.2d, 0d, 15.24d, true, 1,
        PIN_NAMES_1DIGIT, null),
    TwoDigit_0_56_10Pin("2-Digit 0.56\"", 25.0d, 19.0d, 8.1d, 14.2d, 12.7d, 15.24d, true, 2,
        PIN_NAMES_2DIGIT, null),
    FourDigit_0_36_12Pin("4-Digit 0.36\"", 30.0d, 14.0d, 5.2d, 9.14d, 7.5d, 10.16d, true, 4,
        PIN_NAMES_4DIGIT, null),
    FourDigit_0_56_12Pin("4-Digit 0.56\"", 50.3d, 19.0d, 8.1d, 14.2d, 12.7d, 15.24d, true, 4,
        PIN_NAMES_4DIGIT, null),
    TM1637_Module_4Pin("4-Digit TM1637 Module", 42.0d, 24.0d, 5.5d, 9.2d, 7.62d, 0d, false, 4,
        PIN_NAMES_TM1637, new Module(30.0d, 14.0d, 1.778d, 1.524d));

    private final String label;
    private final double bodyWidthMm;
    private final double bodyLengthMm;
    private final double digitWidthMm;
    private final double digitHeightMm;
    private final double digitPitchMm;
    private final double rowSpacingMm;
    private final boolean dualRow;
    private final int digitCount;
    private final String[] pinNames;
    private final Module module;

    DisplayType(String label, double bodyWidthMm, double bodyLengthMm, double digitWidthMm,
        double digitHeightMm, double digitPitchMm, double rowSpacingMm, boolean dualRow,
        int digitCount, String[] pinNames, Module module) {
      this.label = label;
      this.bodyWidthMm = bodyWidthMm;
      this.bodyLengthMm = bodyLengthMm;
      this.digitWidthMm = digitWidthMm;
      this.digitHeightMm = digitHeightMm;
      this.digitPitchMm = digitPitchMm;
      this.rowSpacingMm = rowSpacingMm;
      this.dualRow = dualRow;
      this.digitCount = digitCount;
      this.pinNames = pinNames;
      this.module = module;
    }

    /**
     * Distance between the two pin rows. It cannot be shared: a package only works if its rows sit
     * within its body, and the 0.36 inch part is shorter than the 0.6 inch the larger ones use.
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

    /** Carried rather than inferred, so a two-, six- or eight-digit part is a constant rather than
     * another branch through {@code draw}. */
    public int getDigitCount() { return digitCount; }

    /** The part's pins in DIP order. */
    public String[] getPinNames() { return pinNames; }

    /**
     * The board a driven module is built on, or {@code null} for a bare package. A module carries
     * its own driver, so it brings out no common pins and its polarity is not something you order.
     */
    public Module getModule() { return module; }

    public boolean isModule() { return module != null; }
  }

  /**
   * Which electrode the digits share. The two are identical in pinout, package and dimensions, so
   * this changes nothing that is drawn and reaches only the BOM.
   */
  public enum Common {
    Cathode("Common Cathode"),
    Anode("Common Anode");

    private final String label;
    Common(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  /**
   * What the package carries besides the digits. On a twelve-pin part these are not independent:
   * A-G plus DP and four digit commons already account for every pin, so a colon has to share the
   * decimal point's line or replace it. The property describes the face of the part, not a pin.
   */
  public enum Punctuation {
    None("No Punctuation"),
    DecimalPoints("Decimal Points"),
    Colon("Colon"),
    Both("Colon + Decimal Points");

    private final String label;
    Punctuation(String label) { this.label = label; }
    @Override public String toString() { return label; }

    public boolean hasDecimalPoints() { return this == DecimalPoints || this == Both; }
    public boolean hasColon() { return this == Colon || this == Both; }
  }

  /**
   * The circuit board a driven module is built on, with the display mounted on it as a bezel and
   * mounting holes at the corners. A bare package has none of this.
   *
   * <p>The hole figures are the pixel offsets this class drew before they were figures at all,
   * converted at the canvas scale rather than measured. The exact conversions are kept rather
   * than two-decimal roundings because the roundings moved the holes by a fiftieth of a pixel,
   * which antialiasing turned into a visible difference.
   *
   * @author Branislav Stojkovic
   */
  public static class Module {

    private final double bezelWidthMm;
    private final double bezelLengthMm;
    private final double holeInsetMm;
    private final double holeSizeMm;

    public Module(double bezelWidthMm, double bezelLengthMm, double holeInsetMm,
        double holeSizeMm) {
      this.bezelWidthMm = bezelWidthMm;
      this.bezelLengthMm = bezelLengthMm;
      this.holeInsetMm = holeInsetMm;
      this.holeSizeMm = holeSizeMm;
    }

    public double getBezelWidthMm() { return bezelWidthMm; }
    public double getBezelLengthMm() { return bezelLengthMm; }

    /** Centre of a corner hole, from each of the two edges nearest it. */
    public double getHoleInsetMm() { return holeInsetMm; }

    public double getHoleSizeMm() { return holeSizeMm; }
  }
}
