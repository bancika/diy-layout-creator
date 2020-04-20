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

import java.awt.Point;

import org.diylc.utils.Constants;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Serializes {@link Point} objects by converting coordinates from pixels to inches, thus avoiding
 * the resolution to affect point placement.
 * 
 * @author Branislav Stojkovic
 */
public class PointConverter implements Converter {

  @Override
  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
    Point point = (Point) object;
    writer.addAttribute("x", Double.toString(1d * point.x / Constants.PIXELS_PER_INCH));
    writer.addAttribute("y", Double.toString(1d * point.y / Constants.PIXELS_PER_INCH));
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    double x = Double.parseDouble(reader.getAttribute("x"));
    double y = Double.parseDouble(reader.getAttribute("y"));
    return new Point((int) Math.round(x * Constants.PIXELS_PER_INCH), (int) Math.round(y * Constants.PIXELS_PER_INCH));
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    return Point.class.isAssignableFrom(clazz);
  }
}
