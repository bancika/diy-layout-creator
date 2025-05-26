/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
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

@ComponentDescriptor(name = "IC", author = "Branislav Stojkovic", category = "Schematic Symbols",
    instanceNamePrefix = "IC", description = "IC symbol with 3 or 5 contacts",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE, keywordTag = "Schematic")
public class ICSymbol extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color BODY_COLOR = Color.white;
  public static Color BORDER_COLOR = Color.black;

  protected ICPointCount icPointCount = ICPointCount._5;
  protected String value = "";
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0),
      new Point2D.Double(0, 0)};
  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Display display = Display.NAME;
  transient private Shape[] body;
  private Boolean flip;

  public ICSymbol() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    Shape[] body = getBody();

    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(body[0]);
    g2d.setComposite(oldComposite);
    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = borderColor;
    }
    g2d.setColor(finalBorderColor);
    // Draw contacts
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(body[1]);
    // Draw triangle
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.draw(body[0]);
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
    double x = (controlPoints[0].getX() + controlPoints[2].getX()) / 2;
    String label = "";
    label = display == Display.VALUE ? getValue() : getName();
    if (display == Display.NONE) {
      label = "";
    }
    if (display == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    StringUtils.drawCenteredText(g2d, label, x, controlPoints[0].getY() + pinSpacing, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    // Draw +/- markers    
    StringUtils.drawCenteredText(g2d, getFlip() ? "+" : "-", controlPoints[0].getX() + pinSpacing, controlPoints[0].getY(), HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, getFlip() ? "-" : "+", controlPoints[1].getX() + pinSpacing, controlPoints[1].getY(), HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 3 * width / 32;
    Area area =
        new Area(new Polygon(new int[] {margin, margin, width - margin},
            new int[] {margin, height - margin, height / 2}, 3));
    // area.subtract(new Area(new Rectangle2D.Double(0, 0, 2 * margin,
    // height)));
    area.intersect(new Area(new Rectangle2D.Double(2 * margin, 0, width, height)));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(8f));
    StringUtils.drawCenteredText(g2d, "-", 3 * margin, height / 3, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "+", 3 * margin + 1, height * 2 / 3, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    // g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND,
    // BasicStroke.JOIN_ROUND));
    g2d.draw(area);
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public int getControlPointCount() {
    return icPointCount.getValue();
  }

  private void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();

    controlPoints[1].setLocation(x, y + pinSpacing * 2);
    controlPoints[2].setLocation(x + pinSpacing * 6, y + pinSpacing);
    controlPoints[3].setLocation(x + pinSpacing * 3, y - pinSpacing);
    controlPoints[4].setLocation(x + pinSpacing * 3, y + pinSpacing * 3);
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[2];
      int pinSpacing = (int) PIN_SPACING.convertToPixels();
      int x = (int) controlPoints[0].getX();
      int y = (int) controlPoints[0].getY();
      Shape triangle =
          new Polygon(new int[] {x + pinSpacing / 2, x + pinSpacing * 11 / 2, x + pinSpacing / 2}, new int[] {
              y - pinSpacing * 3 / 2, y + pinSpacing, y + pinSpacing * 7 / 2}, 3);
      body[0] = triangle;

      GeneralPath polyline = new GeneralPath();
      polyline.moveTo(controlPoints[0].getX(), controlPoints[0].getY());
      polyline.lineTo(controlPoints[0].getX() + pinSpacing / 2, controlPoints[0].getY());
      polyline.moveTo(controlPoints[1].getX(), controlPoints[1].getY());
      polyline.lineTo(controlPoints[1].getX() + pinSpacing / 2, controlPoints[1].getY());
      polyline.moveTo(controlPoints[2].getX(), controlPoints[2].getY());
      polyline.lineTo(controlPoints[2].getX() - pinSpacing / 2, controlPoints[2].getY());
      if (icPointCount == ICPointCount._5) {
        polyline.moveTo(controlPoints[3].getX(), controlPoints[3].getY());
        polyline.lineTo(controlPoints[3].getX(), controlPoints[3].getY() + pinSpacing * 3 / 4);
        polyline.moveTo(controlPoints[4].getX(), controlPoints[4].getY());
        polyline.lineTo(controlPoints[4].getX(), controlPoints[4].getY() - pinSpacing * 3 / 4);
      }
      body[1] = polyline;
    }
    return body;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
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
    return true;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @EditableProperty(name = "Contacts")
  public ICPointCount getIcPointCount() {
    return icPointCount;
  }

  public void setIcPointCount(ICPointCount icPointCount) {
    this.icPointCount = icPointCount;
    updateControlPoints();
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
  
  @EditableProperty
  public Boolean getFlip() {
    if (flip == null)
      flip = false;
    return flip;
  }
  
  public void setFlip(Boolean flip) {
    this.flip = flip;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
