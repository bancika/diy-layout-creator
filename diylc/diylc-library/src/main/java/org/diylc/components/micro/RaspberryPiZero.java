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
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.micro;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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

@ComponentDescriptor(name = "Raspberry Pi Zero", category = "Controllers",
    author = "Branislav Stojkovic", description = "Raspberry Pi Zero / Zero W Compact Single Board Computer",
    instanceNamePrefix = "SBC", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RaspberryPiZero extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color RPI_GREEN = Color.decode("#1B5E20");
  public static Size BOARD_WIDTH = new Size(65.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(30.0d, SizeUnit.mm);

  public RaspberryPiZero() {
    super();
    this.bodyColor = RPI_GREEN;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < RaspberryPi.PIN_NAMES.length) {
      return RaspberryPi.PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[40][2];
    for (int col = 0; col < 20; col++) {
      int pinOdd = col * 2;
      int pinEven = col * 2 + 1;
      relativeOffsets[pinOdd][0] = col * spacing;
      relativeOffsets[pinOdd][1] = spacing;
      relativeOffsets[pinEven][0] = col * spacing;
      relativeOffsets[pinEven][1] = 0;
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
    double boardX = x - 55;
    double boardY = y - 25;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 12, 12);
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
    double boardX = x - 55;
    double boardY = y - 25;

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
      drawMountingHole(g2d, boardX + 25, boardY + 25, 20);
      drawMountingHole(g2d, boardX + 25, boardY + boardH - 25, 20);
      drawMountingHole(g2d, boardX + boardW - 25, boardY + 25, 20);
      drawMountingHole(g2d, boardX + boardW - 25, boardY + boardH - 25, 20);

      // Mini HDMI & Dual Micro-USB
      drawMetalConnector(g2d, boardX + 80, boardY + boardH - 12, 45, 24, "HDMI");
      drawMetalConnector(g2d, boardX + 240, boardY + boardH - 12, 40, 24, "USB");
      drawMetalConnector(g2d, boardX + 340, boardY + boardH - 12, 40, 24, "PWR");

      // SoC chip
      drawChip(g2d, boardX + 150, boardY + 80, 80, 80, "RPi SoC");

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "Pi Zero", boardX + 280, boardY + 100, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RPI_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 8, width - 4, height - 16, 4, 4));
    g2d.setColor(RPI_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 8, width - 4, height - 16, 4, 4));

    g2d.setColor(HEADER_BODY_COLOR);
    g2d.fillRect(6, 10, width - 12, 3);

    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(11, 15, 10, 6);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "ZERO", width / 2, height / 2 + 5, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
