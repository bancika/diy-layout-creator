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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import org.diylc.components.transform.AbstractBoardTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Marshall Style Perf Board", category = "Boards", author = "Branislav Stojkovic",
    zOrder = IDIYComponent.BOARD, instanceNamePrefix = "Board",
    description = "Perforated board as found on some Marshall and Trainwreck amps",
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false, keywordPolicy = KeywordPolicy.SHOW_TAG,
    keywordTag = "Perf Board", transformer = AbstractBoardTransformer.class)
public class MarshallPerfBoard extends AbstractBoard {

  private static final long serialVersionUID = 1L;

  public static Color BOARD_COLOR = PHENOLIC_DARK_COLOR;
  public static Color BORDER_COLOR = BOARD_COLOR.darker();
  public static Size SPACING = new Size(3 / 8d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(1 / 8d, SizeUnit.in);

  // private Area copperArea;
  protected Size spacing = SPACING;

  public MarshallPerfBoard() {
    super();
    this.boardColor = BOARD_COLOR;
    this.borderColor = BORDER_COLOR;
  }

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
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      double x = firstPoint.getX();
      double y = firstPoint.getY();
      int holeDiameter = getClosestOdd((int) HOLE_SIZE.convertToPixels());
      double spacing = this.spacing.convertToPixels();

      while (y < finalSecondPoint.getY() - spacing) {
        x = firstPoint.getX();
        y += spacing;
        while (x < finalSecondPoint.getX() - spacing - holeDiameter) {
          x += spacing;
          g2d.setColor(Constants.CANVAS_COLOR);
          g2d.fillOval((int)(x - holeDiameter / 2), (int)(y - holeDiameter / 2), holeDiameter, holeDiameter);
          g2d.setColor(borderColor);
          g2d.drawOval((int)(x - holeDiameter / 2), (int)(y - holeDiameter / 2), holeDiameter, holeDiameter);
        }
      }
      super.drawCoordinates(g2d, spacing, project);
    }
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSpacing() {
    return spacing;
  }

  public void setSpacing(Size spacing) {
    this.spacing = spacing;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(BOARD_COLOR);
    g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);

    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval(width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval(width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));

    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval(2 * width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval(2 * width / 3 - 2 / factor, width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));

    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));

    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
    g2d.setColor(BORDER_COLOR);
    g2d.drawOval(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, getClosestOdd(5.0 / factor),
        getClosestOdd(5.0 / factor));
  }
}
