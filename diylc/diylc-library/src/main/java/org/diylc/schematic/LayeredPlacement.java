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
package org.diylc.schematic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.schematic.SchematicBuilder.SymbolEntry;
import org.diylc.schematic.flow.FlowHints;
import org.diylc.schematic.flow.FlowRole;
import org.diylc.schematic.flow.SignalFlowAnalyzer;

/**
 * Arranges schematic symbols in columns that follow the direction the signal travels, so that the
 * drawing reads from input on the left to output on the right.
 *
 * <p>
 * The circuit becomes a graph of components joined by its nets. Two kinds of net are kept out of that
 * graph: the ones carrying a declared rail, and the single busiest net, which in practice is ground.
 * Ground reaches nearly every part of a circuit, so leaving it in makes every component adjacent to
 * every other and flattens the whole drawing into one column - and most projects never place a ground
 * symbol, so it cannot be recognised by its label alone.
 * </p>
 *
 * <p>
 * Where {@link SignalFlowAnalyzer} can tell that one component drives another the edge is directed,
 * and columns come from the longest path through those edges. Real projects rarely carry that
 * information - a transistor only reports its legs once someone has set its pin-out - so when
 * direction is absent the columns come from distance across the graph instead, measured from the far
 * end of its longest chain. That always spreads a connected circuit out, which is the one thing the
 * layout must never fail to do.
 * </p>
 *
 * <p>
 * Every collection here keeps insertion order. The same project has to produce the same drawing every
 * time, and hash ordering would quietly make the layout depend on object identity.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class LayeredPlacement {

  /** Gap between one column and the next, in pixels. */
  public static final double COLUMN_GAP = 130d;
  /** Gap between symbols stacked within a column, in pixels. */
  public static final double ROW_GAP = 60d;
  public static final double MARGIN = 120d;
  /** Grid step the finished positions are snapped to. */
  public static final double GRID = 10d;

  private static final int ORDERING_SWEEPS = 6;

  private final SignalFlowAnalyzer flowAnalyzer = new SignalFlowAnalyzer();

  /** One placed component: the physical part, the symbols it produced, and where it ended up. */
  private static class Vertex {

    final UUID id;
    final IDIYComponent<?> physical;
    final List<SymbolEntry> entries;
    final Set<UUID> drives = new LinkedHashSet<UUID>();
    final Set<UUID> drivenBy = new LinkedHashSet<UUID>();
    final Set<UUID> neighbours = new LinkedHashSet<UUID>();

    int layer;
    double barycentre;

    Vertex(IDIYComponent<?> physical, List<SymbolEntry> entries) {
      this.id = physical.getId();
      this.physical = physical;
      this.entries = entries;
    }

    boolean isDirected() {
      return !drives.isEmpty() || !drivenBy.isEmpty();
    }

    /** @return where the part sits on the board, used to break ties so the schematic echoes it. */
    double physicalY() {
      return physical.getControlPointCount() == 0 ? 0 : physical.getControlPoint(0).getY();
    }
  }

  private final Map<UUID, Vertex> vertices = new LinkedHashMap<UUID, Vertex>();

  /**
   * Places every symbol of every eligible component. The symbols must already have been given their
   * attitude, because the column widths are measured from their current geometry.
   */
  public void place(List<IDIYComponent<?>> eligible,
      Map<UUID, List<SymbolEntry>> entriesByPhysicalId, Netlist netlist) {
    buildVertices(eligible, entriesByPhysicalId);
    if (vertices.isEmpty()) {
      return;
    }
    buildEdges(netlist);
    breakFeedbackLoops();
    assignLayers();
    spreadUndirectedVertices();
    if (distinctLayerCount() < 2) {
      // nothing in the circuit said which way the signal runs, so fall back to plain distance
      layerByDistance();
    }
    assignCoordinates(orderWithinLayers());
  }

  private void buildVertices(List<IDIYComponent<?>> eligible,
      Map<UUID, List<SymbolEntry>> entriesByPhysicalId) {
    for (IDIYComponent<?> physical : eligible) {
      List<SymbolEntry> entries = entriesByPhysicalId.get(physical.getId());
      if (entries != null && !entries.isEmpty()) {
        vertices.put(physical.getId(), new Vertex(physical, entries));
      }
    }
  }

  /**
   * Turns each ordinary net into edges.
   *
   * <p>
   * Direction has to carry across the passive parts that sit between active devices: a collector
   * reaches the next stage's base through a coupling capacitor, so the two are never on one net and a
   * rule that only looked for an output and an input on the same net would find nothing at all.
   * Anything driving a net therefore drives every other member of it, and every member drives
   * whatever takes the net as an input.
   * </p>
   */
  private void buildEdges(Netlist netlist) {
    if (netlist == null) {
      return;
    }
    Group backbone = SchematicBuilder.backboneNet(netlist);
    Map<UUID, FlowHints> hintCache = new LinkedHashMap<UUID, FlowHints>();
    for (Group group : netlist.getSortedGroups()) {
      if (group == backbone || SchematicBuilder.railLabelOf(group) != null) {
        continue;
      }
      List<UUID> members = new ArrayList<UUID>();
      List<UUID> drivers = new ArrayList<UUID>();
      List<UUID> receivers = new ArrayList<UUID>();
      for (Node node : group.getSortedNodes()) {
        UUID id = node.getComponent().getId();
        if (!vertices.containsKey(id)) {
          continue;
        }
        if (!members.contains(id)) {
          members.add(id);
        }
        FlowRole role = roleOf(node, hintCache);
        if (role == FlowRole.OUTPUT && !drivers.contains(id)) {
          drivers.add(id);
        } else if (role == FlowRole.INPUT && !receivers.contains(id)) {
          receivers.add(id);
        }
      }
      for (int i = 0; i < members.size(); i++) {
        for (int j = i + 1; j < members.size(); j++) {
          vertices.get(members.get(i)).neighbours.add(members.get(j));
          vertices.get(members.get(j)).neighbours.add(members.get(i));
        }
      }
      for (UUID member : members) {
        for (UUID driver : drivers) {
          link(driver, member);
        }
        for (UUID receiver : receivers) {
          link(member, receiver);
        }
      }
    }
  }

  private void link(UUID from, UUID to) {
    if (from.equals(to)) {
      return;
    }
    vertices.get(from).drives.add(to);
    vertices.get(to).drivenBy.add(from);
  }

  private FlowRole roleOf(Node node, Map<UUID, FlowHints> hintCache) {
    UUID id = node.getComponent().getId();
    FlowHints hints;
    if (hintCache.containsKey(id)) {
      hints = hintCache.get(id);
    } else {
      hints = flowAnalyzer.analyze(node.getComponent());
      hintCache.put(id, hints);
    }
    return hints == null ? FlowRole.NEUTRAL : hints.getRole(node.getPointIndex());
  }

  /**
   * Drops the edges that close a loop, so that the graph can be layered at all.
   *
   * <p>
   * Feedback is not an anomaly here: an op-amp with a feedback network, or a transistor with a
   * bootstrapped or degenerated bias, produces a cycle in every real circuit of interest. Removing
   * the edge that closes the loop keeps the forward path intact, which is the one that determines how
   * the drawing reads.
   * </p>
   */
  private void breakFeedbackLoops() {
    Set<UUID> visited = new LinkedHashSet<UUID>();
    Set<UUID> onPath = new LinkedHashSet<UUID>();
    for (UUID id : new ArrayList<UUID>(vertices.keySet())) {
      if (!visited.contains(id)) {
        removeBackEdges(id, visited, onPath);
      }
    }
  }

  private void removeBackEdges(UUID id, Set<UUID> visited, Set<UUID> onPath) {
    visited.add(id);
    onPath.add(id);
    Vertex vertex = vertices.get(id);
    for (UUID next : new ArrayList<UUID>(vertex.drives)) {
      if (onPath.contains(next)) {
        vertex.drives.remove(next);
        vertices.get(next).drivenBy.remove(id);
      } else if (!visited.contains(next)) {
        removeBackEdges(next, visited, onPath);
      }
    }
    onPath.remove(id);
  }

  /** Longest path layering over the acyclic graph: a component sits one column after its drivers. */
  private void assignLayers() {
    Map<UUID, Integer> remaining = new LinkedHashMap<UUID, Integer>();
    List<UUID> ready = new ArrayList<UUID>();
    for (Vertex vertex : vertices.values()) {
      vertex.layer = 0;
      remaining.put(vertex.id, vertex.drivenBy.size());
      if (vertex.drivenBy.isEmpty()) {
        ready.add(vertex.id);
      }
    }
    for (int i = 0; i < ready.size(); i++) {
      Vertex vertex = vertices.get(ready.get(i));
      for (UUID next : vertex.drives) {
        Vertex target = vertices.get(next);
        target.layer = Math.max(target.layer, vertex.layer + 1);
        int left = remaining.get(next) - 1;
        remaining.put(next, left);
        if (left == 0) {
          ready.add(next);
        }
      }
    }
  }

  /**
   * Pulls the parts that no rule could give a direction to - the resistors and capacitors that make
   * up most of a circuit - towards the components they sit between, so they do not all pile up in the
   * first column.
   */
  private void spreadUndirectedVertices() {
    for (int sweep = 0; sweep < ORDERING_SWEEPS; sweep++) {
      boolean changed = false;
      for (Vertex vertex : vertices.values()) {
        if (vertex.isDirected() || vertex.neighbours.isEmpty()) {
          continue;
        }
        double sum = 0;
        int count = 0;
        for (UUID neighbour : vertex.neighbours) {
          sum += vertices.get(neighbour).layer;
          count++;
        }
        int target = (int) Math.round(sum / count);
        if (target != vertex.layer) {
          vertex.layer = target;
          changed = true;
        }
      }
      if (!changed) {
        break;
      }
    }
  }

  private int distinctLayerCount() {
    Set<Integer> layers = new LinkedHashSet<Integer>();
    for (Vertex vertex : vertices.values()) {
      layers.add(vertex.layer);
    }
    return layers.size();
  }

  /**
   * Spreads the circuit out by distance when nothing declared a direction.
   *
   * <p>
   * Each connected island is measured from one end of its longest chain - found by walking to the
   * furthest vertex and then measuring from there - and every component takes its column from how far
   * along it sits. The result is not electrically meaningful the way a driven chain is, but it does
   * lay the circuit out along its own longest path, which is far closer to a schematic than a single
   * stack of parts.
   * </p>
   */
  private void layerByDistance() {
    Set<UUID> remaining = new LinkedHashSet<UUID>(vertices.keySet());
    while (!remaining.isEmpty()) {
      UUID seed = remaining.iterator().next();
      Map<UUID, Integer> reach = distancesFrom(seed);
      UUID far = furthest(reach);
      Map<UUID, Integer> distances = distancesFrom(far);
      for (Map.Entry<UUID, Integer> entry : distances.entrySet()) {
        vertices.get(entry.getKey()).layer = entry.getValue();
        remaining.remove(entry.getKey());
      }
      // a vertex with no connections at all is its own island
      remaining.remove(seed);
    }
  }

  private Map<UUID, Integer> distancesFrom(UUID start) {
    Map<UUID, Integer> distances = new LinkedHashMap<UUID, Integer>();
    List<UUID> queue = new ArrayList<UUID>();
    distances.put(start, 0);
    queue.add(start);
    for (int i = 0; i < queue.size(); i++) {
      UUID current = queue.get(i);
      int next = distances.get(current) + 1;
      for (UUID neighbour : vertices.get(current).neighbours) {
        if (!distances.containsKey(neighbour)) {
          distances.put(neighbour, next);
          queue.add(neighbour);
        }
      }
    }
    return distances;
  }

  private static UUID furthest(Map<UUID, Integer> distances) {
    UUID best = null;
    int bestDistance = -1;
    for (Map.Entry<UUID, Integer> entry : distances.entrySet()) {
      if (entry.getValue() > bestDistance) {
        bestDistance = entry.getValue();
        best = entry.getKey();
      }
    }
    return best;
  }

  /**
   * Orders each column so that symbols sit near the ones they connect to, which is what keeps the
   * wires between columns from crossing. Ties fall back to the part's position on the board, so the
   * schematic keeps a family resemblance to the layout it came from.
   */
  private Map<Integer, List<Vertex>> orderWithinLayers() {
    Map<Integer, List<Vertex>> byLayer = new LinkedHashMap<Integer, List<Vertex>>();
    List<Vertex> sorted = new ArrayList<Vertex>(vertices.values());
    sorted.sort(Comparator.comparingInt((Vertex v) -> v.layer)
        .thenComparingDouble(Vertex::physicalY));
    for (Vertex vertex : sorted) {
      List<Vertex> column = byLayer.get(vertex.layer);
      if (column == null) {
        column = new ArrayList<Vertex>();
        byLayer.put(vertex.layer, column);
      }
      column.add(vertex);
    }

    Map<UUID, Integer> position = new LinkedHashMap<UUID, Integer>();
    for (List<Vertex> column : byLayer.values()) {
      for (int i = 0; i < column.size(); i++) {
        position.put(column.get(i).id, i);
      }
    }
    for (int sweep = 0; sweep < ORDERING_SWEEPS; sweep++) {
      for (List<Vertex> column : byLayer.values()) {
        for (Vertex vertex : column) {
          vertex.barycentre = barycentreOf(vertex, position);
        }
        column.sort(Comparator.comparingDouble((Vertex v) -> v.barycentre)
            .thenComparingDouble(Vertex::physicalY));
        for (int i = 0; i < column.size(); i++) {
          position.put(column.get(i).id, i);
        }
      }
    }
    return byLayer;
  }

  private double barycentreOf(Vertex vertex, Map<UUID, Integer> position) {
    double sum = 0;
    int count = 0;
    for (UUID neighbour : vertex.neighbours) {
      Integer at = position.get(neighbour);
      if (at != null) {
        sum += at;
        count++;
      }
    }
    return count == 0 ? position.get(vertex.id) : sum / count;
  }

  /**
   * Turns columns and orders into pixel positions. Each column is as wide as its widest symbol, and a
   * symbol is placed by its bounding box rather than by a control point, so that a part standing
   * upright cannot hang out of its cell.
   */
  private void assignCoordinates(Map<Integer, List<Vertex>> byLayer) {
    List<Integer> layers = new ArrayList<Integer>(byLayer.keySet());
    Collections.sort(layers);
    double x = MARGIN;
    for (Integer layer : layers) {
      List<Vertex> column = byLayer.get(layer);
      double columnWidth = 0;
      for (Vertex vertex : column) {
        columnWidth = Math.max(columnWidth, boundsOf(vertex.entries).getWidth());
      }
      double y = MARGIN;
      for (Vertex vertex : column) {
        Rectangle2D bounds = boundsOf(vertex.entries);
        // centre the symbol in its column so that a narrow part sits under a wide one
        double offsetX = x + (columnWidth - bounds.getWidth()) / 2;
        moveBoundsTo(vertex.entries, bounds, snap(offsetX), snap(y));
        y += bounds.getHeight() + ROW_GAP;
      }
      x += columnWidth + COLUMN_GAP;
    }
  }

  /** @return the box spanned by every control point of every symbol the component produced. */
  private static Rectangle2D boundsOf(List<SymbolEntry> entries) {
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (SymbolEntry entry : entries) {
      for (int i = 0; i < entry.symbol.getControlPointCount(); i++) {
        Point2D p = entry.symbol.getControlPoint(i);
        minX = Math.min(minX, p.getX());
        minY = Math.min(minY, p.getY());
        maxX = Math.max(maxX, p.getX());
        maxY = Math.max(maxY, p.getY());
      }
    }
    if (minX > maxX) {
      return new Rectangle2D.Double(0, 0, 0, 0);
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  private static void moveBoundsTo(List<SymbolEntry> entries, Rectangle2D bounds, double x,
      double y) {
    double dx = x - bounds.getX();
    double dy = y - bounds.getY();
    for (SymbolEntry entry : entries) {
      translate(entry.symbol, dx, dy);
    }
  }

  /**
   * Moves every control point by the same offset. The targets are worked out before anything is
   * written, because a rigid component such as SchematicBox rebuilds its remaining points when one of
   * them is set and would otherwise apply the offset twice.
   */
  private static void translate(IDIYComponent<?> component, double dx, double dy) {
    int count = component.getControlPointCount();
    Point2D[] targets = new Point2D[count];
    for (int i = 0; i < count; i++) {
      Point2D p = component.getControlPoint(i);
      targets[i] = new Point2D.Double(p.getX() + dx, p.getY() + dy);
    }
    for (int i = 0; i < count; i++) {
      component.setControlPoint(targets[i], i);
    }
  }

  private static double snap(double value) {
    return Math.round(value / GRID) * GRID;
  }
}
