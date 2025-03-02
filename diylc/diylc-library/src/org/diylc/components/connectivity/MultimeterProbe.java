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
package org.diylc.components.connectivity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation45;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Multimeter Probe", category = "Connectivity", author = "Branislav Stojkovic",
    description = "Multimeter Probe", instanceNamePrefix = "Probe",
    zOrder = IDIYComponent.TEXT, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME, transformer = SimpleComponentTransformer.class,
    enableCache = true)
public class MultimeterProbe extends AbstractTransparentComponent<Color> {

  private static final long serialVersionUID = 1L;

  public static Size PROBE_DIAMETER = new Size(0.05d, SizeUnit.in);
  public static Size PROBE_LENGTH = new Size(0.4d, SizeUnit.in);
  public static Color PROBE_COLOR = METAL_COLOR;
  public static Size HANDLE_DIAMETER = new Size(0.2d, SizeUnit.in);
  public static Size HANDLE_LENGTH = new Size(0.4d, SizeUnit.in);  
  public static Color HANDLE_COLOR = Color.red;
  
  private Point2D.Double point = new Point2D.Double(0, 0);
  private Color handleColor = HANDLE_COLOR;
  private Orientation45 orientation = Orientation45._315;
  
  private transient Shape[] body = null;
  
  public MultimeterProbe() {
    this.alpha = (byte) (MAX_ALPHA / 2);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {

    Shape[] shapes = getBody();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
        
    if (!outlineMode) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(PROBE_COLOR);
      g2d.fill(shapes[0]);
      g2d.setColor(handleColor);
      g2d.fill(shapes[1]);
      g2d.setComposite(oldComposite);
    }    
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : handleColor.darker());
    g2d.draw(shapes[1]);        
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : PROBE_COLOR.darker());
    g2d.draw(shapes[0]);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {    
    double probeDiameter = 2;
    double probeLength = height / 2 + 2;
    double handleDiameter = width / 4;
    double handleLength = height / 2 + 2;
    double x = 2;
    double y = height - 2;
    Shape probe = buildProbeShape(probeDiameter, probeLength, x, y);
    Shape handle = buildHandleShape(probeDiameter, probeLength, handleDiameter, handleLength, x, y);
    
    double theta = Math.PI / 4;
    AffineTransform tx = AffineTransform.getRotateInstance(theta, x, y);
    probe = tx.createTransformedShape(probe);
    handle = tx.createTransformedShape(handle);
    
    g2d.setColor(PROBE_COLOR);
    g2d.fill(probe);
    g2d.setColor(HANDLE_COLOR);
    g2d.fill(handle);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.setColor(PROBE_COLOR.darker());
    g2d.draw(probe);
    g2d.setColor(HANDLE_COLOR.darker());
    g2d.draw(handle);
  }
  
  public Shape[] getBody() {
    if (body == null) {
      double probeDiameter = PROBE_DIAMETER.convertToPixels();
      double probeLength = PROBE_LENGTH.convertToPixels();
      double handleDiameter = HANDLE_DIAMETER.convertToPixels();
      double handleLength = HANDLE_LENGTH.convertToPixels();
      double x = point.getX();
      double y = point.getY();
      body = new Shape[2];
      Path2D probe = buildProbeShape(probeDiameter, probeLength, x, y);
      body[0] = probe;
      Path2D handle = buildHandleShape(probeDiameter, probeLength, handleDiameter, handleLength, x, y);
      body[1] = handle;
      
      double theta = orientation.toRadians();
      if (theta != 0) {
        AffineTransform tx = AffineTransform.getRotateInstance(theta, x, y);
        body[0] = tx.createTransformedShape(body[0]);
        body[1] = tx.createTransformedShape(body[1]);
      }
    }
    return body;
  }

private Path2D buildHandleShape(double probeDiameter, double probeLength, double handleDiameter, double handleLength,
		double x, double y) {
	Path2D handle = new Path2D.Double();
      handle.moveTo(x + probeDiameter, y - probeLength);
      handle.lineTo(x + handleDiameter / 2, y - handleLength - probeLength);
      handle.lineTo(x - handleDiameter / 2, y - handleLength - probeLength);
      handle.lineTo(x - probeDiameter, y - probeLength);      
      handle.moveTo(x + probeDiameter, y - probeLength);
      handle.closePath();
	return handle;
}

private Path2D buildProbeShape(double probeDiameter, double probeLength, double x, double y) {
	Path2D probe = new Path2D.Double();
      probe.moveTo(x, y);
      probe.lineTo(x - probeDiameter / 2, y - probeDiameter);
      probe.lineTo(x - probeDiameter / 2, y - probeLength);
      probe.lineTo(x + probeDiameter / 2, y - probeLength);
      probe.lineTo(x + probeDiameter / 2, y - probeDiameter);
      probe.lineTo(x, y);
      probe.closePath();
	return probe;
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
    this.body = null;
  }

  @Override
  @EditableProperty(name = "Color")
  public Color getValue() {
    return handleColor;
  }

  public void setValue(Color color) {
    this.handleColor = color;
  }
  
  @EditableProperty(name = "Angle")
  public Orientation45 getOrientation() {
    return orientation;
  }
  
  public void setOrientation(Orientation45 orientation) {
    this.orientation = orientation;
    this.body = null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return "Tip";
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] shapes = getBody();
    Rectangle2D bounds1 = shapes[0].getBounds2D();
    Rectangle2D bounds2 = shapes[1].getBounds2D();
    double minX = Math.min(bounds1.getMinX(), bounds2.getMinX());
    double minY = Math.min(bounds1.getMinY(), bounds2.getMinY());
    double maxX = Math.max(bounds1.getMaxX(), bounds2.getMaxX());
    double maxY = Math.max(bounds1.getMaxY(), bounds2.getMaxY());
    return new Rectangle2D.Double(minX - 1, minY - 1, maxX - minX + 2, maxY - minY + 2);
  }
}
