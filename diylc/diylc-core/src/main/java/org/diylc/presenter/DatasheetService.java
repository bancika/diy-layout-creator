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
 */
package org.diylc.presenter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;


/***
 * Utility in charge of loading component datasheet from corresponding files.
 * 
 * @author bancika
 *
 */
public class DatasheetService {

  private static final Logger LOG = Logger.getLogger(DatasheetService.class);

  private static DatasheetService instance;

  private static Map<String, List<String[]>> datasheetCache = new HashMap<String, List<String[]>>();
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
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    return datasheetCache.computeIfAbsent(clazz.getCanonicalName(), key -> {
      String fileName = clazz.getSimpleName() + ".datasheet";
      LOG.info("Loading datasheet file for " + fileName);
      URL resource = classloader.getResource(fileName);
      if (resource == null) {
        LOG.warn("Datasheet file not found: " + fileName);
        return null;
      }
      try (BufferedInputStream in = new BufferedInputStream(resource.openStream())) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

          List<String[]> types =
              reader.lines().map(line -> line.split("\\s*,\\s*")).collect(Collectors.toList());

          LOG.info(String.format("Loaded %d component sizes from the file", types.size()));
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

  // Add method to clear cache for testing
  public void clearCache() {
    datasheetCache.clear();
    lookupCache.clear();
  }
}
