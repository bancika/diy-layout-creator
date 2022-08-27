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
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import org.diylc.common.OrientationHV;
import org.diylc.components.transform.VeroBoardTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "TriPad Board", category = "Boards", author = "Hauke Juhls", zOrder = IDIYComponent.BOARD,
    instanceNamePrefix = "Board",
    description = "Perforated FR4 board with copper strips connecting 3 holes in a row (aka TriPad Board)",
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME, transformer = VeroBoardTransformer.class, enableCache = true)
public class TriPadBoard extends AbstractVeroBoard {

  private static final long serialVersionUID = 1L;

  protected int stripSpan = 3; // determines how many holes are covered by a

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Point2D finalSecondPoint = getFinalSecondPoint();
    
    Shape clip = g2d.getClip();
    if (checkPointsClipped(clip) && !clip.contains(firstPoint.getX(), finalSecondPoint.getY())
        && !clip.contains(finalSecondPoint.getX(), firstPoint.getY())) {
      return;
    }
    super.draw(g2d, componentState, outlineMode, project, drawingObserver);
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      double x = firstPoint.getX();
      double y = firstPoint.getY();
      int stripSize = getClosestOdd((int) STRIP_SIZE.convertToPixels());
      int holeSize = getClosestOdd((int) HOLE_SIZE.convertToPixels());
      double spacing = this.spacing.convertToPixels();

      if (orientation == OrientationHV.HORIZONTAL) {
        while (y < finalSecondPoint.getY() - spacing - stripSize / 2) {
          x = firstPoint.getX();
          y += spacing;

          while (x + spacing < finalSecondPoint.getX()) {

            double remainingSpace = finalSecondPoint.getX() - x;
            double spacesToDraw = stripSpan;

            if (remainingSpace < (stripSize + (stripSpan * spacing))) {
              spacesToDraw = (remainingSpace - stripSize) / spacing;
            }

            g2d.setColor(stripColor);
            drawingObserver.startTrackingContinuityArea(true);  
            g2d.fillRect((int)(x + spacing - stripSize / 2), (int)(y - stripSize / 2), (int)(spacing * (spacesToDraw - 1) + stripSize),
                stripSize);
            drawingObserver.stopTrackingContinuityArea();
            g2d.setColor(stripColor.darker());

            g2d.drawRect((int)(x + spacing - stripSize / 2), (int)(y - stripSize / 2), (int)(spacing * (spacesToDraw - 1) + stripSize),
                stripSize);

            x += spacing * spacesToDraw;
          }

          // draw holes
          x = firstPoint.getX();

          while (x < finalSecondPoint.getX() - spacing - holeSize) {
            x += spacing;
            g2d.setColor(Constants.CANVAS_COLOR);
            g2d.fillOval((int)(x - holeSize / 2), (int)(y - holeSize / 2), holeSize, holeSize);
            g2d.setColor(stripColor.darker());
            g2d.drawOval((int)(x - holeSize / 2), (int)(y - holeSize / 2), holeSize, holeSize);
          }
        }
      } else {
        while (x < finalSecondPoint.getX() - spacing) {
          x += spacing;
          y = firstPoint.getY();

          while (y + spacing < finalSecondPoint.getY()) {

            double remainingSpace = finalSecondPoint.getY() - y;
            double spacesToDraw = stripSpan;

            if (remainingSpace < (stripSize + (stripSpan * spacing))) {
              spacesToDraw = (remainingSpace - stripSize) / spacing;
            }

            g2d.setColor(stripColor);
            drawingObserver.startTrackingContinuityArea(true);  
            g2d.fillRect((int)(x - stripSize / 2), (int)(y + spacing - stripSize / 2), stripSize, (int)(spacing * (spacesToDraw - 1)
                + stripSize));
            drawingObserver.stopTrackingContinuityArea();
            g2d.setColor(stripColor.darker());
            g2d.drawRect((int)(x - stripSize / 2), (int)(y + spacing - stripSize / 2), stripSize, (int)(spacing * (spacesToDraw - 1)
                + stripSize));

            y += spacing * spacesToDraw;
          }

          // draw holes
          y = firstPoint.getY();

          while (y < finalSecondPoint.getY() - spacing - holeSize) {
            y += spacing;
            g2d.setColor(Constants.CANVAS_COLOR);
            g2d.fillOval((int)(x - holeSize / 2), (int)(y - holeSize / 2), holeSize, holeSize);
            g2d.setColor(stripColor.darker());
            g2d.drawOval((int)(x - holeSize / 2), (int)(y - holeSize / 2), holeSize, holeSize);
          }
        }
      }
      g2d.setComposite(oldComposite);

      super.drawCoordinates(g2d, spacing, project);
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(BOARD_COLOR);
    g2d.fillRect(0, 0, width, height);

    final int horizontalSpacing = width / 5;
    final int horizontalIndent = horizontalSpacing / 2;

    final int verticalSpacing = height / 5;
    final int verticalIndent = verticalSpacing / 2;

    for (int row = 0; row < 5; row++) {
      g2d.setColor(COPPER_COLOR);
      g2d.fillRect(0, row * verticalSpacing + 2, horizontalIndent / 2 + horizontalSpacing, verticalSpacing - 1);

      g2d.setColor(COPPER_COLOR);
      g2d.fillRect(horizontalSpacing + 2, row * verticalSpacing + 2, horizontalSpacing * 3 - 1, verticalSpacing - 1);

      g2d.fillRect(horizontalSpacing * 4 + 2, row * verticalSpacing + 2, horizontalIndent / 2 + horizontalSpacing,
          verticalSpacing - 1);
    }

    // draw dots
    for (int row = 0; row < 5; row++) {
      int y = (verticalSpacing * row) + verticalIndent;
      for (int col = 0; col < 5; col++) {
        int x = (horizontalSpacing * col) + horizontalIndent;
        g2d.setColor(Constants.CANVAS_COLOR);
        g2d.fillOval(x, y, 2, 2);
        g2d.setColor(COPPER_COLOR.darker());
        g2d.drawOval(x, y, 2, 2);
      }
    }
  }

  @EditableProperty(name = "Holes Per Strip")
  public int getStripSpan() {
    return stripSpan;
  }

  public void setStripSpan(int stripSpan) {
    if (stripSpan < 1) {
      this.stripSpan = 1;
    } else {
      this.stripSpan = stripSpan;
    }
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }
}
