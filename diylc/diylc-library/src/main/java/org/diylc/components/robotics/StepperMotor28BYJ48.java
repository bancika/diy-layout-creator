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

@ComponentDescriptor(name = "Stepper Motor (28BYJ-48)", category = "Robotics",
    author = "Branislav Stojkovic", description = "28BYJ-48 5V Geared Stepper Motor with 5-Pin Connector",
    instanceNamePrefix = "MTR", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class StepperMotor28BYJ48 extends AbstractMotor {

  private static final long serialVersionUID = 1L;

  public static Color MOTOR_BLUE = Color.decode("#1E88E5");
  public static Size BODY_DIAMETER = new Size(28.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "IN1 (Blue)", "IN2 (Pink)", "IN3 (Yellow)", "IN4 (Orange)", "VCC 5V (Red)"
  };

  public StepperMotor28BYJ48() {
    super();
    this.bodyColor = MOTOR_BLUE;
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

    double[][] relativeOffsets = new double[5][2];
    for (int i = 0; i < 5; i++) {
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
    double bodyD = BODY_DIAMETER.convertToPixels();
    double bodyX = x - (bodyD - 4 * PIN_SPACING.convertToPixels()) / 2.0;
    double bodyY = y - bodyD - 40;
    double tabW = bodyD + 60;
    double tabX = bodyX - 30;
    return new RoundRectangle2D.Double(tabX, bodyY, tabW, bodyD, 8, 8);
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

    double bodyD = BODY_DIAMETER.convertToPixels();
    double bodyX = x - (bodyD - 4 * PIN_SPACING.convertToPixels()) / 2.0;
    double bodyY = y - bodyD - 40;

    // Mounting tabs
    double tabW = bodyD + 60;
    double tabX = bodyX - 30;

    Shape tabShape = getBodyShape();
    Shape bodyShape = new Ellipse2D.Double(bodyX, bodyY, bodyD, bodyD);

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(tabShape);
    g2d.fill(bodyShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(tabShape);
    g2d.draw(bodyShape);

    if (!outlineMode) {
      // Mounting holes on side tabs
      g2d.setColor(Color.WHITE);
      g2d.fill(new Ellipse2D.Double(tabX + 8, bodyY + bodyD / 2.0 - 6, 12, 12));
      g2d.fill(new Ellipse2D.Double(tabX + tabW - 20, bodyY + bodyD / 2.0 - 6, 12, 12));

      // Metal gearbox casing with flat D-shaft (offset to top)
      double gbW = 70;
      double gbH = 60;
      double gbX = bodyX + (bodyD - gbW) / 2.0;
      double gbY = bodyY + 15;

      g2d.setColor(Color.decode("#B0BEC5"));
      g2d.fill(new RoundRectangle2D.Double(gbX, gbY, gbW, gbH, 6, 6));

      // Brass D-Shaft
      double shaftD = 30;
      double shaftX = gbX + (gbW - shaftD) / 2.0;
      double shaftY = gbY + (gbH - shaftD) / 2.0;

      g2d.setColor(BRASS_COLOR);
      g2d.fill(new Ellipse2D.Double(shaftX, shaftY, shaftD, shaftD));
      g2d.setColor(BRASS_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(shaftX, shaftY, shaftD, shaftD));

      // Flat side of D-shaft
      g2d.setColor(Color.decode("#8D6E63"));
      g2d.drawLine((int) shaftX + 4, (int) shaftY + 6, (int) shaftX + 4, (int) (shaftY + shaftD - 6));

      // 5-wire ribbon cable (Blue, Pink, Yellow, Orange, Red)
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f));
      Color[] wireColors = new Color[] {
          Color.decode("#1E88E5"), Color.decode("#E91E63"), Color.decode("#FDD835"), Color.decode("#FF9800"), Color.decode("#E53935")
      };
      for (int i = 0; i < 5; i++) {
        g2d.setColor(wireColors[i]);
        g2d.drawLine((int) (bodyX + bodyD / 2.0 - 20 + i * 10), (int) (bodyY + bodyD),
            (int) (x + i * PIN_SPACING.convertToPixels()), (int) (y - 8));
      }
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(MOTOR_BLUE);
    g2d.fillOval(5, 5, width - 10, height - 10);
    g2d.setColor(MOTOR_BLUE.darker());
    g2d.drawOval(5, 5, width - 10, height - 10);

    // Brass shaft
    g2d.setColor(BRASS_COLOR);
    g2d.fillOval(width / 2 - 4, height / 2 - 4, 8, 8);
  }
}
