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

import java.awt.geom.Area;
import java.io.Serializable;
import java.util.Collection;

public class ComponentArea implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private int zOrder;
  private Area outlineArea;
  private Collection<Area> continuityPositiveAreas;
  private Collection<Area> continuityNegativeAreas;
  
  public ComponentArea() {   
  }

  public ComponentArea(int zOrder, Area outlineArea, Collection<Area> continuityPositiveAreas, Collection<Area> continuityNegativeAreas) {
    super();
    this.zOrder = zOrder;
    this.outlineArea = outlineArea;
    this.continuityPositiveAreas = continuityPositiveAreas;
    this.continuityNegativeAreas = continuityNegativeAreas;
  }
  
  public int getZOrder() {
    return zOrder;
  }

  public Area getOutlineArea() {
    return outlineArea;
  }

  public Collection<Area> getContinuityPositiveAreas() {
    return continuityPositiveAreas;
  }

  public Collection<Area> getContinuityNegativeAreas() {
    return continuityNegativeAreas;
  }  
}
