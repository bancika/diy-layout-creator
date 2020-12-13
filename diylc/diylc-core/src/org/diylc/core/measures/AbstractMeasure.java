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
package org.diylc.core.measures;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.Format;

/***
 * Immutable measure class.
 * 
 * @author bancika
 *
 * @param <T>
 */
public abstract class AbstractMeasure<T extends Enum<? extends Unit>> implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  protected static final Format format = new DecimalFormat("0.####");

  protected Double value;
  protected T unit;

  public AbstractMeasure(Double value, T unit) {
    super();
    this.value = value;
    this.unit = unit;
  }

  public Double getValue() {
    return value;
  }

  public T getUnit() {
    return unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((unit == null) ? 0 : unit.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    AbstractMeasure<?> other = (AbstractMeasure<?>) obj;
    if (unit == null) {
      if (other.unit != null)
        return false;
    } else if (!unit.equals(other.unit))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return format.format(value) + unit;
  }
  
  protected static double parse(String value) {
    if (value.startsWith("."))
      value = "0" + value;
    value = value.replace(",", ".").replace("*", "");
    return Double.parseDouble(value);
  }
}
