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

  // Pin names in sequence (44 pins total)
  public static final String[] PIN_NAMES = new String[] {
      // Power Header (0..7)
      "NC", "IOREF", "RESET", "3.3V", "5V", "GND1", "GND2", "VIN",
      // Analog Header (8..13)
      "A0", "A1", "A2", "A3", "A4", "A5",
      // Digital Low (14..21)
      "D0 (RX)", "D1 (TX)", "D2", "D3 (~)", "D4", "D5 (~)", "D6 (~)", "D7",
      // Digital High (22..31)
      "D8", "D9 (~)", "D10 (~)", "D11 (~)", "D12", "D13", "GND3", "AREF", "SDA", "SCL",
      // Main ICSP Header (32..37, ATmega328P)
      "MISO", "5V_ICSP", "SCK", "MOSI", "RST_ICSP", "GND_ICSP",
      // Top-Left ICSP Header (38..43, ATmega16U2)
      "MISO_16U2", "5V_16U2", "SCK_16U2", "MOSI_16U2", "RST_16U2", "GND_16U2"
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

    // Reference: Pin 0 (NC) is at (boardX + 219.78, boardY + 400.00)
    // Board is 539.78px wide, 419.78px high
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Power header pins 0..7 (bottom left row)
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }
    // Analog pins 8..13 (bottom right row, separated from VIN by 0.2" = 40px)
    double analogStartX = 7 * spacing + 40; // 180px
    for (int i = 0; i < 6; i++) {
      relativeOffsets[8 + i][0] = analogStartX + i * spacing;
      relativeOffsets[8 + i][1] = 0;
    }
    // Digital Low pins 14..21 (top right row: D0..D7)
    double topRowY = -380;
    double d0X = 280; // aligns with A5
    for (int i = 0; i < 8; i++) {
      relativeOffsets[14 + i][0] = d0X - i * spacing;
      relativeOffsets[14 + i][1] = topRowY;
    }
    // Digital High pins 22..31 (top left row: D8..D13, GND, AREF, SDA, SCL)
    // Gap between D7 (140px) and D8 is 0.16" = 32px
    double d8X = 140 - 32; // 108px
    for (int i = 0; i < 10; i++) {
      relativeOffsets[22 + i][0] = d8X - i * spacing;
      relativeOffsets[22 + i][1] = topRowY;
    }
    // Main ICSP header (2x3 pins, 32..37) located on right side of board
    double icspX = (257.7 - 131.2) * (20.0 / 9.0); // 281.11 px
    double icspY = (91.0 - 190.0) * (20.0 / 9.0);  // -220.00 px
    relativeOffsets[32] = new double[] {icspX, icspY};
    relativeOffsets[33] = new double[] {icspX + spacing, icspY};
    relativeOffsets[34] = new double[] {icspX, icspY + spacing};
    relativeOffsets[35] = new double[] {icspX + spacing, icspY + spacing};
    relativeOffsets[36] = new double[] {icspX, icspY + 2 * spacing};
    relativeOffsets[37] = new double[] {icspX + spacing, icspY + 2 * spacing};

    // Top-Left ICSP header (2x3 pins, 38..43) for ATmega16U2
    double icsp2X = (106.0 - 131.2) * (20.0 / 9.0); // -56.00 px
    double icsp2Y = (30.7 - 190.0) * (20.0 / 9.0);  // -354.00 px
    relativeOffsets[38] = new double[] {icsp2X, icsp2Y};
    relativeOffsets[39] = new double[] {icsp2X, icsp2Y + spacing};
    relativeOffsets[40] = new double[] {icsp2X - spacing, icsp2Y};
    relativeOffsets[41] = new double[] {icsp2X - spacing, icsp2Y + spacing};
    relativeOffsets[42] = new double[] {icsp2X - 2 * spacing, icsp2Y};
    relativeOffsets[43] = new double[] {icsp2X - 2 * spacing, icsp2Y + spacing};

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardX = x - (131.2 - 32.3) * (20.0 / 9.0); // x - 219.78
    double boardY = y - (190.0 - 10.0) * (20.0 / 9.0); // y - 400.00

    Path2D.Double path = new Path2D.Double();
    path.moveTo(boardX + 507.78, boardY + 0.00);
    path.lineTo(boardX + 519.78, boardY + 12.00);
    path.lineTo(boardX + 519.78, boardY + 102.00);
    path.lineTo(boardX + 539.78, boardY + 122.00);
    path.lineTo(boardX + 539.78, boardY + 380.00);
    path.lineTo(boardX + 519.78, boardY + 400.00);
    path.lineTo(boardX + 519.78, boardY + 412.00);
    path.curveTo(boardX + 519.78, boardY + 416.44, boardX + 516.22, boardY + 419.78, boardX + 512.00, boardY + 419.78);
    path.lineTo(boardX + 7.78, boardY + 419.78);
    path.curveTo(boardX + 3.33, boardY + 419.78, boardX + 0.00, boardY + 416.22, boardX + 0.00, boardY + 412.00);
    path.lineTo(boardX + 0.00, boardY + 7.78);
    path.curveTo(boardX + 0.00, boardY + 3.33, boardX + 3.56, boardY + 0.00, boardX + 7.78, boardY + 0.00);
    path.lineTo(boardX + 507.78, boardY + 0.00);
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

    // Board bounding origin
    double boardX = x - (131.2 - 32.3) * (20.0 / 9.0); // x - 219.78
    double boardY = y - (190.0 - 10.0) * (20.0 / 9.0); // y - 400.00

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
      drawMountingHole(g2d, boardX + 120, boardY + 20, 24);
      drawMountingHole(g2d, boardX + 110, boardY + 400, 24);
      drawMountingHole(g2d, boardX + 520, boardY + 140, 24);
      drawMountingHole(g2d, boardX + 520, boardY + 360, 24);

      // USB Type-B Jack & DC Power Jack
      drawMetalConnector(g2d, boardX - 28, boardY + 75, 102, 90, "USB");
      drawChip(g2d, boardX - 14, boardY + 323, 104, 71, "DC IN");

      // ATmega328P DIP chip
      drawChip(g2d, boardX + 219, boardY + 269, 292, 44, "ATmega328P");

      // Reset Button near USB
      g2d.setColor(Color.decode("#CC3333"));
      g2d.fill(new Ellipse2D.Double(boardX + 38, boardY + 20, 18, 18));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + 47, boardY + 48, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Arduino Infinity Logo
      drawArduinoLogo(g2d, boardX + 252.00, boardY + 88.22);

      // Silkscreen text & branding
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "ARDUINO", boardX + 300, boardY + 155, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "UNO", boardX + 405, boardY + 111, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Header silkscreen labels
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "POWER", boardX + 290, boardY + 370, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "ANALOG IN", boardX + 450, boardY + 370, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "DIGITAL (PWM ~)", boardX + 410, boardY + 55, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
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
