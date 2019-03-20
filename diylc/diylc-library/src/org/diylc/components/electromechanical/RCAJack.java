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
package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.TwoCircleTangent;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "RCA Jack", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Panel mount RCA phono jack socket", zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J")
public class RCAJack extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.black;

  private static Size BODY_DIAMETER = new Size(0.52d, SizeUnit.in);
  private static Size WAFER_DIAMETER = new Size(0.2d, SizeUnit.in);  
  private static Size HEX_DIAMETER = new Size(0.44d, SizeUnit.in);
  private static Size SPRING_LENGTH = new Size(0.563d, SizeUnit.in);
  private static Size SPRING_WIDTH = new Size(0.12d, SizeUnit.in);
  private static Size HOLE_DIAMETER = new Size(0.05d, SizeUnit.in);
  private static Size HOLE_TO_EDGE = new Size(0.063d, SizeUnit.in);

  private String value = "";
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0) };
  transient Area[] body;
  private Orientation orientation = Orientation.DEFAULT;

  public RCAJack() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
//    if (componentState != ComponentState.DRAGGING) {
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
    g2d.fill(body[0]);

    g2d.setComposite(oldComposite);
//    }

    Color finalBorderColor;

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor =  WAFER_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
    
    drawingObserver.startTrackingContinuityArea(true);
    g2d.fill(body[1]);
    g2d.fill(body[2]);
    drawingObserver.stopTrackingContinuityArea();
    
    if (body[3] != null)
      g2d.fill(body[3]);    

    g2d.setComposite(oldComposite);

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);
    g2d.draw(body[2]);
    if (body[3] != null)
      g2d.draw(body[3]);    
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[4];

      int x0 = controlPoints[0].x;
      int y0 = controlPoints[0].y;
      int x1 = controlPoints[1].x;
      int y1 = controlPoints[1].y;      
      int bodyDiameter = getClosestOdd(BODY_DIAMETER.convertToPixels());
      int waferDiameter = getClosestOdd(WAFER_DIAMETER.convertToPixels());      
      int springWidth = (int) SPRING_WIDTH.convertToPixels();
      int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      double hexDiameter = HEX_DIAMETER.convertToPixels();

      Area wafer =
          new Area(new Ellipse2D.Double(x0 - waferDiameter / 2, y0 - waferDiameter / 2, waferDiameter,
              waferDiameter));
      wafer.subtract(new Area(new Ellipse2D.Double(x0 - holeDiameter / 2, y0 - holeDiameter / 2, holeDiameter,
          holeDiameter)));

      body[0] = wafer;
      
      Area tip = new TwoCircleTangent(controlPoints[0], controlPoints[1], bodyDiameter / 2, springWidth / 2);
      tip.subtract(new Area(
          new Ellipse2D.Double(x1 - holeDiameter / 2, y1 - holeDiameter / 2, holeDiameter, holeDiameter)));           
      tip.subtract(new Area(new Ellipse2D.Double(x0 - waferDiameter / 2, y0 - waferDiameter / 2, waferDiameter,
          waferDiameter)));

      body[1] = tip;

      Area sleeve =
          new Area(new Ellipse2D.Double(x0 - springWidth / 2, y0 - springWidth / 2,
              springWidth, springWidth));
      sleeve.subtract(new Area(new Ellipse2D.Double(x0 - holeDiameter / 2, y0 - holeDiameter / 2, holeDiameter,
          holeDiameter)));      

      body[2] = sleeve;
      
      Path2D hex = new Path2D.Double();
      for (int i = 0; i < 6; i++) {
        double x = x0 + Math.cos(Math.PI / 3 * i) * hexDiameter / 2;
        double y = y0 + Math.sin(Math.PI / 3 * i) * hexDiameter / 2;
        if (i == 0)
          hex.moveTo(x, y);
        else 
          hex.lineTo(x, y);
      }
      hex.closePath();
      Area hexArea = new Area(hex);
      hexArea.subtract(new Area(new Ellipse2D.Double(x0 - waferDiameter / 2, y0 - waferDiameter / 2, waferDiameter,
          waferDiameter)));
      body[3] = hexArea;
    }

    return body;
  }

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    int springLength = (int) SPRING_LENGTH.convertToPixels();
    int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();

    int centerY = y + springLength - holeToEdge;

    controlPoints[1].setLocation(x,  centerY);    

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
      rotation.transform(controlPoints[1], controlPoints[1]);      
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double x0 = width * 0.35;
    double y0 = height * 0.65;
    double x1 = width * 0.75;
    double y1 = height * 0.25;
    TwoCircleTangent main = new TwoCircleTangent(new Point2D.Double(width * 0.35, height * 0.65), new Point2D.Double(x1, y1), width * 0.3, width * 0.1);
    main.subtract(new Area(new Ellipse2D.Double(x0 - 1, y0 - 1, 3, 3)));
    main.subtract(new Area(new Ellipse2D.Double(x1 - 1, y1 - 1, 2, 2)));
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.setColor(BASE_COLOR);
    g2d.fill(main);
    g2d.setColor(BASE_COLOR.darker());
    g2d.draw(main);
    
    double hexDiameter = width * 0.48;
    
    Path2D hex = new Path2D.Double();
    for (int i = 0; i < 6; i++) {
      double x = x0 + Math.cos(Math.PI / 3 * i) * hexDiameter / 2;
      double y = y0 + Math.sin(Math.PI / 3 * i) * hexDiameter / 2;
      if (i == 0)
        hex.moveTo(x, y);
      else 
        hex.lineTo(x, y);
    }
    hex.closePath();
    g2d.draw(hex);
    
    double waferDiameter = width * 0.22;
//    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    g2d.setColor(WAFER_COLOR);
    g2d.draw(new Ellipse2D.Double(x0 - waferDiameter / 2, y0 - waferDiameter / 2, waferDiameter, waferDiameter));
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

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
    updateControlPoints();
    // Invalidate the body
    body = null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return getName() + (index == 0 ? "Tip" : "Sleeve");
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
