/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
package org.diylc.swing.plugins.tree;

import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.canvas.EmptyTransferable;

/**
 * {@link DragGestureListener} for {@link TreePanel}.
 * 
 * @author Branislav Stojkovic
 */
class TreeGestureListener implements DragGestureListener {

  private IPlugInPort presenter;

  public TreeGestureListener(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    ComponentType type = null;
    MouseListener clickListener = null;
    if (presenter.getNewComponentTypeSlot() == null) {
      JTree tree = (JTree) dge.getComponent();
      Point p = dge.getDragOrigin();
      TreePath path = tree.getClosestPathForLocation(p.x, p.y);
      if (path != null && path.getLastPathComponent() instanceof TreeNode) {
        TreeNode leaf = (TreeNode) path.getLastPathComponent();
        clickListener = leaf.getClickListener();
        type = leaf.getComponentType();        
      }
    } else {
      type = presenter.getNewComponentTypeSlot();
    }

    boolean start = false;
    if (type == null)
    {
      if (clickListener != null) {
        clickListener.mouseClicked(null);
        start = true;
      }
    }
    else {
      presenter.setNewComponentTypeSlot(type, null, true);
      start = true;
    }

    if (start)
      dge.startDrag(presenter.getCursorAt(dge.getDragOrigin(), false, false, false), new EmptyTransferable(),
        new TreeSourceListener(presenter));
  }
}
