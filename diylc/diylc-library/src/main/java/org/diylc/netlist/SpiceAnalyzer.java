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

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.NetlistSwitchPreference;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistAnalyzer;
import org.diylc.netlist.Node;
import org.diylc.netlist.Summary;
import org.diylc.netlist.TreeException;
import org.diylc.presenter.ComponentProcessor;

public class SpiceAnalyzer extends AbstractNetlistAnalyzer implements INetlistAnalyzer {

  public SpiceAnalyzer() {
  }

  @Override
  public String getName() {
    return "Generate Spice Netlist";
  }
  
  @Override
  public String getShortName() {
    return "spice";
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
  public Set<NetlistSwitchPreference> getSwitchPreference() {
    return EnumSet.allOf(NetlistSwitchPreference.class);
  }
  
  protected Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException {
    // grab all components that are in the netlist
    List<Group> groups = netlist.getSortedGroups();

    List<IDIYComponent<?>> allComponents = groups.stream()
            .flatMap(x -> x.getNodes().stream()
                    .map(y -> y.getComponent()))
            .distinct()
            .collect(Collectors.toList());

    int unconnectedIndex = groups.size();
    
    int maxLen = 0;
    for (IDIYComponent<?> c : allComponents) {
      if (c.getName().length() > maxLen)
        maxLen = c.getName().length();
    }
    
    List<String> lines = new ArrayList<String>();
    
    for (IDIYComponent<?> c : allComponents) {     
      StringBuilder sb = new StringBuilder();
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
        
        // remap if needed
        if (c instanceof ISpiceMapper)
          pointIndex = ((ISpiceMapper)c).mapToSpiceNode(pointIndex);
        
        int nodeIndex = find(new Node(c, pointIndex, 0), groups);
        if (nodeIndex < 0)
          nodeIndex = unconnectedIndex++;
        
        // 1-based convention
        nodeIndex++;       
        
        nodeIndices.add(nodeIndex);
      }
      
      // output to spice
      for (Integer nodeIndex : nodeIndices) {
        sb.append(fill(formatSpiceNode(nodeIndex), 5));
        sb.append(" ");
      }
      
      sb.append(formatValue(c));
      
      if (c instanceof ISpiceMapper) {
        String comment = ((ISpiceMapper)c).getComment();
        if (comment != null)
          sb.append(" ; ").append(comment);
      }
      sb.append("<br>");
      lines.add(sb.toString());
    }
        
    return new Summary(netlist, lines
        .stream()
        .sorted()
        .collect(Collectors.joining("\n")));
  }

  private static Object formatValue(IDIYComponent<?> c) {
    if (c.getValue() == null || c.getValue().toString().trim().isEmpty()) {
      String typeName = ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass()).getName();
//      if (
//          Void.class.equals(((ParameterizedType) c.getClass()
//              .getGenericSuperclass()).getActualTypeArguments()[0])) {
//        return typeName;
//      }
      if (typeName.contains(" ")) {
        return "\"" + typeName + "\"";
      }
      return typeName;
    }
    if (c.getValue().toString().contains(" ")) {
      return "\"" + c.getValue() + "\"";
    }
    return c.getValue();
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
