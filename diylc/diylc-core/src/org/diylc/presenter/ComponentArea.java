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

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;

public class ComponentArea {

  private Area outlineArea;
  private List<Area> continuityPositiveAreas;
  private List<Area> continuityNegativeAreas;

  public ComponentArea(Area outlineArea, List<Area> continuityPositiveAreas, List<Area> continuityNegativeAreas) {
    super();
    this.outlineArea = outlineArea;
    this.continuityPositiveAreas = continuityPositiveAreas;
    this.continuityNegativeAreas = continuityNegativeAreas;
  }

  public Area getOutlineArea() {
    return outlineArea;
  }

  public List<Area> getContinuityPositiveAreas() {
    return continuityPositiveAreas;
  }

  public List<Area> getContinuityNegativeAreas() {
    return continuityNegativeAreas;
  }
  
  public void translate(double dx, double dy) {
	  AffineTransform tx = AffineTransform.getTranslateInstance(dx, dy);
	  outlineArea.transform(tx);
	  if (continuityPositiveAreas != null)
		  for(Area area : continuityPositiveAreas)
			  area.transform(tx);
	  if (continuityNegativeAreas != null)
		  for(Area area : continuityNegativeAreas)
			  area.transform(tx);
  }
}
