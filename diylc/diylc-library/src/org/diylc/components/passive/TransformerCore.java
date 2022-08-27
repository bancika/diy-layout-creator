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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Transformer Core", author = "Branislav Stojkovic", category = "Schematic Symbols", instanceNamePrefix = "T",
    description = "Transformer core symbol. Use multiple instances together with \"Transformer Coil Symbol\"<br>to draw transformer schematics.",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_TAG, creationMethod = CreationMethod.POINT_BY_POINT, keywordTag = "Schematic")
public class TransformerCore extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Size SPACING = new Size(0.025d, SizeUnit.in);
  public static Color COLOR = Color.blue;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0)};

  protected Color color = COLOR;

  public TransformerCore() {
    super();
  }

  @Override
  public int getControlPointCount() {
    return 2;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return this.controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.controlPoints[index].setLocation(point);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
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

    double theta =
        Math.atan2(this.controlPoints[1].getY() - this.controlPoints[0].getY(), this.controlPoints[1].getX() - this.controlPoints[0].getX())
            + Math.PI / 2;
    double spacing = SPACING.convertToPixels();
    // System.out.println(theta);

    g2d.setColor(finalColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.translate(spacing * Math.cos(theta) / 2, spacing * Math.sin(theta) / 2);
    g2d.drawLine((int)this.controlPoints[0].getX(), (int)this.controlPoints[0].getY(), (int)this.controlPoints[1].getX(), (int)this.controlPoints[1].getY());
    g2d.translate(-spacing * Math.cos(theta), -spacing * Math.sin(theta));
    g2d.drawLine((int)this.controlPoints[0].getX(), (int)this.controlPoints[0].getY(), (int)this.controlPoints[1].getX(), (int)this.controlPoints[1].getY());
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(COLOR);

    GeneralPath polyline = new GeneralPath();
    polyline.moveTo(0, height * 7 / 16);
    polyline.lineTo(width, height * 7 / 16);
    polyline.moveTo(0, height * 9 / 16);
    polyline.lineTo(width, height * 9 / 16);

    g2d.draw(polyline);
  }

  @Override
  public Void getValue() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void setValue(Void value) {
    // TODO Auto-generated method stub
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
