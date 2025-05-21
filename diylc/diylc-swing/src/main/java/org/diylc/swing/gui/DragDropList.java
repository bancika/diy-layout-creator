/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class DragDropList<T> extends JList<T> {

  private static final long serialVersionUID = 1L;

  public DragDropList(IDragDropListListener listener) {
    super();
    setDragEnabled(true);
    setDropMode(DropMode.INSERT);

    setTransferHandler(new MyListDropHandler(this, listener));

    new MyDragListener(this);
  }
}

class MyDragListener implements DragSourceListener, DragGestureListener {
  
  DragDropList<?> list;
  DragSource ds = new DragSource();

  public MyDragListener(DragDropList<?> list) {
    this.list = list;
    ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
  }

  public void dragGestureRecognized(DragGestureEvent dge) {
    StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
    ds.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
  }

  public void dragEnter(DragSourceDragEvent dsde) {}

  public void dragExit(DragSourceEvent dse) {}

  public void dragOver(DragSourceDragEvent dsde) {}

  public void dragDropEnd(DragSourceDropEvent dsde) {
//    if (dsde.getDropSuccess()) {
//      System.out.println("Succeeded");
//    } else {
//      System.out.println("Failed");
//    }
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {}
}

class MyListDropHandler extends TransferHandler {

  private static final long serialVersionUID = 1L;
  private DragDropList<?> dragDropList;
  private IDragDropListListener listener;

  public MyListDropHandler() {}

  public MyListDropHandler(DragDropList<?> dragDropList, IDragDropListListener listener) {    
    this.dragDropList = dragDropList;
    this.listener = listener;
  }

  public boolean canImport(TransferHandler.TransferSupport support) {
    if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      return false;
    }
    if (support.getComponent() != dragDropList) {
      return false;
    }
    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    if (dl.getIndex() == -1) {
      return false;
    } else {
      return true;
    }
  }

  public boolean importData(TransferHandler.TransferSupport support) {
    if (!canImport(support)) {
      return false;
    }

    Transferable transferable = support.getTransferable();
    String indexString;
    try {
      indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
    } catch (Exception e) {
      return false;
    }

    int selectedIndex = Integer.parseInt(indexString);
    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
    int dropTargetIndex = dl.getIndex();

    return listener.dropComplete(selectedIndex, dropTargetIndex);
  }
}
