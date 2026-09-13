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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.OrientationHV;
import org.diylc.components.connectivity.OrthogonalLine;
import org.diylc.core.IContinuity;
import org.diylc.core.ICommonNode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistBuilder;
import org.diylc.netlist.NetlistException;
import org.diylc.netlist.Node;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.ContinuityArea;

/**
 * Generates the initial {@link SchematicView} for a layout {@link Project}. Every eligible physical
 * component is turned into one or more schematic symbols via its {@link ISchematicFactory} (or the
 * {@link GenericBoxSchematicFactory} fallback), symbols are placed on a connection-density aware
 * grid and the nets from the layout netlist are drawn as {@link OrthogonalLine}s.
 *
 * @author Branislav Stojkovic
 */
public class SchematicBuilder {

  private static final Logger LOG = Logger.getLogger(SchematicBuilder.class);

  /** Grid step, in pixels, used for symbol placement. */
  public static final double GRID = 90d;
  /** Horizontal/vertical spacing between grid cells, in pixels. */
  public static final double CELL_SIZE = 220d;
  public static final double MARGIN = 120d;

  private final Map<Class<? extends ISchematicFactory>, ISchematicFactory> factoryCache =
      new HashMap<Class<? extends ISchematicFactory>, ISchematicFactory>();
  private final GenericBoxSchematicFactory genericFactory = new GenericBoxSchematicFactory();

  /**
   * One schematic symbol produced from a physical component together with the physical-&gt;schematic
   * pin mapping that produced it.
   */
  static class SymbolEntry {
    final IDIYComponent<?> symbol;
    final Map<Integer, Integer> pinMapping;

    SymbolEntry(IDIYComponent<?> symbol, Map<Integer, Integer> pinMapping) {
      this.symbol = symbol;
      this.pinMapping = pinMapping;
    }
  }

  /**
   * Builds the schematic and stores it on {@code project.getOrCreateSchematicView()}, replacing any
   * previous content.
   *
   * @param project         the layout project
   * @param continuityAreas continuity areas from the drawing manager (may be empty)
   */
  public void build(Project project, List<ContinuityArea> continuityAreas) {
    Netlist netlist = extractNetlist(project, continuityAreas);

    List<IDIYComponent<?>> schematicComponents = new ArrayList<IDIYComponent<?>>();
    Map<UUID, List<UUID>> physicalToSchematicMap = new LinkedHashMap<UUID, List<UUID>>();
    Map<UUID, List<SymbolEntry>> entriesByPhysicalId = new LinkedHashMap<UUID, List<SymbolEntry>>();

    List<IDIYComponent<?>> eligible = new ArrayList<IDIYComponent<?>>();
    for (IDIYComponent<?> physical : project.getComponents()) {
      if (!isEligible(physical)) {
        continue;
      }
      eligible.add(physical);
      List<SymbolEntry> entries = createSymbols(physical);
      if (entries.isEmpty()) {
        continue;
      }
      List<UUID> symbolIds = new ArrayList<UUID>();
      for (SymbolEntry entry : entries) {
        schematicComponents.add(entry.symbol);
        symbolIds.add(entry.symbol.getId());
      }
      physicalToSchematicMap.put(physical.getId(), symbolIds);
      entriesByPhysicalId.put(physical.getId(), entries);
    }

    placeSymbols(eligible, entriesByPhysicalId, netlist);

    List<OrthogonalLine> wires = createWires(netlist, entriesByPhysicalId);
    schematicComponents.addAll(wires);

    schematicComponents.sort(SCHEMATIC_ORDER);

    SchematicView view = project.getOrCreateSchematicView();
    view.getComponents().clear();
    view.getComponents().addAll(schematicComponents);
    view.setPhysicalToSchematicMap(physicalToSchematicMap);
    resizeCanvasToFit(view);
  }

  Netlist extractNetlist(Project project, List<ContinuityArea> continuityAreas) {
    try {
      List<Netlist> netlists = NetlistBuilder.extractNetlists(false, project,
          continuityAreas == null ? new ArrayList<ContinuityArea>() : continuityAreas);
      if (netlists == null || netlists.isEmpty()) {
        return null;
      }
      return netlists.get(0);
    } catch (NetlistException e) {
      LOG.warn("Could not extract netlist for schematic generation: " + e.getMessage());
      return null;
    }
  }

  /* ------------------------------------------------------------------ eligibility */

  static boolean isEligible(IDIYComponent<?> component) {
    if (component instanceof ICommonNode) {
      return true; // ground / common node symbols pass through
    }
    if (component instanceof ISwitch) {
      return true; // switches are drawn as symbols (netlist is built with includeSwitches=false)
    }
    if (component instanceof IContinuity) {
      return false; // wires, traces, solder bridges, board strips
    }
    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (component.isControlPointSticky(i)) {
        return true;
      }
    }
    return false;
  }

  /* ------------------------------------------------------------------ symbol creation */

  @SuppressWarnings("unchecked")
  List<SymbolEntry> createSymbols(IDIYComponent<?> physical) {
    if (physical instanceof ICommonNode) {
      try {
        IDIYComponent<?> clone = physical.clone();
        clone.setId(UUID.randomUUID());
        Map<Integer, Integer> identity = new HashMap<Integer, Integer>();
        for (int i = 0; i < physical.getControlPointCount(); i++) {
          identity.put(i, i);
        }
        List<SymbolEntry> list = new ArrayList<SymbolEntry>();
        list.add(new SymbolEntry(clone, identity));
        return list;
      } catch (CloneNotSupportedException e) {
        LOG.warn("Could not clone common-node component " + physical.getName(), e);
        return new ArrayList<SymbolEntry>();
      }
    }

    ISchematicFactory factory = resolveFactory(physical);
    List<SchematicSymbolMapping> mappings;
    try {
      mappings = factory.createSchematicSymbols(physical);
    } catch (Exception e) {
      LOG.warn("Schematic factory " + factory.getClass().getSimpleName() + " failed for "
          + physical.getName() + ", falling back to generic box", e);
      mappings = genericFactory.createSchematicSymbols(physical);
    }
    List<SymbolEntry> entries = new ArrayList<SymbolEntry>();
    if (mappings != null) {
      for (SchematicSymbolMapping mapping : mappings) {
        if (mapping.getSchematicSymbol() == null) {
          continue;
        }
        if (mapping.getSchematicSymbol().getId() == null) {
          mapping.getSchematicSymbol().setId(UUID.randomUUID());
        }
        entries.add(new SymbolEntry(mapping.getSchematicSymbol(), mapping.getPinMapping()));
      }
    }
    return entries;
  }

  @SuppressWarnings("unchecked")
  private ISchematicFactory resolveFactory(IDIYComponent<?> physical) {
    ComponentType type = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) physical.getClass());
    Class<? extends ISchematicFactory> factoryClass =
        type == null ? null : type.getSchematicFactoryClass();
    if (factoryClass == null) {
      return genericFactory;
    }
    ISchematicFactory cached = factoryCache.get(factoryClass);
    if (cached != null) {
      return cached;
    }
    try {
      ISchematicFactory factory = factoryClass.getDeclaredConstructor().newInstance();
      factoryCache.put(factoryClass, factory);
      return factory;
    } catch (Exception e) {
      LOG.warn("Could not instantiate schematic factory " + factoryClass.getName()
          + ", using generic box", e);
      return genericFactory;
    }
  }

  /* ------------------------------------------------------------------ placement */

  private void placeSymbols(List<IDIYComponent<?>> eligible,
      Map<UUID, List<SymbolEntry>> entriesByPhysicalId, Netlist netlist) {
    Map<UUID, Integer> degree = connectionDegree(netlist);

    List<IDIYComponent<?>> ordered = new ArrayList<IDIYComponent<?>>(eligible);
    ordered.sort(Comparator.comparingInt((IDIYComponent<?> c) -> -degree.getOrDefault(c.getId(), 0)));

    int count = 0;
    for (IDIYComponent<?> physical : ordered) {
      List<SymbolEntry> entries = entriesByPhysicalId.get(physical.getId());
      if (entries == null) {
        continue;
      }
      placeAtGridSlot(entries, count, ordered.size());
      count++;
    }
  }

  /** Places every symbol of {@code entries} into grid cell {@code slot} of {@code totalSlots}. */
  static void placeAtGridSlot(List<SymbolEntry> entries, int slot, int totalSlots) {
    int columns = Math.max(1, (int) Math.ceil(Math.sqrt(Math.max(totalSlots, 1))));
    int col = slot % columns;
    int row = slot / columns;
    double baseX = MARGIN + col * CELL_SIZE;
    double baseY = MARGIN + row * CELL_SIZE;
    int sub = 0;
    for (SymbolEntry entry : entries) {
      moveAnchorTo(entry.symbol, snap(baseX), snap(baseY + sub * (CELL_SIZE / 2)));
      sub++;
    }
  }

  private static Map<UUID, Integer> connectionDegree(Netlist netlist) {
    Map<UUID, Integer> degree = new HashMap<UUID, Integer>();
    if (netlist == null) {
      return degree;
    }
    for (Group group : netlist.getGroups()) {
      for (Node node : group.getNodes()) {
        UUID id = node.getComponent().getId();
        degree.merge(id, group.getNodes().size() - 1, Integer::sum);
      }
    }
    return degree;
  }

  /** Translates every control point of the component so that control point 0 lands on the target. */
  static void moveAnchorTo(IDIYComponent<?> component, double targetX, double targetY) {
    int count = component.getControlPointCount();
    if (count == 0) {
      return;
    }
    Point2D anchor = component.getControlPoint(0);
    double dx = targetX - anchor.getX();
    double dy = targetY - anchor.getY();
    // Snapshot the target positions before mutating: some components (e.g. SchematicBox) recompute
    // all of their control points when control point 0 is set, so reading getControlPoint(i) inside
    // the loop after that would double-apply the offset.
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

  /* ------------------------------------------------------------------ wiring */

  List<OrthogonalLine> createWires(Netlist netlist,
      Map<UUID, List<SymbolEntry>> entriesByPhysicalId) {
    List<OrthogonalLine> wires = new ArrayList<OrthogonalLine>();
    if (netlist == null) {
      return wires;
    }

    for (Group group : netlist.getSortedGroups()) {
      List<Pin> pins = new ArrayList<Pin>();
      for (Node node : group.getNodes()) {
        UUID physicalId = node.getComponent().getId();
        List<SymbolEntry> entries = entriesByPhysicalId.get(physicalId);
        if (entries == null) {
          continue;
        }
        for (SymbolEntry entry : entries) {
          Integer schematicIndex = entry.pinMapping.get(node.getPointIndex());
          if (schematicIndex == null || schematicIndex >= entry.symbol.getControlPointCount()) {
            continue;
          }
          Point2D location = entry.symbol.getControlPoint(schematicIndex);
          pins.add(new Pin(entry.symbol, schematicIndex,
              new Point2D.Double(location.getX(), location.getY())));
        }
      }
      if (pins.size() < 2) {
        continue;
      }
      // chain the pins after sorting left-to-right, top-to-bottom
      pins.sort(Comparator.comparingDouble((Pin p) -> p.location.getX())
          .thenComparingDouble(p -> p.location.getY()));
      for (int i = 0; i < pins.size() - 1; i++) {
        Pin a = pins.get(i);
        Pin b = pins.get(i + 1);
        wires.add(createWire(a, b));
      }
    }
    return wires;
  }

  private static class Pin {
    final IDIYComponent<?> symbol;
    final int pinIndex;
    final Point2D location;

    Pin(IDIYComponent<?> symbol, int pinIndex, Point2D location) {
      this.symbol = symbol;
      this.pinIndex = pinIndex;
      this.location = location;
    }
  }

  /**
   * Connects two pins with a wire whose single bend point is placed so that the wire leaves the
   * first pin along the axis that pin faces. {@link OrthogonalLine} derives the rest of the route
   * from that one point, and keeps deriving it as the sticky endpoints follow the symbols around.
   */
  private static OrthogonalLine createWire(Pin a, Pin b) {
    OrthogonalLine wire = new OrthogonalLine();
    wire.setId(UUID.randomUUID());
    wire.setControlPoint(copyOf(a.location), 0);
    wire.setControlPoint(bendPoint(a, b), 1);
    wire.setControlPoint(copyOf(b.location), 2);
    return wire;
  }

  private static Point2D bendPoint(Pin a, Pin b) {
    Point2D from = a.location;
    Point2D to = b.location;
    // pins that already share a row or a column need no bend; a handle sitting on the first pin
    // collapses the route to a straight run
    if (Math.abs(from.getX() - to.getX()) < 1 || Math.abs(from.getY() - to.getY()) < 1) {
      return copyOf(from);
    }
    if (exitDirection(a.symbol, from) == OrientationHV.VERTICAL) {
      // on the first pin's column, which swallows the leading horizontal run
      return new Point2D.Double(from.getX(), snap((from.getY() + to.getY()) / 2));
    }
    // on the second pin's row, which swallows the trailing vertical run
    return new Point2D.Double(snap((from.getX() + to.getX()) / 2), to.getY());
  }

  /**
   * Guesses whether a wire should leave the given pin horizontally or vertically: away from the
   * symbol's centroid, along the dominant axis.
   */
  static OrientationHV exitDirection(IDIYComponent<?> symbol, Point2D pinLocation) {
    double cx = 0;
    double cy = 0;
    int n = symbol.getControlPointCount();
    if (n == 0) {
      return OrientationHV.HORIZONTAL;
    }
    for (int i = 0; i < n; i++) {
      cx += symbol.getControlPoint(i).getX();
      cy += symbol.getControlPoint(i).getY();
    }
    cx /= n;
    cy /= n;
    double dx = pinLocation.getX() - cx;
    double dy = pinLocation.getY() - cy;
    return Math.abs(dx) >= Math.abs(dy) ? OrientationHV.HORIZONTAL : OrientationHV.VERTICAL;
  }

  private static Point2D copyOf(Point2D p) {
    return new Point2D.Double(p.getX(), p.getY());
  }

  /* ------------------------------------------------------------------ misc */

  static void resizeCanvasToFit(SchematicView view) {
    double maxX = 0;
    double maxY = 0;
    for (IDIYComponent<?> component : view.getComponents()) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point2D p = component.getControlPoint(i);
        maxX = Math.max(maxX, p.getX());
        maxY = Math.max(maxY, p.getY());
      }
    }
    maxX += MARGIN;
    maxY += MARGIN;
    if (maxX > view.getWidth().convertToPixels()) {
      view.setWidth(new org.diylc.core.measures.Size(maxX, org.diylc.core.measures.SizeUnit.px));
    }
    if (maxY > view.getHeight().convertToPixels()) {
      view.setHeight(new org.diylc.core.measures.Size(maxY, org.diylc.core.measures.SizeUnit.px));
    }
  }

  /**
   * Paint and hit-test order for the schematic view. Wires go underneath the symbols, and not where
   * their {@link IDIYComponent#WIRING} z-order would otherwise put them, because the canvas hands a
   * click to the last matching component in this list. A wire runs right up to the pin it connects
   * to and its hit area is widened to three pixels, so a wire drawn on top would swallow the clicks
   * meant for the symbol underneath it and the user could not drag the symbol at all.
   */
  static final Comparator<IDIYComponent<?>> SCHEMATIC_ORDER =
      Comparator.comparingInt((IDIYComponent<?> c) -> c instanceof OrthogonalLine ? 0 : 1)
          .thenComparingDouble(SchematicBuilder::zOrderOf);

  static double zOrderOf(IDIYComponent<?> component) {
    ComponentType type = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
    return type == null ? IDIYComponent.COMPONENT : type.getZOrder();
  }

  /** Convenience overload used by callers that already have the drawing manager's areas. */
  public static void generate(Project project, Collection<ContinuityArea> continuityAreas) {
    new SchematicBuilder().build(project,
        continuityAreas == null ? new ArrayList<ContinuityArea>()
            : new ArrayList<ContinuityArea>(continuityAreas));
  }
}
