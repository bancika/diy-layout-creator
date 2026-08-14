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

@ComponentDescriptor(name = "ESP8266 NodeMCU", category = "Controllers",
    author = "Branislav Stojkovic", description = "ESP8266 NodeMCU V2/V3 Wi-Fi Development Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ESP8266NodeMCU extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NODEMCU_BLACK = Color.decode("#1C1C1C");
  public static Size BOARD_WIDTH = new Size(25.4d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(48.3d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (pins 0..14)
      "A0 (ADC0)", "RSV1", "RSV2", "SD3", "SD2", "SD1", "CMD", "SD0", "CLK", "GND1", "3V3_1", "EN", "RST", "GND2", "VIN",
      // Right row (pins 15..29)
      "D0 (GPIO16)", "D1 (GPIO5)", "D2 (GPIO4)", "D3 (GPIO0)", "D4 (GPIO2)", "3V3_2", "GND3", "D5 (GPIO14)", "D6 (GPIO12)", "D7 (GPIO13)", "D8 (GPIO15)", "RX (GPIO3)", "TX (GPIO1)", "GND4", "3V3_3"
  };

  public ESP8266NodeMCU() {
    super();
    this.bodyColor = NODEMCU_BLACK;
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
    double rowSpacing = new Size(0.9d, SizeUnit.in).convertToPixels(); // 180px

    double[][] relativeOffsets = new double[30][2];
    for (int i = 0; i < 15; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    for (int i = 0; i < 15; i++) {
      relativeOffsets[15 + i][0] = rowSpacing;
      relativeOffsets[15 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double rowSpacing = new Size(0.9d, SizeUnit.in).convertToPixels();
    double boardW = rowSpacing + 26;
    double boardH = 16 * PIN_SPACING.convertToPixels();
    double boardX = x - 13;
    double boardY = y - 10;
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

    double rowSpacing = new Size(0.9d, SizeUnit.in).convertToPixels();
    double boardW = rowSpacing + 26;
    double boardH = 16 * PIN_SPACING.convertToPixels();
    double boardX = x - 13;
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
      // Micro-USB Jack at bottom
      drawMetalConnector(g2d, boardX + (boardW - 60) / 2.0, boardY + boardH - 20, 60, 30, "USB");

      // ESP-12F Metal shield with antenna at top
      drawMetalConnector(g2d, boardX + 20, boardY + 40, boardW - 40, 110, "ESP-12F");

      // Copper trace antenna
      g2d.setColor(Color.decode("#DAA520"));
      g2d.fillRect((int) (boardX + 25), (int) (boardY + 10), (int) (boardW - 50), 20);

      // Buttons
      g2d.setColor(Color.decode("#555555"));
      g2d.fill(new RoundRectangle2D.Double(boardX + 15, boardY + boardH - 45, 25, 18, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(boardX + boardW - 40, boardY + boardH - 45, 25, 18, 3, 3));
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RST", boardX + 27, boardY + boardH - 36, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "FLASH", boardX + boardW - 28, boardY + boardH - 36, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "NodeMCU", boardX + boardW / 2.0, boardY + 180, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(NODEMCU_BLACK);
    g2d.fill(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(5, 2, width - 10, height - 4, 3, 3));

    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(8, 6, width - 16, 12);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "8266", width / 2, height / 2 + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
