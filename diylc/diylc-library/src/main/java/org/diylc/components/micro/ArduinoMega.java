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

  public static final String[] PIN_NAMES = new String[] {
      // Power (0..7)
      "NC", "IOREF", "RESET", "3.3V", "5V", "GND1", "GND2", "VIN",
      // Analog Low A0..A7 (8..15)
      "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
      // Analog High A8..A15 (16..23)
      "A8", "A9", "A10", "A11", "A12", "A13", "A14", "A15",
      // Digital Low (24..31)
      "D0 (RX0)", "D1 (TX0)", "D2", "D3 (~)", "D4", "D5 (~)", "D6 (~)", "D7",
      // Digital High (32..41)
      "D8", "D9 (~)", "D10 (~)", "D11 (~)", "D12", "D13", "GND", "AREF", "SDA", "SCL",
      // Communication Header (42..49)
      "D14 (TX3)", "D15 (RX3)", "D16 (TX2)", "D17 (RX2)", "D18 (TX1)", "D19 (RX1)", "D20 (SDA)", "D21 (SCL)",
      // Double Digital 2x18 Header (50..85: D22..D53, 5V, 5V, GND, GND)
      "D22", "D23", "D24", "D25", "D26", "D27", "D28", "D29", "D30", "D31", "D32", "D33",
      "D34", "D35", "D36", "D37", "D38", "D39", "D40", "D41", "D42", "D43", "D44", "D45",
      "D46", "D47", "D48", "D49", "D50 (MISO)", "D51 (MOSI)", "D52 (SCK)", "D53 (SS)",
      "GND_EXT1", "GND_EXT2", "5V_EXT1", "5V_EXT2"
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
    double spacing = PIN_SPACING.convertToPixels(); // 20px for 0.1"

    // Reference: Pin 0 (NC) is at (boardX + 220, boardY + 400)
    // Board is 800px wide, 420px high
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Power header pins 0..7 (bottom row)
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }
    // Analog Low A0..A7 (8..15) (bottom row, separated by 0.16" = 32px)
    double a0X = 7 * spacing + 32;
    for (int i = 0; i < 8; i++) {
      relativeOffsets[8 + i][0] = a0X + i * spacing;
      relativeOffsets[8 + i][1] = 0;
    }
    // Analog High A8..A15 (16..23) (bottom row, continuing from A7)
    double a8X = a0X + 8 * spacing;
    for (int i = 0; i < 8; i++) {
      relativeOffsets[16 + i][0] = a8X + i * spacing;
      relativeOffsets[16 + i][1] = 0;
    }
    // Digital Low D0..D7 (24..31) (top row)
    double topRowY = -380;
    double d0X = 300;
    for (int i = 0; i < 8; i++) {
      relativeOffsets[24 + i][0] = d0X - i * spacing;
      relativeOffsets[24 + i][1] = topRowY;
    }
    // Digital High D8..D13/GND/AREF/SDA/SCL (32..41) (top row)
    double d8X = 128;
    for (int i = 0; i < 10; i++) {
      relativeOffsets[32 + i][0] = d8X - i * spacing;
      relativeOffsets[32 + i][1] = topRowY;
    }
    // Communication header D14..D21 (42..49) (top row)
    double commX = a8X;
    for (int i = 0; i < 8; i++) {
      relativeOffsets[42 + i][0] = commX + i * spacing;
      relativeOffsets[42 + i][1] = topRowY;
    }
    // Double digital header 2x18 at far right (50..85: D22..D53, GND, GND, 5V, 5V)
    double d22X = 510;
    for (int row = 0; row < 18; row++) {
      double py = -360 + row * spacing;
      relativeOffsets[50 + row * 2][0] = d22X;
      relativeOffsets[50 + row * 2][1] = py;
      relativeOffsets[50 + row * 2 + 1][0] = d22X + spacing;
      relativeOffsets[50 + row * 2 + 1][1] = py;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 220;
    double boardY = y - 400;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 16, 16);
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 220;
    double boardY = y - 400;

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
      // Mounting holes
      drawMountingHole(g2d, boardX + 110, boardY + 30, 24);
      drawMountingHole(g2d, boardX + 120, boardY + 390, 24);
      drawMountingHole(g2d, boardX + boardW - 30, boardY + 30, 24);
      drawMountingHole(g2d, boardX + boardW - 30, boardY + boardH - 30, 24);

      // USB Type-B Jack & DC Power Jack
      drawMetalConnector(g2d, boardX - 10, boardY + 40, 95, 80, "USB");
      drawChip(g2d, boardX - 10, boardY + boardH - 115, 105, 75, "DC IN");

      // ATmega2560 square QFP chip
      drawChip(g2d, boardX + 370, boardY + 165, 90, 90, "m2560");

      // Reset Button near USB
      g2d.setColor(Color.decode("#CC3333"));
      g2d.fill(new Ellipse2D.Double(boardX + 45, boardY + 135, 18, 18));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + 45, boardY + 160, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen text & branding
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "ARDUINO MEGA 2560", boardX + 380, boardY + 100, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Header silkscreen labels
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "POWER", boardX + 290, boardY + 375, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "ANALOG IN", boardX + 540, boardY + 375, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "DIGITAL", boardX + 350, boardY + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "COMMUNICATION", boardX + 620, boardY + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ARDUINO_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 4, 4));
    g2d.setColor(ARDUINO_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 4, 4));

    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(3, 8, 5, 5);

    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(16, 11, 8, 8);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 6));
    StringUtils.drawCenteredText(g2d, "MEGA", width / 2 + 3, height / 2 + 1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
