/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
 * 
 */
package org.diylc.components.electromechanical;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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
import org.diylc.components.transform.CliffJackTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Cliff 1/8\" Jack", category = "Electro-Mechanical",
    author = "Branislav Stojkovic", description = "Cliff-style closed 1/8\" phono jack",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J", autoEdit = false,
    transformer = CliffJackTransformer.class, enableCache = true)
public class CliffJack1_8 extends AbstractMultiPartComponent<JackType> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  private static Size ROW_SPACING = new Size(9.4d, SizeUnit.mm);
  private static Size PIN_SPACING = new Size(3.8d, SizeUnit.mm);
  private static Size PIN_WIDTH = new Size(1d, SizeUnit.mm);
  private static Size PIN_THICKNESS = new Size(0.02d, SizeUnit.in);
  private static Color BODY_COLOR = Color.decode("#666666");
  private static Color BORDER_COLOR = Color.black;
  private static Color LABEL_COLOR = Color.white;
  private static Size BODY_WIDTH = new Size(10.8d, SizeUnit.mm);
  private static Size BODY_LENGTH_MONO = new Size(8.6d, SizeUnit.mm);
  private static Size BODY_LENGTH_STEREO = new Size(12.4d, SizeUnit.mm);
  private static Size SHAFT_LENGTH_W_NUT = new Size(5d, SizeUnit.mm);
  private static Size SHAFT_LENGTH_NO_NUT = new Size(2d, SizeUnit.mm);
  private static Size SHAFT_DIA_W_NUT = new Size(8d, SizeUnit.mm);
  private static Size SHAFT_DIA_NO_NUT = new Size(7d, SizeUnit.mm);

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  private JackType type = JackType.MONO;
  private Orientation orientation = Orientation.DEFAULT;
  transient private Shape[] body;
  private boolean nut = false;

  public CliffJack1_8() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    // invalidate body shape
    body = null;
    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();
    int rowSpacing = (int) ROW_SPACING.convertToPixels();
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    controlPoints = new Point2D[type == JackType.STEREO ? 6 : 4];

    controlPoints[0] = new Point2D.Double(x, y);
    controlPoints[1] = new Point2D.Double(x, y + rowSpacing);
    controlPoints[2] = new Point2D.Double(x + pinSpacing, y);
    controlPoints[3] = new Point2D.Double(x + pinSpacing, y + rowSpacing);
    if (type == JackType.STEREO) {
      controlPoints[4] = new Point2D.Double(x + 2 * pinSpacing, y);
      controlPoints[5] = new Point2D.Double(x + 2 * pinSpacing, y + rowSpacing);
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
      body = new Shape[4];

      // Create body.
      double bodyLength =
          (type == JackType.MONO ? BODY_LENGTH_MONO : BODY_LENGTH_STEREO).convertToPixels();
      double bodyWidth = BODY_WIDTH.convertToPixels();
      int shaftLength = (int) (nut ? SHAFT_LENGTH_W_NUT : SHAFT_LENGTH_NO_NUT).convertToPixels();
      int shaftWidth = (int) (nut ? SHAFT_DIA_W_NUT : SHAFT_DIA_NO_NUT).convertToPixels();
      double centerX =
          (controlPoints[0].getX() + controlPoints[controlPoints.length - 1].getX()) / 2;
      double centerY =
          (controlPoints[0].getY() + controlPoints[controlPoints.length - 1].getY()) / 2;

      body[0] = new Area(new Rectangle2D.Double(centerX - bodyLength / 2, centerY - bodyWidth / 2,
          bodyLength, bodyWidth));

      body[1] = new Area(new Rectangle2D.Double(centerX + bodyLength / 2, centerY - shaftWidth / 2,
          shaftLength, shaftWidth));

      // Apply rotation if necessary
      AffineTransform rotation = null;
      double angle = getAngle();
      if (angle != 0) {
        rotation = AffineTransform.getRotateInstance(angle, centerX, centerY);
        for (int i = 0; i < body.length; i++) {
          if (body[i] != null) {
            Area area = new Area(body[i]);
            area.transform(rotation);
            body[i] = area;
          }
        }
      }

      GeneralPath path = new GeneralPath();
      int step = 4;
      for (double i = centerX + bodyLength / 2 + step; i <= centerX + bodyLength / 2
          + shaftLength; i += step) {
        Point2D p = new Point2D.Double(i, centerY - shaftWidth / 2 + 1);
        if (rotation != null) {
          rotation.transform(p, p);
        }
        path.moveTo(p.getX(), p.getY());
        p = new Point2D.Double(i - step, centerY + shaftWidth / 2 - 1);
        if (rotation != null) {
          rotation.transform(p, p);
        }
        path.lineTo(p.getX(), p.getY());
      }
      body[2] = path;

      // Create pins.
      Area pins = new Area();

      int pinWidth = (int) PIN_WIDTH.convertToPixels();
      int pinThickness = (int) PIN_THICKNESS.convertToPixels();
      for (int i = 0; i < getControlPointCount(); i++) {
        Point2D point = getControlPoint(i);
        Rectangle2D pin;
        if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
          pin = new Rectangle2D.Double(point.getX() - pinWidth / 2, point.getY() - pinThickness / 2,
              pinWidth, pinThickness);
        } else {
          pin = new Rectangle2D.Double(point.getX() - pinThickness / 2, point.getY() - pinWidth / 2,
              pinThickness, pinWidth);
        }
        pins.add(new Area(pin));
      }

      body[3] = pins;
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    // if (componentState != ComponentState.DRAGGING) {
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BODY_COLOR);
    for (int i = 0; i < 2; i++) {
      g2d.fill(body[i]);
    }
    g2d.setComposite(oldComposite);
    // }
    
    if (nut) {
      g2d.setColor(BORDER_COLOR);
      g2d.draw(body[2]);
    }

    Color finalBorderColor;
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
        Constants.DEFAULT_THEME);
    if (outlineMode) {
      finalBorderColor = theme.getOutlineColor();
    } else {
      finalBorderColor = BORDER_COLOR;
    }

    drawingObserver.stopTracking();

    g2d.setColor(finalBorderColor);
    for (int i = 0; i < 2; i++) {
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
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? LABEL_COLOR_SELECTED
              : LABEL_COLOR;
    }
    g2d.setColor(finalLabelColor);
    g2d.setFont(project.getFont());

    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));

    double centerX = (controlPoints[0].getX() + controlPoints[controlPoints.length - 1].getX()) / 2;
    double centerY = (controlPoints[0].getY() + controlPoints[controlPoints.length - 1].getY()) / 2;
    StringUtils.drawCenteredText(g2d, name, centerX, centerY, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);

    drawSelectionOutline(g2d, componentState, outlineMode, project, drawingObserver);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int bodyWidth = getClosestOdd(width * 3 / 5);
    int tailWidth = getClosestOdd(width * 3 / 9);

    g2d.setColor(BODY_COLOR);
    g2d.fillRect((width - tailWidth) / 2, 2 * 32 / height, tailWidth, height / 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - tailWidth) / 2, 2 * 32 / height, tailWidth, height / 2);

    g2d.setColor(BODY_COLOR);
    g2d.fillRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((width - bodyWidth) / 2, height / 7 + 1, bodyWidth, height * 5 / 7);

    g2d.setColor(METAL_COLOR);
    int pinX1 = getClosestOdd((width - bodyWidth * 3 / 4) / 2);
    int pinX2 = getClosestOdd((width + bodyWidth * 3 / 4) / 2) - 1;
    g2d.drawLine(pinX1, width * 4 / 16, pinX1, width * 5 / 16);
    g2d.drawLine(pinX1, width * 11 / 16, pinX1, width * 12 / 16);
    g2d.drawLine(pinX2, width * 4 / 16, pinX2, width * 5 / 16);
    g2d.drawLine(pinX2, width * 11 / 16, pinX2, width * 12 / 16);
    
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(LABEL_FONT.deriveFont(width / 3.5f));
    
    RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    g2d.setRenderingHints(rh);
    
    StringUtils.drawCenteredText(g2d, "3.5", width / 2 - 1, height / 2, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
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
  public boolean getNut() {
    return nut;
  }

  public void setNut(boolean nut) {
    this.nut = nut;
    this.body = null;
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
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
}
