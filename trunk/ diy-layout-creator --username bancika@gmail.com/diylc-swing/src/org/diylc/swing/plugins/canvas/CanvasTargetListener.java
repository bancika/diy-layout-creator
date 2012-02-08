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
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

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
