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

@ComponentDescriptor(name = "Arduino Nano", category = "Controllers",
    author = "Branislav Stojkovic", description = "Arduino Nano Breadboard-Friendly Microcontroller Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ArduinoNano extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color ARDUINO_BLUE = Color.decode("#00878F");
  public static Size BOARD_WIDTH = new Size(18.0d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(45.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (0..14)
      "D13", "3V3", "REF", "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "5V", "RST", "GND", "VIN",
      // Right row (15..29)
      "TX1", "RX0", "RST2", "GND2", "D2", "D3 (~)", "D4", "D5 (~)", "D6 (~)", "D7", "D8", "D9 (~)", "D10 (~)", "D11 (~)", "D12",
      // ICSP (30..35)
      "MISO", "5V_ICSP", "SCK", "MOSI", "RST_ICSP", "GND_ICSP"
  };

  public ArduinoNano() {
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
    double spacing = PIN_SPACING.convertToPixels(); // 20px (0.1")
    double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels(); // 120px (0.6")

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Left row (pins 0..14)
    for (int i = 0; i < 15; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    // Right row (pins 15..29)
    for (int i = 0; i < 15; i++) {
      relativeOffsets[15 + i][0] = rowSpacing;
      relativeOffsets[15 + i][1] = i * spacing;
    }
    // ICSP header (2x3) between rows at the bottom
    double icspX = rowSpacing / 2.0 - spacing / 2.0;
    double icspY = 12 * spacing;
    relativeOffsets[30] = new double[] {icspX, icspY};
    relativeOffsets[31] = new double[] {icspX + spacing, icspY};
    relativeOffsets[32] = new double[] {icspX, icspY + spacing};
    relativeOffsets[33] = new double[] {icspX + spacing, icspY + spacing};
    relativeOffsets[34] = new double[] {icspX, icspY + 2 * spacing};
    relativeOffsets[35] = new double[] {icspX + spacing, icspY + 2 * spacing};

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels();
    double boardW = rowSpacing + 20;
    double boardH = 16 * PIN_SPACING.convertToPixels();
    double boardX = x - 10;
    double boardY = y - 10;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 8, 8);
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

    double rowSpacing = new Size(0.6d, SizeUnit.in).convertToPixels();
    double boardW = rowSpacing + 20;
    double boardH = 16 * PIN_SPACING.convertToPixels();
    double boardX = x - 10;
    double boardY = y - 10;

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
      // Mini USB Jack
      drawMetalConnector(g2d, boardX + (boardW - 50) / 2.0, boardY - 8, 50, 42, "USB");

      // ATmega328P TQFP square chip
      drawChip(g2d, boardX + (boardW - 48) / 2.0, boardY + 90, 48, 48, "m328P");

      // Reset Button
      g2d.setColor(Color.decode("#CC3333"));
      g2d.fill(new RoundRectangle2D.Double(boardX + (boardW - 22) / 2.0, boardY + 50, 22, 18, 4, 4));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + boardW / 2.0, boardY + 59, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "NANO", boardX + boardW / 2.0, boardY + 165, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ARDUINO_BLUE);
    g2d.fill(new RoundRectangle2D.Double(6, 2, width - 12, height - 4, 4, 4));
    g2d.setColor(ARDUINO_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(6, 2, width - 12, height - 4, 4, 4));

    // USB
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(11, 2, 10, 4);

    // IC
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(11, 12, 10, 10);

    // Pin strips on sides
    g2d.setColor(PIN_COLOR);
    for (int y = 5; y < height - 5; y += 4) {
      g2d.fillRect(7, y, 2, 2);
      g2d.fillRect(width - 9, y, 2, 2);
    }
  }
}
