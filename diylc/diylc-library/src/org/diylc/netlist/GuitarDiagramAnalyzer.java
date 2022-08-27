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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.components.electromechanical.ClosedJack1_4;
import org.diylc.components.electromechanical.OpenJack1_4;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.JazzBassPickup;
import org.diylc.components.guitar.P90Pickup;
import org.diylc.components.guitar.PBassPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.passive.AxialFilmCapacitor;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.RadialCeramicDiskCapacitor;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Tree.ITreeWalker;

public class GuitarDiagramAnalyzer extends NetlistAnalyzer implements INetlistAnalyzer {

  private static Set<String> JACK_TYPES = new HashSet<String>();
  private static Set<String> POT_TYPES = new HashSet<String>();
  private static Set<String> PICKUP_TYPES = new HashSet<String>();
  private static Set<String> CAP_TYPES = new HashSet<String>();
  static {
    JACK_TYPES.add(OpenJack1_4.class.getCanonicalName());
    JACK_TYPES.add(ClosedJack1_4.class.getCanonicalName());

    POT_TYPES.add(PotentiometerPanel.class.getCanonicalName());

    PICKUP_TYPES.add(SingleCoilPickup.class.getCanonicalName());
    PICKUP_TYPES.add(HumbuckerPickup.class.getCanonicalName());
    PICKUP_TYPES.add(P90Pickup.class.getCanonicalName());
    PICKUP_TYPES.add(JazzBassPickup.class.getCanonicalName());
    PICKUP_TYPES.add(PBassPickup.class.getCanonicalName());

    CAP_TYPES.add(RadialCeramicDiskCapacitor.class.getCanonicalName());
    CAP_TYPES.add(RadialFilmCapacitor.class.getCanonicalName());
    CAP_TYPES.add(AxialFilmCapacitor.class.getCanonicalName());
  }

  @Override
  public String getName() {
    return "Analyze Guitar Diagrams";
  }

  @Override
  public String getShortName() {
    return "guitar-diagrams";
  }

  @Override
  public String getIconName() {
    return "Guitar";
  }

  @Override
  public String getFontName() {
    return new JLabel().getFont().getName();
  }


  private int positiveCount;
  private int negativeCount;
  private int noiseCount;

  protected Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException {
    List<String> notes = new ArrayList<String>();

    Tree tree = constructTree(netlist);

    Tree pickupTree = tree.filter(PICKUP_TYPES);

    if (pickupTree == null || pickupTree.getChildren().isEmpty()) {
      notes.add("No pickups are detected in this switch configuration");
    } else {
      positiveCount = 0;
      negativeCount = 0;
      noiseCount = 0;

      // analyze hum-cancellation and phase
      pickupTree.walk(new ITreeWalker() {

        @Override
        public void visit(Tree t) {}

        @Override
        public void visit(TreeLeaf l) {
          if ((l.toString().toLowerCase().contains("north")
              && (l.toString().toLowerCase().contains("->")))
              || (l.toString().toLowerCase().contains("south")
                  && (l.toString().toLowerCase().contains("<-"))))
            noiseCount++;
          if ((l.toString().toLowerCase().contains("north")
              && (l.toString().toLowerCase().contains("<-")))
              || (l.toString().toLowerCase().contains("south")
                  && (l.toString().toLowerCase().contains("->"))))
            noiseCount--;

          if (l.toString().toLowerCase().contains("->"))
            positiveCount++;
          if (l.toString().toLowerCase().contains("<-"))
            negativeCount++;
        }
      });

      Set<IDIYComponent<?>> pickups = tree.extractComponents(PICKUP_TYPES);
      Map<IDIYComponent<?>, Tree> pickupRoots = new HashMap<IDIYComponent<?>, Tree>();

      // analyze each pickup separately to see how each coil is wired
      for (IDIYComponent<?> c : pickups) {
        if (c instanceof AbstractGuitarPickup) {
          AbstractGuitarPickup pickup = (AbstractGuitarPickup) c;
          if (((AbstractGuitarPickup) c).isHumbucker()) {
            TreeLeaf nLeaf = new TreeLeaf(pickup, 0, 1);
            TreeLeaf sLeaf = new TreeLeaf(pickup, 2, 3);
            Tree nTree = tree.locate(nLeaf, false);
            Tree sTree = tree.locate(sLeaf, false);
            Tree parent = tree.findCommonParent(nTree, sTree);
            if (parent != null)
              pickupRoots.put(pickup, parent);
            if (nTree != null && sTree != null && parent != null) {
              notes.add("'" + pickup.getName() + "' pickup wired in humbucking mode with "
                  + parent.getConnectionType().name().toLowerCase() + " coils");
            } else if ((nTree == null && sTree != null) || (nTree != null && sTree == null)) {
              notes.add("'" + pickup.getName() + "' pickup wired in coil-split mode");
            }
          } else {
            TreeLeaf leaf = new TreeLeaf(pickup, 1, 2);
            Tree t = tree.locate(leaf, false);
            if (t != null)
              pickupRoots.put(pickup, t);
          }
        }
      }

      // figure out how pickups are wired between them
      if (pickupRoots.size() > 1) {
        Tree root = tree.findCommonParent(new ArrayList<Tree>(pickupRoots.values()));
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (IDIYComponent<?> c : pickupRoots.keySet()) {
          if (!first)
            sb.append(pickupRoots.size() == 2 ? " and " : ", ");
          first = false;
          sb.append("'").append(c.getName()).append("'");
        }
        sb.append(" pickups engaged, wired in ")
            .append(root.getConnectionType().name().toLowerCase());
        notes.add(sb.toString());
      } else if (pickupRoots.size() == 1) {
        AbstractGuitarPickup pickup = (AbstractGuitarPickup) pickupRoots.keySet().iterator().next();
        notes.add("'" + pickup.getName() + "' is the only engaged pickup");
      }

      boolean humCancelling = noiseCount == 0;

      if (humCancelling)
        notes.add("This configuration is hum-cancelling");
      else
        notes.add("This configuration is NOT hum-cancelling");

      if (positiveCount > 1 || negativeCount > 1 || (positiveCount == 1 && negativeCount == 1)) {
        boolean inPhase =
            (positiveCount == 0 && negativeCount > 0) || (positiveCount > 0 && negativeCount == 0);
        if (inPhase)
          notes.add("All pickup coils are wired in-phase");
        else
          notes.add("Some pickup coils are wired OUT-of-phase");
      }

      // locate volume and tone pots
      Map<IDIYComponent<?>, List<IDIYComponent<?>>> volPotPickupMap =
          new HashMap<IDIYComponent<?>, List<IDIYComponent<?>>>();
      Map<IDIYComponent<?>, List<IDIYComponent<?>>> tonePotPickupMap =
          new HashMap<IDIYComponent<?>, List<IDIYComponent<?>>>();
      Map<IDIYComponent<?>, List<IDIYComponent<?>>> tonePotPickupReverseMap =
          new HashMap<IDIYComponent<?>, List<IDIYComponent<?>>>();
      Map<IDIYComponent<?>, List<IDIYComponent<?>>> volPotPickupReverseMap =
          new HashMap<IDIYComponent<?>, List<IDIYComponent<?>>>();

      Set<IDIYComponent<?>> pots = tree.extractComponents(POT_TYPES);
      for (IDIYComponent<?> pot : pots) {
        TreeLeaf leaf1 = new TreeLeaf(pot, 0, 1);
        TreeLeaf leaf2 = new TreeLeaf(pot, 1, 2);
        Tree tree1 = tree.locate(leaf1, false);
        Tree tree2 = tree.locate(leaf2, false);

        if ((tree1 == null && tree2 != null) || (tree1 != null && tree2 == null)) {
          boolean tonePotReversed = tree1 != null;
          Tree t = tree1 == null ? tree2 : tree1;
          Tree p = tree.findParent(t);
          if (p != null && p.getChildren().size() == 2) {
            Set<IDIYComponent<?>> caps = p.extractComponents(CAP_TYPES);
            if (caps != null && caps.size() == 1) {
              for (Map.Entry<IDIYComponent<?>, Tree> r : pickupRoots.entrySet()) {
                Tree toneParent = tree.findCommonParent(p, r.getValue());
                if (toneParent != null
                    && toneParent.getConnectionType() == TreeConnectionType.Parallel) {
                  if (tonePotReversed) {
                    List<IDIYComponent<?>> pickupList = tonePotPickupReverseMap.get(pot);
                    if (pickupList == null) {
                      pickupList = new ArrayList<IDIYComponent<?>>();
                      tonePotPickupReverseMap.put(pot, pickupList);
                    }
                    pickupList.add(r.getKey());
                  } else {
                    List<IDIYComponent<?>> pickupList = tonePotPickupMap.get(pot);
                    if (pickupList == null) {
                      pickupList = new ArrayList<IDIYComponent<?>>();
                      tonePotPickupMap.put(pot, pickupList);
                    }
                    pickupList.add(r.getKey());
                  }
                }
              }
            }
          }
        }

        // volume
        if (tree1 == null || tree2 == null)
          continue;
        for (Map.Entry<IDIYComponent<?>, Tree> r : pickupRoots.entrySet()) {
          Tree parent1 = tree.findCommonParent(tree1, r.getValue());
          Tree parent2 = tree.findCommonParent(tree2, r.getValue());
          if (parent1 != null && parent2 != null
              && parent1.getConnectionType() == TreeConnectionType.Series
              && parent2.getConnectionType() == TreeConnectionType.Parallel) {
            List<IDIYComponent<?>> pickupList = volPotPickupMap.get(pot);
            if (pickupList == null) {
              pickupList = new ArrayList<IDIYComponent<?>>();
              volPotPickupMap.put(pot, pickupList);
            }
            pickupList.add(r.getKey());
          } else if (parent1 != null && parent2 != null
              && parent1.getConnectionType() == TreeConnectionType.Parallel
              && parent2.getConnectionType() == TreeConnectionType.Series) {
            List<IDIYComponent<?>> pickupList = volPotPickupReverseMap.get(pot);
            if (pickupList == null) {
              pickupList = new ArrayList<IDIYComponent<?>>();
              volPotPickupReverseMap.put(pot, pickupList);
            }
            pickupList.add(r.getKey());
          }
        }
      }

      for (Map.Entry<IDIYComponent<?>, List<IDIYComponent<?>>> e : volPotPickupMap.entrySet()) {
        StringBuilder sb = new StringBuilder(
            "'" + e.getKey().getName() + "' potentiometer acts as a volume control for ");
        boolean first = true;
        List<String> pickupNames =
            e.getValue().stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
        for (String pickupName : pickupNames) {
          if (!first)
            sb.append(pickupNames.size() == 2 ? " and " : ", ");
          first = false;
          sb.append("'").append(pickupName).append("'");
        }
        notes.add(sb.toString());
      }

      for (Map.Entry<IDIYComponent<?>, List<IDIYComponent<?>>> e : volPotPickupReverseMap
          .entrySet()) {
        StringBuilder sb = new StringBuilder(
            "'" + e.getKey().getName() + "' potentiometer acts as a volume control for ");
        boolean first = true;
        List<String> pickupNames =
            e.getValue().stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
        for (String pickupName : pickupNames) {
          if (!first)
            sb.append(pickupNames.size() == 2 ? " and " : ", ");
          first = false;
          sb.append("'").append(pickupName).append("'");
        }
        sb.append(", but is wired in REVERSE!");
        notes.add(sb.toString());
      }

      for (Map.Entry<IDIYComponent<?>, List<IDIYComponent<?>>> e : tonePotPickupMap.entrySet()) {
        StringBuilder sb = new StringBuilder(
            "'" + e.getKey().getName() + "' potentiometer acts as a tone control for ");
        boolean first = true;
        List<String> pickupNames =
            e.getValue().stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
        for (String pickupName : pickupNames) {
          if (!first)
            sb.append(pickupNames.size() == 2 ? " and " : ", ");
          first = false;
          sb.append("'").append(pickupName).append("'");
        }
        notes.add(sb.toString());
      }

      for (Map.Entry<IDIYComponent<?>, List<IDIYComponent<?>>> e : tonePotPickupReverseMap
          .entrySet()) {
        StringBuilder sb = new StringBuilder(
            "'" + e.getKey().getName() + "' potentiometer acts as a tone control for ");
        boolean first = true;
        List<String> pickupNames =
            e.getValue().stream().map(x -> x.getName()).sorted().collect(Collectors.toList());
        for (String pickupName : pickupNames) {
          if (!first)
            sb.append(pickupNames.size() == 2 ? " and " : ", ");
          first = false;
          sb.append("'").append(pickupName).append("'");
        }
        sb.append(", but is wired in REVERSE!");
        notes.add(sb.toString());
      }
    }

    StringBuilder sb = new StringBuilder();

    sb.append("Parallel/Series connectivity tree:<br><br>").append(tree.toHTML(0));
    if (!notes.isEmpty()) {
      Collections.sort(notes);
      sb.append("<br><br>Notes:<br>");
      for (String v : notes) {
        sb.append("&nbsp;&nbsp;").append(v).append("<br>");
      }
    }

    return new Summary(netlist, sb.toString());
  }

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
