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
  public static Color INDUCTOR_CORE_COLOR = Color.decode("#1A252F");
  public static Color POT_SCREW_BRASS = Color.decode("#D4AC0D");

  public static Font PAD_LABEL_FONT = new Font("SansSerif", Font.BOLD, 9);

  public static Size BOARD_WIDTH = new Size(36.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(17.0d, SizeUnit.mm);

  private static final double PAD_MARGIN_X = 22.0;
  private static final double PAD_MARGIN_Y = 24.0;

  // Pinout matches physical MT3608 board:
  // Left: Output (0 = VOUT+, 1 = VOUT-)
  // Right: Input (2 = VIN+, 3 = VIN-)
  private static final String[] PIN_NAMES = {"VOUT+", "VOUT-", "VIN+", "VIN-"};

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

    // 4 Solder pads with realistic vertical spacing:
    // Output (Left): 0 = VOUT+ (Top-Left), 1 = VOUT- (Bottom-Left)
    // Input (Right): 2 = VIN+ (Top-Right), 3 = VIN- (Bottom-Right)
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },                                        // Pin 0: VOUT+
      { 0, h - 2 * PAD_MARGIN_Y },                     // Pin 1: VOUT-
      { w - 2 * PAD_MARGIN_X, 0 },                     // Pin 2: VIN+
      { w - 2 * PAD_MARGIN_X, h - 2 * PAD_MARGIN_Y }   // Pin 3: VIN-
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
    double boardX = x - PAD_MARGIN_X;
    double boardY = y - PAD_MARGIN_Y;
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
    double boardX = x - PAD_MARGIN_X;
    double boardY = y - PAD_MARGIN_Y;

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
      // Blue 3296W Trimmer Potentiometer (Left side)
      double potX = boardX + 44.0;
      double potY = boardY + 44.0;
      double potW = 48.0;
      double potH = 46.0;
      g2d.setColor(POT_BLUE);
      g2d.fill(new RoundRectangle2D.Double(potX, potY, potW, potH, 3, 3));
      g2d.setColor(POT_BLUE.darker());
      g2d.draw(new RoundRectangle2D.Double(potX, potY, potW, potH, 3, 3));

      // Brass screw head at bottom-left corner of the pot
      double screwX = potX + 11.0;
      double screwY = potY + potH - 11.0;
      double screwD = 13.0;
      g2d.setColor(POT_SCREW_BRASS);
      g2d.fill(new Ellipse2D.Double(screwX - screwD / 2.0, screwY - screwD / 2.0, screwD, screwD));
      g2d.setColor(Color.BLACK);
      g2d.drawLine((int) (screwX - 4), (int) screwY, (int) (screwX + 4), (int) screwY);

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "3296", potX + potW / 2.0 + 4, potY + 18,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Power Inductor (Top-Right / Center, marked 220)
      double indX = boardX + 115.0;
      double indY = boardY + 12.0;
      double indSize = 54.0;
      g2d.setColor(INDUCTOR_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(indX, indY, indSize, indSize, 6, 6));
      g2d.setColor(INDUCTOR_CORE_COLOR);
      g2d.fill(new Ellipse2D.Double(indX + 5, indY + 5, indSize - 10, indSize - 10));
      g2d.setColor(LIGHT_METAL_COLOR);
      g2d.fillRect((int) indX, (int) (indY + 16), 4, 22);
      g2d.fillRect((int) (indX + indSize - 4), (int) (indY + 16), 4, 22);
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "220", indX + indSize / 2.0, indY + indSize / 2.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // MT3608 SOT23-6 Boost Controller IC (Bottom-Right / Center)
      drawChip(g2d, boardX + 150.0, boardY + 78.0, 26.0, 22.0, "3608");

      // Silkscreen Text Labels (placed above/below pads without overlapping)
      g2d.setColor(Color.WHITE);
      g2d.setFont(PAD_LABEL_FONT);
      StringUtils.drawCenteredText(g2d, "VOUT+", boardX + PAD_MARGIN_X, boardY + 9.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "VOUT-", boardX + PAD_MARGIN_X, boardY + boardH - 9.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "VIN+", boardX + boardW - PAD_MARGIN_X, boardY + 9.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "VIN-", boardX + boardW - PAD_MARGIN_X, boardY + boardH - 9.0,
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw square/rounded rectangular tinned solder pads using LIGHT_METAL_COLOR
    drawSolderPads(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BOOST_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(POT_BLUE);
    g2d.fillRect(6, height / 2 - 4, 8, 8);
    g2d.setColor(INDUCTOR_BODY_COLOR);
    g2d.fillRect(width - 14, height / 2 - 4, 8, 8);
  }
}
