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
package org.diylc.presenter;

import java.lang.reflect.Method;
import org.diylc.common.IComponentFilter;
import org.diylc.common.PCBLayer;
import org.diylc.core.IDIYComponent;

public class PCBLayerFilter implements IComponentFilter {

  private PCBLayer layer;

  public PCBLayerFilter(PCBLayer layer) {
    super();
    this.layer = layer;
  }

  @Override
  public boolean testComponent(IDIYComponent<?> component) {
    Class<?> clazz = component.getClass();
    try {
      Method m = clazz.getMethod("getLayer");
      PCBLayer l = (PCBLayer) m.invoke(component);
      return layer == l;
    } catch (Exception e) {
      return false;
    }
  }
}
