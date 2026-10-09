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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
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

  public static void drawMountingHole(Graphics2D g2d, double cx, double cy, double diameter) {
    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fill(new Ellipse2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter));
    g2d.setColor(Color.DARK_GRAY);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new Ellipse2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter));
  }

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

  /** An FPC / ribbon cable connector, such as a MIPI CSI/DSI or PCIe socket. */
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

  public static void drawMicroUsb(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  public static void drawUsbC(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  public static void drawUsbA(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  public static void drawUsbB(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /** A metal shield or connector body: an RF can, an SD card slot, an HDMI socket. */
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
   * An ESP-style PCB meander antenna over a dark substrate rectangle.
   *
   * @param g2d Graphics2D context
   * @param x top-left X coordinate of the antenna substrate rectangle
   * @param y top-left Y coordinate of the antenna substrate rectangle
   * @param width width of the antenna substrate rectangle
   * @param height height of the antenna substrate rectangle
   */
  public static void drawPcbAntenna(Graphics2D g2d, double x, double y, double width, double height) {
    g2d.setColor(AbstractMakerBoard.ANTENNA_BG_COLOR);
    g2d.fill(new Rectangle2D.Double(x, y, width, height));

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

  /** As above, at the default 15.0 x 7.0 mm. */
  public static void drawPcbAntenna(Graphics2D g2d, double x, double y) {
    drawPcbAntenna(g2d, x, y, AbstractMakerBoard.ANTENNA_WIDTH.convertToPixels(), AbstractMakerBoard.ANTENNA_LENGTH.convertToPixels());
  }

  /**
   * An SMD tactile push button: housing plus circular actuator.
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

  /**
   * An addressable RGB LED in a 5050 package: the white body, the milky phosphor lens over it and
   * the driver die visible through the lens. Shared by every WS2812B part.
   *
   * @param g2d Graphics2D context
   * @param cx centre X coordinate of the package
   * @param cy centre Y coordinate of the package
   * @param size edge length of the square package; lens and die scale with it
   */
  public static void drawAddressableLed(Graphics2D g2d, double cx, double cy, double size) {
    drawAddressableLed(g2d, cx, cy, size, null);
  }

  /**
   * Draws one 5050 addressable RGB package. A {@code litColor} shows the LED emitting that colour;
   * {@code null} draws it dark, which is how an unpowered part looks sitting on a layout.
   */
  public static void drawAddressableLed(Graphics2D g2d, double cx, double cy, double size,
      Color litColor) {
    drawAddressableLed(g2d, cx, cy, size, litColor, 0);
  }

  /**
   * As above, turned by {@code rotation} radians about its own centre, for parts laid out on a
   * circle and mounted facing outwards. The package is square, so the turn only tells modulo a
   * quarter turn -- still enough to distinguish a radial arrangement from an axis-aligned one.
   */
  public static void drawAddressableLed(Graphics2D g2d, double cx, double cy, double size,
      Color litColor, double rotation) {
    drawAddressableLed(g2d, cx, cy, size, litColor, rotation, AbstractMakerBoard.LedType.RGB);
  }

  /**
   * As above, for a package other than the plain RGB one. An RGBW is told from an RGB on sight
   * only by the white die beside the colour dies, so that is what is drawn; WWA has the same
   * three-die layout and is told apart by what it emits, which is the caller's palette.
   */
  public static void drawAddressableLed(Graphics2D g2d, double cx, double cy, double size,
      Color litColor, double rotation, AbstractMakerBoard.LedType ledType) {
    if (rotation != 0) {
      g2d.rotate(rotation, cx, cy);
    }

    double half = size / 2.0;

    g2d.setColor(AbstractMakerBoard.RGB_LED_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(cx - half, cy - half, size, size, 2, 2));
    g2d.setColor(AbstractMakerBoard.RGB_LED_BODY_BORDER);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(0.5f));
    g2d.draw(new RoundRectangle2D.Double(cx - half, cy - half, size, size, 2, 2));

    double lensR = half * 0.7;
    g2d.setColor(litColor == null ? AbstractMakerBoard.RGB_LED_LENS_COLOR : litColor);
    g2d.fill(new Ellipse2D.Double(cx - lensR, cy - lensR, lensR * 2, lensR * 2));
    g2d.setColor(litColor == null ? AbstractMakerBoard.RGB_LED_LENS_BORDER : litColor.darker());
    g2d.draw(new Ellipse2D.Double(cx - lensR, cy - lensR, lensR * 2, lensR * 2));

    double dieSize = Math.max(2.0, lensR * 0.4);
    // the colour dies sit centred on their own, and shoulder aside to make room for a white one
    double dieOffset = ledType != null && ledType.hasWhiteDie() ? dieSize * 0.7 : 0;
    g2d.setColor(litColor == null ? AbstractMakerBoard.RGB_LED_CHIP_COLOR : litColor.brighter());
    g2d.fill(new Rectangle2D.Double(cx - dieOffset - dieSize / 2.0, cy - dieSize / 2.0, dieSize,
        dieSize));
    if (dieOffset > 0) {
      g2d.setColor(litColor == null ? AbstractMakerBoard.RGB_LED_CHIP_COLOR : Color.WHITE);
      g2d.fill(new Rectangle2D.Double(cx + dieOffset - dieSize / 2.0, cy - dieSize / 2.0, dieSize,
          dieSize));
    }

    // turned back rather than saving the transform, so nothing is allocated per LED
    if (rotation != 0) {
      g2d.rotate(-rotation, cx, cy);
    }
  }

  /**
   * Prints a display's own description on its lit area, wrapped and centred.
   *
   * <p>What it prints is what the component already answers for the BOM, so the glass cannot fall
   * out of step with the part list. Nothing is drawn unless the whole block fits, so a panel too
   * small for its description shows a bare screen rather than a crop -- the same rule the pin-name
   * silkscreen follows.
   *
   * <p>The area is the part of the panel the text may occupy rather than the panel itself, so a
   * round display hands over the square inscribed in its glass.
   */
  public static void drawScreenText(Graphics2D g2d, Rectangle2D area, Color inkColor,
      Display display, String name, String value) {
    String text = screenText(display, name, value);
    if (text == null) {
      return;
    }

    g2d.setFont(AbstractMakerBoard.SILK_FONT);
    FontMetrics metrics = g2d.getFontMetrics();
    double margin = AbstractMakerBoard.SCREEN_TEXT_MARGIN.convertToPixels();
    double maxWidth = area.getWidth() - 2 * margin;
    double maxHeight = area.getHeight() - 2 * margin;
    double lineHeight = metrics.getHeight();

    List<String> lines = StringUtils.wrap(text, metrics, (int) maxWidth);
    if (lines.isEmpty() || lines.size() * lineHeight > maxHeight) {
      return;
    }
    // a single word wider than the area cannot be broken, so the width is checked after wrapping
    // rather than trusted to it
    for (String line : lines) {
      if (metrics.stringWidth(line) > maxWidth) {
        return;
      }
    }

    g2d.setColor(inkColor);
    double y = area.getCenterY() - (lines.size() - 1) * lineHeight / 2.0;
    for (String line : lines) {
      StringUtils.drawCenteredText(g2d, line, area.getCenterX(), y, HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
      y += lineHeight;
    }
  }

  /**
   * What each {@link Display} setting puts on the glass, or {@code null} for a screen that prints
   * nothing. The variant string reads "size, interface", and breaking it at the comma gives one
   * property per line, which keeps the longest line well inside the glass instead of spanning it.
   */
  public static String screenText(Display display, String name, String value) {
    boolean hasValue = value != null && !value.trim().isEmpty();
    String variant = hasValue ? value.replace(", ", "\n") : null;

    if (display == null) {
      return variant;
    }
    switch (display) {
      case NONE:
        return null;
      case NAME:
        return name;
      case BOTH:
        return variant == null ? name : name + "\n" + variant;
      default:
        return variant;
    }
  }
}
