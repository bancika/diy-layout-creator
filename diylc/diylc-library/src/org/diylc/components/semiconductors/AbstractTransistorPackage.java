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
package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.netlist.ISpiceMapper;

public abstract class AbstractTransistorPackage extends AbstractLabeledComponent<String> implements ISpiceMapper {
  
  private static final long serialVersionUID = 1L;
  
  protected String value = "";
  protected TransistorPinout pinout;
  protected Orientation orientation = Orientation.DEFAULT;
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient protected Area[] body;
  
  protected Color bodyColor;
  protected Color borderColor;
  protected Color labelColor = LABEL_COLOR;
  protected TransistorDisplay display = TransistorDisplay.NAME;  
  
  protected boolean folded = false;

  public AbstractTransistorPackage() {
  }
  
  protected abstract void updateControlPoints();
  
  public abstract Area[] getBody();
  
  @Override
  public int mapToSpiceNode(int index) {
    if (getPinout() == null)
      return index;
    
    switch (getPinout()) {
      case BJT_CBE:
        return index;
      case BJT_EBC:
        return 2 - index;
      case JFET_DGS:
      case MOSFET_DGS:
        return index;
      case JFET_DSG:
      case MOSFET_DSG:
        switch (index) {
          case 0: return 0;
          case 1: return 2;
          case 2: return 1;
        }        
      case JFET_GSD:
      case MOSFET_GSD:
        switch (index) {
          case 0: return 1;
          case 1: return 2;
          case 2: return 0;
        }
      case JFET_GDS:
      case MOSFET_GDS:
        switch (index) {
          case 0: return 1;
          case 1: return 0;
          case 2: return 2;
        }
      case JFET_SGD:
      case MOSFET_SGD:
        return 2 - index;
    }
    
    return index;
  }
  
  @Override
  public String getComment() {
    return getPinout() == null ? "Pinout not configured, validate this line" : (getPinout().toString() + " pinout");
  }
  
  @Override
  public String getPrefix() {
    if (getPinout() == null)
      return null;    
    return getPinout().name().substring(0, 1);
  }
  
  @EditableProperty
  public String getValue() {
    return value;
  }

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
  
  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null) {
      labelColor = LABEL_COLOR;
    }
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty
  public TransistorDisplay getDisplay() {
    if (display == null) {
      display = TransistorDisplay.NAME;
    }
    return display;
  }

  public void setDisplay(TransistorDisplay display) {
    this.display = display;
  }  
  
  @EditableProperty
  public TransistorPinout getPinout() {
    return pinout;
  }
  
  public void setPinout(TransistorPinout pinout) {
    this.pinout = pinout;
  }
  
  @EditableProperty
  public boolean getFolded() {
    return folded;
  }

  public void setFolded(boolean folded) {
    this.folded = folded;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 3)
      return null;
    return Integer.toString(index + 1);
  }
  
  @Override
  public Rectangle2D getCachingBounds() {    
    int margin = 50;    
    double minX = Integer.MAX_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double maxY = Integer.MIN_VALUE;
    for(Area a : getBody())
      if (a != null) {
        Rectangle2D b = a.getBounds2D();
        if (b.getMinX() < minX)
          minX = b.getMinX();
        if (b.getMaxX() > maxX)
          maxX = b.getMaxX();
        if (b.getMinY() < minY)
          minY = b.getMinY();
        if (b.getMaxY() > maxY)
          maxY = b.getMaxY();
      }
    for (Point2D p : controlPoints) {
      if (p.getX() < minX)
        minX = p.getX();
      if (p.getX() > maxX)
        maxX = p.getX();
      if (p.getY() < minY)
        minY = p.getY() ;
      if (p.getY()  > maxY)
        maxY = p.getY();
    }
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }
  
  protected boolean flipPinoutLabel() {
    return false;
  }
  
  protected void drawPinout(Graphics2D g2d) {
    TransistorPinout p = getPinout();
    if (p == null)
      return;
    Font f = g2d.getFont();
    f = f.deriveFont((float) (f.getSize() * 0.7));
    g2d.setFont(f);
    int d = 4; // hard-coded
    String pinout = p.toPinout();
    for (int i = 0; i < pinout.length(); i++) {
      Point2D point = getControlPoint(i);
      int dx = 0;
      int dy = 0;
      HorizontalAlignment ha = HorizontalAlignment.CENTER;
      VerticalAlignment va = VerticalAlignment.CENTER;
      switch (getOrientation()) {
        case DEFAULT:
          if (flipPinoutLabel()) {
            dx = -d;    
            ha = HorizontalAlignment.RIGHT; 
          } else {
            dx = d;
            ha = HorizontalAlignment.LEFT; 
          }
          break;
        case _90:
          if (flipPinoutLabel()) {
            dy = -d;
            va = VerticalAlignment.TOP;
          } else {
            dy = d;          
            va = VerticalAlignment.BOTTOM;
          }
          break;
        case _180:
          if (flipPinoutLabel()) {
            dx = d;
            ha = HorizontalAlignment.LEFT; 
          } else {
            dx = -d;    
            ha = HorizontalAlignment.RIGHT;
          }
          break;
        case _270:
          if (flipPinoutLabel()) {
            dy = d;          
            va = VerticalAlignment.BOTTOM;
          } else {
            dy = -d;
            va = VerticalAlignment.TOP;
          }
          break;        
      }
      StringUtils.drawCenteredText(g2d, pinout.charAt(i) + "", point.getX() + dx, point.getY() + dy, ha, va);
    }
  }
  
  public enum TransistorDisplay {

    NAME, VALUE, NONE, BOTH, PINOUT;

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
}
