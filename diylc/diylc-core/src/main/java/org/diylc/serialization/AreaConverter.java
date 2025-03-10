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
package org.diylc.serialization;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Serializes {@link Area} objects by converting coordinates from pixels to inches, thus avoiding
 * the resolution to affect point placement.
 * 
 * @author Branislav Stojkovic
 */
public class AreaConverter implements Converter {

  @Override
  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
    Area area = (Area) object;
    PathIterator iterator = area.getPathIterator(null);
//    writer.startNode("area");
    writer.addAttribute("wind", String.valueOf(iterator.getWindingRule()));    
    while (!iterator.isDone()) {
      writer.startNode("segment");
      double[] coord = new double[6];
      int type = iterator.currentSegment(coord);
      writer.addAttribute("type", String.valueOf(type));
      int n = 0;
      switch (type) {
        case PathIterator.SEG_MOVETO:
        case PathIterator.SEG_LINETO:
          n = 2;
          break;
        case PathIterator.SEG_CUBICTO:
          n = 6;          
          break;
        case PathIterator.SEG_QUADTO:
          n = 4;
          break;
      }
      for (int i = 0; i < n; i++) {
        writer.startNode("coordinate");
        writer.setValue(String.valueOf(coord[i]));
        writer.endNode();
      }
      writer.endNode();
      iterator.next();
    }
//    writer.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {    
    Area area = new Area();
//    reader.moveDown();
    int wind = Integer.parseInt(reader.getAttribute("wind"));
    Path2D path = null;
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      int type = Integer.parseInt(reader.getAttribute("type"));
      List<java.lang.Double> values = new ArrayList<java.lang.Double>();
      while (reader.hasMoreChildren()) {
        reader.moveDown();
        values.add(java.lang.Double.parseDouble(reader.getValue()));
        reader.moveUp();
      }
      java.lang.Double[] coord = values.toArray(new java.lang.Double[0]);      
      switch (type) {
        case PathIterator.SEG_MOVETO:
          if (path != null) {
            Area partArea = new Area(path);
            area.add(partArea);
          }
          path = new Path2D.Double(wind);
          path.moveTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(coord[0], coord[1], coord[2], coord[3]);
          break;
      }
      reader.moveUp();
    }
//    reader.moveUp();
    
    // add residual area
    if (path != null) {
      Area partArea = new Area(path);
      area.add(partArea);
    }
    return area;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    if (clazz == null)
      return false;
    return Area.class.isAssignableFrom(clazz);
  }
}
