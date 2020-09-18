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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.TwoCircleTangent;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
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

@ComponentDescriptor(name = "Transistor (TO-3)", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "Q", description = "Transistor with large metal body",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE, enableCache = true)
public class TransistorTO3 extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.lightGray;
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.black;

  public static Size LARGE_DIAMETER = new Size(26.2d, SizeUnit.mm);
  public static Size INNER_DIAMETER = new Size(21.3d, SizeUnit.mm);
  public static Size SMALL_DIAMETER = new Size(8d, SizeUnit.mm);
  public static Size HOLE_DISTANCE = new Size(30.1d, SizeUnit.mm);
  public static Size HOLE_SIZE = new Size(4.1d, SizeUnit.mm);
  public static Size PIN_SPACING = new Size(10.9d, SizeUnit.mm);
  public static Size PIN_OFFSET = new Size(1.85d, SizeUnit.mm);
  public static Size PIN_DIAMETER = new Size(1d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  transient private Area[] body;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  protected Display display = Display.NAME;

  public TransistorTO3() {
    super();
    updateControlPoints();
    alpha = (byte) 100;
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

  private void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    // Update control points.
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    switch (orientation) {
      case DEFAULT:
        controlPoints[1].setLocation(x, y + pinSpacing);
        break;
      case _90:
        controlPoints[1].setLocation(x - pinSpacing, y);
        break;
      case _180:
        controlPoints[1].setLocation(x, y - pinSpacing);
        break;
      case _270:
        controlPoints[1].setLocation(x + pinSpacing, y);
        break;
      default:
        throw new RuntimeException("Unexpected orientation: " + orientation);
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      int pinOffset = (int) PIN_OFFSET.convertToPixels();
      double x = (controlPoints[0].getX() + controlPoints[1].getX()) / 2;
      double y = (controlPoints[0].getY() + controlPoints[1].getY()) / 2;

      switch (orientation) {
        case DEFAULT:
          x += pinOffset;
          break;
        case _90:
          y += pinOffset;
          break;
        case _180:
          x -= pinOffset;
          break;
        case _270:
          y -= pinOffset;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      int largeDiameter = getClosestOdd(LARGE_DIAMETER.convertToPixels());
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      int smallDiameter = getClosestOdd(SMALL_DIAMETER.convertToPixels());
      int holeDistance = getClosestOdd(HOLE_DISTANCE.convertToPixels());
      int holeSize = getClosestOdd(HOLE_SIZE.convertToPixels());
      
      TwoCircleTangent left = new TwoCircleTangent(new Point2D.Double(x, y), new Point2D.Double(x - holeDistance / 2, y), largeDiameter / 2, smallDiameter / 2);
      TwoCircleTangent right = new TwoCircleTangent(new Point2D.Double(x, y), new Point2D.Double(x + holeDistance / 2, y), largeDiameter / 2, smallDiameter / 2);
      
      body[0] = left;
      body[0].add(right);

      body[0].subtract(new Area(new Ellipse2D.Double(x - holeDistance / 2 - holeSize / 2, y - holeSize / 2, holeSize,
          holeSize)));
      body[0].subtract(new Area(new Ellipse2D.Double(x + holeDistance / 2 - holeSize / 2, y - holeSize / 2, holeSize,
          holeSize)));

      switch (orientation) {
        case DEFAULT:
          break;
        case _90:
          body[0].transform(AffineTransform.getRotateInstance(Math.PI / 2, x, y));
          break;
        case _180:
          body[0].transform(AffineTransform.getRotateInstance(Math.PI, x, y));
          break;
        case _270:
          body[0].transform(AffineTransform.getRotateInstance(Math.PI * 3 / 2, x, y));
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }

      body[1] =
          new Area(new Ellipse2D.Double(x - innerDiameter / 2, y - innerDiameter / 2, innerDiameter, innerDiameter));
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    int pinSize = (int) PIN_DIAMETER.convertToPixels() / 2 * 2;
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    
    for (Point2D point : controlPoints) {
      if (!outlineMode) {
        g2d.setColor(PIN_COLOR);
        g2d.fillOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
      }
      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);      
      g2d.drawOval((int)(point.getX() - pinSize / 2), (int)(point.getY() - pinSize / 2), pinSize, pinSize);
    }
    
    Area mainArea = getBody()[0];
    Area innerArea = getBody()[1];
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(mainArea);
    g2d.setComposite(oldComposite);
    Color finalBorderColor;
    
    if (outlineMode) {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : borderColor;
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);
    g2d.draw(innerArea);   

    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor;
    if (outlineMode) {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : getValue();
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = mainArea.getBounds();
    int x = (int) (bounds.getX() + (bounds.width - textWidth) / 2);
    int y = (int) (bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent());
    g2d.drawString(label, x, y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    int largeR = getClosestOdd(width * 3d / 8);
    int smallR = getClosestOdd(width / 6d);
    int innerD = getClosestOdd(width / 2d);    
    int hole = 4 * width / 32;
    
    Area area = new TwoCircleTangent(new Point2D.Double(width * 0.5, height * 0.5), new Point2D.Double(width / 2, height / 8d), largeR, smallR);
    area.add((Area)new TwoCircleTangent(new Point2D.Double(width * 0.5, height * 0.5), new Point2D.Double(width / 2, height * 7 / 8d), largeR, smallR));
    
    area.subtract(new Area(new Ellipse2D.Double((width - hole) / 2, height / 8 - hole / 2, hole, hole)));
    area.subtract(new Area(new Ellipse2D.Double((width - hole) / 2, height * 7 / 8 - hole / 2, hole, hole)));
    area.transform(AffineTransform.getRotateInstance(Math.PI / 4, width / 2, height / 2));
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(area);
    g2d.drawOval((width - innerD) / 2, (height - innerD) / 2, innerD, innerD);
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
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.NAME;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Area area = new Area();
    Area[] body = getBody();
    int margin = 20;
    for (Area a : body)
      if (a != null)
        area.add(a);
    Rectangle2D bounds = area.getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin, bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }
}
