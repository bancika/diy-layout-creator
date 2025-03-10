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

import java.awt.Font;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Serializes {@link Font} objects in a more compact fashion. Backwards compatible when unmarshalling.
 * 
 * @author Branislav Stojkovic
 */
public class FontConverter extends com.thoughtworks.xstream.converters.extended.FontConverter {
  
  public FontConverter(Mapper mapper) {
    super(mapper);
  }

  @Override
  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
    Font f = (Font) object;
    writer.addAttribute("name", f.getFontName());
    writer.addAttribute("size", Integer.toString(f.getSize()));
    writer.addAttribute("style", Integer.toString(f.getStyle()));
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    if (reader.getAttribute("name") != null) {
      return new Font(reader.getAttribute("name"), Integer.parseInt(reader.getAttribute("style")), Integer.parseInt(reader.getAttribute("size")));
    }    
    return super.unmarshal(reader, context);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    if (clazz == null)
      return false;
    return Font.class.isAssignableFrom(clazz);
  }
}
