/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.presenter;

import java.util.Collection;

import org.diylc.common.PCBLayer;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;

/**
 * Puts components placed through an Underside Display mirror on the bottom of the board. Parts and
 * copper that have a board side go to the bottom, and anything else is left as it is. Nothing is
 * mirrored.
 */
public class MirrorPlacement {

  private MirrorPlacement() {
  }

  public static void placeOnBottom(Collection<IDIYComponent<?>> components) {
    for (IDIYComponent<?> component : components) {
      if (component instanceof IBoardSideComponent sided) {
        sided.setBoardSide(PCBLayer._1);
      }
    }
  }
}
