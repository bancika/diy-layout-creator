package org.diylc.plugins.chatbot.service;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.plugins.chatbot.model.*;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.ContinuityArea;

public class AiProjectBuilder {

  public static AiProject build(Project project, List<ContinuityArea> continuityAreas) {

    Map<String, String> metadata = getMetadata(project);
    List<String> tags = getTags(project);

    List<AiComponent> components = project.getComponents().stream().map(AiProjectBuilder::mapComponent).toList();

    List<Set<NetlistBuilder.TerminalRef>> sets =
        NetlistBuilder.buildNets(project.getComponents(), continuityAreas);

    AtomicInteger counter = new AtomicInteger();
    Map<String, Set<String>> nets =
        sets.stream().map(s -> s.stream()
                .map(AiProjectBuilder::mapTerminal)
                .collect(Collectors.toSet()))
            .collect(Collectors.toMap(x -> "N" + counter.incrementAndGet(), x -> x));

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
    if (project.getComponents().stream().anyMatch(x -> x.getClass().getCanonicalName().toLowerCase().contains("guitar"))) {
      tags.add("guitar");
    }
    if (project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains("symbol"))) {
      tags.add("schematic");
    }
    if (project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains("board")) &&
        project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains("trace")) &&
        project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains("pad"))) {
      tags.add("PCB");
    }
    if (project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains("tube"))) {
      tags.add("tube");
    }
    if (project.getComponents().stream().anyMatch(x -> x.getClass().getName().toLowerCase().contains("vero"))) {
      tags.add("vero/strip");
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

  static AiComponent mapComponent(IDIYComponent<?> component) {
    ComponentType componentType = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
    List<PropertyWrapper> properties =
        ComponentProcessor.getInstance().extractProperties(component.getClass());

    AiPoint pos = null;
    AiPoint fromPos = null;
    AiPoint toPos = null;
//    Map<String, Object> componentDescriptorMap = new HashMap<>();
//    if (!component.isControlPointSticky(0)) {
//      Point2D controlPoint1 = component.getControlPoint(0);
//      if (component.getControlPointCount() > 1 && !component.isControlPointSticky(component.getControlPointCount() - 1)) {
//        Point2D controlPoint2 = component.getControlPoint(component.getControlPointCount() - 1);
//        fromPos = new AiPoint((int)Math.round(controlPoint1.getX()), (int)Math.round(controlPoint1.getY()));
//        toPos = new AiPoint((int)Math.round(controlPoint2.getX()), (int)Math.round(controlPoint2.getY()));
//      } else {
//        pos = new AiPoint((int)Math.round(controlPoint1.getX()), (int)Math.round(controlPoint1.getY()));
//      }
//    }
//
//    if (component.getValue() != null && !PROPERTY_TYPES_TO_SKIP.contains(component.getValue().getClass())) {
//      componentDescriptorMap.put("value", component.getValue().toString());
//    }

//    properties.forEach(p -> {
//      if (PROPERTY_TYPES_TO_SKIP.contains(p.getType()))
//        return;
//      if (PROPERTY_NAMES_TO_SKIP.contains(p.getName().toLowerCase()))
//        return;
//
//      try {
//        p.readFrom(component);
//        if (p.getValue() == null)
//          return;
//
//        componentDescriptorMap.put(p.getName(), p.getValue().toString());
//      } catch (Exception e) {
//        LOG.warn("Error extracting properties", e);
//      }
//    });

    List<AiTerminal> terminals = new ArrayList<>();

    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (component.isControlPointSticky(i)) {
        Point2D controlPoint = component.getControlPoint(i);
        String nodeName = component.getControlPointNodeName(i);
        AiTerminal terminal = new AiTerminal(i, Integer.toString(i+1).equals(nodeName) ? null : nodeName,
            new AiPoint((int)Math.round(controlPoint.getX()),
                (int)Math.round(controlPoint.getY())));
        terminals.add(terminal);
      }
    }

    return new AiComponent(component.getName(), componentType.getName(),
        component.getValue() == null ? null : component.getValue().toString(),
         fromPos, toPos, pos, terminals.isEmpty() ? null : terminals);
  }

  static String mapTerminal(NetlistBuilder.TerminalRef terminalRef) {
    return terminalRef.component.getName() + "." + terminalRef.terminalIndex;
  }
}
