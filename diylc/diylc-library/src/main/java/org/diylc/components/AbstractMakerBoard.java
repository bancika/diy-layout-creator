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
