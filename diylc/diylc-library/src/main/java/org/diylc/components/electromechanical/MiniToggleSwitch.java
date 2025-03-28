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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.guitar.ToggleSwitchPositionPropertyValueSource;
import org.diylc.components.transform.MiniToggleSwitchTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ILayeredComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.DynamicEditableProperty;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

import static org.diylc.utils.SwitchUtils.getConnectedTerminals;

@ComponentDescriptor(name = "Mini Toggle Switch", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Panel mounted mini toggle switch", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "SW", autoEdit = false, enableCache = true, transformer = MiniToggleSwitchTransformer.class)
public class MiniToggleSwitch extends AbstractTransparentComponent<ToggleSwitchType> implements ISwitch, ILayeredComponent, IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  private static Size SPACING = new Size(0.2d, SizeUnit.in);
  private static Size MARGIN = new Size(0.08d, SizeUnit.in);
  private static Size CIRCLE_SIZE = new Size(0.09d, SizeUnit.in);
  private static Size LUG_WIDTH = new Size(0.060d, SizeUnit.in);
  private static Size LUG_THICKNESS = new Size(0.02d, SizeUnit.in);

  private static Color BODY_COLOR = Color.decode("#3299CC");
  private static Color BORDER_COLOR = BODY_COLOR.darker();
  private static Color CIRCLE_COLOR = Color.decode("#287ba4");
  private static Color TERMINAL_COLOR = LIGHT_METAL_COLOR;

  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  transient protected Shape body;
  protected ToggleSwitchType switchType = ToggleSwitchType.DPDT;
  private OrientationHV orientation = OrientationHV.VERTICAL;
  private Size spacing = SPACING;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color terminalPadColor = CIRCLE_COLOR;
  private Color terminalColor = TERMINAL_COLOR;

  private Integer selectedPosition;
  private Boolean highlightConnectedTerminals;
  
  @Deprecated
  protected String name;

  public MiniToggleSwitch() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int spacing = (int) getSpacing().convertToPixels();
    switch (switchType) {
      case SPST:
        controlPoints = new Point2D[] {firstPoint, new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing)};
        break;
      case SPDT:
      case SPDT_off:
        controlPoints =
            new Point2D[] {firstPoint, new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing)};
        break;
      case DPDT:
      case DPDT_off:
      case DPDT_ononon_1:
      case DPDT_ononon_2:
        controlPoints =
            new Point2D[] {firstPoint, new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing), 
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 2 * spacing)};
        break;
      case _DP3T_mustang:
        controlPoints =
                new Point2D[] {firstPoint,
                        new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing),
                        new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing),
                        new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 3 * spacing),
                        new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY()),
                        new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + spacing),
                        new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 2 * spacing),
                        new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 3 * spacing)};
        break;
      case _3PDT:
      case _3PDT_off:
        controlPoints =
            new Point2D[] {firstPoint, new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing), 
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 2 * spacing),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + 2 * spacing)};
        break;
      case _4PDT:
      case _4PDT_off:
      case _4PDT_ononon_1:
      case _4PDT_ononon_2:
        controlPoints =
            new Point2D[] {firstPoint, new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing), 
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 2 * spacing),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + 2 * spacing),
                new Point2D.Double(firstPoint.getX() + 3 * spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + 3 * spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + 3 * spacing, firstPoint.getY() + 2 * spacing)};
        break;
      case _5PDT:
      case _5PDT_off:
        controlPoints =
            new Point2D[] {firstPoint, new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX(), firstPoint.getY() + 2 * spacing), 
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + spacing, firstPoint.getY() + 2 * spacing),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + 2 * spacing, firstPoint.getY() + 2 * spacing),
                new Point2D.Double(firstPoint.getX() + 3 * spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + 3 * spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + 3 * spacing, firstPoint.getY() + 2 * spacing),
                new Point2D.Double(firstPoint.getX() + 4 * spacing, firstPoint.getY()),
                new Point2D.Double(firstPoint.getX() + 4 * spacing, firstPoint.getY() + spacing),
                new Point2D.Double(firstPoint.getX() + 4 * spacing, firstPoint.getY() + 2 * spacing)};
        break;
    }
    AffineTransform xform = AffineTransform.getRotateInstance(-Math.PI / 2, firstPoint.getX(), firstPoint.getY());
    if (getOrientation() == OrientationHV.HORIZONTAL) {
      for (int i = 1; i < controlPoints.length; i++) {
        xform.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }

  @Override
  public Point2D getControlPoint(int index) {
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    // Reset body shape.
    body = null;
  }

  @EditableProperty(defaultable = false)
  @Override
  public String getName() {
    if (super.name == null || super.name.trim().isEmpty()) {
      super.name = name;
      name = null;
    }    
    return super.name;
  }

  @Override
  public void setName(String name) {
    super.name = name;
    this.name = null;
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
    
    int lugWidth;
    int lugHeight;
    
    if (getOrientation() == OrientationHV.HORIZONTAL) {
      lugHeight = getClosestOdd((int) LUG_WIDTH.convertToPixels());
      lugWidth = getClosestOdd((int) LUG_THICKNESS.convertToPixels());
    } else {
      lugWidth = getClosestOdd((int) LUG_WIDTH.convertToPixels());
      lugHeight = getClosestOdd((int) LUG_THICKNESS.convertToPixels());  
    }

    List<Set<Integer>> connectedTerminals = getConnectedTerminals(this, controlPoints.length);

    for (int i = 0; i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      if (outlineMode) {
//        g2d.setColor(Constants.TRANSPARENT_COLOR);
//        drawingObserver.startTrackingContinuityArea(true);
//        g2d.fillRect((int)(p.getX() - lugWidth / 2), (int)(p.getY() - lugHeight / 2), lugWidth, lugHeight);
//        drawingObserver.stopTrackingContinuityArea();
        
        g2d.setColor(theme.getOutlineColor());
        g2d.drawRect((int)(p.getX() - lugWidth / 2d), (int)(p.getY() - lugHeight / 2d), lugWidth, lugHeight);
      } else {
        int finalI = i;
        int groupIndex = IntStream.range(0, connectedTerminals.size())
            .filter(j -> connectedTerminals.get(j).contains(finalI))
            .findFirst().orElse(-1);

        if (groupIndex < 0) {
          g2d.setColor(getTerminalPadColor().darker());
        } else {
          g2d.setColor(ISwitch.POLE_COLORS[groupIndex].darker());
        }

        g2d.drawOval((int)(p.getX() - circleDiameter / 2d), (int)(p.getY() - circleDiameter / 2d), circleDiameter, circleDiameter);


        if (groupIndex < 0) {
          g2d.setColor(getTerminalPadColor());
        } else {
          g2d.setColor(ISwitch.POLE_COLORS[groupIndex]);
        }
        g2d.fillOval((int)(p.getX() - circleDiameter / 2d), (int)(p.getY() - circleDiameter / 2d), circleDiameter, circleDiameter);
        if (groupIndex < 0) {
          g2d.setColor(getTerminalColor());
        } else {
          g2d.setColor(getTerminalColor().darker());
        }
//        drawingObserver.startTrackingContinuityArea(true);
        g2d.fillRect((int)(p.getX() - lugWidth / 2d), (int)(p.getY() - lugHeight / 2d), lugWidth, lugHeight);
//        drawingObserver.stopTrackingContinuityArea();
      }
    }
  }

  public Shape getBody() {
    if (body == null) {
      Point2D firstPoint = controlPoints[0];
      int margin = (int) MARGIN.convertToPixels();
      int spacing = (int) getSpacing().convertToPixels();
      switch (switchType) {
        case SPST:
          body =
              new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin, 2 * margin
                  + spacing, margin, margin);
          break;
        case SPDT:
        case SPDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin, 2 * margin + 2
                  * spacing, margin, margin);
          break;
        case DPDT:
        case DPDT_off:
        case DPDT_ononon_1:
        case DPDT_ononon_2:
          body =
              new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin + spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
        case _DP3T_mustang:
          body =
                  new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin + spacing, 2
                          * margin + 3 * spacing, margin, margin);
          break;
        case _3PDT:
        case _3PDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin + 2 * spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
        case _4PDT:
        case _4PDT_off:
        case _4PDT_ononon_1:
        case _4PDT_ononon_2:
          body =
              new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin + 3 * spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
        case _5PDT:
        case _5PDT_off:
          body =
              new RoundRectangle2D.Double(firstPoint.getX() - margin, firstPoint.getY() - margin, 2 * margin + 4 * spacing, 2
                  * margin + 2 * spacing, margin, margin);
          break;
      }
      if (getOrientation() == OrientationHV.HORIZONTAL) {
        AffineTransform xform = AffineTransform.getRotateInstance(-Math.PI / 2, firstPoint.getX(), firstPoint.getY());
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
      g2d.setColor(LIGHT_METAL_COLOR);
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

  @EditableProperty(name = "Pad")
  public Color getTerminalPadColor() {
    if (terminalPadColor == null) {
      terminalPadColor = CIRCLE_COLOR;
    }
    return terminalPadColor;
  }

  public void setTerminalPadColor(Color terminalPadColor) {
    this.terminalPadColor = terminalPadColor;
  }

  @EditableProperty(name = "Terminal")
  public Color getTerminalColor() {
    if (terminalColor == null) {
      terminalColor = TERMINAL_COLOR;
    }
    return terminalColor;
  }

  public void setTerminalColor(Color terminalColor) {
    this.terminalColor = terminalColor;
  }

  // switch stuff
//  
//  @Override
//  public String getControlPointNodeName(int index) {
//    // we don't want the switch to produce any nodes, it just makes connections
//    return null;
//  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public int getPositionCount() {
    return switchType.getPositionCount();
  }

  @Override
  public String getPositionName(int position) {    
    if (switchType.name().endsWith("_off") && position == 2)
      return "OFF";
    return "ON" + Integer.toString(position + 1);
  }

  @DynamicEditableProperty(source = ToggleSwitchPositionPropertyValueSource.class)
  @EditableProperty(name = "Selected Position")
  @Override
  public Integer getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(Integer selectedPosition) {
    this.selectedPosition = selectedPosition;
    this.body = null;
  }

  @EditableProperty(name = "Highlight Connected")
  public Boolean getShowMarkers() {
    if (highlightConnectedTerminals == null) {
      highlightConnectedTerminals = false;
    }
    return highlightConnectedTerminals;
  }

  public void setHighlightConnectedTerminals(Boolean highlightConnectedTerminals) {
    this.highlightConnectedTerminals = highlightConnectedTerminals;
    this.body = null;
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
      case DPDT_ononon_1:
        switch (position) {
          case 0:
            return (index1 == 0 && index2 == 1) || (index1 == 3 && index2 == 4);
          case 1:
            return (index1 == 0 && index2 == 1) || (index1 == 4 && index2 == 5);
          case 2:
            return (index1 == 1 && index2 == 2) || (index1 == 4 && index2 == 5);
          default: return false;
        }
      case DPDT_ononon_2:
        switch (position) {
          case 0:
            return (index1 == 0 && index2 == 1) || (index1 == 3 && index2 == 4);
          case 1:
            return (index1 == 1 && index2 == 2) || (index1 == 3 && index2 == 4);
          case 2:
            return (index1 == 1 && index2 == 2) || (index1 == 4 && index2 == 5);
          default: return false;
        }
      case _4PDT_ononon_1:
        switch (position) {
          case 0:
            return (index1 == 0 && index2 == 1) || (index1 == 3 && index2 == 4) || (index1 == 6 && index2 == 7) || (index1 == 9 && index2 == 10);
          case 1:
            return (index1 == 0 && index2 == 1) || (index1 == 4 && index2 == 5) || (index1 == 6 && index2 == 7) || (index1 == 10 && index2 == 11);
          case 2:
            return (index1 == 1 && index2 == 2) || (index1 == 4 && index2 == 5) || (index1 == 7 && index2 == 8) || (index1 == 10 && index2 == 11);
          default: return false;
        }
      case _4PDT_ononon_2:
        switch (position) {
          case 0:
            return (index1 == 0 && index2 == 1) || (index1 == 3 && index2 == 4) || (index1 == 6 && index2 == 7) || (index1 == 9 && index2 == 10);
          case 1:
            return (index1 == 1 && index2 == 2) || (index1 == 3 && index2 == 4) || (index1 == 7 && index2 == 8) || (index1 == 9 && index2 == 10);
          case 2:
            return (index1 == 1 && index2 == 2) || (index1 == 4 && index2 == 5) || (index1 == 7 && index2 == 8) || (index1 == 10 && index2 == 11);
          default: return false;
        }
    }
    return false;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    int margin = 20;
    Rectangle2D bounds = getBody().getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin, bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
  
  @Override
  public int getLayerId() {   
    return hashCode();
  }
}
