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
import java.awt.geom.Area;
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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Raspberry Pi Pico", category = "Controllers",
    author = "Branislav Stojkovic", description = "Raspberry Pi Pico / Pico W RP2040 Microcontroller Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RaspberryPiPico extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum PicoVersion {
    PICO("Pi Pico"),
    PICO_W("Pi Pico W");

    private final String label;

    PicoVersion(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public static Color RPI_GREEN = Color.decode("#1B5E20");
  public static Size BOARD_WIDTH = new Size(21.0d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(51.0d, SizeUnit.mm);

  public static Color PAD_COLOR = GOLD_COLOR;
  public static Size PAD_SIZE = new Size(1.7d, SizeUnit.mm);
  public static Size HOLE_SIZE = new Size(0.8d, SizeUnit.mm);
  public static Size NOTCH_SIZE = new Size(0.9d, SizeUnit.mm);

  public static Size DEBUG_PAD_OFFSET_X = new Size(7.38d, SizeUnit.mm);
  public static Size DEBUG_PAD_OFFSET_Y = new Size(19.8d, SizeUnit.mm);
  public static Size WIFI_WIDTH = new Size(12.0d, SizeUnit.mm);
  public static Size WIFI_LENGTH = new Size(10.0d, SizeUnit.mm);
  public static Size WIFI_OFFSET_Y = new Size(34.0d, SizeUnit.mm);

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

  protected PicoVersion version = PicoVersion.PICO;
  protected boolean headers = false;

  public RaspberryPiPico() {
    super();
    this.bodyColor = RPI_GREEN;
    updateControlPoints();
  }

  @EditableProperty(name = "Version")
  public PicoVersion getVersion() {
    if (version == null) {
      version = PicoVersion.PICO;
    }
    return version;
  }

  public void setVersion(PicoVersion version) {
    this.version = version;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Headers")
  public boolean getHeaders() {
    return headers;
  }

  public void setHeaders(boolean headers) {
    this.headers = headers;
    invalidateCache();
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

    if (getVersion() == PicoVersion.PICO_W) {
      double pin1OffsetX = new Size(1.61d, SizeUnit.mm).convertToPixels();
      double pin1OffsetY = new Size(1.37d, SizeUnit.mm).convertToPixels();
      double midX = BOARD_WIDTH.convertToPixels() - DEBUG_PAD_OFFSET_X.convertToPixels() - pin1OffsetX;
      double midY = BOARD_LENGTH.convertToPixels() - DEBUG_PAD_OFFSET_Y.convertToPixels() - pin1OffsetY;

      relativeOffsets[40][0] = midX - spacing;
      relativeOffsets[40][1] = midY;
      relativeOffsets[41][0] = midX;
      relativeOffsets[41][1] = midY;
      relativeOffsets[42][0] = midX + spacing;
      relativeOffsets[42][1] = midY;
    } else {
      // SWD Debug Header (40..42) at bottom center, aligned with bottom row of pins
      double swdX = rowSpacing / 2.0 - spacing;
      double swdY = 19 * spacing;
      for (int i = 0; i < 3; i++) {
        relativeOffsets[40 + i][0] = swdX + i * spacing;
        relativeOffsets[40 + i][1] = swdY;
      }
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

    Area boardArea = new Area(new Rectangle2D.Double(boardX, boardY, boardW, boardH));

    double spacing = PIN_SPACING.convertToPixels();
    int notchD = getClosestOdd((int) Math.round(NOTCH_SIZE.convertToPixels()));
    double notchR = notchD / 2.0;

    // Subtract left edge semi-circular notches
    for (int i = 0; i < 20; i++) {
      double py = boardY + pin1OffsetY + i * spacing;
      boardArea.subtract(new Area(new Ellipse2D.Double(boardX - notchR, py - notchR, notchD, notchD)));
    }

    // Subtract right edge semi-circular notches
    double rightEdge = boardX + boardW;
    for (int i = 0; i < 20; i++) {
      double py = boardY + pin1OffsetY + i * spacing;
      boardArea.subtract(new Area(new Ellipse2D.Double(rightEdge - notchR, py - notchR, notchD, notchD)));
    }

    return boardArea;
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

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // Micro USB Connector
      double usbW = USB_MICRO_WIDTH.convertToPixels();
      double usbH = USB_MICRO_LENGTH.convertToPixels();
      double usbOverhang = new Size(1.3d, SizeUnit.mm).convertToPixels();
      drawMicroUsb(g2d, boardX + (boardW - usbW) / 2.0, boardY - usbOverhang, usbW, usbH, "USB");

      // BOOTSEL button
      double btnW = BUTTON_WIDTH.convertToPixels();
      double btnH = BUTTON_LENGTH.convertToPixels();
      double btnX = boardX + (boardW - btnW) / 2.0;
      double btnY = boardY + new Size(11.5d, SizeUnit.mm).convertToPixels();
      drawButton(g2d, btnX, btnY, btnW, btnH);

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "BOOTSEL", boardX + boardW / 2.0, btnY - 7, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // RP2040 chip
      double chipSize = new Size(7.0d, SizeUnit.mm).convertToPixels();
      double chipX = boardX + (boardW - chipSize) / 2.0;
      double chipY = (getVersion() == PicoVersion.PICO_W)
          ? boardY + new Size(23.0d, SizeUnit.mm).convertToPixels() - new Size(0.1d, SizeUnit.in).convertToPixels()
          : boardY + new Size(23.0d, SizeUnit.mm).convertToPixels();
      drawChip(g2d, chipX, chipY, chipSize, chipSize, "RP2040");

      if (getVersion() == PicoVersion.PICO_W) {
        double spacing = PIN_SPACING.convertToPixels();
        double midDebugX = boardX + boardW - DEBUG_PAD_OFFSET_X.convertToPixels();
        double midDebugY = boardY + boardH - DEBUG_PAD_OFFSET_Y.convertToPixels();

        // DEBUG silkscreen text above SWD pins
        g2d.setColor(Color.WHITE);
        g2d.setFont(SILK_FONT_SMALL);
        StringUtils.drawCenteredText(g2d, "DEBUG", midDebugX, midDebugY - new Size(2.2d, SizeUnit.mm).convertToPixels(),
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        // Silkscreen outline box around 3 debug pins
        double boxMarginX = new Size(1.2d, SizeUnit.mm).convertToPixels();
        double boxMarginY = new Size(1.2d, SizeUnit.mm).convertToPixels();
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Rectangle2D.Double(
            midDebugX - spacing - boxMarginX,
            midDebugY - boxMarginY,
            2 * spacing + 2 * boxMarginX,
            2 * boxMarginY));

        // Gray WiFi chip (metal shield) at the bottom of the board
        double wifiW = WIFI_WIDTH.convertToPixels();
        double wifiH = WIFI_LENGTH.convertToPixels();
        double wifiX = boardX + (boardW - wifiW) / 2.0;
        double wifiY = boardY + WIFI_OFFSET_Y.convertToPixels();
        drawMetalConnector(g2d, wifiX, wifiY, wifiW, wifiH, "");
      } else {
        // Raspberry Pi Logo at bottom of the board (~9mm height)
        double logoSize = new Size(9.0d, SizeUnit.mm).convertToPixels();
        double logoWidth = logoSize * 72.515 / 92.604;
        double logoX = boardX + (boardW - logoWidth) / 2.0;
        double logoY = boardY + new Size(35.5d, SizeUnit.mm).convertToPixels();
        drawRaspberryPiLogo(g2d, logoX, logoY, logoSize);

        // DEBUG silkscreen text above SWD pins
        g2d.setColor(Color.WHITE);
        g2d.setFont(SILK_FONT_SMALL);
        StringUtils.drawCenteredText(g2d, "DEBUG", boardX + boardW / 2.0, boardY + new Size(47.8d, SizeUnit.mm).convertToPixels(),
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }

    drawingObserver.stopTracking();

    if (!headers) {
      drawCastellatedPads(g2d, boardX, boardY, boardW, boardH, pin1OffsetX, pin1OffsetY, outlineMode, drawingObserver);
    }

    g2d.setTransform(oldTx);

    if (headers) {
      drawPinHeader(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  /**
   * Helper to draw Raspberry Pi Pico's castellated solder pads along the left and right edges
   * and the SWD round pads.
   */
  protected void drawCastellatedPads(Graphics2D g2d, double boardX, double boardY, double boardW, double boardH,
      double pin1OffsetX, double pin1OffsetY, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;

    double spacing = PIN_SPACING.convertToPixels();
    int padD = getClosestOdd((int) Math.round(PAD_SIZE.convertToPixels()));
    int holeD = getClosestOdd((int) Math.round(HOLE_SIZE.convertToPixels()));
    int notchD = getClosestOdd((int) Math.round(NOTCH_SIZE.convertToPixels()));
    double padR = padD / 2.0;
    double holeR = holeD / 2.0;
    double notchR = notchD / 2.0;

    drawingObserver.startTrackingContinuityArea(true);

    // Left row (pins 0..19): castellated pads extending to left edge
    for (int i = 0; i < 20; i++) {
      double px = boardX + pin1OffsetX;
      double py = boardY + pin1OffsetY + i * spacing;

      Area padArea = new Area(new Rectangle2D.Double(boardX, py - padR, pin1OffsetX, padD));
      padArea.add(new Area(new Ellipse2D.Double(px - padR, py - padR, padD, padD)));
      padArea.subtract(new Area(new Ellipse2D.Double(boardX - notchR, py - notchR, notchD, notchD)));
      padArea.subtract(new Area(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD)));

      g2d.setColor(PAD_COLOR);
      g2d.fill(padArea);
      g2d.setColor(PAD_COLOR.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(padArea);

      // Inner through-hole drill hole (white circle matching Zero and perfboard)
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
      g2d.setColor(PAD_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
    }

    // Right row (pins 20..39): castellated pads extending to right edge
    double rightEdge = boardX + boardW;
    for (int i = 0; i < 20; i++) {
      double px = rightEdge - pin1OffsetX;
      double py = boardY + pin1OffsetY + i * spacing;

      Area padArea = new Area(new Rectangle2D.Double(px, py - padR, pin1OffsetX, padD));
      padArea.add(new Area(new Ellipse2D.Double(px - padR, py - padR, padD, padD)));
      padArea.subtract(new Area(new Ellipse2D.Double(rightEdge - notchR, py - notchR, notchD, notchD)));
      padArea.subtract(new Area(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD)));

      g2d.setColor(PAD_COLOR);
      g2d.fill(padArea);
      g2d.setColor(PAD_COLOR.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(padArea);

      // Inner through-hole drill hole (white circle matching Zero and perfboard)
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
      g2d.setColor(PAD_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
    }

    // SWD Debug Header (pins 40..42): standard round solder pads
    if (getVersion() == PicoVersion.PICO_W) {
      double midX = boardX + boardW - DEBUG_PAD_OFFSET_X.convertToPixels();
      double midY = boardY + boardH - DEBUG_PAD_OFFSET_Y.convertToPixels();
      for (int i = 0; i < 3; i++) {
        double px = midX + (i - 1) * spacing;
        double py = midY;

        Area swdPadArea = new Area(new Ellipse2D.Double(px - padR, py - padR, padD, padD));
        swdPadArea.subtract(new Area(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD)));

        g2d.setColor(PAD_COLOR);
        g2d.fill(swdPadArea);
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(swdPadArea);

        // Inner through-hole drill hole (white circle)
        g2d.setColor(Constants.CANVAS_COLOR);
        g2d.fill(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
        g2d.setColor(PAD_COLOR.darker());
        g2d.draw(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
      }
    } else {
      double swdX = boardX + (boardW - 2 * spacing) / 2.0;
      double swdY = boardY + pin1OffsetY + 19 * spacing;
      for (int i = 0; i < 3; i++) {
        double px = swdX + i * spacing;
        double py = swdY;

        Area swdPadArea = new Area(new Ellipse2D.Double(px - padR, py - padR, padD, padD));
        swdPadArea.subtract(new Area(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD)));

        g2d.setColor(PAD_COLOR);
        g2d.fill(swdPadArea);
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(swdPadArea);

        // Inner through-hole drill hole (white circle)
        g2d.setColor(Constants.CANVAS_COLOR);
        g2d.fill(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
        g2d.setColor(PAD_COLOR.darker());
        g2d.draw(new Ellipse2D.Double(px - holeR, py - holeR, holeD, holeD));
      }
    }

    drawingObserver.stopTrackingContinuityArea();
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RPI_GREEN);
    g2d.fill(new RoundRectangle2D.Double(6, 2, width - 12, height - 4, 3, 3));
    g2d.setColor(RPI_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(6, 2, width - 12, height - 4, 3, 3));

    // Castellated edge pads on left and right in icon
    g2d.setColor(PAD_COLOR);
    for (int y = 5; y <= height - 6; y += 3) {
      g2d.fillRect(6, y, 3, 2);
      g2d.fillRect(width - 9, y, 3, 2);
    }

    // USB Connector
    g2d.setColor(USB_METAL_COLOR);
    g2d.fillRect(11, 2, 10, 3);

    // RP2040 chip
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(11, 12, 10, 10);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "PICO", width / 2, height / 2 + 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
