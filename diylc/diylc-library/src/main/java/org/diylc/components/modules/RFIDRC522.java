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

@ComponentDescriptor(name = "RFID Reader (RC522)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "MFRC522 13.56MHz RFID Reader/Writer SPI Module",
    instanceNamePrefix = "RFID", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RFIDRC522 extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color RFID_BLUE = Color.decode("#1B4F72");
  public static Color ANTENNA_GOLD = Color.decode("#D4AC0D");
  public static Color CRYSTAL_BODY = Color.decode("#BDC3C7");
  public static Color CRYSTAL_BORDER = Color.decode("#7F8C8D");

  public static Size BOARD_WIDTH = new Size(60.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(40.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {"SDA", "SCK", "MOSI", "MISO", "IRQ", "GND", "RST", "3.3V"};

  public RFIDRC522() {
    super();
    this.bodyColor = RFID_BLUE;
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

    // 8-pin 0.1" header: SDA, SCK, MOSI, MISO, IRQ, GND, RST, 3.3V
    double[][] relativeOffsets = new double[8][2];
    for (int i = 0; i < 8; i++) {
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
    double boardY = y - (boardH - 7 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - boardW + 20;
    double boardY = y - (boardH - 7 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // 4 Mounting Holes
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);
      drawMountingHole(g2d, boardX + boardW - 40, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 40, boardY + boardH - 16, 16);

      // Large PCB Loop Antenna (Left side)
      double antX = boardX + 25;
      double antY = boardY + 20;
      double antW = boardW * 0.45;
      double antH = boardH - 40;

      g2d.setColor(ANTENNA_GOLD);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      for (int i = 0; i < 4; i++) {
        g2d.draw(new RoundRectangle2D.Double(antX + i * 4, antY + i * 4, antW - i * 8, antH - i * 8, 4, 4));
      }

      // MFRC522 QFN IC (Center Right)
      double icX = boardX + boardW * 0.62;
      double icY = boardY + boardH / 2.0 - 18;
      drawChip(g2d, icX, icY, 36, 36, "RC522");

      // 27.120 MHz Crystal
      g2d.setColor(CRYSTAL_BODY);
      g2d.fill(new RoundRectangle2D.Double(icX - 25, icY + 6, 18, 24, 3, 3));
      g2d.setColor(CRYSTAL_BORDER);
      g2d.draw(new RoundRectangle2D.Double(icX - 25, icY + 6, 18, 24, 3, 3));

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "RFID-RC522", antX + antW / 2.0, antY + antH / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silk Screen Pin Labels
      g2d.setFont(SILK_FONT_SMALL);
      String[] labels = new String[] { "SDA", "SCK", "MOSI", "MISO", "IRQ", "GND", "RST", "3.3V" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 8; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], boardX + boardW - 24, y + i * spacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw 8 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RFID_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));
    g2d.setColor(ANTENNA_GOLD);
    g2d.drawRect(6, 6, width / 2 - 4, height - 12);
    g2d.drawRect(8, 8, width / 2 - 8, height - 16);
    g2d.setColor(Color.BLACK);
    g2d.fillRect(width - 12, height / 2 - 4, 8, 8);
  }
}
