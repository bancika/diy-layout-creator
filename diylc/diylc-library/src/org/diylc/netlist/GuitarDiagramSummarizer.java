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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.common.INetlistSummarizer;
import org.diylc.components.electromechanical.ClosedJack1_4;
import org.diylc.components.electromechanical.OpenJack1_4;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.JazzBassPickup;
import org.diylc.components.guitar.P90Pickup;
import org.diylc.components.guitar.PBassPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.passive.PotentiometerPanel;

public class GuitarDiagramSummarizer extends AbstractNetlistSummarizer implements INetlistSummarizer {
  
  private static Set<String> JACK_TYPES = new HashSet<String>();
  private static Set<String> POT_TYPES = new HashSet<String>();
  private static Set<String> PICKUP_TYPES = new HashSet<String>();
  static {
    JACK_TYPES.add(OpenJack1_4.class.getCanonicalName());
    JACK_TYPES.add(ClosedJack1_4.class.getCanonicalName());
    
    POT_TYPES.add(PotentiometerPanel.class.getCanonicalName());
    
    PICKUP_TYPES.add(SingleCoilPickup.class.getCanonicalName());
    PICKUP_TYPES.add(HumbuckerPickup.class.getCanonicalName());
    PICKUP_TYPES.add(P90Pickup.class.getCanonicalName());
    PICKUP_TYPES.add(JazzBassPickup.class.getCanonicalName());
    PICKUP_TYPES.add(PBassPickup.class.getCanonicalName());
  }

  @Override
  public String getName() {  
    return "Guitar Diagrams";
  }
  
  @Override
  public String getIconName() {
    return "Guitar";
  }
    
  public List<Summary> summarize(List<Netlist> netlists, Node preferredOutput) {    
    List<Summary> summaries = new ArrayList<Summary>();
    for (Netlist n : netlists)
      summaries.add(summarize(n, preferredOutput));
    
    return summaries;
  }
  
  private Summary summarize(Netlist netlist, Node preferredOutput) {
    List<String> notes = new ArrayList<String>();
    List<Node> jackTipNodes = find(JACK_TYPES, "Tip", netlist, null);
    List<Node> jackSleeveNodes = find(JACK_TYPES, "Sleeve", netlist, null);
    
    netlist = findAndEliminateVolumePot(netlist, jackTipNodes, jackSleeveNodes, notes);
    
    return new Summary(netlist, notes);
  }

  private Netlist findAndEliminateVolumePot(Netlist netlist, List<Node> jackTipGroups, List<Node> jackSleeveGroups, List<String> notes) {
    List<Node> potTipNodes = find(POT_TYPES, null, netlist, jackTipGroups);
    List<Node> potSleeveNodes = find(POT_TYPES, null, netlist, jackSleeveGroups);
    if (!potTipNodes.isEmpty() && !potSleeveNodes.isEmpty()) {
      if (potTipNodes.size() == 1 && potTipNodes.get(0).getDisplayName().equals("2") &&
          potSleeveNodes.size() == 1 && potSleeveNodes.get(0).getDisplayName().equals("3")) {
        notes.add("Volume pot detected: " + potTipNodes.get(0).getComponent().getName());
        
        Set<Node> toEliminate = new HashSet<Node>(potTipNodes);
        toEliminate.addAll(potSleeveNodes);
        return eliminate(netlist, toEliminate);
      } else if (potTipNodes.size() > 1 || potSleeveNodes.size() > 1) {
        notes.add("Detected multiple volume pots, test inconclusive.");
        return netlist;
      }
    }
    
    notes.add("No volume pot detected.");    
    return netlist;
  }
}
