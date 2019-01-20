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
package org.diylc.components.boards;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;

import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "Blank Board", category = "Boards", author = "Branislav Stojkovic",
    zOrder = IDIYComponent.BOARD, instanceNamePrefix = "Board", description = "Blank circuit board",
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false, transformer = SimpleComponentTransformer.class)
public class BlankBoard extends AbstractBoard {

  private static final long serialVersionUID = 1L;

  private Type type = Type.SQUARE;

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(BOARD_COLOR);
    g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape clip = g2d.getClip();
    if (checkPointsClipped(clip) && !clip.contains(firstPoint.x, secondPoint.y)
        && !clip.contains(secondPoint.x, firstPoint.y)) {
      return;
    }

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(boardColor);
      if (getType() == Type.SQUARE)
        g2d.fillRect(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
      else
        g2d.fillOval(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
      g2d.setComposite(oldComposite);
    }
    // Do not track any changes that follow because the whole board has been
    // tracked so far.
    drawingObserver.stopTracking();
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : borderColor);
    if (getType() == Type.SQUARE)
      g2d.drawRect(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
    else
      g2d.drawOval(firstPoint.x, firstPoint.y, secondPoint.x - firstPoint.x, secondPoint.y - firstPoint.y);
  }
  
  @Override
  public CoordinateType getxType() {
    // Override to prevent editing.
    return super.getxType();
  }

  @Override
  public CoordinateDisplay getCoordinateDisplay() {
    // Override to prevent editing.
    return super.getCoordinateDisplay();
  }

  @Override
  public CoordinateType getyType() {
    // Override to prevent editing.
    return super.getyType();
  }

  @Override
  public Color getCoordinateColor() {
    // Override to prevent editing.
    return super.getCoordinateColor();
  }

  @EditableProperty(name = "Shape")
  public Type getType() {
    if (type == null) {
      type = Type.SQUARE;
    }
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  static enum Type {
    ROUND, SQUARE;

    @Override
    public String toString() {
      return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
  }
}
