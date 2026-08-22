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
package org.diylc.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * Base abstract class for Maker-focused boards, microcontrollers, sensors, displays, and breakout modules.
 */
public abstract class AbstractMakerBoard extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color HEADER_BODY_COLOR = Color.decode("#222222");
  public static Color HEADER_BORDER_COLOR = Color.decode("#111111");
  public static Color MOUNTING_HOLE_COLOR = Color.decode("#B8860B");
  public static Color MOUNTING_HOLE_BORDER = Color.decode("#8B6508");
  public static Color IC_BODY_COLOR = Color.decode("#1A1A1A");
  public static Color IC_TEXT_COLOR = Color.decode("#CCCCCC");
  public static Color SCREW_TERMINAL_COLOR = Color.decode("#90AB66");
  public static Color SCREW_TERMINAL_BORDER = Color.decode("#90AB66").darker();
  public static Color SCREW_CIRCLE_COLOR = LIGHT_METAL_COLOR;
  public static Color METAL_SHIELD_COLOR = Color.decode("#C0C0C0");
  public static Color METAL_SHIELD_BORDER = Color.decode("#909090");
  public static Color USB_METAL_COLOR = Color.decode("#D3D3D3");
  public static Color IC_BORDER_COLOR = Color.decode("#333333");
  public static Color PIN_MARKER_COLOR = Color.decode("#555555");
  public static Color METAL_LABEL_COLOR = Color.decode("#555555");

  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);

  public static Font SILK_FONT_SMALL = new Font("SansSerif", Font.PLAIN, 10);
  public static Font SILK_FONT = new Font("SansSerif", Font.BOLD, 11);
  public static Font SILK_FONT_LARGE = new Font("SansSerif", Font.BOLD, 13);

  protected Orientation orientation = Orientation.DEFAULT;
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Color bodyColor = Color.decode("#006699");

  public AbstractMakerBoard() {
    super();
  }

  @EditableProperty(name = "Orientation")
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Board Color")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
    invalidateCache();
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
//    if (index == 0) {
//      updateControlPoints();
//    }
    invalidateCache();
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return false;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  protected abstract void updateControlPoints();

  protected void invalidateCache() {}

  /**
   * Helper to rotate an array of relative point offsets around the origin (first point) by orientation.
   */
  protected void rotatePoints(Point2D firstPoint, double[][] relativeOffsets) {
    controlPoints = new Point2D[relativeOffsets.length];
    controlPoints[0] = firstPoint;
    double theta = orientation.toRadians();
    AffineTransform tx = AffineTransform.getRotateInstance(theta, firstPoint.getX(), firstPoint.getY());
    for (int i = 1; i < relativeOffsets.length; i++) {
      Point2D p = new Point2D.Double(firstPoint.getX() + relativeOffsets[i][0], firstPoint.getY() + relativeOffsets[i][1]);
      tx.transform(p, p);
      controlPoints[i] = p;
    }
  }

  /**
   * Helper to draw standard pin header pins at given control point indices.
   */
  protected void drawPins(Graphics2D g2d, int startIndex, int count, boolean female, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    int pinPx = (int) Math.round(PIN_SIZE.convertToPixels());
    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      g2d.setColor(HEADER_BODY_COLOR);
      g2d.fill(new Rectangle2D.Double(p.getX() - pinPx - 1, p.getY() - pinPx - 1, (pinPx + 1) * 2, (pinPx + 1) * 2));
      g2d.setColor(PIN_COLOR);
      g2d.fill(new Rectangle2D.Double(p.getX() - pinPx / 2.0, p.getY() - pinPx / 2.0, pinPx, pinPx));
    }
    drawingObserver.stopTrackingContinuityArea();

    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      g2d.setColor(PIN_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new Rectangle2D.Double(p.getX() - pinPx / 2.0, p.getY() - pinPx / 2.0, pinPx, pinPx));
    }
  }

  /**
   * Helper to draw the official Arduino infinity logo (white infinity symbol with '-' and '+').
   * Default bounding box size is 95.56 x 45.33 px.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the logo bounding box
   * @param y Top-left Y coordinate of the logo bounding box
   */
  protected void drawArduinoLogo(Graphics2D g2d, double x, double y) {
    drawArduinoLogo(g2d, x, y, 1.0);
  }

  /**
   * Helper to draw the official Arduino infinity logo with custom scaling.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the logo bounding box
   * @param y Top-left Y coordinate of the logo bounding box
   * @param scale Scale factor (1.0 = 95.56 x 45.33 px)
   */
  protected void drawArduinoLogo(Graphics2D g2d, double x, double y, double scale) {
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(x, y);
    if (scale != 1.0) {
      g2d.scale(scale, scale);
    }

    Path2D.Double infinityBody = new Path2D.Double(Path2D.WIND_EVEN_ODD);
    // Outer loop
    infinityBody.moveTo(47.56, 14.67);
    infinityBody.curveTo(50.00, 12.00, 51.78, 9.78, 54.00, 7.78);
    infinityBody.curveTo(62.22, 0.44, 71.56, -2.44, 82.22, 2.22);
    infinityBody.curveTo(92.22, 6.44, 97.78, 16.89, 95.56, 27.56);
    infinityBody.curveTo(93.56, 37.33, 84.00, 44.89, 73.78, 45.33);
    infinityBody.curveTo(64.22, 45.56, 56.89, 41.33, 50.89, 34.44);
    infinityBody.curveTo(50.00, 33.33, 49.11, 32.22, 48.00, 30.89);
    infinityBody.curveTo(47.11, 31.78, 46.44, 32.67, 46.00, 33.33);
    infinityBody.curveTo(39.56, 41.33, 31.56, 46.00, 20.89, 45.33);
    infinityBody.curveTo(10.44, 44.67, 0.67, 34.89, 0.00, 24.44);
    infinityBody.curveTo(-0.89, 11.11, 9.33, 0.22, 23.33, 0.00);
    infinityBody.curveTo(32.67, 0.00, 40.00, 4.67, 45.78, 12.00);
    infinityBody.curveTo(46.44, 12.89, 47.11, 13.56, 47.78, 14.67);
    infinityBody.closePath();

    // Left hole cutout
    infinityBody.moveTo(24.44, 7.56);
    infinityBody.curveTo(14.67, 7.56, 7.56, 13.78, 6.89, 21.78);
    infinityBody.curveTo(6.22, 28.89, 11.78, 36.00, 19.56, 37.78);
    infinityBody.curveTo(27.33, 39.33, 33.33, 36.22, 38.00, 30.67);
    infinityBody.curveTo(45.11, 22.22, 44.89, 23.56, 38.00, 15.11);
    infinityBody.curveTo(34.22, 10.44, 29.11, 7.78, 24.22, 7.56);
    infinityBody.closePath();

    // Right hole cutout
    infinityBody.moveTo(72.67, 7.33);
    infinityBody.curveTo(71.56, 7.33, 70.67, 7.33, 69.78, 7.33);
    infinityBody.curveTo(61.56, 8.89, 56.44, 14.44, 52.44, 21.56);
    infinityBody.curveTo(52.22, 22.00, 52.22, 23.11, 52.44, 23.56);
    infinityBody.curveTo(56.00, 29.56, 60.22, 34.89, 67.33, 37.11);
    infinityBody.curveTo(74.00, 39.33, 80.00, 37.56, 84.67, 32.22);
    infinityBody.curveTo(88.89, 27.56, 89.78, 22.00, 87.11, 16.44);
    infinityBody.curveTo(84.22, 10.44, 78.89, 7.78, 72.67, 7.33);
    infinityBody.lineTo(72.67, 7.33);
    infinityBody.closePath();

    g2d.setColor(Color.WHITE);
    g2d.fill(infinityBody);

    // Minus sign in left loop
    Path2D.Double minus = new Path2D.Double();
    minus.moveTo(31.33, 24.44);
    minus.curveTo(31.33, 24.44, 31.33, 24.89, 30.89, 24.89);
    minus.lineTo(16.44, 24.89);
    minus.curveTo(16.44, 24.89, 16.00, 24.89, 16.00, 24.44);
    minus.lineTo(16.00, 20.00);
    minus.curveTo(16.00, 20.00, 16.00, 19.56, 16.44, 19.56);
    minus.lineTo(30.89, 19.56);
    minus.curveTo(30.89, 19.56, 31.33, 19.56, 31.33, 20.00);
    minus.lineTo(31.33, 24.44);
    minus.closePath();
    g2d.fill(minus);

    // Plus sign in right loop
    Path2D.Double plus = new Path2D.Double();
    plus.moveTo(78.89, 20.22);
    plus.curveTo(78.89, 20.22, 78.89, 19.78, 78.44, 19.78);
    plus.lineTo(74.22, 19.78);
    plus.curveTo(74.22, 19.78, 73.78, 19.78, 73.78, 19.33);
    plus.lineTo(73.78, 15.11);
    plus.curveTo(73.78, 15.11, 73.78, 14.67, 73.33, 14.67);
    plus.lineTo(68.89, 14.67);
    plus.curveTo(68.89, 14.67, 68.44, 14.67, 68.44, 15.11);
    plus.lineTo(68.44, 19.33);
    plus.curveTo(68.44, 19.33, 68.44, 19.78, 68.00, 19.78);
    plus.lineTo(63.78, 19.78);
    plus.curveTo(63.78, 19.78, 63.33, 19.78, 63.33, 20.22);
    plus.lineTo(63.33, 24.67);
    plus.curveTo(63.33, 24.67, 63.33, 25.11, 63.78, 25.11);
    plus.lineTo(68.00, 25.11);
    plus.curveTo(68.00, 25.11, 68.44, 25.11, 68.44, 25.56);
    plus.lineTo(68.44, 29.78);
    plus.curveTo(68.44, 29.78, 68.44, 30.22, 68.89, 30.22);
    plus.lineTo(73.33, 30.22);
    plus.curveTo(73.33, 30.22, 73.78, 30.22, 73.78, 29.78);
    plus.lineTo(73.78, 25.56);
    plus.curveTo(73.78, 25.56, 73.78, 25.11, 74.22, 25.11);
    plus.lineTo(78.44, 25.11);
    plus.curveTo(78.44, 25.11, 78.89, 25.11, 78.89, 24.67);
    plus.lineTo(78.89, 20.22);
    plus.closePath();
    g2d.fill(plus);

    g2d.setTransform(oldTx);
  }

  /**
   * Helper to draw the Raspberry Pi logo in white using Path2D vector paths.
   * Native viewBox is 32 x 32.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate
   * @param y Top-left Y coordinate
   * @param size Target width and height in pixels
   */
  public static void drawRaspberryPiLogo(Graphics2D g2d, double x, double y, double size) {
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(x, y);
    double scale = size / 32.0;
    g2d.scale(scale, scale);

    Path2D.Double logoPath = new Path2D.Double(Path2D.WIND_NON_ZERO);
    logoPath.moveTo(13.8, 6.4);
    logoPath.curveTo(12.400, 5.300, 10.900, 4.500, 9.200, 3.900);
    logoPath.curveTo(10.700, 4.800, 12.200, 5.600, 13.400, 6.800);
    logoPath.curveTo(13.300, 7.900, 11.900, 8.600, 10.300, 8.500);
    logoPath.curveTo(10.200, 8.400, 10.400, 8.400, 10.400, 8.200);
    logoPath.curveTo(10.0, 8.1, 9.5, 8.2, 9.2, 8.0);
    logoPath.curveTo(9.200, 7.900, 9.400, 7.900, 9.300, 7.800);
    logoPath.curveTo(9.0, 7.6, 8.6, 7.5, 8.3, 7.3);
    logoPath.curveTo(8.300, 7.200, 8.500, 7.200, 8.600, 7.100);
    logoPath.curveTo(8.300, 6.900, 7.900, 6.800, 7.600, 6.500);
    logoPath.curveTo(7.700, 6.400, 7.800, 6.500, 7.900, 6.300);
    logoPath.curveTo(7.6, 6.1, 7.3, 5.9, 7.1, 5.6);
    logoPath.curveTo(7.200, 5.500, 7.300, 5.600, 7.400, 5.500);
    logoPath.curveTo(7.3, 5.2, 6.9, 5.0, 6.8, 4.7);
    logoPath.curveTo(7.000, 4.700, 7.100, 4.800, 7.300, 4.600);
    logoPath.curveTo(7.1, 4.3, 6.7, 4.2, 6.6, 3.8);
    logoPath.curveTo(6.700, 3.700, 6.900, 3.800, 7.000, 3.700);
    logoPath.curveTo(7.000, 3.400, 6.800, 3.200, 6.700, 2.900);
    logoPath.curveTo(7.000, 2.800, 7.400, 2.900, 7.700, 2.800);
    logoPath.curveTo(7.700, 2.700, 7.600, 2.600, 7.600, 2.500);
    logoPath.curveTo(8.000, 2.300, 8.400, 2.500, 8.800, 2.600);
    logoPath.curveTo(8.900, 2.400, 8.700, 2.400, 8.800, 2.200);
    logoPath.curveTo(9.100, 2.200, 9.400, 2.400, 9.800, 2.400);
    logoPath.curveTo(9.9, 2.2, 9.6, 2.2, 9.6, 2.0);
    logoPath.curveTo(10.000, 2.000, 10.300, 2.200, 10.600, 2.400);
    logoPath.curveTo(10.700, 2.300, 10.600, 2.200, 10.700, 2.000);
    logoPath.curveTo(11.000, 2.100, 11.200, 2.300, 11.500, 2.500);
    logoPath.curveTo(11.700, 2.500, 11.600, 2.300, 11.700, 2.200);
    logoPath.curveTo(12.000, 2.300, 12.200, 2.600, 12.400, 2.700);
    logoPath.curveTo(12.600, 2.700, 12.500, 2.500, 12.600, 2.400);
    logoPath.curveTo(12.900, 2.600, 13.100, 2.900, 13.300, 3.100);
    logoPath.curveTo(13.500, 3.100, 13.400, 2.900, 13.600, 2.900);
    logoPath.curveTo(14.200, 3.600, 14.800, 4.400, 14.700, 5.400);
    logoPath.curveTo(14.7, 5.9, 14.3, 6.2, 13.8, 6.4);
    logoPath.lineTo(13.8, 6.4);
    logoPath.closePath();
    logoPath.moveTo(23.5, 7.1);
    logoPath.curveTo(23.600, 7.200, 23.700, 7.200, 23.800, 7.200);
    logoPath.curveTo(23.500, 7.500, 23.100, 7.500, 22.700, 7.700);
    logoPath.curveTo(22.700, 7.800, 22.800, 7.800, 22.800, 7.900);
    logoPath.curveTo(22.500, 8.100, 22.000, 8.000, 21.700, 8.100);
    logoPath.curveTo(21.600, 8.200, 21.800, 8.300, 21.700, 8.400);
    logoPath.curveTo(21.300, 8.500, 20.900, 8.400, 20.400, 8.300);
    logoPath.curveTo(19.500, 8.100, 18.800, 7.700, 18.500, 6.800);
    logoPath.curveTo(19.700, 5.500, 21.200, 4.700, 22.700, 3.900);
    logoPath.curveTo(21.000, 4.500, 19.500, 5.300, 18.100, 6.300);
    logoPath.curveTo(17.500, 6.100, 17.200, 5.600, 17.200, 5.000);
    logoPath.curveTo(17.200, 4.300, 17.800, 3.200, 18.400, 2.700);
    logoPath.lineTo(18.6, 3.0);
    logoPath.curveTo(18.900, 2.800, 19.100, 2.400, 19.400, 2.300);
    logoPath.curveTo(19.500, 2.400, 19.400, 2.600, 19.600, 2.600);
    logoPath.curveTo(19.800, 2.500, 20.000, 2.200, 20.300, 2.100);
    logoPath.curveTo(20.400, 2.200, 20.300, 2.300, 20.500, 2.400);
    logoPath.curveTo(20.8, 2.4, 21.0, 2.1, 21.4, 2.0);
    logoPath.curveTo(21.400, 2.100, 21.300, 2.200, 21.400, 2.400);
    logoPath.curveTo(21.7, 2.2, 22.0, 2.0, 22.4, 2.0);
    logoPath.curveTo(22.400, 2.100, 22.200, 2.200, 22.300, 2.400);
    logoPath.curveTo(22.600, 2.400, 22.900, 2.200, 23.300, 2.200);
    logoPath.curveTo(23.300, 2.300, 23.200, 2.400, 23.300, 2.600);
    logoPath.curveTo(23.700, 2.500, 24.100, 2.400, 24.500, 2.500);
    logoPath.curveTo(24.500, 2.600, 24.400, 2.700, 24.400, 2.800);
    logoPath.curveTo(24.700, 2.900, 25.100, 2.800, 25.400, 2.900);
    logoPath.curveTo(25.3, 3.2, 25.0, 3.4, 25.0, 3.7);
    logoPath.curveTo(25.100, 3.800, 25.300, 3.700, 25.400, 3.800);
    logoPath.curveTo(25.300, 4.200, 24.900, 4.300, 24.800, 4.600);
    logoPath.curveTo(24.900, 4.800, 25.100, 4.600, 25.200, 4.700);
    logoPath.curveTo(25.100, 5.000, 24.700, 5.200, 24.500, 5.500);
    logoPath.curveTo(24.600, 5.700, 24.700, 5.600, 24.800, 5.600);
    logoPath.curveTo(24.600, 5.900, 24.300, 6.000, 24.100, 6.300);
    logoPath.curveTo(24.200, 6.400, 24.300, 6.400, 24.400, 6.500);
    logoPath.curveTo(24.2, 6.8, 23.8, 6.9, 23.5, 7.1);
    logoPath.lineTo(23.5, 7.1);
    logoPath.closePath();
    logoPath.moveTo(15.4, 16.0);
    logoPath.curveTo(15.400, 17.800, 14.000, 19.600, 12.200, 20.000);
    logoPath.curveTo(10.400, 20.400, 8.800, 19.100, 8.700, 17.300);
    logoPath.curveTo(8.600, 15.500, 9.900, 13.700, 11.600, 13.300);
    logoPath.curveTo(13.7, 12.7, 15.4, 14.0, 15.4, 16.0);
    logoPath.closePath();
    logoPath.moveTo(23.4, 16.9);
    logoPath.curveTo(23.400, 19.000, 21.600, 20.300, 19.600, 19.700);
    logoPath.curveTo(17.800, 19.100, 16.500, 17.200, 16.800, 15.300);
    logoPath.curveTo(17.100, 13.500, 18.900, 12.400, 20.700, 13.100);
    logoPath.curveTo(22.3, 13.7, 23.4, 15.3, 23.4, 16.9);
    logoPath.lineTo(23.4, 16.9);
    logoPath.closePath();
    logoPath.moveTo(16.1, 19.4);
    logoPath.curveTo(17.100, 19.400, 18.100, 19.800, 18.800, 20.600);
    logoPath.curveTo(20.000, 21.900, 19.900, 23.800, 18.600, 24.900);
    logoPath.curveTo(17.300, 26.000, 15.200, 26.100, 13.900, 25.000);
    logoPath.curveTo(12.900, 24.200, 12.500, 23.200, 12.700, 21.900);
    logoPath.curveTo(13.000, 20.600, 13.900, 19.900, 15.100, 19.500);
    logoPath.curveTo(15.4, 19.5, 15.7, 19.4, 16.1, 19.4);
    logoPath.lineTo(16.1, 19.4);
    logoPath.closePath();
    logoPath.moveTo(19.8, 25.3);
    logoPath.curveTo(19.900, 24.300, 20.300, 23.300, 21.100, 22.400);
    logoPath.curveTo(21.600, 21.900, 22.100, 21.400, 22.600, 21.000);
    logoPath.curveTo(22.900, 20.800, 23.200, 20.700, 23.500, 20.600);
    logoPath.curveTo(24.100, 20.500, 24.600, 20.700, 24.800, 21.300);
    logoPath.curveTo(25.200, 22.300, 25.300, 23.300, 24.800, 24.300);
    logoPath.curveTo(24.200, 25.700, 23.100, 26.600, 21.600, 26.900);
    logoPath.curveTo(21.500, 26.900, 21.300, 26.900, 21.100, 26.900);
    logoPath.curveTo(20.2, 27.0, 19.8, 26.6, 19.8, 25.3);
    logoPath.closePath();
    logoPath.moveTo(6.9, 22.7);
    logoPath.curveTo(6.900, 22.700, 6.900, 22.500, 6.900, 22.400);
    logoPath.curveTo(7.000, 21.300, 7.600, 20.900, 8.700, 21.200);
    logoPath.curveTo(10.400, 21.700, 12.000, 23.700, 12.100, 25.500);
    logoPath.curveTo(12.100, 26.600, 11.600, 27.100, 10.500, 26.900);
    logoPath.curveTo(9.000, 26.700, 8.000, 25.900, 7.400, 24.600);
    logoPath.curveTo(7.0, 24.0, 6.9, 23.4, 6.9, 22.7);
    logoPath.lineTo(6.9, 22.7);
    logoPath.closePath();
    logoPath.moveTo(16.2, 12.8);
    logoPath.curveTo(15.400, 12.800, 14.600, 12.700, 13.900, 12.300);
    logoPath.curveTo(12.600, 11.600, 12.600, 10.700, 13.700, 9.900);
    logoPath.curveTo(15.200, 8.800, 17.200, 8.900, 18.600, 10.100);
    logoPath.curveTo(18.700, 10.200, 18.800, 10.300, 18.900, 10.400);
    logoPath.curveTo(19.400, 11.000, 19.300, 11.600, 18.700, 12.100);
    logoPath.curveTo(18.200, 12.500, 17.600, 12.600, 17.000, 12.700);
    logoPath.curveTo(16.7, 12.8, 16.4, 12.8, 16.2, 12.8);
    logoPath.lineTo(16.2, 12.8);
    logoPath.closePath();
    logoPath.moveTo(16.0, 30.0);
    logoPath.curveTo(14.800, 30.000, 13.800, 29.500, 12.900, 28.600);
    logoPath.curveTo(12.500, 28.200, 12.500, 27.800, 13.000, 27.500);
    logoPath.curveTo(13.700, 27.100, 14.400, 26.900, 15.200, 26.800);
    logoPath.curveTo(16.200, 26.700, 17.200, 26.700, 18.200, 27.000);
    logoPath.curveTo(18.400, 27.100, 18.700, 27.200, 18.900, 27.300);
    logoPath.curveTo(19.500, 27.600, 19.600, 27.900, 19.100, 28.500);
    logoPath.curveTo(19.100, 28.500, 19.100, 28.500, 19.000, 28.600);
    logoPath.curveTo(18.3, 29.5, 17.3, 30.0, 16.0, 30.0);
    logoPath.closePath();
    logoPath.moveTo(7.8, 16.8);
    logoPath.curveTo(7.800, 17.900, 7.600, 18.900, 7.200, 19.900);
    logoPath.curveTo(7.100, 20.200, 7.000, 20.400, 6.800, 20.600);
    logoPath.curveTo(6.5, 21.0, 6.3, 21.0, 6.0, 20.7);
    logoPath.curveTo(4.600, 19.300, 4.800, 16.600, 6.500, 15.400);
    logoPath.curveTo(7.100, 14.900, 7.500, 15.000, 7.700, 15.800);
    logoPath.curveTo(7.7, 16.1, 7.8, 16.5, 7.8, 16.8);
    logoPath.lineTo(7.8, 16.8);
    logoPath.closePath();
    logoPath.moveTo(26.9, 18.3);
    logoPath.curveTo(26.900, 19.100, 26.600, 20.000, 26.000, 20.700);
    logoPath.curveTo(25.700, 21.000, 25.500, 21.000, 25.200, 20.700);
    logoPath.curveTo(24.900, 20.300, 24.700, 19.800, 24.600, 19.300);
    logoPath.curveTo(24.300, 18.300, 24.200, 17.200, 24.300, 16.100);
    logoPath.curveTo(24.300, 15.900, 24.400, 15.600, 24.500, 15.400);
    logoPath.curveTo(24.700, 15.000, 24.900, 14.900, 25.300, 15.200);
    logoPath.curveTo(26.3, 15.8, 26.9, 16.9, 26.9, 18.3);
    logoPath.closePath();
    logoPath.moveTo(7.5, 13.9);
    logoPath.curveTo(7.400, 12.600, 7.800, 11.400, 8.900, 10.600);
    logoPath.curveTo(10.000, 9.800, 11.200, 9.600, 12.500, 9.800);
    logoPath.curveTo(12.500, 10.100, 12.300, 10.300, 12.200, 10.500);
    logoPath.curveTo(11.500, 11.400, 10.600, 12.100, 9.800, 12.800);
    logoPath.curveTo(9.300, 13.200, 8.800, 13.500, 8.300, 13.800);
    logoPath.curveTo(7.9, 13.9, 7.7, 14.1, 7.5, 13.9);
    logoPath.closePath();
    logoPath.moveTo(24.6, 14.0);
    logoPath.curveTo(24.400, 14.100, 24.100, 14.000, 23.900, 13.800);
    logoPath.curveTo(23.200, 13.400, 22.500, 12.900, 21.900, 12.400);
    logoPath.curveTo(21.200, 11.800, 20.600, 11.200, 20.000, 10.600);
    logoPath.curveTo(19.900, 10.400, 19.700, 10.200, 19.700, 10.000);
    logoPath.curveTo(20.300, 9.700, 22.300, 9.800, 23.300, 10.700);
    logoPath.curveTo(24.3, 11.5, 24.9, 13.1, 24.6, 14.0);
    logoPath.closePath();

    g2d.setColor(Color.WHITE);
    g2d.fill(logoPath);
    g2d.setTransform(oldTx);
  }

  /**
   * Helper to draw a complete PCB terminal block (green body + inner compartment lines + screw lugs)
   * that matches the visuals of the PCBTerminalBlock component.
   *
   * @param g2d Graphics2D context
   * @param startIndex First control point index
   * @param count Number of positions in this terminal block
   * @param isHorizontal true if terminal block runs horizontally (along X), false if vertically (along Y)
   * @param wireEntryOffset Direction/offset from pins to the wire entry edge (positive or negative)
   * @param blockDepth Total depth of the green body (e.g. 50.0px for 5.08mm blocks, or 35.0px for compact)
   * @param outlineMode Outline mode flag
   * @param drawingObserver Observer
   */
  protected void drawTerminalBlock(Graphics2D g2d, int startIndex, int count, boolean isHorizontal,
      double wireEntryOffset, double blockDepth, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    if (count <= 0 || startIndex < 0 || startIndex >= controlPoints.length) return;

    Point2D p0 = controlPoints[startIndex];
    Point2D pLast = controlPoints[Math.min(startIndex + count - 1, controlPoints.length - 1)];

    double pitchPx = count > 1 ?
        (isHorizontal ? Math.abs(pLast.getX() - p0.getX()) / (count - 1) : Math.abs(pLast.getY() - p0.getY()) / (count - 1))
        : new Size(5.08d, SizeUnit.mm).convertToPixels();
    if (pitchPx <= 0) {
      pitchPx = new Size(5.08d, SizeUnit.mm).convertToPixels();
    }

    double minX = Math.min(p0.getX(), pLast.getX());
    double minY = Math.min(p0.getY(), pLast.getY());
    double maxX = Math.max(p0.getX(), pLast.getX());
    double maxY = Math.max(p0.getY(), pLast.getY());

    Rectangle2D outerBody;
    Rectangle2D innerCompartment;

    if (isHorizontal) {
      double bx = minX - pitchPx / 2.0;
      double bw = (maxX - minX) + pitchPx;
      double by = wireEntryOffset >= 0 ? minY - (blockDepth - pitchPx) / 2.0 : minY - (blockDepth - pitchPx / 2.0);
      double bh = blockDepth;
      outerBody = new Rectangle2D.Double(bx, by, bw, bh);
      innerCompartment = new Rectangle2D.Double(bx, minY - pitchPx / 2.0, bw, pitchPx);
    } else {
      double by = minY - pitchPx / 2.0;
      double bh = (maxY - minY) + pitchPx;
      double bx = wireEntryOffset >= 0 ? minX - (blockDepth - pitchPx) / 2.0 : minX - (blockDepth - pitchPx / 2.0);
      double bw = blockDepth;
      outerBody = new Rectangle2D.Double(bx, by, bw, bh);
      innerCompartment = new Rectangle2D.Double(minX - pitchPx / 2.0, by, pitchPx, bh);
    }

    // Draw green body and compartment
    g2d.setColor(SCREW_TERMINAL_COLOR);
    g2d.fill(outerBody);
    g2d.setColor(SCREW_TERMINAL_BORDER);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(innerCompartment);
    g2d.draw(outerBody);

    // Draw screw contacts
    drawScrewTerminals(g2d, startIndex, count, pitchPx, outlineMode, drawingObserver);
  }

  /**
   * Helper to draw screw terminal block contacts at given control points.
   * Visuals match the PCBTerminalBlock component: light metal screw circles with diagonal slot.
   */
  protected void drawScrewTerminals(Graphics2D g2d, int startIndex, int count, double pitchPx, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    double effectivePitch = pitchPx > 0 ? pitchPx : PIN_SPACING.convertToPixels();
    int circleDiameter = getClosestOdd((int) (effectivePitch * 3d / 5));

    drawingObserver.startTrackingContinuityArea(true);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      // Light metal screw head circle
      g2d.setColor(SCREW_CIRCLE_COLOR);
      g2d.fillOval((int) Math.round(p.getX() - circleDiameter / 2.0), (int) Math.round(p.getY() - circleDiameter / 2.0),
          circleDiameter, circleDiameter);
      // Diagonal screw slot line
      g2d.setColor(SCREW_CIRCLE_COLOR.darker());
      g2d.drawLine((int) (p.getX() + Math.cos(Math.PI / 4) * circleDiameter / 2.0),
          (int) (p.getY() + Math.sin(Math.PI / 4) * circleDiameter / 2.0),
          (int) (p.getX() + Math.cos(5 * Math.PI / 4) * circleDiameter / 2.0),
          (int) (p.getY() + Math.sin(5 * Math.PI / 4) * circleDiameter / 2.0));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  /**
   * Helper to draw rectangular tinned solder pads with drill holes at given control points.
   */
  protected void drawSolderPads(Graphics2D g2d, int startIndex, int count, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    double padW = 22.0;
    double padH = 16.0;
    double holeD = 7.0;

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      RoundRectangle2D pad = new RoundRectangle2D.Double(p.getX() - padW / 2.0, p.getY() - padH / 2.0, padW, padH, 2, 2);
      g2d.setColor(LIGHT_METAL_COLOR);
      g2d.fill(pad);
      g2d.setColor(LIGHT_METAL_COLOR.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(pad);

      // Central through-hole / drill hole
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(p.getX() - holeD / 2.0, p.getY() - holeD / 2.0, holeD, holeD));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new Ellipse2D.Double(p.getX() - holeD / 2.0, p.getY() - holeD / 2.0, holeD, holeD));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  /**
   * Helper to draw a mounting hole.
   */
  protected void drawMountingHole(Graphics2D g2d, double cx, double cy, double diameter) {
    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fill(new Ellipse2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter));
    g2d.setColor(Color.DARK_GRAY);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new Ellipse2D.Double(cx - diameter / 2.0, cy - diameter / 2.0, diameter, diameter));
  }

  /**
   * Helper to draw an IC chip on the board.
   */
  protected void drawChip(Graphics2D g2d, double x, double y, double w, double h, String label) {
    g2d.setColor(IC_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 4, 4));
    g2d.setColor(IC_BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 4, 4));

    // pin 1 dot
    g2d.setColor(PIN_MARKER_COLOR);
    g2d.fill(new Ellipse2D.Double(x + 3, y + 3, 3, 3));

    if (label != null && !label.isEmpty() && w > 20 && h > 10) {
      g2d.setColor(IC_TEXT_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, label, x + w / 2.0, y + h / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  /**
   * Helper to draw a metal USB connector / shield.
   */
  protected void drawMetalConnector(Graphics2D g2d, double x, double y, double w, double h, String label) {
    g2d.setColor(USB_METAL_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 3, 3));
    g2d.setColor(METAL_SHIELD_BORDER);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 3, 3));
    if (label != null && !label.isEmpty()) {
      g2d.setColor(METAL_LABEL_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, label, x + w / 2.0, y + h / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  /**
   * Helper to get outline border color.
   */
  protected Color getFinalBorderColor(ComponentState componentState, boolean outlineMode) {
    if (outlineMode) {
      Theme theme = Constants.DEFAULT_THEME;
      try {
        Theme t = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
        if (t != null) theme = t;
      } catch (Exception ignored) {}
      return (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) ? SELECTION_COLOR : theme.getOutlineColor();
    }
    return (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) ? SELECTION_COLOR : bodyColor.darker();
  }

  /**
   * Returns the unrotated body shape in local coordinate space (relative to unrotated p0).
   */
  public abstract Shape getBodyShape();

  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    if (controlPoints != null) {
      for (Point2D p : controlPoints) {
        if (p != null) {
          if (p.getX() < minX) minX = p.getX();
          if (p.getX() > maxX) maxX = p.getX();
          if (p.getY() < minY) minY = p.getY();
          if (p.getY() > maxY) maxY = p.getY();
        }
      }
    }

    Shape bodyShape = getBodyShape();
    if (bodyShape != null && controlPoints != null && controlPoints.length > 0 && controlPoints[0] != null) {
      Point2D p0 = controlPoints[0];
      AffineTransform tx = AffineTransform.getRotateInstance(orientation.toRadians(), p0.getX(), p0.getY());
      Shape rotated = tx.createTransformedShape(bodyShape);
      Rectangle2D b = rotated.getBounds2D();
      if (b.getMinX() < minX) minX = b.getMinX();
      if (b.getMaxX() > maxX) maxX = b.getMaxX();
      if (b.getMinY() < minY) minY = b.getMinY();
      if (b.getMaxY() > maxY) maxY = b.getMaxY();
    }

    if (minX == Double.MAX_VALUE) {
      Point2D p0 = (controlPoints != null && controlPoints.length > 0 && controlPoints[0] != null)
          ? controlPoints[0] : new Point2D.Double(0, 0);
      return new Rectangle2D.Double(p0.getX() - 50, p0.getY() - 50, 100, 100);
    }

    int margin = 50;
    return new Rectangle2D.Double(minX - margin, minY - margin, (maxX - minX) + 2 * margin, (maxY - minY) + 2 * margin);
  }
}
