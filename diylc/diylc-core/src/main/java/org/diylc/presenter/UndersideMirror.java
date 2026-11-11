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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.core.BoardUndersideDisplay;
import org.diylc.core.IBoard;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Geometry of the Underside Display mirror, the reflected copy of a board and the components on it
 * drawn next to the board. The mirror is a second drawing of the same components, which keep their
 * main-view coordinates, so the same reflection draws a component into the mirror and maps a point
 * in the mirror back onto the component. A reflection is its own inverse.
 */
public class UndersideMirror {

  private UndersideMirror() {
  }

  /**
   * @return the boards of the project that show an underside mirror, in z-order
   */
  public static List<IBoard> getMirroringBoards(Project project) {
    List<IBoard> boards = new ArrayList<IBoard>();
    for (IDIYComponent<?> component : project.getComponents()) {
      if (component instanceof IBoard && createTransform((IBoard) component) != null) {
        boards.add((IBoard) component);
      }
    }
    return boards;
  }

  /**
   * @return the reflection that maps the board onto its underside mirror, or null when the board
   *         has no underside display
   */
  public static AffineTransform createTransform(IBoard board) {
    if (board.getUndersideDisplay() == null
        || board.getUndersideDisplay() == BoardUndersideDisplay.NONE) {
      return null;
    }
    Rectangle2D bounds = getBoardBounds(board);
    if (bounds == null) {
      return null;
    }
    double offset =
        board.getUndersideOffset() == null ? 0 : board.getUndersideOffset().convertToPixels();

    AffineTransform mirrorTx = new AffineTransform();
    switch (board.getUndersideDisplay()) {
      case ABOVE: {
        double pivotY = bounds.getMinY() - offset / 2;
        mirrorTx.translate(0, pivotY);
        mirrorTx.scale(1.0, -1.0);
        mirrorTx.translate(0, -pivotY);
        break;
      }
      case BELOW: {
        double pivotY = bounds.getMaxY() + offset / 2;
        mirrorTx.translate(0, pivotY);
        mirrorTx.scale(1.0, -1.0);
        mirrorTx.translate(0, -pivotY);
        break;
      }
      case LEFT: {
        double pivotX = bounds.getMinX() - offset / 2;
        mirrorTx.translate(pivotX, 0);
        mirrorTx.scale(-1.0, 1.0);
        mirrorTx.translate(-pivotX, 0);
        break;
      }
      case RIGHT: {
        double pivotX = bounds.getMaxX() + offset / 2;
        mirrorTx.translate(pivotX, 0);
        mirrorTx.scale(-1.0, 1.0);
        mirrorTx.translate(-pivotX, 0);
        break;
      }
      default:
        return null;
    }
    return mirrorTx;
  }

  /**
   * @return the axis the board's mirror flips about, which text drawn into it has to be flipped
   *         back along to stay readable
   */
  public static G2DWrapper.MirrorDirection getMirrorDirection(IBoard board) {
    if (board.getUndersideDisplay() == null) {
      return G2DWrapper.MirrorDirection.NONE;
    }
    switch (board.getUndersideDisplay()) {
      case ABOVE:
      case BELOW:
        return G2DWrapper.MirrorDirection.VERTICAL;
      case LEFT:
      case RIGHT:
        return G2DWrapper.MirrorDirection.HORIZONTAL;
      default:
        return G2DWrapper.MirrorDirection.NONE;
    }
  }

  /**
   * @return the part of the canvas the board's mirror occupies, or null when the board has no
   *         underside display
   */
  public static Rectangle2D getMirrorRegion(IBoard board) {
    AffineTransform mirrorTx = createTransform(board);
    if (mirrorTx == null) {
      return null;
    }
    return mirrorTx.createTransformedShape(getBoardBounds(board)).getBounds2D();
  }

  /**
   * @return true when the board's mirror shows the component: the board itself, or a component
   *         with a control point on the board
   */
  public static boolean isMirrored(IBoard board, IDIYComponent<?> component) {
    if (component == board) {
      return true;
    }
    Rectangle2D bounds = getBoardBounds(board);
    if (bounds == null) {
      return false;
    }
    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (bounds.contains(component.getControlPoint(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the boards whose mirror contains the point, in z-order
   */
  public static List<IBoard> findBoardsMirroredAt(Project project, Point2D point) {
    List<IBoard> boards = new ArrayList<IBoard>();
    for (IBoard board : getMirroringBoards(project)) {
      if (getMirrorRegion(board).contains(point)) {
        boards.add(board);
      }
    }
    return boards;
  }

  // the board rectangle may be stored with its corners in either order
  private static Rectangle2D getBoardBounds(IBoard board) {
    Rectangle2D rect = board.getBoardRectangle();
    if (rect == null) {
      return null;
    }
    double minX = Math.min(rect.getMinX(), rect.getMaxX());
    double minY = Math.min(rect.getMinY(), rect.getMaxY());
    return new Rectangle2D.Double(minX, minY, Math.max(rect.getMinX(), rect.getMaxX()) - minX,
        Math.max(rect.getMinY(), rect.getMaxY()) - minY);
  }
}
