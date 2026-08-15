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

@ComponentDescriptor(name = "Motor Driver (L298N)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "L298N Dual H-Bridge DC and Stepper Motor Driver Module with Heatsink",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class L298NMotorDriver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color L298_RED = Color.decode("#B71C1C");
  public static Color HEATSINK_COLOR = Color.decode("#263238");
  public static Size BOARD_SIZE = new Size(43.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Power Terminal (0..2)
      "12V (VCC)", "GND", "5V (Logic)",
      // Motor A Terminal (3..4)
      "OUT1", "OUT2",
      // Motor B Terminal (5..6)
      "OUT3", "OUT4",
      // Logic Control Header (7..12)
      "ENA", "IN1", "IN2", "IN3", "IN4", "ENB"
  };

  public L298NMotorDriver() {
    super();
    this.bodyColor = L298_RED;
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
    double termSpacing = new Size(5.08d, SizeUnit.mm).convertToPixels(); // 40px
    double pinSpacing = PIN_SPACING.convertToPixels(); // 20px
    double boardSizePx = BOARD_SIZE.convertToPixels();
    double boardX = -45;

    double[][] relativeOffsets = new double[][] {
      // Power screw terminal (0..2) at bottom-left: 12V, GND, 5V
      { 0, 0 },
      { termSpacing, 0 },
      { termSpacing * 2, 0 },
      // Motor A terminal (3..4) on left edge: OUT1, OUT2
      { boardX + 22.5, -140 },
      { boardX + 22.5, -180 },
      // Motor B terminal (5..6) on right edge: OUT3, OUT4
      { boardX + boardSizePx - 22.5, -140 },
      { boardX + boardSizePx - 22.5, -180 },
      // Logic header (7..12) at bottom-right: ENA, IN1, IN2, IN3, IN4, ENB
      { 140, 0 },
      { 140 + pinSpacing, 0 },
      { 140 + pinSpacing * 2, 0 },
      { 140 + pinSpacing * 3, 0 },
      { 140 + pinSpacing * 4, 0 },
      { 140 + pinSpacing * 5, 0 }
    };

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardSizePx = BOARD_SIZE.convertToPixels();
    double boardX = x - 45;
    double boardY = y - boardSizePx + 25;
    return new RoundRectangle2D.Double(boardX, boardY, boardSizePx, boardSizePx, 10, 10);
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

    double boardSizePx = BOARD_SIZE.convertToPixels();
    double boardX = x - 45;
    double boardY = y - boardSizePx + 25;

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
      drawMountingHole(g2d, boardX + 20, boardY + 20, 20);
      drawMountingHole(g2d, boardX + 20, boardY + boardSizePx - 20, 20);
      drawMountingHole(g2d, boardX + boardSizePx - 20, boardY + 20, 20);
      drawMountingHole(g2d, boardX + boardSizePx - 20, boardY + boardSizePx - 20, 20);

      // Large black aluminum heatsink in center
      double hsW = boardSizePx - 100;
      double hsH = 90;
      double hsX = boardX + (boardSizePx - hsW) / 2.0;
      double hsY = boardY + 50;

      g2d.setColor(HEATSINK_COLOR);
      g2d.fill(new RoundRectangle2D.Double(hsX, hsY, hsW, hsH, 4, 4));

      // Heatsink fins
      g2d.setColor(Color.decode("#455A64"));
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
      for (double fx = hsX + 15; fx < hsX + hsW - 10; fx += 18) {
        g2d.drawLine((int) fx, (int) hsY + 5, (int) fx, (int) (hsY + hsH - 5));
      }

      // L298 Multiwatt IC mounted to heatsink
      drawChip(g2d, hsX + (hsW - 100) / 2.0, hsY + hsH - 15, 100, 30, "L298N");

      // Green screw terminal block bodies
      g2d.setColor(SCREW_TERMINAL_COLOR);
      RoundRectangle2D tb1 = new RoundRectangle2D.Double(x - 15, y - 25, 120, 35, 3, 3);
      RoundRectangle2D tb2 = new RoundRectangle2D.Double(boardX + 5, y - 195, 35, 75, 3, 3);
      RoundRectangle2D tb3 = new RoundRectangle2D.Double(boardX + boardSizePx - 40, y - 195, 35, 75, 3, 3);
      g2d.fill(tb1);
      g2d.fill(tb2);
      g2d.fill(tb3);
      g2d.setColor(SCREW_TERMINAL_BORDER);
      g2d.draw(tb1);
      g2d.draw(tb2);
      g2d.draw(tb3);

      // Logic header black block
      g2d.setColor(HEADER_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(x + 130, y - 10, 128, 20, 2, 2));

      // Silkscreen
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "L298N DRIVER", boardX + boardSizePx / 2.0, boardY + 30, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Terminal Silk labels
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "12V GND 5V", x + 40, y - 32, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT1/2", boardX + 50, y - 160, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT3/4", boardX + boardSizePx - 50, y - 160, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw terminals and logic header pins
    drawScrewTerminals(g2d, 0, 7, 40, outlineMode, drawingObserver);
    drawPins(g2d, 7, 6, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(L298_RED);
    g2d.fill(new RoundRectangle2D.Double(3, 3, width - 6, height - 6, 3, 3));
    g2d.setColor(L298_RED.darker());
    g2d.draw(new RoundRectangle2D.Double(3, 3, width - 6, height - 6, 3, 3));

    // Heatsink
    g2d.setColor(HEATSINK_COLOR);
    g2d.fillRect(7, 8, width - 14, 10);

    // Terminals
    g2d.setColor(SCREW_TERMINAL_COLOR);
    g2d.fillRect(5, height - 8, 10, 5);
    g2d.fillRect(width - 15, height - 8, 10, 5);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "L298", width / 2, height / 2 + 5, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
