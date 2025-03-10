/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
package org.diylc.components.electromechanical;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Switch (Latching)", author = "Branislav Stojkovic",
    category = "Schematic Symbols", instanceNamePrefix = "SW",
    description = "Schematic symbol of various types of latching switches",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE,
    keywordTag = "Schematic")
public class SwitchLatchingSymbol
    extends AbstractComponent<SwitchLatchingSymbol.SwitchConfiguration> implements ISwitch {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;
  public static Size SIZE = new Size(0.15d, SizeUnit.in);
  public static Size THROW_SPACING = new Size(0.1d, SizeUnit.in);
  public static Size SPACING = new Size(0.3d, SizeUnit.in);
  public static Size TERMINAL_SIZE = new Size(0.04d, SizeUnit.in);
  public static int ARC_ANGLE = 24;

  private Orientation orientation = Orientation.DEFAULT;
  private SwitchConfiguration configuration = SwitchConfiguration._2x2;
  private PoleCount poleCount = PoleCount.ONE;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  public SwitchLatchingSymbol() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {

    double terminalSize = getClosestOdd(TERMINAL_SIZE);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((float) (terminalSize / 4)));

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = 0; i < controlPoints.length; i++) {

      Ellipse2D.Double terminal = new Ellipse2D.Double(controlPoints[i].getX() - terminalSize / 2,
          controlPoints[i].getY() - terminalSize / 2, terminalSize, terminalSize);

      g2d.setColor(
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : COLOR);
      g2d.fill(terminal);
    }
    drawingObserver.stopTrackingContinuityArea();

    double theta = orientation.toRadians();

    g2d.setColor(
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : COLOR);

    // draw latch
    int throwCount = configuration.getThrowCount();
    for (int j = 0; j < poleCount.getCount(); j++) {
      Point2D startPoint = new Point2D.Double(controlPoints[(1 + throwCount) * j].getX() ,
          controlPoints[(1 + throwCount) * j].getY());

      double offsetX;
      double offsetY;
      if ("SHORT".equalsIgnoreCase(configuration.getInBetween())) {
        offsetX = terminalSize;
        offsetY = 0;
      } else {
        offsetX = terminalSize * 2 / 3;
        offsetY = terminalSize / 2;
      }

      Point2D endPoint = new Point2D.Double(
          controlPoints[(1 + throwCount) * j + 1].getX() + Math.cos(theta + Math.PI) * offsetX
              + Math.cos(theta + Math.PI * 3 / 2) * offsetY,
          controlPoints[(1 + throwCount) * j + 1].getY() + Math.sin(theta + Math.PI) * offsetX
              + Math.sin(theta + Math.PI * 3 / 2) * offsetY);

      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((float) (terminalSize / 4)));
      
      Line2D latch =
          new Line2D.Double(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
      g2d.draw(latch);

      if ("SHORT".equalsIgnoreCase(configuration.getInBetween())) {
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((float) (terminalSize / 2)));

        double d = startPoint.distance(endPoint) + terminalSize / 3;
        double latchAngle =
            Math.atan2(endPoint.getY() - startPoint.getY(), endPoint.getX() - startPoint.getX());
        int startAngle = (int) Math.toDegrees(-latchAngle) - ARC_ANGLE / 2;

        g2d.drawArc((int) (startPoint.getX() - d), (int) (startPoint.getY() - d), (int) (d * 2),
            (int) (d * 2), startAngle, ARC_ANGLE);
      }
    }
    if (poleCount.getCount() > 1) {
      g2d.setStroke(ObjectCache.getInstance().fetchStroke(1f, new float[] {4f, 6f}, 0f, BasicStroke.CAP_SQUARE));
      Line2D line = new Line2D.Double(
          (controlPoints[0].getX() + controlPoints[1].getX()) / 2, 
          (controlPoints[0].getY() + controlPoints[1].getY()) / 2, 
          (controlPoints[(1 + throwCount) * (poleCount.getCount() - 1)].getX() + 
              controlPoints[(1 + throwCount) * (poleCount.getCount() - 1) + 1].getX()) / 2,
          (controlPoints[(1 + throwCount) * (poleCount.getCount() - 1)].getY() + 
              controlPoints[(1 + throwCount) * (poleCount.getCount() - 1) + 1].getY()) / 2);
      g2d.draw(line);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 3 * width / 32;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR);

    int diameter = 5;
    g2d.drawOval(margin, width / 2 - diameter / 2, diameter, diameter);
    g2d.drawOval(width - margin - diameter, (int) (width / 2 - 1.5 * diameter), diameter, diameter);
    g2d.drawOval(width - margin - diameter, (int) (width / 2 + 0.5 * diameter), diameter, diameter);

    g2d.drawLine(margin + diameter, width / 2 - diameter / 2, width - margin - diameter - 1,
        (int) (width / 2 - 1.5 * diameter));
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;

    updateControlPoints();
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @EditableProperty(name = "Configuration")
  @Override
  public SwitchConfiguration getValue() {
    return configuration;
  }

  @Override
  public void setValue(SwitchConfiguration value) {
    this.configuration = value;

    updateControlPoints();
  }

  @EditableProperty(name = "Poles")
  public PoleCount getPoleCount() {
    return poleCount;
  }

  public void setPoleCount(PoleCount poleCount) {
    this.poleCount = poleCount;

    updateControlPoints();
  }

  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];

    int throwCount = configuration.getThrowCount();
    double spacing = SPACING.convertToPixels();
    double throwSpacing = THROW_SPACING.convertToPixels();

    controlPoints = new Point2D[(1 + throwCount) * poleCount.getCount()];
    
    double theta = orientation.toRadians();

    for (int j = 0; j < poleCount.getCount(); j++) {
      Point2D commonPoint = new Point2D.Double(firstPoint.getX() + Math.cos(theta + Math.PI / 2) * j * (throwCount + 1) * throwSpacing,
          firstPoint.getY() + Math.sin(theta + Math.PI / 2) * j * (throwCount + 1) * throwSpacing);
      controlPoints[(1 + throwCount) * j] = commonPoint;
      
      AffineTransform tx = null;
      if (theta != 0) {
        tx = AffineTransform.getRotateInstance(theta, commonPoint.getX(), commonPoint.getY());
      }

      for (int i = 0; i < throwCount; i++) {
        double x = commonPoint.getX() + spacing;
        double y = commonPoint.getY() - (throwCount - 1) * throwSpacing / 2 + throwSpacing * i;
        Point2D point = new Point2D.Double(x, y);
        if (tx != null) {
          tx.transform(point, point);
        }
        controlPoints[(1 + throwCount) * j + i + 1] = point;
      }
    }
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public int getPositionCount() {
    return configuration.getPositionCount();
  }

  @Override
  public String getPositionName(int position) {
    return Integer.toString(position + 1);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    if ("OFF".equals(configuration.getInBetween())) {
      if (position % 2 == 1)
        return false;
      
      return index1 % (configuration.getThrowCount() + 1) == 0 && index2 == index1 + position / 2 + 1;  
    } else if ("SHORT".equals(configuration.getInBetween())) {
      if (position % 2 == 1)
        return index1 % (configuration.getThrowCount() + 1) == 0 && (index2 == index1 + position / 2 + 1 || index2 == index1 + position / 2 + 2);
      
      return index1 % (configuration.getThrowCount() + 1) == 0 && index2 == index1 + position / 2 + 1;
    }
    return index1 % (configuration.getThrowCount() + 1) == 0 && index2 == index1 + position + 1;    
  }

  public static enum SwitchConfiguration {
    _1x2, _2x2, _2x3xOFF, _3x3, _3x5xSHORT, _4x4, _5x5, _6x6, _7x7, _8x8, _9x9, _10x10, _11x11, _12x12;

    private int throwCount;
    private int positionCount;
    private String inBetween;

    private SwitchConfiguration() {
      String[] split = this.name().replaceAll("_", "").split("x");
      this.throwCount = Integer.parseInt(split[0]);
      this.positionCount = Integer.parseInt(split[1]);
      if (split.length > 2) {
        inBetween = split[2];
      }
    }

    public int getPositionCount() {
      return positionCount;
    }

    public int getThrowCount() {
      return throwCount;
    }

    public String getInBetween() {
      return inBetween;
    }

    @Override
    public String toString() {
      return throwCount + " throws / " + positionCount + " positions"
          + (inBetween == null ? "" : " / " + inBetween + " even positions");
    }
  }

  public static enum PoleCount {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    private int count;

    private PoleCount(int count) {
      this.count = count;
    }

    public int getCount() {
      return count;
    }

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
}
