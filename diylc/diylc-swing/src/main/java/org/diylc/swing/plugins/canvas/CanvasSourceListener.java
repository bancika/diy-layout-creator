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
package org.diylc.swing.plugins.canvas;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

import org.diylc.common.IPlugInPort;


/**
 * {@link DragSourceListener} for {@link CanvasPanel}.
 * 
 * @author Branislav Stojkovic
 */
class CanvasSourceListener implements DragSourceListener {

  private IPlugInPort presenter;

  public CanvasSourceListener(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void dragDropEnd(DragSourceDropEvent dsde) {
    presenter.dragEnded(null);
  }

  @Override
  public void dragEnter(DragSourceDragEvent dsde) {
    updateCursor(dsde);
  }

  @Override
  public void dragExit(DragSourceEvent dse) {}

  @Override
  public void dragOver(DragSourceDragEvent dsde) {
    updateCursor(dsde);
    Point p = dsde.getDragSourceContext().getComponent().getMousePosition();
    if (p != null) {
      dsde.getDragSourceContext().getComponent().firePropertyChange("dragPoint", p.x, p.y);
    }
  }

  @Override
  public void dropActionChanged(DragSourceDragEvent dsde) {
    updateCursor(dsde);
  }

  /**
   * Updates the cursor during drag operations using the presenter as the single source of truth.
   * If the presenter returns DEFAULT_CURSOR, uses MOVE_CURSOR to prevent "No" cursor issues.
   */
  private void updateCursor(DragSourceDragEvent dsde) {
    Point mousePos = dsde.getDragSourceContext().getComponent().getMousePosition();
    if (mousePos != null) {
      // Use presenter as single source of truth for cursor
      Cursor requestedCursor = presenter.getCursorAt(mousePos, false, false, false);
      Cursor dragCursor = requestedCursor;
      // If DEFAULT_CURSOR, use MOVE_CURSOR to prevent "No" cursor during drag operations
      if (requestedCursor.getType() == Cursor.DEFAULT_CURSOR) {
        dragCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
      }
      dsde.getDragSourceContext().setCursor(null);
      dsde.getDragSourceContext().setCursor(dragCursor);
    }
  }
}
