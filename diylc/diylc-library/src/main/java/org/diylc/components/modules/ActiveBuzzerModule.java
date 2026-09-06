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

@ComponentDescriptor(name = "Active Buzzer Module (KY-012)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "KY-012 Active Buzzer Module — produces 2.5kHz continuous tone on HIGH signal",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ActiveBuzzerModule extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCB_GREEN = Color.decode("#1A6B1A");
  public static Color BUZZER_BLACK = Color.decode("#1A1A1A");
  public static Color BUZZER_VENT_COLOR = Color.decode("#333333");

  public static Size BOARD_WIDTH = new Size(18.5d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(15.0d, SizeUnit.mm);

  // Standard Keyes KY-012 3-pin layout from left to right: - (GND), + (VCC), S (Signal)
  private static final String[] PIN_NAMES = {"- (GND)", "+ (VCC)", "S (Signal)"};

  public ActiveBuzzerModule() {
    super();
    this.bodyColor = PCB_GREEN;
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

    // 3 pins along the bottom edge: - (GND), + (VCC), S (Signal)
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { spacing, 0 },
      { spacing * 2, 0 }
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
    double boardX = x - (boardW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - boardH + 12;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 4, 4);
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
    double boardX = x - (boardW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - boardH + 12;
    double spacing = PIN_SPACING.convertToPixels();

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
      // Buzzer cylinder — circular element on the board
      double buzzerD = new Size(11.0d, SizeUnit.mm).convertToPixels();
      double buzzerX = boardX + (boardW - buzzerD) / 2.0;
      double buzzerY = boardY + 4;

      // Outer black cylinder
      g2d.setColor(BUZZER_BLACK);
      g2d.fill(new Ellipse2D.Double(buzzerX, buzzerY, buzzerD, buzzerD));
      g2d.setColor(BUZZER_BLACK.brighter());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      g2d.draw(new Ellipse2D.Double(buzzerX, buzzerY, buzzerD, buzzerD));

      // Vent hole ring (inner concentric)
      double ventD = buzzerD * 0.55;
      double ventX = buzzerX + (buzzerD - ventD) / 2.0;
      double ventY = buzzerY + (buzzerD - ventD) / 2.0;
      g2d.setColor(BUZZER_VENT_COLOR);
      g2d.draw(new Ellipse2D.Double(ventX, ventY, ventD, ventD));

      // Small central sound vent hole
      double holeD = buzzerD * 0.15;
      double holeX = buzzerX + (buzzerD - holeD) / 2.0;
      double holeY = buzzerY + (buzzerD - holeD) / 2.0;
      g2d.setColor(Color.decode("#111111"));
      g2d.fill(new Ellipse2D.Double(holeX, holeY, holeD, holeD));

      // Polarity "+" marking on buzzer casing
      g2d.setColor(Color.WHITE);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
      StringUtils.drawCenteredText(g2d, "+", buzzerX + 6, buzzerY + 7, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // "KY-012" model text in top-left silkscreen corner
      g2d.setColor(Color.WHITE);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
      StringUtils.drawCenteredText(g2d, "KY-012", boardX + 14, boardY + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen pin labels (-, +, S) directly above the 3 header pins
      g2d.setColor(Color.WHITE);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
      double labelY = y - 9;
      StringUtils.drawCenteredText(g2d, "-", x, labelY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "+", x + spacing, labelY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "S", x + spacing * 2, labelY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);
    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // Green PCB
    g2d.setColor(PCB_GREEN);
    g2d.fill(new RoundRectangle2D.Double(3, 5, width - 6, height - 10, 3, 3));
    g2d.setColor(PCB_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(3, 5, width - 6, height - 10, 3, 3));

    // Black buzzer cylinder
    int bD = width - 10;
    int bX = (width - bD) / 2;
    int bY = 5;
    g2d.setColor(BUZZER_BLACK);
    g2d.fillOval(bX, bY, bD, bD);
    g2d.setColor(Color.DARK_GRAY);
    g2d.drawOval(bX, bY, bD, bD);

    // Vent circle
    int vD = bD / 2;
    g2d.setColor(BUZZER_VENT_COLOR);
    g2d.drawOval(bX + (bD - vD) / 2, bY + (bD - vD) / 2, vD, vD);

    // 3 pin dots
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 3; i++) {
      g2d.fillRect(6 + i * 4, height - 5, 2, 2);
    }
  }
}
