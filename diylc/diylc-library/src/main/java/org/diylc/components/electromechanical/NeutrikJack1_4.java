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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMultiPartComponent;
import org.diylc.components.transform.NeutrikJack1_4Transformer;
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

@ComponentDescriptor(name = "Neutrik 1/4\" Jack", category = "Electro-Mechanical", author = "Branislav Stojkovic",
    description = "1/4\" mono/stereo phono jack based on Neutrik NMJx series, PCB or Panel-mount",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J", autoEdit = false, transformer = NeutrikJack1_4Transformer.class,
    enableCache = true)
public class NeutrikJack1_4 extends AbstractMultiPartComponent<JackType> {

  private static final long serialVersionUID = 1L;

  private static Size X_SPACING = new Size(0.25d, SizeUnit.in);
  private static Size Y_SPACING = new Size(16.23d, SizeUnit.mm);
  private static Size PIN_WIDTH = new Size(0.1d, SizeUnit.in);
  private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);
  private static Size PIN_DIAMETER = new Size(1.4d, SizeUnit.mm);
  private static Size PIN_OFFSET_MONO = new Size(-6.27d, SizeUnit.mm);
  private static Size PIN_OFFSET_STEREO = new Size(-7.72d, SizeUnit.mm);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color NUT_COLOR = Color.decode("#999999");
  private static Color BORDER_COLOR = Color.black;
  private static Color LABEL_COLOR = Color.white;
  private static Size BODY_WIDTH = new Size(18.2, SizeUnit.mm);
  private static Size BODY_LENGTH = new Size(20.6d, SizeUnit.mm);
  private static Size TAIL_LENGTH = new Size(0.1d, SizeUnit.in);

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private JackType type = JackType.MONO;
  private Mount mount = Mount.Panel;
  private Orientation orientation = Orientation.DEFAULT;
  transient private Area[] body;
  @SuppressWarnings("unused")
  @Deprecated
  private transient String value = "";


  public NeutrikJack1_4() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    // invalidate body shape
    body = null;
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    int xSpacing = (int) X_SPACING.convertToPixels();
    int ySpacing = (int) Y_SPACING.convertToPixels();
    controlPoints = new Point2D[type == JackType.STEREO ? 6 : 4];

    controlPoints[0] = new Point2D.Double(x, y);
    controlPoints[1] = new Point2D.Double(x, y + ySpacing);
    controlPoints[2] = new Point2D.Double(x + 2 * xSpacing, y);
    controlPoints[3] = new Point2D.Double(x + 2 * xSpacing, y + ySpacing);
    if (type == JackType.STEREO) {
      controlPoints[4] = new Point2D.Double(x + xSpacing, y);
      controlPoints[5] = new Point2D.Double(x + xSpacing, y + ySpacing);
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

  public Area[] getBody() {
    if (body == null) {
      body = new Area[5];

      // Create body.
      int bodyLength = (int) BODY_LENGTH.convertToPixels();
      int bodyWidth = (int) BODY_WIDTH.convertToPixels();
      int offset = (int)(type == JackType.MONO ? PIN_OFFSET_MONO : PIN_OFFSET_STEREO).convertToPixels();
      int xSpacing = (int) X_SPACING.convertToPixels();
      int ySpacing = (int) Y_SPACING.convertToPixels();
      double centerX = controlPoints[0].getX() + 2 * xSpacing + offset;
      double centerY = controlPoints[0].getY() + ySpacing / 2;
      body[0] = new Area(new Rectangle2D.Double(centerX - bodyLength / 2, centerY - bodyWidth / 2, bodyLength, bodyWidth));

      int tailLength = (int) TAIL_LENGTH.convertToPixels();
      body[1] = new Area(new RoundRectangle2D.Double(centerX - bodyLength / 2 - tailLength, centerY - bodyWidth / 4, tailLength * 2,
              bodyWidth / 2, tailLength, tailLength));
      Area tailArea = new Area(body[1]);
      tailArea.subtract(new Area(body[0]));
      body[1] = tailArea;

      body[2] = new Area(new Rectangle2D.Double(centerX + bodyLength / 2, centerY - bodyWidth / 4, tailLength, bodyWidth / 2));

      body[3] = new Area(new Rectangle2D.Double(centerX + bodyLength / 2 + tailLength, centerY - bodyWidth / 4, tailLength, bodyWidth / 2));
      tailArea = new Area(body[3]);
      int radius = bodyLength / 2 + tailLength * 2;
      tailArea.intersect(new Area(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2)));
      body[3] = tailArea;

      // Apply rotation if necessary
      double angle = getAngle();
      if (angle != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(angle, controlPoints[0].getX(), controlPoints[0].getY());
        for (int i = 0; i < body.length; i++) {
          if (body[i] != null) {
            Area area = new Area(body[i]);
            area.transform(rotation);
            body[i] = area;
          }
        }
      }

      // Create pins.
      Area pins = new Area();

      int pinWidth = (int) PIN_WIDTH.convertToPixels();
      int pinThickness = (int) PIN_THICKNESS.convertToPixels();
      int pinDiameter = (int) PIN_DIAMETER.convertToPixels();
      for (int i = 0; i < getControlPointCount(); i++) {
        Point2D point = getControlPoint(i);
        Shape pin;
        if (mount == Mount.PCB) {
          pin = new Ellipse2D.Double(point.getX() - pinDiameter / 2, point.getY() - pinDiameter / 2, pinDiameter, pinDiameter);
        } else {
          if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
            pin = new Rectangle2D.Double(point.getX() - pinWidth / 2, point.getY() - pinThickness / 2, pinWidth, pinThickness);
          } else {
            pin = new Rectangle2D.Double(point.getX() - pinThickness / 2, point.getY() - pinWidth / 2, pinThickness, pinWidth);
          }
        }
        pins.add(new Area(pin));
      }

      body[4] = pins;
    }
    return body;
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
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
      for (int i = 0; i < body.length - 1; i++) {
        // Nut is brighter colored.
        if (i == body.length - 2)
          g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : NUT_COLOR);
        g2d.fill(body[i]);
      }
      g2d.setComposite(oldComposite);
//    }

    Color finalBorderColor;
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    if (outlineMode) {
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BORDER_COLOR;
    }
    
    drawingObserver.stopTracking();

    g2d.setColor(finalBorderColor);
    for (int i = 0; i < body.length - 1; i++) {
      g2d.draw(body[i]);
    }

    // Pins are the last piece.
    Shape pins = body[body.length - 1];
    if (!outlineMode) {
      g2d.setColor(METAL_COLOR);
      g2d.fill(pins);
    }
    g2d.setColor(outlineMode ? theme.getOutlineColor() : METAL_COLOR.darker());
    g2d.draw(pins);

    Color finalLabelColor;
    if (outlineMode) {
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
    
    double centerX = (controlPoints[0].getX() + controlPoints[3].getX()) / 2;
    double centerY = (controlPoints[0].getY() + controlPoints[3].getY()) / 2;
    StringUtils.drawCenteredText(g2d, name, centerX, centerY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    
    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyWidth = getClosestOdd(width * 3 / 5);
    int tailWidth = getClosestOdd(width * 3 / 9);

    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - tailWidth) / 2, height / 2, tailWidth, height / 2 - 2 * 32 / height, 4 * 32 / width,
        4 * 32 / width);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect((width - tailWidth) / 2, height / 2, tailWidth, height / 2 - 2 * 32 / height, 4 * 32 / width,
        4 * 32 / width);

    g2d.setColor(NUT_COLOR);
    g2d.fillRoundRect((width - tailWidth) / 2, 2 * 32 / height, tailWidth, height / 2, 4 * 32 / width, 4 * 32 / width);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRoundRect((width - tailWidth) / 2, 2 * 32 / height, tailWidth, height / 2, 4 * 32 / width, 4 * 32 / width);

    g2d.setColor(BODY_COLOR);
    g2d.fillRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);

    g2d.setColor(METAL_COLOR);
    int pinX1 = getClosestOdd((width - bodyWidth * 3 / 4) / 2);
    int pinX2 = getClosestOdd((width + bodyWidth * 3 / 4) / 2) - 1;
    g2d.drawLine(pinX1, width * 2 / 8, pinX1, width * 3 / 8);
    g2d.drawLine(pinX1, width * 5 / 8, pinX1, width * 6 / 8);
    g2d.drawLine(pinX2, width * 2 / 8, pinX2, width * 3 / 8);
    g2d.drawLine(pinX2, width * 5 / 8, pinX2, width * 6 / 8);
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

  @EditableProperty(name = "Type")
  public JackType getValue() {
    return type;
  }

  public void setValue(JackType type) {
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
  
  @EditableProperty
  public Mount getMount() {
    return mount;
  }
  
  public void setMount(Mount mount) {
    this.mount = mount;
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
 
  enum Mount {
    PCB, Panel
  }
}
