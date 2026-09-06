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

@ComponentDescriptor(name = "NEMA 17 Stepper Motor", category = "Robotics",
    author = "Branislav Stojkovic", description = "NEMA 17 42mm Hybrid Bipolar Stepper Motor with 4-Pin Connector",
    instanceNamePrefix = "MTR", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class NEMA17Stepper extends AbstractMotor {

  private static final long serialVersionUID = 1L;

  public static Color MOTOR_FACE = Color.decode("#424242");
  public static Color BOSS_COLOR = Color.decode("#B0BEC5");
  public static Size FACE_SIZE = new Size(42.3d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "1A (Black / A+)", "1B (Green / A-)", "2A (Red / B+)", "2B (Blue / B-)"
  };

  public NEMA17Stepper() {
    super();
    this.bodyColor = MOTOR_FACE;
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
    double faceSizePx = FACE_SIZE.convertToPixels();
    double faceX = x - (faceSizePx - 3 * PIN_SPACING.convertToPixels()) / 2.0;
    double faceY = y - faceSizePx - 40;
    return new RoundRectangle2D.Double(faceX, faceY, faceSizePx, faceSizePx, 10, 10);
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

    double faceSizePx = FACE_SIZE.convertToPixels();
    double faceX = x - (faceSizePx - 3 * PIN_SPACING.convertToPixels()) / 2.0;
    double faceY = y - faceSizePx - 40;

    Shape faceShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(faceShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(faceShape);

    if (!outlineMode) {
      // 4 M3 mounting holes (31mm pitch ~ 244px)
      double holeOffset = new Size((42.3 - 31.0) / 2.0, SizeUnit.mm).convertToPixels();
      double pitchPx = new Size(31.0d, SizeUnit.mm).convertToPixels();
      g2d.setColor(Color.decode("#9E9E9E"));
      g2d.fill(new Ellipse2D.Double(faceX + holeOffset - 8, faceY + holeOffset - 8, 16, 16));
      g2d.fill(new Ellipse2D.Double(faceX + holeOffset + pitchPx - 8, faceY + holeOffset - 8, 16, 16));
      g2d.fill(new Ellipse2D.Double(faceX + holeOffset - 8, faceY + holeOffset + pitchPx - 8, 16, 16));
      g2d.fill(new Ellipse2D.Double(faceX + holeOffset + pitchPx - 8, faceY + holeOffset + pitchPx - 8, 16, 16));

      // Central 22mm round boss
      double bossD = new Size(22.0d, SizeUnit.mm).convertToPixels();
      double bossX = faceX + (faceSizePx - bossD) / 2.0;
      double bossY = faceY + (faceSizePx - bossD) / 2.0;

      g2d.setColor(BOSS_COLOR);
      g2d.fill(new Ellipse2D.Double(bossX, bossY, bossD, bossD));
      g2d.setColor(Color.decode("#78909C"));
      g2d.draw(new Ellipse2D.Double(bossX, bossY, bossD, bossD));

      // 5mm D-Shaft in center
      double shaftD = new Size(5.0d, SizeUnit.mm).convertToPixels();
      double shaftX = faceX + (faceSizePx - shaftD) / 2.0;
      double shaftY = faceY + (faceSizePx - shaftD) / 2.0;

      g2d.setColor(SHAFT_COLOR);
      g2d.fill(new Ellipse2D.Double(shaftX, shaftY, shaftD, shaftD));
      g2d.setColor(SHAFT_BORDER_COLOR);
      g2d.draw(new Ellipse2D.Double(shaftX, shaftY, shaftD, shaftD));

      // 4-wire lead cable to connector (Black, Green, Red, Blue)
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f));
      Color[] wireColors = new Color[] {
          Color.BLACK, Color.decode("#2E7D32"), Color.decode("#C62828"), Color.decode("#1565C0")
      };
      for (int i = 0; i < 4; i++) {
        g2d.setColor(wireColors[i]);
        g2d.drawLine((int) (faceX + faceSizePx / 2.0 - 15 + i * 10), (int) (faceY + faceSizePx),
            (int) (x + i * PIN_SPACING.convertToPixels()), (int) (y - 8));
      }
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(MOTOR_FACE);
    g2d.fill(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 3, 3));
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 3, 3));

    // Boss & Shaft
    g2d.setColor(BOSS_COLOR);
    g2d.fillOval(width / 2 - 6, height / 2 - 6, 12, 12);
    g2d.setColor(SHAFT_COLOR);
    g2d.fillOval(width / 2 - 3, height / 2 - 3, 6, 6);
  }
}
