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

@ComponentDescriptor(name = "Stepper Driver (A4988 / DRV8825)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "A4988 / DRV8825 Microstepping Stepper Motor Driver Carrier",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class A4988StepperDriver extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color A4988_GREEN = Color.decode("#2E7D32");
  public static Size BOARD_WIDTH = new Size(15.2d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(20.3d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (pins 0..7)
      "ENABLE", "MS1", "MS2", "MS3", "RESET", "SLEEP", "STEP", "DIR",
      // Right row (pins 8..15)
      "VMOT", "GND_PWR", "2B", "2A", "1A", "1B", "VDD", "GND_LOGIC"
  };

  public A4988StepperDriver() {
    super();
    this.bodyColor = A4988_GREEN;
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
    double rowSpacing = new Size(0.5d, SizeUnit.in).convertToPixels(); // 100px

    double[][] relativeOffsets = new double[16][2];
    for (int i = 0; i < 8; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    for (int i = 0; i < 8; i++) {
      relativeOffsets[8 + i][0] = rowSpacing;
      relativeOffsets[8 + i][1] = (7 - i) * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double rowSpacing = new Size(0.5d, SizeUnit.in).convertToPixels();
    double boardW = rowSpacing + 20;
    double boardH = 9 * PIN_SPACING.convertToPixels();
    double boardX = x - 10;
    double boardY = y - 10;
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

    double rowSpacing = new Size(0.5d, SizeUnit.in).convertToPixels();
    double boardW = rowSpacing + 20;
    double boardH = 9 * PIN_SPACING.convertToPixels();
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
      // Aluminum heatsink on top of chip in center
      double hsSize = 45;
      double hsX = boardX + (boardW - hsSize) / 2.0;
      double hsY = boardY + 35;

      g2d.setColor(Color.decode("#B0BEC5"));
      g2d.fill(new RoundRectangle2D.Double(hsX, hsY, hsSize, hsSize, 3, 3));
      g2d.setColor(Color.decode("#78909C"));
      g2d.draw(new RoundRectangle2D.Double(hsX, hsY, hsSize, hsSize, 3, 3));
      // Heatsink fins
      g2d.drawLine((int) hsX + 15, (int) hsY + 4, (int) hsX + 15, (int) (hsY + hsSize - 4));
      g2d.drawLine((int) hsX + 30, (int) hsY + 4, (int) hsX + 30, (int) (hsY + hsSize - 4));

      // Tiny metal trimpot at bottom
      g2d.setColor(Color.decode("#CFD8DC"));
      g2d.fill(new RoundRectangle2D.Double(boardX + (boardW - 25) / 2.0, boardY + boardH - 35, 25, 20, 2, 2));

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "A4988", boardX + boardW / 2.0, boardY + 20, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(A4988_GREEN);
    g2d.fill(new RoundRectangle2D.Double(5, 3, width - 10, height - 6, 3, 3));
    g2d.setColor(A4988_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(5, 3, width - 10, height - 6, 3, 3));

    // Heatsink
    g2d.setColor(Color.decode("#B0BEC5"));
    g2d.fillRect(10, 8, width - 20, 10);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "STEP", width / 2, height / 2 + 7, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
