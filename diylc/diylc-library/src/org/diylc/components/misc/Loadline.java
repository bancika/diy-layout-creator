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
package org.diylc.components.misc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

import org.diylc.common.ObjectCache;
import org.diylc.common.SimpleComponentTransformer;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

//@ComponentDescriptor(name = "Loadline", category = "Misc", author = "Branislav Stojkovic",
//    zOrder = IDIYComponent.BOARD, instanceNamePrefix = "LL",
//    description = "Loadline of a tube or transistor", bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME,
//    autoEdit = false, transformer = SimpleComponentTransformer.class)
public class Loadline extends AbstractBoard {

  private static final long serialVersionUID = 1L;
  
  private static final Color COLOR = Color.white;
  private static final Color BORDER = Color.lightGray;

  private LoadlineEntity data;
  
  public Loadline() {
    boardColor = COLOR;
    borderColor = BORDER;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(BOARD_COLOR);
    g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    Point finalSecondPoint = getFinalSecondPoint();

    Shape clip = g2d.getClip();
    if (checkPointsClipped(clip) && !clip.contains(firstPoint.x, finalSecondPoint.y)
        && !clip.contains(finalSecondPoint.x, firstPoint.y)) {
      return;
    }

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(boardColor);

      g2d.fillRect(firstPoint.x, firstPoint.y, finalSecondPoint.x - firstPoint.x,
          finalSecondPoint.y - firstPoint.y);

      g2d.setComposite(oldComposite);
    }
    // Do not track any changes that follow because the whole board has been
    // tracked so far.
    drawingObserver.stopTracking();
    g2d.setColor(
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : borderColor);

    g2d.drawRect(firstPoint.x, firstPoint.y, finalSecondPoint.x - firstPoint.x,
        finalSecondPoint.y - firstPoint.y);

  }

  @Override
  public CoordinateType getxType() {
    // Override to prevent editing.
    return super.getxType();
  }

  @Override
  public CoordinateDisplay getCoordinateDisplay() {   
    return CoordinateDisplay.None;
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

  @EditableProperty(name = "Model")
  public LoadlineEntity getData() {
    return data;
  }
  
  public void setData(LoadlineEntity data) {
    this.data = data;
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
  
  @Override
  public CoordinateOrigin getCoordinateOrigin() {
    // Override to prevent editing.
    return super.getCoordinateOrigin();
  }
  
  @Override
  public Color getBoardColor() {
    // Override to prevent editing.
    return super.getBoardColor();
  }
  
  @Override
  public Color getBorderColor() {
    // Override to prevent editing.
    return super.getBorderColor();
  }
  
  @Override
  public String getValue() {
    // Override to prevent editing.
    return super.getValue();
  }
}
