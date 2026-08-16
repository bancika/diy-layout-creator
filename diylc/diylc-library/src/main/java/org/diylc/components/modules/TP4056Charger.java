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

@ComponentDescriptor(name = "LiPo Charger (TP4056)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "TP4056 1A Li-ion Battery Charging and Protection Module",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class TP4056Charger extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color TP4056_BLUE = Color.decode("#1B4F72");
  public static Size BOARD_WIDTH = new Size(28.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(17.5d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {"IN+", "IN-", "OUT+", "B+", "B-", "OUT-"};

  public TP4056Charger() {
    super();
    this.bodyColor = TP4056_BLUE;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  private static final double PAD_MARGIN_X = 14.0;
  private static final double PAD_MARGIN_Y = 14.0;

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double h = BOARD_HEIGHT.convertToPixels();
    double w = BOARD_WIDTH.convertToPixels();

    // 6 Solder pads:
    // Input (Left): 0 = IN+ (Top-Left), 1 = IN- (Bottom-Left)
    // Output (Right): 2 = OUT+ (Top-Right), 3 = B+, 4 = B-, 5 = OUT- (Bottom-Right)
    double rightX = w - 2 * PAD_MARGIN_X;
    double padSpanY = h - 2 * PAD_MARGIN_Y;
    double rightSpacing = Math.min(spacing, padSpanY / 3.0);

    double[][] relativeOffsets = new double[][] {
      { 0, 0 },                           // Pin 0: IN+ (Top-Left)
      { 0, padSpanY },                    // Pin 1: IN- (Bottom-Left)
      { rightX, 0 },                      // Pin 2: OUT+ (Top-Right)
      { rightX, rightSpacing },           // Pin 3: B+
      { rightX, padSpanY - rightSpacing }, // Pin 4: B-
      { rightX, padSpanY }                // Pin 5: OUT- (Bottom-Right)
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
      // USB Type-C Connector (Left Edge)
      drawMetalConnector(g2d, boardX - 6, boardY + boardH / 2.0 - 18, 32, 36, "Type-C");

      // TP4056 Linear Charger IC (SOP-8)
      drawChip(g2d, boardX + 60, boardY + 20, 26, 32, "4056");

      // DW01A Battery Protection IC & Dual MOSFET
      drawChip(g2d, boardX + 110, boardY + 22, 18, 20, "DW01");
      drawChip(g2d, boardX + 110, boardY + boardH - 44, 20, 24, "8205");

      // Dual Status LEDs (Red = CHRG, Blue = STDBY)
      g2d.setColor(Color.RED);
      g2d.fill(new RoundRectangle2D.Double(boardX + 42, boardY + 18, 8, 12, 2, 2));
      g2d.setColor(Color.CYAN);
      g2d.fill(new RoundRectangle2D.Double(boardX + 42, boardY + boardH - 30, 8, 12, 2, 2));

      // Silk Screen Labels (cleanly positioned beside pads)
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "IN+", x + 16, y, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "IN-", x + 16, y + boardH - 2 * PAD_MARGIN_Y, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

      double rightX = x + boardW - 2 * PAD_MARGIN_X;
      double padSpanY = boardH - 2 * PAD_MARGIN_Y;
      double rightSpacing = Math.min(PIN_SPACING.convertToPixels(), padSpanY / 3.0);
      StringUtils.drawCenteredText(g2d, "OUT+", rightX - 16, y, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "B+", rightX - 16, y + rightSpacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "B-", rightX - 16, y + padSpanY - rightSpacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT-", rightX - 16, y + padSpanY, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw tinned solder pads
    drawSolderPads(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(TP4056_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(Color.LIGHT_GRAY);
    g2d.fillRect(2, height / 2 - 4, 8, 8);
    g2d.setColor(Color.RED);
    g2d.fill(new Ellipse2D.Double(14, height / 2 - 3, 6, 6));
    g2d.setColor(Color.CYAN);
    g2d.fill(new Ellipse2D.Double(22, height / 2 - 3, 6, 6));
  }
}
