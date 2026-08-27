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
  public static Font RPI_TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);

  public static final String[] PIN_NAMES = new String[] {
      "3.3V (Pin 1)", "5V (Pin 2)",
      "GPIO2/SDA (Pin 3)", "5V (Pin 4)",
      "GPIO3/SCL (Pin 5)", "GND (Pin 6)",
      "GPIO4 (Pin 7)", "GPIO14/TXD (Pin 8)",
      "GND (Pin 9)", "GPIO15/RXD (Pin 10)",
      "GPIO17 (Pin 11)", "GPIO18 (Pin 12)",
      "GPIO27 (Pin 13)", "GND (Pin 14)",
      "GPIO22 (Pin 15)", "GPIO23 (Pin 16)",
      "3.3V (Pin 17)", "GPIO24 (Pin 18)",
      "GPIO10/MOSI (Pin 19)", "GND (Pin 20)",
      "GPIO9/MISO (Pin 21)", "GPIO25 (Pin 22)",
      "GPIO11/SCLK (Pin 23)", "GPIO8/CE0 (Pin 24)",
      "GND (Pin 25)", "GPIO7/CE1 (Pin 26)",
      "ID_SD (Pin 27)", "ID_SC (Pin 28)",
      "GPIO5 (Pin 29)", "GND (Pin 30)",
      "GPIO6 (Pin 31)", "GPIO12 (Pin 32)",
      "GPIO13 (Pin 33)", "GND (Pin 34)",
      "GPIO19 (Pin 35)", "GPIO16 (Pin 36)",
      "GPIO26 (Pin 37)", "GPIO20 (Pin 38)",
      "GND (Pin 39)", "GPIO21 (Pin 40)",
      "PoE TR0 (Pin 1)", "PoE TR1 (Pin 2)",
      "PoE TR2 (Pin 3)", "PoE TR3 (Pin 4)",
      "PCIe", "MIPI 1", "MIPI 0"
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

    // 2x20 header: Pin 1 at (0,0), Pin 2 at (0, -spacing), Pin 3 at (spacing, 0), Pin 4 at (spacing, -spacing)...
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int col = 0; col < 20; col++) {
      int pinOdd = col * 2;      // Pin 1, 3, 5... (bottom/inner row of header)
      int pinEven = col * 2 + 1; // Pin 2, 4, 6... (top/outer row of header)
      relativeOffsets[pinOdd][0] = col * spacing;
      relativeOffsets[pinOdd][1] = 0;
      relativeOffsets[pinEven][0] = col * spacing;
      relativeOffsets[pinEven][1] = -spacing;
    }

    // 2x2 PoE header (pins 40..43) sitting 6mm above the bottom-right mounting hole
    double pin1OffsetX = new Size(8.37d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() + spacing / 2.0;
    double poeCenterX = new Size(61.5d, SizeUnit.mm).convertToPixels() - pin1OffsetX;
    double poeCenterY = new Size(46.5d, SizeUnit.mm).convertToPixels() - pin1OffsetY;

    relativeOffsets[40] = new double[] {poeCenterX - spacing / 2.0, poeCenterY - spacing / 2.0};
    relativeOffsets[41] = new double[] {poeCenterX - spacing / 2.0, poeCenterY + spacing / 2.0};
    relativeOffsets[42] = new double[] {poeCenterX + spacing / 2.0, poeCenterY - spacing / 2.0};
    relativeOffsets[43] = new double[] {poeCenterX + spacing / 2.0, poeCenterY + spacing / 2.0};

    // PCIe connector (pin 44) on the left edge (center at X = 0.1" + 2.0mm, Y = 1.0")
    double pcieW = new Size(4.0d, SizeUnit.mm).convertToPixels();
    double pcieCenterX = new Size(0.1d, SizeUnit.in).convertToPixels() + pcieW / 2.0 - pin1OffsetX;
    double pcieCenterY = new Size(1.0d, SizeUnit.in).convertToPixels() - pin1OffsetY;
    relativeOffsets[44] = new double[] {pcieCenterX, pcieCenterY};

    // MIPI 1 (pin 45) and MIPI 0 (pin 46) connectors on bottom edge
    double mipiH = new Size(14.0d, SizeUnit.mm).convertToPixels();
    double mipiCenterY = BOARD_HEIGHT.convertToPixels() - new Size(1.5d, SizeUnit.mm).convertToPixels() - mipiH / 2.0 - pin1OffsetY;
    double mipi1CenterX = new Size(48.0d, SizeUnit.mm).convertToPixels() - pin1OffsetX;
    double mipi0CenterX = new Size(54.0d, SizeUnit.mm).convertToPixels() - pin1OffsetX;
    relativeOffsets[45] = new double[] {mipi1CenterX, mipiCenterY};
    relativeOffsets[46] = new double[] {mipi0CenterX, mipiCenterY};

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double pin1OffsetX = new Size(8.37d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() + PIN_SPACING.convertToPixels() / 2.0;
    double boardX = x - pin1OffsetX;
    double boardY = y - pin1OffsetY;
    double cornerArc = new Size(6.0d, SizeUnit.mm).convertToPixels();
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, cornerArc, cornerArc);
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
    double pin1OffsetX = new Size(8.37d, SizeUnit.mm).convertToPixels();
    double pin1OffsetY = new Size(3.5d, SizeUnit.mm).convertToPixels() + PIN_SPACING.convertToPixels() / 2.0;
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
      // 4 Mounting holes (diameter 2.7mm, spaced 58mm apart horizontally and 49mm vertically)
      double holeDiameter = new Size(2.7d, SizeUnit.mm).convertToPixels();
      drawMountingHole(g2d, boardX + new Size(3.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(3.5d, SizeUnit.mm).convertToPixels(), holeDiameter);
      drawMountingHole(g2d, boardX + new Size(3.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(52.5d, SizeUnit.mm).convertToPixels(), holeDiameter);
      drawMountingHole(g2d, boardX + new Size(61.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(3.5d, SizeUnit.mm).convertToPixels(), holeDiameter);
      drawMountingHole(g2d, boardX + new Size(61.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(52.5d, SizeUnit.mm).convertToPixels(), holeDiameter);

      // USB & Ethernet Ports on the right edge
      // USB 3.0 (top): center Y = 9.0 mm from top edge (47.0 mm from bottom edge)
      drawUsbA(g2d, boardX + boardW - new Size(14.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(2.0d, SizeUnit.mm).convertToPixels(),
          USB_A_DUAL_LENGTH.convertToPixels(),
          new Size(14.0d, SizeUnit.mm).convertToPixels(), "USB 3.0");

      // USB 2.0 (middle): center Y = 26.9 mm from top edge (29.1 mm from bottom edge)
      drawUsbA(g2d, boardX + boardW - new Size(14.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(19.9d, SizeUnit.mm).convertToPixels(),
          USB_A_DUAL_LENGTH.convertToPixels(),
          new Size(14.0d, SizeUnit.mm).convertToPixels(), "USB 2.0");

      // Ethernet (bottom): center Y = 45.8 mm from top edge (10.2 mm from bottom edge)
      drawMetalConnector(g2d, boardX + boardW - new Size(18.0d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(37.8d, SizeUnit.mm).convertToPixels(),
          new Size(21.0d, SizeUnit.mm).convertToPixels(),
          new Size(16.0d, SizeUnit.mm).convertToPixels(), "ETHERNET");

      // Broadcom SoC with metal heat spreader and chipped top-left corner (17x17mm)
      double socX = boardX + new Size(32.5d, SizeUnit.mm).convertToPixels() - new Size(0.3d, SizeUnit.in).convertToPixels();
      double socY = boardY + new Size(24.5d, SizeUnit.mm).convertToPixels();
      double socW = new Size(17.0d, SizeUnit.mm).convertToPixels();
      double socH = new Size(17.0d, SizeUnit.mm).convertToPixels();
      double socCut = new Size(2.0d, SizeUnit.mm).convertToPixels();

      Path2D.Double socShape = new Path2D.Double();
      socShape.moveTo(socX + socCut, socY);
      socShape.lineTo(socX + socW, socY);
      socShape.lineTo(socX + socW, socY + socH);
      socShape.lineTo(socX, socY + socH);
      socShape.lineTo(socX, socY + socCut);
      socShape.closePath();

      g2d.setColor(USB_METAL_COLOR);
      g2d.fill(socShape);
      g2d.setColor(METAL_SHIELD_BORDER);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(socShape);

      g2d.setColor(METAL_LABEL_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "BCM SoC", socX + socW / 2.0, socY + socH / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Bottom edge: USB-C Power & Micro-HDMI connectors
      // PWR (USB-C): center X = 11.2 mm
      double usbCW = USB_C_WIDTH.convertToPixels();
      double usbCH = USB_C_LENGTH.convertToPixels();
      drawUsbC(g2d, boardX + new Size(11.2d, SizeUnit.mm).convertToPixels() - usbCW / 2.0,
          boardY + boardH - new Size(6.5d, SizeUnit.mm).convertToPixels(),
          usbCW, usbCH, "PWR");

      // HDMI0: center X = 25.8 mm
      drawMetalConnector(g2d, boardX + new Size(22.05d, SizeUnit.mm).convertToPixels(),
          boardY + boardH - new Size(6.5d, SizeUnit.mm).convertToPixels(),
          new Size(7.5d, SizeUnit.mm).convertToPixels(),
          new Size(7.5d, SizeUnit.mm).convertToPixels(), "HDMI0");

      // HDMI1: center X = 39.2 mm
      drawMetalConnector(g2d, boardX + new Size(35.45d, SizeUnit.mm).convertToPixels(),
          boardY + boardH - new Size(6.5d, SizeUnit.mm).convertToPixels(),
          new Size(7.5d, SizeUnit.mm).convertToPixels(),
          new Size(7.5d, SizeUnit.mm).convertToPixels(), "HDMI1");

      // Two MIPI CSI/DSI connectors on bottom edge (MIPI 1 at 48.0mm, MIPI 0 at 54.0mm)
      double mipiW = new Size(3.2d, SizeUnit.mm).convertToPixels();
      double mipiH = new Size(14.0d, SizeUnit.mm).convertToPixels();
      double mipiY = boardY + boardH - mipiH - new Size(1.5d, SizeUnit.mm).convertToPixels();
      double mipi1X = boardX + new Size(48.0d, SizeUnit.mm).convertToPixels() - mipiW / 2.0;
      double mipi0X = boardX + new Size(54.0d, SizeUnit.mm).convertToPixels() - mipiW / 2.0;
      drawFpcConnector(g2d, mipi1X, mipiY, mipiW, mipiH, true, "");
      drawFpcConnector(g2d, mipi0X, mipiY, mipiW, mipiH, true, "");

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "MIPI 1", boardX + new Size(48.0d, SizeUnit.mm).convertToPixels(),
          mipiY - new Size(2.5d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "MIPI 0", boardX + new Size(54.0d, SizeUnit.mm).convertToPixels(),
          mipiY - new Size(2.5d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // PCI Express FPC connector on the left edge (0.1" from left edge, 1" from top edge)
      double pcieW = new Size(4.0d, SizeUnit.mm).convertToPixels();
      double pcieH = new Size(12.5d, SizeUnit.mm).convertToPixels();
      double pcieX = boardX + new Size(0.1d, SizeUnit.in).convertToPixels();
      double pcieY = boardY + new Size(1.0d, SizeUnit.in).convertToPixels() - pcieH / 2.0;
      drawFpcConnector(g2d, pcieX, pcieY, pcieW, pcieH, true, "");

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "PCIe", pcieX + pcieW / 2.0,
          pcieY + pcieH + new Size(2.5d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // 2x2 PoE header silkscreen label
      double poeX = boardX + new Size(61.5d, SizeUnit.mm).convertToPixels();
      double poeY = boardY + new Size(52.5d, SizeUnit.mm).convertToPixels() - new Size(6.0d, SizeUnit.mm).convertToPixels();
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "PoE", poeX,
          poeY - PIN_SPACING.convertToPixels() - new Size(1.5d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Raspberry Pi Silkscreen text below 40-pin header
      g2d.setColor(Color.WHITE);
      g2d.setFont(RPI_TITLE_FONT);
      StringUtils.drawCenteredText(g2d, "Raspberry Pi", boardX + new Size(32.5d, SizeUnit.mm).convertToPixels(),
          boardY + new Size(9.0d, SizeUnit.mm).convertToPixels(), HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Raspberry Pi Logo to the left of the SoC (shrunk 20% to 10.4mm, moved 0.2" up)
      double logoSize = new Size(10.4d, SizeUnit.mm).convertToPixels();
      double logoX = boardX + new Size(8.3d, SizeUnit.mm).convertToPixels();
      double logoY = boardY + new Size(26.5d, SizeUnit.mm).convertToPixels() - new Size(0.2d, SizeUnit.in).convertToPixels();
      drawRaspberryPiLogo(g2d, logoX, logoY, logoSize);
    }

    g2d.setTransform(oldTx);

    // Draw 40 GPIO header pins and 4 PoE header pins
    drawPinHeader(g2d, 0, 44, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RPI_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 4, 4));
    g2d.setColor(RPI_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 4, 4));

    // USB & Ethernet (Top USB 3.0, Middle USB 2.0, Bottom Ethernet)
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(width - 8, 6, 6, 5);
    g2d.fillRect(width - 8, 13, 6, 5);
    g2d.fillRect(width - 9, 20, 7, 6);

    // SoC
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(13, 14, 7, 7);

    // GPIO Header
    g2d.setColor(HEADER_BODY_COLOR);
    g2d.fillRect(4, 5, 18, 3);

    // PCIe & MIPI connectors & PoE
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(3, 13, 1, 4);
    g2d.fillRect(17, height - 6, 1, 2);
    g2d.fillRect(19, height - 6, 1, 2);
    g2d.fillRect(width - 11, height - 9, 2, 2);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 6));
    StringUtils.drawCenteredText(g2d, "RPi", 14, 25, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
