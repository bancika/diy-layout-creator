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
package org.diylc.swing.plugins.canvas;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import org.diylc.common.IPlugInPort;


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
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
    presenter.dragActionChanged(dtde.getDropAction());
  }
}
