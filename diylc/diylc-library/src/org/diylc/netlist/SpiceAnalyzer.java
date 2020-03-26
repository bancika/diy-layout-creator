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
  
  protected Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException {
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
    int unconnectedIndex = groups.size();
    
    int maxLen = 0;
    for (IDIYComponent<?> c : allComponents) {
      if (c.getName().length() > maxLen)
        maxLen = c.getName().length();
    }
    
    for (IDIYComponent<?> c : allComponents) {      
      // change the prefix to match spice convention if needed
      String name = c.getName();
      String prefix = null;
      if (c instanceof ISpiceMapper)
        prefix = ((ISpiceMapper)c).getPrefix();
      if (prefix != null && !name.toLowerCase().startsWith(prefix.toLowerCase()))
        name = prefix + name;
        
      sb.append(fill(name, (int) (Math.ceil(maxLen / 5.0) * 5)));
      sb.append(" ");
      List<Integer> nodeIndices = new ArrayList<Integer>();
      
      // find node indices for each control point
      for (int i = 0; i < c.getControlPointCount(); i++) {
        // skip non-sticky points
        if (c.getControlPointNodeName(i) == null)
          continue;
        
        int pointIndex = i;        
        
        int nodeIndex = find(new Node(c, pointIndex), groups);
        if (nodeIndex < 0)
          nodeIndex = unconnectedIndex++;
        
        // 1-based convention
        nodeIndex++;
        
        // remap if needed
        if (c instanceof ISpiceMapper)
          pointIndex = ((ISpiceMapper)c).mapToSpiceNode(pointIndex);
        
        nodeIndices.add(nodeIndex);
      }
      
      // output to spice
      for (Integer nodeIndex : nodeIndices) {
        sb.append(fill(formatSpiceNode(nodeIndex), 5));
        sb.append(" ");
      }
      
      sb.append(c.getValue());
      
      if (c instanceof ISpiceMapper) {
        String comment = ((ISpiceMapper)c).getComment();
        if (comment != null)
          sb.append(" ; ").append(comment);
      }
      sb.append("<br>");
    }
        
    return new Summary(netlist, sb.toString());
  }
  
  private static String formatSpiceNode(int i) {
    return String.format("N%03d" , i);
  }
  
  private String fill(String source, int desiredLength) {
    String res = source;
    for (int i = 0; i < desiredLength - source.length(); i++)
      res += "&nbsp;";
    return res;
  }
}
