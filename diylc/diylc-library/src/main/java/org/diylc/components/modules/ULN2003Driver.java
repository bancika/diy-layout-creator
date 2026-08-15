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

@ComponentDescriptor(name = "Stepper Driver Board (ULN2003)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "ULN2003 Stepper Motor Driver Board with 4 Status LEDs",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ULN2003Driver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color ULN_BLUE = Color.decode("#1F618D");
  public static Color JST_WHITE = Color.decode("#EAEDED");
  public static Color LED_YELLOW = Color.decode("#F1C40F");

  public static Size BOARD_WIDTH = new Size(35.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(31.5d, SizeUnit.mm);

  public ULN2003Driver() {
    super();
    this.bodyColor = ULN_BLUE;
    updateControlPoints();
  }

  private static final String[] PIN_NAMES = {"IN1", "IN2", "IN3", "IN4", "5-12V", "GND",
      "A", "B", "C", "D", "COM"};

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

    // 11 pins total:
    // Pin 0..3: IN1, IN2, IN3, IN4 (Bottom left control header)
    // Pin 4, 5: +5-12V, GND (Power input header)
    // Pin 6..10: 5-pin JST motor socket (Top right)
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { spacing, 0 },
      { spacing * 2, 0 },
      { spacing * 3, 0 },
      { spacing * 5, 0 },
      { spacing * 6, 0 },
      // JST motor connector pins
      { spacing * 4, -spacing * 7 },
      { spacing * 5, -spacing * 7 },
      { spacing * 6, -spacing * 7 },
      { spacing * 7, -spacing * 7 },
      { spacing * 8, -spacing * 7 }
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
    double boardX = x - 20;
    double boardY = y - boardH + 20;
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 20;
    double boardY = y - boardH + 20;

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
      // 4 Mounting Holes in corners
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 16, 16);

      // ULN2003A DIP-16 IC Chip (Center)
      drawChip(g2d, boardX + 35, boardY + 45, 95, 42, "ULN2003A");

      // 4 Status LEDs (A, B, C, D)
      for (int i = 0; i < 4; i++) {
        double lx = boardX + 42 + i * 20;
        double ly = boardY + 25;
        g2d.setColor(LED_YELLOW);
        g2d.fill(new RoundRectangle2D.Double(lx - 5, ly - 5, 10, 10, 2, 2));
      }

      // JST-XH 5-Pin Shrouded Motor Header (Top Right)
      double spacing = PIN_SPACING.convertToPixels();
      g2d.setColor(JST_WHITE);
      g2d.fill(new RoundRectangle2D.Double(x + spacing * 3.5, y - spacing * 7.8, spacing * 5 + 10, spacing + 14, 3, 3));
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.draw(new RoundRectangle2D.Double(x + spacing * 3.5, y - spacing * 7.8, spacing * 5 + 10, spacing + 14, 3, 3));

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "IN1", x, y - 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "IN2", x + spacing, y - 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "IN3", x + spacing * 2, y - 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "IN4", x + spacing * 3, y - 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "5-12V", x + spacing * 5, y - 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "GND", x + spacing * 6, y - 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw header pins and JST contacts
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ULN_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));
    g2d.setColor(Color.DARK_GRAY);
    g2d.fillRect(6, height / 2 - 4, width - 12, 8);
    for (int i = 0; i < 4; i++) {
      g2d.setColor(LED_YELLOW);
      g2d.fill(new Ellipse2D.Double(6 + i * 5, 4, 3, 3));
    }
  }
}
