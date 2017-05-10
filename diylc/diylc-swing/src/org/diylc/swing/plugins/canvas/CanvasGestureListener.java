package org.diylc.swing.plugins.canvas;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

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
    boolean forceReSelection = false;
    InputEvent e = dge.getTriggerEvent();
    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      forceReSelection = me.getButton() != MouseEvent.BUTTON1;
    }
    presenter.dragStarted(dge.getDragOrigin(), dge.getDragAction(), forceReSelection);
    dge.startDrag(presenter.getCursorAt(dge.getDragOrigin()), new EmptyTransferable(), new CanvasSourceListener(
        presenter));
  }
}
