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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import org.diylc.core.IDIYComponent;


public class SelectionTransferable implements Transferable {

  public static final DataFlavor selectionFlavor = new DataFlavor(List.class, "Component Selection");

  private Point startPoint;
  private List<IDIYComponent<?>> selection;

  public SelectionTransferable(List<IDIYComponent<?>> selection, Point startPoint) {
    super();
    this.selection = selection;
    this.startPoint = startPoint;
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.equals(selectionFlavor)) {
      return selection;
    }
    if (flavor.equals(PointTransferable.pointFlavor)) {
      return startPoint;
    }
    throw new UnsupportedFlavorException(flavor);
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {selectionFlavor, PointTransferable.pointFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(selectionFlavor) || flavor.equals(selectionFlavor);
  }
}
