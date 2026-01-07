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

import java.awt.*;
import java.awt.dnd.*;

import org.diylc.common.IPlugInPort;

import javax.swing.*;


/**
 * {@link DropTargetListener} for {@link CanvasPanel}.
 * 
 * @author Branislav Stojkovic
 */
class CanvasTargetListener implements DropTargetListener {

  private IPlugInPort presenter;

  // Cached values
  private Point currentPoint = null;
  private boolean lastAccept;

  public CanvasTargetListener(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {}

  @Override
  public void dragExit(DropTargetEvent dte) {}

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
    // If dragOver was previously called for this location use cached value.
    if (dtde.getLocation().equals(currentPoint)) {
      if (lastAccept) {
        dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      } else {
        dtde.rejectDrag();
      }
      return;
    }
    try {
      // Transferable t = dtde.getTransferable();
      // if
      // (t.isDataFlavorSupported(SelectionTransferable.selectionFlavor))
      // {
      // if (currentPoint != null) {
      // List<IDIYComponent> selection = (List<IDIYComponent>) t
      // .getTransferData(SelectionTransferable.selectionFlavor);
      // Point startPoint = (Point) t
      // .getTransferData(PointTransferable.pointFlavor);
      // presenter.selectionMoved(dtde.getLocation().x
      // - currentPoint.x, dtde.getLocation().y
      // - currentPoint.y);
      // }
      // } else if
      // (t.isDataFlavorSupported(PointTransferable.pointFlavor)) {
      // Point startPoint = (Point) t
      // .getTransferData(PointTransferable.pointFlavor);
      // Point endPoint = dtde.getLocation();
      // presenter.setSelectionRect(Utils.createRectangle(startPoint,
      // endPoint));
      // }
      currentPoint = dtde.getLocation();
      if (presenter.dragOver(currentPoint)) {
        dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        lastAccept = true;
      } else {
        dtde.rejectDrag();
        lastAccept = false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      dtde.rejectDrag();
      lastAccept = false;
    }
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {
    presenter.dragEnded(dtde.getLocation());
    updateCursor(dtde);
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
    presenter.dragActionChanged(dtde.getDropAction());
  }

  /**
   * Updates the cursor during drag operations using the presenter as the single source of truth.
   * If the presenter returns DEFAULT_CURSOR, uses MOVE_CURSOR to prevent "No" cursor issues.
   */
  private void updateCursor(DropTargetDropEvent dsde) {
    SwingUtilities.invokeLater(new Runnable() {
                                 @Override
                                 public void run() {
                                   Point mousePos = dsde.getDropTargetContext().getComponent().getMousePosition();
                                   if (mousePos != null) {
                                     // Use presenter as single source of truth for cursor
                                     Cursor requestedCursor = presenter.getCursorAt(mousePos, false, false, false);
                                     Cursor dragCursor = requestedCursor;
                                     // If DEFAULT_CURSOR, use MOVE_CURSOR to prevent "No" cursor during drag operations
                                     if (requestedCursor.getType() == Cursor.DEFAULT_CURSOR) {
                                       dragCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
                                     }
                                     dsde.getDropTargetContext().getComponent().setCursor(null);
                                     dsde.getDropTargetContext().getComponent().setCursor(dragCursor);
                                   }
                                 }
                               });
  }
}
