package org.diylc.plugins.chatbot.service;

import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.presenter.ContinuityArea;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.*;

public class NetlistBuilder {

  public static class TerminalRef {
    public final IDIYComponent<?> component;
    public final int terminalIndex;

    public TerminalRef(IDIYComponent<?> component, int terminalIndex) {
      this.component = component;
      this.terminalIndex = terminalIndex;
    }

    public Point2D getPosition() {
      return component.getControlPoint(terminalIndex);
    }

    @Override
    public String toString() {
      return component.getName() + "." + terminalIndex;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof TerminalRef)) return false;
      TerminalRef other = (TerminalRef) obj;
      return Objects.equals(component, other.component) && terminalIndex == other.terminalIndex;
    }

    @Override
    public int hashCode() {
      return Objects.hash(component, terminalIndex);
    }
  }

  public static List<Set<TerminalRef>> buildNets(List<IDIYComponent<?>> components, List<ContinuityArea> continuityAreas) {
    Map<TerminalRef, Integer> terminalIndex = new HashMap<>();
    List<TerminalRef> allTerminals = new ArrayList<>();
    int idx = 0;

    for (IDIYComponent<?> c : components) {
      for (int i = 0; i < c.getControlPointCount(); i++) {
        if (c.isControlPointSticky(i)) {
          TerminalRef ref = new TerminalRef(c, i);
          terminalIndex.put(ref, idx++);
          allTerminals.add(ref);
        }
      }
    }

    UnionFind uf = new UnionFind(allTerminals.size());

    // Step 1: connect conductive components
    for (IDIYComponent<?> c : components) {
      if (c instanceof IContinuity continuity && !(c instanceof ISwitch)) {
        int count = c.getControlPointCount();
        for (int i = 0; i < count; i++) {
          for (int j = i + 1; j < count; j++) {
            if (continuity.arePointsConnected(i, j)) {
              TerminalRef a = new TerminalRef(c, i);
              TerminalRef b = new TerminalRef(c, j);
              uf.union(terminalIndex.get(a), terminalIndex.get(b));
            }
          }
        }
      }
    }

    // Step 2: connect terminals inside continuity areas
    for (ContinuityArea continuity : continuityAreas) {
      Area area = continuity.getSimplifiedArea();
      List<TerminalRef> inside = new ArrayList<>();

      for (TerminalRef t : allTerminals) {
        Point2D p = t.getPosition();
        if (area.contains(p)) {
          inside.add(t);
        }
      }

      for (int i = 0; i < inside.size(); i++) {
        for (int j = i + 1; j < inside.size(); j++) {
          uf.union(terminalIndex.get(inside.get(i)), terminalIndex.get(inside.get(j)));
        }
      }
    }

    Map<Integer, Set<TerminalRef>> netMap = new HashMap<>();
    for (TerminalRef t : allTerminals) {
      int root = uf.find(terminalIndex.get(t));
      netMap.computeIfAbsent(root, k -> new HashSet<>()).add(t);
    }

    // Filter out nets with only one terminal and remove exact duplicates
    List<Set<TerminalRef>> nets = new ArrayList<>();
    Set<Set<TerminalRef>> seen = new HashSet<>();

    for (Set<TerminalRef> net : netMap.values()) {
      if (net.size() > 1 && seen.add(net)) {
        nets.add(net);
      }
    }
    return nets;

  }

  // Union-Find with path compression
  static class UnionFind {
    int[] parent;

    UnionFind(int n) {
      parent = new int[n];
      for (int i = 0; i < n; i++) parent[i] = i;
    }

    int find(int x) {
      if (parent[x] != x)
        parent[x] = find(parent[x]);
      return parent[x];
    }

    void union(int x, int y) {
      parent[find(x)] = find(y);
    }
  }
}

