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

@ComponentDescriptor(name = "Boost Converter (MT3608)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "MT3608 2A Adjustable DC-DC Step-Up Boost Converter Module",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class MT3608BoostConverter extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color BOOST_BLUE = Color.decode("#1B4F72");
  public static Color POT_BLUE = Color.decode("#0055AA");
  public static Color INDUCTOR_BODY_COLOR = Color.decode("#2C3E50");
  public static Color POT_SCREW_BRASS = Color.decode("#D4AC0D");

  public static Size BOARD_WIDTH = new Size(36.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(17.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {"VIN+", "VIN-", "VOUT+", "VOUT-"};

  public MT3608BoostConverter() {
    super();
    this.bodyColor = BOOST_BLUE;
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
    double h = BOARD_HEIGHT.convertToPixels();
    double w = BOARD_WIDTH.convertToPixels();

    // 4 Solder pads:
    // Input (Left): 0 = VIN+, 1 = VIN-
    // Output (Right): 2 = VOUT+, 3 = VOUT-
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },             // Pin 0: VIN+
      { 0, h - 20 },        // Pin 1: VIN-
      { w - 20, 0 },        // Pin 2: VOUT+
      { w - 20, h - 20 }    // Pin 3: VOUT-
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
    return new RoundRectangle2D.Double(x - 10, y - 10, boardW, boardH, 6, 6);
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
    double boardY = y - 10;

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
      // Power Inductor (Square SMD, Center Left)
      g2d.setColor(INDUCTOR_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(boardX + 35, boardY + boardH / 2.0 - 20, 40, 40, 4, 4));
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "220", boardX + 55, boardY + boardH / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // MT3608 SOT23-6 Boost Controller IC
      drawChip(g2d, boardX + 85, boardY + boardH / 2.0 - 10, 20, 20, "3608");

      // Blue 3296W Trimmer Potentiometer (Right Center)
      g2d.setColor(POT_BLUE);
      g2d.fill(new RoundRectangle2D.Double(boardX + 115, boardY + 12, 45, 26, 3, 3));
      // Brass screw head
      g2d.setColor(POT_SCREW_BRASS);
      g2d.fill(new Ellipse2D.Double(boardX + 120, boardY + 19, 12, 12));
      g2d.setColor(Color.BLACK);
      g2d.drawLine((int)(boardX + 122), (int)(boardY + 25), (int)(boardX + 130), (int)(boardY + 25));

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "VIN+", boardX + 15, boardY + 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "VIN-", boardX + 15, boardY + boardH - 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "VOUT+", boardX + boardW - 18, boardY + 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "VOUT-", boardX + boardW - 18, boardY + boardH - 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw connection pads
    drawScrewTerminals(g2d, 0, controlPoints.length, 0, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BOOST_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(INDUCTOR_BODY_COLOR);
    g2d.fillRect(6, height / 2 - 4, 8, 8);
    g2d.setColor(POT_BLUE);
    g2d.fillRect(width - 12, height / 2 - 4, 8, 8);
  }
}
