/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
package org.diylc.components.connectivity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.connectivity.PCBTerminalBlock.PCBTerminalBlockCount;
import org.diylc.components.transform.PCBTerminalBlockTransformer;
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

@ComponentDescriptor(name = "PCB Terminal Block", category = "Connectivity",
    author = "Branislav Stojkovic", description = "Horizontal PCB terminal block with 5mm pitch",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "TB", autoEdit = false,
    enableCache = true, transformer = PCBTerminalBlockTransformer.class)
public class PCBTerminalBlock extends AbstractTransparentComponent<PCBTerminalBlockCount> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#90AB66");
  private static Color BORDER_COLOR = BODY_COLOR.darker();
  private static Color CIRCLE_COLOR = LIGHT_METAL_COLOR;

  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  transient protected Shape[] body;
  protected PCBTerminalBlockCount count = PCBTerminalBlockCount._3;
  private Orientation orientation = Orientation.DEFAULT;

  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  
  private Size pitch = new Size(0.2d, SizeUnit.in);
  private Size width = new Size(0.3d, SizeUnit.in);
  private ScrewPosition screwPosition = ScrewPosition.Offset;

  public PCBTerminalBlock() {
    super();
    updateControlPoints();
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int spacing = (int) getPitch().convertToPixels();
    controlPoints = new Point2D[count.getCount()];
    controlPoints[0] = firstPoint;

    for (int i = 1; i < controlPoints.length; i++) {
      controlPoints[i] = new Point2D.Double(firstPoint.getX(), firstPoint.getY() + spacing * i);
    }

    if (orientation != Orientation.DEFAULT) {
      AffineTransform xform = AffineTransform.getRotateInstance(orientation.toRadians(),
          firstPoint.getX(), firstPoint.getY());
      for (int i = 1; i < controlPoints.length; i++) {
        xform.transform(controlPoints[i], controlPoints[i]);
      }
    }
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
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Pins")
  @Override
  public PCBTerminalBlockCount getValue() {
    return count;
  }

  @Override
  public void setValue(PCBTerminalBlockCount value) {
    this.count = value;
    updateControlPoints();
    // Reset body shape.
    body = null;
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
  
  @EditableProperty
  public Size getPitch() {
    if (pitch == null) {
      pitch = new Size(0.2d, SizeUnit.in);
    }
    return pitch;
  }
  
  public void setPitch(Size pitch) {
    this.pitch = pitch;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }
  
  @EditableProperty
  public Size getWidth() {
    if (width == null) {
      width = new Size(0.3, SizeUnit.mm);
    }
    return width;
  }
  
  public void setWidth(Size width) {
    this.width = width;
    // Reset body shape.
    body = null;
  }
  
  @EditableProperty(name = "Screw Position")
  public ScrewPosition getScrewPosition() {
    if (screwPosition == null) {
      screwPosition = ScrewPosition.Offset;
    }
    return screwPosition;
  }
  
  public void setScrewPosition(ScrewPosition screwPosition) {
    this.screwPosition = screwPosition;
    // Reset body shape.
    body = null;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Shape[] body = getBody();
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
        Constants.DEFAULT_THEME);
    // Draw body if available.
    if (body != null) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
      g2d.fill(body[0]);
      g2d.setComposite(oldComposite);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      Color finalBorderColor;
      if (outlineMode) {
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
                ? SELECTION_COLOR
                : theme.getOutlineColor();
      } else {
        g2d.setColor(getBorderColor());
        g2d.draw(body[1]);
        finalBorderColor =
            componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
                ? SELECTION_COLOR
                : getBorderColor();
      }
      g2d.setColor(finalBorderColor);
      g2d.draw(body[0]);      
    }
    // Do not track these changes because the whole switch has been tracked
    // so far.
    drawingObserver.stopTracking();
    // Draw lugs.
    int circleDiameter = getClosestOdd((int) (getPitch().convertToPixels() * 3d / 5));

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    for (Point2D p : controlPoints) {
      if (outlineMode) {
        // g2d.setColor(Constants.TRANSPARENT_COLOR);
        // drawingObserver.startTrackingContinuityArea(true);
        // g2d.fillRect((int)(p.getX() - lugWidth / 2), (int)(p.getY() - lugHeight / 2), lugWidth,
        // lugHeight);
        // drawingObserver.stopTrackingContinuityArea();

        g2d.setColor(theme.getOutlineColor());
      } else {
        g2d.setColor(CIRCLE_COLOR);
        g2d.fillOval((int) (p.getX() - circleDiameter / 2), (int) (p.getY() - circleDiameter / 2),
            circleDiameter, circleDiameter);

        g2d.setColor(CIRCLE_COLOR.darker());
        g2d.drawLine((int) (p.getX() + Math.cos(Math.PI / 4) * circleDiameter / 2),
            (int) (p.getY() + Math.sin(Math.PI / 4) * circleDiameter / 2),
            (int) (p.getX() + Math.cos(5 * Math.PI / 4) * circleDiameter / 2),
            (int) (p.getY() + Math.sin(5 * Math.PI / 4) * circleDiameter / 2));
      }
    }
  }

  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[2];
      Point2D firstPoint = controlPoints[0];
      int spacing = (int) getPitch().convertToPixels();
      int pointCount = count.getCount();
      int width = (int) getWidth().convertToPixels();
      int offset;
      if (getScrewPosition() == ScrewPosition.Center) {
        offset = (int) (getWidth().convertToPixels() / 2);
      } else {
        offset = (int) ((getWidth().convertToPixels() - getPitch().convertToPixels()) + getPitch().convertToPixels() / 2);
      }

      body[0] = new Rectangle2D.Double(firstPoint.getX() - offset, firstPoint.getY() - spacing / 2,
          width, pointCount * spacing);
      body[1] = new Rectangle2D.Double(firstPoint.getX() - spacing / 2, firstPoint.getY() - spacing / 2,
          spacing, pointCount * spacing);

      if (orientation != Orientation.DEFAULT) {
        AffineTransform xform = AffineTransform.getRotateInstance(orientation.toRadians(),
            firstPoint.getX(), firstPoint.getY());
        Area b = new Area(body[0]);
        b.transform(xform);
        body[0] = b;
        
        b = new Area(body[1]);
        b.transform(xform);
        body[1] = b;
      }
    }
    return body;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int circleSize = 6 * width / 32;
    g2d.setColor(BODY_COLOR);
    g2d.fillRect((int) (width / 4d - 2), 1, width / 2, height - 2);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect((int) (width / 4d - 2), 1, width / 2, height - 2);
    g2d.drawRect((int) (width / 3d), 1, (int) (width / 3d), height - 2);
    for (int i = 1; i <= 3; i++) {
      g2d.setColor(CIRCLE_COLOR);
      g2d.fillOval(width / 2 - circleSize / 2, (int) (i * height / 4d - circleSize / 2), circleSize, circleSize);
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
  public String getControlPointNodeName(int index) {
    // we don't want the block to produce any nodes, it just makes connections
    return null;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    int margin = 20;
    Rectangle2D bounds = getBody()[0].getBounds2D();
    return new Rectangle2D.Double(bounds.getX() - margin, bounds.getY() - margin,
        bounds.getWidth() + 2 * margin, bounds.getHeight() + 2 * margin);
  }

  static enum PCBTerminalBlockCount {
    _2("Two"), _3("Three"), _4("Four"), _5("Five"), _6("Six"), _7("Seven"), _8("Eight"), _9(
        "Nine"), _10("Ten");

    private String label;

    PCBTerminalBlockCount(String label) {
      this.label = label;
    }

    public int getCount() {
      return Integer.parseInt(name().substring(1));
    }

    @Override
    public String toString() {
      return label;
    }
  }
  
  static enum ScrewPosition {
    Center, Offset;
  }
}
