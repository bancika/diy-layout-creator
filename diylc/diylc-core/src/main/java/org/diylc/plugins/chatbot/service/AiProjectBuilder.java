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
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistBuilder;
import org.diylc.netlist.NetlistException;
import org.diylc.netlist.Node;
import org.diylc.plugins.chatbot.model.*;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.ContinuityArea;

public class AiProjectBuilder {

  public static AiProject build(Project project, List<ContinuityArea> continuityAreas) {

    Map<String, String> metadata = getMetadata(project);
    List<String> tags = getTags(project);

    double gridSpacingPx = project.getGridSpacing().convertToPixels();
    List<AiComponent> components = project.getComponents().stream().map(c -> mapComponent(c, gridSpacingPx)).toList();

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
              .map(AiProjectBuilder::mapTerminal)
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
            .map(AiProjectBuilder::mapSwitch).toList();

    return new AiProject(metadata, tags, components, nets, switches);
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
  private static final Set<String> PROPERTY_NAMES_TO_SKIP = Set.of("alpha");

  static AiSwitch mapSwitch(ISwitch sw) {
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
    return new AiSwitch(c.getName(), positions);
  }

  static AiComponent mapComponent(IDIYComponent<?> component, double gridSpacingPx) {

    List<AiTerminal> terminals = new ArrayList<>();

    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (component.isControlPointSticky(i)) {
        Point2D controlPoint = component.getControlPoint(i);
        String nodeName = component.getControlPointNodeName(i);
        AiTerminal terminal = new AiTerminal(i, Integer.toString(i+1).equals(nodeName) ? null : nodeName,
            createAiPoint(controlPoint, gridSpacingPx));
        terminals.add(terminal);
      }
    }

    String compType = component.getClass().getCanonicalName().replace("org.diylc.components.", "");
    return new AiComponent(component.getName(), compType,
        component.getValue() == null ? null : component.getValue().toString(),
        terminals.isEmpty() ? null : terminals);
  }

  private static AiPoint createAiPoint(Point2D controlPoint, double gridSpacingPx) {

    BigDecimal gridX = BigDecimal.valueOf(controlPoint.getX() / gridSpacingPx).setScale(2, RoundingMode.HALF_UP);
    BigDecimal gridY = BigDecimal.valueOf(controlPoint.getY() / gridSpacingPx).setScale(2, RoundingMode.HALF_UP);
    return new AiPoint(gridX, gridY);
  }

  static String mapTerminal(Node terminalRef) {
    return terminalRef.getComponent().getName() + "." + terminalRef.getPointIndex();
  }
}
