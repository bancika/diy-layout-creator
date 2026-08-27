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
import java.awt.geom.Area;
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
  public static Color ANTENNA_COLOR = Color.decode("#DAA520");
  public static Color ANTENNA_BG_COLOR = Color.decode("#1E1E1E");
  public static Color PAD_COLOR = Color.decode("#DAA520");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");

  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Size PAD_SIZE = new Size(0.065d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(0.035d, SizeUnit.in);
  public static Size ANTENNA_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH = new Size(7.0d, SizeUnit.mm);
  public static Size BUTTON_WIDTH = new Size(3.5d, SizeUnit.mm);
  public static Size BUTTON_LENGTH = new Size(3.0d, SizeUnit.mm);

  // Standard USB Port Dimensions
  public static Size USB_MICRO_WIDTH = new Size(7.5d, SizeUnit.mm);
  public static Size USB_MICRO_LENGTH = new Size(5.6d, SizeUnit.mm);
  public static Size USB_MICRO_OVERHANG = new Size(0.5d, SizeUnit.mm);

  public static Size USB_C_WIDTH = new Size(8.94d, SizeUnit.mm);
  public static Size USB_C_LENGTH = new Size(7.5d, SizeUnit.mm);
  public static Size USB_C_OVERHANG = new Size(0.5d, SizeUnit.mm);

  public static Size USB_A_WIDTH = new Size(14.5d, SizeUnit.mm);
  public static Size USB_A_LENGTH = new Size(14.0d, SizeUnit.mm);
  public static Size USB_A_DUAL_LENGTH = new Size(17.5d, SizeUnit.mm);

  public static Size USB_B_WIDTH = new Size(0.45d, SizeUnit.in);
  public static Size USB_B_LENGTH = new Size(0.51d, SizeUnit.in);
  public static Size USB_B_OVERHANG = new Size(0.14d, SizeUnit.in);

  public static Size USB_MINI_WIDTH = new Size(0.30d, SizeUnit.in);
  public static Size USB_MINI_LENGTH = new Size(0.36d, SizeUnit.in);
  public static Size USB_MINI_OVERHANG = new Size(0.05d, SizeUnit.in);

  public enum UsbPortType {
    MICRO,
    TYPE_C,
    TYPE_A,
    TYPE_B,
    MINI
  }

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
  protected void drawPinHeader(Graphics2D g2d, int startIndex, int count, boolean female, boolean outlineMode, IDrawingObserver drawingObserver) {
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
   * Alias for {@link #drawPinHeader(Graphics2D, int, int, boolean, boolean, IDrawingObserver)}.
   */
  protected void drawPins(Graphics2D g2d, int startIndex, int count, boolean female, boolean outlineMode, IDrawingObserver drawingObserver) {
    drawPinHeader(g2d, startIndex, count, female, outlineMode, drawingObserver);
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

  private static final Area RASPBERRY_PI_LOGO_AREA = createRaspberryPiLogoArea();

  private static Area createRaspberryPiLogoArea() {
    Path2D.Double p0 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p0.moveTo(19.6295, 0.0014);
    p0.curveTo(19.1612, 0.0159, 18.6569, 0.1889, 18.0849, 0.6402);
    p0.curveTo(16.6837, 0.0999, 15.3251, -0.0878, 14.1102, 1.0122);
    p0.curveTo(12.2342, 0.7689, 11.6245, 1.2710, 11.1626, 1.8572);
    p0.curveTo(10.7507, 1.8487, 8.0807, 1.4339, 6.8564, 3.2603);
    p0.curveTo(3.7794, 2.8963, 2.8070, 5.0701, 3.9089, 7.0973);
    p0.curveTo(3.2805, 8.0702, 2.6293, 9.0313, 4.0989, 10.8859);
    p0.curveTo(3.5790, 11.9189, 3.9013, 13.0394, 5.1259, 14.3954);
    p0.curveTo(4.8027, 15.8475, 5.4379, 16.8720, 6.5775, 17.6704);
    p0.curveTo(6.3644, 19.6575, 8.3997, 20.8128, 9.0075, 21.2245);
    p0.curveTo(9.2408, 22.3823, 9.7272, 23.4749, 12.0520, 24.0790);
    p0.curveTo(12.4354, 25.8045, 13.8326, 26.1024, 15.1856, 26.4646);
    p0.curveTo(10.7138, 29.0639, 6.8789, 32.4840, 6.9049, 40.8755);
    p0.lineTo(6.2500, 42.0440);
    p0.curveTo(1.1224, 45.1622, -3.4908, 55.1843, 3.7230, 63.3304);
    p0.curveTo(4.1942, 65.8804, 4.9845, 67.7119, 5.6881, 69.7390);
    p0.curveTo(6.7405, 77.9072, 13.6088, 81.7320, 15.4203, 82.1843);
    p0.curveTo(18.0749, 84.2063, 20.9021, 86.1249, 24.7280, 87.4689);
    p0.curveTo(28.3346, 91.1885, 32.2420, 92.6062, 36.1706, 92.6039);
    p0.curveTo(36.2282, 92.6039, 36.2868, 92.6045, 36.3445, 92.6039);
    p0.curveTo(40.2731, 92.6063, 44.1803, 91.1887, 47.7870, 87.4689);
    p0.curveTo(51.6128, 86.1249, 54.4401, 84.2063, 57.0947, 82.1843);
    p0.curveTo(58.9062, 81.7319, 65.7745, 77.9072, 66.8269, 69.7390);
    p0.curveTo(67.5305, 67.7119, 68.3208, 65.8803, 68.7920, 63.3304);
    p0.curveTo(76.0057, 55.1837, 71.3926, 45.1609, 66.2650, 42.0427);
    p0.lineTo(65.6090, 40.8744);
    p0.curveTo(65.6350, 32.4834, 61.8002, 29.0634, 57.3284, 26.4634);
    p0.curveTo(58.6814, 26.1014, 60.0786, 25.8034, 60.4619, 24.0779);
    p0.curveTo(62.7867, 23.4737, 63.2732, 22.3811, 63.5065, 21.2234);
    p0.curveTo(64.1143, 20.8118, 66.1495, 19.6564, 65.9365, 17.6693);
    p0.curveTo(67.0759, 16.8708, 67.7113, 15.8463, 67.3880, 14.3943);
    p0.curveTo(68.6127, 13.0383, 68.9350, 11.9177, 68.4150, 10.8847);
    p0.curveTo(69.8849, 9.0309, 69.2328, 8.0698, 68.6052, 7.0970);
    p0.curveTo(69.7063, 5.0698, 68.7346, 2.8959, 65.6566, 3.2600);
    p0.curveTo(64.4326, 1.4335, 61.7634, 1.8484, 61.3506, 1.8569);
    p0.curveTo(60.8887, 1.2707, 60.2793, 0.7685, 58.4032, 1.0119);
    p0.curveTo(57.1883, -0.0882, 55.8298, 0.0995, 54.4285, 0.6399);
    p0.curveTo(52.7646, -0.6731, 51.6635, 0.3794, 50.4059, 0.7773);
    p0.curveTo(48.3914, 0.1191, 47.9309, 1.0207, 46.9410, 1.3879);
    p0.curveTo(44.7440, 0.9236, 44.0764, 1.9344, 43.0232, 3.0012);
    p0.lineTo(41.7982, 2.9769);
    p0.curveTo(38.4844, 4.9297, 36.8382, 8.9061, 36.2548, 10.9503);
    p0.curveTo(35.6710, 8.9057, 34.0286, 4.9293, 30.7155, 2.9769);
    p0.lineTo(29.4904, 3.0012);
    p0.curveTo(28.4359, 1.9344, 27.7683, 0.9236, 25.5714, 1.3879);
    p0.curveTo(24.5815, 1.0207, 24.1222, 0.1191, 22.1064, 0.7773);
    p0.curveTo(21.2810, 0.5162, 20.5218, -0.0267, 19.6279, 0.0010);
    p0.closePath();

    Path2D.Double p1 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p1.moveTo(13.0322, 8.5925);
    p1.curveTo(21.8236, 13.1250, 26.9346, 16.7915, 29.7345, 19.9142);
    p1.curveTo(28.3006, 25.6612, 20.8204, 25.9235, 18.0852, 25.7623);
    p1.curveTo(18.6452, 25.5016, 19.1125, 25.1894, 19.2782, 24.7097);
    p1.curveTo(18.5919, 24.2219, 16.1583, 24.6583, 14.4594, 23.7037);
    p1.curveTo(15.1120, 23.5686, 15.4173, 23.4369, 15.7225, 22.9552);
    p1.curveTo(14.1174, 22.4433, 12.3885, 22.0022, 11.3715, 21.1541);
    p1.curveTo(11.9203, 21.1609, 12.4327, 21.2768, 13.1493, 20.7798);
    p1.curveTo(11.7116, 20.0050, 10.1775, 19.3910, 8.9856, 18.2068);
    p1.curveTo(9.7289, 18.1885, 10.5303, 18.1994, 10.7633, 17.9260);
    p1.curveTo(9.4475, 17.1108, 8.3373, 16.2042, 7.4183, 15.2126);
    p1.curveTo(8.4585, 15.3381, 8.8977, 15.2300, 9.1493, 15.0488);
    p1.curveTo(8.1546, 14.0300, 6.8956, 13.1698, 6.2954, 11.9143);
    p1.curveTo(7.0678, 12.1805, 7.7744, 12.2825, 8.2838, 11.8909);
    p1.curveTo(7.9458, 11.1284, 6.4976, 10.6787, 5.6638, 8.8967);
    p1.curveTo(6.4770, 8.9756, 7.3394, 9.0741, 7.5119, 8.8967);
    p1.curveTo(7.1345, 7.3591, 6.4869, 6.4947, 5.8518, 5.5991);
    p1.curveTo(7.5920, 5.5732, 10.2286, 5.6058, 10.1092, 5.4587);
    p1.lineTo(9.0332, 4.3593);
    p1.curveTo(10.7330, 3.9016, 12.4723, 4.4328, 13.7350, 4.8271);
    p1.curveTo(14.3020, 4.3797, 13.7250, 3.8141, 13.0332, 3.2365);
    p1.curveTo(14.4778, 3.4293, 15.7832, 3.7615, 16.9630, 4.2189);
    p1.curveTo(17.5935, 3.6497, 16.5537, 3.0805, 16.0507, 2.5113);
    p1.curveTo(18.2825, 2.9347, 19.2281, 3.5297, 20.1677, 4.1253);
    p1.curveTo(20.8496, 3.4719, 20.2068, 2.9165, 19.7467, 2.3476);
    p1.curveTo(21.4295, 2.9709, 22.2962, 3.7756, 23.2088, 4.5698);
    p1.curveTo(23.5182, 4.1523, 23.9948, 3.8463, 23.4193, 2.8388);
    p1.curveTo(24.6141, 3.5275, 25.5139, 4.3390, 26.1796, 5.2482);
    p1.curveTo(26.9189, 4.7775, 26.6200, 4.1338, 26.6241, 3.5406);
    p1.curveTo(27.8657, 4.5506, 28.6537, 5.6255, 29.6183, 6.6751);
    p1.curveTo(29.8126, 6.5336, 29.9827, 6.0538, 30.1329, 5.2949);
    p1.curveTo(33.0952, 8.1688, 37.2809, 15.4074, 31.2089, 18.2774);
    p1.curveTo(26.0413, 14.0154, 19.8697, 10.9176, 13.0304, 8.5937);
    p1.closePath();
    p1.moveTo(59.6793, 8.5925);
    p1.curveTo(50.8889, 13.1254, 45.7783, 16.7909, 42.9783, 19.9142);
    p1.curveTo(44.4122, 25.6612, 51.8924, 25.9235, 54.6276, 25.7623);
    p1.curveTo(54.0676, 25.5016, 53.6003, 25.1894, 53.4346, 24.7097);
    p1.curveTo(54.1210, 24.2219, 56.5545, 24.6583, 58.2535, 23.7037);
    p1.curveTo(57.6008, 23.5686, 57.2955, 23.4369, 56.9903, 22.9552);
    p1.curveTo(58.5954, 22.4433, 60.3244, 22.0022, 61.3413, 21.1541);
    p1.curveTo(60.7925, 21.1609, 60.2801, 21.2768, 59.5635, 20.7798);
    p1.curveTo(61.0012, 20.0050, 62.5353, 19.3910, 63.7272, 18.2068);
    p1.curveTo(62.9839, 18.1885, 62.1825, 18.1994, 61.9495, 17.9260);
    p1.curveTo(63.2653, 17.1108, 64.3755, 16.2042, 65.2946, 15.2126);
    p1.curveTo(64.2543, 15.3381, 63.8151, 15.2300, 63.5636, 15.0488);
    p1.curveTo(64.5582, 14.0300, 65.8172, 13.1698, 66.4174, 11.9143);
    p1.curveTo(65.6450, 12.1805, 64.9384, 12.2825, 64.4290, 11.8909);
    p1.curveTo(64.7670, 11.1284, 66.2152, 10.6787, 67.0490, 8.8967);
    p1.curveTo(66.2358, 8.9756, 65.3734, 9.0741, 65.2010, 8.8967);
    p1.curveTo(65.5790, 7.3585, 66.2266, 6.4940, 66.8617, 5.5984);
    p1.curveTo(65.1215, 5.5726, 62.4849, 5.6052, 62.6043, 5.4581);
    p1.lineTo(63.6803, 4.3587);
    p1.curveTo(61.9805, 3.9010, 60.2411, 4.4322, 58.9785, 4.8265);
    p1.curveTo(58.4115, 4.3791, 58.9885, 3.8135, 59.6802, 3.2358);
    p1.curveTo(58.2357, 3.4287, 56.9302, 3.7608, 55.7504, 4.2183);
    p1.curveTo(55.1200, 3.6491, 56.1598, 3.0799, 56.6627, 2.5107);
    p1.curveTo(54.4310, 2.9341, 53.4854, 3.5290, 52.5457, 4.1247);
    p1.curveTo(51.8639, 3.4713, 52.5067, 2.9159, 52.9667, 2.3470);
    p1.curveTo(51.2840, 2.9703, 50.4172, 3.7750, 49.5047, 4.5692);
    p1.curveTo(49.1953, 4.1517, 48.7187, 3.8456, 49.2941, 2.8382);
    p1.curveTo(48.0994, 3.5268, 47.1995, 4.3384, 46.5338, 5.2476);
    p1.curveTo(45.7946, 4.7769, 46.0934, 4.1332, 46.0894, 3.5399);
    p1.curveTo(44.8477, 4.5499, 44.0597, 5.6249, 43.0952, 6.6744);
    p1.curveTo(42.9009, 6.5330, 42.7307, 6.0532, 42.5805, 5.2943);
    p1.curveTo(39.6183, 8.1682, 35.4325, 15.4067, 41.5045, 18.2768);
    p1.curveTo(46.6695, 14.0139, 52.8407, 10.9163, 59.6805, 8.5924);
    p1.closePath();

    Path2D.Double p2 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p2.moveTo(47.0024, 67.1140);
    p2.curveTo(47.0188, 70.5889, 45.0112, 73.8170, 41.7504, 75.5588);
    p2.curveTo(38.4896, 77.3006, 34.4542, 77.3006, 31.1934, 75.5588);
    p2.curveTo(27.9326, 73.8170, 25.9250, 70.5889, 25.9414, 67.1140);
    p2.curveTo(25.9250, 63.6391, 27.9326, 60.4110, 31.1934, 58.6692);
    p2.curveTo(34.4542, 56.9274, 38.4896, 56.9274, 41.7504, 58.6692);
    p2.curveTo(45.0112, 60.4110, 47.0188, 63.6391, 47.0024, 67.1140);
    p2.closePath();
    p2.moveTo(30.3786, 39.3746);
    p2.curveTo(33.2055, 41.2176, 34.6522, 44.7077, 34.1649, 48.5090);
    p2.curveTo(33.6776, 52.3102, 31.3275, 55.8665, 28.0144, 57.8164);
    p2.curveTo(24.7013, 59.7663, 20.9097, 59.8245, 18.0912, 57.9688);
    p2.curveTo(15.2643, 56.1258, 13.8176, 52.6357, 14.3049, 48.8344);
    p2.curveTo(14.7922, 45.0332, 17.1423, 41.4769, 20.4554, 39.5270);
    p2.curveTo(23.7685, 37.5771, 27.5601, 37.5189, 30.3786, 39.3746);
    p2.closePath();

    Path2D.Double p3 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p3.moveTo(42.1557, 38.8570);
    p3.curveTo(37.8215, 41.6966, 37.0284, 48.2301, 40.3987, 53.3303);
    p3.curveTo(43.7691, 58.4306, 50.1089, 60.2909, 54.4431, 57.4513);
    p3.curveTo(58.7773, 54.6117, 59.5704, 48.0782, 56.2001, 42.9780);
    p3.curveTo(52.8297, 37.8777, 46.4899, 36.0174, 42.1557, 38.8570);
    p3.closePath();
    p3.moveTo(8.5710, 44.0484);
    p3.curveTo(13.2819, 42.7856, 10.1614, 63.5377, 6.3284, 61.8350);
    p3.curveTo(2.1120, 58.4438, 0.7540, 48.5122, 8.5710, 44.0484);
    p3.closePath();

    Path2D.Double p4 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p4.moveTo(63.0123, 43.7896);
    p4.curveTo(58.3009, 42.5272, 61.4219, 63.2802, 65.2549, 61.5775);
    p4.curveTo(69.4712, 58.1859, 70.8292, 48.2535, 63.0123, 43.7896);
    p4.closePath();
    p4.moveTo(47.0060, 28.3359);
    p4.curveTo(55.1360, 26.9631, 61.9009, 31.7933, 61.6279, 40.6091);
    p4.curveTo(61.3605, 43.9888, 44.0109, 28.8391, 47.0061, 28.3359);
    p4.closePath();
    p4.moveTo(24.5436, 28.0771);
    p4.curveTo(16.4130, 26.7043, 9.6488, 31.5356, 9.9218, 40.3506);
    p4.curveTo(10.1892, 43.7301, 27.5388, 28.5804, 24.5436, 28.0771);
    p4.closePath();
    p4.moveTo(36.2218, 26.0212);
    p4.curveTo(31.3695, 25.8950, 26.7127, 29.6225, 26.7014, 31.7845);
    p4.curveTo(26.6879, 34.4115, 30.5378, 37.1013, 36.2548, 37.1695);
    p4.curveTo(42.0931, 37.2113, 45.8185, 35.0165, 45.8372, 32.3055);
    p4.curveTo(45.8585, 29.2338, 40.5274, 25.9737, 36.2217, 26.0211);
    p4.closePath();
    p4.moveTo(36.5181, 79.8920);
    p4.curveTo(40.7486, 79.7073, 46.4253, 81.2546, 46.4364, 83.3072);
    p4.curveTo(46.5066, 85.3003, 41.2882, 89.8037, 36.2375, 89.7166);
    p4.curveTo(31.0069, 89.9423, 25.8778, 85.4319, 25.9449, 83.8686);
    p4.curveTo(25.8666, 81.5764, 32.3140, 79.7867, 36.5181, 79.8919);
    p4.closePath();
    p4.moveTo(20.8923, 67.7271);
    p4.curveTo(23.9043, 71.3559, 25.2773, 77.7311, 22.7637, 79.6104);
    p4.curveTo(20.3857, 81.0450, 14.6109, 80.4542, 10.5063, 74.5576);
    p4.curveTo(7.7381, 69.6096, 8.0949, 64.5746, 10.0385, 63.0956);
    p4.curveTo(12.9450, 61.3250, 17.4358, 63.7165, 20.8925, 67.7271);
    p4.closePath();
    p4.moveTo(51.5410, 66.5770);
    p4.curveTo(48.2822, 70.3940, 46.4676, 77.3560, 48.8449, 79.5982);
    p4.curveTo(51.1179, 81.3402, 57.2199, 81.0967, 61.7272, 74.8425);
    p4.curveTo(65.0002, 70.6421, 63.9035, 63.6272, 62.0340, 61.7643);
    p4.curveTo(59.2569, 59.6163, 55.2701, 62.3653, 51.5410, 66.5759);
    p4.closePath();

    Area area = new Area(p0);
    area.subtract(new Area(p1));
    area.subtract(new Area(p2));
    area.subtract(new Area(p3));
    area.subtract(new Area(p4));

    return area;
  }

  /**
   * Helper to draw the Raspberry Pi logo in white using vector paths.
   * Native bounding box is 72.51 x 92.60 px.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate
   * @param y Top-left Y coordinate
   * @param size Target height in pixels
   */
  public static void drawRaspberryPiLogo(Graphics2D g2d, double x, double y, double size) {
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(x, y);
    double scale = size / 92.604;
    g2d.scale(scale, scale);

    g2d.setColor(Color.WHITE);
    g2d.fill(RASPBERRY_PI_LOGO_AREA);
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
   * Helper to draw standard PCB through-hole solder pads (gold/copper pads with drill holes, square for Pin 1).
   *
   * @param g2d Graphics2D context
   * @param startIndex First control point index
   * @param count Number of pads to draw
   * @param squarePin1 If true, the first pad (index == startIndex) is drawn as a square pad
   * @param outlineMode Outline mode flag
   * @param drawingObserver Observer
   */
  protected void drawPcbSolderPads(Graphics2D g2d, int startIndex, int count, boolean squarePin1, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    int diameter = getClosestOdd((int) Math.round(PAD_SIZE.convertToPixels()));
    int holeDiameter = getClosestOdd((int) Math.round(HOLE_SIZE.convertToPixels()));

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      if (squarePin1 && i == startIndex) {
        // Pin 1 is a square solder pad
        g2d.setColor(PAD_COLOR);
        g2d.fill(new Rectangle2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Rectangle2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
      } else {
        // Round solder pads
        g2d.setColor(PAD_COLOR);
        g2d.fill(new Ellipse2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Ellipse2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
      }

      // Central drill hole
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(p.getX() - holeDiameter / 2.0, p.getY() - holeDiameter / 2.0, holeDiameter, holeDiameter));
      g2d.setColor(PAD_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(p.getX() - holeDiameter / 2.0, p.getY() - holeDiameter / 2.0, holeDiameter, holeDiameter));
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
   * Helper to draw an FPC / ribbon cable connector (e.g. MIPI CSI/DSI, PCIe FPC).
   */
  protected void drawFpcConnector(Graphics2D g2d, double x, double y, double w, double h, boolean vertical, String label) {
    g2d.setColor(IC_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 2, 2));
    g2d.setColor(IC_BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 2, 2));

    // Inner slot / latch bar
    g2d.setColor(HEADER_BODY_COLOR.darker());
    if (vertical) {
      double slotW = Math.max(1.5, w * 0.35);
      double slotH = Math.max(2.0, h - 4);
      g2d.fill(new Rectangle2D.Double(x + (w - slotW) / 2.0, y + 2, slotW, slotH));
    } else {
      double slotW = Math.max(2.0, w - 4);
      double slotH = Math.max(1.5, h * 0.35);
      g2d.fill(new Rectangle2D.Double(x + 2, y + (h - slotH) / 2.0, slotW, slotH));
    }

    if (label != null && !label.isEmpty()) {
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      if (w >= 20 && h >= 10) {
        StringUtils.drawCenteredText(g2d, label, x + w / 2.0, y + h / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }
  }

  /**
   * Helper to draw a standard Micro-USB connector.
   */
  protected void drawMicroUsb(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard USB Type-C connector.
   */
  protected void drawUsbC(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard USB Type-A connector.
   */
  protected void drawUsbA(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard USB Type-B connector.
   */
  protected void drawUsbB(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a standard Mini-USB connector.
   */
  protected void drawMiniUsb(Graphics2D g2d, double x, double y, double w, double h, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw any standardized USB port type.
   */
  protected void drawUsbPort(Graphics2D g2d, double x, double y, double w, double h, UsbPortType type, String label) {
    drawMetalConnector(g2d, x, y, w, h, label);
  }

  /**
   * Helper to draw a metal connector / shield (e.g. RF shield cans, SD card slots, HDMI).
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
   * Helper to draw a standard ESP-style PCB meander antenna with a dark (#1E1E1E) substrate rectangle underneath.
   *
   * @param g2d Graphics2D context
   * @param x top-left X coordinate of the antenna substrate rectangle
   * @param y top-left Y coordinate of the antenna substrate rectangle
   * @param width width of the antenna substrate rectangle (e.g. 15.0mm, matching main chip width)
   * @param height height of the antenna substrate rectangle (e.g. 7.0mm)
   */
  protected void drawPcbAntenna(Graphics2D g2d, double x, double y, double width, double height) {
    // Dark rectangle underneath (#1E1E1E)
    g2d.setColor(ANTENNA_BG_COLOR);
    g2d.fill(new Rectangle2D.Double(x, y, width, height));

    // Serpentine antenna trace (gold/copper)
    g2d.setColor(ANTENNA_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    Path2D.Double antPath = new Path2D.Double();
    double antPadX = x + new Size(1.5d, SizeUnit.mm).convertToPixels();
    double antPadW = width - new Size(3.0d, SizeUnit.mm).convertToPixels();
    double traceH = new Size(4.1d, SizeUnit.mm).convertToPixels();
    double marginY = height > traceH ? (height - traceH) / 2.0 : height * 0.15;
    double antTopY = y + marginY;
    double antMidY = y + height - marginY;
    antPath.moveTo(antPadX, antMidY);
    antPath.lineTo(antPadX, antTopY);
    antPath.lineTo(antPadX + antPadW * 0.25, antTopY);
    antPath.lineTo(antPadX + antPadW * 0.25, antMidY);
    antPath.lineTo(antPadX + antPadW * 0.50, antMidY);
    antPath.lineTo(antPadX + antPadW * 0.50, antTopY);
    antPath.lineTo(antPadX + antPadW * 0.75, antTopY);
    antPath.lineTo(antPadX + antPadW * 0.75, antMidY);
    antPath.lineTo(antPadX + antPadW, antMidY);
    antPath.lineTo(antPadX + antPadW, antTopY);
    g2d.draw(antPath);
  }

  /**
   * Helper to draw a standard ESP-style PCB meander antenna using default 15.0mm x 7.0mm dimensions.
   */
  protected void drawPcbAntenna(Graphics2D g2d, double x, double y) {
    drawPcbAntenna(g2d, x, y, ANTENNA_WIDTH.convertToPixels(), ANTENNA_LENGTH.convertToPixels());
  }

  /**
   * Helper to draw a standard SMD tactile push button (housing + circular actuator).
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the button body
   * @param y Top-left Y coordinate of the button body
   * @param w Width of the button body
   * @param h Height of the button body
   */
  protected void drawButton(Graphics2D g2d, double x, double y, double w, double h) {
    g2d.setColor(BUTTON_BODY_COLOR);
    g2d.fill(new RoundRectangle2D.Double(x, y, w, h, 2, 2));
    g2d.setColor(BUTTON_BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(new RoundRectangle2D.Double(x, y, w, h, 2, 2));

    double actuatorD = Math.min(w, h) * 0.55;
    g2d.setColor(BUTTON_ACTUATOR_COLOR);
    g2d.fill(new Ellipse2D.Double(x + (w - actuatorD) / 2.0, y + (h - actuatorD) / 2.0, actuatorD, actuatorD));
  }

  /**
   * Helper to draw a standard SMD tactile push button with default 3.5mm x 3.0mm dimensions.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the button body
   * @param y Top-left Y coordinate of the button body
   */
  protected void drawButton(Graphics2D g2d, double x, double y) {
    drawButton(g2d, x, y, BUTTON_WIDTH.convertToPixels(), BUTTON_LENGTH.convertToPixels());
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
