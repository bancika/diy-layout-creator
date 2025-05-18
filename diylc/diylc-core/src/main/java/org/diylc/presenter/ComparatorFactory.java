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
package org.diylc.presenter;

import java.util.Comparator;

import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

public class ComparatorFactory {

  private static ComparatorFactory instance;

  private Comparator<IDIYComponent<?>> componentNameComparator;
  private Comparator<ComponentType> componentTypeComparator;
  private Comparator<PropertyWrapper> propertyNameComparator;
  private Comparator<IDIYComponent<?>> componentZOrderComparator;

  public static ComparatorFactory getInstance() {
    if (instance == null) {
      instance = new ComparatorFactory();
    }
    return instance;
  }

  public Comparator<IDIYComponent<?>> getComponentNameComparator() {
    if (componentNameComparator == null) {
      componentNameComparator = new Comparator<IDIYComponent<?>>() {

        @Override
        public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
          String name1 = o1.getName();
          String name2 = o2.getName();
          if (name1 == null || name2 == null) {
            return 0;
          }
          return name1.compareToIgnoreCase(name2);
        }
      };
    }
    return componentNameComparator;
  }

  public Comparator<ComponentType> getComponentTypeComparator() {
    if (componentTypeComparator == null) {
      componentTypeComparator = new Comparator<ComponentType>() {

        @Override
        public int compare(ComponentType o1, ComponentType o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }
      };
    }
    return componentTypeComparator;
  }

  public Comparator<PropertyWrapper> getPropertyNameComparator() {
    if (propertyNameComparator == null) {
      propertyNameComparator = new Comparator<PropertyWrapper>() {

        @Override
        public int compare(PropertyWrapper o1, PropertyWrapper o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }

      };
    }
    return propertyNameComparator;
  }

  /**
   * Creates a comparator that compares properties by sort order and then by name
   * @return
   */
  public Comparator<PropertyWrapper> getDefaultPropertyComparator() {
    if (propertyNameComparator == null) {
      propertyNameComparator = new Comparator<PropertyWrapper>() {

        @Override
        public int compare(PropertyWrapper o1, PropertyWrapper o2) {
          if (o1.isReadOnly() && !o2.isReadOnly())
            return 1;
          if (!o1.isReadOnly() && o2.isReadOnly())
            return -1;
          int comp = Integer.valueOf(o1.getSortOrder()).compareTo(o2.getSortOrder());
          if (comp != 0)
            return comp;
          return o1.getName().compareToIgnoreCase(o2.getName());
        }

      };
    }
    return propertyNameComparator;
  }

  public Comparator<IDIYComponent<?>> getComponentZOrderComparator() {
    if (componentZOrderComparator == null) {
      componentZOrderComparator = new Comparator<IDIYComponent<?>>() {

        @SuppressWarnings("unchecked")
        @Override
        public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
          ComponentType type1 =
              ComponentProcessor.getInstance().extractComponentTypeFrom(
                  (Class<? extends IDIYComponent<?>>) o1.getClass());
          ComponentType type2 =
              ComponentProcessor.getInstance().extractComponentTypeFrom(
                  (Class<? extends IDIYComponent<?>>) o2.getClass());
          return Double.valueOf(type1.getZOrder()).compareTo(type2.getZOrder());
        }
      };
    }
    return componentZOrderComparator;
  }
  
  public Comparator<IDIYComponent<?>> getComponentProjectZOrderComparator(final Project project) {
    return new Comparator<IDIYComponent<?>>() {
        
        @Override
        public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
          int index1 = project.getComponents().indexOf(o1);
          int index2 = project.getComponents().indexOf(o2);
          return Integer.valueOf(index1).compareTo(index2);
        }
      };
  }
}
