package com.diyfever.diylc.plugins.canvas;

import java.awt.Point;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import com.diyfever.diylc.common.IPlugInPort;

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
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
		Point p = dsde.getDragSourceContext().getComponent().getMousePosition();
		if (p != null) {
			dsde.getDragSourceContext().getComponent().firePropertyChange("dragPoint", p.x, p.y);
		}
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}
}
