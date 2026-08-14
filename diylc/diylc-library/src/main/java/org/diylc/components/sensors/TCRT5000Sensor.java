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
package org.diylc.components.sensors;

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

@ComponentDescriptor(name = "Line / Obstacle Sensor (TCRT5000)", category = "Sensors",
    author = "Branislav Stojkovic", description = "TCRT5000 Infrared Line Tracking and Optical Obstacle Avoidance Sensor",
    instanceNamePrefix = "SEN", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class TCRT5000Sensor extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color SENSOR_BLUE = Color.decode("#1B4F72");
  public static Color POT_BLUE = Color.decode("#0055AA");

  public static Size BOARD_WIDTH = new Size(32.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(14.0d, SizeUnit.mm);

  public TCRT5000Sensor() {
    super();
    this.bodyColor = SENSOR_BLUE;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // 4-pin 0.1" header: VCC, GND, DO, AO
    double[][] relativeOffsets = new double[4][2];
    for (int i = 0; i < 4; i++) {
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
    double boardY = y - (boardH - 3 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardY = y - (boardH - 3 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // TCRT5000 Optical Package (Left Edge)
      g2d.setColor(Color.decode("#2C3E50"));
      g2d.fill(new RoundRectangle2D.Double(boardX - 10, boardY + boardH / 2.0 - 20, 36, 40, 4, 4));
      // Blue IR LED
      g2d.setColor(Color.decode("#3498DB"));
      g2d.fill(new Ellipse2D.Double(boardX - 6, boardY + boardH / 2.0 - 15, 14, 14));
      // Black Phototransistor
      g2d.setColor(Color.BLACK);
      g2d.fill(new Ellipse2D.Double(boardX - 6, boardY + boardH / 2.0 + 1, 14, 14));

      // LM393 Comparator IC (Center)
      drawChip(g2d, boardX + 45, boardY + boardH / 2.0 - 14, 30, 28, "393");

      // Sensitivity Trimmer Potentiometer (Right Center)
      g2d.setColor(POT_BLUE);
      g2d.fill(new RoundRectangle2D.Double(boardX + 90, boardY + boardH / 2.0 - 16, 32, 32, 3, 3));
      g2d.setColor(Color.decode("#BDC3C7"));
      g2d.fill(new Ellipse2D.Double(boardX + 96, boardY + boardH / 2.0 - 10, 20, 20));
      g2d.setColor(Color.BLACK);
      g2d.drawLine((int)(boardX + 99), (int)(boardY + boardH / 2.0), (int)(boardX + 113), (int)(boardY + boardH / 2.0));

      // Status LEDs (Power & Signal)
      g2d.setColor(Color.RED);
      g2d.fill(new RoundRectangle2D.Double(boardX + 135, boardY + 12, 10, 8, 2, 2));
      g2d.setColor(Color.GREEN);
      g2d.fill(new RoundRectangle2D.Double(boardX + 135, boardY + boardH - 20, 10, 8, 2, 2));

      // Silk Screen Pin Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      String[] labels = new String[] { "VCC", "GND", "DO", "AO" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 4; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], boardX + boardW - 24, y + i * spacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw 4 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(SENSOR_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(Color.CYAN);
    g2d.fill(new Ellipse2D.Double(4, height / 2 - 6, 6, 6));
    g2d.setColor(Color.BLACK);
    g2d.fill(new Ellipse2D.Double(4, height / 2, 6, 6));
  }
}
