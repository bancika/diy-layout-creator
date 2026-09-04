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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

import org.diylc.common.ComponentType;
import org.diylc.common.Percentage;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.*;
import org.diylc.presenter.ComponentProcessor;

/**
 * Standalone tool that generates component catalog JSON files from the current codebase.
 * <p>
 * Produces two files:
 * <ul>
 *   <li><b>catalog_full.json</b> — all component types with all editable properties and their
 *       possible values. Used by the main LLM call (filtered to selected types).</li>
 *   <li><b>catalog_index.json</b> — compact index with just component names grouped by category.
 *       Used by the preliminary LLM call to select which types are needed.</li>
 * </ul>
 * <p>
 * Usage: {@code java -cp ... org.diylc.plugins.chatbot.service.ComponentCatalogGenerator [outputDir]}
 *
 * @author Branislav Stojkovic
 */
public class ComponentCatalogGenerator {

  private static final Logger LOG = Logger.getLogger(ComponentCatalogGenerator.class);

  /** Current DIYLC version — should match pom.xml */
  private static final String VERSION = "6.2.0";

  /**
   * Property types that are purely cosmetic and not useful for LLM-driven circuit editing.
   * These are excluded from the full catalog to save tokens.
   */
  private static final Set<String> SKIP_PROPERTY_TYPES = Set.of(
      "Image", "BufferedImage"
  );

  /**
   * Property names that are purely cosmetic/visual and should be excluded.
   */
  private static final Set<String> SKIP_PROPERTY_NAMES = Set.of(

  );

  public static void main(String[] args) throws IOException {
    String outputDir = args.length > 0 ? args[0] : ".";

    LOG.info("Generating component catalog in: " + outputDir);

    ComponentProcessor processor = ComponentProcessor.getInstance();
    Map<String, List<ComponentType>> componentTypes = processor.getComponentTypes();

    List<Map<String, Object>> fullComponents = new ArrayList<>();
    Map<String, List<Map<String, String>>> indexCategories = new TreeMap<>();

    for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet()) {
      String category = entry.getKey();
      List<Map<String, String>> indexEntries = new ArrayList<>();

      for (ComponentType ct : entry.getValue()) {
        // Build full catalog entry
        Map<String, Object> comp = buildFullEntry(ct, processor);
        if (comp != null) {
          fullComponents.add(comp);
        }
        // Build index entry
        Map<String, String> idx = new LinkedHashMap<>();
        idx.put("name", (String) comp.get("name"));
        idx.put("className", (String) comp.get("className"));
        indexEntries.add(idx);
      }

      indexEntries.sort((a, b) -> a.get("name").compareTo(b.get("name")));
      indexCategories.put(category, indexEntries);
    }

    // Sort full components by category then name for consistency
    fullComponents.sort((a, b) -> {
      int catCmp = ((String) a.get("category")).compareTo((String) b.get("category"));
      return catCmp != 0 ? catCmp : ((String) a.get("name")).compareTo((String) b.get("name"));
    });

    // Write catalog_full.json
    Map<String, Object> fullCatalog = new LinkedHashMap<>();
    fullCatalog.put("version", VERSION);
    fullCatalog.put("components", fullComponents);

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    File fullFile = new File(outputDir, "catalog_full.json");
    mapper.writeValue(fullFile, fullCatalog);
    LOG.info("Wrote " + fullFile.getAbsolutePath() + " (" + fullComponents.size() + " components)");

    // Write catalog_index.json
    Map<String, Object> indexCatalog = new LinkedHashMap<>();
    indexCatalog.put("version", VERSION);
    indexCatalog.put("categories", indexCategories);

    File indexFile = new File(outputDir, "catalog_index.json");
    mapper.writeValue(indexFile, indexCatalog);
    LOG.info("Wrote " + indexFile.getAbsolutePath() + " (" + indexCategories.size() + " categories)");
  }

  /**
   * Builds a full catalog entry for a single component type.
   */
  private static Map<String, Object> buildFullEntry(ComponentType ct, ComponentProcessor processor) {
    Map<String, Object> comp = new LinkedHashMap<>();
    
    String name = ct.getName();
    if ("Schematic Symbols".equals(ct.getCategory()) && !name.contains("Symbol")) {
      name += " (Schematic)";
    }
    
    comp.put("name", name);
    comp.put("className", ct.getInstanceClass().getCanonicalName().replace("org.diylc.components.", ""));
    comp.put("category", ct.getCategory());
    comp.put("namePrefix", ct.getNamePrefix());
    comp.put("description", ct.getDescription());
    comp.put("creationMethod", ct.getCreationMethod().name());

    // Extract editable properties
    try {
      List<PropertyWrapper> properties = processor.extractProperties(ct.getInstanceClass());
      List<Map<String, Object>> propList = new ArrayList<>();

      // Also read default values from a fresh instance
      IDIYComponent<?> defaultInstance = ct.getInstanceClass().getDeclaredConstructor().newInstance();

      for (PropertyWrapper pw : properties) {
        String propName = pw.getName();
        Class<?> propType = pw.getType();

        // Skip cosmetic/visual properties
        if (SKIP_PROPERTY_TYPES.contains(propType.getSimpleName())) continue;
        if (SKIP_PROPERTY_NAMES.contains(propName)) continue;

        // Skip read-only properties (no setter)
        if (pw.isReadOnly()) continue;

        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("name", propName);
        prop.put("type", formatType(propType));

        // Format guidance
        if (Color.class.isAssignableFrom(propType)) {
          prop.put("format", "hex");
        } else if (Percentage.class.isAssignableFrom(propType)) {
          prop.put("format", "number without the % sign, e.g. 100");
        } else if (AbstractMeasure.class.isAssignableFrom(propType)) {
          prop.put("format", "(value)(unit)");
          List<String> units = extractUnits(propType);
          if (!units.isEmpty()) {
            prop.put("possibleUnits", units);
          }
        }

        // Include enum possible values (using name() instead of toString())
        if (propType.isEnum()) {
          List<String> possibleValues = Arrays.stream(propType.getEnumConstants())
              .map(e -> ((Enum<?>) e).name())
              .collect(Collectors.toList());
          prop.put("possibleValues", possibleValues);
        }

        // Include default value
        try {
          pw.readFrom(defaultInstance);
          Object defaultValue = pw.getValue();
          if (defaultValue != null) {
            String formatted = formatDefaultValue(defaultValue);
            if (formatted != null) {
              prop.put("defaultValue", formatted);
            }
          }
        } catch (Exception e) {
          // skip default value if we can't read it
        }

        propList.add(prop);
      }

      if (!propList.isEmpty()) {
        comp.put("properties", propList);
      }
    } catch (Exception e) {
      LOG.warn("Failed to extract properties for " + ct.getName(), e);
    }

    return comp;
  }

  private static List<String> extractUnits(Class<?> propType) {
    try {
      Type genericSuperclass = propType.getGenericSuperclass();
      if (genericSuperclass instanceof ParameterizedType) {
        Type[] args = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        if (args.length > 0 && args[0] instanceof Class<?>) {
          Class<?> unitEnum = (Class<?>) args[0];
          if (unitEnum.isEnum()) {
            return Arrays.stream(unitEnum.getEnumConstants())
                .map(e -> ((Enum<?>) e).name())
                .collect(Collectors.toList());
          }
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return Collections.emptyList();
  }

  /**
   * Formats a property type for the catalog. Uses friendly names for measure types.
   */
  private static String formatType(Class<?> type) {
    if (type == null) return "Unknown";

    String name = type.getSimpleName();
    
    // Capitalize primitive types to match wrapper classes for consistency
    if (type == boolean.class) return "Boolean";
    if (type == int.class) return "Integer";
    if (type == double.class) return "Double";
    if (type == byte.class) return "Byte";
    if (type == long.class) return "Long";

    return name;
  }

  /**
   * Formats a default value for display in the catalog.
   */
  private static String formatDefaultValue(Object value) {
    if (value == null) return null;

    if (value instanceof Color) {
      Color c = (Color) value;
      return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
    if (value instanceof Percentage) {
      java.math.BigDecimal percentage = ((Percentage) value).getValue();
      return percentage == null ? null : percentage.stripTrailingZeros().toPlainString();
    }
    if (value instanceof java.awt.Font) return null;
    if (value instanceof java.awt.Image) return null;

    // Measures - format with unit
    if (value instanceof AbstractMeasure) {
      return value.toString();
    }

    // Enums
    if (value.getClass().isEnum()) {
      return ((Enum<?>) value).name();
    }

    // Basic types
    if (value instanceof String) {
      String s = (String) value;
      return s.isEmpty() ? null : s;
    }
    if (value instanceof Boolean || value instanceof Number) {
      return value.toString();
    }

    return null;
  }
}
