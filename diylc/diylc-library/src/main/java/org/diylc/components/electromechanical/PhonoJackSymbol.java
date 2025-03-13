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

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.Abstract3LegSymbol;
import org.diylc.components.semiconductors.SymbolFlipping;
import org.diylc.components.transform.ThreeLegTransformer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

@ComponentDescriptor(name = "Phono Jack", author = "Branislav Stojkovic",
    category = "Schematic Symbols", instanceNamePrefix = "J",
    description = "Connectors typically used for analog audio signals", zOrder = IDIYComponent.COMPONENT,
    transformer = ThreeLegTransformer.class, keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE,
    keywordTag = "Schematic")
public class PhonoJackSymbol extends Abstract3LegSymbol {

  private static final long serialVersionUID = 1L;

  protected PhonoJackType type = PhonoJackType.MONO;

  public Shape[] getBody() {
    Shape[] body = new Shape[3];
    Point2D[] controlPoints = getControlPoints();
    double x;
    double y;
    double pinSpacing = PIN_SPACING.convertToPixels();

    GeneralPath polyline = new GeneralPath();

    x = controlPoints[0].getX();
    y = controlPoints[0].getY();
    polyline.moveTo(x, y);
    polyline.lineTo(x - pinSpacing * 1.5, y);
    polyline.lineTo(x - pinSpacing * 1.75, y + pinSpacing * 0.25);
    polyline.lineTo(x - pinSpacing * 2, y);
    
    y += pinSpacing * 2;    
    polyline.moveTo(x, y);
    polyline.lineTo(x - pinSpacing * 3, y);
    polyline.lineTo(x - pinSpacing * 3, y - pinSpacing  * 2);
    polyline.lineTo(x - pinSpacing * 2.75, y - pinSpacing  * 2);
    polyline.lineTo(x - pinSpacing * 2.75, y);
    
    if (getType() == PhonoJackType.STEREO) {
      y -= pinSpacing;
      polyline.moveTo(x, y);
      polyline.lineTo(x - pinSpacing, y);
      polyline.lineTo(x - pinSpacing * 1.25, y - pinSpacing * 0.25);
      polyline.lineTo(x - pinSpacing * 1.5, y);
    }
    
    body[1] = polyline;

    return body;
  }

  @Override
  protected void setDefaultPointLocations(Point2D[] controlPoints, int pinSpacing, double x,
      double y) {
    controlPoints[1].setLocation(x, y + pinSpacing * 2);
    controlPoints[2].setLocation(x, y + pinSpacing);
    controlPoints[3].setLocation(x - pinSpacing * 4, y + pinSpacing);
  }
  
  @Override
  protected Point2D getDefaultLabelPosition(Point2D[] oldControlPoints) {
    double pinSpacing = PIN_SPACING.convertToPixels();
    int f = flip == SymbolFlipping.X ? -1 : 1;    
    int d = 0;    
    return new Point2D.Double(oldControlPoints[0].getX() - f * (pinSpacing * 4 + d), oldControlPoints[0].getY() + pinSpacing);
  }
  
  @Override
  protected double getLabelX(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics, boolean outlineMode) {
    if (orientation == Orientation._180 ^ flip == SymbolFlipping.X)
      return shapeRect.getMaxX() + textRect.getWidth() / 2;
    return shapeRect.getMinX() - textRect.getWidth() * 1.5;
  }

  @Override
  protected double getLabelY(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics, boolean outlineMode) {
    return shapeRect.getCenterY();
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(COLOR);
    
    int margin = 4;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawLine(margin, margin, margin, height - margin);
    g2d.drawLine(margin, margin, margin * 2, margin);
    g2d.drawLine(margin * 2, margin, margin * 2, height - margin);
    g2d.drawLine(margin, height - margin, width - margin, height - margin);
    
    g2d.drawLine(margin * 3, margin, margin * 4, margin * 2);
    g2d.drawLine(margin * 4, margin * 2, margin * 5, margin);
    g2d.drawLine(margin * 5, margin, width - margin, margin);
    
    int center = width / 2;
    g2d.drawLine(center, center, center + margin, center - margin);
    g2d.drawLine(center + margin, center - margin, center + margin * 2, center);
    g2d.drawLine(center + margin * 2, center, width - margin, center);
  }

  @EditableProperty(name = "Type")
  public PhonoJackType getType() {
    return type;
  }

  public void setType(PhonoJackType type) {
    this.type = type;

    body = null;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return getName() + "." + index;
  }
  
  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    switch (index) {
      case 0:
      case 1:
        return VisibilityPolicy.WHEN_SELECTED;
      case 2:
        return getType() == PhonoJackType.STEREO ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
      case 3:
        return getMoveLabel() ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
    }
    return VisibilityPolicy.NEVER;    
  }
  
  @Override
  public boolean isControlPointSticky(int index) {
    return index < 3 && (getType() == PhonoJackType.STEREO || index != 2);
  }

  public enum PhonoJackType {

    MONO, STEREO;

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
}
