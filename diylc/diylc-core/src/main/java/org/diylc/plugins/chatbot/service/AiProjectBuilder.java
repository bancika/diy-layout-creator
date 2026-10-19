package org.diylc.plugins.chatbot.service;

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistBuilder;
import org.diylc.netlist.NetlistException;
import org.diylc.netlist.Node;
import org.diylc.plugins.chatbot.model.*;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.Connection;
import org.diylc.presenter.ContinuityArea;

public class AiProjectBuilder {

  public static final String ID_SEPARATOR = "#";
  private static final int MIN_ID_SUFFIX_LENGTH = 4;

  public static AiProject build(Project project, List<ContinuityArea> continuityAreas) {

    Map<String, String> metadata = getMetadata(project);
    List<String> tags = getTags(project);

    Map<IDIYComponent<?>, String> ids = assignIds(project.getComponents());
    Map<Node, List<String>> contacts = getConnectivityContacts(project, continuityAreas, ids);

    double gridSpacingPx = project.getGridSpacing().convertToPixels();
    List<AiComponent> components = project.getComponents().stream()
        .map(c -> mapComponent(c, ids.get(c), gridSpacingPx, contacts))
        .toList();

//    List<Set<Node>> sets =
    List<Netlist> netlists = null;
    try {
      netlists = NetlistBuilder.extractNetlists(false, project, continuityAreas);
    } catch (NetlistException e) {
      throw new RuntimeException(e);
    }
    Map<String, Set<String>> nets;
    if (netlists == null || netlists.isEmpty()) {
      nets = new HashMap<>();
    } else {
      AtomicInteger counter = new AtomicInteger();
      nets = netlists.get(0).getGroups().stream().map(s -> s.getNodes().stream()
              .map(node -> mapTerminal(node, ids))
              .collect(Collectors.toSet()))
          .collect(Collectors.toMap(x -> "N" + String.format("%03d", counter.incrementAndGet()),
              Function.identity()));
    }

//    Map<String, Set<String>> nets =
//        sets.stream().map(s -> s.stream()
//                .map(AiProjectBuilder::mapTerminal)
//                .collect(Collectors.toSet()))
//            .collect(Collectors.toMap(x -> "N" + String.format("%03d", counter.incrementAndGet()),
//                Function.identity()));

    List<AiSwitch> switches =
        project.getComponents().stream()
            .filter(x -> x instanceof ISwitch)
            .map(x -> (ISwitch) x)
            .map(sw -> mapSwitch(sw, ids)).toList();

    return new AiProject(metadata, tags, components, nets, switches);
  }

  /**
   * The ids the AI refers to components by. Usually that is the name, but DIYLC lets several
   * components share a name (copied wires often do) and an edit script needs to tell them apart,
   * so a repeated name gets the start of the component's UUID appended: W3#a1b2. The ids depend
   * only on names and UUIDs, so the edit script editor derives the same ones when it applies a
   * script.
   *
   * @return id for each component, keyed by identity because components with equal properties are
   *         still different components
   */
  public static Map<IDIYComponent<?>, String> assignIds(List<IDIYComponent<?>> components) {
    Map<String, List<IDIYComponent<?>>> byName = new HashMap<>();
    for (IDIYComponent<?> c : components) {
      String name = c.getName() == null ? "" : c.getName();
      byName.computeIfAbsent(name, key -> new ArrayList<>()).add(c);
    }

    Map<IDIYComponent<?>, String> ids = new IdentityHashMap<>();
    for (Map.Entry<String, List<IDIYComponent<?>>> entry : byName.entrySet()) {
      List<IDIYComponent<?>> sameName = entry.getValue();
      if (sameName.size() == 1) {
        ids.put(sameName.get(0), entry.getKey());
        continue;
      }
      // lengthen the suffix in the rare case that two UUIDs start the same
      int length = MIN_ID_SUFFIX_LENGTH;
      while (length < 32 && !idSuffixesDistinct(sameName, length)) {
        length++;
      }
      for (IDIYComponent<?> c : sameName) {
        ids.put(c, entry.getKey() + ID_SEPARATOR + idSuffix(c, length));
      }
    }
    return ids;
  }

  private static boolean idSuffixesDistinct(List<IDIYComponent<?>> components, int length) {
    Set<String> suffixes = new HashSet<>();
    for (IDIYComponent<?> c : components) {
      if (!suffixes.add(idSuffix(c, length))) {
        return false;
      }
    }
    return true;
  }

  private static String idSuffix(IDIYComponent<?> component, int length) {
    return component.getId().toString().replace("-", "").substring(0, length);
  }

  private static Map<String, String> getMetadata(Project project) {
    Map<String, String> metadata = new HashMap<>();
    if (project.getTitle() != null) metadata.put("title", project.getTitle());
    if (project.getAuthor() != null) metadata.put("author", project.getAuthor());
    if (project.getDescription() != null) metadata.put("description", project.getDescription());
    metadata.put("gridSpacing", project.getGridSpacing().toString());
    metadata.put("width", project.getWidth().toString());
    metadata.put("height", project.getHeight().toString());
    return metadata;
  }

  private static List<String> getTags(Project project) {

    List<String> tags = new ArrayList<>();
    
    // Helper: checks if any component's class name (lowercase) contains the given substring
    java.util.function.Predicate<String> hasClass = sub ->
        project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains(sub));
    
    boolean isGuitar = project.getComponents().stream().anyMatch(x -> {
      if (x.getClass().getName().toLowerCase().contains("guitar")) return true;
      try {
        ComponentType ct = ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) x.getClass());
        return ct != null && "Guitar".equalsIgnoreCase(ct.getCategory());
      } catch (Exception e) {
        return false;
      }
    });
    if (isGuitar) {
      tags.add("guitar");
    }
    if (hasClass.test("symbol")) {
      tags.add("schematic");
    }
    if (hasClass.test("board") && hasClass.test("trace") && hasClass.test("pad")) {
      tags.add("pcb");
    }
    if (hasClass.test("tube")) {
      tags.add("tube");
    }
    // Veroboard / stripboard (includes EurorackStripboard, VeroBoard, TriPadBoard)
    if (hasClass.test("vero") || hasClass.test("tripad") || hasClass.test("eurorackstrip")) {
      tags.add("veroboard");
    }
    // Perfboard / protoboard (PerfBoard, MarshallPerfBoard, ProtoBoard)
    if (hasClass.test("perfboard") || hasClass.test("marshallperf") || hasClass.test("protoboard")) {
      tags.add("perfboard");
    }
    // Turret / eyelet board
    if (hasClass.test("eyeletboard") || hasClass.test("turret") || hasClass.test("eyelet")) {
      tags.add("turret");
    }
    // Tag strip / terminal strip
    if (hasClass.test("tagstrip") || hasClass.test("terminalstrip")) {
      tags.add("tagboard");
    }
    // Breadboard
    if (hasClass.test("breadboard")) {
      tags.add("breadboard");
    }
    // Point-to-point: has hookup wire but no board at all
    if (hasClass.test("hookupwire") && !hasClass.test("board") && !hasClass.test("breadboard")
        && !hasClass.test("tagstrip") && !hasClass.test("terminalstrip")) {
      tags.add("point-to-point");
    }
    return tags;
  }

  private static final Set<Class<?>> PROPERTY_TYPES_TO_SKIP = Set.of(Font.class, Color.class);
  // name and value are exported as the component's id and value already
  private static final Set<String> PROPERTY_NAMES_TO_SKIP = Set.of("alpha", "name", "value");

  /**
   * What each terminal of a connectivity component (wire, jumper, trace, pad...) lands on: the
   * terminals of other components at the same spot or on the same copper, such as a strip, a lug
   * or the trace itself. Unlike the nets, this does not follow other wires or switches, so it tells
   * which wires are attached to a given component, which an edit needs to rewire it.
   */
  static Map<Node, List<String>> getConnectivityContacts(Project project,
      List<ContinuityArea> continuityAreas, Map<IDIYComponent<?>, String> ids) {
    List<Node> nodes = new ArrayList<>();
    for (IDIYComponent<?> c : project.getComponents()) {
      for (int i = 0; i < c.getControlPointCount(); i++) {
        if (c.isControlPointSticky(i)) {
          nodes.add(new Node(c, i));
        }
      }
    }

    Map<Node, List<String>> contacts = new HashMap<>();
    if (nodes.isEmpty()) {
      return contacts;
    }

    // without connections, points only join where they touch or share copper
    Netlist touching = NetlistBuilder.buildNetlist(project.getComponents(), nodes,
        continuityAreas, new ArrayList<Connection>());
    for (Group group : touching.getGroups()) {
      for (Node node : group.getNodes()) {
        if (!isConnectivity(node.getComponent())) {
          continue;
        }
        contacts.put(node, group.getNodes().stream()
            .filter(other -> other.getComponent() != node.getComponent())
            .map(other -> mapTerminal(other, ids))
            .sorted()
            .toList());
      }
    }
    return contacts;
  }

  @SuppressWarnings("unchecked")
  private static boolean isConnectivity(IDIYComponent<?> component) {
    ComponentType type = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
    return type != null && "Connectivity".equalsIgnoreCase(type.getCategory());
  }

  static AiSwitch mapSwitch(ISwitch sw, Map<IDIYComponent<?>, String> ids) {
    List<AiSwitchPosition> positions = new ArrayList<>();
    IDIYComponent<?> c = (IDIYComponent<?>) sw;
    for (int p = 0; p < sw.getPositionCount(); p++) {
      List<Set<Integer>> connections = new ArrayList<>();
      for (int i = 0; i < c.getControlPointCount() - 1; i++) {
        for (int j = i + 1; j < c.getControlPointCount(); j++) {
          if (sw.arePointsConnected(i, j, p)) {
            connections.add(Set.of(i, j));
          }
        }
      }
      positions.add(new AiSwitchPosition(sw.getPositionName(p), connections));
    }
    return new AiSwitch(ids.get(c), positions);
  }

  static AiComponent mapComponent(IDIYComponent<?> component, String id, double gridSpacingPx,
      Map<Node, List<String>> contacts) {

    boolean connectivity = isConnectivity(component);
    List<AiTerminal> terminals = new ArrayList<>();

    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (component.isControlPointSticky(i)) {
        Point2D controlPoint = component.getControlPoint(i);
        String nodeName = component.getControlPointNodeName(i);
        // an empty list on a connectivity component tells the AI that the end is not attached
        List<String> connectedTo =
            connectivity ? contacts.getOrDefault(new Node(component, i), List.of()) : null;
        AiTerminal terminal = new AiTerminal(i, Integer.toString(i+1).equals(nodeName) ? null : nodeName,
            createAiPoint(controlPoint, gridSpacingPx), connectedTo);
        terminals.add(terminal);
      }
    }

    String compType = component.getClass().getCanonicalName().replace("org.diylc.components.", "");
    Map<String, String> properties = getProperties(component);
    return new AiComponent(id, compType,
        component.getValue() == null ? null : component.getValue().toString(),
        properties.isEmpty() ? null : properties,
        terminals.isEmpty() ? null : terminals);
  }

  /**
   * Current values of the editable properties, so the AI knows e.g. that a pickup is rotated or a
   * pot is log taper without needing the catalog defaults.
   */
  private static Map<String, String> getProperties(IDIYComponent<?> component) {
    Map<String, String> result = new TreeMap<>();
    for (PropertyWrapper property : ComponentProcessor.getInstance()
        .extractProperties(component.getClass())) {
      if (property.isReadOnly() || PROPERTY_TYPES_TO_SKIP.contains(property.getType())
          || PROPERTY_NAMES_TO_SKIP.contains(property.getName().toLowerCase())) {
        continue;
      }
      try {
        property.readFrom(component);
        String formatted = ComponentCatalogGenerator.formatDefaultValue(property.getValue());
        if (formatted != null) {
          result.put(property.getName(), formatted);
        }
      } catch (Exception e) {
        // a property that cannot be read is left out of the export, the AI can do without it
      }
    }
    return result;
  }

  private static AiPoint createAiPoint(Point2D controlPoint, double gridSpacingPx) {

    BigDecimal gridX = BigDecimal.valueOf(controlPoint.getX() / gridSpacingPx).setScale(2, RoundingMode.HALF_UP);
    BigDecimal gridY = BigDecimal.valueOf(controlPoint.getY() / gridSpacingPx).setScale(2, RoundingMode.HALF_UP);
    return new AiPoint(gridX, gridY);
  }

  static String mapTerminal(Node terminalRef, Map<IDIYComponent<?>, String> ids) {
    return ids.get(terminalRef.getComponent()) + "." + terminalRef.getPointIndex();
  }
}
