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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.RoundedPolygon;
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

@ComponentDescriptor(name = "IEC Socket", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Panel mounted IEC power socket", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "IEC", autoEdit = false)
public class IECSocket extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;  

  // common
  private static Size HORIZONTAL_SPACING = new Size(0.3d, SizeUnit.in);
  private static Size VERTICAL_SPACING = new Size(0.2d, SizeUnit.in);
  private static Size LUG_WIDTH = new Size(4d, SizeUnit.mm);
  private static Size LUG_THICKNESS = new Size(0.8d, SizeUnit.mm);
  
  // simple  
  private static Size SIMPLE_CUTOUT_LENGTH = new Size(27.5d, SizeUnit.mm);
  private static Size SIMPLE_CUTOUT_WIDTH = new Size(19.5d, SizeUnit.mm);
  private static Size SIMPLE_BASE_LENGTH = new Size(30.8d, SizeUnit.mm);
  private static Size SIMPLE_BASE_WIDTH = new Size(22.6d, SizeUnit.mm);
  private static Size SIMPLE_BASE_RADIUS = new Size(3d, SizeUnit.mm);
  private static Size SIMPLE_CUTOUT_SLANT = new Size(5d, SizeUnit.mm);
  private static Size SIMPLE_CUTOUT_RADIUS = new Size(1d, SizeUnit.mm);
  private static Size SIMPLE_LENGTH = new Size(50d, SizeUnit.mm);
  private static Size SIMPLE_OUTER_RADIUS = new Size(6d, SizeUnit.mm);
  private static Size HOLE_SPACING = new Size(40d, SizeUnit.mm);
  private static Size HOLE_DIAMETER = new Size(3d, SizeUnit.mm);
  
  private static Color BODY_COLOR = Color.decode("#555555");
  private static Color BORDER_COLOR = BODY_COLOR.darker();

  protected Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  transient protected Area[] body;
  protected String name;
  protected String value;
  private Orientation orientation = Orientation.DEFAULT;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;

  public IECSocket() {
    super();
    updateControlPoints();
  }

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    Point firstPoint = controlPoints[0];
    int hSpacing = (int) HORIZONTAL_SPACING.convertToPixels();
    int vSpacing = (int) VERTICAL_SPACING.convertToPixels();
   
    controlPoints[1].setLocation(firstPoint.x - hSpacing, firstPoint.y + vSpacing);
    controlPoints[2].setLocation(firstPoint.x + hSpacing, firstPoint.y + vSpacing);
    
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
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, firstPoint.x, firstPoint.y);

      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }      
    }   
  }

  @Override
  public Point getControlPoint(int index) {
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
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    controlPoints[index].setLocation(point);
    // Reset body shape.
    body = null;
  }

  @EditableProperty
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
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
    if (orientation == null) {
      orientation = Orientation.DEFAULT;
    }
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }
  
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area[] body = getBody();
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    // Draw body if available.
    if (body != null) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
      for (Area a : body)
        if (a != null) {
          g2d.fill(a);
          break;
        }
      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      Color finalBorderColor;
      if (outlineMode) {
        finalBorderColor = theme.getOutlineColor();
      } else {
        finalBorderColor = getBorderColor();
      }
      g2d.setColor(finalBorderColor);
      for (Area a : body)
        if (a != null)
          g2d.draw(a);
    }
    // Do not track these changes because the whole switch has been tracked
    // so far.
    drawingObserver.stopTracking();

    // Draw lugs.  
    int lugWidth = getClosestOdd((int) LUG_WIDTH.convertToPixels());
    int lugHeight = getClosestOdd((int) LUG_THICKNESS.convertToPixels());
    
    if (orientation == Orientation._90 || orientation == Orientation._270) {
      int p = lugHeight;
      lugHeight = lugWidth;
      lugWidth = p;
    }
    
    for (Point p : controlPoints) {
      if (outlineMode) {
        g2d.setColor(theme.getOutlineColor());
        g2d.drawRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
      } else {
        g2d.setColor(METAL_COLOR);
        g2d.fillRect(p.x - lugWidth / 2, p.y - lugHeight / 2, lugWidth, lugHeight);
      }
    }
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @SuppressWarnings("incomplete-switch")
  public Area[] getBody() {
    if (body == null) {
      Point firstPoint = controlPoints[0];
      int vSpacing = (int) VERTICAL_SPACING.convertToPixels();
      
      double cutoutRadius;
      double cutoutLength;
      double cutoutWidth;
      double cutoutSlant;
      double baseLength;
      double baseWidth = SIMPLE_BASE_WIDTH.convertToPixels();;
      double baseRadius;
      double length;
      double outerRadius;
      double holeDiameter = HOLE_DIAMETER.convertToPixels();
      double holeSpacing = HOLE_SPACING.convertToPixels();
      
      body = new Area[3];
           
      cutoutRadius = SIMPLE_CUTOUT_RADIUS.convertToPixels();
      baseRadius = SIMPLE_BASE_RADIUS.convertToPixels();
      cutoutLength = SIMPLE_CUTOUT_LENGTH.convertToPixels();
      cutoutWidth = SIMPLE_CUTOUT_WIDTH.convertToPixels();
      cutoutSlant = SIMPLE_CUTOUT_SLANT.convertToPixels();
      baseLength = SIMPLE_BASE_LENGTH.convertToPixels();
      length = SIMPLE_LENGTH.convertToPixels();
      outerRadius = SIMPLE_OUTER_RADIUS.convertToPixels();
      
      Point[] outerPoints = new Point[] {
          new Point((int) Math.round(firstPoint.x), (int) Math.round(firstPoint.y + vSpacing / 2 - baseWidth / 2)),
          new Point((int) Math.round(firstPoint.x + baseLength / 2), (int) Math.round(firstPoint.y + vSpacing / 2 - baseWidth / 2)),
          new Point((int) Math.round(firstPoint.x + length / 2), (int) Math.round(firstPoint.y + vSpacing / 2)),
          new Point((int) Math.round(firstPoint.x + baseLength / 2), (int) Math.round(firstPoint.y + vSpacing / 2 + baseWidth / 2)),
          new Point((int) Math.round(firstPoint.x - baseLength / 2), (int) Math.round(firstPoint.y + vSpacing / 2 + baseWidth / 2)),
          new Point((int) Math.round(firstPoint.x - length / 2), (int) Math.round(firstPoint.y + vSpacing / 2)),
          new Point((int) Math.round(firstPoint.x - baseLength / 2), (int) Math.round(firstPoint.y + vSpacing / 2 - baseWidth / 2)),
      };
      
      double[] outerRadiuses = new double[] {
        baseRadius / 2,
        outerRadius,
        baseRadius / 2, 
        baseRadius / 2,
        outerRadius,
        baseRadius / 2
      };
      
      body[0] = new Area(new RoundedPolygon(outerPoints, outerRadiuses));
      body[0].subtract(new Area(new Ellipse2D.Double(firstPoint.x - holeSpacing / 2 - holeDiameter / 2, firstPoint.y + vSpacing / 2 - holeDiameter / 2, holeDiameter, holeDiameter)));
      body[0].subtract(new Area(new Ellipse2D.Double(firstPoint.x + holeSpacing / 2 - holeDiameter / 2, firstPoint.y + vSpacing / 2 - holeDiameter / 2, holeDiameter, holeDiameter)));
      
      body[1] = new Area(new RoundRectangle2D.Double(firstPoint.x - baseLength / 2, firstPoint.y + vSpacing / 2 - baseWidth / 2, baseLength, baseWidth, baseRadius, baseRadius));
      
      Point[] cutoutPoints = new Point[] {
          new Point(firstPoint.x, (int) (firstPoint.y + vSpacing / 2 - cutoutWidth / 2)),
          new Point((int) (firstPoint.x + cutoutLength / 2 - cutoutSlant), (int) (firstPoint.y + vSpacing / 2 - cutoutWidth / 2)),
          new Point((int) (firstPoint.x + cutoutLength / 2), (int) (firstPoint.y + vSpacing / 2 - cutoutWidth / 2 + cutoutSlant)),
          new Point((int) (firstPoint.x + cutoutLength / 2), (int) (firstPoint.y + vSpacing / 2 + cutoutWidth / 2)),
          new Point((int) (firstPoint.x - cutoutLength / 2), (int) (firstPoint.y + vSpacing / 2 + cutoutWidth / 2)),
          new Point((int) (firstPoint.x - cutoutLength / 2), (int) (firstPoint.y + vSpacing / 2 - cutoutWidth / 2 + cutoutSlant)),
          new Point((int) (firstPoint.x - cutoutLength / 2 + cutoutSlant), (int) (firstPoint.y + vSpacing / 2 - cutoutWidth / 2)),
      };
      
      double[] cutoutRadiuses = new double[] {
          cutoutRadius,
      };
      
      body[2] = new Area(new RoundedPolygon(cutoutPoints, cutoutRadiuses));
      
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
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, firstPoint.x, firstPoint.y);
        for (Area a : body)
          if (a != null)
            a.transform(rotation);
      }     
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {    
    g2d.setColor(BODY_COLOR);
    int margin = (int) (2f * width / 32);
    int slant = (int) (5f * width / 32);
    int terminal = (int) (4f * width / 32);
    int terminalSpacingH = (int) (7f * width / 32);
    int terminalSpacingV = (int) (4f * width / 32);
    RoundedPolygon poly = new RoundedPolygon(new Point[] {
        new Point(width / 2, height / 5),
        new Point(width - margin - slant, height / 5),
        new Point(width - margin, height / 5 + slant),
        new Point(width - margin, height * 4 / 5),
        new Point(margin, height * 4 / 5),
        new Point(margin, height / 5 + slant),
        new Point(margin + slant, height / 5),
    }, new double[] { 2d });
    g2d.fill(poly);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(poly);
    g2d.setColor(METAL_COLOR);
    for (int i = -1; i <= 1; i++) {      
      g2d.drawLine(width / 2 + terminalSpacingH * i - terminal / 2, height / 2 + terminalSpacingV * Math.abs(i) - terminalSpacingV / 2,
          width / 2 + terminalSpacingH * i + terminal / 2, height / 2 + terminalSpacingV * Math.abs(i) - terminalSpacingV / 2);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null)
      bodyColor = BODY_COLOR;
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null)
      borderColor = BORDER_COLOR;
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
