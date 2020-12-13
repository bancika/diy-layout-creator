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
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.transform.PotentiometerTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
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

@ComponentDescriptor(name = "Rotary Selector Switch", author = "Branislav Stojkovic",
    category = "Electro-Mechanical", creationMethod = CreationMethod.SINGLE_CLICK,
    instanceNamePrefix = "SW",
    description = "Single pole rotary switch, typically used for impedance selector",
    zOrder = IDIYComponent.COMPONENT, transformer = PotentiometerTransformer.class,
    enableCache = true)
public class RotarySelectorSwitch extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color TERMINAL_COLOR = Color.lightGray;
  private static Color BODY_COLOR = Color.decode("#555555");

  private static Size OUTER_DIAMETER = new Size(34d, SizeUnit.mm);
  private static Size HOLE_DISTANCE = new Size(20d, SizeUnit.mm);
  private static Size INNER_DIAMETER = new Size(20d, SizeUnit.mm);
  private static Size TERMINAL_LENGTH = new Size(23d, SizeUnit.mm);
  private static Size TERMINAL_WIDTH = new Size(4.8d, SizeUnit.mm);
  private static Size HOLE_DIAMETER = new Size(2.5d, SizeUnit.mm);

  private String value = "";
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0),
      new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient Area[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private boolean showLabels = true;
  private RotaryPositionCount positionCount = RotaryPositionCount.THREE;

  public RotarySelectorSwitch() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    // if (componentState != ComponentState.DRAGGING) {
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    g2d.fill(body[0]);

    g2d.setComposite(oldComposite);
    // }

    Color finalBorderColor;

    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BODY_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : TERMINAL_COLOR);

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = 2; i < body.length; i++)
      g2d.fill(body[i]);
    drawingObserver.stopTrackingContinuityArea();

    Color finalTerminalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalTerminalBorderColor = theme.getOutlineColor();
    } else {
      finalTerminalBorderColor = TERMINAL_COLOR.darker();
    }

    drawingObserver.stopTracking();

    g2d.setColor(finalTerminalBorderColor);
    for (int i = 2; i < body.length; i++)
      g2d.draw(body[i]);

    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    g2d.fill(body[1]);
    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);

    g2d.setComposite(oldComposite);

    // draw labels
    if (showLabels) {
      g2d.setColor(TERMINAL_COLOR.darker());
      g2d.setFont(project.getFont().deriveFont(project.getFont().getSize2D() * 0.8f));
      
      // Override font size
      if (getFontSizeOverride() != null)
        g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
      
      for (int i = 2; i < body.length; i++) {
        Rectangle2D bounds = body[i].getBounds2D();
        double x = (int) ((bounds.getCenterX() + controlPoints[i - 2].getX()) / 2);
        double y = (int) ((bounds.getCenterY() + controlPoints[i - 2].getY()) / 2);
        StringUtils.drawCenteredText(g2d, i == 2 ? "C" : String.valueOf(i - 2), x, y,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[3 + getPositionCount().getCount()];

      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      int outerDiameter = getClosestOdd(OUTER_DIAMETER.convertToPixels());
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      int springLength = (int) TERMINAL_LENGTH.convertToPixels();
      int springWidth = (int) TERMINAL_WIDTH.convertToPixels();
      int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      int holeDistance = (int) HOLE_DISTANCE.convertToPixels();

      double centerY = y - holeDistance;

      Area main = new Area(new Ellipse2D.Double(x - outerDiameter / 2, centerY - outerDiameter / 2,
          outerDiameter, outerDiameter));

      body[0] = main;

      Area inner = new Area(new Ellipse2D.Double(x - innerDiameter / 2, centerY - innerDiameter / 2,
          innerDiameter, innerDiameter));

      body[1] = inner;

      for (int i = 0; i < getPositionCount().getCount() + 1; i++) {
        Area terminal = new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeDistance,
            springWidth, springLength, springWidth, springWidth));
        terminal.subtract(new Area(new Ellipse2D.Double(x - holeDiameter / 2, y - holeDiameter / 2,
            holeDiameter, holeDiameter)));
        terminal.transform(AffineTransform.getRotateInstance(-i * Math.PI / 4, x, centerY));
        terminal.subtract(inner);
        body[2 + i] = terminal;
      }

      // Rotate if needed
      double theta = getOrientation().toRadians();
      if (theta != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        // Skip the last one because it's already rotated
        for (int i = 0; i < body.length; i++) {
          Shape shape = body[i];
          Area area = (Area) shape;
          if (area != null) {
            area.transform(rotation);
          }
        }
      }
    }

    return body;
  }

  private void updateControlPoints() {
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();

    int holeDistance = (int) HOLE_DISTANCE.convertToPixels();

    double centerY = y - holeDistance;

    for (int i = 1; i < controlPoints.length; i++) {
      double theta = -i * Math.PI / 4;
      controlPoints[i].setLocation(x, y);
      AffineTransform.getRotateInstance(theta, x, centerY).transform(controlPoints[i],
          controlPoints[i]);
    }

    double theta = getOrientation().toRadians();
    // Rotate if needed
    if (theta != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point2D point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyDiameter = 19 * width / 32;
    int innerDiameter = 11 * width / 32;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BODY_COLOR);
    g2d.fillOval(width / 2 - bodyDiameter / 2, height / 2 - bodyDiameter / 2, bodyDiameter,
        bodyDiameter);
    g2d.setColor(BODY_COLOR.darker());
    g2d.drawOval(width / 2 - bodyDiameter / 2, height / 2 - bodyDiameter / 2, bodyDiameter,
        bodyDiameter);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(4f * width / 32));
    g2d.setColor(TERMINAL_COLOR);
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);

    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);

    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BODY_COLOR);
    g2d.fillOval(width / 2 - innerDiameter / 2, height / 2 - innerDiameter / 2 - 1, innerDiameter,
        innerDiameter);
    g2d.setColor(BODY_COLOR.darker());
    g2d.drawOval(width / 2 - innerDiameter / 2, height / 2 - innerDiameter / 2 - 1, innerDiameter,
        innerDiameter);
  }

  @Override
  public int getControlPointCount() {
    return 1 + getPositionCount().getCount();
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index < 1 + getPositionCount().getCount();
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

  @EditableProperty(name = "Labels")
  public boolean getShowLabels() {
    return showLabels;
  }

  public void setShowLabels(boolean showLabels) {
    this.showLabels = showLabels;
  }

  @EditableProperty(name = "Positions")
  public RotaryPositionCount getPositionCount() {
    return positionCount;
  }

  public void setPositionCount(RotaryPositionCount positionCount) {
    this.positionCount = positionCount;
    updateControlPoints();
    // Invalidate the body
    body = null;
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
    if (index == 0)
      return "Common";
    return String.valueOf(index);
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    Area area = new Area();
    Area[] body = getBody();
    int margin = 20;
    for (Area a : body)
      if (a != null)
        area.add(a);
    Rectangle2D bounds = area.getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
        bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }


  public static enum RotaryPositionCount {
    TWO(2), THREE(3), FOUR(4);

    private int count;

    private RotaryPositionCount(int value) {
      count = value;
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
