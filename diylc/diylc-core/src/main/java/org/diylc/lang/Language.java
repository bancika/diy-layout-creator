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
package org.diylc.lang;

import java.util.Map;

public class Language implements Comparable<Language> {

  private String name;
  private Map<String, String> properties;   
  
  public Language(String name, Map<String, String> properties) {
    super();
    this.name = name;
    this.properties = properties;
  }

  public String getName() {
    return name;
  }
  
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public int compareTo(Language o) {
    return name.compareToIgnoreCase(o.name);
  }
}
