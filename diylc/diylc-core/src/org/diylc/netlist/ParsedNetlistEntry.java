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
package org.diylc.netlist;

import java.util.List;
import java.util.Map;

public class ParsedNetlistEntry {

  private List<Class<?>> typeCandidates;
  private Map<String, Object> values;
  private String rawType;
  
  public ParsedNetlistEntry() {   
  }
  
  public ParsedNetlistEntry(List<Class<?>> typeCandidates, String rawType,
      Map<String, Object> values) {
    super();
    this.typeCandidates = typeCandidates;
    this.rawType = rawType;
    this.values = values;
  }

  public List<Class<?>> getTypeCandidates() {
    return typeCandidates;
  }
  
  public String getRawType() {
    return rawType;
  }

  public Map<String, Object> getValues() {
    return values;
  }
}
