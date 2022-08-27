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
package org.diylc.components.passive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.Format;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Multi-Section Capacitor", author = "Branislav Stojkovic", category = "Passive",
    instanceNamePrefix = "C", description = "Multi-section vertically mounted electrolytic capacitor, similar to JJ, CE and others",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true)
public class MultiSectionCapacitor extends AbstractLabeledComponent<Capacitance[]> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.decode("#6B6DCE");
  public static Color BASE_COLOR = Color.decode("#333333");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = METAL_COLOR;// Color.decode("#00B2EE");
//  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.white;
  public static Size PIN_SIZE = new Size(0.08d, SizeUnit.in);
//  public static Size PIN_SPACING = new Size(0.05d, SizeUnit.in);
  public static Size BODY_DIAMETER = new Size(1d, SizeUnit.in);
  private static double[] RELATIVE_DIAMETERS = new double[] { 0.4d, 0.6d };
  private static final Format format = new DecimalFormat("0.#####");

  private Capacitance[] value = new Capacitance[3];
  private org.diylc.core.measures.Voltage voltage = null;

  private Orientation orientation = Orientation.DEFAULT;
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient private Area[] body;
  private Color bodyColor = BODY_COLOR;
  private Color baseColor = BASE_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color pinColor = PIN_COLOR;  
  protected Display display = Display.NAME;
//  private Size pinSpacing = PIN_SPACING;
  private Size diameter = BODY_DIAMETER;

  public MultiSectionCapacitor() {
    super();
    updateControlPoints();
  }

  @EditableProperty
  public Capacitance[] getValue() {
    return value;
  }

  public void setValue(Capacitance[] value) {
    boolean needsUpdate = false;
    if ((this.value == null ? 0 : this.value.length) != (value == null ? 0 : value.length))
      needsUpdate = true;
    
    this.value = value;
    
    if (needsUpdate) {
      updateControlPoints();
      body = null;
    }
  }
  
  private String getStringValue() {
    if (value == null || value.length == 0)
      return "";
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;    
    for (Capacitance c : value) {
      if (isFirst)
        isFirst = false;
      else
        sb.append("/");
      sb.append(c == null || c.getValue() == null ? "" : format.format(c.getValue()));
    }
    if (value[0] != null)
      sb.append(" " + value[0].getUnit() == null ? "" : value[0].getUnit());
    return sb.toString();
  }

  @EditableProperty
  public org.diylc.core.measures.Voltage getVoltage() {
    return voltage;
  }

  public void setVoltage(org.diylc.core.measures.Voltage voltage) {
    this.voltage = voltage;
  }

  @EditableProperty
  public Size getDiameter() {
    return diameter;
  }
  
  public void setDiameter(Size diameter) {
    this.diameter = diameter;
    // Reset body shape;    
    updateControlPoints();
    body = null;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    int pinSpacing = (int) (getDiameter().convertToPixels() * RELATIVE_DIAMETERS[value == null || value.length == 1 ? 0 : 1]);
    
    int newCount = value.length + 1;
    if (newCount != controlPoints.length) {
      // need new control points
      Point2D[] newPoints = new Point2D[newCount];
      newPoints[0] = controlPoints[0];
      for (int i = 1; i < newCount; i++) {
        newPoints[i] = new Point2D.Double(0, 0);
      }
      controlPoints = newPoints;
    }
    
    // Update control points.
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    
    if (newCount == 2) {      
      switch (orientation) {
        case DEFAULT:        
          controlPoints[1].setLocation(x, y + pinSpacing);
          break;
        case _90:
          controlPoints[1].setLocation(x - pinSpacing, y);
          break;
        case _180:
          controlPoints[1].setLocation(x, y- pinSpacing);
          break;
        case _270:
          controlPoints[1].setLocation(x + pinSpacing, y);
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
    } else if (newCount == 3) {
      switch (orientation) {
        case DEFAULT:                  
          controlPoints[1].setLocation(x + pinSpacing / 2, y + pinSpacing / 2);
          controlPoints[2].setLocation(x - pinSpacing / 2, y + pinSpacing / 2);
          break;
        case _90:          
          controlPoints[1].setLocation(x - pinSpacing / 2, y - pinSpacing / 2);
          controlPoints[2].setLocation(x - pinSpacing / 2, y + pinSpacing / 2);
          break;
        case _180:          
          controlPoints[1].setLocation(x - pinSpacing / 2, y - pinSpacing / 2);
          controlPoints[2].setLocation(x + pinSpacing / 2, y - pinSpacing / 2);
          break;
        case _270:
          controlPoints[1].setLocation(x + pinSpacing / 2, y - pinSpacing / 2);
          controlPoints[2].setLocation(x + pinSpacing / 2, y + pinSpacing / 2);
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
    } else {
      double theta = Math.PI * 2 / newCount;      
      double centerX;
      double centerY;      
      double theta0;
      
      switch (orientation) {
        case DEFAULT:                  
          centerX = x;
          centerY = y + pinSpacing / 2;
          theta0 = -Math.PI / 2;
          break;
        case _90:          
          centerX = x - pinSpacing / 2;
          centerY = y;
          theta0 = 0;
          break;
        case _180:          
          centerX = x;
          centerY = y - pinSpacing / 2;
          theta0 = Math.PI / 2;
          break;
        case _270:
          centerX = x + pinSpacing / 2;
          centerY = y;
          theta0 = -Math.PI;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
          
      for (int i = 1; i < newCount; i++) {
        controlPoints[i].setLocation(centerX + Math.cos(theta0 + theta * i) * pinSpacing / 2, centerY + Math.sin(theta0 + theta * i) * pinSpacing / 2);
      }   
    }
  }

  public Area[] getBody() {
    if (body == null) {
      double centerX;
      double centerY;
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      int pinSpacing = (int) (getDiameter().convertToPixels() * RELATIVE_DIAMETERS[value == null || value.length == 1 ? 0 : 1]);
      if (controlPoints.length == 2 || controlPoints.length == 3) {       
        switch (orientation) {
          case DEFAULT:                    
            centerX = x;
            centerY = y + pinSpacing / 2;
            break;
          case _90:            
            centerX = x - pinSpacing / 2;
            centerY = y;
            break;
          case _180:            
            centerX = x;
            centerY = y - pinSpacing / 2;
            break;
          case _270:
            centerX = x + pinSpacing / 2;
            centerY = y;
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      } else {
        switch (orientation) {
          case DEFAULT:                    
            centerX = x;
            centerY = y + pinSpacing / 2;
            break;
          case _90:            
            centerX = x - pinSpacing / 2;
            centerY = y;
            break;
          case _180:            
            centerX = x;
            centerY = y - pinSpacing / 2;
            break;
          case _270:
            centerX = x + pinSpacing / 2;
            centerY = y;
            break;
          default:
            throw new RuntimeException("Unexpected orientation: " + orientation);
        }
      }
      int bodyDiameter = getClosestOdd(getDiameter().convertToPixels());
      int innerDiameter = getClosestOdd(getDiameter().convertToPixels() * 0.85);

      body = new Area[] { new Area(new Ellipse2D.Double(centerX - bodyDiameter / 2, centerY - bodyDiameter / 2, bodyDiameter, bodyDiameter)),
          new Area(new Ellipse2D.Double(centerX - innerDiameter / 2, centerY - innerDiameter / 2, innerDiameter, innerDiameter))};

    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
    Area[] area = getBody();
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(area[0]);
    drawingObserver.startTracking();
    g2d.setComposite(oldComposite);
    Color finalBorderColor;
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    if (outlineMode) {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : borderColor;
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(area[0]);
    
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : baseColor);    
    g2d.fill(area[1]);
    if (outlineMode) {
      g2d.setColor(baseColor.darker());    
      g2d.draw(area[1]);
    }

    for (Point2D point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(pinColor);
        g2d.fillOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : pinColor.darker());
      g2d.drawOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
    }

    // Draw label.
    g2d.setFont(project.getFont());
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : getStringValue();
    if (getDisplay() == Display.NONE) {
      label = "";
    } else if (getDisplay() == Display.NAME) {
      label = getName();
    } else if (getDisplay() == Display.VALUE) {
      label = getStringValue();
    } else if (getDisplay() == Display.BOTH) {
      label = getName() + "\n" + getStringValue();
    }
    
    Rectangle bounds = area[0].getBounds();

    StringUtils.drawCenteredText(g2d, label, bounds.getX() + bounds.width / 2, bounds.getY() + bounds.height / 2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    
    // draw polarity markers
    g2d.setColor(pinColor.darker());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    double markerSize = pinSize * 0.7;
    for (int i = 0; i < controlPoints.length; i++) {
      double x = controlPoints[i].getX();
      double y = controlPoints[i].getY();
      g2d.drawLine((int)(x - markerSize / 2), (int)y, (int)(x + markerSize / 2), (int)y);
      if (i > 0)
        g2d.drawLine((int)x, (int)(y - markerSize / 2), (int)x, (int)(y + markerSize / 2));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 2 * width / 32;
    Area area = new Area(new Ellipse2D.Double(margin, margin, width - 2 * margin, width - 2 * margin));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(area);
    g2d.setColor(BASE_COLOR);
    margin = 6 * width / 32;
    area = new Area(new Ellipse2D.Double(margin, margin, width - 2 * margin + 1, width - 2 * margin + 1));
    g2d.fill(area);
    g2d.setColor(PIN_COLOR);
    int pinSize = 2 * width / 32;
    for (int i = 0; i < 3; i++) {
      g2d.fillOval((i == 1 ? width * 3 / 8 : width / 2) - pinSize / 2, height / 2 + (i - 1) * (height / 5), pinSize, pinSize);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }
  
  @EditableProperty(name = "Base")
  public Color getBaseColor() {
    return baseColor;
  }
  
  public void setBaseColor(Color baseColor) {
    this.baseColor = baseColor;
  }

//  @EditableProperty(name = "Pin spacing")
//  public Size getPinSpacing() {
//    return pinSpacing;
//  }
//
//  public void setPinSpacing(Size pinSpacing) {
//    this.pinSpacing = pinSpacing;
//    updateControlPoints();
//    // Reset body shape;
//    body = null;
//  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.NAME;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }
  
  @EditableProperty(name = "Pin Color")
  public Color getPinColor() {
    return pinColor;
  }
  
  public void setPinColor(Color pinColor) {
    this.pinColor = pinColor;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {    
    int margin = 20;    
    Rectangle2D bounds = getBody()[0].getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin, bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
}
