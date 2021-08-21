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
 */
package org.diylc.components.chassis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

import com.sun.prism.BasicStroke;

@ComponentDescriptor(name = "Dial Scale", author = "Branislav Stojkovic", category = "Misc",
    instanceNamePrefix = "DS", description = "Control panel dial scale", zOrder = IDIYComponent.CHASSIS + 0.1,
    bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false, transformer = SimpleComponentTransformer.class)
public class DialScale extends AbstractComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size DIAMETER = new Size(1d, SizeUnit.in);
  public static Size MARKER_SIZE = new Size(0.1d, SizeUnit.in);
  public static Size TICK_WIDTH = new Size(0.5d, SizeUnit.mm);
  public static Color COLOR = Color.black;

  private Size diameter = DIAMETER;
  private Size markerSize = MARKER_SIZE;
  private Size tickWidth = TICK_WIDTH;
  private Color color = COLOR;
  private Point2D.Double point = new Point2D.Double(0, 0);
  private String value = "";
  private DialScaleType type = DialScaleType.Ticks;
  private Font font = LABEL_FONT;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    
    double x0 = point.getX();
    double y0 = point.getY();
    
    double r = diameter.convertToPixels() / 2;
    double marker = markerSize.convertToPixels();
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    
    g2d.drawLine((int)(x0 - r / 6), (int)y0, (int)(x0 + r / 6), (int)y0);
    g2d.drawLine((int)x0, (int)(y0 - r / 6), (int)x0, (int)(y0 + r / 6));
    
    
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR : color);
    g2d.setFont(font.deriveFont((float)marker));
    
    Stroke tickStroke = ObjectCache.getInstance().fetchStroke((float)tickWidth.convertToPixels(), BasicStroke.CAP_BUTT);
    
    double x1;
    double y1;
    
    int segments;    
    if (type == DialScaleType.Numeric_1_10) {
      segments = 10;      
    } else if (type == DialScaleType.Numeric_1_12) {
      segments = 12;      
    } else {
      segments = 11;
    }
    
    double shift = (5 * Math.PI / 3) / (segments - 1);

    double alpha = 4 * Math.PI / 6;
    
    for (int i = 0; i < segments; i++) {
      double x = x0 + Math.cos(alpha) * r;
      double y = y0 + Math.sin(alpha) * r;
      
      switch (type) {
        case Dots:
          g2d.fillOval((int)(x - marker / 2), (int)(y - marker / 2), (int)marker, (int)marker);
          break;
        case Numeric_0_10:
          StringUtils.drawCenteredText(g2d, Integer.toString(i), x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
          break;
        case Numeric_1_12:
        case Numeric_1_11:
        case Numeric_1_10:
          StringUtils.drawCenteredText(g2d, Integer.toString(i + 1), x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
          break;
        case Ticks:
          x1 = x0 + Math.cos(alpha) * (r - marker);
          y1 = y0 +  Math.sin(alpha) * (r - marker);
          g2d.setStroke(tickStroke);
          g2d.drawLine((int)x, (int)y, (int)x1, (int)y1);
          break;
        case Numeric_Even:
          if (i % 2 == 0) {
            StringUtils.drawCenteredText(g2d, Integer.toString(i), x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
          }
          break;
        case Numeric_Even_Ticks:
          if (i % 2 == 0) {
            StringUtils.drawCenteredText(g2d, Integer.toString(i), x, y, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
          } else {
            x1 = x0 + Math.cos(alpha) * (r - marker);
            y1 = y0 +  Math.sin(alpha) * (r - marker);
            g2d.setStroke(tickStroke);
            g2d.drawLine((int)x, (int)y, (int)x1, (int)y1);
          }
          break;
        case Dots_Gradual: {
          double currentMarker = marker * (i + 5) / 15;
          g2d.fillOval((int)(x - currentMarker / 2), (int)(y - currentMarker / 2), (int)currentMarker, (int)currentMarker);
        }
      }
      
      alpha += shift;
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double r = getClosestOdd(width * 2d / 3) - 2;
    double mark = 4d * width / 32;
    double alpha = 5 * Math.PI / 6;
    double x0 = width * 2d / 3;
    double y0 = height * 2d / 3;
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(Color.black);
    for (int i = 0; i < 7; i++) {
      g2d.drawLine((int)(x0 + Math.cos(alpha) * r), (int)(y0 + Math.sin(alpha) * r), 
          (int)(x0 + Math.cos(alpha) * (r - mark)), (int)(y0 + Math.sin(alpha) * (r - mark)));
      alpha += Math.PI / 6;
    }
  }

  @EditableProperty
  public Size getDiameter() {
    return diameter;
  }

  public void setDiameter(Size diameter) {
    this.diameter = diameter;
  }
  
  @EditableProperty(name = "Marker Size")
  public Size getMarkerSize() {
    return markerSize;
  }
  
  public void setMarkerSize(Size markerSize) {
    this.markerSize = markerSize;
  }
  
  @EditableProperty(name = "Tick Width")
  public Size getTickWidth() {
    return tickWidth;
  }
  
  public void setTickWidth(Size tickWidth) {
    this.tickWidth = tickWidth;
  }
  
  @EditableProperty
  public Font getFont() {
    return font;
  }
  
  public void setFont(Font font) {
    this.font = font;
  }
  
  @EditableProperty
  public DialScaleType getType() {
    return type;
  }
  
  public void setType(DialScaleType type) {
    this.type = type;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public int getControlPointCount() {
    return 1;
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
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point.setLocation(point);
  }

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  @EditableProperty
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    double size = getDiameter().convertToPixels();
    return new Rectangle2D.Double(point.getX() - size, point.getY() - size, size * 2, size * 2);
  }
  
  public enum DialScaleType {
    Ticks("Ticks"), Dots("Dots (Uniform)"), Dots_Gradual("Dots (Gradual)"), Numeric_0_10("Numeric (0-10)"), Numeric_1_11("Numeric (1-11)"), 
    Numeric_1_10("Numeric (1-10)"), Numeric_1_12("Numeric (1-12)"), Numeric_Even("Numeric (Even, No Ticks)"), Numeric_Even_Ticks("Numeric (Even, With Ticks)");
    
    private String label;

    private DialScaleType(String label) {
      this.label = label;    
    }
    
    @Override
    public String toString() {
      return label;
    }
  }
}