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
import java.awt.geom.Path2D;
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

@ComponentDescriptor(name = "Soil Moisture Sensor (Capacitive)", category = "Sensors",
    author = "Branislav Stojkovic", description = "Capacitive Corrosion-Resistant Soil Moisture Sensor Module",
    instanceNamePrefix = "SEN", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class SoilMoistureSensor extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color SOIL_PCB = Color.decode("#17202A");
  public static Color PROBE_TRACE = Color.decode("#273746");

  public static Size BOARD_WIDTH = new Size(60.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(20.0d, SizeUnit.mm);

  public SoilMoistureSensor() {
    super();
    this.bodyColor = SOIL_PCB;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // 3-pin 0.1" header on handle: VCC, GND, AOUT
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { 0, spacing },
      { 0, spacing * 2 }
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
    double boardX = x - 15;
    double boardY = y - (boardH - 2 * PIN_SPACING.convertToPixels()) / 2.0;

    Path2D path = new Path2D.Double();
    path.moveTo(boardX, boardY);
    path.lineTo(boardX + boardW * 0.35, boardY);
    path.lineTo(boardX + boardW - 15, boardY + 4);
    path.lineTo(boardX + boardW, boardY + boardH / 2.0);
    path.lineTo(boardX + boardW - 15, boardY + boardH - 4);
    path.lineTo(boardX + boardW * 0.35, boardY + boardH);
    path.lineTo(boardX, boardY + boardH);
    path.closePath();
    return path;
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
    double boardX = x - 15;
    double boardY = y - (boardH - 2 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // Capacitive Sensing Copper Area (Right blade section)
      double bladeX = boardX + boardW * 0.38;
      double bladeW = boardW * 0.55;
      g2d.setColor(PROBE_TRACE);
      g2d.fill(new RoundRectangle2D.Double(bladeX, boardY + 12, bladeW, boardH - 24, 6, 6));

      // White silk dividing line & warning
      g2d.setColor(Color.WHITE);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.drawLine((int)(boardX + boardW * 0.35), (int)boardY + 4, (int)(boardX + boardW * 0.35), (int)(boardY + boardH - 4));

      // NE555 / TLC555 Oscillator Timer IC (Handle area)
      drawChip(g2d, boardX + 45, boardY + boardH / 2.0 - 14, 28, 28, "555");

      // 3.3V Voltage Regulator
      drawChip(g2d, boardX + 85, boardY + boardH / 2.0 - 10, 18, 20, "");

      // Silk Screen Text
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "Capacitive Soil Sensor v1.2", bladeX + bladeW / 2.0, boardY + boardH / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Pin labels
      String[] labels = new String[] { "VCC", "GND", "AOUT" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 3; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], x + 16, y + i * spacing, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw 3 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(SOIL_PCB);
    Path2D p = new Path2D.Double();
    p.moveTo(2, 6);
    p.lineTo(width - 10, 6);
    p.lineTo(width - 2, height / 2.0);
    p.lineTo(width - 10, height - 6);
    p.lineTo(2, height - 6);
    p.closePath();
    g2d.fill(p);
    g2d.setColor(PROBE_TRACE);
    g2d.fillRect(width / 2, 8, width / 2 - 8, height - 16);
  }
}
