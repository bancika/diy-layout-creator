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

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AreaUtils {  

  /**
   * Merges all areas that either overlap or are joined by connections.
   * 
   * @param areas
   * @param connections
   * @return
   */
  public static boolean crunchAreas(List<Area> areas, Set<Connection> connections) {
    boolean isChanged = false;

    List<Area> newAreas = new ArrayList<Area>();
    List<Boolean> consumed = new ArrayList<Boolean>();
    for (int i = 0; i < areas.size(); i++) {
      consumed.add(false);
    }
    for (int i = 0; i < areas.size(); i++) {
      for (int j = i + 1; j < areas.size(); j++) {
        if (consumed.get(j))
          continue;
        Area a1 = areas.get(i);
        Area a2 = areas.get(j);
        Area intersection = null;
        if (a1.getBounds2D().intersects(a2.getBounds())) {
          intersection = new Area(a1);
          intersection.intersect(a2);
        }
        // if the two areas intersect, make a union and consume the second area
        if (intersection != null && !intersection.isEmpty()) {
          a1.add(a2);
          consumed.set(j, true);
        } else if (connections != null) { // maybe there's a connection between them
          for (Connection p : connections) { // use getBounds to optimize the computation, don't get into complex math if not needed
            if ((a1.getBounds().contains(p.getP1()) && a2.getBounds().contains(p.getP2()) && a1.contains(p.getP1()) && a2.contains(p.getP2())) || 
                (a1.getBounds().contains(p.getP2()) && a2.getBounds().contains(p.getP1())) && a1.contains(p.getP2()) && a2.contains(p.getP1())) {
              a1.add(a2);
              consumed.set(j, true);
              break;
            }
          }
        }
      }
    }
    for (int i = 0; i < areas.size(); i++)
      if (!consumed.get(i))
        newAreas.add(areas.get(i));
      else
        isChanged = true;

    if (isChanged) {
      areas.clear();
      areas.addAll(newAreas);
      crunchAreas(areas, connections);
    }

    return isChanged;
  }

  public static List<Area> tryAreaBreakout(Area a) {
    List<Area> toReturn = new ArrayList<Area>();
    Path2D p = null;
    PathIterator pathIterator = a.getPathIterator(null);
    while (!pathIterator.isDone()) {
      double[] coord = new double[6];
      int type = pathIterator.currentSegment(coord);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          if (p != null) {
            Area partArea = new Area(p);
            toReturn.add(partArea);
          }
          p = new Path2D.Double();
          p.moveTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_LINETO:
          p.lineTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_CUBICTO:
          p.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
          break;
        case PathIterator.SEG_QUADTO:
          p.quadTo(coord[0], coord[1], coord[2], coord[3]);
          break;
      }
      pathIterator.next();
    }
    if (p != null) {
      Area partArea = new Area(p);
      toReturn.add(partArea);
    }

    return toReturn;
  }
}
