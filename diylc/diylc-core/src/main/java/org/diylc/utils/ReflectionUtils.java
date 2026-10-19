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
package org.diylc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

import org.diylc.core.annotations.IAutoCreator;
import org.diylc.core.annotations.Indexed;
import org.diylc.netlist.IGuitarDiagramAnalyzer;
import org.diylc.netlist.INetlistParser;

public class ReflectionUtils {
  
  private static final Logger LOG = Logger.getLogger(ReflectionUtils.class);

  private static final String SERVICES_DIR = "META-INF/services/";
  
  /**
   * {@link List} of {@link IAutoCreator} objects that are capable of creating more components
   * automatically when a component is created, e.g. Solder Pads.
   */ 
  private static List<IAutoCreator> autoCreators;

  /**
   * Returns the concrete implementations of an {@link Indexed} type, as listed at build time by
   * {@code diylc-processor}. Classes are loaded but not initialized.
   */
  public static <T> List<Class<? extends T>> getIndexedClasses(Class<T> type) {
    ClassLoader classLoader = ReflectionUtils.class.getClassLoader();
    // each module jar carries its own index file for the same type
    Set<String> classNames = new LinkedHashSet<String>();
    try {
      Enumeration<URL> indexFiles = classLoader.getResources(SERVICES_DIR + type.getName());
      while (indexFiles.hasMoreElements()) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(indexFiles.nextElement().openStream(), StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
              classNames.add(line);
            }
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Could not read the class index of " + type.getName(), e);
    }

    List<Class<? extends T>> result = new ArrayList<Class<? extends T>>(classNames.size());
    for (String className : classNames) {
      try {
        Class<?> clazz = Class.forName(className, false, classLoader);
        if (type.isAssignableFrom(clazz)) {
          result.add(clazz.asSubclass(type));
        } else {
          LOG.warn("Class index of " + type.getName() + " lists unrelated class " + className);
        }
      } catch (ClassNotFoundException e) {
        LOG.warn("Class index of " + type.getName() + " lists missing class " + className);
      }
    }
    return result;
  }

  /**
   * Instantiates every class returned by {@link #getIndexedClasses(Class)} through its no-arg
   * constructor.
   */
  public static <T> List<T> getIndexedInstances(Class<T> type) throws ReflectiveOperationException {
    List<T> result = new ArrayList<T>();
    for (Class<? extends T> clazz : getIndexedClasses(type)) {
      result.add(clazz.getDeclaredConstructor().newInstance());
    }
    return result;
  }

  public static List<IAutoCreator> getAutoCreators() {
    if (autoCreators == null) {
      try {
        autoCreators = getIndexedInstances(IAutoCreator.class);
      } catch (Exception e) {
        autoCreators = new ArrayList<IAutoCreator>();
        LOG.error("Error loading auto-creator types", e);
      }
    }
    return autoCreators;
  }
  
  public static List<INetlistParser> getNetlistParserDefinitions() {
    try {
      List<INetlistParser> result = getIndexedInstances(INetlistParser.class);

      Collections.sort(result, new Comparator<INetlistParser>() {

        @Override
        public int compare(INetlistParser o1, INetlistParser o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }
      });

      return result;
    } catch (Exception e) {
      LOG.error("Could not load INetlistParserDefinition implementations", e);
      return null;
    }
  }

  public static IGuitarDiagramAnalyzer getGuitarDiagramAnalyzer() throws Exception {
    List<IGuitarDiagramAnalyzer> result = getIndexedInstances(IGuitarDiagramAnalyzer.class);

    if (result.isEmpty()) {
      return null;
    }
    if (result.size() > 1) {
      throw  new RuntimeException("Found more than one IGuitarDiagramAnalyzer");
    }
    return result.get(0);
  }
}
