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
package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
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

@ComponentDescriptor(name = "Logic Gate (symbol)", author = "Branislav Stojkovic", category = "Schematic Symbols",
    instanceNamePrefix = "LG", description = "Basic logic gate schematic symbols", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_TAG_AND_VALUE, keywordTag = "Schematic")
public class LogicGateSymbol extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Color BODY_COLOR = Color.white;
  public static Color BORDER_COLOR = Color.black;

  protected GateType gateType = GateType.Not;
  protected String value = "";
  protected Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  protected Color bodyColor = BODY_COLOR;
  protected Color borderColor = BORDER_COLOR;
  protected Display display = Display.NONE;
  transient private Shape[] body;

  public LogicGateSymbol() {
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
    if (body[2] != null)
      g2d.draw(body[2]);
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
    int x = (controlPoints[0].x + controlPoints[getControlPointCount() == 2 ? 1 : 2].x) / 2;
    String label = "";
    label = display == Display.VALUE ? getValue() : getName();
    if (display == Display.NONE) {
      label = "";
    }
    if (display == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    drawCenteredText(g2d, label, x, controlPoints[0].y + (getControlPointCount() == 2 ? 0 : pinSpacing), HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);   
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 3 * width / 32;
    Path2D path = new Path2D.Double();
    path.moveTo(margin, margin);
    path.curveTo(margin * 4, margin * 2, margin * 4, height - margin * 2, margin, height - margin);    
    path.quadTo(width - 2 * margin, height - margin * 2, width - margin, height / 2);
    path.quadTo(width - margin * 2, margin * 2, margin, margin);
    Area area = new Area(path);
    area.intersect(new Area(new Rectangle2D.Double(2 * margin, 0, width, height)));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(8f));
    g2d.draw(area);
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public int getControlPointCount() {
    return getGateType() == GateType.Not || getGateType() == GateType.Buffer ? 2 : 3;
  }

  private void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    if (getControlPointCount() == 2) {
      controlPoints[1].x = x + pinSpacing * 6;
      controlPoints[1].y = y;
    } else {
      controlPoints[1].x = x;
      controlPoints[1].y = y + pinSpacing * 2;

      controlPoints[2].x = x + pinSpacing * 6;
      controlPoints[2].y = y + pinSpacing;
    }   
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[3];
      int pinSpacing = (int) PIN_SPACING.convertToPixels();
      int x = controlPoints[0].x;
      int y = controlPoints[0].y;

      Area main = null;

      switch (getGateType()) {
        case Not:         
        case Buffer:
          main = new Area(new Polygon(new int[] {x + pinSpacing * 3 / 2, x + pinSpacing * 9 / 2, x + pinSpacing * 3 / 2}, new int[] {
                  y - pinSpacing * 2, y, y + pinSpacing * 2}, 3));
          break;
        case And:
        case Nand:
          Path2D path1 = new Path2D.Double();          
          path1.moveTo(x + pinSpacing * 3 / 2, y - pinSpacing);
          path1.lineTo(x + pinSpacing * 3 / 2, y + pinSpacing * 3);
          path1.quadTo(x + pinSpacing * 9 / 2, y + pinSpacing * 3, x + pinSpacing * 9 / 2, y + pinSpacing);
          path1.quadTo(x + pinSpacing * 9 / 2, y - pinSpacing, x + pinSpacing * 3 / 2, y - pinSpacing);
          main = new Area(path1);
          break;
        default:
          Path2D path2 = new Path2D.Double();          
          path2.moveTo(x + pinSpacing, y - pinSpacing);
          path2.curveTo(x + pinSpacing * 2, y, x + pinSpacing * 2, y + 2 * pinSpacing, x + pinSpacing, y + pinSpacing * 3);
          path2.quadTo(x + pinSpacing * 7 / 2, y + pinSpacing * 5 / 2, x + pinSpacing * 9 / 2, y + pinSpacing);
          path2.quadTo(x + pinSpacing * 7 / 2, y - pinSpacing / 2, x + pinSpacing, y - pinSpacing);
          main = new Area(path2);
      }
      
      if (getGateType().getNeedsCircle()) {
        Area circle = new Area(new Ellipse2D.Double(x + pinSpacing * 9 / 2, y - pinSpacing / 4 + (getControlPointCount() == 2 ? 0 : pinSpacing), pinSpacing / 2, pinSpacing / 2));
        main.add(circle);
      }

      body[0] = main;

      GeneralPath connections = new GeneralPath();
      if (getControlPointCount() == 2) {
        connections.moveTo(controlPoints[0].x, controlPoints[0].y);
        connections.lineTo(controlPoints[0].x + pinSpacing * 3 / 2, controlPoints[0].y);
        connections.moveTo(controlPoints[1].x, controlPoints[1].y);
        if (getGateType().getNeedsCircle())
          connections.lineTo(controlPoints[1].x - pinSpacing, controlPoints[1].y);
        else
          connections.lineTo(controlPoints[1].x - pinSpacing * 3 / 2, controlPoints[1].y);
      } else {
        connections.moveTo(controlPoints[0].x, controlPoints[0].y);
        connections.lineTo(controlPoints[0].x + pinSpacing * 3 / 2, controlPoints[0].y);
        connections.moveTo(controlPoints[1].x, controlPoints[1].y);
        connections.lineTo(controlPoints[1].x + pinSpacing * 3 / 2, controlPoints[1].y);
        connections.moveTo(controlPoints[2].x, controlPoints[2].y);
        if (getGateType().getNeedsCircle())
          connections.lineTo(controlPoints[2].x - pinSpacing, controlPoints[2].y);
        else
          connections.lineTo(controlPoints[2].x - pinSpacing * 3 / 2, controlPoints[2].y);
      }
      body[1] = connections;
      
      if (getGateType().toString().startsWith("X")) {
        Path2D path = new Path2D.Double();         
        path.moveTo(x + pinSpacing / 2, y - pinSpacing);
        path.curveTo(x + pinSpacing * 3 / 2, y, x + pinSpacing * 3 / 2, y + 2 * pinSpacing, x + pinSpacing / 2, y + pinSpacing * 3);
        body[2] = path;
      }     
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
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @EditableProperty(name = "Type")
  public GateType getGateType() {
    return gateType;
  }

  public void setGateType(GateType gateType) {
    this.gateType = gateType;
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
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  public enum GateType {
    Buffer(false), Not(true), Or(false), And(false), Xor(false), Nor(true), Nand(true), Xnor(true);
    
    boolean needsCircle;
    
    private GateType(boolean needsCircle) {
      this.needsCircle = needsCircle;
    }
    
    public boolean getNeedsCircle() {
      return needsCircle;
    }
  }
}
