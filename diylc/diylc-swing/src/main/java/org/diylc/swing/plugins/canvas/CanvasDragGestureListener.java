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

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.diylc.common.IPlugInPort;

/**
 * {@link DragGestureListener} implementation for {@link CanvasPanel}.
 * 
 * @author Branislav Stojkovic
 */
class CanvasDragGestureListener implements DragGestureListener {

  private IPlugInPort presenter;
  private CanvasPanel canvasPanel;

  public CanvasDragGestureListener(IPlugInPort presenter, CanvasPanel canvasPanel) {
    super();
    this.presenter = presenter;
    this.canvasPanel = canvasPanel;
  }

  @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    boolean forceReSelection = false;
    InputEvent e = dge.getTriggerEvent();
    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      forceReSelection = me.getButton() != MouseEvent.BUTTON1;
    }
    canvasPanel.setClickInProgress(false);
    presenter.dragStarted(dge.getDragOrigin(), dge.getDragAction(), forceReSelection);
    dge.startDrag(presenter.getCursorAt(dge.getDragOrigin(), false, false, false), new EmptyTransferable(), new CanvasSourceListener(
        presenter));
  }
}
