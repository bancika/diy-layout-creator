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
 */
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.diylc.core.IDIYComponent;

public abstract class AbstractNetlistAnalyzer {

  public AbstractNetlistAnalyzer() {}
  
  public List<Summary> summarize(List<Netlist> netlists) throws TreeException {
    Map<String, Summary> summaries = new HashMap<String, Summary>();
    for (Netlist n : netlists) {
      Summary s = summarize(n);
      if (summaries.containsKey(s.getSummaryHtml()))
        summaries.get(s.getSummaryHtml()).append(n);
      else
        summaries.put(s.getSummaryHtml(), s);
    }
    
    List<Summary> res = new ArrayList<Summary>(summaries.values());
    res.forEach(summary -> NetlistBuilder.cleanupRedundantSwitches(summary.getNetlist().getSwitchSetup()));
    Collections.sort(res);
    return res;
  }
  
  protected abstract Summary summarize(Netlist netlist) throws TreeException;

  public Tree constructTreeBetween(Netlist netlist, Node nodeA, Node nodeB) {
    Tree tree = new Tree(TreeConnectionType.Parallel);
    connectNodes(netlist, nodeA, nodeB, tree.getChildren(), new Tree(TreeConnectionType.Series), new HashSet<Node>());

    return tree;
  }
  
//  protected Tree removeRedundantElements(Tree tree) {
//    List<Tree> children = tree.getChildren()
//        .stream()
//        .map(t -> removeRedundantElements(t))
//        .collect(Collectors.toList());
//    if (children.size() == 1) {
//      Tree first = children.get(0);
//      return first;
//    }
//  }

  protected void connectNodes(Netlist netlist, Node nodeA, Node nodeB, List<Tree> concurrentPaths, Tree currentPath,
      Set<Node> visited) {
    if (nodeA == nodeB) {
      concurrentPaths.add(currentPath);
      return;
    }

    Group groupA = findGroup(netlist, nodeA);

    if (groupA == null)
      return;

    if (groupA.getNodes().contains(nodeB)) {
      concurrentPaths.add(currentPath);
      return;
    }

    visited.addAll(groupA.getNodes());

    List<Tree> newConcurrentPaths = new ArrayList<Tree>();

    for (Node n : groupA.getNodes()) {
      IDIYComponent<?> c = n.getComponent();
      for (int i = 0; i < c.getControlPointCount(); i++) {
        if (i != n.getPointIndex() && c.getInternalLinkName(i, n.getPointIndex()) != null) {
          TreeLeaf l = new TreeLeaf(c, i, n.getPointIndex());
          Node newNodeA = new Node(c, i);
          if (!visited.contains(newNodeA)) {
            // visited.add(n);
            Tree newCurrentPath = null;
            try {
              newCurrentPath = (Tree) currentPath.clone();
            } catch (CloneNotSupportedException e) {
            }
            Tree newItem = new Tree(l);
            if (!newCurrentPath.getChildren().contains(newItem))
              newCurrentPath.getChildren().add(newItem);
            // if (!currentPath.contains(l))
            // newCurrentPath.add(l);
            Set<Node> newVisited = new HashSet<Node>(visited);
            connectNodes(netlist, newNodeA, nodeB, newConcurrentPaths, newCurrentPath, newVisited);
          }
        }
      }
    }

    if (newConcurrentPaths.size() == 0)
      return;

    if (newConcurrentPaths.size() == 1) {
      if (newConcurrentPaths.get(0).getConnectionType() == TreeConnectionType.Series) {
        currentPath.getChildren().clear();
        currentPath.getChildren().addAll(newConcurrentPaths.get(0).getChildren());
      } else {
        currentPath.getChildren().add(newConcurrentPaths.get(0));
      }
      concurrentPaths.add(currentPath);
      return;
    }

    mergePaths(newConcurrentPaths, false);
    concurrentPaths.addAll(newConcurrentPaths);
  }

  protected void mergePaths(List<Tree> paths, boolean backward) {
    // First sort paths to get deterministic output
    paths.sort((a, b) -> {
        // Convert paths to strings for comparison
        String pathA = pathToString(a);
        String pathB = pathToString(b);
        return pathA.compareTo(pathB);
    });

    // Find common prefixes/suffixes between paths
    for (int i = 0; i < paths.size(); i++) {
        for (int j = i + 1; j < paths.size(); j++) {
            Tree path1 = paths.get(i);
            Tree path2 = paths.get(j);
            
            // Find common prefix
            List<Tree> commonPrefix = new ArrayList<>();
            int prefixLen = 0;
            while (prefixLen < path1.getChildren().size() && 
                   prefixLen < path2.getChildren().size() &&
                   path1.getChildren().get(prefixLen).equals(path2.getChildren().get(prefixLen))) {
                commonPrefix.add(path1.getChildren().get(prefixLen));
                prefixLen++;
            }
            
            // Find common suffix
            List<Tree> commonSuffix = new ArrayList<>();
            int suffixLen = 0;
            while (suffixLen < path1.getChildren().size() - prefixLen && 
                   suffixLen < path2.getChildren().size() - prefixLen &&
                   path1.getChildren().get(path1.getChildren().size() - 1 - suffixLen)
                       .equals(path2.getChildren().get(path2.getChildren().size() - 1 - suffixLen))) {
                commonSuffix.add(0, path1.getChildren().get(path1.getChildren().size() - 1 - suffixLen));
                suffixLen++;
            }
            
            // If we found common elements, merge the paths
            if (prefixLen > 0 || suffixLen > 0) {
                // Create series structure: prefix + parallel + suffix
                Tree mergedPath = new Tree(TreeConnectionType.Series);
                
                // Add common prefix
                if (!commonPrefix.isEmpty()) {
                    mergedPath.getChildren().addAll(commonPrefix);
                }
                
                // Add parallel section
                Tree parallelSection = new Tree(TreeConnectionType.Parallel);
                
                // Add unique parts of both paths
                List<Tree> unique1 = path1.getChildren().subList(prefixLen, path1.getChildren().size() - suffixLen);
                List<Tree> unique2 = path2.getChildren().subList(prefixLen, path2.getChildren().size() - suffixLen);
                
                if (!unique1.isEmpty()) {
                    Tree series1 = new Tree(TreeConnectionType.Series);
                    series1.getChildren().addAll(unique1);
                    parallelSection.getChildren().add(series1);
                }
                
                if (!unique2.isEmpty()) {
                    Tree series2 = new Tree(TreeConnectionType.Series);
                    series2.getChildren().addAll(unique2);
                    parallelSection.getChildren().add(series2);
                }
                
                if (!parallelSection.getChildren().isEmpty()) {
                    mergedPath.getChildren().add(parallelSection);
                }
                
                // Add common suffix
                if (!commonSuffix.isEmpty()) {
                    mergedPath.getChildren().addAll(commonSuffix);
                }
                
                // Replace original paths with merged path
                paths.remove(j);
                paths.set(i, mergedPath);
                j--;
            }
        }
    }
  }

  // Helper method to convert a path to a sortable string
  private String pathToString(Tree path) {
    StringBuilder sb = new StringBuilder();
    for (Tree child : path.getChildren()) {
        if (child.getLeaf() != null) {
            sb.append(child.getLeaf().getComponent().getName())
              .append(":");
        } else {
            // For non-leaf nodes (parallel/series), recursively convert
            sb.append("(").append(pathToString(child)).append(")");
        }
    }
    return sb.toString();
  }

  protected Group findGroup(Netlist netlist, Node node) {
    for (Group g : netlist.getGroups())
      if (g.getNodes().contains(node))
        return g;
    return null;
  }

  protected List<Node> find(Set<String> typeNames, String nodeName, Netlist netlist) {
    List<Node> res = new ArrayList<Node>();
    for (Group g : netlist.getGroups()) {
      for (Node n : g.getNodes()) {
        if (typeNames.contains(n.getComponent().getClass().getCanonicalName())
            && (nodeName == null || n.getDisplayName().equalsIgnoreCase(nodeName))) {
          res.add(n);
        }
      }
    }
    return res;
  }

  protected String extractName(IDIYComponent<?> c) {
    return c.getName() + " " + (c.getValueForDisplay() == null ? "" : c.getValueForDisplay());
  }
  
  public static List<Set<IDIYComponent<?>>> extractComponentGroups(List<Netlist> netlists) {
    List<Set<IDIYComponent<?>>> res = new ArrayList<Set<IDIYComponent<?>>>();
    for (Netlist n : netlists) {
      res.addAll(extractComponentGroups(n));
    }
    return res;
  }
  
  public static List<Set<IDIYComponent<?>>> extractComponentGroups(Netlist netlist) {
    List<Set<IDIYComponent<?>>> res = new ArrayList<Set<IDIYComponent<?>>>();
    for (Group g : netlist.getGroups()) {
      Set<IDIYComponent<?>> components = new HashSet<IDIYComponent<?>>();
      for (Node n : g.getNodes())
        components.add(n.getComponent());
      res.add(components);
    }
    return res;
  }
  
  public static int find(Node node, List<Group> groups) {
    for (int i = 0; i < groups.size(); i++)
      if (groups.get(i).getNodes().contains(node))
        return i;
    return -1;
  }

  public static boolean intersect(List<Group> groups, Node node) {
    for (Group g : groups)
      if (g.getNodes().contains(node))
        return true;
    return false;
  }

  public static Node intersect(List<Group> groups, Group group) {
    for (Group g : groups)
      for (Node node : group.getNodes())
        if (g.getNodes().contains(node))
          return node;
    return null;
  }
}
