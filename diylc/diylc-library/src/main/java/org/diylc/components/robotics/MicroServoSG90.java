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

@ComponentDescriptor(name = "Micro Servo (SG90 / 9g)", category = "Robotics",
    author = "Branislav Stojkovic", description = "SG90 9g Micro Servo Motor with 3-Pin Lead Connector",
    instanceNamePrefix = "MTR", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class MicroServoSG90 extends AbstractMotor {

  private static final long serialVersionUID = 1L;

  public static Color SERVO_BLUE = Color.decode("#0077CC");
  public static Color HORN_WHITE = Color.decode("#FAFAFA");

  public static Size BODY_WIDTH = new Size(12.2d, SizeUnit.mm);
  public static Size BODY_LENGTH = new Size(22.8d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "GND (Brown/Black)", "VCC 5V (Red)", "PWM Signal (Orange/Yellow)"
  };

  public MicroServoSG90() {
    super();
    this.bodyColor = SERVO_BLUE;
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
    double bodyW = BODY_LENGTH.convertToPixels();
    double bodyH = BODY_WIDTH.convertToPixels();
    double bodyX = x - (bodyW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double bodyY = y - bodyH - 45;
    double flangeW = bodyW + 40;
    double flangeX = bodyX - 20;
    return new RoundRectangle2D.Double(flangeX, bodyY, flangeW, bodyH, 6, 6);
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

    double bodyW = BODY_LENGTH.convertToPixels();
    double bodyH = BODY_WIDTH.convertToPixels();
    double bodyX = x - (bodyW - 2 * PIN_SPACING.convertToPixels()) / 2.0;
    double bodyY = y - bodyH - 45;

    // Body with mounting flanges
    double flangeW = bodyW + 40;
    double flangeX = bodyX - 20;

    Shape bodyShape = new RoundRectangle2D.Double(bodyX, bodyY, bodyW, bodyH, 6, 6);
    Shape flangeShape = new RoundRectangle2D.Double(flangeX, bodyY + 10, flangeW, bodyH - 20, 4, 4);

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(flangeShape);
    g2d.fill(bodyShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(flangeShape);
    g2d.draw(bodyShape);

    if (!outlineMode) {
      // Mounting holes on flanges
      g2d.setColor(Color.WHITE);
      g2d.fill(new Ellipse2D.Double(flangeX + 8, bodyY + bodyH / 2.0 - 4, 8, 8));
      g2d.fill(new Ellipse2D.Double(flangeX + flangeW - 16, bodyY + bodyH / 2.0 - 4, 8, 8));

      // White Output Spline / Servo Horn
      double splineX = bodyX + 25;
      double splineY = bodyY + bodyH / 2.0;
      g2d.setColor(HORN_WHITE);
      g2d.fill(new Ellipse2D.Double(splineX - 18, splineY - 18, 36, 36));
      g2d.fill(new RoundRectangle2D.Double(splineX - 10, splineY - 35, 20, 70, 8, 8));
      g2d.setColor(Color.decode("#BDBDBD"));
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      g2d.draw(new Ellipse2D.Double(splineX - 18, splineY - 18, 36, 36));
      g2d.draw(new RoundRectangle2D.Double(splineX - 10, splineY - 35, 20, 70, 8, 8));

      // Center screw
      g2d.setColor(Color.decode("#757575"));
      g2d.fill(new Ellipse2D.Double(splineX - 5, splineY - 5, 10, 10));

      // 3-wire colored cable from body to header connector
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f));
      // Brown (GND)
      g2d.setColor(Color.decode("#795548"));
      g2d.drawLine((int) (bodyX + bodyW - 10), (int) (bodyY + bodyH / 2.0 - 8), (int) x, (int) (y - 8));
      // Red (VCC)
      g2d.setColor(Color.decode("#E53935"));
      g2d.drawLine((int) (bodyX + bodyW - 10), (int) (bodyY + bodyH / 2.0), (int) (x + PIN_SPACING.convertToPixels()), (int) (y - 8));
      // Orange (PWM)
      g2d.setColor(Color.decode("#FF9800"));
      g2d.drawLine((int) (bodyX + bodyW - 10), (int) (bodyY + bodyH / 2.0 + 8), (int) (x + 2 * PIN_SPACING.convertToPixels()), (int) (y - 8));
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, true, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(SERVO_BLUE);
    g2d.fill(new RoundRectangle2D.Double(6, 8, width - 12, height - 16, 3, 3));
    g2d.setColor(SERVO_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(6, 8, width - 12, height - 16, 3, 3));

    // White horn
    g2d.setColor(HORN_WHITE);
    g2d.fillOval(10, 10, 10, 10);
    g2d.fillRect(13, 6, 4, 18);
    g2d.setColor(Color.GRAY);
    g2d.drawOval(10, 10, 10, 10);
  }
}
