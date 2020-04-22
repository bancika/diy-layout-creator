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
package org.diylc.components.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import org.diylc.common.Display;
import org.diylc.common.LineStyle;
import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Tape Measure", author = "Branislav Stojkovic", category = "Misc",
    creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "TM", description = "Measures distance between the two points",
    zOrder = IDIYComponent.COMPONENT, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class)
public class TapeMeasure extends AbstractLeadedComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color COLOR = Color.black;  

  private Color color = COLOR;
  protected LineStyle style = LineStyle.SOLID; 
  private Size thickness = new Size(1d, SizeUnit.px);
  private Size arrowSize = new Size(5d, SizeUnit.px);
  private Polygon arrow = null;  
  private AffineTransform arrowTx = new AffineTransform();
  private boolean arrowStart = true;
  private boolean arrowEnd = true;
  private SizeUnit unit = SizeUnit.mm;
  private int decimals = 2;

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(COLOR);
    g2d.drawLine(1, height - 2, width / 4, height * 3 / 4 - 1);
    g2d.drawLine(width * 3 / 4, height / 4 - 1, width - 2, 1);
    g2d.setBackground(Color.black);
    g2d.fillPolygon(new int[] {1, 1, 4}, new int[] {height - 4, height - 1, height - 1}, 3);
    g2d.fillPolygon(new int[] {width - 4, width - 1, width - 1}, new int[] {1, 1, 4}, 3);
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    String text = "8mm";
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    Font font = Project.DEFAULT_FONT.deriveFont(8f);
    FontMetrics metrics = g2d.getFontMetrics(font); 
    int x = (width - metrics.stringWidth(text)) / 2; 
    int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent() - 1;    
    g2d.setFont(font);
    g2d.drawString(text, x, y);    
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    float thickness = (float) getThickness().convertToPixels();
    Stroke stroke = null;
    switch (getStyle()) {
      case SOLID:
        stroke = ObjectCache.getInstance().fetchZoomableStroke(thickness);
        break;
      case DASHED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness * 2, thickness * 4}, thickness * 4, BasicStroke.CAP_SQUARE);
        break;
      case DOTTED:
        stroke = ObjectCache.getInstance().fetchStroke(thickness, new float[] {thickness, thickness * 5}, 0, BasicStroke.CAP_ROUND);
        break;
    }
    g2d.setStroke(stroke);
    g2d.setColor(componentState == ComponentState.SELECTED ? SELECTION_COLOR : color);
    
    Point startPoint = new Point(getControlPoint(0));
    Point endPoint = new Point(getControlPoint(1));
    
    if (arrowStart) {
      arrowTx.setToIdentity();
      double angle = Math.atan2(getControlPoint(1).y - getControlPoint(0).y, getControlPoint(1).x - getControlPoint(0).x);
      arrowTx.translate(getControlPoint(0).x, getControlPoint(0).y);
      arrowTx.rotate((angle + Math.PI / 2d));
      AffineTransform oldTx = g2d.getTransform();
      g2d.transform(arrowTx);         
      g2d.fill(getArrow());
      g2d.setTransform(oldTx);
      
      // make the line slightly shorter so line end doesn't overlap with the arrow
      double distance = distance(startPoint, endPoint);
      interpolate(startPoint, endPoint, getArrowSize().convertToPixels() * 0.9 / distance, startPoint);
    }
    if (arrowEnd) {
      arrowTx.setToIdentity();
      double angle = Math.atan2(getControlPoint(1).y - getControlPoint(0).y, getControlPoint(1).x - getControlPoint(0).x);
      arrowTx.translate(getControlPoint(1).x, getControlPoint(1).y);
      arrowTx.rotate((angle - Math.PI / 2d));
      AffineTransform oldTx = g2d.getTransform();
      g2d.transform(arrowTx);   
      g2d.fill(getArrow());
      g2d.setTransform(oldTx);
      
      // make the line slightly shorter so line end doesn't overlap with the arrow
      double distance = distance(startPoint, endPoint);
      interpolate(endPoint, startPoint, getArrowSize().convertToPixels() * 0.9 / distance, endPoint);
    }
    
    double theta = Math.atan2(getPoints()[1].y - getPoints()[0].y, getPoints()[1].x - getPoints()[0].x);
    
    String text = getValue();
    Font font = Project.DEFAULT_FONT;
    FontMetrics metrics = g2d.getFontMetrics(font);
    int strWidth = metrics.stringWidth(text);
    int x = -strWidth / 2;
    int y = -metrics.getHeight() / 2 + metrics.getAscent();
    
    int legLength = (int) (getPoints()[0].distance(getPoints()[1]) - strWidth) / 2 - 6;
    
    System.out.println(strWidth + " " + legLength);
        
    // draw legs around the enter text
    g2d.drawLine(startPoint.x, startPoint.y, 
    		(int)Math.round(startPoint.x + Math.cos(theta) * legLength), 
    		(int)Math.round(startPoint.y + Math.sin(theta) * legLength));
    
    g2d.drawLine(endPoint.x, endPoint.y, 
    		(int)Math.round(endPoint.x - Math.cos(theta) * legLength), 
    		(int)Math.round(endPoint.y - Math.sin(theta) * legLength));
    
    // Adjust label angle if needed to make sure that it's readable.
    if ((theta >= Math.PI / 2 && theta <= Math.PI) || (theta < -Math.PI / 2 && theta > -Math.PI)) {      
      theta += Math.PI;
    }
    
    g2d.translate((getPoints()[0].x + getPoints()[1].x) / 2, (getPoints()[0].y + getPoints()[1].y) / 2);
    g2d.rotate(theta);
    
    g2d.setFont(font);
    g2d.drawString(text, x, y);
  }
  
  private void interpolate(Point p1, Point p2, double t, Point p) {
    p.setLocation((int)Math.round(p1.x * (1-t) + p2.x * t), (int)Math.round(p1.y * (1-t) + p2.y * t));
  }
  
  private double distance(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
  }

  @Override
  public Color getLeadColorForPainting(ComponentState componentState) {
    return componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : color;
  }
  
  public Polygon getArrow() {
    if (arrow == null) {
      arrow = new Polygon();
      int t = (int) getArrowSize().convertToPixels();
      arrow.addPoint(0, 0);
      arrow.addPoint(-t, -t * 2);
      arrow.addPoint(t, -t * 2);
    }
    return arrow;
  }
  
  @EditableProperty
  public Size getThickness() {
    if (thickness == null)
      thickness = new Size(1d, SizeUnit.px);
    return thickness;
  }
  
  public void setThickness(Size thickness) {
    this.thickness = thickness;   
  }
  
  @EditableProperty(name = "Arrow Size")
  public Size getArrowSize() {
    if (arrowSize == null)
      arrowSize = thickness = new Size(1d, SizeUnit.px); 
    return arrowSize;
  }
  
  public void setArrowSize(Size arrowSize) {
    this.arrowSize = arrowSize;
    arrow = null;
  }

  @Override
  public Color getLeadColor() {
    return super.getLeadColor();
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @EditableProperty(name = "Style")
  public LineStyle getStyle() {
    if (style == null)
      style = LineStyle.SOLID;
    return style;
  }

  public void setStyle(LineStyle style) {
    this.style = style;
  }
  
  @EditableProperty
  public SizeUnit getUnit() {
	return unit;
  }
  
  public void setUnit(SizeUnit unit) {
	this.unit = unit;
  }
  
  @EditableProperty
  public int getDecimals() {
	return decimals;
  }
  
  public void setDecimals(int decimals) {
	this.decimals = decimals;
  }

  public Color getBodyColor() {
    return super.getBodyColor();
  }

  @Override
  public Color getBorderColor() {
    return super.getBorderColor();
  }

  @Override
  public Byte getAlpha() {
    return super.getAlpha();
  }

  @Override
  public Size getLength() {
    return super.getLength();
  }

  @Override
  public Size getWidth() {
    return super.getWidth();
  }

  @Override
  public String getValue() {
	Point a = getControlPoint(0);
	Point b = getControlPoint(1);
	double d = a.distance(b);
	double convertedD = SizeUnit.px.getFactor() / getUnit().getFactor() * d;
	String format = "#";
	if (getDecimals() > 0) {
		format += ".";
		for (int i = 0; i < getDecimals(); i++)
			format += "#";
	}
	DecimalFormat decimalFormat = new DecimalFormat(format);
    return decimalFormat.format(convertedD) + getUnit().toString();
  }

  @Override
  public void setValue(String value) {}

  @Override
  protected Shape getBodyShape() {
    return new Rectangle2D.Double(0, 0, 50, 20);
  }

  @Override
  protected Size getDefaultWidth() {
    return null;
  }

  @Override
  protected Size getDefaultLength() {
    return null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }

  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }
  
  @Deprecated
  @Override
  public Display getDisplay() {
    return Display.VALUE;
  }
    
  @Deprecated
  @Override
  public org.diylc.components.AbstractLeadedComponent.LabelOriantation getLabelOriantation() {
    return org.diylc.components.AbstractLeadedComponent.LabelOriantation.Directional;
  }
  
  @Deprecated
  @Override
  public boolean getMoveLabel() {
	return false;
  }
}
