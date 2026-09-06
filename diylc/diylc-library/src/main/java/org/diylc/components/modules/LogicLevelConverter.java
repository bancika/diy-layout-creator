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

@ComponentDescriptor(name = "Logic Level Converter (3.3V-5V)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "4-Channel Bi-Directional Logic Level Shifter Module",
    instanceNamePrefix = "MOD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class LogicLevelConverter extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color SHIFTER_RED = Color.decode("#C0392B");
  public static Size BOARD_WIDTH = new Size(13.0d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(16.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Low Voltage Side (0..5)
      "LV1", "LV2", "LV (3.3V)", "GND_LV", "LV3", "LV4",
      // High Voltage Side (6..11)
      "HV1", "HV2", "HV (5V)", "GND_HV", "HV3", "HV4"
  };

  public LogicLevelConverter() {
    super();
    this.bodyColor = SHIFTER_RED;
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

    double[][] relativeOffsets = new double[12][2];
    for (int i = 0; i < 6; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    for (int i = 0; i < 6; i++) {
      relativeOffsets[6 + i][0] = rowSpacing;
      relativeOffsets[6 + i][1] = i * spacing;
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
    double boardH = 7 * PIN_SPACING.convertToPixels();
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
    double boardH = 7 * PIN_SPACING.convertToPixels();
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
      // 4 tiny SOT-23 MOSFETs
      for (int i = 0; i < 4; i++) {
        double my = boardY + 25 + i * 25;
        g2d.setColor(IC_BODY_COLOR);
        g2d.fill(new RoundRectangle2D.Double(boardX + boardW / 2.0 - 10, my, 20, 14, 2, 2));
      }

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "LV", x + 15, y - 2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "HV", x + rowSpacing - 15, y - 2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(SHIFTER_RED);
    g2d.fill(new RoundRectangle2D.Double(6, 4, width - 12, height - 8, 3, 3));
    g2d.setColor(SHIFTER_RED.darker());
    g2d.draw(new RoundRectangle2D.Double(6, 4, width - 12, height - 8, 3, 3));

    // Pins on left & right
    g2d.setColor(PIN_COLOR);
    for (int y = 7; y < height - 7; y += 4) {
      g2d.fillRect(7, y, 2, 2);
      g2d.fillRect(width - 9, y, 2, 2);
    }

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "LVL", width / 2, height / 2 + 2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
