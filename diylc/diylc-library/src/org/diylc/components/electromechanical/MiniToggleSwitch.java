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
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
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
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Mini Toggle Switch", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Panel mounted mini toggle switch", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "SW", autoEdit = false)
public class MiniToggleSwitch extends AbstractTransparentComponent<ToggleSwitchType> implements ISwitch {

  private static final long serialVersionUID = 1L;

  private static Size SPACING = new Size(0.2d, SizeUnit.in);
  private static Size MARGIN = new Size(0.08d, SizeUnit.in);
  private static Size CIRCLE_SIZE = new Size(0.09d, SizeUnit.in);
  private static Size LUG_WIDTH = new Size(0.060d, SizeUnit.in);
  private static Size LUG_THICKNESS = new Size(0.02d, SizeUnit.in);

  private static Color BODY_COLOR = Color.decode("#3299CC");
  private static Color BORDER_COLOR = BODY_COLOR.darker();
  private static Color CIRCLE_COLOR = Color.decode("#FFFFAA");

  protected Point[] controlPoints = new Point[] {new Point(0, 0)};
  transient protected Shape body;
  protected String name;
  protected ToggleSwitchType switchType = ToggleSwitchType.DPDT;
  private OrientationHV orientation = OrientationHV.VERTICAL;
  private Size spacing = SPACING;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;

  public MiniToggleSwitch() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    int spacing = (int) getSpacing().convertToPixels();
    switch (switchType) {
      case SPST:
        controlPoints = new Point[] {firstPoint, new Point(firstPoint.x, firstPoint.y + spacing)};
        break;
      case SPDT:
      case SPDT_off:
        controlPoints =
            new Point[] {firstPoint, new Point(firstPoint.x, firstPoint.y + spacing),
                new Point(firstPoint.x, firstPoint.y + 2 * spacing)};
        break;
      case DPDT:
      case DPDT_off:
        controlPoints =
            new Point[] {firstPoint, new Point(firstPoint.x, firstPoint.y + spacing),
                new Point(firstPoint.x, firstPoint.y + 2 * spacing), new Point(firstPoint.x + spacing, firstPoint.y),
                new Point(firstPoint.x + spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing)};
        break;
      case _DP3T_mustang:
        controlPoints =
                new Point[] {firstPoint,
                        new Point(firstPoint.x, firstPoint.y + spacing),
                        new Point(firstPoint.x, firstPoint.y + 2 * spacing),
                        new Point(firstPoint.x, firstPoint.y + 3 * spacing),
                        new Point(firstPoint.x + spacing, firstPoint.y),
                        new Point(firstPoint.x + spacing, firstPoint.y + spacing),
                        new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
                        new Point(firstPoint.x + spacing, firstPoint.y + 3 * spacing)};
        break;
      case _3PDT:
      case _3PDT_off:
        controlPoints =
            new Point[] {firstPoint, new Point(firstPoint.x, firstPoint.y + spacing),
                new Point(firstPoint.x, firstPoint.y + 2 * spacing), new Point(firstPoint.x + spacing, firstPoint.y),
                new Point(firstPoint.x + spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y + 2 * spacing)};
        break;
      case _4PDT:
      case _4PDT_off:
        controlPoints =
            new Point[] {firstPoint, new Point(firstPoint.x, firstPoint.y + spacing),
                new Point(firstPoint.x, firstPoint.y + 2 * spacing), new Point(firstPoint.x + spacing, firstPoint.y),
                new Point(firstPoint.x + spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y + 2 * spacing),
                new Point(firstPoint.x + 3 * spacing, firstPoint.y),
                new Point(firstPoint.x + 3 * spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + 3 * spacing, firstPoint.y + 2 * spacing)};
        break;
      case _5PDT:
      case _5PDT_off:
        controlPoints =
            new Point[] {firstPoint, new Point(firstPoint.x, firstPoint.y + spacing),
                new Point(firstPoint.x, firstPoint.y + 2 * spacing), new Point(firstPoint.x + spacing, firstPoint.y),
                new Point(firstPoint.x + spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + spacing, firstPoint.y + 2 * spacing),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + 2 * spacing, firstPoint.y + 2 * spacing),
                new Point(firstPoint.x + 3 * spacing, firstPoint.y),
                new Point(firstPoint.x + 3 * spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + 3 * spacing, firstPoint.y + 2 * spacing),
                new Point(firstPoint.x + 4 * spacing, firstPoint.y),
                new Point(firstPoint.x + 4 * spacing, firstPoint.y + spacing),
                new Point(firstPoint.x + 4 * spacing, firstPoint.y + 2 * spacing)};
        break;
    }
    AffineTransform xform = AffineTransform.getRotateInstance(-Math.PI / 2, firstPoint.x, firstPoint.y);
    if (getOrientation() == OrientationHV.HORIZONTAL) {
      for (int i = 1; i < controlPoints.length; i++) {
        xform.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
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
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    // Reset body shape.
    body = null;
  }

  @EditableProperty
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @EditableProperty(name = "Type")
  @Override
  public ToggleSwitchType getValue() {
    return switchType;
  }

  @Override
  public void setValue(ToggleSwitchType value) {
    this.switchType = value;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty
  public OrientationHV getOrientation() {
    if (orientation == null) {
      orientation = OrientationHV.VERTICAL;
    }
    return orientation;
  }

  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSpacing() {
    if (spacing == null) {
      spacing = SPACING;
    }
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
    updateControlPoints();
    // Reset body shape.
    body = null;
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
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
      g2d.fill(body);
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
                : getBorderColor();
      }
      g2d.setColor(finalBorderColor);
      g2d.draw(body);
    }
    // Do not track these changes because the whole switch has been tracked
    // so far.
    drawingObserver.stopTracking();
    // Draw lugs.
    int circleDiameter = getClosestOdd((int) CIRCLE_SIZE.convertToPixels());
    int lugWidth = getClosestOdd((int) LUG_WIDTH.convertToPixels());
    int lugHeight = getClosestOdd((int) LUG_THICKNESS.convertToPixels());
    for (Point p : controlPoints) {
      if (outlineMode) {
        g2d.setColor(theme.getOutlineColor());
        g2d.drawRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
      } else {
        g2d.setColor(CIRCLE_COLOR);
        g2d.fillOval(p.x - circleDiameter / 2, p.y - circleDiameter / 2, circleDiameter, circleDiameter);
        g2d.setColor(METAL_COLOR);
        g2d.fillRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
      }
    }
  }

  public Shape getBody() {
    if (body == null) {
      Point firstPoint = controlPoints[0];
      int margin = (int) MARGIN.convertToPixels();
      int spacing = (int) getSpacing().convertToPixels();
      switch (switchType) {
        case SPST:
          body =
              new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin, 2 * margin
                  + spacing, margin, margin);
          break;
        case SPDT:
        case SPDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin, 2 * margin + 2
                  * spacing, margin, margin);
          break;
        case DPDT:
        case DPDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin + spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
        case _DP3T_mustang:
          body =
                  new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin + spacing, 2
                          * margin + 3 * spacing, margin, margin);
          break;
        case _3PDT:
        case _3PDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin + 2 * spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
        case _4PDT:
        case _4PDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin + 3 * spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
        case _5PDT:
        case _5PDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.x - margin, firstPoint.y - margin, 2 * margin + 4 * spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
      }
      if (getOrientation() == OrientationHV.HORIZONTAL) {
        AffineTransform xform = AffineTransform.getRotateInstance(-Math.PI / 2, firstPoint.x, firstPoint.y);
        body = new Area(body);
        ((Area) body).transform(xform);
      }
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int circleSize = 5 * width / 32;
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect(width / 4, 1, width / 2, height - 2, circleSize, circleSize);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect(width / 4, 1, width / 2, height - 2, circleSize, circleSize);
    for (int i = 1; i <= 3; i++) {
      g2d.setColor(CIRCLE_COLOR);
      g2d.fillOval(width / 2 - circleSize / 2, i * height / 4 - 3, circleSize, circleSize);
      g2d.setColor(METAL_COLOR);
      g2d.drawLine(width / 2 - circleSize / 2 + 1, i * height / 4 - 1, width / 2 + circleSize / 2 - 1, i * height / 4
          - 1);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null)
      bodyColor = BODY_COLOR;
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null)
      borderColor = BORDER_COLOR;
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
  
  // switch stuff
  
  @Override
  public String getControlPointNodeName(int index) {
    // we don't want the switch to produce any nodes, it just makes connections
    return null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public int getPositionCount() {
    switch (switchType) {
      case SPST:        
      case SPDT:
      case DPDT:
      case _3PDT:
      case _4PDT:
      case _5PDT:
        return 2;        
      case _DP3T_mustang:             
      case SPDT_off:
      case DPDT_off:
      case _3PDT_off:
      case _4PDT_off:
      case _5PDT_off:
        return 3;      
    }
    return 2;
  }

  @Override
  public String getPositionName(int position) {
    if (switchType.name().endsWith("_off") && position == 2)
      return "OFF";
    return "ON" + Integer.toString(position + 1);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    switch (switchType) {
      case SPST:
        return position == 0;
      case SPDT:        
      case DPDT:
      case _3PDT:
      case _4PDT:
      case _5PDT:
        return (index2 - index1) < 3 && index1 % 3 == position && index2 % 3 == position + 1;        
      case SPDT_off:        
      case DPDT_off:
      case _3PDT_off:
      case _4PDT_off:
      case _5PDT_off:
        return position != 2 && (index2 - index1) < 3 && index1 % 3 == position && index2 % 3 == position + 1; 
      case _DP3T_mustang:
        return (index2 - index1) < 3 && index1 % 3 == 0 && index2 % 3 == position + 1;      
    }
    return false;
  }
}
