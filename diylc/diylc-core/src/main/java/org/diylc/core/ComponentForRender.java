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
package org.diylc.core;

/**
 * Component with all extra info for it to be rendered on the screen.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentForRender {

  private IDIYComponent<?> component;
  private ComponentState state;
  private int zOrder;
  
  public ComponentForRender(IDIYComponent<?> component, ComponentState state, int zOrder) {
    super();
    this.component = component;
    this.state = state;
    this.zOrder = zOrder;
  }
  
  public IDIYComponent<?> getComponent() {
    return component;
  }
  
  public ComponentState getState() {
    return state;
  }
  
  public int getZOrder() {
    return zOrder;
  }
}
