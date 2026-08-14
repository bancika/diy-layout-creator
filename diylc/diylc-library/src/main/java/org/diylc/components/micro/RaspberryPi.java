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

@ComponentDescriptor(name = "Raspberry Pi", category = "Controllers",
    author = "Branislav Stojkovic", description = "Raspberry Pi 3/4/5 Single Board Computer with 40-pin GPIO",
    instanceNamePrefix = "SBC", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RaspberryPi extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color RPI_GREEN = Color.decode("#1B5E20");
  public static Size BOARD_WIDTH = new Size(85.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(56.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "Pin 1 (3.3V)", "Pin 2 (5V)",
      "Pin 3 (GPIO2/SDA)", "Pin 4 (5V)",
      "Pin 5 (GPIO3/SCL)", "Pin 6 (GND)",
      "Pin 7 (GPIO4)", "Pin 8 (GPIO14/TXD)",
      "Pin 9 (GND)", "Pin 10 (GPIO15/RXD)",
      "Pin 11 (GPIO17)", "Pin 12 (GPIO18)",
      "Pin 13 (GPIO27)", "Pin 14 (GND)",
      "Pin 15 (GPIO22)", "Pin 16 (GPIO23)",
      "Pin 17 (3.3V)", "Pin 18 (GPIO24)",
      "Pin 19 (GPIO10/MOSI)", "Pin 20 (GND)",
      "Pin 21 (GPIO9/MISO)", "Pin 22 (GPIO25)",
      "Pin 23 (GPIO11/SCLK)", "Pin 24 (GPIO8/CE0)",
      "Pin 25 (GND)", "Pin 26 (GPIO7/CE1)",
      "Pin 27 (ID_SD)", "Pin 28 (ID_SC)",
      "Pin 29 (GPIO5)", "Pin 30 (GND)",
      "Pin 31 (GPIO6)", "Pin 32 (GPIO12)",
      "Pin 33 (GPIO13)", "Pin 34 (GND)",
      "Pin 35 (GPIO19)", "Pin 36 (GPIO16)",
      "Pin 37 (GPIO26)", "Pin 38 (GPIO20)",
      "Pin 39 (GND)", "Pin 40 (GPIO21)"
  };

  public RaspberryPi() {
    super();
    this.bodyColor = RPI_GREEN;
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

    // 2x20 header: Pin 1 at (0,0), Pin 2 at (0, spacing), Pin 3 at (spacing, 0), Pin 4 at (spacing, spacing)...
    double[][] relativeOffsets = new double[40][2];
    for (int col = 0; col < 20; col++) {
      int pinOdd = col * 2;     // Pin 1, 3, 5... (bottom row of header)
      int pinEven = col * 2 + 1; // Pin 2, 4, 6... (top row of header)
      relativeOffsets[pinOdd][0] = col * spacing;
      relativeOffsets[pinOdd][1] = spacing;
      relativeOffsets[pinEven][0] = col * spacing;
      relativeOffsets[pinEven][1] = 0;
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
    double boardX = x - 60;
    double boardY = y - 30;
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
    double boardX = x - 60;
    double boardY = y - 30;

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
      // 4 Mounting holes
      drawMountingHole(g2d, boardX + 30, boardY + 30, 22);
      drawMountingHole(g2d, boardX + 30, boardY + boardH - 30, 22);
      drawMountingHole(g2d, boardX + boardW - 170, boardY + 30, 22);
      drawMountingHole(g2d, boardX + boardW - 170, boardY + boardH - 30, 22);

      // Ethernet & USB Ports on the right edge
      drawMetalConnector(g2d, boardX + boardW - 140, boardY + 30, 150, 110, "ETHERNET");
      drawMetalConnector(g2d, boardX + boardW - 140, boardY + 160, 150, 100, "USB 3.0");
      drawMetalConnector(g2d, boardX + boardW - 140, boardY + 280, 150, 100, "USB 2.0");

      // Broadcom SoC with metal heat spreader
      drawMetalConnector(g2d, boardX + 220, boardY + 180, 110, 110, "BCM SoC");

      // HDMI & USB-C Power
      drawMetalConnector(g2d, boardX + 30, boardY + boardH - 15, 60, 30, "PWR");
      drawMetalConnector(g2d, boardX + 110, boardY + boardH - 15, 55, 30, "HDMI0");
      drawMetalConnector(g2d, boardX + 180, boardY + boardH - 15, 55, 30, "HDMI1");

      // Raspberry Pi Silkscreen
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "Raspberry Pi", boardX + 120, boardY + 120, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "40-PIN GPIO", x + 190, y - 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw 40 GPIO header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RPI_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 4, 4));
    g2d.setColor(RPI_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 4, 4));

    // Ethernet & USB
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(width - 8, 6, 6, 6);
    g2d.fillRect(width - 8, 14, 6, 6);
    g2d.fillRect(width - 8, 22, 6, 6);

    // SoC
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(10, 14, 8, 8);

    // GPIO Header
    g2d.setColor(HEADER_BODY_COLOR);
    g2d.fillRect(4, 5, 18, 4);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 6));
    StringUtils.drawCenteredText(g2d, "RPi", 14, 25, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
