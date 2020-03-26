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

import java.awt.Point;
import java.util.List;

import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Size;

public class CalcUtils {

  /**
   * Rounds the number to the closest grid line.
   * 
   * @param x
   * @return
   */
  public static int roundToGrid(double x, Size gridSpacing) {
    double grid = gridSpacing.convertToPixels();
    return (int) (Math.round(1f * x / grid) * grid);
  }

  public static void snapPointToGrid(Point point, Size gridSpacing) {
    int x = roundToGrid(point.x, gridSpacing);
    int y = roundToGrid(point.y, gridSpacing);
    point.setLocation(x, y);
  }
  
  public static void snapPointToObjects(Point point, Size gridSpacing, IDIYComponent<?> component, List<IDIYComponent<?>> components) {
    for (IDIYComponent<?> c : components) {
      if (c == component)
        continue;
      for (int i = 0; i < c.getControlPointCount(); i++)
        if (c.isControlPointSticky(i) && point.distance(c.getControlPoint(i)) < gridSpacing.convertToPixels() / 2) {
          point.setLocation(c.getControlPoint(i));
          return;
        }
    }    
  }
}
