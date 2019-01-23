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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
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

@ComponentDescriptor(name = "Pilot Lamp Holder", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Fender-style pilot bulb holder for T2 and T-3 ¼ miniature bayonet lamps", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PL")
public class PilotLampHolder extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");

  private static Size THREAD_OUTER_DIAMETER = new Size(11 / 16d, SizeUnit.in);
  private static Size NUT_DIAMETER = new Size(14 / 16d, SizeUnit.in);
  private static Size THREAD_THICKNESS = new Size(0.05d, SizeUnit.in);
  private static Size WAFER_DIAMETER = new Size(0.2d, SizeUnit.in);
  private static Size INNER_DIAMETER = new Size(0.05d, SizeUnit.in);
  private static Size RING_DIAMETER = new Size(0.15d, SizeUnit.in);
  private static Size SPRING_LENGTH = new Size(0.463d, SizeUnit.in);
  private static Size SPRING_WIDTH = new Size(0.12d, SizeUnit.in);
  private static Size HOLE_DIAMETER = new Size(0.05d, SizeUnit.in);
  private static Size HOLE_TO_EDGE = new Size(0.063d, SizeUnit.in);
  private static Size HOLE_SPACING = new Size(0.1d, SizeUnit.in);

  private String value = "";
  private Point[] controlPoints = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0) };
  transient Area[] body;
  private Orientation orientation = Orientation.DEFAULT;

  public PilotLampHolder() {
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
      finalBorderColor = WAFER_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

//    if (componentState != ComponentState.DRAGGING) {
      oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      
      drawingObserver.startTrackingContinuityArea(true);
      
      g2d.fill(body[3]);
      g2d.fill(body[4]);
      g2d.fill(body[5]);
      
      g2d.fill(body[1]);
      g2d.fill(body[2]);
      
      drawingObserver.stopTrackingContinuityArea();

      g2d.setComposite(oldComposite);
//    }

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    
    g2d.draw(body[3]);
    g2d.draw(body[4]);
    g2d.draw(body[5]);
    
    g2d.draw(body[1]);
    g2d.draw(body[2]);
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @SuppressWarnings("incomplete-switch")
  public Area[] getBody() {
    if (body == null) {
      body = new Area[6];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int threadOuterDiameter = getClosestOdd(THREAD_OUTER_DIAMETER.convertToPixels());
      int threadThickness = getClosestOdd(THREAD_THICKNESS.convertToPixels());
      int nutDiameter = getClosestOdd(NUT_DIAMETER.convertToPixels());
      int waferDiameter = getClosestOdd(WAFER_DIAMETER.convertToPixels());
      int innerDiameter = getClosestOdd(INNER_DIAMETER.convertToPixels());
      int ringDiameter = getClosestOdd(RING_DIAMETER.convertToPixels());
      int springLength = (int) SPRING_LENGTH.convertToPixels();
      int springWidth = (int) SPRING_WIDTH.convertToPixels();
      int holeDiameter = getClosestOdd(HOLE_DIAMETER.convertToPixels());
      int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();

      int centerY = y + springLength - holeToEdge;

      Area wafer =
          new Area(new Ellipse2D.Double(x - waferDiameter / 2, centerY - waferDiameter / 2, waferDiameter,
              waferDiameter));
      wafer.subtract(new Area(new Ellipse2D.Double(x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter,
          ringDiameter)));

      body[0] = wafer;

      Area tip =
          new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength, springWidth, springWidth));
      tip.subtract(new Area(new Ellipse2D.Double(x - waferDiameter / 2, centerY - waferDiameter / 2, waferDiameter, waferDiameter)));

      body[1] = tip;

      Area sleeve =
          new Area(new RoundRectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength,
              springWidth, springWidth));
      sleeve.transform(AffineTransform.getRotateInstance(Math.PI * 0.295, x, centerY));
      sleeve.add(new Area(new Ellipse2D.Double(x - ringDiameter / 2, centerY - ringDiameter / 2, ringDiameter, ringDiameter)));
      sleeve.subtract(new Area(new Ellipse2D.Double(x - innerDiameter / 2, centerY - innerDiameter / 2, innerDiameter, innerDiameter)));

      body[2] = sleeve;  
      tip.subtract(sleeve);
      
      Area thread = new Area(new Ellipse2D.Double(x - threadOuterDiameter / 2, centerY - threadOuterDiameter / 2, threadOuterDiameter, threadOuterDiameter));
      
      Path2D polygon = new Path2D.Double();
      for (int i = 0; i < 6; i++) {
        double theta = Math.PI /3 * i;
        if (i == 0)
          polygon.moveTo(x + nutDiameter / 2 * Math.cos(theta), centerY + nutDiameter / 2 * Math.sin(theta));
        else
          polygon.lineTo(x + nutDiameter / 2 * Math.cos(theta), centerY + nutDiameter / 2 * Math.sin(theta));
      }
      polygon.closePath();
      Area nut = new Area(polygon);
      nut.subtract(thread);
      nut.subtract(new Area(new Rectangle2D.Double(x - springWidth / 2, y - holeToEdge, springWidth, springLength)));
      nut.subtract(sleeve);
      
      thread.subtract(new Area(new Ellipse2D.Double(x - threadOuterDiameter / 2 + threadThickness, centerY - threadOuterDiameter / 2 + threadThickness, 
          threadOuterDiameter - 2 * + threadThickness, threadOuterDiameter - 2 * + threadThickness)));
      thread.subtract(tip);
      thread.subtract(sleeve);
      
      body[3] = thread;
      body[4] = nut;
      
      double linkLength = (int) (Math.sin(Math.PI / 3) * nutDiameter / 2);
      Area link = new Area(new Rectangle2D.Double(x - waferDiameter / 2, centerY, waferDiameter, linkLength));
      link.subtract(new Area(new Ellipse2D.Double(x - waferDiameter / 2, centerY - waferDiameter / 2, waferDiameter,
              waferDiameter)));
      
      body[5] = link;
      
      nut.subtract(link);
      thread.subtract(link);
      
      double theta = 0;
      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
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
      }

      if (theta != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Area area : body) {  
          if (area != null)
            area.transform(rotation);
        }
      }
      
      for (int i = 1; i <= 2; i++)
        for (Point p : controlPoints)
          body[i].subtract(new Area(new Ellipse2D.Double(p.x - holeDiameter / 2, p.y - holeDiameter / 2, holeDiameter, holeDiameter)));
    }

    return body;
  }

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;

    int springLength = (int) SPRING_LENGTH.convertToPixels();
    int holeToEdge = (int) HOLE_TO_EDGE.convertToPixels();
    int holeSpacing = (int) HOLE_SPACING.convertToPixels();

    int centerY = y + springLength - holeToEdge;
    
    AffineTransform rotation = AffineTransform.getRotateInstance(Math.PI * 0.295, x, centerY);

    rotation.transform(controlPoints[0], controlPoints[1]);
    
    controlPoints[2].setLocation(controlPoints[0].x, controlPoints[0].y + holeSpacing);
    rotation.transform(controlPoints[2], controlPoints[3]);

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
      rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int waferDiameter = (int) (7f * width / 32);
    int sleeveDiameter = (int) (7f * width / 32);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 4);

    g2d.setColor(WAFER_COLOR);
    g2d.draw(new Ellipse2D.Double(width / 2 - waferDiameter / 2, height / 2 - waferDiameter / 2, waferDiameter,
        waferDiameter));

    g2d.setColor(BASE_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f * width / 32));
    g2d.draw(new Ellipse2D.Double(width / 2 - sleeveDiameter / 2, height / 2 - sleeveDiameter / 2, sleeveDiameter,
        sleeveDiameter));
    
    g2d.rotate(Math.PI * 0.295, width / 2, height / 2);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);
//
//    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(6f * width / 32));
//    g2d.rotate(-Math.PI / 2, width / 2, height / 2);
//
//    g2d.drawLine(width / 2, 4 * width / 32, width / 2, width / 3);
//    g2d.setColor(BASE_COLOR);
//    int margin = (int) (2f * width / 32);
//    g2d.fillOval(0, 0, width, height);
//    Color jewelColor = Color.decode("#0CDEFF");
//    Color darkerColor = Color.decode("#0AB2CC");
//    RadialGradientPaint gradient = new RadialGradientPaint(new Point(width / 2, height / 2), width / 2 - margin * 2, new float[] {0f,  1f}, new Color[] { jewelColor, darkerColor });
//    g2d.setPaint(gradient);
//    g2d.fillOval(margin, margin, width - margin * 2, height - margin * 2);
//    
//    g2d.setColor(darkerColor);
//    double r = 6f * width / 32;
//    Polygon p = new Polygon();
//    List<Polygon> p1 = new ArrayList<Polygon>();
//    for (int i = 0; i < 6; i++) {
//      double theta = Math.PI / 3 * i;
//      
//      int x = (int) (width / 2 + r * Math.cos(theta));
//      int y = (int) (height / 2 + r * Math.sin(theta));
//      
//      g2d.drawLine(width / 2, height / 2, x, y);
//      p.addPoint(x, y);
//      
//      Polygon pa = new Polygon();
//      pa.addPoint(x, y);
//      int x1 = (int) (x + r * Math.cos(theta - Math.PI / 6));
//      int y1 = (int) (x + r * Math.sin(theta - Math.PI / 6));
//      pa.addPoint(x1, y1);
//      int x2 = (int) (x + r * Math.cos(theta+ Math.PI / 6));
//      int y2 = (int) (x + r * Math.sin(theta+ Math.PI / 6));
//      pa.addPoint(x2, y2);
//      p1.add(pa);
////      
////      Polygon pb = new Polygon();
////      pb.addPoint(x, y);
////      pb.addPoint(x2, y2);
////      int x3 = (int) (x + r * Math.cos(theta + Math.PI / 6));
////      int y3 = (int) (x + r * Math.sin(theta + Math.PI / 6));
////      pb.addPoint(x3, y3);
////      p1.add(pb);
//    }
//    g2d.draw(p);
//    for (Polygon pa : p1)
//      g2d.draw(pa);
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
}
