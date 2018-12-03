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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.apache.log4j.Logger;

public class MemoryCleaner {

  private static final Logger LOG = Logger.getLogger(MemoryCleaner.class);

  public static void clean(Object obj) {
    Class<?> clazz = obj.getClass();
    LOG.debug("Cleaning up object of type: " + clazz.getName());
    try {
      Method disposeMethod = clazz.getMethod("dispose");
      disposeMethod.invoke(obj);
    } catch (Exception e) {
      LOG.error(e);
    }
    // If it's a collection loop over and dispose elements.
    if (Collection.class.isAssignableFrom(clazz)) {
      LOG.info("Cleaning up collection elements");
      for (Object childObj : (Collection<?>) obj) {
        clean(childObj);
      }
    }
    for (Field field : clazz.getFields()) {
      if (!Modifier.isFinal(field.getModifiers())) {
        try {
          LOG.info("Cleaning up field: " + field.getName());
          Object childObj = field.get(obj);
          field.set(obj, null);
          clean(childObj);
        } catch (Exception e) {
          LOG.error(e);
        }
      }
    }
  }
}
