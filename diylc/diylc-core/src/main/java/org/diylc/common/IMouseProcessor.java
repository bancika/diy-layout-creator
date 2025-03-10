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
package org.diylc.common;

import java.awt.Point;

public interface IMouseProcessor {

  /**
   * Notifies the presenter that mouse is clicked.
   * 
   * Note: point coordinates are display based, i.e. scaled for zoom factor.
   * 
   * @param point
   * @param button
   * @param ctrlDown
   * @param shiftDown
   * @param altDown
   * @param clickCount
   */
  void mouseClicked(Point point, int button, boolean ctrlDown, boolean shiftDown, boolean altDown, int clickCount);

  /**
   * Notifies the presenter that mouse is moved.
   * 
   * Note: point coordinates are display based, i.e. scaled for zoom factor.
   * 
   * @param point
   * @param ctrlDown
   * @param shiftDown
   * @param altDown
   */
  void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown);

  /**
   * Notification that drag has been started from the specified point.
   * 
   * Note: point coordinates are scaled for zoom factor.
   * 
   * @param point
   * @param dragAction
   * @param forceSelectionRect
   */
  void dragStarted(Point point, int dragAction, boolean forceSelectionRect);

  /**
   * Checks if it's possible to drop over the specified point.
   * 
   * Note: point coordinates are scaled for zoom factor.
   * 
   * @param point
   * @return
   */
  boolean dragOver(Point point);

  /**
   * Changes the current drag action during the dragging.
   * 
   * @param dragAction
   */
  void dragActionChanged(int dragAction);

  /**
   * Notification that drag has been ended in the specified point.
   * 
   * Note: point coordinates are scaled for zoom factor.
   * 
   * @param point
   */
  void dragEnded(Point point);
}
