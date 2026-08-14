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
package org.diylc.components.sensors;

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

@ComponentDescriptor(name = "PIR Motion Sensor (HC-SR501)", category = "Sensors",
    author = "Branislav Stojkovic", description = "HC-SR501 Passive Infrared (PIR) Motion Detector Module",
    instanceNamePrefix = "SEN", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class PIRMotionSensor extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color SENSOR_GREEN = Color.decode("#2E7D32");
  public static Color DOME_COLOR = Color.decode("#F5F5F5");
  public static Color TRIMPOT_COLOR = Color.decode("#E67E22");

  public static Size BOARD_WIDTH = new Size(32.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(24.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {"VCC", "OUT", "GND"};

  public PIRMotionSensor() {
    super();
    this.bodyColor = SENSOR_GREEN;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[3][2];
    for (int i = 0; i < 3; i++) {
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
    double boardX = x - (boardW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - boardH + 12;
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
    double boardX = x - (boardW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - boardH + 12;

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
      // 2 Corner mounting holes
      drawMountingHole(g2d, boardX + 15, boardY + 15, 16);
      drawMountingHole(g2d, boardX + boardW - 15, boardY + 15, 16);

      // Large central white Fresnel lens dome
      double domeD = new Size(20.0d, SizeUnit.mm).convertToPixels();
      double domeX = boardX + (boardW - domeD) / 2.0;
      double domeY = boardY + (boardH - domeD) / 2.0 - 10;

      g2d.setColor(DOME_COLOR);
      g2d.fill(new Ellipse2D.Double(domeX, domeY, domeD, domeD));
      g2d.setColor(Color.decode("#CCCCCC"));
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      g2d.draw(new Ellipse2D.Double(domeX, domeY, domeD, domeD));

      // Fresnel lens ring pattern
      for (double r = domeD - 20; r > 10; r -= 20) {
        g2d.draw(new Ellipse2D.Double(domeX + (domeD - r) / 2.0, domeY + (domeD - r) / 2.0, r, r));
      }

      // Two orange trimpots (Sensitivity & Delay)
      g2d.setColor(TRIMPOT_COLOR);
      g2d.fill(new RoundRectangle2D.Double(boardX + 15, boardY + boardH - 35, 25, 20, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(boardX + boardW - 40, boardY + boardH - 35, 25, 20, 3, 3));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "SENS", boardX + 27, boardY + boardH - 42, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "TIME", boardX + boardW - 28, boardY + boardH - 42, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(SENSOR_GREEN);
    g2d.fill(new RoundRectangle2D.Double(3, 4, width - 6, height - 8, 3, 3));
    g2d.setColor(SENSOR_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(3, 4, width - 6, height - 8, 3, 3));

    // White Dome
    g2d.setColor(DOME_COLOR);
    g2d.fillOval(width / 2 - 7, height / 2 - 8, 14, 14);
    g2d.setColor(Color.GRAY);
    g2d.drawOval(width / 2 - 7, height / 2 - 8, 14, 14);

    // 3 Pins
    g2d.setColor(PIN_COLOR);
    g2d.fillRect(width / 2 - 4, height - 6, 2, 2);
    g2d.fillRect(width / 2 - 1, height - 6, 2, 2);
    g2d.fillRect(width / 2 + 2, height - 6, 2, 2);
  }
}
