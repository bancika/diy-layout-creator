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

import java.awt.Color;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Serializes {@link Color} objects as hex values instead of r/g/b. Backwards compatible when unmarshalling.
 * 
 * @author Branislav Stojkovic
 */
public class ColorConverter extends com.thoughtworks.xstream.converters.extended.ColorConverter {

  @Override
  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
    Color c = (Color) object;
    if (c.getAlpha() == 255) {
      writer.addAttribute("hex", String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
    } else {
      writer.addAttribute("hex", String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
      writer.addAttribute("alpha", String.format("%02x", c.getAlpha()));
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    if (reader.getAttribute("hex") != null) {
      String hex = reader.getAttribute("hex");
      String alpha = reader.getAttribute("alpha");
      Color color =  Color.decode("#" + hex);
      if (alpha == null)
        return color;
      else
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)Long.parseLong(alpha, 16));      
    }
    return super.unmarshal(reader, context);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    if (clazz == null)
      return false;
    return Color.class.isAssignableFrom(clazz);
  }
}
