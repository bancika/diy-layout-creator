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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.connectivity.OrthogonalLine;
import org.diylc.components.misc.CommonNode;
import org.diylc.components.misc.GroundSymbol;
import org.diylc.core.IContinuity;
import org.diylc.core.ICommonNode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
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
 * <p>
 * Nets that carry a common node (ground, supply rails) are not wired up at all. Each pin on such a
 * net gets its own ground or common-node stub drawn right next to it, which is how schematics are
 * conventionally drawn and keeps the densest net in the circuit from crossing the whole drawing.
 * </p>
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

  /**
   * Pin-to-pin span given to every two-terminal symbol. {@link AbstractLeadedComponent} defaults to
   * a one-inch span (200px) while the schematic symbol bodies are only 0.05in to 0.3in long, so the
   * default leaves 70 to 95 pixels of bare lead on each side — more lead than body, running well
   * into the neighbouring cell. Half an inch clears the longest body with a short stub either side
   * and lands on the 0.1in schematic grid.
   */
  public static final double SYMBOL_PITCH = new Size(0.5d, SizeUnit.in).convertToPixels();

  /** The label {@link GroundSymbol} reports; every other common-node label is treated as a supply. */
  private static final String GROUND_LABEL = "GND";

  /** Smallest net that may be taken for the ground backbone. */
  private static final int MIN_BACKBONE_SIZE = 4;

  private final Map<Class<? extends ISchematicFactory>, ISchematicFactory> factoryCache =
      new HashMap<Class<? extends ISchematicFactory>, ISchematicFactory>();
  private final GenericBoxSchematicFactory genericFactory = new GenericBoxSchematicFactory();
  private final WireRouter router = new WireRouter();

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
   * Everything the wiring pass produces: the routed wires for ordinary signal nets, and the ground
   * and supply stubs that stand in for the rail nets.
   */
  static class Wiring {

    private final List<OrthogonalLine> wires = new ArrayList<OrthogonalLine>();
    private final List<IDIYComponent<?>> stubs = new ArrayList<IDIYComponent<?>>();

    List<OrthogonalLine> getWires() {
      return wires;
    }

    List<IDIYComponent<?>> getStubs() {
      return stubs;
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

    Wiring wiring = createWires(netlist, entriesByPhysicalId);
    schematicComponents.addAll(wiring.getStubs());
    schematicComponents.addAll(wiring.getWires());

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
      // ground and supply nodes are not placed as symbols of their own; every pin on their net gets
      // a freshly generated stub during wiring instead
      return false;
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

  List<SymbolEntry> createSymbols(IDIYComponent<?> physical) {
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
    Map<UUID, Map<Integer, String>> railPins = railPins(netlist);

    // attitude first: the placement measures each symbol, so it has to see its final geometry
    for (IDIYComponent<?> physical : eligible) {
      List<SymbolEntry> entries = entriesByPhysicalId.get(physical.getId());
      if (entries != null) {
        orientSymbols(entries, railPins.get(physical.getId()));
      }
    }

    new LayeredPlacement().place(eligible, entriesByPhysicalId, netlist);
  }

  /**
   * Chooses the resting attitude of every two-terminal symbol of one component. A part with a leg on
   * a rail is a shunt element and stands upright with that leg pointing at the rail it belongs to;
   * everything else passes signal along the drawing and lies down.
   *
   * @param rails pin index -&gt; rail label for this component, or null when it touches no rail
   */
  static void orientSymbols(List<SymbolEntry> entries, Map<Integer, String> rails) {
    for (SymbolEntry entry : entries) {
      String railLabel = null;
      int railPin = -1;
      if (rails != null) {
        for (Map.Entry<Integer, Integer> mapped : entry.pinMapping.entrySet()) {
          String label = rails.get(mapped.getKey());
          if (label != null && mapped.getValue() < 2) {
            railLabel = label;
            railPin = mapped.getValue();
            break;
          }
        }
      }
      if (railLabel == null) {
        applyPitch(entry.symbol, false, false);
        continue;
      }
      // a ground symbol is drawn downward from its point, so the grounded leg belongs at the bottom;
      // supply labels sit above the part the way they are drawn on paper
      boolean railAtBottom = GROUND_LABEL.equals(railLabel);
      applyPitch(entry.symbol, true, (railPin == 0) == railAtBottom);
    }
  }

  /**
   * Rewrites the two terminals of a leaded symbol so that they sit {@link #SYMBOL_PITCH} apart,
   * centred on wherever the symbol already is. Rigid multi-pin symbols are left untouched — their
   * geometry comes from an anchor and a pin spacing, not from the terminals.
   *
   * <p>
   * The midpoint is preserved, which is what keeps the label control point (index 2, positioned
   * relative to the centre by the constructor) correct without having to move it as well.
   * </p>
   *
   * @param vertical stand the symbol upright rather than lying it down
   * @param flip     put control point 0 at the far end (bottom when upright, right when lying down)
   */
  static void applyPitch(IDIYComponent<?> symbol, boolean vertical, boolean flip) {
    if (!(symbol instanceof AbstractLeadedComponent) || symbol.getControlPointCount() < 2) {
      return;
    }
    Point2D first = symbol.getControlPoint(0);
    Point2D second = symbol.getControlPoint(1);
    double cx = (first.getX() + second.getX()) / 2;
    double cy = (first.getY() + second.getY()) / 2;
    double half = SYMBOL_PITCH / 2;
    Point2D near =
        vertical ? new Point2D.Double(cx, cy - half) : new Point2D.Double(cx - half, cy);
    Point2D far = vertical ? new Point2D.Double(cx, cy + half) : new Point2D.Double(cx + half, cy);
    symbol.setControlPoint(flip ? far : near, 0);
    symbol.setControlPoint(flip ? near : far, 1);
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

  /**
   * Collects, per physical component, which of its control points sit on a rail net and under which
   * label, so that placement can stand shunt parts upright.
   */
  static Map<UUID, Map<Integer, String>> railPins(Netlist netlist) {
    Map<UUID, Map<Integer, String>> result = new HashMap<UUID, Map<Integer, String>>();
    if (netlist == null) {
      return result;
    }
    for (Group group : netlist.getGroups()) {
      String label = railLabelOf(group);
      if (label == null) {
        continue;
      }
      for (Node node : group.getNodes()) {
        if (node.getComponent() instanceof ICommonNode) {
          continue;
        }
        result.computeIfAbsent(node.getComponent().getId(), k -> new HashMap<Integer, String>())
            .put(node.getPointIndex(), label);
      }
    }
    return result;
  }

  /**
   * {@link NetlistBuilder} emits a single node per common-node label, so a rail net is recognised by
   * the one common-node member it carries rather than by its size.
   *
   * @return the rail label of the net, or null when it is an ordinary signal net
   */
  static String railLabelOf(Group group) {
    for (Node node : group.getNodes()) {
      if (node.getComponent() instanceof ICommonNode) {
        return ((ICommonNode) node.getComponent()).getCommonNodeLabel();
      }
    }
    return null;
  }

  /**
   * Picks out the net that behaves like ground: the busiest one that carries no declared rail.
   *
   * <p>
   * Only about one project in eight places a ground symbol, so in most circuits ground arrives as an
   * ordinary net that simply happens to touch a large share of the components. Left in the layout
   * graph it makes every part adjacent to every other and flattens the drawing into a single column;
   * left in the wiring it is chained from one side of the schematic to the other and crosses
   * everything in between.
   * </p>
   *
   * @return the backbone net, or null when no net is big enough to be taken for one
   */
  static Group backboneNet(Netlist netlist) {
    if (netlist == null) {
      return null;
    }
    Group largest = null;
    int largestSize = 0;
    for (Group group : netlist.getSortedGroups()) {
      if (railLabelOf(group) != null) {
        continue;
      }
      int size = group.getNodes().size();
      if (size > largestSize) {
        largestSize = size;
        largest = group;
      }
    }
    return largestSize >= MIN_BACKBONE_SIZE ? largest : null;
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

  Wiring createWires(Netlist netlist, Map<UUID, List<SymbolEntry>> entriesByPhysicalId) {
    Wiring wiring = new Wiring();
    if (netlist == null) {
      return wiring;
    }

    Map<UUID, Rectangle2D> bodies = symbolBodies(entriesByPhysicalId);
    // wires already routed become obstacles for the ones that follow, so a dense net does not end up
    // drawing every connection on top of the last
    List<Line2D> placed = new ArrayList<Line2D>();
    Group backbone = backboneNet(netlist);

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

      String railLabel = railLabelOf(group);
      if (railLabel == null && group == backbone) {
        // an unlabelled ground net says the same thing drawn as stubs as it does chained across the
        // whole drawing, and every stub ties back to the same net, so nothing is claimed that is not
        // already true of the circuit
        railLabel = GROUND_LABEL;
      }
      if (railLabel != null) {
        // rails are never routed: a lone stub on each pin says the same thing without dragging the
        // busiest net in the circuit across the drawing
        for (Pin pin : pins) {
          wiring.getStubs().add(createRailStub(railLabel, pin.location));
        }
        continue;
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
        OrthogonalLine wire = createWire(a, b, bodies, placed);
        wiring.getWires().add(wire);
        placed.addAll(segmentsOf(wire));
      }
    }
    return wiring;
  }

  /** Creates the ground or supply symbol that stands in for one pin's connection to a rail. */
  private static IDIYComponent<?> createRailStub(String label, Point2D location) {
    IDIYComponent<?> stub;
    if (GROUND_LABEL.equals(label)) {
      stub = new GroundSymbol();
    } else {
      CommonNode node = new CommonNode();
      node.setValue(label);
      stub = node;
    }
    stub.setId(UUID.randomUUID());
    stub.setControlPoint(copyOf(location), 0);
    return stub;
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
  private OrthogonalLine createWire(Pin a, Pin b, Map<UUID, Rectangle2D> bodies,
      List<Line2D> placed) {
    List<Rectangle2D> obstacles = new ArrayList<Rectangle2D>();
    for (Map.Entry<UUID, Rectangle2D> entry : bodies.entrySet()) {
      // the wire starts and ends on these two symbols, so their own boxes are not obstacles
      if (!entry.getKey().equals(a.symbol.getId()) && !entry.getKey().equals(b.symbol.getId())) {
        obstacles.add(entry.getValue());
      }
    }
    WireRouter.Route route = router.route(a.location, exitDirection(a.symbol, a.location),
        b.location, exitDirection(b.symbol, b.location), obstacles, placed);

    OrthogonalLine wire = new OrthogonalLine();
    wire.setId(UUID.randomUUID());
    wire.setStartDirection(route.getStartDirection());
    wire.setControlPoint(copyOf(a.location), 0);
    wire.setControlPoint(route.getBendPoint(), 1);
    wire.setControlPoint(copyOf(b.location), 2);
    return wire;
  }

  /** @return the boxes spanned by each symbol's control points, keyed by symbol id. */
  private static Map<UUID, Rectangle2D> symbolBodies(
      Map<UUID, List<SymbolEntry>> entriesByPhysicalId) {
    Map<UUID, Rectangle2D> bodies = new LinkedHashMap<UUID, Rectangle2D>();
    for (List<SymbolEntry> entries : entriesByPhysicalId.values()) {
      for (SymbolEntry entry : entries) {
        Rectangle2D bounds = boundsOf(entry.symbol);
        if (bounds != null) {
          bodies.put(entry.symbol.getId(), bounds);
        }
      }
    }
    return bodies;
  }

  private static Rectangle2D boundsOf(IDIYComponent<?> symbol) {
    int count = symbol.getControlPointCount();
    if (count == 0) {
      return null;
    }
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (int i = 0; i < count; i++) {
      Point2D p = symbol.getControlPoint(i);
      minX = Math.min(minX, p.getX());
      minY = Math.min(minY, p.getY());
      maxX = Math.max(maxX, p.getX());
      maxY = Math.max(maxY, p.getY());
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  private static List<Line2D> segmentsOf(OrthogonalLine wire) {
    List<Point2D> vertices = WireRouter.vertices(wire.getControlPoint(0), wire.getControlPoint(1),
        wire.getControlPoint(2), wire.getStartDirection());
    List<Line2D> segments = new ArrayList<Line2D>();
    for (int i = 1; i < vertices.size(); i++) {
      segments.add(new Line2D.Double(vertices.get(i - 1), vertices.get(i)));
    }
    return segments;
  }

  /**
   * Works out which way a wire should leave a pin.
   *
   * <p>
   * A two terminal symbol has an axis of its own and its leads run along it, so a wire has to carry
   * on in that direction or it will bend the moment it leaves the lead. Anything else - a box, a
   * transistor - has its pins pointing away from the body, which the centroid finds well enough.
   * </p>
   */
  static OrientationHV exitDirection(IDIYComponent<?> symbol, Point2D pinLocation) {
    int n = symbol.getControlPointCount();
    if (n == 0) {
      return OrientationHV.HORIZONTAL;
    }
    if (symbol instanceof AbstractLeadedComponent && n >= 2) {
      Point2D first = symbol.getControlPoint(0);
      Point2D second = symbol.getControlPoint(1);
      return Math.abs(second.getX() - first.getX()) >= Math.abs(second.getY() - first.getY())
          ? OrientationHV.HORIZONTAL
          : OrientationHV.VERTICAL;
    }
    double cx = 0;
    double cy = 0;
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
      view.setWidth(new Size(maxX, SizeUnit.px));
    }
    if (maxY > view.getHeight().convertToPixels()) {
      view.setHeight(new Size(maxY, SizeUnit.px));
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

  @SuppressWarnings("unchecked")
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
