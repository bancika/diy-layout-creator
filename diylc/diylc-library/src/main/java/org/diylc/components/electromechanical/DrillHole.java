/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
package org.diylc.components.electromechanical;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.bancika.gerberwriter.GerberFunctions;

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
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.GerberLayer;
import org.diylc.core.gerber.IGerberComponentCustom;
import org.diylc.core.gerber.IGerberDrawingObserver;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "Drill Hole", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Drill hole marker for PCB, chassis or faceplate", instanceNamePrefix = "Hole",
    zOrder = IDIYComponent.BOARD + 0.1, bomPolicy = BomPolicy.NEVER_SHOW,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "PCB",
    transformer = SimpleComponentTransformer.class, enableCache = true)
public class DrillHole extends AbstractTransparentComponent<Void>
    implements IGerberComponentCustom {

  private static final long serialVersionUID = 1L;

  public static Size DIAMETER = new Size(3.0d, SizeUnit.mm);
  public static Color COLOR = Color.black;
  public static Size STROKE_WIDTH = new Size(1.0d, SizeUnit.px);
  public static Size CROSSHAIR_WIDTH = new Size(1.0d, SizeUnit.px);

  private Size diameter = DIAMETER;
  private Color color = COLOR;
  private Point2D.Double point = new Point2D.Double(0, 0);

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver,
      IGerberDrawingObserver gerberDrawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    double holeDiameter = getDiameter().convertToPixels();
    double borderWidth = STROKE_WIDTH.convertToPixels(); // Use constant stroke width
    
    g2d.setColor(
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : color);
    
    drawingObserver.startTrackingContinuityArea(true);
    
    // Export to Gerber drill layer
    if (gerberDrawingObserver != null) {
      GerberLayer drillLayer = GerberLayer.DrillNonPlated;
      gerberDrawingObserver.startGerberOutput(drillLayer, GerberFunctions.COMPONENT_DRILL, false);
    }
    
    Composite oldComposite = g2d.getComposite();
    if (getAlpha() < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * getAlpha() / MAX_ALPHA));
    }

    // Draw white circle (hole)
    g2d.setColor(Color.white);
    g2d.fill(new Ellipse2D.Double(point.getX() - holeDiameter / 2, point.getY() - holeDiameter / 2,
        holeDiameter, holeDiameter));
    
    // Draw colored border
    g2d.setColor(color);
    g2d.setStroke(new BasicStroke((float) borderWidth));
    g2d.draw(new Ellipse2D.Double(point.getX() - holeDiameter / 2, point.getY() - holeDiameter / 2,
        holeDiameter, holeDiameter));
    
    // Draw crosshairs
    double crosshairLength = holeDiameter * 0.4; // 40% of diameter for crosshair length (increased from 30%)
    double crosshairWidth = CROSSHAIR_WIDTH.convertToPixels(); // Use constant crosshair width
    
    g2d.setStroke(new BasicStroke((float) crosshairWidth));
    
    // Horizontal line
    g2d.draw(new Line2D.Double(
        point.getX() - crosshairLength / 2, point.getY(),
        point.getX() + crosshairLength / 2, point.getY()));
    
    // Vertical line
    g2d.draw(new Line2D.Double(
        point.getX(), point.getY() - crosshairLength / 2,
        point.getX(), point.getY() + crosshairLength / 2));
    
    drawingObserver.stopTracking();
    drawingObserver.stopTrackingContinuityArea();
    
    if (gerberDrawingObserver != null) {
      gerberDrawingObserver.stopGerberOutput();
    }
    
    g2d.setComposite(oldComposite);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    this.draw(g2d, componentState, outlineMode, project, drawingObserver, null);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int diameter = getClosestOdd(width * 3 / 4); // Increased from width/2 to 3/4 of width
    int borderWidth = Math.max(1, (int) STROKE_WIDTH.convertToPixels()); // Use constant stroke width
    int crosshairLength = diameter * 2 / 5; // 40% of diameter (increased from 1/3)
    int crosshairWidth = Math.max(1, (int) CROSSHAIR_WIDTH.convertToPixels()); // Use constant crosshair width
    
    // Draw white circle
    g2d.setColor(Color.white);
    g2d.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
    
    // Draw colored border
    g2d.setColor(COLOR);
    g2d.setStroke(new BasicStroke(borderWidth));
    g2d.drawOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
    
    // Draw crosshairs
    g2d.setStroke(new BasicStroke(crosshairWidth));
    
    // Horizontal line
    g2d.drawLine(
        (width - crosshairLength) / 2, height / 2,
        (width + crosshairLength) / 2, height / 2);
    
    // Vertical line
    g2d.drawLine(
        width / 2, (height - crosshairLength) / 2,
        width / 2, (height + crosshairLength) / 2);
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getDiameter() {
    return diameter;
  }

  public void setDiameter(Size diameter) {
    this.diameter = diameter;
  }

  @EditableProperty(name = "Color")
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
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

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  public Rectangle2D getCachingBounds() {
    double diameter = getDiameter().convertToPixels();
    return new Rectangle2D.Double(point.getX() - diameter, point.getY() - diameter, 
        diameter * 2, diameter * 2);
  }
} 
