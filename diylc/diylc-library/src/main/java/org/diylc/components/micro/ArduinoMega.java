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
package org.diylc.components.micro;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.MakerBoardLogos;
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

@ComponentDescriptor(name = "Arduino Mega 2560", category = "Controllers",
    author = "Branislav Stojkovic", description = "Arduino Mega 2560 R3 High-I/O Microcontroller Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ArduinoMega extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color SILK_COLOR = Color.WHITE;

  public static Size BOARD_WIDTH = new Size(101.6d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(53.4d, SizeUnit.mm);

  // Pin names in sequence (98 pins total)
  public static final String[] PIN_NAMES = new String[] {
      // Power Header (0..7)
      "NC", "IOREF", "RESET", "3.3V", "5V", "GND1", "GND2", "VIN",
      // Analog Low A0..A7 (8..15)
      "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
      // Analog High A8..A15 (16..23)
      "A8", "A9", "A10", "A11", "A12", "A13", "A14", "A15",
      // Digital Low D0..D7 (24..31)
      "D0 (RX0)", "D1 (TX0)", "D2 (~)", "D3 (~)", "D4 (~)", "D5 (~)", "D6 (~)", "D7 (~)",
      // Digital High D8..D13, GND, AREF, SDA, SCL (32..41)
      "D8 (~)", "D9 (~)", "D10 (~)", "D11 (~)", "D12 (~)", "D13 (~)", "GND3", "AREF", "SDA", "SCL",
      // Communication Header D14..D21 (42..49)
      "D14 (TX3)", "D15 (RX3)", "D16 (TX2)", "D17 (RX2)", "D18 (TX1)", "D19 (RX1)", "D20 (SDA)", "D21 (SCL)",
      // Double Digital 2x18 Header (50..85: D22..D53, GNDx2, 5Vx2)
      "D22", "D23", "D24", "D25", "D26", "D27", "D28", "D29", "D30", "D31", "D32", "D33",
      "D34", "D35", "D36", "D37", "D38", "D39", "D40", "D41", "D42", "D43", "D44", "D45",
      "D46", "D47", "D48", "D49", "D50 (MISO)", "D51 (MOSI)", "D52 (SCK)", "D53 (SS)",
      "GND_EXT1", "GND_EXT2", "5V_EXT1", "5V_EXT2",
      // Main ICSP Header (86..91, ATmega2560)
      "MISO", "5V_ICSP", "SCK", "MOSI", "RST_ICSP", "GND_ICSP",
      // Top-Left ICSP Header (92..97, ATmega16U2)
      "MISO_16U2", "5V_16U2", "SCK_16U2", "MOSI_16U2", "RST_16U2", "GND_16U2"
  };

  // What the board prints next to the headers, which is not the node name: every ground says
  // "GND", the supply says "3V3", the digital pins carry bare numbers with a "~" on the PWM-capable
  // ones, and the reserved first power pin is left blank. The ICSP pins are printed nowhere on the
  // board, so their entries only keep this array parallel to PIN_NAMES.
  public static final String[] SILK_NAMES = new String[] {
      // Power Header (0..7)
      "", "IOREF", "RESET", "3V3", "5V", "GND", "GND", "VIN",
      // Analog Low A0..A7 (8..15)
      "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
      // Analog High A8..A15 (16..23)
      "A8", "A9", "A10", "A11", "A12", "A13", "A14", "A15",
      // Digital Low D0..D7 (24..31)
      "RX0", "TX0", "~2", "~3", "~4", "~5", "~6", "~7",
      // Digital High D8..D13, GND, AREF, SDA, SCL (32..41)
      "~8", "~9", "~10", "~11", "~12", "~13", "GND", "AREF", "SDA", "SCL",
      // Communication Header D14..D21 (42..49)
      "14", "15", "16", "17", "18", "19", "20", "21",
      // Double Digital 2x18 Header (50..85)
      "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33",
      "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45",
      "46", "47", "48", "49", "50", "51", "52", "53",
      // the block's power and ground rows are printed once for the pair, on the outer column,
      // which is the only side with room for them next to the analog header and the mounting hole
      "", "GND", "", "5V",
      // Main ICSP Header (86..91)
      "MISO", "5V", "SCK", "MOSI", "RST", "GND",
      // Top-Left ICSP Header (92..97)
      "MISO", "5V", "SCK", "MOSI", "RST", "GND"
  };

  public ArduinoMega() {
    super();
    this.bodyColor = ARDUINO_TEAL;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected String getSilkPinLabel(int index) {
    if (index >= 0 && index < SILK_NAMES.length) {
      return SILK_NAMES[index];
    }
    return super.getSilkPinLabel(index);
  }

  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels(); // 20px for 0.1" (100 mils)
    double gap02 = new Size(0.2d, SizeUnit.in).convertToPixels(); // 40px for 0.2"

    // Reference: Pin 0 (NC) is at (boardX + 220.0, boardY + 400.0) [1100 mils, 100 mils from bottom-left]
    // Board is 800px wide (4000 mils), 420px high (2100 mils)
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Power header pins 0..7 (bottom row, 1100..1800 mils)
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }
    // Analog Low A0..A7 pins 8..15 (bottom row, 2000..2700 mils, separated from VIN by 0.2" = 40px)
    double a0X = 7 * spacing + gap02; // 180px
    for (int i = 0; i < 8; i++) {
      relativeOffsets[8 + i][0] = a0X + i * spacing;
      relativeOffsets[8 + i][1] = 0;
    }
    // Analog High A8..A15 pins 16..23 (bottom row, 2900..3600 mils, separated from A7 by 0.2" = 40px)
    double a8X = a0X + 7 * spacing + gap02; // 360px
    for (int i = 0; i < 8; i++) {
      relativeOffsets[16 + i][0] = a8X + i * spacing;
      relativeOffsets[16 + i][1] = 0;
    }
    // Digital Low D0..D7 pins 24..31 (top row, 2500..1800 mils, Y=2000 mils)
    double topRowY = -new Size(1.9d, SizeUnit.in).convertToPixels(); // -380px
    double d0X = new Size(1.4d, SizeUnit.in).convertToPixels(); // aligns with A5 at 2500 mils (280px)
    for (int i = 0; i < 8; i++) {
      relativeOffsets[24 + i][0] = d0X - i * spacing;
      relativeOffsets[24 + i][1] = topRowY;
    }
    // Digital High D8..D13/GND/AREF/SDA/SCL pins 32..41 (top row, 1640..740 mils, gap between D7 and D8 is 0.16" = 32px)
    double d7X = 7 * spacing; // 140px
    double d7d8Gap = new Size(0.16d, SizeUnit.in).convertToPixels(); // 32px
    double d8X = d7X - d7d8Gap; // 108px
    for (int i = 0; i < 10; i++) {
      relativeOffsets[32 + i][0] = d8X - i * spacing;
      relativeOffsets[32 + i][1] = topRowY;
    }
    // Communication header D14..D21 pins 42..49 (top row, 2700..3400 mils, gap from D0 is 0.2" = 40px)
    double d14X = d0X + gap02; // 320px, aligns with A7 at 2700 mils
    for (int i = 0; i < 8; i++) {
      relativeOffsets[42 + i][0] = d14X + i * spacing;
      relativeOffsets[42 + i][1] = topRowY;
    }
    // Double digital header 2x18 at far right pins 50..85 (D22..D53, GNDx2, 5Vx2; X=3600, 3700 mils,
    // Y=2000..300 mils). The block is powered from its top row and grounded at the bottom one, so
    // the two 5V pins sit alongside the digital header and D22..D53 fill the sixteen rows below.
    double d22InnerX = new Size(2.6d, SizeUnit.in).convertToPixels(); // 520px: (3700 - 1100) * 0.2
    double d22OuterX = new Size(2.7d, SizeUnit.in).convertToPixels(); // 540px: (3800 - 1100) * 0.2
    relativeOffsets[84] = new double[] {d22InnerX, topRowY};
    relativeOffsets[85] = new double[] {d22OuterX, topRowY};
    for (int row = 0; row < 16; row++) {
      double py = topRowY + (row + 1) * spacing;
      relativeOffsets[50 + row * 2][0] = d22InnerX;
      relativeOffsets[50 + row * 2][1] = py;
      relativeOffsets[50 + row * 2 + 1][0] = d22OuterX;
      relativeOffsets[50 + row * 2 + 1][1] = py;
    }
    relativeOffsets[82] = new double[] {d22InnerX, topRowY + 17 * spacing};
    relativeOffsets[83] = new double[] {d22OuterX, topRowY + 17 * spacing};
    // Main ICSP header (2x3 pins, 86..91) for ATmega2560 at (2505, 1200) mils
    double icspX = new Size(1.405d, SizeUnit.in).convertToPixels(); // 281.0 px: (2505 - 1100) * 0.2
    double icspY = -new Size(1.1d, SizeUnit.in).convertToPixels();  // -220.0 px: (100 - 1200) * 0.2
    relativeOffsets[86] = new double[] {icspX, icspY};
    relativeOffsets[87] = new double[] {icspX + spacing, icspY};
    relativeOffsets[88] = new double[] {icspX, icspY + spacing};
    relativeOffsets[89] = new double[] {icspX + spacing, icspY + spacing};
    relativeOffsets[90] = new double[] {icspX, icspY + 2 * spacing};
    relativeOffsets[91] = new double[] {icspX + spacing, icspY + 2 * spacing};

    // Top-Left ICSP header (2x3 pins, 92..97) for ATmega16U2 near USB jack at (820, 1870) mils
    double icsp2X = -new Size(0.28d, SizeUnit.in).convertToPixels(); // -56.0 px: (820 - 1100) * 0.2
    // far enough below the digital header for its rotated pin names to fit above the connector
    double icsp2Y = -new Size(1.77d, SizeUnit.in).convertToPixels()
        + new Size(3.5d, SizeUnit.mm).convertToPixels();
    relativeOffsets[92] = new double[] {icsp2X, icsp2Y};
    relativeOffsets[93] = new double[] {icsp2X, icsp2Y + spacing};
    relativeOffsets[94] = new double[] {icsp2X - spacing, icsp2Y};
    relativeOffsets[95] = new double[] {icsp2X - spacing, icsp2Y + spacing};
    relativeOffsets[96] = new double[] {icsp2X - 2 * spacing, icsp2Y};
    relativeOffsets[97] = new double[] {icsp2X - 2 * spacing, icsp2Y + spacing};

    return relativeOffsets;
  }

  @Override
  protected void updateControlPoints() {
    rotatePoints(controlPoints[0], getRelativeOffsets());
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardX = x - new Size(1.1d, SizeUnit.in).convertToPixels();
    double boardY = y - new Size(2.0d, SizeUnit.in).convertToPixels();

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();

    double notchInset = new Size(0.1d, SizeUnit.in).convertToPixels();
    double topCutoff = new Size(0.16d, SizeUnit.in).convertToPixels();
    double y12 = new Size(0.06d, SizeUnit.in).convertToPixels();
    double y102 = new Size(0.51d, SizeUnit.in).convertToPixels();
    double y122 = new Size(0.61d, SizeUnit.in).convertToPixels();
    double y380 = new Size(1.9d, SizeUnit.in).convertToPixels();
    double y400 = new Size(2.0d, SizeUnit.in).convertToPixels();
    double cornerRadius = new Size(1.0d, SizeUnit.mm).convertToPixels();
    double c1 = cornerRadius * (1 - 0.5523);
    double c2 = cornerRadius * 0.5523;
    double y412 = boardH - cornerRadius;

    Path2D.Double path = new Path2D.Double();
    path.moveTo(boardX + boardW - topCutoff, boardY);
    path.lineTo(boardX + boardW - notchInset, boardY + y12);
    path.lineTo(boardX + boardW - notchInset, boardY + y102);
    path.lineTo(boardX + boardW, boardY + y122);
    path.lineTo(boardX + boardW, boardY + y380);
    path.lineTo(boardX + boardW - notchInset, boardY + y400);
    path.lineTo(boardX + boardW - notchInset, boardY + y412);
    path.curveTo(boardX + boardW - notchInset, boardY + boardH - c1, boardX + boardW - notchInset - c2, boardY + boardH, boardX + boardW - notchInset - cornerRadius, boardY + boardH);
    path.lineTo(boardX + cornerRadius, boardY + boardH);
    path.curveTo(boardX + c1, boardY + boardH, boardX, boardY + boardH - c1, boardX, boardY + boardH - cornerRadius);
    path.lineTo(boardX, boardY + cornerRadius);
    path.curveTo(boardX, boardY + c1, boardX + c1, boardY, boardX + cornerRadius, boardY);
    path.lineTo(boardX + boardW - topCutoff, boardY);
    path.closePath();
    return path;
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

    double boardX = x - new Size(1.1d, SizeUnit.in).convertToPixels();
    double boardY = y - new Size(2.0d, SizeUnit.in).convertToPixels();

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    // Draw PCB body
    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // Mounting holes (6 mounting holes from technical design)
      double holeDiameter = new Size(0.12d, SizeUnit.in).convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, boardX + new Size(0.55d, SizeUnit.in).convertToPixels(), boardY + new Size(2.0d, SizeUnit.in).convertToPixels(), holeDiameter); // (550, 100 mils)
      MakerBoardPainter.drawMountingHole(g2d, boardX + new Size(0.6d, SizeUnit.in).convertToPixels(), boardY + new Size(0.1d, SizeUnit.in).convertToPixels(), holeDiameter);  // (600, 2000 mils)
      MakerBoardPainter.drawMountingHole(g2d, boardX + new Size(2.6d, SizeUnit.in).convertToPixels(), boardY + new Size(0.7d, SizeUnit.in).convertToPixels(), holeDiameter); // (2600, 1400 mils)
      MakerBoardPainter.drawMountingHole(g2d, boardX + new Size(2.6d, SizeUnit.in).convertToPixels(), boardY + new Size(1.8d, SizeUnit.in).convertToPixels(), holeDiameter); // (2600, 300 mils)
      MakerBoardPainter.drawMountingHole(g2d, boardX + new Size(3.55d, SizeUnit.in).convertToPixels(), boardY + new Size(0.1d, SizeUnit.in).convertToPixels(), holeDiameter);  // (3550, 2000 mils)
      MakerBoardPainter.drawMountingHole(g2d, boardX + new Size(3.8d, SizeUnit.in).convertToPixels(), boardY + new Size(2.0d, SizeUnit.in).convertToPixels(), holeDiameter); // (3800, 100 mils)

      // USB Type-B Jack & DC Power Jack
      MakerBoardPainter.drawUsbB(g2d, boardX - USB_B_OVERHANG.convertToPixels(),
          boardY + new Size(0.375d, SizeUnit.in).convertToPixels(),
          USB_B_LENGTH.convertToPixels(),
          USB_B_WIDTH.convertToPixels(), "USB");
      MakerBoardPainter.drawChip(g2d, boardX - new Size(0.07d, SizeUnit.in).convertToPixels(),
          boardY + new Size(1.625d, SizeUnit.in).convertToPixels(),
          new Size(0.52d, SizeUnit.in).convertToPixels(),
          new Size(0.35d, SizeUnit.in).convertToPixels(), "DC IN");

      // ATmega2560 square QFP chip
      MakerBoardPainter.drawChip(g2d, boardX + new Size(1.825d, SizeUnit.in).convertToPixels(),
          boardY + new Size(0.825d, SizeUnit.in).convertToPixels(),
          new Size(0.4d, SizeUnit.in).convertToPixels(),
          new Size(0.4d, SizeUnit.in).convertToPixels(), "ATmega2560");

      // Reset Button, which the Mega carries beside the ICSP header rather than by the USB jack
      // the way the Uno does; it sits level with the middle row of the header
      double btnW = BUTTON_WIDTH.convertToPixels();
      double btnH = BUTTON_LENGTH.convertToPixels();
      double btnX = boardX + new Size(2.72d, SizeUnit.in).convertToPixels();
      double btnY = boardY + new Size(1.0d, SizeUnit.in).convertToPixels() - btnH / 2.0;
      MakerBoardPainter.drawButton(g2d, btnX, btnY, btnW, btnH);

      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", btnX + btnW / 2.0,
          btnY + btnH + new Size(1.0d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Arduino Infinity Logo
      MakerBoardLogos.drawArduinoLogo(g2d, boardX + new Size(1.2d, SizeUnit.in).convertToPixels(),
          boardY + new Size(0.4d, SizeUnit.in).convertToPixels());

      // Silkscreen text & branding
      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "ARDUINO", boardX + new Size(1.44d, SizeUnit.in).convertToPixels(),
          boardY + new Size(0.725d, SizeUnit.in).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "MEGA 2560", boardX + new Size(1.44d, SizeUnit.in).convertToPixels(),
          boardY + new Size(0.825d, SizeUnit.in).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Header silkscreen labels; the captions sit beyond the rotated pin names, which take up the
      // space right next to the headers themselves
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "POWER", boardX + new Size(1.45d, SizeUnit.in).convertToPixels(),
          boardY + new Size(1.75d, SizeUnit.in).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "ANALOG IN", boardX + new Size(3.15d, SizeUnit.in).convertToPixels(),
          boardY + new Size(1.75d, SizeUnit.in).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "DIGITAL (PWM ~)", boardX + new Size(1.85d, SizeUnit.in).convertToPixels(),
          boardY + new Size(0.34d, SizeUnit.in).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "COMMUNICATION", boardX + new Size(3.05d, SizeUnit.in).convertToPixels(),
          boardY + new Size(0.34d, SizeUnit.in).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Text below the 3x2 ICSP header rather than above it, where the Uno prints it: on the Mega
      // that space belongs to a mounting hole. Clears the bottom edge of the MCU package as well.
      double icspLabelX = boardX + new Size(2.505d, SizeUnit.in).convertToPixels() + new Size(0.05d, SizeUnit.in).convertToPixels();
      double icspLabelY = boardY + new Size(1.1d, SizeUnit.in).convertToPixels() + new Size(2.5d, SizeUnit.mm).convertToPixels();
      StringUtils.drawCenteredText(g2d, "ICSP", icspLabelX, icspLabelY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Header pin names, printed above the power / analog row and below the digital row the way
      // they are silkscreened on the board; the 2x18 block at the right edge is not a row
      double[][] relativeOffsets = getRelativeOffsets();
      drawRowPinLabels(g2d, x, y, relativeOffsets, 0, 24, false, SILK_COLOR);
      drawRowPinLabels(g2d, x, y, relativeOffsets, 24, 26, true, SILK_COLOR);

      // The 2x18 block prints its numbers upright rather than rotated like the header names, each
      // on the far side of the column it belongs to
      g2d.setColor(SILK_COLOR);
      g2d.setFont(PIN_FONT);
      double innerLabelOffset = PIN_LABEL_OFFSET.convertToPixels();
      // the outer column has only the strip between it and the notched board edge to print in
      double outerLabelOffset = new Size(1.27d, SizeUnit.mm).convertToPixels();
      for (int i = 50; i < PIN_NAMES.length - 12; i++) {
        String label = getSilkPinLabel(i);
        if (label.isEmpty()) {
          continue;
        }
        boolean outerColumn = (i - 50) % 2 == 1;
        StringUtils.drawCenteredText(g2d, label,
            outerColumn ? x + relativeOffsets[i][0] + outerLabelOffset
                : x + relativeOffsets[i][0] - innerLabelOffset,
            y + relativeOffsets[i][1],
            outerColumn ? HorizontalAlignment.LEFT : HorizontalAlignment.RIGHT,
            VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw header pins with continuity tracking
    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double boardX = 5;
    double boardY = 3;
    double boardW = width - boardX - 2;
    double boardH = height - 6;

    // USB Type-B Jack (metallic silver, top left protruding)
    double usbW = 7;
    double usbH = 8;
    double usbX = 1;
    double usbY = boardY + 2;

    // DC Power Jack (dark body, bottom left protruding)
    double dcW = 7;
    double dcH = 6;
    double dcX = 1;
    double dcY = boardY + boardH - dcH - 3;

    // Board PCB
    g2d.setColor(ARDUINO_TEAL);
    g2d.fill(new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4));
    g2d.setColor(ARDUINO_TEAL.darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4));

    // Draw USB Jack
    g2d.setColor(USB_METAL_COLOR);
    g2d.fill(new RoundRectangle2D.Double(usbX, usbY, usbW, usbH, 2, 2));
    g2d.setColor(METAL_SHIELD_BORDER);
    g2d.draw(new RoundRectangle2D.Double(usbX, usbY, usbW, usbH, 2, 2));

    // Draw DC Jack
    g2d.setColor(IC_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(dcX, dcY, dcW, dcH, 2, 2));
    g2d.setColor(Color.BLACK);
    g2d.draw(new RoundRectangle2D.Double(dcX, dcY, dcW, dcH, 2, 2));

    // Arduino Infinity logo
    double scale = 14.0 / 95.56;
    double logoW = 95.56 * scale;
    double logoH = 45.33 * scale;
    double logoX = boardX + (boardW - logoW) / 2.0 + 1.0;
    double logoY = boardY + 3.5;
    MakerBoardLogos.drawArduinoLogo(g2d, logoX, logoY, scale);

    // MEGA text below logo
    g2d.setColor(SILK_COLOR);
    int fontSize = Math.max(6, (int) Math.round(boardH * 0.26));
    g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
    double textY = logoY + logoH + (boardY + boardH - (logoY + logoH)) / 2.0;
    double textX = boardX + (boardW / 2.0) + 0.5;
    StringUtils.drawCenteredText(g2d, "MEGA", textX, textY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
