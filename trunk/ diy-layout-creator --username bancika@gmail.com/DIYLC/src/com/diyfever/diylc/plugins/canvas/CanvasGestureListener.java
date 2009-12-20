package com.diyfever.diylc.plugins.canvas;

import java.awt.Cursor;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;

import com.diyfever.diylc.common.IPlugInPort;

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
		presenter.dragStarted(dge.getDragOrigin());
		dge.startDrag(Cursor.getDefaultCursor(), new EmptyTransferable(),
				new CanvasSourceListener(presenter));
	}
}
