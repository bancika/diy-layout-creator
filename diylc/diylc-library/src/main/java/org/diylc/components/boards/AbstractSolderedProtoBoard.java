/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.BoardUndersideDisplay;
import org.diylc.core.IBoard;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

/**
 * A prototyping board that parts are soldered to, which can show its underside next to it like the
 * boards that extend {@link AbstractBoard}. The underside properties are declared with the same
 * names as there, so both kinds of boards offer them in the same way.
 *
 * @author Branislav Stojkovic
 */
@AiNote("Set 'Underside Display' only if explicitly asked to. The values other than 'NONE' render the mirror image of the board in the provided direction with the offset controlled by 'Underside Offset'")
public abstract class AbstractSolderedProtoBoard extends AbstractProtoBoard implements IBoard {

  private static final long serialVersionUID = 1L;

  protected BoardUndersideDisplay boardUndersideDisplay = BoardUndersideDisplay.NONE;
  protected Size undersideOffset = IBoard.DEFAULT_UNDERSIDE_OFFSET;
  private Boolean undersideTransparency = true;

  /**
   * @return the body of the board as drawn before it is turned to its orientation
   */
  protected abstract Rectangle2D getUnrotatedBody();

  /**
   * @return the point the board is turned around to its orientation
   */
  protected abstract Point2D getRotationCenter();

  @Override
  public Rectangle2D getBoardRectangle() {
    Point2D center = getRotationCenter();
    AffineTransform rotation = AffineTransform.getRotateInstance(getOrientation().toRadians(),
        center.getX(), center.getY());
    return rotation.createTransformedShape(getUnrotatedBody()).getBounds2D();
  }

  @EditableProperty(name = "Underside Display")
  @Override
  public BoardUndersideDisplay getUndersideDisplay() {
    if (boardUndersideDisplay == null) {
      boardUndersideDisplay = BoardUndersideDisplay.NONE;
    }
    return boardUndersideDisplay;
  }

  public void setUndersideDisplay(BoardUndersideDisplay boardUndersideDisplay) {
    this.boardUndersideDisplay = boardUndersideDisplay;
  }

  @EditableProperty(name = "Underside Offset")
  @Override
  public Size getUndersideOffset() {
    if (undersideOffset == null) {
      undersideOffset = IBoard.DEFAULT_UNDERSIDE_OFFSET;
    }
    return undersideOffset;
  }

  public void setUndersideOffset(Size undersideOffset) {
    this.undersideOffset = undersideOffset;
  }

  @EditableProperty(name = "Underside Transparency")
  @Override
  public Boolean getUndersideTransparency() {
    if (undersideTransparency == null) {
      undersideTransparency = true;
    }
    return undersideTransparency;
  }

  public void setUndersideTransparency(Boolean undersideTransparency) {
    this.undersideTransparency = undersideTransparency;
  }

  @Override
  public boolean shouldExportToGerber() {
    return false;
  }
}
