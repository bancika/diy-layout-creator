/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
 *
 */
package org.diylc.netlist;

import java.util.*;
import javax.swing.JLabel;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.components.connectivity.MultimeterProbe;

public class MultimeterAnalyzer extends AbstractNetlistAnalyzer implements INetlistAnalyzer {

  @Override
  public String getName() {
    return "Virtual Multimeter";
  }

  @Override
  public String getShortName() {
    return "multimeter";
  }

  @Override
  public String getIconName() {
    return "Multimeter";
  }

  @Override
  public String getFontName() {
    return new JLabel().getFont().getName();
  }

  protected Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException {
    Tree tree = constructTree(netlist);

    if (tree == null) {
      return new Summary(netlist, "No components found connecting the two multimeter probes.");
    }

    List<String> notes = new ArrayList<String>();

    StringBuilder sb = new StringBuilder();

    String asciiTree = tree.toAsciiString();

    if (asciiTree.trim().isEmpty()) {
      notes.add("No connection detected");
    } else {
      sb.append("<br><br>")
              .append("<font face='Courier New'>")
              .append(asciiTree)
              .append("</font>");
    }

    if (!notes.isEmpty()) {
      Collections.sort(notes);
      sb.append("<br><br>");
      for (String v : notes) {
        sb.append(v).append("<br>");
      }
    }

    return new Summary(netlist, sb.toString());
  }

  public Tree constructTree(Netlist netlist) throws TreeException {
    List<Node> probeNodes = find(Set.of(MultimeterProbe.class.getCanonicalName()), null, netlist);

    if (probeNodes.isEmpty()) {
      throw new TreeException("No multimeter probes are placed on the circuit.");
    } else if (probeNodes.size() != 2) {
      throw new TreeException(
          "Place exactly two multimeter probes on the circuit in order to run the analysis.");
    } else {
      probeNodes.sort(Comparator.comparing(n -> n.getDisplayName()));
      return constructTreeBetween(netlist, probeNodes.get(0), probeNodes.get(1));
    }
  }
}
