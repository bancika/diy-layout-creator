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
 * along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.modules;

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

@ComponentDescriptor(name = "Stepper Driver Board (ULN2003)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "ULN2003 Stepper Motor Driver Board with 4 Status LEDs",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ULN2003Driver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color ULN_GREEN = Color.decode("#1E824C");
  public static Color JST_BODY_COLOR = Color.decode("#F4F6F6");
  public static Color JST_BORDER_COLOR = Color.decode("#BDC3C7");
  public static Color LED_DOME_COLOR = Color.decode("#FCF3CF");
  public static Color LED_BORDER_COLOR = Color.decode("#B7950B");
  public static Color JUMPER_COLOR = Color.decode("#1C1C1C");

  public static Font LABEL_FONT_BOLD = new Font("SansSerif", Font.BOLD, 9);
  public static Font LABEL_FONT_TINY = new Font("SansSerif", Font.PLAIN, 8);

  // Real ULN2003 stepper driver board dimensions: ~32mm wide x 35mm tall
  public static Size BOARD_WIDTH = new Size(32.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(35.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {
      "IN1", "IN2", "IN3", "IN4",
      "GND (-)", "+5-12V (+)", "PWR Jumper 1", "PWR Jumper 2",
      "Motor A", "Motor B", "Motor C", "Motor D", "Motor COM (+)"
  };

  public ULN2003Driver() {
    super();
    this.bodyColor = ULN_GREEN;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();

    // 13 pins total:
    // 0..3: IN1, IN2, IN3, IN4 (Left edge control input header)
    // 4..7: GND, +5-12V, Jumper 1, Jumper 2 (Bottom power header)
    // 8..12: Motor A, B, C, D, COM (5-Pin JST-XH socket)
    double[][] relativeOffsets = new double[][] {
      // IN1..IN4 along left edge
      { 0, 0 },
      { 0, spacing },
      { 0, spacing * 2 },
      { 0, spacing * 3 },

      // Bottom power header (GND, 5-12V, Jumper 1, Jumper 2)
      { 15, boardH - 95.0 },
      { 15 + spacing, boardH - 95.0 },
      { 15 + spacing * 2, boardH - 95.0 },
      { 15 + spacing * 3, boardH - 95.0 },

      // 5-Pin JST-XH Stepper Motor Socket (Motor A, B, C, D, COM)
      { 123.0, -10.0 },
      { 123.0, -10.0 + spacing },
      { 123.0, -10.0 + spacing * 2 },
      { 123.0, -10.0 + spacing * 3 },
      { 123.0, -10.0 + spacing * 4 }
    };

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 32.0;
    double boardY = y - 65.0;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 6, 6);
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

    double spacing = PIN_SPACING.convertToPixels();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 32.0;
    double boardY = y - 65.0;

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
      // 4 Mounting Holes with silver solder rings in corners
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 16, 16);

      // Left edge unpopulated through-holes (IN5, IN6, IN7) below IN4
      for (int i = 4; i < 7; i++) {
        double px = x;
        double py = y + i * spacing;
        g2d.setColor(LIGHT_METAL_COLOR);
        g2d.fill(new Ellipse2D.Double(px - 5, py - 5, 10, 10));
        g2d.setColor(Constants.CANVAS_COLOR);
        g2d.fill(new Ellipse2D.Double(px - 2.5, py - 2.5, 5, 5));
      }

      // ULN2003A DIP-16 IC (Center) - Vertical orientation
      double icX = boardX + 68.0;
      double icY = boardY + 45.0;
      double icW = 44.0;
      double icH = 118.0;

      // DIP IC body
      g2d.setColor(IC_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(icX, icY, icW, icH, 4, 4));
      g2d.setColor(IC_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new RoundRectangle2D.Double(icX, icY, icW, icH, 4, 4));

      // Pin 1 notch at top
      g2d.setColor(IC_BORDER_COLOR);
      g2d.drawArc((int) (icX + icW / 2.0 - 6), (int) (icY - 4), 12, 8, 180, 180);

      // Pin 1 dot
      g2d.setColor(PIN_MARKER_COLOR);
      g2d.fill(new Ellipse2D.Double(icX + 5, icY + 5, 4, 4));

      // Vertical text on IC
      AffineTransform preIC = g2d.getTransform();
      g2d.rotate(Math.PI / 2, icX + icW / 2.0, icY + icH / 2.0);
      g2d.setColor(IC_TEXT_COLOR);
      g2d.setFont(LABEL_FONT_BOLD);
      StringUtils.drawCenteredText(g2d, "ULN2003A", icX + icW / 2.0, icY + icH / 2.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(preIC);

      // 5-Pin JST-XH Stepper Motor Shrouded Header (Center-Right)
      double jstX = boardX + 140.0;
      double jstY = boardY + 45.0;
      double jstW = 32.0;
      double jstH = 100.0;
      g2d.setColor(JST_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(jstX, jstY, jstW, jstH, 3, 3));
      g2d.setColor(JST_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new RoundRectangle2D.Double(jstX, jstY, jstW, jstH, 3, 3));

      // Keying notch on JST body
      g2d.setColor(JST_BORDER_COLOR);
      g2d.drawLine((int) (jstX + 4), (int) (jstY + 10), (int) (jstX + 4), (int) (jstY + jstH - 10));

      // 4 Status LEDs (A, B, C, D) along the right side
      double ledX = boardX + 215.0;
      for (int i = 0; i < 4; i++) {
        double ly = boardY + 55.0 + i * spacing;
        // Outer ring
        g2d.setColor(LED_BORDER_COLOR);
        g2d.draw(new Ellipse2D.Double(ledX - 10, ly - 10, 20, 20));
        // Clear / Yellow dome
        g2d.setColor(LED_DOME_COLOR);
        g2d.fill(new Ellipse2D.Double(ledX - 8, ly - 8, 16, 16));
        // Highlight reflection
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(ledX - 4, ly - 6, 5, 5));

        // Silkscreen letter (A, B, C, D)
        g2d.setFont(LABEL_FONT_BOLD);
        String letter = String.valueOf((char) ('A' + i));
        StringUtils.drawCenteredText(g2d, letter, boardX + 185.0, ly,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }

      // Silkscreen Text Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(LABEL_FONT_BOLD);
      for (int i = 0; i < 7; i++) {
        StringUtils.drawCenteredText(g2d, "IN" + (i + 1), boardX + 16.0, y + i * spacing,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }

      double pwrHeaderY = boardY + boardH - 30.0;
      double pwrX0 = boardX + 47.0;
      double pwrX1 = boardX + 67.0;
      double pwrJumperCenterX = boardX + 97.0;

      StringUtils.drawCenteredText(g2d, "-", pwrX0, pwrHeaderY + 16.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "+", pwrX1, pwrHeaderY + 16.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "5-12V", pwrJumperCenterX, pwrHeaderY + 16.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Model text along right edge
      AffineTransform preModel = g2d.getTransform();
      g2d.rotate(Math.PI / 2, boardX + boardW - 14.0, boardY + boardH / 2.0);
      g2d.setFont(LABEL_FONT_BOLD);
      StringUtils.drawCenteredText(g2d, "28BYJ-48 DRIVER", boardX + boardW - 14.0, boardY + boardH / 2.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(preModel);
    }

    g2d.setTransform(oldTx);

    // Draw left input header pins (4 populated pins: IN1..IN4)
    drawPins(g2d, 0, 4, false, outlineMode, drawingObserver);

    // Draw bottom power header pins (4 pins: GND, 5-12V, Jumper 1, Jumper 2)
    drawPins(g2d, 4, 4, false, outlineMode, drawingObserver);

    // Draw 5-Pin JST motor connector contacts
    drawPins(g2d, 8, 5, true, outlineMode, drawingObserver);

    if (!outlineMode) {
      // Black Jumper shunt cap installed over the two rightmost power selection pins (pins 6 & 7)
      double pwrHeaderY = boardY + boardH - 30.0;
      double pwrJumperCenterX = boardX + 97.0;
      RoundRectangle2D jumperCap = new RoundRectangle2D.Double(pwrJumperCenterX - 16.0, pwrHeaderY - 7.0, 32.0, 14.0, 3, 3);
      g2d.setColor(JUMPER_COLOR);
      g2d.fill(jumperCap);
      g2d.setColor(Color.decode("#444444"));
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(jumperCap);
    }

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ULN_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));
    g2d.setColor(ULN_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));

    // Vertical DIP IC
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(width / 2 - 5, 5, 8, height - 10);

    // JST White connector
    g2d.setColor(JST_BODY_COLOR);
    g2d.fillRect(width - 12, 6, 6, height - 12);

    // LEDs
    g2d.setColor(LED_DOME_COLOR);
    g2d.fillOval(width - 6, 8, 3, 3);
    g2d.fillOval(width - 6, 14, 3, 3);
    g2d.fillOval(width - 6, 20, 3, 3);
    g2d.fillOval(width - 6, 26, 3, 3);
  }
}
