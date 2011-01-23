package org.diylc.plugins.canvas;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;

import org.diylc.common.IPlugInPort;

/**
 * {@link DragGestureListener} for {@link CanvasPanel}.
 * 
 * @author Branislav Stojkovic
 */
class CanvasGestureListener implements DragGestureListener {

	private IPlugInPort presenter;

	public CanvasGestureListener(IPlugInPort presenter) {
		super();
		this.presenter = presenter;
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		presenter.dragStarted(dge.getDragOrigin(), dge.getTriggerEvent().isControlDown(), dge
				.getTriggerEvent().isShiftDown(), dge.getTriggerEvent().isAltDown());
		dge.startDrag(presenter.getCursorAt(dge.getDragOrigin()), new EmptyTransferable(),
				new CanvasSourceListener(presenter));
	}
}
