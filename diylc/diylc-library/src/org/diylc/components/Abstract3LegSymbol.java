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
package org.diylc.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.semiconductors.SymbolFlipping;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class Abstract3LegSymbol extends AbstractComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color COLOR = Color.black;
  
  protected String value = "";
  protected Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0) };
  protected Color color = COLOR;
  protected SymbolFlipping flip = SymbolFlipping.NONE;
  protected Display display = Display.NAME;
  transient protected Shape[] body;
  protected Orientation orientation = Orientation.DEFAULT;
  protected boolean moveLabel = false;

  public Abstract3LegSymbol() {
    super();
    updateControlPoints();
    controlPoints[3] = getDefaultLabelPosition(controlPoints);
  }
  
  public Point[] getControlPoints() {
    if (controlPoints.length == 3) {
      controlPoints = new Point[] { controlPoints[0], controlPoints[1], controlPoints[2], getDefaultLabelPosition(controlPoints) };
    }
    return controlPoints;
  }
  
  protected Point getDefaultLabelPosition(Point[] oldControlPoints) {
    int f = flip == SymbolFlipping.X ? -1 : 1;
    int d = flip == SymbolFlipping.X ? (int) PIN_SPACING.convertToPixels() / 2 : 0;
    return new Point(oldControlPoints[0].x + f * (int) (PIN_SPACING.convertToPixels() * 1.5 + d), oldControlPoints[0].y);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Color finalColor;
    if (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) {
      finalColor = SELECTION_COLOR;
    } else if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalColor = theme.getOutlineColor();
    } else {
      finalColor = color;
    }
    g2d.setColor(finalColor);

    if (this.body == null) {
      this.body = getBody();
      applyOrientation(this.body);
    }

    AffineTransform old = g2d.getTransform();
    
    Point[] controlPoints = getControlPoints();

    if (this.flip == SymbolFlipping.Y) {
      g2d.translate(0, controlPoints[0].y);
      g2d.scale(1, -1);
      g2d.translate(0, -1 * controlPoints[0].y);
    } else if (this.flip == SymbolFlipping.X) {
      g2d.translate(controlPoints[0].x, 0);
      g2d.scale(-1, 1);
      g2d.translate(-1 * controlPoints[0].x, 0);
    }

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.draw(body[0]);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body[1]);

    g2d.fill(body[2]);
    g2d.setTransform(old);

    // Draw label
    g2d.setFont(project.getFont());
    Color finalLabelColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : LABEL_COLOR;
    }
    g2d.setColor(finalLabelColor);
    String label = "";
    label = display == Display.NAME ? getName() : (getValue() == null ? "" : getValue().toString());
    if (display == Display.NONE) {
      label = "";
    }
    if (display == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D textRect = fontMetrics.getStringBounds(label, g2d);
    Rectangle shapeRect = body[0].getBounds().union(body[1].getBounds()).union(body[2].getBounds());
    
    if (getMoveLabel()) {
      drawCenteredText(g2d, label, controlPoints[3].x, controlPoints[3].y, flip == SymbolFlipping.X ? HorizontalAlignment.RIGHT
              : HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    } else {
      drawCenteredText(g2d, label, getLabelX(shapeRect, textRect, fontMetrics, outlineMode),
          getLabelY(shapeRect, textRect, fontMetrics, outlineMode), flip == SymbolFlipping.X ? HorizontalAlignment.RIGHT
              : HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    }
  }

  @Override
  public Point getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  protected int getLabelX(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics, boolean outlineMode) {
    int f = flip == SymbolFlipping.X ? -1 : 1;
    int d = flip == SymbolFlipping.X ? (int) PIN_SPACING.convertToPixels() / 2 : 0;
    return getControlPoints()[0].x + f * (int) (PIN_SPACING.convertToPixels() * 1.5 + d);
  }

  protected int getLabelY(Rectangle2D shapeRect, Rectangle2D textRect, FontMetrics fontMetrics, boolean outlineMode) {
    return getControlPoints()[0].y;
  }

  protected void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    Point[] controlPoints = getControlPoints();
        
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    // set default point positions
    controlPoints[1].x = x +  pinSpacing * 2;
    controlPoints[1].y = y - pinSpacing * 2;

    controlPoints[2].x = x + pinSpacing * 2;
    controlPoints[2].y = y + pinSpacing * 2;
    
    controlPoints[3].x = x + (int) (PIN_SPACING.convertToPixels() * 2);
    controlPoints[3].y = y;
    
    // apply rotation if needed
    if (getOrientation() != Orientation.DEFAULT)
    {    
      Point first = controlPoints[0];
      double angle = Double.parseDouble(getOrientation().name().replace("_", ""));
      AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(angle), first.x, first.y);
      for (int i = 1; i < controlPoints.length; i++) {
        rotate.transform(controlPoints[i], controlPoints[i]);
      }
    }
    
    // flip at the end
    if (flip == SymbolFlipping.X) {
      controlPoints[1].x = controlPoints[0].x - (controlPoints[1].x - controlPoints[0].x);
      controlPoints[2].x = controlPoints[0].x - (controlPoints[2].x - controlPoints[0].x);
      controlPoints[3].x = controlPoints[0].x - (controlPoints[3].x - controlPoints[0].x);
    }
    if (flip == SymbolFlipping.Y) {
      controlPoints[1].y = controlPoints[0].y - (controlPoints[1].y - controlPoints[0].y);
      controlPoints[2].y = controlPoints[0].y - (controlPoints[2].y - controlPoints[0].y);
      controlPoints[3].y = controlPoints[0].y - (controlPoints[3].y - controlPoints[0].y);
    }
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return index < 3 || getMoveLabel() ? VisibilityPolicy.WHEN_SELECTED : VisibilityPolicy.NEVER;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index < 3;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    getControlPoints()[index].setLocation(point);

    // make sure we have a new drawing
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {   
    return pointIndex == 3;
  }
  
  @Override
  public boolean canControlPointOverlap(int index) {
    return index >= 3;
  }

  @EditableProperty
  public SymbolFlipping getFlip() {
    return flip;
  }

  public void setFlip(SymbolFlipping flip) {
    this.flip = flip;

    updateControlPoints();
    // make sure we have a new drawing
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @EditableProperty(name = "Moveable Label")
  public boolean getMoveLabel() {
    return moveLabel;
  }

  public void setMoveLabel(boolean moveLabel) {
    this.moveLabel = moveLabel;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }
  
  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;

    updateControlPoints();
    // make sure we have a new drawing
    body = null;
  }
  
  protected void applyOrientation(Shape[] body) {
    if (getOrientation() == Orientation.DEFAULT)
      return;

    Point first = getControlPoints()[0];
    double angle = Double.parseDouble(getOrientation().name().replace("_", ""));
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(angle), first.x, first.y);
    
    if (body != null) {
      for (int i = 0; i < body.length; i++) {
        body[i] = rotate.createTransformedShape(body[i]);
      }
    }
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 3)
      return null;
    return Integer.toString(index + 1);
  }

  /**
   * Returns transistor shape consisting of 3 parts, in this order: main body, connectors, polarity
   * arrow.
   * 
   * @return
   */
  protected abstract Shape[] getBody();
}
