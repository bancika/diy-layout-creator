/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAngledComponent;
import org.diylc.components.transform.AngledComponentTransformer;
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
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Rotary Switch (Sealed)", author = "Branislav Stojkovic", category = "Electro-Mechanical",
    instanceNamePrefix = "SW", description = "Sealed plastic rotary switch in several different switching configurations",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true,
    transformer = AngledComponentTransformer.class)
public class RotarySwitchSealed extends AbstractAngledComponent<RotarySwitchSealedType> implements ISwitch {

  private static final int OUTER_ANGLE_OFFSET = 15;
  private static final int INNER_ANGLE_OFFSET = 30;

  private static final Size DIAMETER = new Size(26d, SizeUnit.mm);
  private static final Size OUTER_PIN_DIAMETER = new Size(21.5d, SizeUnit.mm);
  private static final Size INNER_PIN_DIAMETER = new Size(8d, SizeUnit.mm);

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#F7F7EF");
  private static Color LABEL_COLOR = BODY_COLOR.darker();

  public static Size PIN_DIAMETER = new Size(1d, SizeUnit.mm);
  private static Size PIN_WIDTH = new Size(0.08d, SizeUnit.in);
  private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);

  private RotarySwitchSealedType configuration = RotarySwitchSealedType._4P3T;
  private Color color = BODY_COLOR;
  private Mount mount = Mount.CHASSIS;
  private SwitchTiming timing;
  private Color labelColor = LABEL_COLOR;
  private Color pinColor = METAL_COLOR;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private double[] pointAngles;

  public RotarySwitchSealed() {
    super();
    updateControlPoints();
  }

  @EditableProperty(name = "Type")
  public RotarySwitchSealedType getValue() {
    return configuration;
  }

  public void setValue(RotarySwitchSealedType configuration) {
    this.configuration = configuration;
    updateControlPoints();
    // Reset body shape
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    if (color == null) {
      color = BODY_COLOR;
    }
    return color;
  }
  
  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int outerPinCount = 12;
    int innerPinCount = configuration.getPoleCount();
    int outerPinDiameter = getClosestOdd(OUTER_PIN_DIAMETER.convertToPixels());
    int innerPinDiameter = getClosestOdd(INNER_PIN_DIAMETER.convertToPixels());
    
    double outerAngleIncrement = - Math.PI * 2 / outerPinCount;
    double innerAngleIncrement = - Math.PI * 2 / innerPinCount;
    double outerAngleOffset = Math.toRadians(OUTER_ANGLE_OFFSET);
    double innerAngleOffset = Math.toRadians(INNER_ANGLE_OFFSET);

    controlPoints = new Point2D[outerPinCount + innerPinCount + 1];
    pointAngles = new double[controlPoints.length];
    
    double theta = outerAngleOffset + getAngle().getValueRad();
    controlPoints[0] = firstPoint;
    
    for (int i = 0; i < outerPinCount; i++) {
      controlPoints[i + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * outerPinDiameter / 2,
              firstPoint.getY() + Math.sin(theta) * outerPinDiameter / 2);
      pointAngles[i + 1] = theta;
      theta += outerAngleIncrement;
    }
    
    theta = innerAngleOffset + getAngle().getValueRad();
    for (int i = 0; i < innerPinCount; i++) {
      controlPoints[i + outerPinCount + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * innerPinDiameter / 2,
              firstPoint.getY() + Math.sin(theta) * innerPinDiameter / 2);
      pointAngles[i + outerPinCount + 1] = theta;
      theta += innerAngleIncrement;
    }
  }

  public Shape[] getBody() {
    if (body == null) {
      int bodyDiameter = getClosestOdd(DIAMETER.convertToPixels());      
      
      Ellipse2D baseShape =
          new Ellipse2D.Double(controlPoints[0].getX() - bodyDiameter / 2, controlPoints[0].getY() - bodyDiameter / 2,
              bodyDiameter, bodyDiameter);      
      
      body = new Shape[] { baseShape };
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    // Draw body
    Shape[] body = getBody();
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    if (componentState != ComponentState.DRAGGING) {
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getColor());
      g2d.fill(body[0]);
    }
    g2d.setComposite(oldComposite);
    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : getColor().darker();
    }
    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    
    drawingObserver.stopTracking();
    
    // Draw pins
    if (!outlineMode) {      
      for (int i = 1; i < controlPoints.length; i++) {
        Shape pinShape;
        if (getMount() == Mount.PCB) {
          int pinSize = getClosestOdd(PIN_DIAMETER.convertToPixels());
          pinShape = new Ellipse2D.Double(controlPoints[i].getX() - pinSize / 2, controlPoints[i].getY() - pinSize / 2, pinSize, pinSize);
        } else {
          int pinWidth = getClosestOdd(PIN_WIDTH.convertToPixels());
          int pinThickness = getClosestOdd(PIN_THICKNESS.convertToPixels());
          pinShape = new Rectangle2D.Double(controlPoints[i].getX() - pinWidth / 2, controlPoints[i].getY() - pinThickness / 2, pinWidth, pinThickness);
          double theta = Math.atan2(controlPoints[i].getY() - controlPoints[0].getY(), controlPoints[i].getX() - controlPoints[0].getX()) + Math.PI / 2;
          Area rotatedPin = new Area(pinShape);
          rotatedPin.transform(AffineTransform.getRotateInstance(theta, controlPoints[i].getX(), controlPoints[i].getY()));
          pinShape = rotatedPin;
        }
        g2d.setColor(pinColor);
        if (getMount() == Mount.CHASSIS)
          drawingObserver.startTrackingContinuityArea(true);        
        g2d.fill(pinShape);
        drawingObserver.stopTrackingContinuityArea();
        g2d.setColor(pinColor.darker());
        g2d.draw(pinShape);
      }
    }
    
    int outerPinDiameter = getClosestOdd(OUTER_PIN_DIAMETER.convertToPixels());
    int innerPinDiameter = getClosestOdd(INNER_PIN_DIAMETER.convertToPixels());
    int outerPinCount = 12;
    int innerPinCount = configuration.getPoleCount();
    g2d.setColor(labelColor);
    double relativeLabelLocation = 0.85;
    Point2D firstPoint = controlPoints[0];
    for (int i = 0; i < outerPinCount; i++) {
      int x = (int) (firstPoint.getX() + Math.cos(pointAngles[i + 1]) * relativeLabelLocation * outerPinDiameter / 2);
      int y = (int) (firstPoint.getY() + Math.sin(pointAngles[i + 1]) * relativeLabelLocation * outerPinDiameter / 2);
      StringUtils.drawCenteredText(g2d, getControlPointNodeName(i + 1), x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
    relativeLabelLocation = 0.65;

    for (int i = 0; i < innerPinCount; i++) {
      int x = (int) (firstPoint.getX() + Math.cos(pointAngles[i + outerPinCount + 1]) * relativeLabelLocation * innerPinDiameter / 2);
      int y = (int) (firstPoint.getY() + Math.sin(pointAngles[i + outerPinCount + 1]) * relativeLabelLocation * innerPinDiameter / 2);
      StringUtils.drawCenteredText(g2d, getControlPointNodeName(i + outerPinCount + 1), x, y, 
          HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {    
    g2d.setColor(BODY_COLOR);
    g2d.fillOval(0, 0, width * 2, width * 2);
    g2d.setColor(BODY_COLOR.darker());
    g2d.drawOval(0, 0, width * 2, width * 2);      
    
    int commonX = (int) (width - 8 * width / 32d);
    int commonY = (int) (height - 8 * width / 32d);
    g2d.setColor(pinColor);
    g2d.fillOval(commonX - 1, commonY - 1, 3, 3);
    g2d.setColor(pinColor.darker());
    g2d.drawOval(commonX - 1, commonY - 1, 3, 3);
    
    int radius = (int) (width - 6 * width / 32d);
    for (int i = 0; i < 4; i++) {
      int x = (int) (width + Math.cos(1.1 * Math.PI + i * Math.PI / 6) * radius);
      int y = (int) (height + Math.sin(1.1 * Math.PI + i * Math.PI / 6) * radius);
      g2d.setColor(pinColor);
      g2d.fillOval(x - 1, y - 1, 3, 3);
      g2d.setColor(pinColor.darker());
      g2d.drawOval(x - 1, y - 1, 3, 3);
    }
    
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
    g2d.drawLine(0, height - 1, width, height - 1);
    g2d.drawLine(width - 1, 0, width - 1, height - 1);
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
  
  @EditableProperty(name = "Mount")
  public Mount getMount() {
    if (mount == null)
      mount = Mount.CHASSIS;
    return mount;
  }
  
  public void setMount(Mount mount) {
    this.mount = mount;
    
    updateControlPoints();
    // Reset body shape
    body = null;
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
  
  @Override
  public Rectangle2D getCachingBounds() {
    Rectangle2D rect = getBody()[0].getBounds2D();
    int margin = 40; // to catch the pins that are outside the body, if needed
    return new Rectangle2D.Double(rect.getX() - margin, rect.getY() - margin, rect.getWidth() + 2 * margin, rect.getHeight() + 2 * margin);
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
  
  @Override
  public String getControlPointNodeName(int index) {
    int outerPinCount = 12;

    if (index <= outerPinCount) {
      return Integer.toString(index);
    }

    int labelStep = 1;
    if (configuration.getPoleCount() == 2) {      
        labelStep = 2; 
    }

    return Character.toString((char)('A' + labelStep * (index - outerPinCount - 1)));
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    if (index2 < 13)
      return false;
    
    int pole = index2 - 13;
    return index1 == 1 + pole * 12 / configuration.getPoleCount() + position;
  }
}
