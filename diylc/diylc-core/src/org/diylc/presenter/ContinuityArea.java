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
 * 
 */
package org.diylc.presenter;

import java.awt.geom.Area;
import java.io.Serializable;

public class ContinuityArea implements Serializable {

  private static final long serialVersionUID = 1L;

  private int zIndex;
  private int layerId;
  private Area area;

  public ContinuityArea() {}

  public ContinuityArea(int zIndex, int layerId, Area area) {
    super();
    this.zIndex = zIndex;
    this.layerId = layerId;
    this.area = area;
  }
  
  public int getZIndex() {
    return zIndex;
  }

  public Area getArea() {
    return area;
  }

  public int getLayerId() {
    return layerId;
  }
}
