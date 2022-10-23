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
package org.diylc.utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.core.annotations.IAutoCreator;
import org.diylc.netlist.INetlistParser;
import org.diylc.presenter.Presenter;

public class ReflectionUtils {
  
  private static final Logger LOG = Logger.getLogger(Presenter.class);
  
  /**
   * {@link List} of {@link IAutoCreator} objects that are capable of creating more components
   * automatically when a component is created, e.g. Solder Pads.
   */ 
  private static List<IAutoCreator> autoCreators;

  public static List<IAutoCreator> getAutoCreators() {
    if (autoCreators == null) {
      autoCreators = new ArrayList<IAutoCreator>();
      Set<Class<?>> classes = null;
      try {
        classes = Utils.getClasses("org.diylc.components.autocreate");
        for (Class<?> clazz : classes) {
          if (IAutoCreator.class.isAssignableFrom(clazz)) {
            autoCreators.add((IAutoCreator) clazz.newInstance());
            LOG.debug("Loaded auto-creator: " + clazz.getName());
          }
        }
      } catch (Exception e) {
        LOG.error("Error loading auto-creator types", e);
      }
    }
    return autoCreators;
  }
  
  public static List<INetlistParser> getNetlistParserDefinitions() {
    Set<Class<?>> classes;
    try {
      classes = Utils.getClasses("org.diylc.netlist");
      List<INetlistParser> result = new ArrayList<INetlistParser>();

      for (Class<?> clazz : classes) {
        if (!Modifier.isAbstract(clazz.getModifiers()) && INetlistParser.class.isAssignableFrom(clazz)) {
          result.add((INetlistParser) clazz.newInstance());
        }
      }

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
}
