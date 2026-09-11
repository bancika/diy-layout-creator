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
package org.diylc.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * Painters for the discrete parts populated onto a maker board -- chips, connectors, antennas,
 * buttons and mounting holes. They are pure functions of the geometry they are handed, drawn in
 * whatever coordinate space {@code g2d} is currently in, so a board calls them while its own
 * rotation is still applied.
 *
 * @author Branislav Stojkovic
 */
public class MakerBoardPainter {

  private MakerBoardPainter() {}

  /**
   * Helper to draw a mounting hole.
   */
  public static void drawMountingHole(Graphics2D g2d, double cx, double cy, double diameter) {
    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fill(new Ellipse2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter));
    g2d.setColor(Color.DARK_GRAY);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new Ellipse2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter));
  }

  /**
   * Helper to draw an IC chip on the board.
   */
  public static void drawChip(Graphics2D g2d, double x, double y, double w, double h, String label) {
    g2d.setColor(AbstractMakerBoard.IC_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 4, 4));
    g2d.setColor(AbstractMakerBoard.IC_BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 4, 4));

    // pin 1 dot
    g2d.setColor(AbstractMakerBoard.PIN_MARKER_COLOR);
    g2d.fill(new Ellipse2D.Double(x + 3, y + 3, 3, 3));

    if (label != null && !label.isEmpty() && w > 20 && h > 10) {
      g2d.setColor(AbstractMakerBoard.IC_TEXT_COLOR);
      g2d.setFont(AbstractMakerBoard.SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, label, x + w / 2.0, y + h / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  /**
   * Helper to draw an FPC / ribbon cable connector (e.g. MIPI CSI/DSI, PCIe FPC).
   */
  public static void drawFpcConnector(Graphics2D g2d, double x, double y, double w, double h, boolean vertical, String label) {
    g2d.setColor(AbstractMakerBoard.IC_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 2, 2));
    g2d.setColor(AbstractMakerBoard.IC_BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 2, 2));

    // Inner slot / latch bar
    g2d.setColor(AbstractMakerBoard.HEADER_BODY_COLOR.darker());
    if (vertical) {
      double slotW = Math.max(1.5, w * 0.35);
      double slotH = Math.max(2.0, h - 4);
      g2d.fill(new Rectangle2D.Double(x + (w - slotW) / 2.0, y + 2, slotW, slotH));
    } else {
      double slotW = Math.max(2.0, w - 4);
      double slotH = Math.max(1.5, h * 0.35);
      g2d.fill(new Rectangle2D.Double(x + 2, y + (h - slotH) / 2.0, slotW, slotH));
    }

    if (label != null && !label.isEmpty()) {
      g2d.setColor(AbstractMakerBoard.SILK_COLOR);
      g2d.setFont(AbstractMakerBoard.SILK_FONT_SMALL);
      if (w >= 20 && h >= 10) {
        StringUtils.drawCenteredText(g2d, label, x + w / 2.0, y + h / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }
  }

  /**
   * Helper to draw a standard Micro-USB connector.
   */
  public static void drawMicroUsb(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard USB Type-C connector.
   */
  public static void drawUsbC(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard USB Type-A connector.
   */
  public static void drawUsbA(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard USB Type-B connector.
   */
  public static void drawUsbB(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a metal connector / shield (e.g. RF shield cans, SD card slots, HDMI).
   */
  public static void drawMetalConnector(Graphics2D g2d, double x, double y, double w, double h, String label) {
    g2d.setColor(AbstractMakerBoard.USB_METAL_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 3, 3));
    g2d.setColor(AbstractMakerBoard.METAL_SHIELD_BORDER);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 3, 3));
    if (label != null && !label.isEmpty()) {
      g2d.setColor(AbstractMakerBoard.METAL_LABEL_COLOR);
      g2d.setFont(AbstractMakerBoard.SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, label, x + w / 2.0, y + h / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  /**
   * Helper to draw a standard ESP-style PCB meander antenna with a dark (#1E1E1E) substrate rectangle underneath.
   *
   * @param g2d Graphics2D context
   * @param x top-left X coordinate of the antenna substrate rectangle
   * @param y top-left Y coordinate of the antenna substrate rectangle
   * @param width width of the antenna substrate rectangle (e.g. 15.0mm, matching main chip width)
   * @param height height of the antenna substrate rectangle (e.g. 7.0mm)
   */
  public static void drawPcbAntenna(Graphics2D g2d, double x, double y, double width, double height) {
    // Dark rectangle underneath (#1E1E1E)
    g2d.setColor(AbstractMakerBoard.ANTENNA_BG_COLOR);
    g2d.fill(new Rectangle2D.Double(x, y, width, height));

    // Serpentine antenna trace (gold/copper)
    g2d.setColor(AbstractMakerBoard.ANTENNA_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    Path2D.Double antPath = new Path2D.Double();

    if (width >= height) {
      double antPadX = x + new Size(1.5d, SizeUnit.mm).convertToPixels();
      double antPadW = width - new Size(3.0d, SizeUnit.mm).convertToPixels();
      double traceH = new Size(4.1d, SizeUnit.mm).convertToPixels();
      double marginY = height > traceH ? (height - traceH) / 2.0 : height * 0.15;
      double antTopY = y + marginY;
      double antMidY = y + height - marginY;
      antPath.moveTo(antPadX, antMidY);
      antPath.lineTo(antPadX, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.25, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.25, antMidY);
      antPath.lineTo(antPadX + antPadW * 0.50, antMidY);
      antPath.lineTo(antPadX + antPadW * 0.50, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.75, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.75, antMidY);
      antPath.lineTo(antPadX + antPadW, antMidY);
      antPath.lineTo(antPadX + antPadW, antTopY);
    } else {
      double antPadY = y + new Size(1.5d, SizeUnit.mm).convertToPixels();
      double antPadH = height - new Size(3.0d, SizeUnit.mm).convertToPixels();
      double traceW = new Size(4.1d, SizeUnit.mm).convertToPixels();
      double marginX = width > traceW ? (width - traceW) / 2.0 : width * 0.15;
      double antLeftX = x + marginX;
      double antRightX = x + width - marginX;
      antPath.moveTo(antRightX, antPadY);
      antPath.lineTo(antLeftX, antPadY);
      antPath.lineTo(antLeftX, antPadY + antPadH * 0.25);
      antPath.lineTo(antRightX, antPadY + antPadH * 0.25);
      antPath.lineTo(antRightX, antPadY + antPadH * 0.50);
      antPath.lineTo(antLeftX, antPadY + antPadH * 0.50);
      antPath.lineTo(antLeftX, antPadY + antPadH * 0.75);
      antPath.lineTo(antRightX, antPadY + antPadH * 0.75);
      antPath.lineTo(antRightX, antPadY + antPadH);
      antPath.lineTo(antLeftX, antPadY + antPadH);
    }
    g2d.draw(antPath);
  }

  /**
   * Helper to draw a standard ESP-style PCB meander antenna using default 15.0mm x 7.0mm dimensions.
   */
  public static void drawPcbAntenna(Graphics2D g2d, double x, double y) {
    drawPcbAntenna(g2d, x, y, AbstractMakerBoard.ANTENNA_WIDTH.convertToPixels(), AbstractMakerBoard.ANTENNA_LENGTH.convertToPixels());
  }

  /**
   * Helper to draw a standard SMD tactile push button (housing + circular actuator).
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the button body
   * @param y Top-left Y coordinate of the button body
   * @param w Width of the button body
   * @param h Height of the button body
   */
  public static void drawButton(Graphics2D g2d, double x, double y, double w, double h) {
    g2d.setColor(AbstractMakerBoard.BUTTON_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 2, 2));
    g2d.setColor(AbstractMakerBoard.BUTTON_BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 2, 2));

    double actuatorD = Math.min(w, h) * 0.55;
    g2d.setColor(AbstractMakerBoard.BUTTON_ACTUATOR_COLOR);
    g2d.fill(new Ellipse2D.Double(x + (w - actuatorD) / 2.0, y + (h - actuatorD) / 2.0, actuatorD, actuatorD));
  }
}
