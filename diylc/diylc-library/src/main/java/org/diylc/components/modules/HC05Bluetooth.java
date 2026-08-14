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

@ComponentDescriptor(name = "Bluetooth Module (HC-05 / HC-06)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "HC-05 / HC-06 Serial Bluetooth SPP Module Breakout",
    instanceNamePrefix = "BT", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class HC05Bluetooth extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color BT_BLUE = Color.decode("#1B4F72");
  public static Color DAUGHTER_BOARD = Color.decode("#154360");

  public static Size BOARD_WIDTH = new Size(37.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(16.0d, SizeUnit.mm);

  public HC05Bluetooth() {
    super();
    this.bodyColor = BT_BLUE;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // 6-pin 0.1" header: STATE, RXD, TXD, GND, VCC, EN
    double[][] relativeOffsets = new double[6][2];
    for (int i = 0; i < 6; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
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
    double boardX = x - boardW + 20;
    double boardY = y - (boardH - 5 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - boardW + 20;
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
      // Bluetooth Daughterboard (HC-05 Core Module)
      double dbX = boardX + 15;
      double dbY = boardY + 12;
      double dbW = boardW - 55;
      double dbH = boardH - 24;

      g2d.setColor(DAUGHTER_BOARD);
      g2d.fill(new RoundRectangle2D.Double(dbX, dbY, dbW, dbH, 3, 3));
      g2d.setColor(Color.decode("#2E86C1"));
      g2d.draw(new RoundRectangle2D.Double(dbX, dbY, dbW, dbH, 3, 3));

      // Metal RF Shield on daughterboard
      g2d.setColor(METAL_SHIELD_COLOR);
      g2d.fill(new RoundRectangle2D.Double(dbX + 15, dbY + 10, dbW - 40, dbH - 20, 3, 3));
      g2d.setColor(METAL_SHIELD_BORDER);
      g2d.draw(new RoundRectangle2D.Double(dbX + 15, dbY + 10, dbW - 40, dbH - 20, 3, 3));
      g2d.setColor(Color.decode("#555555"));
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "HC-05", dbX + 15 + (dbW - 40) / 2.0, dbY + dbH / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Gold PCB Antenna on left tip of daughterboard
      g2d.setColor(Color.decode("#D4AC0D"));
      g2d.fillRect((int)(dbX + 4), (int)(dbY + 8), 8, (int)(dbH - 16));

      // Tactile Key/EN Push Button (Top Right)
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new RoundRectangle2D.Double(boardX + boardW - 35, boardY + 10, 14, 14, 2, 2));
      g2d.setColor(Color.DARK_GRAY);
      g2d.fill(new Ellipse2D.Double(boardX + boardW - 32, boardY + 13, 8, 8));

      // Status LED (Blue, Bottom Right)
      g2d.setColor(Color.CYAN);
      g2d.fill(new RoundRectangle2D.Double(boardX + boardW - 34, boardY + boardH - 24, 12, 10, 2, 2));

      // Silk Screen Pin Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      String[] labels = new String[] { "STATE", "RXD", "TXD", "GND", "VCC", "EN" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 6; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], boardX + boardW - 24, y + i * spacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw 6 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BT_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(6, height / 2 - 4, 10, 8);
    g2d.setColor(Color.CYAN);
    g2d.fill(new Ellipse2D.Double(width - 10, height / 2 - 3, 6, 6));
  }
}
