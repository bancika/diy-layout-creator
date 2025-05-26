/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.plugins.compare.service;

import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.DummyView;
import org.diylc.common.IPlugInPort;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.netlist.NetlistException;
import org.diylc.plugins.compare.model.ConnectionDiff;
import org.diylc.plugins.compare.model.ComponentDiff;
import org.diylc.plugins.compare.model.CompareResults;
import org.diylc.presenter.Presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CompareService {

  private IPlugInPort plugInPort;

  public CompareService(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
  }

  public CompareResults compareWith(File file) {
    Presenter presenter = new Presenter(new DummyView(), InMemoryConfigurationManager.getInstance());
    presenter.loadProjectFromFile(file.getAbsolutePath());
    try {
      List<Netlist> otherNetlists = presenter.extractNetlists(false);
      List<Netlist> thisNetlists = plugInPort.extractNetlists(false);
      if (otherNetlists.size() != 1 || thisNetlists.size() != 1) {
        return new CompareResults(false, List.of(), List.of());
      }
      return compare(thisNetlists.get(0), otherNetlists.get(0));
    } catch (NetlistException e) {
      throw new RuntimeException(e);
    }
  }

  public CompareResults compare(Netlist netlist1, Netlist netlist2) {
    // Get all groups from both netlists
    Set<Group> groups1 = netlist1.getGroups();
    Set<Group> groups2 = netlist2.getGroups();

    // Build lists of differences
    List<ConnectionDiff> connectionDiffs = new ArrayList<>();
    Set<ComponentDiff> componentDiffs = new HashSet<>();
    Set<String> addedComponentNames = new HashSet<>();

    // Helper to get a unique string for a node
    java.util.function.Function<Node, String> nodeKey = node ->
        node.getComponent().getName() + ":" + node.getComponent().getControlPointNodeName(node.getPointIndex());

    // Get all component names from both netlists for existence check
    Set<String> netlist1Components = netlist1.getComponents().stream()
        .map(comp -> comp.getName())
        .collect(Collectors.toSet());
    Set<String> netlist2Components = netlist2.getComponents().stream()
        .map(comp -> comp.getName())
        .collect(Collectors.toSet());

    // For each group in netlist1, check if it exists in netlist2
    for (Group group1 : groups1) {
        boolean groupFound = false;
        for (Group group2 : groups2) {
            if (areGroupsEqual(group1, group2)) {
                groupFound = true;
                break;
            }
        }
        if (!groupFound) {
            // If group not found, check each component
            List<Node> nodes = new ArrayList<>(group1.getNodes());
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                String componentName = node.getComponent().getName();
                
                // Check if this component exists in any group in netlist2
                boolean componentExistsInNetlist2 = false;
                for (Group group2 : groups2) {
                    for (Node node2 : group2.getNodes()) {
                        if (node2.getComponent().getName().equals(componentName)) {
                            componentExistsInNetlist2 = true;
                            break;
                        }
                    }
                    if (componentExistsInNetlist2) break;
                }
                
                // Add component difference if it doesn't exist in any group in netlist2 and hasn't been added yet
                if (!componentExistsInNetlist2 && addedComponentNames.add(componentName)) {
                    componentDiffs.add(new ComponentDiff(componentName, true));
                }
                
                // Add connection differences for each pair of nodes
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node otherNode = nodes.get(j);
                    connectionDiffs.add(new ConnectionDiff(
                        node.getComponent().getName(),
                        node.getComponent().getControlPointNodeName(node.getPointIndex()),
                        otherNode.getComponent().getName(),
                        otherNode.getComponent().getControlPointNodeName(otherNode.getPointIndex()),
                        true));
                }
            }
        }
    }

    // For each group in netlist2, check if it exists in netlist1
    for (Group group2 : groups2) {
        boolean groupFound = false;
        for (Group group1 : groups1) {
            if (areGroupsEqual(group1, group2)) {
                groupFound = true;
                break;
            }
        }
        if (!groupFound) {
            // If group not found, check each component
            List<Node> nodes = new ArrayList<>(group2.getNodes());
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                String componentName = node.getComponent().getName();
                
                // Check if this component exists in any group in netlist1
                boolean componentExistsInNetlist1 = false;
                for (Group group1 : groups1) {
                    for (Node node1 : group1.getNodes()) {
                        if (node1.getComponent().getName().equals(componentName)) {
                            componentExistsInNetlist1 = true;
                            break;
                        }
                    }
                    if (componentExistsInNetlist1) break;
                }
                
                // Add component difference if it doesn't exist in any group in netlist1 and hasn't been added yet
                if (!componentExistsInNetlist1 && addedComponentNames.add(componentName)) {
                    componentDiffs.add(new ComponentDiff(componentName, false));
                }
                
                // Add connection differences for each pair of nodes
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node otherNode = nodes.get(j);
                    connectionDiffs.add(new ConnectionDiff(
                        node.getComponent().getName(),
                        node.getComponent().getControlPointNodeName(node.getPointIndex()),
                        otherNode.getComponent().getName(),
                        otherNode.getComponent().getControlPointNodeName(otherNode.getPointIndex()),
                        false));
                }
            }
        }
    }

    // Try to resolve differences by flipping non-polarized components
    if (!connectionDiffs.isEmpty()) {
        // Find all non-polarized components that have nodes in different groups
        Map<String, List<Group>> componentGroups = new HashMap<>();
        for (Group group : groups1) {
            for (Node node : group.getNodes()) {
                if (!node.getComponent().isPolarized()) {
                    componentGroups.computeIfAbsent(node.getComponent().getName(), k -> new ArrayList<>())
                        .add(group);
                }
            }
        }

        // Try swapping nodes for each non-polarized component that appears in multiple groups
        for (Map.Entry<String, List<Group>> entry : componentGroups.entrySet()) {
            if (entry.getValue().size() >= 2) {
                String componentName = entry.getKey();
                List<Group> groupsWithComponent = entry.getValue();
                
                // Get the nodes from the first two groups
                Group group1 = groupsWithComponent.get(0);
                Group group2 = groupsWithComponent.get(1);
                
                Node node1 = group1.getNodes().stream()
                    .filter(n -> n.getComponent().getName().equals(componentName))
                    .findFirst()
                    .get();
                Node node2 = group2.getNodes().stream()
                    .filter(n -> n.getComponent().getName().equals(componentName))
                    .findFirst()
                    .get();
                
                // Create new groups with swapped nodes
                Group newGroup1 = new Group();
                Group newGroup2 = new Group();
                
                // Copy all nodes except the ones we're swapping
                for (Node n : group1.getNodes()) {
                    if (!n.getComponent().getName().equals(componentName)) {
                        newGroup1.getNodes().add(n);
                    }
                }
                for (Node n : group2.getNodes()) {
                    if (!n.getComponent().getName().equals(componentName)) {
                        newGroup2.getNodes().add(n);
                    }
                }
                
                // Add the swapped nodes
                newGroup1.getNodes().add(node2);
                newGroup2.getNodes().add(node1);
                
                // Create a new set of groups with the swapped nodes
                Set<Group> newGroups1 = new HashSet<>(groups1);
                newGroups1.remove(group1);
                newGroups1.remove(group2);
                newGroups1.add(newGroup1);
                newGroups1.add(newGroup2);
                
                // Check if this resolves ALL differences
                List<ConnectionDiff> newDiffs = new ArrayList<>();
                Set<ComponentDiff> newComponentDiffs = new HashSet<>();
                
                // Check groups from netlist1 against netlist2
                for (Group g1 : newGroups1) {
                    boolean groupFound = false;
                    for (Group g2 : groups2) {
                        if (areGroupsEqual(g1, g2)) {
                            groupFound = true;
                            break;
                        }
                    }
                    if (!groupFound) {
                        // Add connection differences for this group
                        List<Node> nodes = new ArrayList<>(g1.getNodes());
                        for (int i = 0; i < nodes.size(); i++) {
                            Node node = nodes.get(i);
                            for (int j = i + 1; j < nodes.size(); j++) {
                                Node otherNode = nodes.get(j);
                                newDiffs.add(new ConnectionDiff(
                                    node.getComponent().getName(),
                                    node.getComponent().getControlPointNodeName(node.getPointIndex()),
                                    otherNode.getComponent().getName(),
                                    otherNode.getComponent().getControlPointNodeName(otherNode.getPointIndex()),
                                    true));
                            }
                        }
                    }
                }
                
                // Check groups from netlist2 against netlist1
                for (Group g2 : groups2) {
                    boolean groupFound = false;
                    for (Group g1 : newGroups1) {
                        if (areGroupsEqual(g1, g2)) {
                            groupFound = true;
                            break;
                        }
                    }
                    if (!groupFound) {
                        // Add connection differences for this group
                        List<Node> nodes = new ArrayList<>(g2.getNodes());
                        for (int i = 0; i < nodes.size(); i++) {
                            Node node = nodes.get(i);
                            for (int j = i + 1; j < nodes.size(); j++) {
                                Node otherNode = nodes.get(j);
                                newDiffs.add(new ConnectionDiff(
                                    node.getComponent().getName(),
                                    node.getComponent().getControlPointNodeName(node.getPointIndex()),
                                    otherNode.getComponent().getName(),
                                    otherNode.getComponent().getControlPointNodeName(otherNode.getPointIndex()),
                                    false));
                            }
                        }
                    }
                }
                
                // If this swap resolved all differences, use the new groups
                if (newDiffs.isEmpty() && newComponentDiffs.isEmpty()) {
                    groups1 = newGroups1;
                    connectionDiffs = new ArrayList<>();
                    componentDiffs = new HashSet<>();
                    break;
                }
            }
        }
    }

    if (connectionDiffs.isEmpty() && componentDiffs.isEmpty()) {
        return new CompareResults(true, List.of(), List.of());
    }
    return new CompareResults(false, connectionDiffs, new ArrayList<>(componentDiffs));
  }

  private boolean areGroupsEqual(Group group1, Group group2) {
    if (group1.getNodes().size() != group2.getNodes().size()) {
        return false;
    }

    // Helper to get a unique string for a node
    java.util.function.Function<Node, String> nodeKey = node ->
        node.getComponent().getName() + ":" + node.getComponent().getControlPointNodeName(node.getPointIndex());

    // For polarized components, we need to check exact node order
    List<String> group1Keys = group1.getNodes().stream()
        .map(nodeKey)
        .collect(Collectors.toList());
    List<String> group2Keys = group2.getNodes().stream()
        .map(nodeKey)
        .collect(Collectors.toList());

    // If any component in the group is polarized, we need exact order match
    boolean hasPolarizedComponent = group1.getNodes().stream()
        .anyMatch(node -> node.getComponent().isPolarized()) ||
        group2.getNodes().stream()
        .anyMatch(node -> node.getComponent().isPolarized());

    if (hasPolarizedComponent) {
        return group1Keys.equals(group2Keys);
    }

    // For non-polarized components, we can use unordered comparison
    return new HashSet<>(group1Keys).equals(new HashSet<>(group2Keys));
  }
}
