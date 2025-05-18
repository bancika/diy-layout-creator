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

public abstract class NetlistAnalyzer {

  public NetlistAnalyzer() {
  }

  public List<Summary> summarize(List<Netlist> netlists, Node preferredOutput)
      throws TreeException {
    Map<String, Summary> summaries = new HashMap<String, Summary>();
    for (Netlist n : netlists) {
      Summary s = summarize(n, preferredOutput);
      if (summaries.containsKey(s.getSummaryHtml()))
        summaries.get(s.getSummaryHtml()).append(n);
      else
        summaries.put(s.getSummaryHtml(), s);
    }

    List<Summary> res = new ArrayList<Summary>(summaries.values());
    Collections.sort(res);
    return res;
  }

  protected abstract Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException;

  public Tree constructTreeBetween(Netlist netlist, Node nodeA, Node nodeB) {
    Tree tree = new Tree(TreeConnectionType.Parallel);
    connectNodes(netlist, nodeA, nodeB, tree.getChildren(), new Tree(TreeConnectionType.Series),
        new HashSet<Node>());

    return tree;
  }

  protected void connectNodes(Netlist netlist, Node nodeA, Node nodeB, List<Tree> concurrentPaths,
      Tree currentPath, Set<Node> visited) {
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

    Set<Node> pathVisited = new HashSet<>(visited);
    pathVisited.add(nodeA);

    // First collect all immediate next nodes and their paths
    Map<Node, List<Tree>> nextNodes = new HashMap<>();
    for (Node n : groupA.getNodes()) {
      if (pathVisited.contains(n))
        continue;

      IDIYComponent<?> c = n.getComponent();
      for (int i = 0; i < c.getControlPointCount(); i++) {
        int targetPoint = i;
        int sourcePoint = n.getPointIndex();

        if (targetPoint == sourcePoint)
          continue;

        if (c.getInternalLinkName(targetPoint, sourcePoint) != null) {
          Node newNodeA = new Node(c, targetPoint);
          if (pathVisited.contains(newNodeA))
            continue;

          try {
            Tree newPath = (Tree) currentPath.clone();
            TreeLeaf leaf = new TreeLeaf(c, targetPoint, sourcePoint);
            Tree newItem = new Tree(leaf);

            if (!newPath.getChildren().contains(newItem)) {
              newPath.getChildren().add(newItem);
              nextNodes.computeIfAbsent(newNodeA, k -> new ArrayList<>()).add(newPath);
            }
          } catch (CloneNotSupportedException e) {
            // Skip this path if clone fails
          }
        }
      }
    }

    // Now explore each next node
    List<Tree> allPaths = new ArrayList<>();
    for (Map.Entry<Node, List<Tree>> entry : nextNodes.entrySet()) {
      for (Tree path : entry.getValue()) {
        List<Tree> branchPaths = new ArrayList<>();
        connectNodes(netlist, entry.getKey(), nodeB, branchPaths, path, pathVisited);
        allPaths.addAll(branchPaths);
      }
    }

    if (allPaths.isEmpty())
      return;

    if (allPaths.size() > 1) {
      // Find common prefix among all paths
      List<Tree> commonPrefix = findCommonElements(allPaths, true, false);

      // Create a new tree for the merged result
      Tree mergedTree = new Tree(TreeConnectionType.Series);

      // Add common prefix elements
      for (Tree prefixElement : commonPrefix) {
        mergedTree.getChildren().add(prefixElement);
      }

      // Extract unique parts
      List<Tree> uniqueParts = new ArrayList<>();
      for (Tree path : allPaths) {
        Tree uniquePart = extractUniquePart(path, commonPrefix.size(), 0);
        if (uniquePart != null) {
          if (uniquePart.getChildren().size() == 1) {
            uniqueParts.add(uniquePart.getChildren().get(0));
          } else {
            uniqueParts.add(uniquePart);
          }
        }
      }

      // Create parallel section for unique parts
      if (!uniqueParts.isEmpty()) {
        Tree parallelSection = new Tree(TreeConnectionType.Parallel);
        parallelSection.getChildren().addAll(uniqueParts);

        // Add parallel section to series container
        Tree parallelContainer = new Tree(TreeConnectionType.Series);
        parallelContainer.getChildren().add(parallelSection);
        mergedTree.getChildren().add(parallelContainer);
      }

      concurrentPaths.add(mergedTree);
    } else {
      concurrentPaths.add(allPaths.get(0));
    }
  }

  private List<Tree> findCommonElements(List<Tree> paths, boolean fromStart, boolean backward) {
    if (paths.isEmpty())
      return new ArrayList<>();

    List<Tree> common = new ArrayList<>();
    Tree template = paths.get(0);
    int maxLength = template.getChildren().size();

    for (int i = 0; i < maxLength; i++) {
      int index = fromStart ?
          (backward ? template.getChildren().size() - i - 1 : i) :
          (backward ? i : template.getChildren().size() - i - 1);

      if (index < 0 || index >= template.getChildren().size())
        break;

      Tree element = template.getChildren().get(index);
      boolean isCommon = true;

      for (int j = 1; j < paths.size(); j++) {
        Tree current = paths.get(j);
        if (current.getChildren().size() <= i || index >= current.getChildren()
            .size() || !current.getChildren().get(index).equals(element)) {
          isCommon = false;
          break;
        }
      }

      if (isCommon) {
        common.add(element);
      } else {
        break;
      }
    }

    return common;
  }

  private Tree extractUniquePart(Tree path, int prefixSize, int suffixSize) {
    if (path.getChildren().size() <= prefixSize + suffixSize)
      return null;

    Tree uniquePart = new Tree(TreeConnectionType.Series);
    for (int i = prefixSize; i < path.getChildren().size() - suffixSize; i++) {
      uniquePart.getChildren().add(path.getChildren().get(i));
    }

    return uniquePart.getChildren().isEmpty() ? null : uniquePart;
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
        if (typeNames.contains(n.getComponent().getClass()
            .getCanonicalName()) && (nodeName == null || n.getDisplayName()
            .equalsIgnoreCase(nodeName))) {
          res.add(n);
        }
      }
    }
    return res;
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
