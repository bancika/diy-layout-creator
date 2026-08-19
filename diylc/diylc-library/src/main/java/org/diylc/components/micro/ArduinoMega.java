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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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

  public static Color ARDUINO_BLUE = Color.decode("#00878F");
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

  public ArduinoMega() {
    super();
    this.bodyColor = ARDUINO_BLUE;
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
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels(); // 20px for 0.1" (100 mils)

    // Reference: Pin 0 (NC) is at (boardX + 220.0, boardY + 400.0) [1100 mils, 100 mils from bottom-left]
    // Board is 800px wide (4000 mils), 420px high (2100 mils)
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Power header pins 0..7 (bottom row, 1100..1800 mils)
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }
    // Analog Low A0..A7 pins 8..15 (bottom row, 2000..2700 mils, separated from VIN by 0.2" = 40px)
    double a0X = 7 * spacing + 40; // 180px
    for (int i = 0; i < 8; i++) {
      relativeOffsets[8 + i][0] = a0X + i * spacing;
      relativeOffsets[8 + i][1] = 0;
    }
    // Analog High A8..A15 pins 16..23 (bottom row, 2900..3600 mils, separated from A7 by 0.2" = 40px)
    double a8X = a0X + 7 * spacing + 40; // 360px
    for (int i = 0; i < 8; i++) {
      relativeOffsets[16 + i][0] = a8X + i * spacing;
      relativeOffsets[16 + i][1] = 0;
    }
    // Digital Low D0..D7 pins 24..31 (top row, 2500..1800 mils, Y=2000 mils)
    double topRowY = -380;
    double d0X = 280; // aligns with A5 at 2500 mils
    for (int i = 0; i < 8; i++) {
      relativeOffsets[24 + i][0] = d0X - i * spacing;
      relativeOffsets[24 + i][1] = topRowY;
    }
    // Digital High D8..D13/GND/AREF/SDA/SCL pins 32..41 (top row, 1640..740 mils, gap between D7 and D8 is 0.16" = 32px)
    double d8X = 140 - 32; // 108px
    for (int i = 0; i < 10; i++) {
      relativeOffsets[32 + i][0] = d8X - i * spacing;
      relativeOffsets[32 + i][1] = topRowY;
    }
    // Communication header D14..D21 pins 42..49 (top row, 2700..3400 mils, gap from D0 is 0.2" = 40px)
    double d14X = d0X + 40; // 320px, aligns with A7 at 2700 mils
    for (int i = 0; i < 8; i++) {
      relativeOffsets[42 + i][0] = d14X + i * spacing;
      relativeOffsets[42 + i][1] = topRowY;
    }
    // Double digital header 2x18 at far right pins 50..85 (D22..D53, GNDx2, 5Vx2; X=3600, 3700 mils, Y=2000..300 mils)
    double d22InnerX = 520; // (3700 - 1100) * 0.2
    double d22OuterX = 540; // (3800 - 1100) * 0.2
    for (int row = 0; row < 18; row++) {
      double py = topRowY + row * spacing;
      relativeOffsets[50 + row * 2][0] = d22InnerX;
      relativeOffsets[50 + row * 2][1] = py;
      relativeOffsets[50 + row * 2 + 1][0] = d22OuterX;
      relativeOffsets[50 + row * 2 + 1][1] = py;
    }
    // Main ICSP header (2x3 pins, 86..91) for ATmega2560 at (2505, 1200) mils
    double icspX = (2505 - 1100) * 0.2; // 281.0 px
    double icspY = (100 - 1200) * 0.2;  // -220.0 px
    relativeOffsets[86] = new double[] {icspX, icspY};
    relativeOffsets[87] = new double[] {icspX + spacing, icspY};
    relativeOffsets[88] = new double[] {icspX, icspY + spacing};
    relativeOffsets[89] = new double[] {icspX + spacing, icspY + spacing};
    relativeOffsets[90] = new double[] {icspX, icspY + 2 * spacing};
    relativeOffsets[91] = new double[] {icspX + spacing, icspY + 2 * spacing};

    // Top-Left ICSP header (2x3 pins, 92..97) for ATmega16U2 near USB jack at (820, 1870) mils
    double icsp2X = (820 - 1100) * 0.2; // -56.0 px
    double icsp2Y = (100 - 1870) * 0.2; // -354.0 px
    relativeOffsets[92] = new double[] {icsp2X, icsp2Y};
    relativeOffsets[93] = new double[] {icsp2X, icsp2Y + spacing};
    relativeOffsets[94] = new double[] {icsp2X - spacing, icsp2Y};
    relativeOffsets[95] = new double[] {icsp2X - spacing, icsp2Y + spacing};
    relativeOffsets[96] = new double[] {icsp2X - 2 * spacing, icsp2Y};
    relativeOffsets[97] = new double[] {icsp2X - 2 * spacing, icsp2Y + spacing};

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardX = x - 220.0;
    double boardY = y - 400.0;

    Path2D.Double path = new Path2D.Double();
    path.moveTo(boardX + 767.78, boardY + 0.00);
    path.lineTo(boardX + 779.78, boardY + 12.00);
    path.lineTo(boardX + 779.78, boardY + 102.00);
    path.lineTo(boardX + 799.78, boardY + 122.00);
    path.lineTo(boardX + 799.78, boardY + 380.00);
    path.lineTo(boardX + 779.78, boardY + 400.00);
    path.lineTo(boardX + 779.78, boardY + 412.00);
    path.curveTo(boardX + 779.78, boardY + 416.44, boardX + 776.22, boardY + 419.78, boardX + 772.00, boardY + 419.78);
    path.lineTo(boardX + 7.78, boardY + 419.78);
    path.curveTo(boardX + 3.33, boardY + 419.78, boardX + 0.00, boardY + 416.22, boardX + 0.00, boardY + 412.00);
    path.lineTo(boardX + 0.00, boardY + 7.78);
    path.curveTo(boardX + 0.00, boardY + 3.33, boardX + 3.56, boardY + 0.00, boardX + 7.78, boardY + 0.00);
    path.lineTo(boardX + 767.78, boardY + 0.00);
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

    double boardX = x - 220.0;
    double boardY = y - 400.0;

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
      drawMountingHole(g2d, boardX + 110, boardY + 400, 24); // (550, 100 mils)
      drawMountingHole(g2d, boardX + 120, boardY + 20, 24);  // (600, 2000 mils)
      drawMountingHole(g2d, boardX + 520, boardY + 140, 24); // (2600, 1400 mils)
      drawMountingHole(g2d, boardX + 520, boardY + 360, 24); // (2600, 300 mils)
      drawMountingHole(g2d, boardX + 710, boardY + 20, 24);  // (3550, 2000 mils)
      drawMountingHole(g2d, boardX + 760, boardY + 400, 24); // (3800, 100 mils)

      // USB Type-B Jack & DC Power Jack
      drawMetalConnector(g2d, boardX - 28, boardY + 75, 103, 90, "USB");
      drawChip(g2d, boardX - 14, boardY + 325, 104, 70, "DC IN");

      // ATmega2560 square QFP chip
      drawChip(g2d, boardX + 365, boardY + 165, 80, 80, "ATmega2560");

      // Reset Button near USB
      g2d.setColor(Color.decode("#CC3333"));
      g2d.fill(new Ellipse2D.Double(boardX + 38, boardY + 20, 18, 18));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + 47, boardY + 48, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Arduino Infinity Logo
      drawArduinoLogo(g2d, boardX + 240, boardY + 80);

      // Silkscreen text & branding
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "ARDUINO", boardX + 288, boardY + 145, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "MEGA 2560", boardX + 288, boardY + 165, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Header silkscreen labels
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "POWER", boardX + 290, boardY + 370, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "ANALOG IN", boardX + 630, boardY + 370, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "DIGITAL (PWM ~)", boardX + 370, boardY + 55, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "COMMUNICATION", boardX + 610, boardY + 55, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw header pins with continuity tracking
    drawPins(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);

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
    g2d.setColor(ARDUINO_BLUE);
    g2d.fill(new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4));
    g2d.setColor(ARDUINO_BLUE.darker());
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
    drawArduinoLogo(g2d, logoX, logoY, scale);

    // MEGA text below logo
    g2d.setColor(Color.WHITE);
    int fontSize = Math.max(6, (int) Math.round(boardH * 0.26));
    g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
    double textY = logoY + logoH + (boardY + boardH - (logoY + logoH)) / 2.0;
    double textX = boardX + (boardW / 2.0) + 0.5;
    StringUtils.drawCenteredText(g2d, "MEGA", textX, textY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
