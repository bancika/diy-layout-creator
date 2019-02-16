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
import java.awt.Polygon;
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

@ComponentDescriptor(name = "P-90 Single Coil Pickup", category = "Guitar", author = "Branislav Stojkovic",
    description = "Single coil P-90 guitar pickup, both \"dog ear\" and \"soap bar\"", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PKP", autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram")
public class P90Pickup extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#D8C989");;
//  private static Color POINT_COLOR = Color.darkGray;

  // dog ear
  private static Size DOG_EAR_WIDTH = new Size(41d, SizeUnit.mm);
  private static Size DOG_EAR_LENGTH = new Size(86.9d, SizeUnit.mm);
  private static Size TOTAL_LENGTH = new Size(118.7d, SizeUnit.mm);
  private static Size DOG_EAR_EDGE_RADIUS = new Size(4d, SizeUnit.mm);

  // soap bar
  private static Size SOAP_BAR_WIDTH = new Size(35.3d, SizeUnit.mm);
  private static Size SOAP_BAR_LENGTH = new Size(85.6d, SizeUnit.mm);
  private static Size SOAP_BAR_EDGE_RADIUS = new Size(8d, SizeUnit.mm);

  private static Size LIP_RADIUS = new Size(10d, SizeUnit.mm);
  private static Size POINT_MARGIN = new Size(3.5d, SizeUnit.mm);
  private static Size POINT_SIZE = new Size(2d, SizeUnit.mm);
  private static Size LIP_HOLE_SIZE = new Size(2.5d, SizeUnit.mm);
  private static Size LIP_HOLE_SPACING = new Size(97d, SizeUnit.mm);
  private static Size POLE_SIZE = new Size(4d, SizeUnit.mm);
  private static Size POLE_SPACING = new Size(11.68d, SizeUnit.mm);
  private static Size POINT_SPACING = new Size(0.1d, SizeUnit.in);
  
  private static final int TERMINAL_FONT_SIZE = 11;

  private String value = "";
  private Point controlPoint = new Point(0, 0);
  transient Shape[] body;
  private Orientation orientation = Orientation.DEFAULT;
  private Color color = BODY_COLOR;
  private P90Type type = P90Type.DOG_EAR;
  private Color poleColor = METAL_COLOR;
  private Polarity polarity = Polarity.North;
  
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  
  public P90Pickup() {
    updateControlPoints();
  }
  
  private Point[] getControlPoints() {
    if (controlPoints == null) {
      controlPoints =
          new Point[] {controlPoint, new Point(controlPoint.x, controlPoint.y),
              new Point(controlPoint.x, controlPoint.y), new Point(controlPoint.x, controlPoint.y)};
      updateControlPoints();
    }
    return controlPoints;
  }
  
  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    Point[] points = getControlPoints();
    int pointSpacing = (int) POINT_SPACING.convertToPixels();
    int dx = 0;
    int dy = 1;
    if (orientation != Orientation.DEFAULT) {
      switch (orientation) {
        case _90:
          dx = -1;
          dy = 0;
          break;
        case _180:
          dx = 0;
          dy = -1;
          break;
        case _270:
          dx = 1;
          dy = 0;
          break;
      }
    }
    points[1].setLocation(points[0].x + dx * pointSpacing, points[0].y + dy * pointSpacing);
    points[2]
        .setLocation(points[0].x + 2 * dx * pointSpacing, points[0].y + 2 * dy * pointSpacing);
    points[3]
        .setLocation(points[0].x + 3 * dx * pointSpacing, points[0].y + 3 * dy * pointSpacing);
  }

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
    if (body[1] != null)
      g2d.fill(body[1]);
//    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : POINT_COLOR);
//    g2d.fill(body[2]);
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
    if (body[1] != null)
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
    
    // terminal labels
    Point[] points = getControlPoints();
    g2d.setColor(finalBorderColor);

    g2d.setFont(project.getFont().deriveFont(TERMINAL_FONT_SIZE * 1f));
    int dx = 0;
    int dy = 0;
    switch (orientation) {
      case DEFAULT:        
        dx = (int) (TERMINAL_FONT_SIZE * 0.8);
        dy = 0;  
        break;
      case _90:
        dx = 0;
        dy = (int) (TERMINAL_FONT_SIZE * 0.8);
        break;
      case _180:
        dx = -(int) (TERMINAL_FONT_SIZE * 0.8);
        dy = 0;       
        break;
      case _270:
        dx = 0;
        dy = -(int) (TERMINAL_FONT_SIZE * 0.8);
        break;     
    }
    
    if (getPolarity() == Polarity.North || getPolarity() == Polarity.South) {
      drawCenteredText(g2d, getPolarity().name().substring(0, 1), (points[1].x + points[2].x) / 2 + dx, (points[1].y + points[2].y) / 2 + dy, HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
    } else {
      drawCenteredText(g2d, "N", (points[0].x + points[1].x) / 2 + dx, (points[0].y + points[1].y) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      drawCenteredText(g2d, "S", (points[2].x + points[3].x) / 2 + dx, (points[2].y + points[3].y) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
  }

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      Point[] points = getControlPoints();
      int x = points[0].x;
      int y = points[0].y;
      int width = (int) getType().getWidth().convertToPixels();
      int length = (int) getType().getLength().convertToPixels();
      int edgeRadius = (int) getType().getEdgeRadius().convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels();
      int totalLength = (int) TOTAL_LENGTH.convertToPixels();
      int lipRadius = (int) LIP_RADIUS.convertToPixels();
      int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
      int lipHoleSize = getClosestOdd(LIP_HOLE_SIZE.convertToPixels());
      int lipHoleSpacing = getClosestOdd(LIP_HOLE_SPACING.convertToPixels());

      body[0] =
          new Area(new RoundRectangle2D.Double(x /*+ pointMargin*/ - length, y - pointMargin, length, width, edgeRadius,
              edgeRadius));

      if (getType() == P90Type.DOG_EAR) {
        double rectWidth = (totalLength - length) / Math.sqrt(2);
        RoundRectangle2D roundRect =
            new RoundRectangle2D.Double(-rectWidth / 2, -rectWidth / 2, rectWidth, rectWidth, lipRadius, lipRadius);
        Area leftEar = new Area(roundRect);
        leftEar.transform(AffineTransform.getRotateInstance(Math.PI / 4));
        leftEar.transform(AffineTransform.getScaleInstance(1.1, 1.45));
        leftEar.transform(AffineTransform.getTranslateInstance(x /*+ pointMargin*/ - length, y - pointMargin + width / 2));
        leftEar.subtract((Area) body[0]);
        Area rightEar = new Area(roundRect);
        rightEar.transform(AffineTransform.getRotateInstance(Math.PI / 4));
        rightEar.transform(AffineTransform.getScaleInstance(1.1, 1.45));
        rightEar.transform(AffineTransform.getTranslateInstance(x /*+ pointMargin*/, y - pointMargin + width / 2));
        rightEar.subtract((Area) body[0]);
        Area lipArea = leftEar;
        lipArea.add(rightEar);
        lipArea.subtract(new Area(new Ellipse2D.Double(x /*+ pointMargin*/ - length / 2 - lipHoleSpacing / 2 - lipHoleSize
            / 2, y - pointMargin + width / 2 - lipHoleSize / 2, lipHoleSize, lipHoleSize)));
        lipArea.subtract(new Area(new Ellipse2D.Double(x /*+ pointMargin*/ - length / 2 + lipHoleSpacing / 2 - lipHoleSize
            / 2, y - pointMargin + width / 2 - lipHoleSize / 2, lipHoleSize, lipHoleSize)));

        body[1] = lipArea;
      }

      body[2] = new Area(new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));

      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int poleMargin = (length - poleSpacing * 5) / 2;
      Area poleArea = new Area();
      for (int i = 0; i < 6; i++) {
        Ellipse2D pole =
            new Ellipse2D.Double(x /*+ pointMargin*/ - length + poleMargin + i * poleSpacing - poleSize / 2, y
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

    int baseWidth = 13 * width / 32;
    int baseLength = 27 * width / 32;
    int radius = 6 * width / 32;

    g2d.setColor(BODY_COLOR);
    Polygon base =
        new Polygon(new int[] {width / 2, (width + baseWidth) / 2, (width + baseWidth) / 2, width / 2,
            (width - baseWidth) / 2, (width - baseWidth) / 2}, new int[] {-2, (height - baseLength) / 2,
            (height + baseLength) / 2, height + 1, (height + baseLength) / 2, (height - baseLength) / 2}, 6);
    Area baseArea = new Area(base);
    baseArea.intersect(new Area(new Rectangle(0, -1, width, height + 1)));
    g2d.fill(baseArea);
    g2d.setColor(BODY_COLOR.darker());
    g2d.draw(baseArea);

    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth, baseLength, radius, radius);
    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth, baseLength, radius, radius);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = 17 * width / 32;
    for (int i = 0; i < 6; i++) {
      g2d.fillOval((width - poleSize) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 5), poleSize, poleSize);
    }
  }

  @EditableProperty(name = "Type")
  public P90Type getType() {
    return type;
  }

  public void setType(P90Type type) {
    this.type = type;
    // Invalidate the body
    body = null;
  }

  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    if (getPolarity() != Polarity.Humbucking && (index == 0 || index == 3))
      return VisibilityPolicy.NEVER;
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    getControlPoints()[index].setLocation(point);
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
  
  @Override
  public String getControlPointNodeName(int index) {
    switch (index) {
      case 0:
        if (getPolarity() != Polarity.Humbucking)
          return null;
        return"North Start";
      case 1:
        if (getPolarity() == Polarity.South)
          return "South Start";
        if (getPolarity() == Polarity.North)
          return "North Start";
        return "North Finish";
      case 2:
        if (getPolarity() == Polarity.South)
          return "South Finish";
        if (getPolarity() == Polarity.North)
          return "North Finish";
        return "South Start";
      case 3:
        if (getPolarity() != Polarity.Humbucking)
          return null;
        return "South Finish";
    }
    return null;
  }
  
  @EditableProperty
  public Polarity getPolarity() {
    if (polarity == null)
      polarity = Polarity.North;
    return polarity;
  }
  
  public void setPolarity(Polarity polarity) {
    this.polarity = polarity;
    // Invalidate the body
    body = null;
  }

  public enum P90Type {
    DOG_EAR("Dog Ear", DOG_EAR_LENGTH, DOG_EAR_WIDTH, DOG_EAR_EDGE_RADIUS), SOAP_BAR("Soap Bar", SOAP_BAR_LENGTH,
        SOAP_BAR_WIDTH, SOAP_BAR_EDGE_RADIUS);

    private String label;
    private Size length;
    private Size width;
    private Size edgeRadius;

    private P90Type(String label, Size length, Size width, Size edgeRadius) {
      this.label = label;
      this.length = length;
      this.width = width;
      this.edgeRadius = edgeRadius;
    }

    public Size getLength() {
      return length;
    }

    public Size getWidth() {
      return width;
    }

    public Size getEdgeRadius() {
      return edgeRadius;
    }

    @Override
    public String toString() {
      return label;
    }
  }
  
  public enum Polarity {
    North, South, Humbucking;
  }
}
