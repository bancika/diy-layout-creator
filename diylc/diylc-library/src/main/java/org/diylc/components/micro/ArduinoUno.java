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

@ComponentDescriptor(name = "Arduino Uno", category = "Controllers",
    author = "Branislav Stojkovic", description = "Arduino Uno R3 Microcontroller Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ArduinoUno extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color ARDUINO_BLUE = Color.decode("#00878F");
  public static Color ITALIAN_TEAL = Color.decode("#00979D");

  public static Size BOARD_WIDTH = new Size(68.6d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(53.4d, SizeUnit.mm);

  // Pin names in sequence
  public static final String[] PIN_NAMES = new String[] {
      // Power Header (0..7)
      "NC", "IOREF", "RESET", "3.3V", "5V", "GND1", "GND2", "VIN",
      // Analog Header (8..13)
      "A0", "A1", "A2", "A3", "A4", "A5",
      // Digital Low (14..21)
      "D0 (RX)", "D1 (TX)", "D2", "D3 (~)", "D4", "D5 (~)", "D6 (~)", "D7",
      // Digital High (22..31)
      "D8", "D9 (~)", "D10 (~)", "D11 (~)", "D12", "D13", "GND", "AREF", "SDA", "SCL",
      // ICSP (32..37)
      "MISO", "5V_ICSP", "SCK", "MOSI", "RST_ICSP", "GND_ICSP"
  };

  public ArduinoUno() {
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
    // Board is 540px wide, 420px high
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Power header pins 0..7 (bottom left row)
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }
    // Analog pins 8..13 (bottom right row, separated by 0.16" = 32px)
    double analogStartX = 7 * spacing + 32;
    for (int i = 0; i < 6; i++) {
      relativeOffsets[8 + i][0] = analogStartX + i * spacing;
      relativeOffsets[8 + i][1] = 0;
    }
    // Digital Low pins 14..21 (top right row: D0..D7)
    double topRowY = -380;
    double d0X = 300;
    for (int i = 0; i < 8; i++) {
      relativeOffsets[14 + i][0] = d0X - i * spacing;
      relativeOffsets[14 + i][1] = topRowY;
    }
    // Digital High pins 22..31 (top left row: D8..D13, GND, AREF, SDA, SCL)
    double d8X = 128;
    for (int i = 0; i < 10; i++) {
      relativeOffsets[22 + i][0] = d8X - i * spacing;
      relativeOffsets[22 + i][1] = topRowY;
    }
    // ICSP header (2x3 pins, 32..37) located on right side of board
    double icspX = 260;
    double icspY = -210;
    relativeOffsets[32] = new double[] {icspX, icspY};
    relativeOffsets[33] = new double[] {icspX + spacing, icspY};
    relativeOffsets[34] = new double[] {icspX, icspY + spacing};
    relativeOffsets[35] = new double[] {icspX + spacing, icspY + spacing};
    relativeOffsets[36] = new double[] {icspX, icspY + 2 * spacing};
    relativeOffsets[37] = new double[] {icspX + spacing, icspY + 2 * spacing};

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

    // Board bounding rectangle
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 220;
    double boardY = y - 400;

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
      // Mounting holes
      drawMountingHole(g2d, boardX + 110, boardY + 30, 24);
      drawMountingHole(g2d, boardX + 120, boardY + 390, 24);
      drawMountingHole(g2d, boardX + 520, boardY + 60, 24);
      drawMountingHole(g2d, boardX + 520, boardY + 280, 24);

      // USB Type-B Jack & DC Power Jack
      drawMetalConnector(g2d, boardX - 10, boardY + 40, 95, 80, "USB");
      drawChip(g2d, boardX - 10, boardY + boardH - 115, 105, 75, "DC IN");

      // ATmega328P DIP chip
      drawChip(g2d, boardX + 180, boardY + 190, 210, 55, "ATmega328P");

      // Reset Button near USB
      g2d.setColor(Color.decode("#CC3333"));
      g2d.fill(new Ellipse2D.Double(boardX + 45, boardY + 135, 18, 18));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + 45, boardY + 160, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen text & branding
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "ARDUINO", boardX + 290, boardY + 100, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "UNO", boardX + 290, boardY + 125, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Header silkscreen labels
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "POWER", boardX + 290, boardY + 375, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "ANALOG IN", boardX + 440, boardY + 375, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "DIGITAL", boardX + 350, boardY + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw header pins with continuity tracking
    drawPins(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ARDUINO_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 6, 6));
    g2d.setColor(ARDUINO_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 6, 6));

    // USB / DC jacks
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(3, 7, 7, 6);
    g2d.setColor(Color.DARK_GRAY);
    g2d.fillRect(3, 19, 8, 6);

    // IC
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(14, 12, 14, 8);

    // Header pin strips
    g2d.setColor(HEADER_BODY_COLOR);
    g2d.fillRect(12, 5, 16, 3);
    g2d.fillRect(12, 24, 16, 3);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
    StringUtils.drawCenteredText(g2d, "UNO", width / 2 + 5, height / 2 + 1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
