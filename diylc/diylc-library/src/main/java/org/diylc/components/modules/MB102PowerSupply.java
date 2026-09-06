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

@ComponentDescriptor(name = "Breadboard Power Supply (MB102)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "MB102 Dual 3.3V/5V Breadboard Power Supply Module",
    instanceNamePrefix = "PWR", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class MB102PowerSupply extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color MB102_PCB = Color.decode("#E8E4D9");
  public static Color JUMPER_YELLOW = Color.decode("#F1C40F");
  public static Color SWITCH_RED = Color.decode("#E74C3C");
  public static Color JACK_BODY_COLOR = Color.decode("#111111");
  public static Color JACK_BORDER_COLOR = Color.decode("#333333");
  public static Color POWER_LED_COLOR = Color.decode("#2ECC71");

  public static Size BOARD_WIDTH = new Size(32.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(54.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {
      "V+ (Top)", "GND (Top)", "V+ (Top)", "GND (Top)",
      "V+ (Bottom)", "GND (Bottom)", "V+ (Bottom)", "GND (Bottom)"
  };

  public MB102PowerSupply() {
    super();
    this.bodyColor = MB102_PCB;
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

    // 8 output pins to plug directly into breadboard power rails:
    // Top Power Rail (4 pins: 0..3): +, -, +, -
    // Bottom Power Rail (4 pins: 4..7): +, -, +, - spaced 18 grid units apart
    double railSpan = 18 * spacing;
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { spacing, 0 },
      { 0, spacing },
      { spacing, spacing },
      { 0, railSpan },
      { spacing, railSpan },
      { 0, railSpan + spacing },
      { spacing, railSpan + spacing }
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
    double boardX = x - boardW + PIN_SPACING.convertToPixels() + 25;
    double boardY = y - 20;
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - boardW + PIN_SPACING.convertToPixels() + 25;
    double boardY = y - 20;

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
      // DC Barrel Jack (Left Edge Center)
      g2d.setColor(JACK_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(boardX - 8, boardY + boardH / 2.0 - 25, 45, 50, 4, 4));
      g2d.setColor(JACK_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(boardX - 8, boardY + boardH / 2.0 - 25, 45, 50, 4, 4));
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new Ellipse2D.Double(boardX - 4, boardY + boardH / 2.0 - 10, 20, 20));
      g2d.setColor(Color.BLACK);
      g2d.fill(new Ellipse2D.Double(boardX + 1, boardY + boardH / 2.0 - 5, 10, 10));

      // USB-A Connector (Center)
      drawUsbA(g2d, boardX + 45, boardY + boardH / 2.0 - 22, 48, 44, "USB");

      // Push Button Latch Switch (Left-ish)
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new RoundRectangle2D.Double(boardX + 48, boardY + 45, 28, 28, 4, 4));
      g2d.setColor(SWITCH_RED);
      g2d.fill(new Ellipse2D.Double(boardX + 53, boardY + 50, 18, 18));

      // Power Green LED
      g2d.setColor(POWER_LED_COLOR);
      g2d.fill(new Ellipse2D.Double(boardX + 85, boardY + 54, 10, 10));

      // AMS1117 Voltage Regulators (3.3V and 5.0V)
      drawChip(g2d, boardX + 110, boardY + 70, 28, 22, "3.3V");
      drawChip(g2d, boardX + 110, boardY + boardH - 92, 28, 22, "5.0V");

      // Voltage Selection Jumpers (Top & Bottom rail selectors)
      g2d.setColor(JUMPER_YELLOW);
      g2d.fill(new RoundRectangle2D.Double(boardX + boardW - 75, boardY + 20, 26, 18, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(boardX + boardW - 75, boardY + boardH - 38, 26, 18, 3, 3));

      // Silk Screen Text
      g2d.setColor(Color.DARK_GRAY);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "MB102", boardX + boardW / 2.0, boardY + boardH - 60, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "3.3V / 5V", boardX + boardW / 2.0, boardY + boardH - 48, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Rail markings (+ / -)
      g2d.setColor(Color.RED);
      g2d.drawString("+", (int)(x - 12), (int)(y + 8));
      g2d.drawString("+", (int)(x - 12), (int)(y + 18 * PIN_SPACING.convertToPixels() + 8));
      g2d.setColor(Color.BLUE);
      g2d.drawString("-", (int)(x + PIN_SPACING.convertToPixels() + 6), (int)(y + 8));
      g2d.drawString("-", (int)(x + PIN_SPACING.convertToPixels() + 6), (int)(y + 18 * PIN_SPACING.convertToPixels() + 8));
    }

    g2d.setTransform(oldTx);

    // Draw header pins connecting to breadboard rails
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(MB102_PCB);
    g2d.fill(new RoundRectangle2D.Double(4, 2, width - 8, height - 4, 3, 3));
    g2d.setColor(SWITCH_RED);
    g2d.fill(new Ellipse2D.Double(width / 2.0 - 4, height / 2.0 - 4, 8, 8));
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(new RoundRectangle2D.Double(4, 2, width - 8, height - 4, 3, 3));
  }
}
