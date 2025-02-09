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
import java.util.stream.Collectors;
import org.diylc.core.IDIYComponent;

public abstract class NetlistAnalyzer {

  public NetlistAnalyzer() {}
  
  public List<Summary> summarize(List<Netlist> netlists, Node preferredOutput) throws TreeException {    
    Map<String, Summary> summaries = new HashMap<String, Summary>();
    for (Netlist n : netlists) {
      Summary s = summarize(n, preferredOutput);
      if (summaries.containsKey(s.getSummary()))
        summaries.get(s.getSummary()).append(n);
      else
        summaries.put(s.getSummary(), s);
    }
    
    List<Summary> res = new ArrayList<Summary>(summaries.values());
    Collections.sort(res);
    return res;
  }
  
  protected abstract Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException;

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
          Node newNodeA = new Node(c, i, n.getZIndex());
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
    Map<Tree, Integer> uniques = new HashMap<Tree, Integer>();
    for (Tree t : paths) {
      if (t.getConnectionType() != TreeConnectionType.Series)
        continue;
      Tree key = t.getChildren().get(backward ? t.getChildren().size() - 1 : 0);
      Integer count = uniques.get(key);
      uniques.put(key, count == null ? 1 : count + 1);
    }
    
    List<Tree> unmerged = new ArrayList<Tree>(paths);

    for (Map.Entry<Tree, Integer> e : uniques.entrySet()) {
      if (e.getValue() > 1) {
        List<Tree> pathsToMerge = new ArrayList<Tree>();
        for (Tree t : paths) {
          Tree key = t.getChildren().get(backward ? t.getChildren().size() - 1 : 0);
          if (key.equals(e.getKey()))
            pathsToMerge.add(t);
        }
        unmerged.removeAll(pathsToMerge);

        Tree jointStart = new Tree(new ArrayList<Tree>(), TreeConnectionType.Series);
        Tree jointFinish = new Tree(new ArrayList<Tree>(), TreeConnectionType.Series);
        
        Tree unique = e.getKey();

        for (int i = 0; i < pathsToMerge.get(0).getChildren().size(); i++) {
          boolean canMerge = true;
          unique = pathsToMerge.get(0).getChildren().get(backward ? pathsToMerge.get(0).getChildren().size() - i - 1 : i);
          for (Tree t : pathsToMerge) {            
            if (t.getChildren().size() <= i || !t.getChildren().get(backward ? t.getChildren().size() - i - 1 : i).equals(unique)) {
              canMerge = false;
              break;
            }
          }
          if (canMerge)
            (backward ? jointFinish : jointStart).getChildren().add(unique);
          else
            break;
        }        
        
        for (int i = 0; i < pathsToMerge.get(0).getChildren().size(); i++) {
          boolean canMerge = true;
          unique = pathsToMerge.get(0).getChildren().get(backward ? i : pathsToMerge.get(0).getChildren().size() - i - 1);
          for (Tree t : pathsToMerge) {            
            if (t.getChildren().size() <= i || !t.getChildren().get(backward ? 0 : t.getChildren().size() - 1).equals(unique)) {
              canMerge = false;
              break;
            }
          }
          if (canMerge)
            (backward ? jointStart : jointFinish).getChildren().add(unique);
          else
            break;
        }

        if (!jointStart.getChildren().isEmpty() || !jointFinish.getChildren().isEmpty()) {
          Tree mergedTree = new Tree(new ArrayList<Tree>(), TreeConnectionType.Series);
          Tree parallelSection = new Tree(new ArrayList<Tree>(pathsToMerge), TreeConnectionType.Parallel);
          if (!jointStart.getChildren().isEmpty()) {
            mergedTree.getChildren().add(jointStart);
            for (Tree t : parallelSection.getChildren()) {
              t.trimChildrenLeft(jointStart.getChildren().size());
            }
          }
          mergedTree.getChildren().add(parallelSection);
          if (!jointFinish.getChildren().isEmpty()) {
            mergedTree.getChildren().add(jointFinish);
            for (Tree t : parallelSection.getChildren()) {
              t.trimChildrenRight(jointFinish.getChildren().size());
            }
          }
          paths.removeAll(pathsToMerge);
          paths.add(mergedTree);          
        }
      }
    }
    
    // try in the opposite direction for paths we haven't merged
    if (!backward && !unmerged.isEmpty()) {
      paths.removeAll(unmerged);
      mergePaths(unmerged, true);
      paths.addAll(unmerged);
    }
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
  
  protected int find(Node node, List<Group> groups) {
    for (int i = 0; i < groups.size(); i++)
      if (groups.get(i).getNodes().contains(node))
        return i;
    return -1;
  }

  protected boolean intersect(List<Group> groups, Node node) {
    for (Group g : groups)
      if (g.getNodes().contains(node))
        return true;
    return false;
  }

  protected Node intersect(List<Group> groups, Group group) {
    for (Group g : groups)
      for (Node node : group.getNodes())
        if (g.getNodes().contains(node))
          return node;
    return null;
  }
}
