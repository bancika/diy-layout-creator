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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.diylc.common.INetlistAnalyzer;
import org.diylc.core.IDIYComponent;

public class SpiceAnalyzer extends NetlistAnalyzer implements INetlistAnalyzer {

  public SpiceAnalyzer() {
  }

  @Override
  public String getName() {
    return "Generate Spice Netlist (beta)";
  }

  @Override
  public String getIconName() {
    return "JarBeanInto";
  }
  
  @Override
  public String getFontName() {   
    return "Courier New";
  }

  @Override
  public List<Summary> summarize(List<Netlist> netlists, Node preferredOutput) throws TreeException {
    List<Summary> summaries = new ArrayList<Summary>();
    for (Netlist n : netlists)
      summaries.add(summarize(n, preferredOutput));
    
    return summaries;
  }
  
  private Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException {
    // grab all components that are in the netlist
    List<IDIYComponent<?>> allComponents = new ArrayList<IDIYComponent<?>>(extractComponents(netlist));
    Collections.sort(allComponents, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }      
    });
    
    StringBuilder sb = new StringBuilder();
    
    List<Group> groups = netlist.getSortedGroups();
    int unconnectedIndex = groups.size() + 1;
    
    int maxLen = 0;
    for (IDIYComponent<?> c : allComponents) {
      if (c.getName().length() > maxLen)
        maxLen = c.getName().length();
    }
    
    for (IDIYComponent<?> c : allComponents) {
      sb.append(fill(c.getName(), (int) (Math.ceil(maxLen / 5.0) * 5)));
      sb.append(" ");
      for (int i = 0; i < c.getControlPointCount(); i++) {
        int index = find(new Node(c, i), groups);
        if (index < 0)
          index = unconnectedIndex++;
        else
          index++;
        sb.append(fill(Integer.toString(index), 5));
        sb.append(" ");
      }
      sb.append(c.getValueForDisplay());
      sb.append("<br>");
    }
        
    return new Summary(netlist, sb.toString());
  }
  
  private String fill(String source, int desiredLength) {
    String res = source;
    for (int i = 0; i < desiredLength - source.length(); i++)
      res += "&nbsp;";
    return res;
  }
}
