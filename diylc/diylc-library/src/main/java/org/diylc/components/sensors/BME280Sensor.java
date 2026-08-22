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

@ComponentDescriptor(name = "Temp/Pressure/Humidity Sensor (BME280)", category = "Sensors",
    author = "Branislav Stojkovic", description = "BME280 / BMP280 Temperature, Humidity, and Pressure Sensor (I2C/SPI)",
    instanceNamePrefix = "SEN", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class BME280Sensor extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color BME_PURPLE = Color.decode("#5B2C6F");
  public static Color SENSOR_METAL = Color.decode("#C0C0C0");

  public static Size BOARD_WIDTH = new Size(15.2d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(12.0d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {"VCC", "GND", "SCL", "SDA", "CSB", "SDO"};

  public BME280Sensor() {
    super();
    this.bodyColor = BME_PURPLE;
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

    // 6-pin 0.1" header: VCC, GND, SCL, SDA, CSB, SDO
    double[][] relativeOffsets = new double[6][2];
    for (int i = 0; i < 6; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
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
    double boardX = x - (boardW - 5 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - 16;
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
    double boardX = x - (boardW - 5 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - 16;

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
      // 1 Mounting Hole (Right side)
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 20, 14);

      // BME280 Metal Sensor Can with vent hole (Center)
      double sx = boardX + 35;
      double sy = boardY + boardH - 35;
      g2d.setColor(SENSOR_METAL);
      g2d.fill(new RoundRectangle2D.Double(sx, sy, 24, 20, 2, 2));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new RoundRectangle2D.Double(sx, sy, 24, 20, 2, 2));
      // Vent hole
      g2d.setColor(Color.BLACK);
      g2d.fill(new Ellipse2D.Double(sx + 5, sy + 5, 4, 4));

      // LDO Voltage Regulator (Left)
      drawChip(g2d, boardX + 12, boardY + boardH - 32, 16, 16, "");

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "BME280", boardX + boardW / 2.0, boardY + 18, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    // Draw 6 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BME_PURPLE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(SENSOR_METAL);
    g2d.fill(new RoundRectangle2D.Double(width / 2 - 5, height / 2 - 4, 10, 8, 2, 2));
    g2d.setColor(Color.BLACK);
    g2d.fill(new Ellipse2D.Double(width / 2 - 3, height / 2 - 2, 2, 2));
  }
}
