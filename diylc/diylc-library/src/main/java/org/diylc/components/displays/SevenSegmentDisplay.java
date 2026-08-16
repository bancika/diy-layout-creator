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

  public enum DisplayType {
    SingleDigit_10Pin("1-Digit 0.56\" (10-Pin DIP)"),
    FourDigit_12Pin("4-Digit Bare (12-Pin DIP)"),
    TM1637_Module_4Pin("4-Digit TM1637 Module (4-Pin)");

    private final String label;
    DisplayType(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public static Color BODY_BLACK = Color.decode("#1C1C1C");
  public static Color FACE_BLACK = Color.decode("#111111");
  public static Color FACE_BORDER = Color.decode("#333333");
  public static Color LED_RED = Color.decode("#E53935");
  public static Color LED_OFF = Color.decode("#2E2E2E");
  public static Color LED_OFF_BORDER = Color.decode("#222222");

  // Exact physical proportions from datasheet
  public static Size DIGIT_HEIGHT = new Size(14.2d, SizeUnit.mm); // 0.56"
  public static Size DIGIT_WIDTH = new Size(8.1d, SizeUnit.mm);
  public static Size DIGIT_PITCH = new Size(12.7d, SizeUnit.mm);
  public static Size SEGMENT_THICKNESS = new Size(1.4d, SizeUnit.mm);
  public static Size SEGMENT_GAP = new Size(0.35d, SizeUnit.mm);
  public static double SLANT_DEGREES = 8.0d;

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

  @EditableProperty(name = "LED Color")
  public Color getLedColor() {
    return ledColor;
  }

  public void setLedColor(Color ledColor) {
    this.ledColor = ledColor;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    switch (displayType) {
      case SingleDigit_10Pin:
        if (index >= 0 && index < PIN_NAMES_1DIGIT.length) return PIN_NAMES_1DIGIT[index];
        break;
      case FourDigit_12Pin:
        if (index >= 0 && index < PIN_NAMES_4DIGIT.length) return PIN_NAMES_4DIGIT[index];
        break;
      case TM1637_Module_4Pin:
        if (index >= 0 && index < PIN_NAMES_TM1637.length) return PIN_NAMES_TM1637[index];
        break;
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    if (displayType == DisplayType.SingleDigit_10Pin) {
      double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels(); // 120px
      double[][] relativeOffsets = new double[10][2];
      for (int i = 0; i < 5; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
      }
      for (int i = 0; i < 5; i++) {
        relativeOffsets[5 + i][0] = (4 - i) * spacing;
        relativeOffsets[5 + i][1] = -rowSpacing;
      }
      rotatePoints(firstPoint, relativeOffsets);
    } else if (displayType == DisplayType.FourDigit_12Pin) {
      double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels();
      double[][] relativeOffsets = new double[12][2];
      for (int i = 0; i < 6; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
      }
      for (int i = 0; i < 6; i++) {
        relativeOffsets[6 + i][0] = (5 - i) * spacing;
        relativeOffsets[6 + i][1] = -rowSpacing;
      }
      rotatePoints(firstPoint, relativeOffsets);
    } else {
      // TM1637 Module 4-pin
      double[][] relativeOffsets = new double[4][2];
      for (int i = 0; i < 4; i++) {
        relativeOffsets[i][0] = i * spacing;
        relativeOffsets[i][1] = 0;
      }
      rotatePoints(firstPoint, relativeOffsets);
    }
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW;
    double boardH;
    double boardX;
    double boardY;

    if (displayType == DisplayType.SingleDigit_10Pin) {
      boardW = new Size(12.6d, SizeUnit.mm).convertToPixels();
      boardH = new Size(19.0d, SizeUnit.mm).convertToPixels();
      double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels();
      boardX = x - (boardW - 4 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - rowSpacing - (boardH - rowSpacing) / 2.0;
    } else if (displayType == DisplayType.FourDigit_12Pin) {
      boardW = new Size(50.3d, SizeUnit.mm).convertToPixels();
      boardH = new Size(19.0d, SizeUnit.mm).convertToPixels();
      double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels();
      boardX = x - (boardW - 5 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - rowSpacing - (boardH - rowSpacing) / 2.0;
    } else {
      boardW = new Size(42.0d, SizeUnit.mm).convertToPixels();
      boardH = new Size(24.0d, SizeUnit.mm).convertToPixels();
      boardX = x - (boardW - 3 * PIN_SPACING.convertToPixels()) / 2.0;
      boardY = y - (boardH - PIN_SPACING.convertToPixels()) / 2.0;
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

        // Single 0.56" 7-segment digit with exact datasheet dimensions
        double dw = DIGIT_WIDTH.convertToPixels();
        double dh = DIGIT_HEIGHT.convertToPixels();
        double dx = boardX + (boardW - dw) / 2.0 - 0.5 * SEGMENT_THICKNESS.convertToPixels();
        double dy = boardY + (boardH - dh) / 2.0;
        drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8.", ledColor);

      } else if (displayType == DisplayType.FourDigit_12Pin) {
        // Dark display face
        g2d.setColor(FACE_BLACK);
        g2d.fill(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));
        g2d.setColor(FACE_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new RoundRectangle2D.Double(boardX + 3, boardY + 3, boardW - 6, boardH - 6, 3, 3));

        // 4 digits across with 12.70mm center-to-center pitch
        double dw = DIGIT_WIDTH.convertToPixels();
        double dh = DIGIT_HEIGHT.convertToPixels();
        double pitch = DIGIT_PITCH.convertToPixels();
        double firstCenterX = boardX + new Size(6.1d, SizeUnit.mm).convertToPixels();
        double dy = boardY + (boardH - dh) / 2.0;

        for (int d = 0; d < 4; d++) {
          double centerX = firstCenterX + d * pitch;
          double dx = centerX - dw / 2.0 - 0.5 * SEGMENT_THICKNESS.convertToPixels();
          drawSevenSegmentDigit(g2d, dx, dy, dw, dh, "8.", ledColor);
        }

        // Center colon dots between digits 1 & 2
        double colonX = boardX + boardW / 2.0;
        double colonY1 = dy + dh * 0.35;
        double colonY2 = dy + dh * 0.65;
        g2d.setColor(ledColor);
        g2d.fill(new Ellipse2D.Double(colonX - 2.0, colonY1 - 2.0, 4.0, 4.0));
        g2d.fill(new Ellipse2D.Double(colonX - 2.0, colonY2 - 2.0, 4.0, 4.0));

      } else {
        // TM1637 4-Digit Display Module
        drawMountingHole(g2d, boardX + 14, boardY + 14, 12);
        drawMountingHole(g2d, boardX + 14, boardY + boardH - 14, 12);
        drawMountingHole(g2d, boardX + boardW - 14, boardY + 14, 12);
        drawMountingHole(g2d, boardX + boardW - 14, boardY + boardH - 14, 12);

        // Display Bezel (Center)
        double bezelW = new Size(30.0d, SizeUnit.mm).convertToPixels();
        double bezelH = new Size(14.0d, SizeUnit.mm).convertToPixels();
        double bezelX = boardX + (boardW - bezelW) / 2.0 - 6.0;
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

        // Center colon dots
        double colonX = bezelX + bezelW / 2.0;
        double colonY1 = bezelY + bezelH / 2.0 - 8.0;
        double colonY2 = bezelY + bezelH / 2.0 + 6.0;
        g2d.setColor(ledColor);
        g2d.fill(new Ellipse2D.Double(colonX - 1.5, colonY1 - 1.5, 3.0, 3.0));
        g2d.fill(new Ellipse2D.Double(colonX - 1.5, colonY2 - 1.5, 3.0, 3.0));
      }
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

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
   * Draws an authentic 7-segment digit matching exact engineering drawing specifications:
   * - 8.0° italic forward slant
   * - Realistic polygon segment geometry (trapezoids/hexagons with clean junction bevels)
   * - 1.40mm segment thickness, 0.35mm inter-segment gaps without overlap
   * - Unlit segments rendered in dim silhouette and active segments lit in onColor
   * - 1.40mm Decimal point circle at bottom-right
   */
  private void drawSevenSegmentDigit(Graphics2D g2d, double x, double y, double w, double h, String charToDisplay, Color onColor) {
    AffineTransform orig = g2d.getTransform();

    // Exact 8.0° forward italic slant from engineering drawing
    g2d.translate(x + w / 2.0, y + h / 2.0);
    g2d.shear(-Math.tan(Math.toRadians(SLANT_DEGREES)), 0);
    g2d.translate(-w / 2.0, -h / 2.0);

    double t = Math.max(3.0, SEGMENT_THICKNESS.convertToPixels() * (h / DIGIT_HEIGHT.convertToPixels()));
    double g = Math.max(1.0, SEGMENT_GAP.convertToPixels() * (h / DIGIT_HEIGHT.convertToPixels()));
    double segW = w - t * 0.8;

    double x0 = 0;
    double x1 = segW;
    double y0 = 0;
    double ym = h / 2.0;
    double y1 = h;

    // Segment A (Top horizontal)
    Path2D pathA = new Path2D.Double();
    pathA.moveTo(x0 + t * 0.6 + g, y0);
    pathA.lineTo(x1 - t * 0.6 - g, y0);
    pathA.lineTo(x1 - g, y0 + t * 0.45);
    pathA.lineTo(x1 - t - g, y0 + t);
    pathA.lineTo(x0 + t + g, y0 + t);
    pathA.lineTo(x0 + g, y0 + t * 0.45);
    pathA.closePath();

    // Segment B (Top-Right vertical)
    Path2D pathB = new Path2D.Double();
    pathB.moveTo(x1, y0 + t * 0.5 + g);
    pathB.lineTo(x1 - t * 0.35, y0 + g);
    pathB.lineTo(x1 - t, y0 + t + 1.4 * g);
    pathB.lineTo(x1 - t, ym - t * 0.5 - 0.8 * g);
    pathB.lineTo(x1, ym - 0.8 * g);
    pathB.closePath();

    // Segment C (Bottom-Right vertical)
    Path2D pathC = new Path2D.Double();
    pathC.moveTo(x1, ym + 0.8 * g);
    pathC.lineTo(x1 - t, ym + t * 0.5 + 0.8 * g);
    pathC.lineTo(x1 - t, y1 - t - 1.4 * g);
    pathC.lineTo(x1 - t * 0.35, y1 - g);
    pathC.lineTo(x1, y1 - t * 0.5 - g);
    pathC.closePath();

    // Segment D (Bottom horizontal)
    Path2D pathD = new Path2D.Double();
    pathD.moveTo(x0 + t + g, y1 - t);
    pathD.lineTo(x1 - t - g, y1 - t);
    pathD.lineTo(x1 - g, y1 - t * 0.45);
    pathD.lineTo(x1 - t * 0.6 - g, y1);
    pathD.lineTo(x0 + t * 0.6 + g, y1);
    pathD.lineTo(x0 + g, y1 - t * 0.45);
    pathD.closePath();

    // Segment E (Bottom-Left vertical)
    Path2D pathE = new Path2D.Double();
    pathE.moveTo(x0, ym + 0.8 * g);
    pathE.lineTo(x0 + t, ym + t * 0.5 + 0.8 * g);
    pathE.lineTo(x0 + t, y1 - t - 1.4 * g);
    pathE.lineTo(x0 + t * 0.35, y1 - g);
    pathE.lineTo(x0, y1 - t * 0.5 - g);
    pathE.closePath();

    // Segment F (Top-Left vertical)
    Path2D pathF = new Path2D.Double();
    pathF.moveTo(x0, y0 + t * 0.5 + g);
    pathF.lineTo(x0 + t * 0.35, y0 + g);
    pathF.lineTo(x0 + t, y0 + t + 1.4 * g);
    pathF.lineTo(x0 + t, ym - t * 0.5 - 0.8 * g);
    pathF.lineTo(x0, ym - 0.8 * g);
    pathF.closePath();

    // Segment G (Middle horizontal hexagon)
    Path2D pathG = new Path2D.Double();
    pathG.moveTo(x0 + 1.4 * g, ym);
    pathG.lineTo(x0 + t * 0.6 + 1.4 * g, ym - t * 0.48);
    pathG.lineTo(x1 - t * 0.6 - 1.4 * g, ym - t * 0.48);
    pathG.lineTo(x1 - 1.4 * g, ym);
    pathG.lineTo(x1 - t * 0.6 - 1.4 * g, ym + t * 0.48);
    pathG.lineTo(x0 + t * 0.6 + 1.4 * g, ym + t * 0.48);
    pathG.closePath();

    // Decimal Point (DP)
    double dpD = t * 0.95;
    Shape dpShape = new Ellipse2D.Double(x1 + t * 0.25, y1 - dpD, dpD, dpD);

    Shape[] segments = new Shape[] { pathA, pathB, pathC, pathD, pathE, pathF, pathG };
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
