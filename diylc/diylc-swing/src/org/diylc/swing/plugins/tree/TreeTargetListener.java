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
package org.diylc.swing.plugins.tree;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import org.diylc.common.IPlugInPort;


/**
 * {@link DropTargetListener} for {@link TreePanel}.
 * 
 * @author Branislav Stojkovic
 */
class TreeTargetListener implements DropTargetListener {

  private IPlugInPort presenter;

  public TreeTargetListener(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {}

  @Override
  public void dragExit(DropTargetEvent dte) {}

  @Override
  public void dragOver(DropTargetDragEvent dtde) {   
    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);   
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {
    presenter.setNewComponentTypeSlot(null, null, false);
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
  }
}
