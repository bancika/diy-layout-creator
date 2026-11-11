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

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.log4j.Logger;

import org.diylc.common.PCBLayer;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;

/**
 * Puts components placed through an Underside Display mirror on the bottom of the board. A part
 * that is mounted on a side goes to the bottom, copper with a {@link PCBLayer} goes to the bottom
 * layer, and anything else is left as it is. Nothing is mirrored.
 */
public class MirrorPlacement {

  private static final Logger LOG = Logger.getLogger(MirrorPlacement.class);

  private MirrorPlacement() {
  }

  public static void placeOnBottom(Collection<IDIYComponent<?>> components) {
    for (IDIYComponent<?> component : components) {
      if (component instanceof IBoardSideComponent) {
        ((IBoardSideComponent) component).setBoardSide(PCBLayer._1);
        continue;
      }
      // traces, pads and PCB text share no interface for their layer, so it is found the same way
      // PCBLayerFilter reads it
      Method setLayer;
      try {
        setLayer = component.getClass().getMethod("setLayer", PCBLayer.class);
      } catch (NoSuchMethodException e) {
        continue;
      }
      try {
        setLayer.invoke(component, PCBLayer._1);
      } catch (Exception e) {
        LOG.warn("Could not put " + component.getName() + " on the bottom layer", e);
      }
    }
  }
}
