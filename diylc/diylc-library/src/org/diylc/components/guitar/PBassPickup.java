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
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
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

@ComponentDescriptor(name = "P- Bass Pickup", category = "Guitar", author = "Branislav Stojkovic",
    description = "Split-coil pickup for P-Bass and similar guitars", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PKP", autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram")
public class PBassPickup extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#333333");;
  private static Color POINT_COLOR = Color.gray;

  private static Size WIDTH = new Size(1.1, SizeUnit.in);
  private static Size LENGTH = new Size(2.2, SizeUnit.in);
  private static Size EDGE_RADIUS = new Size(0.08d, SizeUnit.in);
  private static Size LIP_RADIUS = new Size(0.45d, SizeUnit.in);
  private static Size LIP_HOLE_SIZE = new Size(0.1d, SizeUnit.in);
  private static Size LIP_HOLE_SPACING = new Size(0.1d, SizeUnit.in);

  private static Size POINT_MARGIN = new Size(1.5d, SizeUnit.mm);
  private static Size POINT_SIZE = new Size(2d, SizeUnit.mm);
  private static Size POLE_SIZE = new Size(4d, SizeUnit.mm);
  private static Size POLE_SPACING = new Size(0.38d, SizeUnit.in);  

  private String value = "";
  private Point controlPoint = new Point(0, 0);
  transient Shape[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private Color color = BODY_COLOR;
  private Color poleColor = METAL_COLOR;

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    g2d.fill(body[0]);
    g2d.fill(body[1]);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : POINT_COLOR);
    g2d.fill(body[2]);
    g2d.setComposite(oldComposite);

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : color.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    g2d.draw(body[1]);
    
    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);
    }

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
    g2d.setFont(project.getFont());
    Rectangle bounds = body[0].getBounds();
    drawCenteredText(g2d, value, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      int x = controlPoint.x;
      int y = controlPoint.y;
      int width = (int) WIDTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int edgeRadius = (int) EDGE_RADIUS.convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels(); 
      int lipRadius = (int) LIP_RADIUS.convertToPixels();
      int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());
      int lipHoleSpacing = getClosestOdd(LIP_HOLE_SPACING.convertToPixels());

      body[0] =
          new Area(new RoundRectangle2D.Double(x + pointMargin - length, y - pointMargin, length, width, edgeRadius,
              edgeRadius));
      
      Area lip = new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip.subtract(new Area(new Ellipse2D.Double(-lipHoleSize / 2 + lipHoleSpacing, -lipHoleSize / 2, lipHoleSize, lipHoleSize)));
      lip.transform(AffineTransform.getTranslateInstance(x + pointMargin, y - pointMargin + width / 2));
      Area lip2 = new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip2 = new Area(new Ellipse2D.Double(-lipRadius / 2, -lipRadius / 2, lipRadius, lipRadius));
      lip2.subtract(new Area(new Ellipse2D.Double(-lipHoleSize / 2 - lipHoleSpacing, -lipHoleSize / 2, lipHoleSize, lipHoleSize)));
      lip2.transform(AffineTransform.getTranslateInstance(x + pointMargin - length, y - pointMargin + width / 2));
      lip.add(lip2);
      
      body[1] = new Area(lip);
      
      ((Area)body[1]).subtract((Area)body[0]);

      body[2] = new Area(new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));

      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();      
      int poleMargin = (length - poleSpacing * 3) / 2;
      Area poleArea = new Area();
      for (int i = 0; i < 4; i++) {
        Ellipse2D pole =
            new Ellipse2D.Double(x + pointMargin - length + poleMargin + i * poleSpacing - poleSize / 2, y
                - pointMargin - poleSize / 2 + width / 2, poleSize, poleSize);
        poleArea.add(new Area(pole));
      }
      body[3] = poleArea;

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        double theta = 0;
        switch (orientation) {
          case _90:
            theta = Math.PI / 2;
            break;
          case _180:
            theta = Math.PI;
            break;
          case _270:
            theta = Math.PI * 3 / 2;
            break;
        }
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (shape != null)
            area.transform(rotation);
        }
      }
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    int bodyWidth = (int) (13f * width / 32);
    int bodyLength = (int) (30f * width / 32);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f)); 
    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3, 3);
        
    int lipSize = (int) (5f * width / 32);
    
    g2d.fillRoundRect((width - lipSize) / 2, (height - bodyLength) / 2 - lipSize + 1, lipSize, bodyLength + 2 * lipSize - 2, lipSize, lipSize);    

    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRoundRect((width - bodyWidth) / 2, (height - bodyLength) / 2, bodyWidth, bodyLength, 3, 3);

    // g2d.setColor(Color.gray);
    // g2d.drawLine(width / 2, 4 * width / 32, width / 2, 4 * width / 32);
    // g2d.drawLine(width / 2, height - 4 * width / 32, width / 2, height - 4 * width / 32);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = (int) (15d * width / 32);
    for (int i = 0; i < 4; i++) {
      g2d.fillOval((width - poleSize) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 3), poleSize, poleSize);
    }
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoint;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoint.setLocation(point);
    // Invalidate the body
    body = null;
  }

  @EditableProperty(name = "Model")
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    // Invalidate the body
    body = null;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @EditableProperty(name = "Pole Color")
  public Color getPoleColor() {
    if (poleColor == null)
      poleColor = METAL_COLOR;
    return poleColor;
  }

  public void setPoleColor(Color poleColor) {
    this.poleColor = poleColor;
  }
}
