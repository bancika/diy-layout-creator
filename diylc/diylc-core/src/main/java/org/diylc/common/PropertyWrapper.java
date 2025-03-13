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
package org.diylc.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.diylc.core.IPropertyValidator;

/**
 * Entity class for editable properties extracted from component objects. Represents a single
 * editable property together with it's current value.
 * 
 * @author Branislav Stojkovic
 */
public class PropertyWrapper implements Cloneable {

  private String name;
  private Class<?> type;
  private Object value;
  private String setter;
  private String getter;
  private boolean defaultable;
  private IPropertyValidator validator;
  private boolean unique = true;
  private boolean changed = false;
  private int sortOrder;
  private Object ownerObject;

  public PropertyWrapper(String name, Class<?> type, String getter, String setter, boolean defaultable,
      IPropertyValidator validator, int sortOrder) {
    super();
    this.name = name;
    this.type = type;
    this.getter = getter;
    this.setter = setter;
    this.defaultable = defaultable;
    this.validator = validator;
    this.sortOrder = sortOrder;
    this.ownerObject = null;
  }

  public void readFrom(Object object) throws IllegalArgumentException, IllegalAccessException,
      InvocationTargetException, SecurityException, NoSuchMethodException {
    this.ownerObject = object;
    this.value = getGetter().invoke(object);
  }

  // public void readUniqueFrom(IDIYComponent component)
  // throws IllegalArgumentException, IllegalAccessException,
  // InvocationTargetException {
  // Object newValue = getter.invoke(component);
  // if (!newValue.equals(value)) {
  // this.value = null;
  // }
  // }

  public void writeTo(Object object) throws IllegalArgumentException, IllegalAccessException,
      InvocationTargetException, SecurityException, NoSuchMethodException {
    // nothing to do if no setter
    if (setter == null)
      return;
    object.getClass().getMethod(setter, type).invoke(object, this.value);
  }

  public Method getGetter() throws SecurityException, NoSuchMethodException {
    return getOwnerObject().getClass().getMethod(getter);
  }
  
  public boolean isReadOnly() {
    return setter == null;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Object getOwnerObject() {
    return ownerObject;
  }

  public boolean isDefaultable() {
    return defaultable;
  }

  public IPropertyValidator getValidator() {
    return validator;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isChanged() {
    return changed;
  }

  public void setChanged(boolean changed) {
    this.changed = changed;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  // @Override
  // public Object clone() throws CloneNotSupportedException {
  // // Try to invoke clone method on value if possible.
  // try {
  // Method cloneMethod = value.getClass().getMethod("clone");
  // return new Property(name, type, cloneMethod.invoke(value));
  // } catch (Exception e) {
  // }
  // return new Property(name, type, value);
  // }
  @Override
  public Object clone() throws CloneNotSupportedException {
    PropertyWrapper clone =
        new PropertyWrapper(this.name, this.type, this.getter, this.setter, this.defaultable, this.validator,
            this.sortOrder);
    clone.value = this.value;
    clone.changed = this.changed;
    clone.unique = this.unique;
    return clone;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (defaultable ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((setter == null) ? 0 : setter.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PropertyWrapper other = (PropertyWrapper) obj;
    if (defaultable != other.defaultable)
      return false;
    if (getter == null) {
      if (other.getter != null)
        return false;
    } else if (!getter.equals(other.getter))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (setter == null) {
      if (other.setter != null)
        return false;
    } else if (!setter.equals(other.setter))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return name + " = " + value;
  }
}
