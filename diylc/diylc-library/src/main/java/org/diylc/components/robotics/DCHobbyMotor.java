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
package org.diylc.components.robotics;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
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

@ComponentDescriptor(name = "DC Motor (130-Size / 3V-6V)", category = "Robotics",
    author = "Branislav Stojkovic", description = "Standard 130-Size 3V-6V Toy and Hobby DC Motor with Solder Terminals",
    instanceNamePrefix = "MTR", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class DCHobbyMotor extends AbstractMotor {

  private static final long serialVersionUID = 1L;

  public static Color CAN_METAL = Color.decode("#B0BEC5");
  public static Color END_CAP = Color.decode("#E65100");
  public static Size MOTOR_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size MOTOR_LENGTH = new Size(25.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "Positive (+)", "Negative (-)"
  };

  public DCHobbyMotor() {
    super();
    this.bodyColor = CAN_METAL;
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
    double spacing = new Size(0.2d, SizeUnit.in).convertToPixels(); // 40px

    double[][] relativeOffsets = new double[2][2];
    relativeOffsets[0] = new double[] {0, 0};
    relativeOffsets[1] = new double[] {spacing, 0};

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double motorW = MOTOR_WIDTH.convertToPixels();
    double motorL = MOTOR_LENGTH.convertToPixels();
    double motorX = x - (motorW - 40) / 2.0;
    double motorY = y - motorL - 15;
    return new RoundRectangle2D.Double(motorX, motorY - 35, motorW, motorL + 35, 16, 16);
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

    double motorW = MOTOR_WIDTH.convertToPixels();
    double motorL = MOTOR_LENGTH.convertToPixels();
    double motorX = x - (motorW - 40) / 2.0;
    double motorY = y - motorL - 15;

    Shape canShape = new RoundRectangle2D.Double(motorX, motorY, motorW, motorL, 16, 16);

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(canShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(canShape);

    if (!outlineMode) {
      // Front 2mm steel shaft
      double shaftW = 12;
      double shaftH = 35;
      double shaftX = motorX + (motorW - shaftW) / 2.0;
      double shaftY = motorY - shaftH;

      g2d.setColor(SHAFT_COLOR);
      g2d.fill(new RoundRectangle2D.Double(shaftX, shaftY, shaftW, shaftH, 4, 4));
      g2d.setColor(SHAFT_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(shaftX, shaftY, shaftW, shaftH, 4, 4));

      // Rear plastic end cap
      double capH = 20;
      double capY = motorY + motorL - capH;
      g2d.setColor(END_CAP);
      g2d.fill(new RoundRectangle2D.Double(motorX + 4, capY, motorW - 8, capH, 4, 4));

      // Solder lug terminals (+ and -)
      g2d.setColor(COPPER_COLOR);
      g2d.fill(new RoundRectangle2D.Double(x - 5, y - 12, 10, 16, 2, 2));
      g2d.fill(new RoundRectangle2D.Double(x + 35, y - 12, 10, 16, 2, 2));

      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "+", x, y - 16, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "-", x + 40, y - 16, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "130 DC", motorX + motorW / 2.0, motorY + motorL / 2.0 - 5, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(CAN_METAL);
    g2d.fill(new RoundRectangle2D.Double(width / 2 - 7, 8, 14, height - 12, 6, 6));
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(width / 2 - 7, 8, 14, height - 12, 6, 6));

    // Shaft
    g2d.setColor(SHAFT_COLOR);
    g2d.fillRect(width / 2 - 2, 2, 4, 6);

    // End cap
    g2d.setColor(END_CAP);
    g2d.fillRect(width / 2 - 5, height - 8, 10, 4);
  }
}
