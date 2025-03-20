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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.awt.TwoCircleTangent;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAngledComponent;
import org.diylc.components.transform.AngledComponentTransformer;
import org.diylc.core.Angle;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.AreaUtils;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Rotary Switch (Open)", author = "Branislav Stojkovic",
    category = "Electro-Mechanical", instanceNamePrefix = "SW",
    description = "Open rotary switch in several different switching configurations",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true,
    transformer = AngledComponentTransformer.class)
public class RotarySwitchOpen extends AbstractAngledComponent<RotarySwitchOpenType>
    implements ISwitch, IGerberComponentSimple {

  private static final int ANGLE_OFFSET = -15;
  private static final int INNER_COMMON_ANGLE_OFFSET = -90;

  public static Size LARGE_DIAMETER = new Size(25d, SizeUnit.mm);
  public static Size SMALL_DIAMETER = new Size(11d, SizeUnit.mm);
  public static Size SMALL_DISTANCE = new Size(20d, SizeUnit.mm);
  public static Size HOLE_DISTANCE = new Size(26d, SizeUnit.mm);
  public static Size BODY_HOLE_SIZE = new Size(3d, SizeUnit.mm);
  public static Size TERMINAL_HOLE_SIZE = new Size(1d, SizeUnit.mm);

  private static final Size INNER_PIN_SPACING = new Size(20d, SizeUnit.mm);
  private static final Size OUTER_PIN_SPACING = new Size(30d, SizeUnit.mm);
  private static final Size INNER_COMMON_PIN_SPACING = new Size(11d, SizeUnit.mm);
  private static final Size OUTER_COMMON_PIN_SPACING = new Size(32d, SizeUnit.mm);

  private static final long serialVersionUID = 1L;

  public static Size PIN_DIAMETER = new Size(1d, SizeUnit.mm);
  private static Size PIN_WIDTH = new Size(0.08d, SizeUnit.in);
  private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);

  private RotarySwitchOpenType configuration = RotarySwitchOpenType._2P6T;
  private Color color = PHENOLIC_DARK_COLOR;
  private SwitchTiming timing;
  private Color labelColor = Color.white;
  private Color pinColor = METAL_COLOR;
  private boolean showMarkers = true;
  private Angle angleOffset = Angle.of(ANGLE_OFFSET);

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private double[] pointAngles;

  public RotarySwitchOpen() {
    super();
    updateControlPoints();
  }

  @EditableProperty(name = "Type")
  public RotarySwitchOpenType getValue() {
    return configuration;
  }

  public void setValue(RotarySwitchOpenType configuration) {
    this.configuration = configuration;
    updateControlPoints();
    // Reset body shape
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int innerPinCount = 2 * configuration.getPositionCount();
    int commonPinCount = 2;
    int innerPinSpacing = getClosestOdd(INNER_PIN_SPACING.convertToPixels());
    int innerCommonPinSpacing = getClosestOdd(INNER_COMMON_PIN_SPACING.convertToPixels());
    int outerPinSpacing = getClosestOdd(OUTER_PIN_SPACING.convertToPixels());
    int outerCommonPinSpacing = getClosestOdd(OUTER_COMMON_PIN_SPACING.convertToPixels());

    double angleIncrement = -Math.PI / 6;
    double commonAngleIncrement = -Math.PI;
    double angleOffset = this.angleOffset.getValueRad();
    double innerCommonAngleOffset = Math.toRadians(INNER_COMMON_ANGLE_OFFSET);
    double outerCommonAngleOffset = Math.toRadians(INNER_COMMON_ANGLE_OFFSET);
    double angleRad = Math.toRadians(getAngle().getValue());

    controlPoints =
        new Point2D[1 + (innerPinCount + commonPinCount) * configuration.getPoleCount() / 2];
    pointAngles = new double[controlPoints.length];

    controlPoints[0] = firstPoint;

    double theta = angleOffset + angleRad;

    for (int i = 0; i < configuration.getPositionCount(); i++) {
      controlPoints[i + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * innerPinSpacing / 2,
              firstPoint.getY() + Math.sin(theta) * innerPinSpacing / 2);
      pointAngles[i + 1] = theta;
      theta += angleIncrement;
    }

    theta = angleOffset + angleRad - Math.PI;

    for (int i = 0; i < configuration.getPositionCount(); i++) {
      controlPoints[i + configuration.getPositionCount() + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * innerPinSpacing / 2,
              firstPoint.getY() + Math.sin(theta) * innerPinSpacing / 2);
      pointAngles[i + configuration.getPositionCount() + 1] = theta;
      theta += angleIncrement;
    }

    theta = innerCommonAngleOffset + angleRad;
    for (int i = 0; i < commonPinCount; i++) {
      controlPoints[i + innerPinCount + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * innerCommonPinSpacing / 2,
              firstPoint.getY() + Math.sin(theta) * innerCommonPinSpacing / 2);
      pointAngles[i + innerPinCount + 1] = theta;
      theta += commonAngleIncrement;
    }

    if (configuration.getNeedsSecondLevel()) {
      int secondLevelStart = commonPinCount + innerPinCount + 1;

      theta = angleOffset + angleRad;

      if (configuration.getPositionCount() == 5) {
        theta += angleIncrement;
      }

      for (int i = 0; i < configuration.getPositionCount(); i++) {
        controlPoints[secondLevelStart + i] =
            new Point2D.Double(firstPoint.getX() + Math.cos(theta) * outerPinSpacing / 2,
                firstPoint.getY() + Math.sin(theta) * outerPinSpacing / 2);
        pointAngles[secondLevelStart + i] = theta;
        theta += angleIncrement;
      }

      theta = angleOffset + angleRad - Math.PI;

      for (int i = 0; i < configuration.getPositionCount(); i++) {
        controlPoints[secondLevelStart + i + configuration.getPositionCount()] =
            new Point2D.Double(firstPoint.getX() + Math.cos(theta) * outerPinSpacing / 2,
                firstPoint.getY() + Math.sin(theta) * outerPinSpacing / 2);
        pointAngles[secondLevelStart + i + configuration.getPositionCount()] = theta;
        theta += angleIncrement;
      }

      theta = outerCommonAngleOffset + angleRad;
      for (int i = 0; i < commonPinCount; i++) {
        controlPoints[secondLevelStart + i + innerPinCount] =
            new Point2D.Double(firstPoint.getX() + Math.cos(theta) * outerCommonPinSpacing / 2,
                firstPoint.getY() + Math.sin(theta) * outerCommonPinSpacing / 2);
        pointAngles[secondLevelStart + i + innerPinCount] = theta;
        theta += commonAngleIncrement;
      }
    }
  }

  public Shape[] getBody() {
    if (body == null) {
      List<Area> bodyList = new ArrayList<Area>();
      int largeDiameter = getClosestOdd(LARGE_DIAMETER.convertToPixels());
      int smallDiameter = getClosestOdd(SMALL_DIAMETER.convertToPixels());
      int smallDistance = getClosestOdd(SMALL_DISTANCE.convertToPixels());
      int holeDistance = getClosestOdd(HOLE_DISTANCE.convertToPixels());
      int holeSize = getClosestOdd(BODY_HOLE_SIZE.convertToPixels());
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();

      TwoCircleTangent left = new TwoCircleTangent(new Point2D.Double(x, y),
          new Point2D.Double(x - smallDistance / 2, y), largeDiameter / 2, smallDiameter / 2);
      TwoCircleTangent right = new TwoCircleTangent(new Point2D.Double(x, y),
          new Point2D.Double(x + smallDistance / 2, y), largeDiameter / 2, smallDiameter / 2);

      Area bodyArea = new Area(left);
      bodyArea.add(right);

      bodyArea.subtract(new Area(new Ellipse2D.Double(x - holeDistance / 2 - holeSize / 2,
          y - holeSize / 2, holeSize, holeSize)));
      bodyArea.subtract(new Area(new Ellipse2D.Double(x + holeDistance / 2 - holeSize / 2,
          y - holeSize / 2, holeSize, holeSize)));

      if (angle != 0) {
        AffineTransform tx = AffineTransform.getRotateInstance(Math.toRadians(angle), x, y);
        bodyArea.transform(tx);
      }

      bodyList.add(bodyArea);


      int pinWidth = getClosestOdd(PIN_WIDTH.convertToPixels());
      int pinThickness = getClosestOdd(PIN_THICKNESS.convertToPixels());

      // inner terminals and commons
      for (int i = 1; i < Math
          .ceil(controlPoints.length / (configuration.getNeedsSecondLevel() ? 2.0 : 1.0)); i++) {
        Shape pinShape;

        pinShape = new Rectangle2D.Double(controlPoints[i].getX() - pinWidth / 2,
            controlPoints[i].getY() - pinThickness / 2, pinWidth, pinThickness);
        double theta = Math.atan2(controlPoints[i].getY() - controlPoints[0].getY(),
            controlPoints[i].getX() - controlPoints[0].getX()) + Math.PI / 2;
        Area rotatedPin = new Area(pinShape);
        rotatedPin.transform(AffineTransform.getRotateInstance(theta, controlPoints[i].getX(),
            controlPoints[i].getY()));

        bodyList.add(rotatedPin);
      }

      if (configuration.getNeedsSecondLevel()) {
        int innerPinCount = 2 * configuration.getPositionCount();
        int commonPinCount = 2;

        double outerCommonAngleOffset = Math.toRadians(INNER_COMMON_ANGLE_OFFSET);
        int outerPinSpacing = getClosestOdd(OUTER_PIN_SPACING);
        int outerCommonPinSpacing = getClosestOdd(OUTER_COMMON_PIN_SPACING);
        int terminalHoleSize = getClosestOdd(TERMINAL_HOLE_SIZE);

        int secondLevelStart = commonPinCount + innerPinCount + 1;

        // outer terminals
        for (int i = 0; i < configuration.getPositionCount() * 2; i++) {
          Area terminalArea = new Area(new RoundRectangle2D.Double(x, y - pinWidth / 2,
              outerPinSpacing / 2 + pinWidth / 2, pinWidth, pinWidth / 2, pinWidth / 2));
          AffineTransform tx =
              AffineTransform.getRotateInstance(pointAngles[secondLevelStart + i], x, y);

          terminalArea.transform(tx);
          terminalArea.subtract(bodyArea);
          terminalArea.subtract(new Area(new Ellipse2D.Double(
              controlPoints[secondLevelStart + i].getX() - terminalHoleSize / 2,
              controlPoints[secondLevelStart + i].getY() - terminalHoleSize / 2, terminalHoleSize,
              terminalHoleSize)));
          bodyList.add(terminalArea);
        }

        // outer common terminals
        Area terminalArea =
            new Area(new RoundRectangle2D.Double(x - outerCommonPinSpacing / 2 - pinWidth / 2,
                y - pinWidth / 2, outerCommonPinSpacing + pinWidth, pinWidth, pinWidth / 2,
                pinWidth / 2));
        AffineTransform tx = AffineTransform.getRotateInstance(outerCommonAngleOffset, x, y);

        terminalArea.transform(tx);
        terminalArea.subtract(bodyArea);
        terminalArea.subtract(new Area(new Ellipse2D.Double(
            controlPoints[controlPoints.length - 1].getX() - terminalHoleSize / 2,
            controlPoints[controlPoints.length - 1].getY() - terminalHoleSize / 2, terminalHoleSize,
            terminalHoleSize)));
        terminalArea.subtract(new Area(new Ellipse2D.Double(
            controlPoints[controlPoints.length - 2].getX() - terminalHoleSize / 2,
            controlPoints[controlPoints.length - 2].getY() - terminalHoleSize / 2, terminalHoleSize,
            terminalHoleSize)));

        bodyList.addAll(AreaUtils.tryAreaBreakout(terminalArea));
      }

      body = bodyList.toArray(new Shape[0]);
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Shape[] body = getBody();

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : getColor().darker();
    }

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    int commonPinCount = 2;
    int secondLevelStart = 2 * configuration.getPositionCount() + commonPinCount;

    // Draw outer pins
    if (configuration.getNeedsSecondLevel()) {
      drawingObserver.startTrackingContinuityArea(true);
      for (int i = secondLevelStart; i < body.length; i++) {
        g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : pinColor);
        g2d.fill(body[i]);
        g2d.setColor(outlineMode ? finalBorderColor : pinColor.darker());
        g2d.draw(body[i]);
      }
      drawingObserver.stopTrackingContinuityArea();
    }

    // Draw body
    if (componentState != ComponentState.DRAGGING) {
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getColor());
      g2d.fill(body[0]);
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    drawingObserver.stopTracking();

    // Draw pins
    for (int i = 1; i <= secondLevelStart; i++) {
      drawingObserver.startTrackingContinuityArea(true);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : pinColor);
      g2d.fill(body[i]);
      drawingObserver.stopTrackingContinuityArea();
      if (!outlineMode) {
        g2d.setColor(outlineMode ? finalBorderColor : pinColor.darker());
        g2d.draw(body[i]);
      }
    }

    g2d.setComposite(oldComposite);

    // draw labels
    // StringUtils.drawCenteredText(g2d, name, controlPoints[0].getX(), controlPoints[0].getY(),
    // HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    if (showMarkers) {
      g2d.setColor(labelColor);
      int innerPinSpacing = getClosestOdd(INNER_PIN_SPACING.convertToPixels());
      int innerCommonPinSpacing = getClosestOdd(INNER_COMMON_PIN_SPACING.convertToPixels());
      int outerPinCount = 2 * configuration.getPositionCount();
      double relativeLabelLocation = 0.85;
      Point2D firstPoint = controlPoints[0];
      for (int i = 0; i < outerPinCount; i++) {
        int x = (int) (firstPoint.getX()
            + Math.cos(pointAngles[i + 1]) * relativeLabelLocation * innerPinSpacing / 2);
        int y = (int) (firstPoint.getY()
            + Math.sin(pointAngles[i + 1]) * relativeLabelLocation * innerPinSpacing / 2);
        StringUtils.drawCenteredText(g2d, getControlPointNodeNameForRender(i + 1), x, y,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
      relativeLabelLocation = 0.65;
      for (int i = 0; i < commonPinCount; i++) {
        int x = (int) (firstPoint.getX() + Math.cos(pointAngles[i + outerPinCount + 1])
            * relativeLabelLocation * innerCommonPinSpacing / 2);
        int y = (int) (firstPoint.getY() + Math.sin(pointAngles[i + outerPinCount + 1])
            * relativeLabelLocation * innerCommonPinSpacing / 2);
        StringUtils.drawCenteredText(g2d, getControlPointNodeNameForRender(i + outerPinCount + 1),
            x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }

      if (configuration.getNeedsSecondLevel()) {
        relativeLabelLocation = 0.90;
        int outerPinSpacing = getClosestOdd(OUTER_PIN_SPACING.convertToPixels());
        int outerCommonPinSpacing = getClosestOdd(OUTER_COMMON_PIN_SPACING.convertToPixels());

        for (int i = 0; i < outerPinCount; i++) {
          int x = (int) (firstPoint.getX() + Math.cos(pointAngles[i + 1 + secondLevelStart])
              * relativeLabelLocation * outerPinSpacing / 2);
          int y = (int) (firstPoint.getY() + Math.sin(pointAngles[i + 1 + secondLevelStart])
              * relativeLabelLocation * outerPinSpacing / 2);
          StringUtils.drawCenteredText(g2d,
              getControlPointNodeNameForRender(i + 1 + secondLevelStart), x, y,
              HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        }
        // relativeLabelLocation = 0.90;
        for (int i = 0; i < commonPinCount; i++) {
          int x = (int) (firstPoint.getX()
              + Math.cos(pointAngles[i + outerPinCount + 1 + secondLevelStart])
                  * relativeLabelLocation * outerCommonPinSpacing / 2);
          int y = (int) (firstPoint.getY()
              + Math.sin(pointAngles[i + outerPinCount + 1 + secondLevelStart])
                  * relativeLabelLocation * outerCommonPinSpacing / 2);
          StringUtils.drawCenteredText(g2d,
              getControlPointNodeNameForRender(i + outerPinCount + 1 + secondLevelStart), x, y,
              HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        }
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    int largeR = getClosestOdd(width * 3d / 8);
    int smallR = getClosestOdd(width / 6d);
    int hole = 4 * width / 32;

    Area area = new TwoCircleTangent(new Point2D.Double(width * 0.5, height * 0.5),
        new Point2D.Double(width / 2, height / 8d), largeR, smallR);
    area.add((Area) new TwoCircleTangent(new Point2D.Double(width * 0.5, height * 0.5),
        new Point2D.Double(width / 2, height * 7 / 8d), largeR, smallR));

    area.subtract(
        new Area(new Ellipse2D.Double((width - hole) / 2, height / 8 - hole / 2, hole, hole)));
    area.subtract(
        new Area(new Ellipse2D.Double((width - hole) / 2, height * 7 / 8 - hole / 2, hole, hole)));
    area.transform(AffineTransform.getRotateInstance(Math.PI / 4, width / 2, height / 2));
    g2d.setColor(color);
    g2d.fill(area);
    g2d.setColor(color.darker());
    g2d.draw(area);

    int center = width / 2 + 1;
    int radius = width / 2 - 7;
    for (int i = 0; i < 10; i++) {
      int x = (int) (center + Math.cos(Math.PI / 10 + i * Math.PI / 5) * radius);
      int y = (int) (center + Math.sin(Math.PI / 10 + i * Math.PI / 5) * radius);
      g2d.setColor(pinColor);
      g2d.fillOval(x - 1, y - 1, 2, 2);
      g2d.setColor(pinColor.darker());
      g2d.drawOval(x - 1, y - 1, 2, 2);
    }
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
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index > 0;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);

    body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null)
      labelColor = LABEL_COLOR;
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty
  public SwitchTiming getTiming() {
    return timing;
  }

  public void setTiming(SwitchTiming timing) {
    this.timing = timing;
  }

  @EditableProperty(name = "Terminals")
  public Color getPinColor() {
    return pinColor;
  }

  public void setPinColor(Color pinColor) {
    this.pinColor = pinColor;
  }

  @EditableProperty(name = "Markers")
  public boolean getShowMarkers() {
    return showMarkers;
  }

  public void setShowMarkers(boolean showMarkers) {
    this.showMarkers = showMarkers;
  }
  
  @EditableProperty(name = "Angle Offset")
  public Angle getAngleOffset() {
    return angleOffset;
  }
  
  public void setAngleOffset(Angle angleOffset) {
    this.angleOffset = angleOffset;
    updateControlPoints();
    // Reset body shape
    body = null;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] body = getBody();
    double minX = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;
    for (Shape shape : body) {
      Rectangle2D rect = shape.getBounds2D();
      minX = Math.min(rect.getMinX(), minX);
      maxX = Math.max(rect.getMaxX(), maxX);
      minY = Math.min(rect.getMinY(), minY);
      maxY = Math.max(rect.getMaxY(), maxY);
    }

    int margin = 40; // to catch the pins that are outside the body, if needed
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin,
        maxY - minY + 2 * margin);
  }

  public static enum Mount {
    CHASSIS("Chassis"), PCB("PCB");

    String name;

    private Mount(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @Override
  public int getPositionCount() {
    return configuration.getPositionCount();
  }

  @Override
  public String getPositionName(int position) {
    return Integer.toString(position + 1);
  }

  public String getControlPointNodeNameForRender(int index) {
    int commonPinCount = 2;
    int positionCount = configuration.getPositionCount();

    if (index <= positionCount) {
      return Integer.toString(positionCount + 1 - index);
    }
    if (index <= 2 * positionCount) {
      return Integer.toString(2 * positionCount + 1 - index);
    }
    if (index <= 2 * positionCount + commonPinCount) {
      return Character.toString((char) ('A' + (index - 2 * positionCount - 1)));
    }
    int secondLevelStart = 2 * positionCount + commonPinCount;
    if (index <= positionCount + secondLevelStart) {
      return Integer.toString(positionCount + 1 - index + secondLevelStart);
    }
    if (index <= 2 * positionCount + secondLevelStart) {
      return Integer.toString(2 * positionCount + 1 - index + secondLevelStart);
    }
    if (index <= 2 * positionCount + commonPinCount + secondLevelStart) {
      return Character.toString((char) ('C' + (index - 2 * positionCount - 1 - secondLevelStart)));
    }
    return null;
  }

  @Override
  public String getControlPointNodeName(int index) {
    int commonPinCount = 2;
    int positionCount = configuration.getPositionCount();

    if (index <= positionCount) {
      return "A" + (positionCount + 1 - index);
    }
    if (index <= 2 * positionCount) {
      return "B" + (2 * positionCount + 1 - index);
    }
    if (index <= 2 * positionCount + commonPinCount) {
      return Character.toString((char) ('A' + (index - 2 * positionCount - 1)));
    }

    int secondLevelStart = 2 * positionCount + commonPinCount;
    if (index <= positionCount + secondLevelStart) {
      return "C" + (positionCount + 1 - index + secondLevelStart);
    }
    if (index <= 2 * positionCount + secondLevelStart) {
      return "D" + (2 * positionCount + 1 - index + secondLevelStart);
    }
    if (index <= 2 * positionCount + commonPinCount + secondLevelStart) {
      return Character.toString((char) ('C' + (index - 2 * positionCount - 1 - secondLevelStart)));
    }
    return null;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    if (index1 == 0)
      return false;

    int commonPinCount = 2;
    int positionCount = configuration.getPositionCount();

    if (index1 <= positionCount) {
      return index1 == positionCount - position && index2 == 2 * positionCount + 1;
    }
    if (index1 <= 2 * positionCount) {
      return index1 == 2 * positionCount - position && index2 == 2 * positionCount + 2;
    }
    if (index1 <= 2 * positionCount + commonPinCount) {
      return false;
    }

    if (configuration.getNeedsSecondLevel()) {
      int secondLevelStart = 2 * positionCount + commonPinCount;
      if (index1 <= positionCount + secondLevelStart) {
        return index1 == secondLevelStart + positionCount - position
            && index2 == 2 * positionCount + 1 + secondLevelStart;
      }
      if (index2 <= 2 * positionCount + secondLevelStart + commonPinCount) {
        return index1 == secondLevelStart + 2 * positionCount - position
            && index2 == 2 * positionCount + 2 + secondLevelStart;
      }
    }
    return false;
  }
}
