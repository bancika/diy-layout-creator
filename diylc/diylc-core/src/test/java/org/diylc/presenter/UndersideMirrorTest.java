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
package org.diylc.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;

import org.diylc.core.BoardUndersideDisplay;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.DrawingManagerTest.MockBoard;
import org.diylc.presenter.DrawingManagerTest.SidePaintingComponent;
import org.junit.Test;

public class UndersideMirrorTest {

  // the board spans x 100..300 and y 100..400, with a 20px gap to its mirror

  @Test
  public void testMirrorRegionBelow() {
    assertEquals(new Rectangle2D.Double(100, 420, 200, 300),
        UndersideMirror.getMirrorRegion(board(BoardUndersideDisplay.BELOW)));
  }

  @Test
  public void testMirrorRegionAbove() {
    assertEquals(new Rectangle2D.Double(100, -220, 200, 300),
        UndersideMirror.getMirrorRegion(board(BoardUndersideDisplay.ABOVE)));
  }

  @Test
  public void testMirrorRegionLeft() {
    assertEquals(new Rectangle2D.Double(-120, 100, 200, 300),
        UndersideMirror.getMirrorRegion(board(BoardUndersideDisplay.LEFT)));
  }

  @Test
  public void testMirrorRegionRight() {
    assertEquals(new Rectangle2D.Double(320, 100, 200, 300),
        UndersideMirror.getMirrorRegion(board(BoardUndersideDisplay.RIGHT)));
  }

  @Test
  public void testMirrorRegionFollowsOffset() {
    MockBoard board = board(BoardUndersideDisplay.BELOW);
    board.setUndersideOffset(new Size(60.0, SizeUnit.px));
    assertEquals(new Rectangle2D.Double(100, 460, 200, 300),
        UndersideMirror.getMirrorRegion(board));
  }

  @Test
  public void testNoMirrorWithoutUndersideDisplay() {
    MockBoard board = board(BoardUndersideDisplay.NONE);
    assertNull(UndersideMirror.createTransform(board));
    assertNull(UndersideMirror.getMirrorRegion(board));
  }

  @Test
  public void testReflectingTwiceReturnsPoint() {
    for (BoardUndersideDisplay display : Arrays.asList(BoardUndersideDisplay.ABOVE,
        BoardUndersideDisplay.BELOW, BoardUndersideDisplay.LEFT, BoardUndersideDisplay.RIGHT)) {
      AffineTransform mirrorTx = UndersideMirror.createTransform(board(display));
      Point2D point = new Point2D.Double(123, 456);
      assertEquals(point, mirrorTx.transform(mirrorTx.transform(point, null), null));
    }
  }

  @Test
  public void testMirroredComponentsAreOnTheBoard() {
    MockBoard board = board(BoardUndersideDisplay.BELOW);
    assertTrue(UndersideMirror.isMirrored(board, board));
    assertTrue(UndersideMirror.isMirrored(board,
        new SidePaintingComponent(new Rectangle2D.Double(150, 150, 10, 10), null)));
    assertTrue(!UndersideMirror.isMirrored(board,
        new SidePaintingComponent(new Rectangle2D.Double(450, 150, 10, 10), null)));
  }

  @Test
  public void testFindBoardsMirroredAt() {
    Project project = new Project();
    MockBoard below = board(BoardUndersideDisplay.BELOW);
    MockBoard none = board(BoardUndersideDisplay.NONE);
    project.getComponents().add(below);
    project.getComponents().add(none);

    assertEquals(Collections.singletonList(below),
        UndersideMirror.findBoardsMirroredAt(project, new Point2D.Double(150, 500)));
    assertTrue(
        UndersideMirror.findBoardsMirroredAt(project, new Point2D.Double(150, 300)).isEmpty());
  }

  private static MockBoard board(BoardUndersideDisplay display) {
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(display);
    board.setUndersideOffset(new Size(20.0, SizeUnit.px));
    return board;
  }
}
