//package org.diylc.plugins.chatbot.service;
//
//import org.diylc.core.IContinuity;
//import org.diylc.core.IDIYComponent;
//import org.diylc.core.ISwitch;
//import org.diylc.netlist.Node;
//import org.diylc.presenter.ContinuityArea;
//
//import java.awt.geom.Area;
//import java.awt.geom.Point2D;
//import java.util.*;
//
//public class NetlistBuilder {
//  private static final double eps = 1e-6;
//
//  public static List<Set<Node>> buildNets(List<IDIYComponent<?>> components, List<ContinuityArea> continuityAreas) {
//    Map<Node, Integer> terminalIndex = new HashMap<>();
//    List<Node> allTerminals = new ArrayList<>();
//    int idx = 0;
//
//    for (IDIYComponent<?> c : components) {
//      for (int i = 0; i < c.getControlPointCount(); i++) {
//        if (c.isControlPointSticky(i)) {
//          Node ref = new Node(c, i);
//          terminalIndex.put(ref, idx++);
//          allTerminals.add(ref);
//        }
//      }
//    }
//
//    UnionFind uf = new UnionFind(allTerminals.size());
//
//    // Step 1: connect conductive components
//    for (IDIYComponent<?> c : components) {
//      if (c instanceof IContinuity continuity && !(c instanceof ISwitch)) {
//        int count = c.getControlPointCount();
//        for (int i = 0; i < count; i++) {
//          for (int j = i + 1; j < count; j++) {
//            if (continuity.arePointsConnected(i, j)) {
//              Node a = new Node(c, i);
//              Node b = new Node(c, j);
//              uf.union(terminalIndex.get(a), terminalIndex.get(b));
//            }
//          }
//        }
//      }
//    }
//
//    // Step 2: connect terminals inside continuity areas
//    for (ContinuityArea continuity : continuityAreas) {
//      Area area = continuity.getSimplifiedArea();
//      List<Node> inside = new ArrayList<>();
//
//      for (Node t : allTerminals) {
//        Point2D p = t.getPoint2D();
//        if (area.contains(p) || area.intersects(p.getX() - eps/2, p.getY() - eps/2, eps, eps)) {
//          inside.add(t);
//        }
//      }
//
//      for (int i = 0; i < inside.size(); i++) {
//        for (int j = i + 1; j < inside.size(); j++) {
//          uf.union(terminalIndex.get(inside.get(i)), terminalIndex.get(inside.get(j)));
//        }
//      }
//    }
//
//    Map<Integer, Set<Node>> netMap = new HashMap<>();
//    for (Node t : allTerminals) {
//      int root = uf.find(terminalIndex.get(t));
//      netMap.computeIfAbsent(root, k -> new HashSet<>()).add(t);
//    }
//
//    // Filter out nets with only one terminal and remove exact duplicates
//    List<Set<Node>> nets = new ArrayList<>();
//    Set<Set<Node>> seen = new HashSet<>();
//
//    for (Set<Node> net : netMap.values()) {
//      if (net.size() > 1 && seen.add(net)) {
//        nets.add(net);
//      }
//    }
//    return nets;
//
//  }
//
//  // Union-Find with path compression and union by size
//  static class UnionFind {
//    private final int[] parent;
//    private final int[] size;
//
//    UnionFind(int n) {
//      parent = new int[n];
//      size = new int[n];
//      for (int i = 0; i < n; i++) {
//        parent[i] = i;
//        size[i] = 1;
//      }
//    }
//
//    int find(int x) {
//      if (parent[x] != x) {
//        parent[x] = find(parent[x]); // Path compression
//      }
//      return parent[x];
//    }
//
//    void union(int x, int y) {
//      int rootX = find(x);
//      int rootY = find(y);
//
//      if (rootX == rootY) return;
//
//      // Union by size: attach smaller tree to larger tree
//      if (size[rootX] < size[rootY]) {
//        parent[rootX] = rootY;
//        size[rootY] += size[rootX];
//      } else {
//        parent[rootY] = rootX;
//        size[rootX] += size[rootY];
//      }
//    }
//  }
//}
//
