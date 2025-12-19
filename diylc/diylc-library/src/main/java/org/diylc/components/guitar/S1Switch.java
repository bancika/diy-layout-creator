/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "S1 Switch", category = "Guitar", author = "Branislav Stojkovic",
    description = "Fender S1 4 pole pushbutton switch", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "SW", enableCache = true)
public class S1Switch extends AbstractTransparentComponent<Void> implements ISwitch {

  private static final long serialVersionUID = 1L;

  private static Size DIAMETER = new Size(1d, SizeUnit.in);
  private static Size SPACING = new Size(4.5d, SizeUnit.mm);
  private static Size PAD_DIAMETER = new Size(0.08d, SizeUnit.in);
  private static Size HOLE_DIAMETER = new Size(0.03d, SizeUnit.in);
  private static Size Y_OFFSET = new Size(-0.05d, SizeUnit.in); // Align S1 with panel potentiometer body to make it easier

  private static Color BODY_COLOR = FR4_COLOR;
  private static Color BORDER_COLOR = BODY_COLOR.darker();
  private static Color PAD_COLOR = COPPER_COLOR;
  public static Color HOLE_COLOR = Color.white;
  public static Color LABEL_COLOR = Color.white;
  public static Color LINE_COLOR = Color.white;

  protected Point2D[] controlPoints = new Point2D[] {
      new Point2D.Double(0, 0) };
  
  transient protected Shape body;
  
  private boolean showMarkers = true;

  public S1Switch() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = SPACING.convertToPixels();
    
    double yOffset = Y_OFFSET.convertToPixels();
    
    controlPoints = new Point2D[13];
    
    controlPoints[0] = firstPoint;
    
    controlPoints[1] = new Point2D.Double(firstPoint.getX() - spacing, firstPoint.getY() - 2 * spacing + yOffset);
    controlPoints[2] = new Point2D.Double(firstPoint.getX(), firstPoint.getY() - 2 * spacing + yOffset);
    controlPoints[3] = new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() - 2 * spacing + yOffset);
    
    controlPoints[4] = new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() - spacing + yOffset);
    controlPoints[5] = new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + yOffset);
    controlPoints[6] = new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + spacing + yOffset);
    
    controlPoints[7] = new Point2D.Double(firstPoint.getX() - spacing, firstPoint.getY() + 2 * spacing + yOffset);
    controlPoints[8] = new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing + yOffset);
    controlPoints[9] = new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 2 * spacing + yOffset);
    
    controlPoints[10] = new Point2D.Double(firstPoint.getX() - 2 * spacing, firstPoint.getY() - spacing + yOffset);
    controlPoints[11] = new Point2D.Double(firstPoint.getX() - 2 * spacing, firstPoint.getY() + yOffset);
    controlPoints[12] = new Point2D.Double(firstPoint.getX() - 2 * spacing, firstPoint.getY() + spacing + yOffset);
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index > 0;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    
    body = null;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {    
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Shape body = getBody();
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    // Draw body if available.
    if (body != null) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
      g2d.fill(body);
      drawingObserver.stopTracking();
      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      Color finalBorderColor;
      if (outlineMode) {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
                : BORDER_COLOR;
      }
      g2d.setColor(finalBorderColor);
      g2d.draw(body);
    }
    // Do not track these changes because the whole switch has been tracked
    // so far.
    drawingObserver.stopTracking();
    // Draw lugs.
    
    int padDiameter = getClosestOdd((int) PAD_DIAMETER.convertToPixels());
    int holeDiameter = getClosestOdd((int) HOLE_DIAMETER.convertToPixels());
    
    for (int i = 1; i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      if (outlineMode) {
        g2d.setColor(theme.getOutlineColor());
        g2d.drawOval((int)(p.getX() - padDiameter / 2), (int)(p.getY() - padDiameter / 2), padDiameter, padDiameter);
      } else {
        g2d.setColor(PAD_COLOR);
        drawingObserver.startTrackingContinuityArea(true);
        g2d.fillOval((int)(p.getX() - padDiameter / 2), (int)(p.getY() - padDiameter / 2), padDiameter, padDiameter);
        drawingObserver.stopTrackingContinuityArea();
        g2d.setColor(PAD_COLOR.darker());
        g2d.drawOval((int)(p.getX() - padDiameter / 2), (int)(p.getY() - padDiameter / 2), padDiameter, padDiameter);
        g2d.setColor(HOLE_COLOR);
        g2d.fillOval((int)(p.getX() - holeDiameter / 2), (int)(p.getY() - holeDiameter / 2), holeDiameter, holeDiameter);
      }
      
      if (!showMarkers || i % 3 == 2) {
        continue;
      }
      
      double dy = 0;
      if (i % 3 == 1) {
        dy = 1;
      } else if (i % 3 == 0) {
        dy = -1;
      }
      
      Stroke stroke = g2d.getStroke();
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
      g2d.setColor(LINE_COLOR);
      g2d.drawLine((int)(p.getX() - padDiameter / 3), (int)(p.getY() + dy * padDiameter * 4 / 5), 
          (int)(p.getX() + padDiameter / 3), (int)(p.getY() + dy * padDiameter * 4 / 5));
      g2d.setStroke(stroke);
    }
    
    double yOffset = Y_OFFSET.convertToPixels();
    
    g2d.setFont(g2d.getFont().deriveFont(1.5f * g2d.getFont().getSize()));
    
    g2d.setColor(LABEL_COLOR);
    StringUtils.drawCenteredText(g2d, getName(), controlPoints[0].getX(), controlPoints[0].getY() + yOffset, 
        HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  public Shape getBody() {
    if (body == null) {
      int diameter = getClosestOdd(DIAMETER.convertToPixels());
      double yOffset = Y_OFFSET.convertToPixels();
      body =
          new Ellipse2D.Double(controlPoints[0].getX() - diameter / 2, controlPoints[0].getY() - diameter / 2 + yOffset,
              diameter, diameter);
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int circleSize = 5 * width / 32;
    int holeSize = 1;
    g2d.setColor(BODY_COLOR);
    g2d.fillOval(0, 0, width * 2, width * 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval(0, 0, width * 2, width * 2);
    for (int i = 1; i <= 2; i++) {
      g2d.setColor(PAD_COLOR);
      g2d.fillOval(width / 3 - circleSize / 2, i * height / 4 + height / 2 - circleSize / 2 - 2, circleSize, circleSize);
      g2d.fillOval(i * width / 4 + width / 2 - circleSize / 2 - 2, height / 3 - circleSize / 2, circleSize, circleSize);
      g2d.setColor(HOLE_COLOR);
      g2d.fillOval(width / 3 - holeSize / 2, i * height / 4 + height / 2 - holeSize / 2 - 2, holeSize, holeSize);
      g2d.fillOval(i * width / 4 + width / 2 - holeSize / 2 - 2, height / 3 - holeSize / 2, holeSize, holeSize);
    }
    
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize2D() * 0.8f));
    StringUtils.drawCenteredText(g2d, "S1", width - 2, height - 4, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
    
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
    g2d.drawLine(0, height - 1, width, height - 1);
    g2d.drawLine(width - 1, 0, width - 1, height - 1);
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public int getPositionCount() {    
    return 2;
  }

  @Override
  public String getPositionName(int position) {    
    if (position == 0) {
      return "Down";
    }
    return "Up";
  }
  
  @EditableProperty(name = "Markers")
  public boolean getShowMarkers() {
    return showMarkers;
  }
  
  public void setShowMarkers(boolean showMarkers) {
    this.showMarkers = showMarkers;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    if ((index1 == 1 && index2 == 2 && position == 0)
        || (index1 == 4 && index2 == 5 && position == 0)
        || (index1 == 7 && index2 == 8 && position == 0)
        || (index1 == 10 && index2 == 11 && position == 0)) {
      return true;
    }
    
    if ((index1 == 2 && index2 == 3 && position == 1)
        || (index1 == 5 && index2 == 6 && position == 1)
        || (index1 == 8 && index2 == 9 && position == 1)
        || (index1 == 11 && index2 == 12 && position == 1)) {
      return true;
    }
    
    return false;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    int margin = 20;
    Rectangle2D bounds = getBody().getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin, bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
}
