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
package org.diylc.swing.plugins.toolbox;

import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.canvas.CanvasPanel;


/**
 * {@link DragGestureListener} for {@link CanvasPanel}.
 * 
 * @author Branislav Stojkovic
 */
class ToolboxGestureListener implements DragGestureListener {

  private IPlugInPort presenter;
  private String className;

  public ToolboxGestureListener(IPlugInPort presenter, String className) {
    super();
    this.presenter = presenter;
    this.className = className;
  }

  @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    // presenter.dragStarted(dge.getDragOrigin());
    dge.startDrag(Cursor.getDefaultCursor(), new StringSelection(className), new ToolboxSourceListener(presenter));
  }
}
