/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.netlist;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public abstract class AbstractNetlistParser implements INetlistParser {

  private static Size X_OFFSET = new Size(1.5d, SizeUnit.in);
  private static Size Y_OFFSET = new Size(0.3d, SizeUnit.in);
  private static int ROW_COUNT = 12;

  public List<ParsedNetlistEntry> parseFile(String fileName, List<String> outputWarnings)
      throws NetlistParseException {
    String content;
    try {
      content = new String(Files.readAllBytes(Paths.get(fileName)));
    } catch (IOException e) {
      throw new NetlistParseException("Error reading file", e);
    }

    return parse(content, outputWarnings);
  }

  public List<ParsedNetlistEntry> parse(String content, List<String> outputWarnings)
      throws NetlistParseException {
    List<ParsedNetlistEntry> result = new ArrayList<ParsedNetlistEntry>();

    Pattern pattern = Pattern.compile(getItemRegex(), Pattern.MULTILINE | Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);
    while (matcher.find()) {
      String type;
      try {
        type = matcher.group("Type").trim();
      } catch (Exception e) {
        outputWarnings.add("'Type' group not found.");
        continue;
      }

      if (type == null) {
        outputWarnings.add("'Type' group not found.");
        continue;
      }

      Map<String, Object> valueMap = new HashMap<String, Object>();
      List<Class<?>> typeCandidates;
      try {
        typeCandidates = getComponentTypeCandidates(type, valueMap);
        if (typeCandidates == null) {
          outputWarnings.add("Type mappings not found for " + type + ".");
          continue;
        }
      } catch (Exception e) {
        outputWarnings.add("Error parsing type mappings for " + type + ".");
        continue;
      }

      for (String propertyName : getPropertyNames()) {
        String valueStr = matcher.group(propertyName).trim();
        try {
          Object value = parseValue(type, propertyName, valueStr);
          valueMap.put(propertyName, value);
        } catch (Exception e) {
          outputWarnings
              .add("Error while parsing value for " + type + "." + propertyName + " = " + valueStr);
        }
      }

      result.add(new ParsedNetlistEntry(typeCandidates, type, valueMap));
    }

    Collections.sort(result, new Comparator<ParsedNetlistEntry>() {

      @Override
      public int compare(ParsedNetlistEntry o1, ParsedNetlistEntry o2) {
        int comp = o1.getRawType().compareToIgnoreCase(o2.getRawType());
        if (comp == 0 && o1.getValues().containsKey("Name") && o2.getValues().containsKey("Name"))
          comp = o1.getValues().get("Name").toString()
              .compareToIgnoreCase(o2.getValues().get("Name").toString());
        return comp;
      }
    });

    return result;
  }

  public List<IDIYComponent<?>> generateComponents(List<ParsedNetlistComponent> components,
      List<String> outputWarnings) {
    double dx = 0;
    double dy = 0;
    int rowIdx = 0;
    List<IDIYComponent<?>> result = new ArrayList<IDIYComponent<?>>();
    for (int i = 0; i < components.size(); i++) {
      ParsedNetlistComponent parsedComponent = components.get(i);
      if (parsedComponent.getType() == null) {
        outputWarnings.add("Type not selected for " + parsedComponent.getValues().get("Name"));
        continue;
      }
      Class<?> clazz = parsedComponent.getType();
      IDIYComponent<?> component;
      try {
        component = (IDIYComponent<?>) clazz.newInstance();
      } catch (Exception e) {
        outputWarnings.add(e.getMessage());
        continue;
      }
      for (int j = 0; j < component.getControlPointCount(); j++) {
        Point2D p = component.getControlPoint(j);
        Point2D controlPoint = new Point2D.Double(p.getX() + dx, p.getY() + dy);
        component.setControlPoint(controlPoint, j);
      }

      if (rowIdx + 1 == ROW_COUNT) {
        dx += X_OFFSET.convertToPixels();
        dy = 0;
        rowIdx = 0;
      } else {
        dy += Y_OFFSET.convertToPixels();
        rowIdx++;
      }

      Method[] methods = clazz.getMethods();
      for (Map.Entry<String, Object> value : parsedComponent.getValues().entrySet()) {
        try {
          Optional<Method> first = Arrays.stream(methods)
              .filter(x -> x.getName().equals("set" + value.getKey())).findFirst();
          if (first.isPresent()) {
            Method setter = first.get();
            setter.invoke(component, value.getValue());
          } else {
            outputWarnings.add("No setter found for " + value.getKey());
          }
        } catch (Exception e) {
          outputWarnings.add("Error setting " + value.getKey() + ": " + e.getMessage());
        }
      }
      result.add(component);

    }
    return result;
  }
}
