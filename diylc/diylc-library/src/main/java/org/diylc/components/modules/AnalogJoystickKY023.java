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
package org.diylc.components.modules;

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

@ComponentDescriptor(name = "Analog Joystick (KY-023)", category = "Modules & Breakouts",
    author = "Branislav Stojkovic", description = "KY-023 Dual-Axis PS2 Thumb Joystick Module with Push Switch",
    instanceNamePrefix = "JOY", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class AnalogJoystickKY023 extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color JOY_PCB = Color.decode("#1B2631");
  public static Color POT_GREEN = Color.decode("#27AE60");
  public static Color THUMB_CAP = Color.decode("#17202A");

  public static Size BOARD_WIDTH = new Size(34.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(26.0d, SizeUnit.mm);

  public AnalogJoystickKY023() {
    super();
    this.bodyColor = JOY_PCB;
    updateControlPoints();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    // 5-pin 0.1" header: GND, +5V, VRx, VRy, SW
    double[][] relativeOffsets = new double[5][2];
    for (int i = 0; i < 5; i++) {
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
    double boardY = y - (boardH - 4 * PIN_SPACING.convertToPixels()) / 2.0;
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
    double boardX = x - boardW + 20;
    double boardY = y - (boardH - 4 * PIN_SPACING.convertToPixels()) / 2.0;

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
      // 4 Mounting Holes in corners
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardH - 16, 16);
      drawMountingHole(g2d, boardX + boardW - 40, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardW - 40, boardY + boardH - 16, 16);

      // Metal Gimbal Frame (Center)
      double cx = boardX + (boardW - 30) / 2.0;
      double cy = boardY + boardH / 2.0;
      g2d.setColor(Color.decode("#7F8C8D"));
      g2d.fill(new RoundRectangle2D.Double(cx - 50, cy - 50, 100, 100, 8, 8));

      // Potentiometer cases (X and Y side blocks)
      g2d.setColor(POT_GREEN);
      g2d.fill(new RoundRectangle2D.Double(cx - 56, cy - 30, 12, 60, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(cx - 30, cy + 44, 60, 12, 3, 3));

      // Circular Rubber Thumbstick Cap
      g2d.setColor(THUMB_CAP);
      g2d.fill(new Ellipse2D.Double(cx - 42, cy - 42, 84, 84));
      g2d.setColor(Color.decode("#2C3E50"));
      g2d.fill(new Ellipse2D.Double(cx - 30, cy - 30, 60, 60));
      g2d.setColor(Color.decode("#111111"));
      g2d.fill(new Ellipse2D.Double(cx - 20, cy - 20, 40, 40));

      // Silk Screen Pin Labels
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      String[] labels = new String[] { "GND", "+5V", "VRx", "VRy", "SW" };
      double spacing = PIN_SPACING.convertToPixels();
      for (int i = 0; i < 5; i++) {
        StringUtils.drawCenteredText(g2d, labels[i], boardX + boardW - 32, y + i * spacing, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw 5 header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(JOY_PCB);
    g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 3, 3));
    g2d.setColor(THUMB_CAP);
    g2d.fill(new Ellipse2D.Double(width / 2.0 - 10, height / 2.0 - 10, 20, 20));
    g2d.setColor(Color.BLACK);
    g2d.fill(new Ellipse2D.Double(width / 2.0 - 5, height / 2.0 - 5, 10, 10));
  }
}
