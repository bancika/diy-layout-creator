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
package org.diylc.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.diylc.core.IDIYComponent;

/**
 * Represents component selection as a {@link List} of {@link IDIYComponent} objects. Implements
 * {@link Transferable}, so it is suitable for clipboard usage.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentTransferable implements Transferable, Serializable {

  private static final long serialVersionUID = 1L;

  public static final DataFlavor listFlavor = new DataFlavor(ComponentTransferable.class, "application/diylc");
  
  private List<IDIYComponent<?>> components;
  private Set<Set<IDIYComponent<?>>> groups;

  public ComponentTransferable() {
    super();
  }
  
  public ComponentTransferable(List<IDIYComponent<?>> components) {
    super();
    this.components = components;
    this.groups = null;
  }

  public ComponentTransferable(List<IDIYComponent<?>> components, Set<Set<IDIYComponent<?>>> groups) {
    super();
    this.components = components;
    this.groups = groups;
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
  
  public List<IDIYComponent<?>> getComponents() {
    return components;
  }
  
  public Set<Set<IDIYComponent<?>>> getGroups() {
    return groups;
  }
}
