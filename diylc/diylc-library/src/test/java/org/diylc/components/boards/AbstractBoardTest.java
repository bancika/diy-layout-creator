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

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;

import org.junit.Test;

import org.diylc.components.boards.AbstractBoard.BoardSizingMode;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public class AbstractBoardTest {

  private static final Size LENGTH = new Size(3d, SizeUnit.in);
  private static final Size WIDTH = new Size(2d, SizeUnit.in);

  @Test
  public void dimensionsSetBeforeTheModeResizeTheBoard() {
    BlankBoard board = boardAt(100, 100);
    board.setLength(LENGTH);
    board.setWidth(WIDTH);
    board.setMode(BoardSizingMode.Explicit);

    assertSecondPoint(board, 100 + LENGTH.convertToPixels(), 100 + WIDTH.convertToPixels());
  }

  @Test
  public void dimensionsSetAfterTheModeResizeTheBoard() {
    BlankBoard board = boardAt(100, 100);
    board.setMode(BoardSizingMode.Explicit);
    board.setLength(LENGTH);
    board.setWidth(WIDTH);

    assertSecondPoint(board, 100 + LENGTH.convertToPixels(), 100 + WIDTH.convertToPixels());
  }

  @Test
  public void zeroDimensionsLeaveTheBoardAlone() {
    BlankBoard board = boardAt(100, 100);
    Point2D before = new Point2D.Double(board.getControlPoint(1).getX(),
        board.getControlPoint(1).getY());
    board.setLength(new Size(0d, SizeUnit.mm));
    board.setWidth(new Size(0d, SizeUnit.mm));
    board.setMode(BoardSizingMode.Explicit);

    assertSecondPoint(board, before.getX(), before.getY());
  }

  private static BlankBoard boardAt(double x, double y) {
    BlankBoard board = new BlankBoard();
    Point2D second = board.getControlPoint(1);
    board.setControlPoint(new Point2D.Double(second.getX() + x, second.getY() + y), 1);
    board.setControlPoint(new Point2D.Double(x, y), 0);
    return board;
  }

  private static void assertSecondPoint(BlankBoard board, double x, double y) {
    assertEquals(x, board.getControlPoint(1).getX(), 0.0001);
    assertEquals(y, board.getControlPoint(1).getY(), 0.0001);
  }
}
