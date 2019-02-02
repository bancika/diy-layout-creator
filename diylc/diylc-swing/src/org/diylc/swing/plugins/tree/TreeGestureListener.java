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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.canvas.EmptyTransferable;
import org.diylc.swing.plugins.tree.TreePanel.Payload;

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
    JTree tree = (JTree) dge.getComponent();
    Point p = dge.getDragOrigin();
    TreePath path = tree.getClosestPathForLocation(p.x, p.y);
    if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode leaf = (DefaultMutableTreeNode)path.getLastPathComponent();
      Payload payload = (Payload) leaf.getUserObject();
      if (payload != null) {
        if (payload.getComponentType() != null)
          presenter.setNewComponentTypeSlot(payload.getComponentType(), null, true);
        else
          payload.getClickListener().mouseClicked(null);
        dge.startDrag(presenter.getCursorAt(dge.getDragOrigin()), new EmptyTransferable(), new TreeSourceListener(
            presenter));
      }
    }
  }
}
