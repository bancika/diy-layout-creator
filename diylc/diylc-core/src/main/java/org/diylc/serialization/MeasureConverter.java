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

import org.diylc.core.measures.AbstractMeasure;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Serializes objects of types derived from {@link AbstractMeasure} objects in a compact format. Backwards compatible when unmarshalling.
 * 
 * @author Branislav Stojkovic
 */
public class MeasureConverter implements Converter {

  @SuppressWarnings("rawtypes")
  @Override
  public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
    AbstractMeasure m = (AbstractMeasure) object;
    if (m.getValue() != null)
      writer.addAttribute("value", Double.toString(m.getValue()));
    if (m.getUnit() != null)
      writer.addAttribute("unit", m.getUnit().name());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Double value = null;
    String unitStr = null;
    if (reader.getAttribute("value") != null) {
      value = Double.parseDouble(reader.getAttribute("value"));
      unitStr = reader.getAttribute("unit");
    } else {
      if (reader.hasMoreChildren()) {        
        reader.moveDown();
        if (reader.getNodeName().equals("value"))
          value = Double.parseDouble(reader.getValue());
        else if (reader.getNodeName().equals("unit"))
          unitStr = reader.getValue();
        reader.moveUp();
      }
      if (reader.hasMoreChildren()) {
        reader.moveDown();
        if (reader.getNodeName().equals("value"))
          value = Double.parseDouble(reader.getValue());
        else if (reader.getNodeName().equals("unit"))
          unitStr = reader.getValue();
        reader.moveUp();
      }
    }    
    try {
      Class requiredType = context.getRequiredType();       
      java.lang.reflect.ParameterizedType generic = (java.lang.reflect.ParameterizedType)requiredType.getGenericSuperclass();
      Class unitType = (Class)generic.getActualTypeArguments()[0];
      Object unit = unitStr == null ? null : Enum.valueOf(unitType, unitStr);
      return requiredType.getConstructor(Double.class, unitType).newInstance(value, unit);      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean canConvert(Class clazz) {
    if (clazz == null)
      return false;
    return AbstractMeasure.class.isAssignableFrom(clazz);
  }
}
