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
package org.diylc.plugins.chatbot.service;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diylc.common.Percentage;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.DatasheetService;

/**
 * Writes the property values an edit script gives as text onto components, and applies the
 * datasheet models it names.
 *
 * @author Branislav Stojkovic
 */
class AiPropertyApplier {

  private static final Logger LOG = Logger.getLogger(AiPropertyApplier.class);

  private final ComponentProcessor processor;
  private final List<String> warnings;

  AiPropertyApplier(ComponentProcessor processor, List<String> warnings) {
    this.processor = processor;
    this.warnings = warnings;
  }

  void apply(IDIYComponent<?> comp, Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      return;
    }

    List<PropertyWrapper> compProps = processor.extractProperties(comp.getClass());
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String propName = entry.getKey();
      String propValue = entry.getValue();

      for (PropertyWrapper pw : compProps) {
        if (pw.getName().equalsIgnoreCase(propName) && !pw.isReadOnly()) {
          try {
            pw.readFrom(comp);
            Object parsedValue = parseValue(pw.getType(), propValue);
            pw.setValue(parsedValue);
            pw.writeTo(comp);
          } catch (Exception e) {
            LOG.warn("Failed to set property " + propName + " to " + propValue + " on "
                + comp.getName(), e);
            warnings.add("Could not set property '" + propName + "' on " + comp.getName() + ": "
                + e.getMessage());
          }
          break;
        }
      }
    }
  }

  /**
   * Carries a property over to a component of another type, if both have it and it holds the same
   * kind of value on both.
   */
  void copy(String name, IDIYComponent<?> from, IDIYComponent<?> to) throws Exception {
    PropertyWrapper source = find(name, from);
    PropertyWrapper target = find(name, to);
    if (source == null || target == null || target.isReadOnly()
        || source.getType() != target.getType()) {
      return;
    }
    source.readFrom(from);
    target.setValue(source.getValue());
    target.writeTo(to);
  }

  void applyDatasheetModel(IDIYComponent<?> comp, String datasheetModel) {
    if (datasheetModel == null || datasheetModel.isEmpty()) {
      return;
    }
    if (!(comp instanceof IDatasheetSupport)) {
      warnings.add("Component '" + comp.getName() + "' does not take datasheet models.");
      return;
    }
    DatasheetService datasheetService = DatasheetService.getInstance();
    List<String[]> rows = datasheetService.findSeries(comp.getClass(), datasheetModel.trim());
    if (rows.isEmpty()) {
      warnings.add("No datasheet model '" + datasheetModel + "' found for " + comp.getName() + ".");
      return;
    }
    String[] model = datasheetService.findModel(comp, rows);
    if (model == null) {
      warnings.add("Datasheet model '" + datasheetModel + "' has no part with the value "
          + comp.getValue() + " of " + comp.getName() + ".");
      return;
    }
    ((IDatasheetSupport) comp).applyModel(model);
  }

  private PropertyWrapper find(String name, IDIYComponent<?> comp) {
    for (PropertyWrapper pw : processor.extractProperties(comp.getClass())) {
      if (pw.getName().equalsIgnoreCase(name)) {
        return pw;
      }
    }
    return null;
  }

  private static Object parseValue(Class<?> type, String value) throws Exception {
    if (type == String.class) {
      return value;
    }
    if (type == Boolean.class || type == boolean.class) {
      return Boolean.parseBoolean(value);
    }
    if (type == Integer.class || type == int.class) {
      return Integer.parseInt(value);
    }
    if (type == Double.class || type == double.class) {
      return Double.parseDouble(value);
    }
    if (type == Byte.class || type == byte.class) {
      return Byte.parseByte(value);
    }
    if (type == Long.class || type == long.class) {
      return Long.parseLong(value);
    }

    if (type == Color.class) {
      // Support hex (#RRGGBB), and common color names
      if (value.startsWith("#")) {
        return Color.decode(value);
      }
      try {
        Field field = Color.class.getField(value.toLowerCase());
        return (Color) field.get(null);
      } catch (NoSuchFieldException e) {
        return Color.decode(value);
      }
    }

    if (type.isEnum()) {
      for (Object enumConstant : type.getEnumConstants()) {
        if (enumConstant.toString().equalsIgnoreCase(value)
            || ((Enum<?>) enumConstant).name().equalsIgnoreCase(value)) {
          return enumConstant;
        }
      }
      throw new IllegalArgumentException("Unknown enum value: " + value);
    }

    if (Percentage.class.isAssignableFrom(type)) {
      String cleanVal = value.replace("%", "").trim();
      return new Percentage(Integer.parseInt(cleanVal));
    }

    if (AbstractMeasure.class.isAssignableFrom(type)) {
      for (Method m : type.getMethods()) {
        if (Modifier.isStatic(m.getModifiers())
            && m.getName().startsWith("parse")
            && m.getParameterCount() == 1
            && m.getParameterTypes()[0] == String.class) {
          return m.invoke(null, value);
        }
      }
    }
    throw new IllegalArgumentException("Unsupported property type: " + type.getName());
  }
}
