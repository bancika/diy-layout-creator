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

@ComponentDescriptor(name = "Buck Converter (LM2596)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "LM2596 Adjustable DC-DC Step-Down Buck Converter Module",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class LM2596BuckConverter extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color BUCK_BLUE = Color.decode("#1F618D");
  public static Color POT_BLUE = Color.decode("#0055AA");
  public static Color COPPER_COIL = Color.decode("#D35400");

  public static Size BOARD_WIDTH = new Size(43.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(21.0d, SizeUnit.mm);

  public LM2596BuckConverter() {
    super();
    this.bodyColor = BUCK_BLUE;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double h = BOARD_HEIGHT.convertToPixels();
    double w = BOARD_WIDTH.convertToPixels();

    // 4 Solder / Screw terminal pads:
    // Input (Left): 0 = IN+, 1 = IN-
    // Output (Right): 2 = OUT+, 3 = OUT-
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },             // Pin 0: IN+
      { 0, h - 20 },        // Pin 1: IN-
      { w - 20, 0 },        // Pin 2: OUT+
      { w - 20, h - 20 }    // Pin 3: OUT-
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
      // 2 Diagonal Mounting holes
      drawMountingHole(g2d, boardX + 45, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 45, boardY + boardH - 16, 16);

      // Input Capacitor (Left)
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new Ellipse2D.Double(boardX + 22, boardY + boardH / 2.0 - 18, 36, 36));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new Ellipse2D.Double(boardX + 22, boardY + boardH / 2.0 - 18, 36, 36));

      // Output Capacitor (Right)
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fill(new Ellipse2D.Double(boardX + boardW - 58, boardY + boardH / 2.0 - 18, 36, 36));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new Ellipse2D.Double(boardX + boardW - 58, boardY + boardH / 2.0 - 18, 36, 36));

      // LM2596 IC (Center Left) with tab
      g2d.setColor(Color.decode("#333333"));
      g2d.fill(new RoundRectangle2D.Double(boardX + 70, boardY + 15, 38, 48, 3, 3));
      g2d.setColor(Color.decode("#CCCCCC"));
      g2d.fill(new RoundRectangle2D.Double(boardX + 70, boardY + 12, 38, 10, 2, 2));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "LM2596", boardX + 89, boardY + 38, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Toroidal Power Inductor (Center Right)
      g2d.setColor(Color.decode("#222222"));
      g2d.fill(new Ellipse2D.Double(boardX + 120, boardY + boardH / 2.0 - 24, 48, 48));
      g2d.setColor(COPPER_COIL);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3));
      g2d.draw(new Ellipse2D.Double(boardX + 124, boardY + boardH / 2.0 - 20, 40, 40));

      // Blue 3296W Trimmer Potentiometer (Top Center)
      g2d.setColor(POT_BLUE);
      g2d.fill(new RoundRectangle2D.Double(boardX + 185, boardY + 15, 45, 26, 3, 3));
      // Brass screw head
      g2d.setColor(Color.decode("#D4AC0D"));
      g2d.fill(new Ellipse2D.Double(boardX + 190, boardY + 22, 12, 12));
      g2d.setColor(Color.BLACK);
      g2d.drawLine((int)(boardX + 192), (int)(boardY + 28), (int)(boardX + 200), (int)(boardY + 28));

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "IN+", boardX + 14, boardY + 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "IN-", boardX + 14, boardY + boardH - 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT+", boardX + boardW - 14, boardY + 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT-", boardX + boardW - 14, boardY + boardH - 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw connection pads
    drawScrewTerminals(g2d, 0, controlPoints.length, 0, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BUCK_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(COPPER_COIL);
    g2d.fill(new Ellipse2D.Double(width / 2 - 4, height / 2 - 4, 8, 8));
    g2d.setColor(POT_BLUE);
    g2d.fillRect(width - 12, height / 2 - 5, 8, 10);
  }
}
