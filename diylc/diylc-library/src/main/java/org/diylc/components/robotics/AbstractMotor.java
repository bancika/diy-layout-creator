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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

/**
 * Base abstract class for robotics motors (servos, steppers, DC motors).
 */
public abstract class AbstractMotor extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color SHAFT_COLOR = Color.decode("#E0E0E0");
  public static Color SHAFT_BORDER_COLOR = Color.decode("#A0A0A0");
  public static Color METAL_BODY_COLOR = Color.decode("#8899A6");
  public static Color BRASS_COLOR = Color.decode("#D4AF37");
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();

  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);

  public static Font SILK_FONT_SMALL = new Font("SansSerif", Font.PLAIN, 8);
  public static Font SILK_FONT = new Font("SansSerif", Font.BOLD, 9);

  protected Orientation orientation = Orientation.DEFAULT;
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Color bodyColor = METAL_BODY_COLOR;

  public AbstractMotor() {
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

  @EditableProperty(name = "Body Color")
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

  protected void drawPins(Graphics2D g2d, int startIndex, int count, boolean female, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    int pinPx = (int) Math.round(PIN_SIZE.convertToPixels());
    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
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
