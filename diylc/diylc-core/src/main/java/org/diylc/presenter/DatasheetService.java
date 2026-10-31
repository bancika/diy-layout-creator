/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
 */
package org.diylc.presenter;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.core.measures.Unit;


/***
 * Utility in charge of loading component datasheet from corresponding files.
 * 
 * @author bancika
 *
 */
public class DatasheetService {

  private static final Logger LOG = Logger.getLogger(DatasheetService.class);

  // relative difference below which two values count as the same
  private static final double VALUE_TOLERANCE = 0.05;

  private static DatasheetService instance;

  private static Map<String, List<String[]>> datasheetCache = new ConcurrentHashMap<>();
  private static Map<String, Map<String, List<String[]>>> lookupCache =
      new HashMap<String, Map<String, List<String[]>>>();

  public static DatasheetService getInstance() {
    if (instance == null) {
      instance = new DatasheetService();
    }
    return instance;
  }

  private DatasheetService() {}

  public List<String[]> loadDatasheet(Class<?> clazz) {
    return datasheetCache.computeIfAbsent(clazz.getCanonicalName(), key -> {
      String fileName = clazz.getSimpleName() + ".datasheet";
      InputStream inputStream = clazz.getResourceAsStream("/datasheets/"  + fileName);
      if (inputStream == null) {
        LOG.warn("Datasheet file not found: " + fileName);
        return null;
      }
      try (BufferedInputStream in = new BufferedInputStream(inputStream)) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

          List<String[]> types =
              reader.lines().map(line -> line.split("\\s*,\\s*")).collect(Collectors.toList());

          LOG.info(String.format("Loaded %d component sizes from the file %s", types.size(), fileName));
          return types;
        }
      } catch (IOException e) {
        LOG.error("Error loading component sizes", e);
        return null;
      }
    });
  }

  public String[] lookup(Class<?> clazz, double tolerance, String key,
      double... approximateValues) {
    int keySize = (int) (key.chars().filter(i -> i == '|').count() + 1);

    Map<String, List<String[]>> classData =
        lookupCache.computeIfAbsent(clazz.getCanonicalName(), k -> {
          List<String[]> datasheet = loadDatasheet(clazz);
          if (datasheet == null)
            return null;

          return datasheet.stream().collect(Collectors.groupingBy(line -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keySize; i++) {
              if (i > 0) {
                sb.append("|");
              }
              sb.append(line[i]);
            }
            return sb.toString();
          }));
        });

    List<String[]> list = classData.get(key);

    if (list == null) {
      return null;
    }

    outerloop: for (String[] line : list) {
      for (int i = 0; i < approximateValues.length; i++) {
        String valueStr = line[keySize + i];
        double actualValue = Double.parseDouble(valueStr.split(" ")[0]);
        if (Math.abs(actualValue - approximateValues[i]) / actualValue * 100 > tolerance)
          continue outerloop;
      }
      return line;
    }

    return null;
  }

  /**
   * Finds the rows of a datasheet series by its name, ignoring case. When no series has exactly
   * that name, every series whose name contains it is returned instead.
   *
   * @param clazz
   * @param series
   * @return rows of the series, empty if there is none.
   */
  public List<String[]> findSeries(Class<?> clazz, String series) {
    List<String[]> datasheet = loadDatasheet(clazz);
    if (datasheet == null || series == null || series.isBlank()) {
      return new ArrayList<String[]>();
    }
    String name = series.trim();
    List<String[]> rows = datasheet.stream()
        .filter(row -> row.length > 0 && row[0].trim().equalsIgnoreCase(name))
        .collect(Collectors.toList());
    if (rows.isEmpty()) {
      String lowerName = name.toLowerCase();
      rows = datasheet.stream()
          .filter(row -> row.length > 0 && row[0].trim().toLowerCase().contains(lowerName))
          .collect(Collectors.toList());
    }
    return rows;
  }

  /**
   * Picks the row that can be applied to the component without changing its value, since a
   * series lists one row per value and the component already has the value it needs. A series
   * usually lists the same value more than once, typically at several voltages, so among those
   * rows the one that changes the fewest other properties wins: a 250 V part stays 250 V. Each row
   * is tried on a copy of the component, so nothing has to be known about which column holds what.
   *
   * @param component
   * @param rows rows of a single series, see {@link #findSeries(Class, String)}
   * @return the best row that keeps the value, or null if every row would change it.
   */
  public String[] findModel(IDIYComponent<?> component, List<String[]> rows) {
    if (!(component instanceof IDatasheetSupport)) {
      return null;
    }
    Object value = component.getValue();
    if (value == null) {
      // nothing to preserve, but nothing to choose by either
      return rows.size() == 1 ? rows.get(0) : null;
    }
    List<PropertyWrapper> properties =
        ComponentProcessor.getInstance().extractProperties(component.getClass());
    String[] best = null;
    int bestChanges = Integer.MAX_VALUE;
    for (String[] row : rows) {
      try {
        IDIYComponent<?> copy = component.clone();
        ((IDatasheetSupport) copy).applyModel(row);
        if (!sameValue(value, copy.getValue())) {
          continue;
        }
        int changes = countChangedProperties(properties, component, copy);
        if (changes < bestChanges) {
          best = row;
          bestChanges = changes;
        }
      } catch (Exception e) {
        LOG.warn("Could not try datasheet row " + String.join(",", row) + " on "
            + component.getName(), e);
      }
    }
    return best;
  }

  private static int countChangedProperties(List<PropertyWrapper> properties, Object before,
      Object after) throws Exception {
    int changes = 0;
    for (PropertyWrapper property : properties) {
      property.readFrom(before);
      Object oldValue = property.getValue();
      property.readFrom(after);
      if (!sameValue(oldValue, property.getValue())) {
        changes++;
      }
    }
    return changes;
  }

  /**
   * Compares two measures in base units, so that 47 nF and 0.047 uF are the same value. The
   * tolerance absorbs rounding in how the datasheet writes a value while staying under the
   * roughly 10% step between neighbouring E24 values, which is what capacitor series list.
   */
  private static boolean sameValue(Object first, Object second) {
    if (first instanceof AbstractMeasure<?> a && second instanceof AbstractMeasure<?> b) {
      if (a.getValue() == null || a.getUnit() == null || b.getValue() == null
          || b.getUnit() == null || a.getUnit().getClass() != b.getUnit().getClass()) {
        return false;
      }
      double x = a.getValue() * ((Unit) a.getUnit()).getFactor();
      double y = b.getValue() * ((Unit) b.getUnit()).getFactor();
      return x == y || Math.abs(x - y) <= VALUE_TOLERANCE * Math.max(Math.abs(x), Math.abs(y));
    }
    return Objects.equals(first, second);
  }

  // Add method to clear cache for testing
  public void clearCache() {
    datasheetCache.clear();
    lookupCache.clear();
  }
}
