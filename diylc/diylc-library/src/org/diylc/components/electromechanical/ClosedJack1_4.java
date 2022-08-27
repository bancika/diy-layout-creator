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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.transform.ClosedJackTransformer;
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

@ComponentDescriptor(name = "Closed 1/4\" Jack", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "Enclosed panel mount 1/4\" phono jack", zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "J", autoEdit = false, transformer = ClosedJackTransformer.class, enableCache = true)
public class ClosedJack1_4 extends AbstractMultiPartComponent<String> {

  private static final long serialVersionUID = 1L;

  private static Size SPACING = new Size(0.1d, SizeUnit.in);
  private static Size LUG_WIDTH = new Size(0.1d, SizeUnit.in);
  private static Size LUG_LENGTH = new Size(0.12d, SizeUnit.in);
  private static Size LUG_HOLE_SIZE = new Size(1d, SizeUnit.mm);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color SHAFT_COLOR = Color.decode("#AAAAAA");
  private static Size SHAFT_LENGTH = new Size(0.25d, SizeUnit.in);
  private static Size SHAFT_WIDTH = new Size(3d / 8, SizeUnit.in);
  private static Color BORDER_COLOR = Color.black;
  private static Color LABEL_COLOR = Color.white;
  private static Size BODY_WIDTH = new Size(0.65d, SizeUnit.in);
  private static Size BODY_LENGTH = new Size(0.8d, SizeUnit.in);

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private JackType type = JackType.MONO;
  private Orientation orientation = Orientation.DEFAULT;
  transient private Shape[] body;
  private String value = "";

  public ClosedJack1_4() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    // invalidate body shape
    body = null;
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    int spacing = (int) SPACING.convertToPixels();
    int bodyLength = (int) BODY_LENGTH.convertToPixels();
    controlPoints = new Point2D[type == JackType.STEREO ? 3 : 2];

    controlPoints[0] = new Point2D.Double(x, y);
    controlPoints[1] = new Point2D.Double(x + bodyLength, y);
    if (type == JackType.STEREO) {
      controlPoints[2] = new Point2D.Double(x, y + 2 * spacing);
    }

    // Apply rotation if necessary
    double angle = getAngle();
    if (angle != 0) {
      AffineTransform rotation = AffineTransform.getRotateInstance(angle, x, y);
      for (int i = 1; i < controlPoints.length; i++) {
        rotation.transform(controlPoints[i], controlPoints[i]);
      }
    }
  }

  private double getAngle() {
    // Apply rotation if necessary
    double angle;
    switch (orientation) {
      case _90:
        angle = Math.PI / 2;
        break;
      case _180:
        angle = Math.PI;
        break;
      case _270:
        angle = Math.PI * 3 / 2;
        break;
      default:
        angle = 0;
    }

    return angle;
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[5];

      // Create body.
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      int lugWidth = (int) LUG_WIDTH.convertToPixels();
      int lugLength = (int) LUG_LENGTH.convertToPixels();
      int lugHoleSize = (int) LUG_HOLE_SIZE.convertToPixels();
      int bodyLength = (int) BODY_LENGTH.convertToPixels();
      int bodyWidth = (int) BODY_WIDTH.convertToPixels();
      body[0] = new Area(new Rectangle2D.Double(x + lugLength, y - bodyWidth / 2, bodyLength, bodyWidth));

      int shaftLength = (int) SHAFT_LENGTH.convertToPixels();
      int shaftWidth = (int) SHAFT_WIDTH.convertToPixels();
      Area shaft = new Area(new Rectangle2D.Double(x + lugLength + bodyLength, y - shaftWidth / 2, shaftLength, shaftWidth));
      body[1] = shaft;

      double angle = getAngle();
      AffineTransform rotation = null;
      if (angle != 0) {
        rotation = AffineTransform.getRotateInstance(angle, x, y);
      }

      GeneralPath path = new GeneralPath();
      int step = 4;
      for (double i = x + lugLength + bodyLength + step; i <= x + lugLength + bodyLength + shaftLength; i += step) {
        Point2D p = new Point2D.Double(i, y - shaftWidth / 2 + 1);
        if (rotation != null) {
          rotation.transform(p, p);
        }
        path.moveTo(p.getX(), p.getY());
        p = new Point2D.Double(i - step, y + shaftWidth / 2 - 1);
        if (rotation != null) {
          rotation.transform(p, p);
        }
        path.lineTo(p.getX(), p.getY());
      }
//      Area pathArea = new Area(path);
//      pathArea.intersect(shaft);
      body[2] = path;

      // Create lugs.
      Area lugs = new Area();

      int spacing = (int) SPACING.convertToPixels();
      Point2D[] untransformedControlPoints = new Point2D[type == JackType.STEREO ? 3 : 2];

      untransformedControlPoints[0] = new Point2D.Double(x, y);
      untransformedControlPoints[1] = new Point2D.Double(x + bodyLength, y);
      if (type == JackType.STEREO) {
        untransformedControlPoints[2] = new Point2D.Double(x, y + 2 * spacing);
      }

      for (int i = 0; i < untransformedControlPoints.length; i++) {
        Point2D point = untransformedControlPoints[i];
        Area lug = new Area(new Ellipse2D.Double(point.getX() - lugWidth / 2, point.getY() - lugWidth / 2, lugWidth, lugWidth));
        lug.add(new Area(new Rectangle2D.Double(point.getX(), point.getY() - lugWidth / 2, lugLength, lugWidth)));
        lug.subtract(new Area(new Ellipse2D.Double(point.getX() - lugHoleSize / 2, point.getY() - lugHoleSize / 2, lugHoleSize,
            lugHoleSize)));
        lugs.add(lug);
      }

      body[3] = lugs;

      // Rotate everything that's of Area type, e.g. everything but lines.
      if (rotation != null) {
        for (Shape shape : body) {
          if (shape instanceof Area) {
            Area area = (Area) shape;
            area.transform(rotation);
          }
        }
      }
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    // Rectangle bounds = body.getBounds();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
//    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
      g2d.fill(body[0]);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : SHAFT_COLOR);
      g2d.fill(body[1]);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : METAL_COLOR);
      g2d.fill(body[3]);
      g2d.setComposite(oldComposite);
//    }

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BORDER_COLOR;
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    g2d.draw(body[1]);
    if (!outlineMode) {
      g2d.setColor(SHAFT_COLOR.darker());
//      g2d.fill(body[2]);
      g2d.draw(body[2]);
    }

    // Pins are the last piece.
    g2d.setColor(outlineMode ? finalBorderColor : METAL_COLOR.darker());
    g2d.draw(body[3]);

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
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Rectangle bounds = body[0].getBounds();
    double centerX = bounds.getX() + bounds.width / 2;
    double centerY = bounds.getY() + bounds.height / 2;
    StringUtils.drawCenteredText(g2d, name, centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyWidth = getClosestOdd(width * 3 / 5);
    int tailWidth = getClosestOdd(width * 3 / 10);

    g2d.setColor(SHAFT_COLOR);
    g2d.fillRect((width - tailWidth) / 2, 1, tailWidth, height / 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - tailWidth) / 2, 1, tailWidth, height / 2);

    g2d.setColor(BODY_COLOR);
    g2d.fillRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);

    g2d.setColor(METAL_COLOR);

    g2d.fillRect(width * 7 / 16, height * 6 / 7 + 1, width / 8, height / 7 - 1);
    g2d.fillRect(width * 7 / 16, height / 7 + 2, width / 8, height / 7 - 1);
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
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @EditableProperty
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public JackType getType() {
    return type;
  }

  public void setType(JackType type) {
    this.type = type;
    updateControlPoints();
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    switch (index) {
      case 0:
        return "Tip";
      case 1:
        return "Sleeve";
      case 2:
        return "Ring";
    }
    return null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
