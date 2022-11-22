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
package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.common.ObjectCache;
import org.diylc.common.OrientationHV;
import org.diylc.components.AbstractComponent;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.VeroBoard;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Trace Cut", category = "Connectivity", author = "Branislav Stojkovic",
    description = "Designates the place where a trace on the vero board needs to be cut", instanceNamePrefix = "Cut",
    zOrder = IDIYComponent.BOARD + 1, bomPolicy = BomPolicy.NEVER_SHOW, autoEdit = false,
    transformer = SimpleComponentTransformer.class, enableCache = true)
public class TraceCut extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Size SIZE = new Size(0.08d, SizeUnit.in);
  public static Size CUT_WIDTH = new Size(0.5d, SizeUnit.mm);
  public static Color FILL_COLOR = Color.white;
  public static Color BORDER_COLOR = Color.red;
  public static Color SELECTION_COLOR = Color.red;
  public static Size HOLE_SIZE = new Size(0.7d, SizeUnit.mm);

  private Size size = SIZE;
  @SuppressWarnings("unused")
  @Deprecated
  private Color fillColor = FILL_COLOR;
  @Deprecated
  private Color borderColor = BORDER_COLOR;
  private Color boardColor = AbstractBoard.PHENOLIC_COLOR;
  private Boolean cutBetweenHoles = false;
  private OrientationHV orientation = OrientationHV.VERTICAL;
  private Size holeSpacing = VeroBoard.SPACING;

  protected Point2D.Double point = new Point2D.Double(0, 0);

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(1f));
    int size = getClosestOdd((int) this.size.convertToPixels());
    int cutWidth = getClosestOdd((int) CUT_WIDTH.convertToPixels());
    if (getCutBetweenHoles()) {
      int holeSpacing = getClosestOdd(getHoleSpacing().convertToPixels());
      g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
          : getBoardColor());
      drawingObserver.startTrackingContinuityArea(1, false);
      if (getOrientation() == OrientationHV.VERTICAL)
        g2d.fillRect((int)(point.getX() - holeSpacing / 2 - cutWidth / 2), (int)(point.getY() - size / 2 - 1), cutWidth, size + 2);
      else
        g2d.fillRect((int)(point.getX() - size / 2 - 1), (int)(point.getY() - holeSpacing / 2 - cutWidth / 2), size + 2, cutWidth);
      drawingObserver.stopTrackingContinuityArea();
    } else {
      g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
          : getBoardColor());
      drawingObserver.startTrackingContinuityArea(1, false);
      g2d.fillRoundRect((int)(point.getX() - size / 2), (int)(point.getY() - size / 2), size, size, size, size);
      drawingObserver.stopTrackingContinuityArea();

      g2d.setColor(Constants.CANVAS_COLOR);
      int holeSize = getClosestOdd((int) HOLE_SIZE.convertToPixels());      
      g2d.fillOval((int)(point.getX() - holeSize / 2), (int)(point.getY() - holeSize / 2), holeSize, holeSize);      
      g2d.setColor(getBoardColor().darker());
      g2d.drawOval((int)(point.getX() - holeSize / 2), (int)(point.getY() - holeSize / 2), holeSize, holeSize);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(AbstractBoard.PHENOLIC_COLOR);
    g2d.fillRect(0, 2 / factor, width - 1, height - 4 / factor);
    g2d.setColor(AbstractBoard.BORDER_COLOR);
    g2d.drawRect(0, 2 / factor, width - 1, height - 4 / factor);
    g2d.setColor(COPPER_COLOR);
    g2d.fillRect(1 / factor, width / 3, width - 2 / factor, getClosestOdd(width / 3) + 1);
    g2d.setColor(COPPER_COLOR.darker());
    g2d.drawRect(1 / factor, width / 3, width - 2 / factor, getClosestOdd(width / 3) + 1);

    g2d.setColor(AbstractBoard.PHENOLIC_COLOR);
    g2d.fillRoundRect(width / 3, width / 3, getClosestOdd(width / 3) + 2, getClosestOdd(width / 3) + 2, width / 3,
        width / 3);

    g2d.setColor(COPPER_COLOR);
    g2d.fillRect(1 / factor, 2 / factor, width - 2 / factor, 4 / factor);
    g2d.fillRect(1 / factor, height - 6 / factor, width - 2 / factor, 4 / factor);
    g2d.setColor(COPPER_COLOR.darker());
    g2d.drawRect(1 / factor, 2 / factor, width - 2 / factor, 4 / factor);
    g2d.drawRect(1 / factor, height - 6 / factor, width - 2 / factor, 4 / factor);

    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval(width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.fillOval(width / 2 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.fillOval(5 * width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.setColor(COPPER_COLOR.darker());
    g2d.drawOval(width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.drawOval(width / 2 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
    g2d.drawOval(5 * width / 6 - 1, width / 2 - 1, getClosestOdd(3.0 / factor), getClosestOdd(3.0 / factor));
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point.setLocation(point);
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @EditableProperty
  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @Deprecated
  public Color getBorderColor() {
    return borderColor;
  }

  @Deprecated
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Cut Between Holes")
  public boolean getCutBetweenHoles() {
    if (cutBetweenHoles == null) {
      cutBetweenHoles = false;
    }
    return cutBetweenHoles;
  }

  public void setCutBetweenHoles(boolean cutBetweenHoles) {
    this.cutBetweenHoles = cutBetweenHoles;
  }

  @EditableProperty(name = "Board")
  public Color getBoardColor() {
    if (boardColor == null) {
      boardColor = AbstractBoard.PHENOLIC_COLOR;
    }
    return boardColor;
  }

  public void setBoardColor(Color boardColor) {
    this.boardColor = boardColor;
  }

  @EditableProperty(name = "Hole Spacing")
  public Size getHoleSpacing() {
    if (holeSpacing == null) {
      holeSpacing = VeroBoard.SPACING;
    }
    return holeSpacing;
  }

  public void setHoleSpacing(Size holeSpacing) {
    this.holeSpacing = holeSpacing;
  }
  
  @EditableProperty(name = "Cut Orientation")
  public OrientationHV getOrientation() {
    if (orientation == null)
      orientation = OrientationHV.VERTICAL;
    return orientation;
  }
  
  public void setOrientation(OrientationHV orientation) {
    this.orientation = orientation;
  }

  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }
  
  @Override
  public String getControlPointNodeName(int index) {   
    return null;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    double size = getSize().convertToPixels();
    return new Rectangle2D.Double(point.getX() - size, point.getY() - size, size * 2, size * 2);
  }
}
