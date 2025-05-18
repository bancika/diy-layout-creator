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
package org.diylc.common;

import java.io.Serializable;

public class Favorite implements Serializable, Comparable<Favorite> {
  
  private static final long serialVersionUID = 1L;
  
  private FavoriteType type;
  private String name;   

  public Favorite(FavoriteType type, String name) {
    super();
    this.type = type;
    this.name = name;
  }

  public FavoriteType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    Favorite other = (Favorite) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type != other.type)
      return false;
    return true;
  }
  
  @Override
  public String toString() {   
    return type + ":" + name;
  }

  public enum FavoriteType {
    Component, Block
  }
  
  public String toDisplay() {
    String[] parts = name.split("\\.");
    return parts[parts.length - 1];    
  }

  @Override
  public int compareTo(Favorite o) {
    return toDisplay().compareToIgnoreCase(o.toDisplay());
  }
}
