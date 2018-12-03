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
package org.diylc.swing.plugins.edit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.diylc.core.IDIYComponent;

/**
 * Represents component selection as a {@link List} of {@link IDIYComponent} objects. Implements
 * {@link Transferable}, so it is suitable for clipboard usage.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentTransferable extends ArrayList<IDIYComponent<?>> implements Transferable {

  private static final long serialVersionUID = 1L;

  public static final DataFlavor listFlavor = new DataFlavor(ComponentTransferable.class, "application/diylc");

  public ComponentTransferable() {
    super();
  }

  public ComponentTransferable(Collection<IDIYComponent<?>> selectedComponents) {
    super(selectedComponents);
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.equals(listFlavor)) {
      return this;
    }
    throw new UnsupportedFlavorException(flavor);
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {listFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(listFlavor);
  }
}
