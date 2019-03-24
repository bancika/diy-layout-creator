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
package org.diylc.components.tube;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractTubeSymbol extends AbstractComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color COLOR = Color.black;

  protected String value = "";

  protected Color color = COLOR;
  protected Display display = Display.NAME;
  transient protected Shape[] body;
  protected boolean showHeaters;
  protected Orientation orientation = Orientation.DEFAULT;
  protected SymbolFlipping flip = SymbolFlipping.NONE;
  protected Point[] controlPoints;

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

    // Draw tube

    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.draw(body[0]);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body[1]);

    if (body[2] != null) {
      g2d.draw(body[2]);
    }

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

    Rectangle rect = body[2] != null ? body[2].getBounds() : body[1].getBounds();

    String label = "";
    label = display == Display.VALUE ? getValue() : getName();
    if (display == Display.NONE) {
      label = "";
    }
    if (display == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    StringUtils.drawCenteredText(g2d, label, rect.x + rect.width, rect.y + rect.height, HorizontalAlignment.RIGHT,
        VerticalAlignment.BOTTOM);
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);

    if (index == controlPoints.length - 1)
      this.body = null;
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

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty(name = "Heaters")
  public boolean getShowHeaters() {
    return showHeaters;
  }

  public void setShowHeaters(boolean showHeaters) {
    this.showHeaters = showHeaters;

    this.body = null;
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
    this.body = null;
  }

  @EditableProperty
  public SymbolFlipping getFlip() {
    if (flip == null)
      flip = SymbolFlipping.NONE;
    return flip;
  }

  public void setFlip(SymbolFlipping flip) {
    this.flip = flip;

    updateControlPoints();
    this.body = null;
  }

  /**
   * Returns transistor shape consisting of 3 parts, in this order: electrodes, connectors, bulb.
   * 
   * @return
   */
  protected abstract Shape[] initializeBody();

  protected Shape[] getBody() {
    if (this.body == null) {
      Shape[] newBody = initializeBody();
      
//      int pinSpacing = (int) PIN_SPACING.convertToPixels();
      int centerX = this.controlPoints[0].x;// + pinSpacing * 3;
      int centerY = this.controlPoints[0].y;

      if (getFlip() == SymbolFlipping.X) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.concatenate(AffineTransform.getTranslateInstance(-2 * centerX, 0));
        if (newBody != null) {
          for (int i = 0; i < newBody.length; i++) {
            newBody[i] = tx.createTransformedShape(newBody[i]);
          }
        }
      } else if (getFlip() == SymbolFlipping.Y) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.concatenate(AffineTransform.getTranslateInstance(0, -2 * centerY));
        if (newBody != null) {
          for (int i = 0; i < newBody.length; i++) {
            newBody[i] = tx.createTransformedShape(newBody[i]);
          }
        }
      }

      if (getOrientation() != Orientation.DEFAULT) {

        Point first = this.controlPoints[0];
        double angle = Double.parseDouble(getOrientation().name().replace("_", ""));
        AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(angle), first.x, first.y);
        if (newBody != null) {
          for (int i = 0; i < newBody.length; i++) {
            newBody[i] = rotate.createTransformedShape(newBody[i]);
          }
        }
      }

      this.body = newBody;
    }

    return this.body;
  }

  protected abstract Point[] initializeControlPoints(Point first);

  protected void updateControlPoints() {
    Point[] newPoints = initializeControlPoints(this.controlPoints[0]);
    this.controlPoints = newPoints;

//    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    int centerX = this.controlPoints[0].x;// + pinSpacing * 3;
    int centerY = this.controlPoints[0].y;

    if (getFlip() == SymbolFlipping.X) {
      AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
      tx.concatenate(AffineTransform.getTranslateInstance(-2 * centerX, 0));
      for (int i = 0; i < this.controlPoints.length; i++) {
        tx.transform(this.controlPoints[i], this.controlPoints[i]);
      }
    } else if (getFlip() == SymbolFlipping.Y) {
      AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
      tx.concatenate(AffineTransform.getTranslateInstance(0, -2 * centerY));
      for (int i = 0; i < this.controlPoints.length; i++) {
        tx.transform(this.controlPoints[i], this.controlPoints[i]);
      }
    }

    if (getOrientation() == Orientation.DEFAULT)
      return;

    Point first = this.controlPoints[0];
    double angle = Double.parseDouble(getOrientation().name().replace("_", ""));
    AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(angle), first.x, first.y);
    for (int i = 1; i < this.controlPoints.length; i++) {
      rotate.transform(this.controlPoints[i], this.controlPoints[i]);
    }
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
