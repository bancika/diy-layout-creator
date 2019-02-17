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
import org.diylc.core.IDIYComponent;

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
    List<Node> jackTipNodes = find(JACK_TYPES, "Tip", netlist);
    List<Node> jackSleeveNodes = find(JACK_TYPES, "Sleeve", netlist);
    
    netlist = findAndEliminateVolumeControls(netlist, jackTipNodes, jackSleeveNodes, notes);
    
    return new Summary(netlist, notes);
  }

  private Netlist findAndEliminateVolumeControls(Netlist netlist, List<Node> jackTipGroups, List<Node> jackSleeveGroups, List<String> notes) {
    List<Node> potTipNodes = find(POT_TYPES, null, netlist, jackTipGroups);
    List<Node> potSleeveNodes = find(POT_TYPES, null, netlist, jackSleeveGroups);
    if (!potTipNodes.isEmpty() && !potSleeveNodes.isEmpty()) {                  
      Set<IDIYComponent<?>> pots = extractComponents(potTipNodes);
      List<Node> potInputNodes = find(POT_TYPES, "1", netlist, pots);
      
      if (allMatch(potTipNodes, "2") && allMatch(potSleeveNodes, "3") && allComponentsMatch(potInputNodes, pots)) {        
        List<String> potNames = extractNames(pots);
        notes.add("Detected volume control(s): " + potNames);
        
        Set<Node> toMerge = new HashSet<Node>(potTipNodes);
        toMerge.addAll(potInputNodes);
        return simplify(netlist, toMerge, potSleeveNodes);
      } else {
        notes.add("Detected incorrectly wired volume control(s).");
        return netlist;
      }
    }
    
    notes.add("No volume controls detected.");    
    return netlist;
  }
}
