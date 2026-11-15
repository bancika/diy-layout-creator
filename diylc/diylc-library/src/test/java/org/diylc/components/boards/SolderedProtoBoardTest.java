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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.diylc.common.Orientation;
import org.diylc.core.BoardUndersideDisplay;
import org.diylc.core.IBoard;
import org.diylc.serialization.ProjectFileManager;
import org.junit.Assert;
import org.junit.Test;

/**
 * The prototyping boards parts are soldered to show their underside like other boards, and their
 * board rectangle, which places the mirror, follows the board as it is turned.
 */
public class SolderedProtoBoardTest {

  private static final double DELTA = 1e-6;

  private static XStream createXStream() {
    XStream xStream = new XStream(new DomDriver("UTF-8"));
    ProjectFileManager.configure(xStream);
    return xStream;
  }

  @Test
  public void solderlessBreadboardHasNoUnderside() {
    Assert.assertFalse(IBoard.class.isAssignableFrom(Breadboard.class));
  }

  @Test
  public void boardRectangleIsTheBodyAtDefaultOrientation() {
    ProtoBoard board = new ProtoBoard();
    board.setControlPoint(new Point2D.Double(100, 50), 0);
    assertRect(100, 50, ProtoBoard.WIDTH_SIZE.convertToPixels(),
        ProtoBoard.LENGTH_SIZE.convertToPixels(), board.getBoardRectangle());
  }

  @Test
  public void boardRectangleTurnsWithTheBoard() {
    EurorackStripboard board = new EurorackStripboard();
    board.setControlPoint(new Point2D.Double(1000, 50), 0);
    board.setOrientation(Orientation._90);
    double width = (int) EurorackStripboard.WIDTH_SIZE.convertToPixels();
    double length = (int) EurorackStripboard.LENGTH_SIZE.convertToPixels();
    double offset = (int) EurorackStripboard.BODY_OFFSET_Y.convertToPixels();
    // a quarter turn clockwise around the control point swings the body to its left
    assertRect(1000 - length + offset, 50, length, width, board.getBoardRectangle());
  }

  @Test
  public void boardRectangleFollowsTheGridShift() {
    ProtoBoard780 board = new ProtoBoard780();
    board.setControlPoint(new Point2D.Double(100, 50), 0);
    assertRect(100 - ProtoBoard780.X_OFFSET.convertToPixels(),
        50 + ProtoBoard780.SPACING.convertToPixels() / 2,
        (int) ProtoBoard780.WIDTH_SIZE.convertToPixels(),
        (int) ProtoBoard780.LENGTH_SIZE.convertToPixels(), board.getBoardRectangle());
  }

  @Test
  public void boardSavedBeforeUndersideDisplayLoadsWithoutOne() {
    String xml = createXStream().toXML(new EurorackStripboard());
    Assert.assertTrue(xml.contains("<boardUndersideDisplay>"));

    EurorackStripboard loaded = (EurorackStripboard) createXStream().fromXML(xml
        .replaceAll("\\s*<boardUndersideDisplay>[^<]*</boardUndersideDisplay>", "")
        .replaceAll("\\s*<undersideOffset[\\s\\S]*?</undersideOffset>", "")
        .replaceAll("\\s*<undersideTransparency>[^<]*</undersideTransparency>", ""));
    Assert.assertEquals(BoardUndersideDisplay.NONE, loaded.getUndersideDisplay());
    Assert.assertEquals(IBoard.DEFAULT_UNDERSIDE_OFFSET, loaded.getUndersideOffset());
    Assert.assertTrue(loaded.getUndersideTransparency());
  }

  @Test
  public void undersideDisplayIsSaved() {
    ProtoBoard board = new ProtoBoard();
    board.setUndersideDisplay(BoardUndersideDisplay.BELOW);
    ProtoBoard loaded = (ProtoBoard) createXStream().fromXML(createXStream().toXML(board));
    Assert.assertEquals(BoardUndersideDisplay.BELOW, loaded.getUndersideDisplay());
  }

  private static void assertRect(double x, double y, double width, double height,
      Rectangle2D actual) {
    Assert.assertEquals(x, actual.getX(), DELTA);
    Assert.assertEquals(y, actual.getY(), DELTA);
    Assert.assertEquals(width, actual.getWidth(), DELTA);
    Assert.assertEquals(height, actual.getHeight(), DELTA);
  }
}
