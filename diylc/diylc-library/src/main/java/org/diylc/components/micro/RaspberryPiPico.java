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

@ComponentDescriptor(name = "Raspberry Pi Pico", category = "Controllers",
    author = "Branislav Stojkovic", description = "Raspberry Pi Pico RP2040 Microcontroller Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RaspberryPiPico extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PICO_GREEN = Color.decode("#007A3D");
  public static Size BOARD_WIDTH = new Size(21.0d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(51.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (pins 0..19)
      "GP0", "GP1", "GND1", "GP2", "GP3", "GP4", "GP5", "GND2", "GP6", "GP7",
      "GP8", "GP9", "GND3", "GP10", "GP11", "GP12", "GP13", "GND4", "GP14", "GP15",
      // Right row (pins 20..39)
      "VBUS", "VSYS", "GND5", "3V3_EN", "3V3 (OUT)", "ADC_VREF", "GP28", "GND6", "GP27", "GP26",
      "RUN", "GP22", "GND7", "GP21", "GP20", "GP19", "GP18", "GND8", "GP17", "GP16",
      // SWD debug pins (pins 40..42)
      "SWCLK", "GND_SWD", "SWDIO"
  };

  public RaspberryPiPico() {
    super();
    this.bodyColor = PICO_GREEN;
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
    double spacing = PIN_SPACING.convertToPixels();
    double rowSpacing = new Size(0.7d, SizeUnit.in).convertToPixels(); // 140px

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Left row (0..19)
    for (int i = 0; i < 20; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    // Right row (20..39)
    for (int i = 0; i < 20; i++) {
      relativeOffsets[20 + i][0] = rowSpacing;
      relativeOffsets[20 + i][1] = i * spacing;
    }
    // SWD Debug Header (40..42) at bottom center, aligned with bottom row of pins
    double swdX = rowSpacing / 2.0 - spacing;
    double swdY = 19 * spacing;
    for (int i = 0; i < 3; i++) {
      relativeOffsets[40 + i][0] = swdX + i * spacing;
      relativeOffsets[40 + i][1] = swdY;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_LENGTH.convertToPixels();
    double pin1OffsetX = new Size(1.61d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(1.37d, SizeUnit.mm).convertToPixels();
    double boardX = x - pin1OffsetX;
    double boardY = y - pin1OffsetY;
    return new Rectangle2D.Double(boardX, boardY, boardW, boardH);
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
    double boardH = BOARD_LENGTH.convertToPixels();
    double pin1OffsetX = new Size(1.61d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(1.37d, SizeUnit.mm).convertToPixels();
    double boardX = x - pin1OffsetX;
    double boardY = y - pin1OffsetY;

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
      // 4 Mounting holes (2.1mm diameter)
      double holeDiameter = new Size(2.1d, SizeUnit.mm).convertToPixels();
      double topHoleY = boardY + new Size(2.0d, SizeUnit.mm).convertToPixels();
      double bottomHoleY = boardY + boardH - new Size(2.4d, SizeUnit.mm).convertToPixels();
      double leftHoleX = boardX + new Size(4.8d, SizeUnit.mm).convertToPixels();
      double rightHoleX = boardX + boardW - new Size(4.8d, SizeUnit.mm).convertToPixels();

      drawMountingHole(g2d, leftHoleX, topHoleY, holeDiameter);
      drawMountingHole(g2d, rightHoleX, topHoleY, holeDiameter);
      drawMountingHole(g2d, leftHoleX, bottomHoleY, holeDiameter);
      drawMountingHole(g2d, rightHoleX, bottomHoleY, holeDiameter);

      // Micro USB Connector
      double usbW = new Size(7.5d, SizeUnit.mm).convertToPixels();
      double usbH = new Size(5.6d, SizeUnit.mm).convertToPixels();
      double usbOverhang = new Size(1.3d, SizeUnit.mm).convertToPixels();
      drawMetalConnector(g2d, boardX + (boardW - usbW) / 2.0, boardY - usbOverhang, usbW, usbH, "USB");

      // BOOTSEL button
      double btnW = new Size(3.8d, SizeUnit.mm).convertToPixels();
      double btnH = new Size(3.0d, SizeUnit.mm).convertToPixels();
      double btnY = boardY + new Size(11.5d, SizeUnit.mm).convertToPixels();
      g2d.setColor(Color.WHITE);
      g2d.fill(new RoundRectangle2D.Double(boardX + (boardW - btnW) / 2.0, btnY, btnW, btnH, 2, 2));
      g2d.setColor(Color.DARK_GRAY);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "BOOT", boardX + boardW / 2.0, btnY + btnH / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // RP2040 chip
      double chipSize = new Size(7.0d, SizeUnit.mm).convertToPixels();
      double chipX = boardX + (boardW - chipSize) / 2.0;
      double chipY = boardY + new Size(23.0d, SizeUnit.mm).convertToPixels();
      drawChip(g2d, chipX, chipY, chipSize, chipSize, "RP2040");

      // Raspberry Pi Logo at bottom of the board (~9mm height)
      double logoSize = new Size(9.0d, SizeUnit.mm).convertToPixels();
      double logoWidth = logoSize * 72.515 / 92.604;
      double logoX = boardX + (boardW - logoWidth) / 2.0;
      double logoY = boardY + new Size(35.5d, SizeUnit.mm).convertToPixels();
      drawRaspberryPiLogo(g2d, logoX, logoY, logoSize);

      // DEBUG silkscreen text above SWD pins
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "DEBUG", boardX + boardW / 2.0, boardY + new Size(46.8d, SizeUnit.mm).convertToPixels(),
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PICO_GREEN);
    g2d.fill(new RoundRectangle2D.Double(6, 2, width - 12, height - 4, 3, 3));
    g2d.setColor(PICO_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(6, 2, width - 12, height - 4, 3, 3));

    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(11, 2, 10, 3);

    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(11, 12, 10, 10);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "PICO", width / 2, height / 2 + 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
