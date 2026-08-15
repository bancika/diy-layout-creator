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

@ComponentDescriptor(name = "Rotary Encoder (KY-040)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "KY-040 Incremental Rotary Encoder Module with Push Button",
    instanceNamePrefix = "ENC", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RotaryEncoderKY040 extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color ENCODER_PCB = Color.decode("#1B2631");
  public static Color METAL_SHAFT = Color.decode("#BDC3C7");

  public static Size BOARD_WIDTH = new Size(32.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(19.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {"CLK", "DT", "SW", "+", "GND"};

  public RotaryEncoderKY040() {
    super();
    this.bodyColor = ENCODER_PCB;
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

    // 5-pin 0.1" header: CLK, DT, SW, +, GND
    double[][] relativeOffsets = new double[5][2];
    for (int i = 0; i < 5; i++) {
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
    double boardY = y - (boardH - 4 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - boardW + 20;
    double boardY = y - (boardH - 4 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // 2 Mounting Holes
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);

      // Rotary Encoder Metal Body (Center Left)
      double cx = boardX + 90;
      double cy = boardY + boardH / 2.0;

      g2d.setColor(Color.decode("#7F8C8D"));
      g2d.fill(new RoundRectangle2D.Double(cx - 36, cy - 36, 72, 72, 6, 6));

      // Threaded Collar / Bushing
      g2d.setColor(METAL_SHAFT);
      g2d.fill(new Ellipse2D.Double(cx - 24, cy - 24, 48, 48));
      g2d.setColor(Color.decode("#95A5A6"));
      g2d.draw(new Ellipse2D.Double(cx - 24, cy - 24, 48, 48));

      // D-Shaft (Flatted center shaft)
      g2d.setColor(Color.decode("#34495E"));
      g2d.fill(new Ellipse2D.Double(cx - 15, cy - 15, 30, 30));
      g2d.setColor(METAL_SHAFT);
      g2d.fillRect((int)(cx + 4), (int)(cy - 14), 10, 28);

      // SMD Pull-up Resistors (10k)
      drawChip(g2d, boardX + 145, boardY + 25, 12, 18, "");
      drawChip(g2d, boardX + 145, boardY + boardH - 43, 12, 18, "");

      // Silk Screen Pin Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      String[] labels = new String[] { "CLK", "DT", "SW", "+", "GND" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 5; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], boardX + boardW - 32, y + i * spacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw 5 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ENCODER_PCB);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(METAL_SHAFT);
    g2d.fill(new Ellipse2D.Double(width / 2.0 - 8, height / 2.0 - 8, 16, 16));
    g2d.setColor(Color.decode("#34495E"));
    g2d.fill(new Ellipse2D.Double(width / 2.0 - 4, height / 2.0 - 4, 8, 8));
  }
}
