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

@ComponentDescriptor(name = "16-Channel PWM/Servo Driver (PCA9685)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "PCA9685 16-Channel 12-Bit PWM I2C Servo Controller Board",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class PCA9685ServoDriver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCA_BLUE = Color.decode("#1B4F72");
  public static Color PWM_YELLOW = Color.decode("#F1C40F");
  public static Color V_RED = Color.decode("#E74C3C");
  public static Color GND_BLACK = Color.decode("#1C1C1C");

  public static Size BOARD_WIDTH = new Size(62.5d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(25.4d, SizeUnit.mm);

  public PCA9685ServoDriver() {
    super();
    this.bodyColor = PCA_BLUE;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double w = BOARD_WIDTH.convertToPixels();

    // 60 pins total:
    // Left I2C header (6 pins): 0 = GND, 1 = OE, 2 = SCL, 3 = SDA, 4 = VCC, 5 = V+
    // Right I2C cascade (6 pins): 6..11
    // 16 Servo Channels x 3 rows (GND, V+, PWM) = 48 pins (12..59)
    double[][] relativeOffsets = new double[60][2];

    // Left I2C
    for (int i = 0; i < 6; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }

    // Right I2C
    for (int i = 0; i < 6; i++) {
      relativeOffsets[6 + i][0] = w - 20;
      relativeOffsets[6 + i][1] = i * spacing;
    }

    // 16-Channel 3-row Servo header (Center area)
    double startX = spacing * 2.5;
    int idx = 12;
    for (int col = 0; col < 16; col++) {
      for (int row = 0; row < 3; row++) {
        relativeOffsets[idx][0] = startX + col * spacing;
        relativeOffsets[idx][1] = row * spacing + spacing * 1.5;
        idx++;
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
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 10;
    double boardY = y - (boardH - 5 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - 10;
    double boardY = y - (boardH - 5 * PIN_SPACING.convertToPixels()) / 2.0;

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
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 16, 16);

      // 2-Pin Screw Terminal Block (External Servo Power V+, GND on Top Left)
      g2d.setColor(SCREW_TERMINAL_COLOR);
      g2d.fill(new RoundRectangle2D.Double(boardX + 45, boardY + 8, 40, 30, 3, 3));
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new Ellipse2D.Double(boardX + 50, boardY + 15, 12, 12));
      g2d.fill(new Ellipse2D.Double(boardX + 68, boardY + 15, 12, 12));

      // Large Filter Electrolytic Capacitor (Top)
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new Ellipse2D.Double(boardX + 100, boardY + 6, 32, 32));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new Ellipse2D.Double(boardX + 100, boardY + 6, 32, 32));

      // PCA9685 TSSOP-28 IC (Center)
      drawChip(g2d, boardX + boardW / 2.0 - 25, boardY + 10, 50, 24, "PCA9685");

      // Color-coded headers background strips for 16 channels
      double spacing = PIN_SPACING.convertToPixels();
      double startX = x + spacing * 2.5;
      double chanW = spacing * 16;

      g2d.setColor(GND_BLACK);
      g2d.fill(new Rectangle2D.Double(startX - 8, y + spacing * 1.5 - 6, chanW + 6, 12));
      g2d.setColor(V_RED);
      g2d.fill(new Rectangle2D.Double(startX - 8, y + spacing * 2.5 - 6, chanW + 6, 12));
      g2d.setColor(PWM_YELLOW);
      g2d.fill(new Rectangle2D.Double(startX - 8, y + spacing * 3.5 - 6, chanW + 6, 12));

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "PCA9685 16-CH SERVO DRIVER", boardX + boardW / 2.0, boardY + boardH - 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw all 60 pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCA_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(PWM_YELLOW);
    g2d.fillRect(6, height / 2 - 2, width - 12, 4);
    g2d.setColor(V_RED);
    g2d.fillRect(6, height / 2 + 2, width - 12, 4);
  }
}
