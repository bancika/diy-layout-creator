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
import java.util.HashSet;
import java.util.List;
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
                
                // Only add component difference if it doesn't exist in either netlist
                if (!netlist2Components.contains(componentName)) {
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
                
                // Only add component difference if it doesn't exist in either netlist
                if (!netlist1Components.contains(componentName)) {
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

    Set<String> group1Keys = group1.getNodes().stream()
        .map(nodeKey)
        .collect(Collectors.toSet());
    Set<String> group2Keys = group2.getNodes().stream()
        .map(nodeKey)
        .collect(Collectors.toSet());

    return group1Keys.equals(group2Keys);
  }
}
