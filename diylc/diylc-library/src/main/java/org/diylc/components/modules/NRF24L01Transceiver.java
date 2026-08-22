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

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
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
  public static Color CRYSTAL_BODY = Color.decode("#85929E");
  public static Color CRYSTAL_BORDER = Color.decode("#BDC3C7");
  public static Color SILK_LINE_COLOR = Color.WHITE;

  public static Size BOARD_WIDTH = new Size(29.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(15.2d, SizeUnit.mm);

  // Pin offsets: pins start right at the top-left edge
  private static final double LEFT_PIN_OFFSET_X = 14.0;
  private static final double TOP_PIN_OFFSET_Y = 14.0;

  private static final String[] PIN_NAMES = {"GND", "VCC", "CE", "CSN", "SCK", "MOSI", "MISO", "IRQ"};

  public NRF24L01Transceiver() {
    super();
    this.bodyColor = NRF_PCB;
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

    // 4x2 Header (8 pins) — 4 rows, 2 columns positioned directly on top-left edge:
    // Row 0: 0 = GND,  1 = VCC (3.3V)
    // Row 1: 2 = CE,   3 = CSN
    // Row 2: 4 = SCK,  5 = MOSI
    // Row 3: 6 = MISO, 7 = IRQ
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { spacing, 0 },
      { 0, spacing },
      { spacing, spacing },
      { 0, spacing * 2 },
      { spacing, spacing * 2 },
      { 0, spacing * 3 },
      { spacing, spacing * 3 }
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
    double boardX = x - LEFT_PIN_OFFSET_X;
    double boardY = y - TOP_PIN_OFFSET_Y;
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

    double spacing = PIN_SPACING.convertToPixels();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - LEFT_PIN_OFFSET_X;
    double boardY = y - TOP_PIN_OFFSET_Y;

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
      // 2x4 Header silkscreen box on top-left edge
      g2d.setColor(SILK_LINE_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.2f));
      g2d.draw(new Rectangle2D.Double(boardX + 4.0, boardY + 4.0, spacing * 2 + 3.0, spacing * 4 + 3.0));
      // Pin 1 square marker around top-left pin
      g2d.draw(new Rectangle2D.Double(boardX + 4.0, boardY + 4.0, spacing + 1.0, spacing + 1.0));

      // NRF24L01+ QFN-20 IC (Center Upper)
      double icX = boardX + 88.0;
      double icY = boardY + 20.0;
      double icSize = 32.0;
      drawChip(g2d, icX, icY, icSize, icSize, "nRF24");

      // White corner silkscreen brackets around IC
      g2d.setColor(SILK_LINE_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.2f));
      // Top-left bracket
      g2d.drawLine((int) (icX - 3), (int) (icY + 4), (int) (icX - 3), (int) (icY - 3));
      g2d.drawLine((int) (icX - 3), (int) (icY - 3), (int) (icX + 4), (int) (icY - 3));
      // Top-right bracket
      g2d.drawLine((int) (icX + icSize - 4), (int) (icY - 3), (int) (icX + icSize + 3), (int) (icY - 3));
      g2d.drawLine((int) (icX + icSize + 3), (int) (icY - 3), (int) (icX + icSize + 3), (int) (icY + 4));
      // Bottom-left bracket
      g2d.drawLine((int) (icX - 3), (int) (icY + icSize - 4), (int) (icX - 3), (int) (icY + icSize + 3));
      g2d.drawLine((int) (icX - 3), (int) (icY + icSize + 3), (int) (icX + 4), (int) (icY + icSize + 3));
      // Bottom-right bracket
      g2d.drawLine((int) (icX + icSize - 4), (int) (icY + icSize + 3), (int) (icX + icSize + 3), (int) (icY + icSize + 3));
      g2d.drawLine((int) (icX + icSize + 3), (int) (icY + icSize + 3), (int) (icX + icSize + 3), (int) (icY + icSize - 4));

      // 16.000 MHz Metal Can Crystal (Bottom Center)
      double xtalW = 74.0;
      double xtalH = 24.0;
      double xtalX = boardX + 68.0;
      double xtalY = boardY + boardH - 30.0;
      RoundRectangle2D xtal = new RoundRectangle2D.Double(xtalX, xtalY, xtalW, xtalH, 12, 12);
      g2d.setColor(CRYSTAL_BODY);
      g2d.fill(xtal);
      g2d.setColor(CRYSTAL_BORDER);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      g2d.draw(xtal);

      // Exact 2.4GHz MIFA (Meandered Inverted-F Antenna) trace on right side
      double antLeftX = boardX + 150.0;
      double stubX = boardX + 164.0;
      double rightX = boardX + boardW - 6.0;
      double meanderLeftX = stubX + 12.0;
      double topY = boardY + 10.0;
      double bottomY = boardY + boardH - 10.0;
      double feedY = topY + 16.0;
      double stubBottomY = bottomY - 12.0;

      g2d.setColor(ANTENNA_GOLD);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3.5f));

      Path2D mifa = new Path2D.Double();

      // Continuous Meander Track:
      // Starts at stubX, topY -> goes across top rail to right edge
      mifa.moveTo(stubX, topY);
      mifa.lineTo(rightX, topY);

      // Meander Loop 1
      mifa.lineTo(rightX, topY + 14.0);
      mifa.lineTo(meanderLeftX, topY + 14.0);
      mifa.lineTo(meanderLeftX, topY + 28.0);
      mifa.lineTo(rightX, topY + 28.0);

      // Meander Loop 2
      mifa.lineTo(rightX, topY + 42.0);
      mifa.lineTo(meanderLeftX, topY + 42.0);
      mifa.lineTo(meanderLeftX, topY + 56.0);
      mifa.lineTo(rightX, topY + 56.0);

      // Meander Loop 3
      mifa.lineTo(rightX, topY + 70.0);
      mifa.lineTo(meanderLeftX, topY + 70.0);
      mifa.lineTo(meanderLeftX, topY + 84.0);
      mifa.lineTo(rightX, topY + 84.0);

      // Final drop to bottom-right corner
      mifa.lineTo(rightX, bottomY);

      // Horizontal RF Feed line (from RF network into the vertical stub)
      mifa.moveTo(antLeftX, feedY);
      mifa.lineTo(stubX, feedY);

      // Left Vertical Ground Shunt Stub (runs from top rail down past feed line)
      mifa.moveTo(stubX, topY);
      mifa.lineTo(stubX, stubBottomY);

      g2d.draw(mifa);

      // Small gold ground via dot near bottom-right
      g2d.fill(new Ellipse2D.Double(stubX + 20.0, bottomY - 6.0, 3.5, 3.5));
    }

    g2d.setTransform(oldTx);

    // Draw 8 header pins on top-left edge
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(NRF_PCB);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(ANTENNA_GOLD);
    g2d.drawLine(width - 6, 8, width - 6, height - 8);
    g2d.drawLine(width - 6, 8, width - 12, 8);
    g2d.drawLine(width - 6, 14, width - 12, 14);
    g2d.drawLine(width - 6, 20, width - 12, 20);
    g2d.setColor(CRYSTAL_BODY);
    g2d.fillRoundRect(6, height - 12, 16, 6, 3, 3);
  }
}
