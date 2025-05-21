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
package org.diylc.components.tube;

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
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAngledComponent;
import org.diylc.components.transform.AngledComponentTransformer;
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

@ComponentDescriptor(name = "4-pin Jumbo Tube Socket", category = "Tubes",
    author = "Branislav Stojkovic", description = "4-pin Jumbo ceramic tube socket for 211, 805 and 845 tubes",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "J", enableCache = true,
    transformer = AngledComponentTransformer.class)
public class Jumbo4PinTubeSocket extends AbstractAngledComponent<String> {


  private static final Size JUMBO_PIN_SPACING = new Size(98.5d, SizeUnit.mm);
  private static final Size JUMBO_MOUNT_HOLE_SPACING = new Size(72d, SizeUnit.mm);
  private static final Size JUMBO_MOUNT_HOLE_SIZE = new Size(6d, SizeUnit.mm);
  private static final Size JUMBO_TERMINAL_HOLE_SIZE = new Size(4d, SizeUnit.mm);
  private static final Size JUMBO_TERMINAL_LENGTH = new Size(11.5d, SizeUnit.mm);
  private static final Size JUMBO_TERMINAL_WIDTH = new Size(10d, SizeUnit.mm);
  private static final Size JUMBO_DIAMETER = new Size(86d, SizeUnit.mm);
  private static final Size JUMBO_HOLE_SIZE = new Size(30d, SizeUnit.mm);

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#F7F7EF");
  private static Color LABEL_COLOR = BODY_COLOR.darker();
  public static Color TERMINAL_COLOR = METAL_COLOR;
  public static Size PIN_DIAMETER = new Size(1d, SizeUnit.mm);
  public static Size HOLE_SIZE = new Size(5d, SizeUnit.mm);
  public static Size OCTAL_TICK_SIZE = new Size(2d, SizeUnit.mm);
  private static String[] electrodeLabels = new String[] {"G", "F", "P", "F"};

  private String type = "";

  private Color color = BODY_COLOR;

  private Color labelColor = LABEL_COLOR;
  private Color terminalColor = TERMINAL_COLOR;

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  public Jumbo4PinTubeSocket() {
    super();
    updateControlPoints();
  }

  @EditableProperty
  public Color getColor() {
    if (color == null) {
      color = BODY_COLOR;
    }
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int pinCount = 4;
    int pinSpacing = getClosestOdd(JUMBO_PIN_SPACING.convertToPixels());

    double angleIncrement = Math.PI * 2 / pinCount;
    double initialAngleOffset = angleIncrement / 2;

    controlPoints = new Point2D[pinCount + 1];
    double theta = initialAngleOffset + getAngle().getValueRad();
    controlPoints[0] = firstPoint;
    for (int i = 0; i < pinCount; i++) {
      controlPoints[i + 1] =
          new Point2D.Double(firstPoint.getX() + Math.cos(theta) * pinSpacing / 2,
              firstPoint.getY() + Math.sin(theta) * pinSpacing / 2);
      theta += angleIncrement;
    }
  }

  public Shape[] getBody() {
    if (body == null) {
      int bodyDiameter = getClosestOdd(JUMBO_DIAMETER.convertToPixels());

      body = new Shape[2];

      Ellipse2D baseShape = new Ellipse2D.Double(controlPoints[0].getX() - bodyDiameter / 2,
          controlPoints[0].getY() - bodyDiameter / 2, bodyDiameter, bodyDiameter);
      Area baseArea = new Area(baseShape);

      int holeSize = getClosestOdd(JUMBO_HOLE_SIZE.convertToPixels());

      baseArea.subtract(new Area(new Ellipse2D.Double(controlPoints[0].getX() - holeSize / 2,
          controlPoints[0].getY() - holeSize / 2, holeSize, holeSize)));

      double mountingHoleSize = getClosestOdd(JUMBO_MOUNT_HOLE_SIZE.convertToPixels());
      double mountingHoleSpacing = getClosestOdd(JUMBO_MOUNT_HOLE_SPACING.convertToPixels());

      double theta = getAngle().getValueRad();
      int centerX = (int) (controlPoints[0].getX()
          + Math.cos(theta) * (mountingHoleSpacing / 2 - mountingHoleSize / 2));
      int centerY = (int) (controlPoints[0].getY()
          + Math.sin(theta) * (mountingHoleSpacing / 2 - mountingHoleSize / 2));
      baseArea.subtract(new Area(new Ellipse2D.Double(centerX - mountingHoleSize / 2,
          centerY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize)));
      centerX = (int) (controlPoints[0].getX()
          - Math.cos(theta) * (mountingHoleSpacing / 2 - mountingHoleSize / 2));
      centerY = (int) (controlPoints[0].getY()
          - Math.sin(theta) * (mountingHoleSpacing / 2 - mountingHoleSize / 2));
      baseArea.subtract(new Area(new Ellipse2D.Double(centerX - mountingHoleSize / 2,
          centerY - mountingHoleSize / 2, mountingHoleSize, mountingHoleSize)));

      body[0] = baseArea;

      double angleIncrement = Math.PI / 2;
      double initialAngleOffset = angleIncrement / 2;

      Area terminalArea = new Area();

      double alpha = initialAngleOffset + getAngle().getValueRad();
      double terminalLength = JUMBO_TERMINAL_LENGTH.convertToPixels();
      int terminalWidth = getClosestOdd(JUMBO_TERMINAL_WIDTH.convertToPixels());
      int terminalHoleDiameter = getClosestOdd(JUMBO_TERMINAL_HOLE_SIZE.convertToPixels());

      Area subtractArea = new Area(baseShape);

      for (int i = 1; i < controlPoints.length; i++) {
        RoundRectangle2D rect =
            new RoundRectangle2D.Double(controlPoints[0].getX() + bodyDiameter / 2 - terminalWidth,
                controlPoints[0].getY() - terminalWidth / 2, terminalLength + terminalWidth,
                terminalWidth, terminalWidth, terminalWidth);
        Area rectArea = new Area(rect);

        AffineTransform tx = AffineTransform.getRotateInstance(alpha, controlPoints[0].getX(),
            controlPoints[0].getY());
        rectArea.transform(tx);
        
        // reduce the terminal to the visible area
        rectArea.subtract(subtractArea);

        // cut the terminal hole out
        rectArea.subtract(
            new Area(new Ellipse2D.Double(controlPoints[i].getX() - terminalHoleDiameter / 2,
                controlPoints[i].getY() - terminalHoleDiameter / 2, terminalHoleDiameter,
                terminalHoleDiameter)));
        terminalArea.add(rectArea);
        alpha += angleIncrement;
      }

      body[1] = terminalArea;
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    // Draw body
    Shape[] body = getBody();

    Shape baseShape = body[0];

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    if (componentState != ComponentState.DRAGGING) {
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getColor());
      g2d.fill(baseShape);
    }
    g2d.setComposite(oldComposite);
    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : getColor().darker();
    }
    g2d.setColor(finalBorderColor);
    g2d.draw(baseShape);    

    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : getTerminalColor().darker();
    }
    
    Shape terminalShape = body[1];
    
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getTerminalColor());
    drawingObserver.startTrackingContinuityArea(true);
    g2d.fill(terminalShape);
    drawingObserver.stopTrackingContinuityArea();
    
    drawingObserver.stopTracking();
    
    g2d.setColor(finalBorderColor);
    g2d.draw(terminalShape);

    // draw electrode labels
    if (electrodeLabels != null) {
      g2d.setColor(getLabelColor());
      g2d.setFont(project.getFont().deriveFont((float) (project.getFont().getSize2D() * 2)));

      double electrodeLabelOffset = project.getFont().getSize2D() * 5;
      for (int i = 0; i < electrodeLabels.length; i++) {
        if (i < controlPoints.length - 1) {
          String label = electrodeLabels[i];
          double theta = Math.atan2(controlPoints[i + 1].getY() - controlPoints[0].getY(),
              controlPoints[i + 1].getX() - controlPoints[0].getX());
          double x = controlPoints[i + 1].getX() - Math.cos(theta) * electrodeLabelOffset;
          double y = controlPoints[i + 1].getY() - Math.sin(theta) * electrodeLabelOffset;
          StringUtils.drawCenteredText(g2d, label, (int) x, (int) y, HorizontalAlignment.CENTER,
              VerticalAlignment.CENTER);
        }
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double factor = width / 32.0;
    
    g2d.setColor(TERMINAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke((float) (5 * factor)));
    g2d.drawLine(3, 3, width - 4, width - 4);
    g2d.drawLine(3, width - 4, width - 4, 3);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    Area area = new Area(new Ellipse2D.Double(1, 1, width - 2, width - 2));
    
    Composite composite = g2d.getComposite();
    g2d.setComposite(AlphaComposite.Clear);
    g2d.fill(area);
    
    int center = width / 2;
    double hole = 10 * factor;
    area.subtract(new Area(new Ellipse2D.Double(center - hole / 2, center - hole / 2, hole, hole)));    
    
    g2d.setComposite(composite);
    g2d.setColor(BODY_COLOR);
    g2d.fill(area);
    g2d.setColor(BODY_COLOR.darker());
    g2d.draw(area);   
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
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
  @EditableProperty(name = "Designation")
  public String getValue() {
    return type;
  }

  @Override
  public void setValue(String value) {
    this.type = value;
  }
  
  @EditableProperty(name = "Terminal")
  public Color getTerminalColor() {
    return terminalColor;
  }
  
  public void setTerminalColor(Color terminalColor) {
    this.terminalColor = terminalColor;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index > 0;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);

    body = null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return index > 0 && index <= electrodeLabels.length ? electrodeLabels[index - 1] : null;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null)
      labelColor = LABEL_COLOR;
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    Rectangle2D rect0 = getBody()[0].getBounds2D();
    Rectangle2D rect1 = getBody()[1].getBounds2D();
    int margin = 40; // to catch the pins that are outside the body, if needed
    return new Rectangle2D.Double(Math.min(rect0.getX(), rect1.getX()) - margin,
        Math.min(rect0.getY(), rect1.getY()) - margin,
        Math.max(rect0.getWidth(), rect1.getWidth()) + 2 * margin,
        Math.max(rect0.getHeight(), rect1.getHeight()) + 2 * margin);
  }
}
