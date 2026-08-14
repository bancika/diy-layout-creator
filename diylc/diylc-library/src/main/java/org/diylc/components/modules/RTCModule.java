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
package org.diylc.components.modules;

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

@ComponentDescriptor(name = "Real-Time Clock (DS3231)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "DS3231 High-Precision I2C Real-Time Clock Module with Battery Backup",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class RTCModule extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color RTC_BLUE = Color.decode("#0055A5");
  public static Size BOARD_WIDTH = new Size(38.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(22.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "32K", "SQW", "SCL", "SDA", "VCC", "GND"
  };

  public RTCModule() {
    super();
    this.bodyColor = RTC_BLUE;
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

    double[][] relativeOffsets = new double[6][2];
    for (int i = 0; i < 6; i++) {
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
    double boardX = x - 12;
    double boardY = y - (boardH - 5 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - 12;
    double boardY = y - (boardH - 5 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // 2 Mounting holes
      drawMountingHole(g2d, boardX + boardW - 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 16, boardY + boardH - 16, 16);

      // Round CR2032 Coin Cell Battery holder
      double battD = boardH - 30;
      double battX = boardX + boardW - battD - 35;
      double battY = boardY + 15;

      g2d.setColor(Color.decode("#E0E0E0"));
      g2d.fill(new Ellipse2D.Double(battX, battY, battD, battD));
      g2d.setColor(Color.decode("#9E9E9E"));
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      g2d.draw(new Ellipse2D.Double(battX, battY, battD, battD));

      g2d.setColor(Color.decode("#757575"));
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "CR2032", battX + battD / 2.0, battY + battD / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // DS3231 SOIC-16 IC
      drawChip(g2d, boardX + 35, boardY + 25, 65, 50, "DS3231");

      // AT24C32 EEPROM
      drawChip(g2d, boardX + 35, boardY + 90, 50, 35, "24C32");

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "RTC I2C", boardX + 60, boardY + boardH - 16, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(RTC_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(RTC_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));

    // Coin cell
    g2d.setColor(Color.decode("#E0E0E0"));
    g2d.fillOval(width - 14, 8, 10, 10);

    // IC
    g2d.setColor(IC_BODY_COLOR);
    g2d.fillRect(8, 8, 8, 8);

    // Pins on left
    g2d.setColor(PIN_COLOR);
    for (int y = 7; y < height - 7; y += 3) {
      g2d.fillRect(3, y, 2, 2);
    }
  }
}
