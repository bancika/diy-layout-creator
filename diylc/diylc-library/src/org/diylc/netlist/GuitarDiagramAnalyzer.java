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

import org.diylc.common.INetlistAnalyzer;
import org.diylc.components.electromechanical.ClosedJack1_4;
import org.diylc.components.electromechanical.OpenJack1_4;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.JazzBassPickup;
import org.diylc.components.guitar.P90Pickup;
import org.diylc.components.guitar.PBassPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.core.IDIYComponent;

public class GuitarDiagramAnalyzer extends AbstractNetlistAnalyzer implements INetlistAnalyzer {
  
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
    List<Group> jackTipGroups = findGroups(JACK_TYPES, "Tip", netlist);
    List<Group> jackSleeveGroups = findGroups(JACK_TYPES, "Sleeve", netlist);
    
//    if (jackTipGroups.size() == 0 || jackSleeveGroups.size() == 0) {
//      notes.add("Could not find anything connected to a jack tip terminal.");
//    } else if (jackTipGroups.size() > 1 || jackSleeveGroups.size() > 1) {
//      notes.add("Multiple jacks found, could not proceed.");
//    } else {      
//      Group jackTipGroup = jackTipGroups.get(0);
//      Group jackSleeveGroup = jackSleeveGroups.get(0);      
//      netlist = findAndEliminatePots(netlist, jackTipGroup, jackSleeveGroup, notes);
//      // find jack tip group again after eliminating pots, it should be connecter straight to the pickups
//      jackTipGroups = findGroups(JACK_TYPES, "Tip", netlist);
//      if (jackTipGroups.size() == 1) {
//        jackTipGroup = jackTipGroups.get(0);
//        List<Node> pickupTipNodes = find(PICKUP_TYPES, null, jackTipGroup);
//        List<Node> pickupSleeveNodes = find(PICKUP_TYPES, null, jackSleeveGroup);
//        notes.add("test");
//      }
//    }
    
    try {
      return new Summary(netlist, notes, constructTree(netlist));
    } catch (TreeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  private Netlist findAndEliminatePots(Netlist netlist, Group jackTipGroup, Group jackSleeveGroup, List<String> notes) {
    // try to find any pots that have connections to jack tip or sleeve
    List<Node> potTipNodes = find(POT_TYPES, null, jackTipGroup);
    List<Node> potSleeveNodes = find(POT_TYPES, null, jackSleeveGroup);
    
    if (!potTipNodes.isEmpty() && !potSleeveNodes.isEmpty()) {     
      
      Set<IDIYComponent<?>> pots = extractComponents(potTipNodes);
      List<Node> potInputNodes = find(POT_TYPES, "1", netlist, pots);
//      List<Group> potInputGroups = findGroups(POT_TYPES, "1", netlist, pots);
      
//      if (allMatch(potTipNodes, "2") && allMatch(potSleeveNodes, "3")) {  
//        
//        for (Group g : potInputGroups) {
//          List<Node> potNodes = find(POT_TYPES, null, g);
//          List<Node> pickupNodes = find(PICKUP_TYPES, null, g);
//          if (potNodes.size() != 1) {
//            notes.add("Detected control potentiometer wiring issue.");
//          } else {            
//            StringBuilder sb = new StringBuilder("Detected volume potentiometer [");
//            sb.append(extractName(potNodes.get(0).getComponent())).append("] controlling: ");
//            Set<String> pickups = new HashSet<String>();
//            for (Node n : pickupNodes) {
//              pickups.add(n.getComponent().getName());
//            }
//            List<String> pickupList = new ArrayList<String>(pickups);
//            Collections.sort(pickupList);
//            sb.append(pickupList.toString());
//            notes.add(sb.toString());
//          }
//        }
//        List<String> potNames = extractNames(pots);
//        notes.add("Detected volume control(s): " + potNames);
        
        Set<Node> toMerge = new HashSet<Node>(potTipNodes);
        toMerge.addAll(potInputNodes);
        return simplify(netlist, toMerge, potSleeveNodes);
//      } else {
//        notes.add("Detected incorrectly wired volume control(s).");
//        return netlist;
//      }
    }
    
//    notes.add("No volume controls detected.");    
    return netlist;
  }

  @Override
  public Tree constructTree(Netlist netlist) throws TreeException {
    List<Node> jackTipNodes = find(JACK_TYPES, "Tip", netlist);
    List<Node> jackSleeveNodes = find(JACK_TYPES, "Sleeve", netlist);
    
    if (jackTipNodes.size() == 0 || jackSleeveNodes.size() == 0) {
      throw new TreeException("Could not find anything connected to a jack tip terminal.");
    } else if (jackTipNodes.size() > 1 || jackSleeveNodes.size() > 1) {
      throw new TreeException("Multiple jacks found, could not proceed.");
    } else {      
      Node jackTip = jackTipNodes.get(0);
      Node jackSleeve = jackSleeveNodes.get(0);      
      return constructTreeBetween(netlist, jackTip, jackSleeve);
    }
  }
}
