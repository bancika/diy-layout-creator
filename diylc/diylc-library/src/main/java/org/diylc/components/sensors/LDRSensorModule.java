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

@ComponentDescriptor(name = "LDR / Photoresistor Module", category = "Sensors",
    author = "Branislav Stojkovic", description = "Light Dependent Resistor / Photoresistor Sensor Module with Digital/Analog Output",
    instanceNamePrefix = "SEN", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class LDRSensorModule extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color MODULE_BLUE = Color.decode("#006699");
  public static Size BOARD_WIDTH = new Size(14.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(32.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {"VCC", "GND", "DO", "AO"};

  public LDRSensorModule() {
    super();
    this.bodyColor = MODULE_BLUE;
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

    double[][] relativeOffsets = new double[4][2];
    for (int i = 0; i < 4; i++) {
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
    double boardX = x - (boardW - 3 * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - boardH + 12;
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
    double boardX = x - (boardW - 3 * PIN_SPACING.convertToPixels()) / 2.0;
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
      // Photoresistor (LDR) bulb at top
      double ldrX = boardX + boardW / 2.0;
      double ldrY = boardY - 6;
      g2d.setColor(Color.decode("#CC6600"));
      g2d.fill(new Ellipse2D.Double(ldrX - 12, ldrY - 12, 24, 24));
      g2d.setColor(Color.decode("#993300"));
      g2d.draw(new Ellipse2D.Double(ldrX - 12, ldrY - 12, 24, 24));
      // Zig-zag trace on LDR
      g2d.setColor(Color.decode("#FF9900"));
      g2d.drawLine((int) ldrX - 6, (int) ldrY - 4, (int) ldrX + 6, (int) ldrY - 4);
      g2d.drawLine((int) ldrX - 6, (int) ldrY, (int) ldrX + 6, (int) ldrY);
      g2d.drawLine((int) ldrX - 6, (int) ldrY + 4, (int) ldrX + 6, (int) ldrY + 4);

      // LM393 comparator chip
      drawChip(g2d, boardX + 15, boardY + 40, boardW - 30, 45, "LM393");

      // Blue sensitivity trimpot
      g2d.setColor(Color.decode("#0088CC"));
      g2d.fill(new RoundRectangle2D.Double(boardX + 15, boardY + 100, boardW - 30, 35, 3, 3));
      g2d.setColor(Color.decode("#DCDCDC"));
      g2d.fill(new Ellipse2D.Double(boardX + boardW / 2.0 - 6, boardY + 112, 12, 12));

      // Silkscreen
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "LDR", boardX + boardW / 2.0, boardY + boardH - 22, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(MODULE_BLUE);
    g2d.fill(new RoundRectangle2D.Double(8, 4, width - 16, height - 8, 3, 3));
    g2d.setColor(MODULE_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(8, 4, width - 16, height - 8, 3, 3));

    // LDR sensor top
    g2d.setColor(Color.decode("#CC6600"));
    g2d.fillOval(width / 2 - 4, 3, 8, 8);

    // Trimpot
    g2d.setColor(Color.decode("#0088CC"));
    g2d.fillRect(10, 14, width - 20, 6);

    // Pins
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      g2d.fillRect(10 + i * 3, height - 5, 2, 2);
    }
  }
}
