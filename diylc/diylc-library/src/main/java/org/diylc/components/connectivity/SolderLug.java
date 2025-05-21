/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.connectivity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.TwoCircleTangent;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Solder Lug", category = "Connectivity", author = "Branislav Stojkovic",
    description = "Locking terminal lug commonly used for chassis ground connections",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SL", enableCache = true)
public class SolderLug extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;

  private static Size BODY_LARGE_DIAMETER = new Size(7.92d, SizeUnit.mm);
  private static Size BODY_SMALL_DIAMETER = new Size(3.66d, SizeUnit.mm);
  private static Size LARGE_HOLE_DIAMETER = new Size(3.66d, SizeUnit.mm);
  private static Size CENTER_TO_HOLE1 = new Size(8.4d, SizeUnit.mm);
  private static Size CENTER_TO_HOLE2 = new Size(10.4d, SizeUnit.mm);
  private static Size HOLE_DIAMETER = new Size(1.5d, SizeUnit.mm);

  private String value = "";
  private Point2D[] controlPoints =
      new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient Area[] body;
  private Orientation orientation = Orientation.DEFAULT;

  public SolderLug() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
    g2d.fill(body[0]);

    g2d.setComposite(oldComposite);

    Color finalBorderColor;

    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    
    g2d.setColor(Constants.TRANSPARENT_COLOR);
    drawingObserver.startTrackingContinuityArea(true);
    g2d.fill(body[1]);
    drawingObserver.stopTrackingContinuityArea();
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      
      int bodyLargeDiameter = getClosestOdd(BODY_LARGE_DIAMETER.convertToPixels());
      int bodySmallDiameter = getClosestOdd(BODY_SMALL_DIAMETER.convertToPixels());
      int largeHoleDiameter = getClosestOdd(LARGE_HOLE_DIAMETER.convertToPixels());
      int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());

      Area area = new TwoCircleTangent(controlPoints[0], controlPoints[2], bodyLargeDiameter / 2,
          bodySmallDiameter / 2);
      
      Area conductivityMask = new Area(area);
      
      area.subtract(new Area(new Ellipse2D.Double(controlPoints[1].getX() - holeDiameter / 2, controlPoints[1].getY() - holeDiameter / 2,
          holeDiameter, holeDiameter)));
      area.subtract(new Area(new Ellipse2D.Double(controlPoints[2].getX() - holeDiameter / 2, controlPoints[2].getY() - holeDiameter / 2,
          holeDiameter, holeDiameter)));
      
      area.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - largeHoleDiameter / 2,
          controlPoints[0].getY() - largeHoleDiameter / 2, largeHoleDiameter, largeHoleDiameter)));

      body[0] = area;
      body[1] = conductivityMask;
    }

    return body;
  }

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();

    int centerToHole1 = (int) CENTER_TO_HOLE1.convertToPixels();
    int centerToHole2 = (int) CENTER_TO_HOLE2.convertToPixels();

    double centerY1 = y + centerToHole1;
    double centerY2 = y + centerToHole2;

    controlPoints[1].setLocation(x, centerY1);
    controlPoints[2].setLocation(x, centerY2);

    // Rotate if needed
    if (orientation != Orientation.DEFAULT) {
      double theta = 0;
      switch (orientation) {
        case _90:
          theta = Math.PI / 2;
          break;
        case _180:
          theta = Math.PI;
          break;
        case _270:
          theta = Math.PI * 3 / 2;
          break;
      }

      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      rotation.transform(controlPoints[1], controlPoints[1]);
      rotation.transform(controlPoints[2], controlPoints[2]);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double x0 = width * 0.35;
    double y0 = height * 0.65;
    double x1 = width * 0.75;
    double y1 = height * 0.25;
    
    TwoCircleTangent main = new TwoCircleTangent(new Point2D.Double(x0, y0),
        new Point2D.Double(x1, y1), width * 0.28, width * 0.12);
    
    main.subtract(new Area(new Ellipse2D.Double(x0 - 3, y0 - 3, 7, 7)));    
    main.subtract(new Area(new Ellipse2D.Double(x1 - 2, y1 - 1, 3, 3)));
    main.subtract(new Area(new Ellipse2D.Double(x1 - 6, y1 + 3, 3, 3)));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BASE_COLOR);
    g2d.fill(main);
    g2d.setColor(BASE_COLOR.darker());
    g2d.draw(main);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + (index == 0 ? "Tip" : "Sleeve");
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    int margin = 20;
    Rectangle2D bounds = getBody()[0].getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
        bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }

  @Deprecated
  @Override
  public Integer getFontSizeOverride() {
    return super.getFontSizeOverride();
  }
}
