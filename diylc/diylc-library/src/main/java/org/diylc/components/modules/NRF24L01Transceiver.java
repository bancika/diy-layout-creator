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
import java.awt.geom.Path2D;
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

@ComponentDescriptor(name = "2.4GHz RF Transceiver (NRF24L01+)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "NRF24L01+ 2.4GHz Wireless SPI Transceiver Module",
    instanceNamePrefix = "RF", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class NRF24L01Transceiver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NRF_PCB = Color.decode("#111111");
  public static Color ANTENNA_GOLD = Color.decode("#D4AC0D");

  public static Size BOARD_WIDTH = new Size(29.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(15.2d, SizeUnit.mm);

  public NRF24L01Transceiver() {
    super();
    this.bodyColor = NRF_PCB;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // 2x4 Header (8 pins):
    // Row 1 (Top): 0 = GND, 1 = CE, 2 = SCK, 3 = MISO
    // Row 2 (Bottom): 4 = VCC (3.3V), 5 = CSN, 6 = MOSI, 7 = IRQ
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { spacing, 0 },
      { spacing * 2, 0 },
      { spacing * 3, 0 },
      { 0, spacing },
      { spacing, spacing },
      { spacing * 2, spacing },
      { spacing * 3, spacing }
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
    double boardX = x - boardW + 4 * PIN_SPACING.convertToPixels() + 10;
    double boardY = y - (boardH - PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - boardW + 4 * PIN_SPACING.convertToPixels() + 10;
    double boardY = y - (boardH - PIN_SPACING.convertToPixels()) / 2.0;

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
      // Serpentine PCB Trace Antenna (Left edge)
      g2d.setColor(ANTENNA_GOLD);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2.0f));
      Path2D antenna = new Path2D.Double();
      double ax = boardX + 8;
      double ay = boardY + 12;
      antenna.moveTo(ax, ay);
      antenna.lineTo(ax + 35, ay);
      antenna.lineTo(ax + 35, ay + 16);
      antenna.lineTo(ax + 8, ay + 16);
      antenna.lineTo(ax + 8, ay + 32);
      antenna.lineTo(ax + 35, ay + 32);
      antenna.lineTo(ax + 35, ay + 48);
      antenna.lineTo(ax + 8, ay + 48);
      antenna.lineTo(ax + 8, ay + 64);
      antenna.lineTo(ax + 35, ay + 64);
      g2d.draw(antenna);

      // NRF24L01+ QFN IC (Center)
      drawChip(g2d, boardX + 60, boardY + boardH / 2.0 - 15, 30, 30, "NRF");

      // 16.000 MHz Crystal (Silver can)
      g2d.setColor(Color.decode("#BDC3C7"));
      g2d.fill(new RoundRectangle2D.Double(boardX + 98, boardY + boardH / 2.0 - 10, 24, 38, 4, 4));
      g2d.setColor(Color.decode("#7F8C8D"));
      g2d.draw(new RoundRectangle2D.Double(boardX + 98, boardY + boardH / 2.0 - 10, 24, 38, 4, 4));

      // 2x4 Header Body (Black Shroud)
      g2d.setColor(Color.decode("#1C1C1C"));
      double spacing = PIN_SPACING.convertToPixels();
      g2d.fill(new RoundRectangle2D.Double(x - 6, y - 6, spacing * 3 + 12, spacing + 12, 3, 3));
      g2d.setColor(Color.decode("#333333"));
      g2d.draw(new RoundRectangle2D.Double(x - 6, y - 6, spacing * 3 + 12, spacing + 12, 3, 3));

      // Silk Screen Pin Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "NRF24L01+", boardX + boardW / 2.0, boardY + 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw 8 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(NRF_PCB);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(ANTENNA_GOLD);
    g2d.drawLine(4, 8, 12, 8);
    g2d.drawLine(12, 8, 12, 14);
    g2d.drawLine(12, 14, 4, 14);
    g2d.setColor(Color.GRAY);
    g2d.fillRect(width - 12, height / 2 - 4, 8, 8);
  }
}
