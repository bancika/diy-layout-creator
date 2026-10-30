/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.ImmutableGraph.Builder;

import org.diylc.common.ComponentType;
import org.diylc.core.ICommonNode;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.lang.LangUtil;
import org.diylc.netlist.ContinuityGraph.ContinuityNode;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.Connection;
import org.diylc.presenter.ContinuityArea;
import org.diylc.utils.RTree;

import com.google.common.graph.Traverser;

public class NetlistBuilder {

  public static final float eps = 4; // consider any nodes closer than this as connected

  public static int MAX_SWITCH_COMBINATIONS = 1024;

  private static final String MAX_SWITCH_COMBINATIONS_ERROR = LangUtil
      .translate("Maximum number of switching combinations exceeded. Allowed: %s, actual: %s");
  
  private static final Logger LOG = Logger.getLogger(NetlistBuilder.class);

  public static List<Netlist> extractNetlists(boolean includeSwitches, Project project,
      List<ContinuityArea> continuityAreas) throws NetlistException {
    return extractNetlists(includeSwitches, project, continuityAreas, List.of());
  }

  /**
   * @param extraNodes points to place in the groups they touch on top of the component terminals,
   *        such as wire ends, so a caller can tell which net a wire is on. They only join existing
   *        connectivity and never connect anything themselves.
   */
  @SuppressWarnings("unchecked")
  public static List<Netlist> extractNetlists(boolean includeSwitches, Project project,
      List<ContinuityArea> continuityAreas, Collection<Node> extraNodes) throws NetlistException {
    Map<Netlist, Netlist> result = new HashMap<Netlist, Netlist>();
    List<Node> nodes = new ArrayList<Node>();

    List<ISwitch> switches = new ArrayList<ISwitch>();

    Set<String> commonNodes = new HashSet<String>();

    for (int j = 0; j < project.getComponents().size(); j++) {
      IDIYComponent<?> c = project.getComponents().get(j);
      ComponentType type = ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());

      // extract nodes
      if (!(c instanceof IContinuity && !(c instanceof ISwitch)) && !(c instanceof ICommonNode)) {
        // regular components, create a node per control point
        for (int i = 0; i < c.getControlPointCount(); i++) {
          // Node renders the name through Node.sanitizeNodeName; here it only marks a named point
          String nodeName = c.getControlPointNodeName(i);
          if (c.isControlPointSticky(i) && nodeName != null
              && (!includeSwitches || !ISwitch.class.isAssignableFrom(type.getInstanceClass()))) {
            nodes.add(new Node(c, i));
          }
        }
      }

      // create only one node per common label to avoid duplications in the netlist, as they will
      // get eventually connected anyways
      if (c instanceof ICommonNode) {
        String label = ((ICommonNode) c).getCommonNodeLabel();
        if (!commonNodes.contains(label)) {
          commonNodes.add(label);
          nodes.add(new Node(c, 0));
        }
      }

      // extract switches
      if (includeSwitches && ISwitch.class.isAssignableFrom(type.getInstanceClass()))
        switches.add((ISwitch) c);
    }

    for (Node extra : extraNodes) {
      if (!nodes.contains(extra)) {
        nodes.add(extra);
      }
    }

    // save us the trouble
    if (nodes.isEmpty())
      return null;

    // if there are no switches, make one with 1 position so we get 1 result back
    if (switches.isEmpty())
      switches.add(new TrivialSwitch());

    // construct all possible combinations
    int[] positions = new int[switches.size()];
    for (int i = 0; i < switches.size(); i++)
      positions[i] = 0;

    int i = switches.size() - 1;

    int totalSwitchCombinations =
        switches.stream().map(sw -> sw.getPositionCount()).reduce(1, (a, b) -> a * b);
    
    int maxSwitch = MAX_SWITCH_COMBINATIONS;
    String maxSwitchStr = System.getProperty("org.diylc.maxSwitchCombinations");
    if (maxSwitchStr != null) {
      try {
        maxSwitch = Integer.parseInt(maxSwitchStr);
      } catch (Exception e) { 
        LOG.error("Could not apply property org.diylc.maxSwitchCombinations", e);
      }
    }

    if (totalSwitchCombinations > maxSwitch) {
      throw new NetlistException(String.format(MAX_SWITCH_COMBINATIONS_ERROR,
          MAX_SWITCH_COMBINATIONS, totalSwitchCombinations));
    }

    while (i >= 0) {
      // process the current combination
      Map<ISwitch, Integer> switchPositions = new HashMap<ISwitch, Integer>();
      List<Position> posList = new ArrayList<Position>();
      for (int j = 0; j < positions.length; j++) {
        switchPositions.put(switches.get(j), positions[j]);
        posList.add(new Position(switches.get(j), positions[j]));
      }
      List<Connection> connections = getConnections(includeSwitches, project, switchPositions);

      Netlist netlist =
          NetlistBuilder.buildNetlist(project.getComponents(), nodes, continuityAreas, connections);

      // merge graphs that are effectively the same
      if (result.containsKey(netlist)) {
        result.get(netlist).getSwitchSetup().add(new SwitchSetup(posList));
      } else {
        netlist.getSwitchSetup().add(new SwitchSetup(posList));
        result.put(netlist, netlist);
      }

      // find the next combination if possible
      if (positions[i] < switches.get(i).getPositionCount() - 1) {
        positions[i]++;
      } else {
        while (i >= 0 && positions[i] == switches.get(i).getPositionCount() - 1)
          i--;
        if (i >= 0) {
          positions[i]++;
          for (int j = i + 1; j < positions.length; j++)
            positions[j] = 0;
          i = switches.size() - 1;
        }
      }
    }

    // sort everything alphabetically
    List<Netlist> netlists = new ArrayList<Netlist>(result.keySet());
    Collections.sort(netlists);
    netlists.forEach(netlist -> cleanupRedundantSwitches(netlist.getSwitchSetup()));

    return netlists;
  }

  public static void cleanupRedundantSwitches(List<SwitchSetup> switchSetups) {
    if (switchSetups.isEmpty()) {
      return;
    }

    // An empty setup means every switch position is allowed.
    if (switchSetups.stream().anyMatch(s -> s.getPositions().isEmpty())) {
      switchSetups.clear();
      return;
    }

    Set<ISwitch> switches = switchSetups.stream()
            .flatMap(setup -> setup.getPositions().stream())
            .map(Position::getSwitch)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    // Restore concrete states from any previously simplified setups.
    // A missing switch means all positions of that switch are allowed.
    Set<Set<Position>> concreteStates = new HashSet<>();

    for (SwitchSetup setup : switchSetups) {
      Set<Set<Position>> expanded = new HashSet<>();
      expanded.add(new HashSet<>(setup.getPositions()));

      for (ISwitch theSwitch : switches) {
        Set<Set<Position>> next = new HashSet<>();

        for (Set<Position> state : expanded) {
          boolean present = state.stream()
                  .anyMatch(p -> p.getSwitch().equals(theSwitch));

          if (present) {
            next.add(state);
          } else {
            for (int position = 0;
                 position < theSwitch.getPositionCount(); position++) {
              Set<Position> copy = new HashSet<>(state);
              copy.add(new Position(theSwitch, position));
              next.add(copy);
            }
          }
        }

        expanded = next;
      }

      concreteStates.addAll(expanded);
    }

    // Find maximally simplified descriptions. Keep all overlapping
    // combinations at each level, rather than committing to one merge.
    Set<Set<Position>> current = new HashSet<>(concreteStates);
    Set<Set<Position>> primeSetups = new HashSet<>();

    while (!current.isEmpty()) {
      Set<Set<Position>> combined = new HashSet<>();
      Set<Set<Position>> next = new HashSet<>();

      for (ISwitch theSwitch : switches) {
        Map<Set<Position>, List<Set<Position>>> groups = new HashMap<>();

        for (Set<Position> setup : current) {
          boolean present = setup.stream()
                  .anyMatch(p -> p.getSwitch().equals(theSwitch));

          if (!present) {
            continue;
          }

          Set<Position> otherPositions = setup.stream()
                  .filter(p -> !p.getSwitch().equals(theSwitch))
                  .collect(Collectors.toSet());

          groups.computeIfAbsent(otherPositions, key -> new ArrayList<>())
                  .add(setup);
        }

        for (Map.Entry<Set<Position>, List<Set<Position>>> entry
                : groups.entrySet()) {
          if (entry.getValue().size() == theSwitch.getPositionCount()) {
            next.add(entry.getKey());
            combined.addAll(entry.getValue());
          }
        }
      }

      for (Set<Position> setup : current) {
        if (!combined.contains(setup)) {
          primeSetups.add(setup);
        }
      }

      current = next;
    }

    // A fully unrestricted description covers every configuration.
    if (primeSetups.stream().anyMatch(Set::isEmpty)) {
      switchSetups.clear();
      return;
    }

    List<SwitchSetup> result = primeSetups.stream()
            .map(setup -> new SwitchSetup(new ArrayList<>(setup)))
            .collect(Collectors.toCollection(ArrayList::new));

    // Prefer removing longer descriptions first; use text as a stable
    // tie-breaker so HashSet iteration order does not affect the result.
    result.sort(Comparator
            .comparingInt((SwitchSetup s) -> s.getPositions().size())
            .reversed()
            .thenComparing(SwitchSetup::toString));

    // Remove a description only if the remaining descriptions still
    // cover every concrete state it represents.
    for (int i = 0; i < result.size();) {
      SwitchSetup candidate = result.get(i);

      boolean redundant = concreteStates.stream()
              .filter(state -> state.containsAll(candidate.getPositions()))
              .allMatch(state -> result.stream()
                      .anyMatch(other -> other != candidate
                              && state.containsAll(other.getPositions())));

      if (redundant) {
        result.remove(i);
      } else {
        i++;
      }
    }

    result.sort(Comparator.comparing(SwitchSetup::toString));
    switchSetups.clear();
    switchSetups.addAll(result);
  }

  public static Netlist buildNetlist(List<IDIYComponent<?>> components, Collection<Node> nodes,
      Collection<ContinuityArea> continuityAreas, Collection<Connection> connections) {
    ImmutableGraph<ProjectGraphNode> graph = buildGraph(nodes, continuityAreas, connections);

    Traverser<ProjectGraphNode> traverser = Traverser.forGraph(graph);
    Set<ProjectGraphNode> visited = new HashSet<ProjectGraphNode>();

    ProjectGraphNode[] nodeArr = graph.nodes().toArray(new ProjectGraphNode[0]);

    Netlist netlist = new Netlist(components);

    while (true) {

      int i = 0;
      while (i < nodeArr.length && visited.contains(nodeArr[i]))
        i++;

      if (i == graph.nodes().size())
        break;

      // build a group of connected nodes
      Group g = new Group();
      traverser.breadthFirst(nodeArr[i]).forEach(node -> {
        visited.add(node);
        if (node.getNode() != null) {
          g.getNodes().add(node.getNode());
        }
      });
      if (g.getNodes().size() > 1) {
        netlist.add(g);
      }
    }

    return netlist;
  }

  public static ContinuityGraph buildContinuityGraph(Collection<ContinuityArea> continuityAreas,
      Collection<Connection> connections) {
    ImmutableGraph<ProjectGraphNode> graph =
        buildGraph(new ArrayList<Node>(), continuityAreas, connections);

    Traverser<ProjectGraphNode> traverser = Traverser.forGraph(graph);
    Set<ProjectGraphNode> visited = new HashSet<ProjectGraphNode>();

    ProjectGraphNode[] nodeArr = graph.nodes().toArray(new ProjectGraphNode[0]);

    int groupId = 1;

    RTree<ContinuityNode> groupTree = new RTree<ContinuityGraph.ContinuityNode>();
    Map<Integer, List<ContinuityArea>> areaGroupMap = new HashMap<Integer, List<ContinuityArea>>();

    while (true) {

      int i = 0;
      while (i < nodeArr.length && visited.contains(nodeArr[i]))
        i++;

      if (i == graph.nodes().size())
        break;

      final int finalGroupId = groupId;

      List<ContinuityArea> areaList = new ArrayList<ContinuityArea>();
      traverser.breadthFirst(nodeArr[i]).forEach(node -> {
        visited.add(node);
        if (node.getArea() != null) {
          areaList.add(node.getArea());
          groupTree.insert(node.getArea().getArea(),
              new ContinuityNode(node.getArea(), finalGroupId));
        }
      });

      if (!areaList.isEmpty()) {
        areaGroupMap.put(groupId, areaList);
        groupId++;
      }
    }

    return new ContinuityGraph(groupTree, areaGroupMap);
  }

  /****
   * Build an {@link ImmutableGraph} containing the following nodes: project nodes, both ends of
   * each connection and one node per continuity area. All vertices are added based on object
   * proximity or intersection.
   * 
   * @param nodes
   * @param continuityAreas
   * @param connections
   * @return
   */
  private static ImmutableGraph<ProjectGraphNode> buildGraph(Collection<Node> nodes,
      Collection<ContinuityArea> continuityAreas, Collection<Connection> connections) {

    Map<Point2D, List<ProjectGraphNode>> nodeMapByPoint =
        new HashMap<Point2D, List<ProjectGraphNode>>();

    RTree<ProjectGraphNode> areaTree = new RTree<ProjectGraphNode>();

    Builder<ProjectGraphNode> builder = GraphBuilder.undirected().<ProjectGraphNode>immutable();

    buildPointNodes(nodes, nodeMapByPoint, builder);

    buildConnectionNodes(connections, nodeMapByPoint, builder);

    joinPointsWithConnectionNodes(nodeMapByPoint, builder);

    buildAndConnectAreaNodes(continuityAreas, nodeMapByPoint, areaTree, builder);

    return builder.build();
  }

  private static void buildAndConnectAreaNodes(Collection<ContinuityArea> continuityAreas,
      Map<Point2D, List<ProjectGraphNode>> nodeMapByPoint, RTree<ProjectGraphNode> areaTree,
      Builder<ProjectGraphNode> builder) {
    for (ContinuityArea area : continuityAreas) {
      ProjectGraphNode areaNode = new ProjectGraphNode(area);
      builder.addNode(areaNode);

      areaTree.search(area.getArea()).stream().filter(graphNode -> {
        if (graphNode.getArea() != null && graphNode.getArea() != area
            && (graphNode.getArea().getLayerId() == 0 || area.getLayerId() == 0 || graphNode.getArea().getLayerId() == area.getLayerId())) {
          Area a = new Area(graphNode.getArea().getSimplifiedArea());
          a.intersect(area.getSimplifiedArea());
          return !a.isEmpty();
        }
        return false;
      }).forEach(graphNode -> {
        builder.putEdge(graphNode, areaNode);
      });

      areaTree.insert(area.getArea(), areaNode);
    }

    for (Map.Entry<Point2D, List<ProjectGraphNode>> entry : nodeMapByPoint.entrySet()) {      
      final Point2D point = entry.getKey();
      
      List<ProjectGraphNode> candidateAreaNodes = areaTree.search(point, eps).stream()
          .filter(graphNode -> graphNode.getArea().getArea().contains(point)
              || graphNode.getArea().getArea().intersects(point.getX() - eps / 2,
                  point.getY() - eps / 2, eps, eps))
          .collect(Collectors.toList());
      
      if (candidateAreaNodes.isEmpty())
        continue;
      
      for (ProjectGraphNode pointNode : entry.getValue()) {
        for (ProjectGraphNode areaNode : candidateAreaNodes) {
          builder.putEdge(pointNode, areaNode);
        }
      }
    }
  }

  private static void joinPointsWithConnectionNodes(
      Map<Point2D, List<ProjectGraphNode>> nodeMapByPoint, Builder<ProjectGraphNode> builder) {
    for (Map.Entry<Point2D, List<ProjectGraphNode>> entry : nodeMapByPoint.entrySet()) {
      ProjectGraphNode[] nodeArr = entry.getValue().toArray(new ProjectGraphNode[0]);
      for (int i = 0; i < nodeArr.length - 1; i++) {
        for (int j = i + 1; j < nodeArr.length; j++) {
          builder.putEdge(nodeArr[i], nodeArr[j]);
        }
      }

      // snapping alone splits points that straddle a cell boundary, e.g. 522 and 520 land in
      // the cells at 524 and 520, so neighbouring cells are joined by actual distance
      Point2D cell = entry.getKey();
      for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
          if (dx == 0 && dy == 0) {
            continue;
          }
          List<ProjectGraphNode> neighbours = nodeMapByPoint
              .get(new Point2D.Double(cell.getX() + dx * eps, cell.getY() + dy * eps));
          if (neighbours == null) {
            continue;
          }
          for (ProjectGraphNode node : nodeArr) {
            for (ProjectGraphNode neighbour : neighbours) {
              if (node.getPoint().distance(neighbour.getPoint()) < eps) {
                builder.putEdge(node, neighbour);
              }
            }
          }
        }
      }
    }
  }

  private static void buildConnectionNodes(Collection<Connection> connections,
      Map<Point2D, List<ProjectGraphNode>> nodeMapByPoint, Builder<ProjectGraphNode> builder) {
    for (Connection conn : connections) {
      Point2D p1 = snapToEps(conn.getP1(), eps);
      Point2D p2 = snapToEps(conn.getP2(), eps);

      ProjectGraphNode graphNode1 = new ProjectGraphNode(conn.getP1(), conn.getZIndex());
      ProjectGraphNode graphNode2 = new ProjectGraphNode(conn.getP2(), conn.getZIndex());

      builder.addNode(graphNode1);
      builder.addNode(graphNode2);
      builder.putEdge(graphNode1, graphNode2);

      nodeMapByPoint.computeIfAbsent(p1, (point) -> new ArrayList<ProjectGraphNode>())
          .add(graphNode1);
      nodeMapByPoint.computeIfAbsent(p2, (point) -> new ArrayList<ProjectGraphNode>())
          .add(graphNode2);
    }
  }

  private static void buildPointNodes(Collection<Node> nodes,
      Map<Point2D, List<ProjectGraphNode>> nodeMapByPoint, Builder<ProjectGraphNode> builder) {
    for (Node node : nodes) {
      Point2D p = snapToEps(node.getPoint2D(), eps);
      ProjectGraphNode graphNode = new ProjectGraphNode(node.getPoint2D(), node);
      builder.addNode(graphNode);
      nodeMapByPoint.computeIfAbsent(p, (point) -> new ArrayList<ProjectGraphNode>())
          .add(new ProjectGraphNode(node.getPoint2D(), node));
    }
  }

  @SuppressWarnings({"unchecked", "unlikely-arg-type"})
  private static List<Connection> getConnections(boolean includeSwitches, Project project,
      Map<ISwitch, Integer> switchPositions) {
    Set<Connection> connections = new TreeSet<Connection>();

    Map<String, List<IDIYComponent<?>>> commonNodeMap =
        new HashMap<String, List<IDIYComponent<?>>>();

    for (int z = 0; z < project.getComponents().size(); z++) {
      IDIYComponent<?> c = project.getComponents().get(z);
      ComponentType type = ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      // handle direct connections, and switches in their selected position when switch positions
      // are not being enumerated
      if (c instanceof IContinuity && (!(c instanceof ISwitch) || !includeSwitches)) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (((IContinuity) c).arePointsConnected(i, j))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j), z));
      }
      // Pins of one component that name the same rail are the same node: a board's eight ground
      // pins land on one plane, and grounding one of them must not leave the rest floating.
      // Switches are excluded because their connectivity depends on the position, which a name
      // cannot express -- a switch states it through ISwitch.arePointsConnected instead.
      if (!(c instanceof ISwitch)) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++) {
          String rail = Node.railName(c.getControlPointNodeName(i));
          if (rail == null) {
            continue;
          }
          for (int j = i + 1; j < c.getControlPointCount(); j++) {
            if (rail.equals(Node.railName(c.getControlPointNodeName(j)))) {
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j), z));
            }
          }
        }
      }
      // handle switches
      if (ISwitch.class.isAssignableFrom(type.getInstanceClass())
          && switchPositions.containsKey(c)) {
        int position = switchPositions.get(c);
        ISwitch s = (ISwitch) c;
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (s.arePointsConnected(i, j, position))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j), z));
      }
      // handle common nodes
      if (ICommonNode.class.isAssignableFrom(type.getInstanceClass())) {
        ICommonNode commonNode = (ICommonNode) c;
        String label = commonNode.getCommonNodeLabel();
        commonNodeMap.computeIfAbsent(label, (key) -> new ArrayList<IDIYComponent<?>>()).add(c);
      }
    }

    // connect common nodes together by chaining them one by one
    for (List<IDIYComponent<?>> commonPoints : commonNodeMap.values()) {
      IDIYComponent<?>[] commonPointArr = commonPoints.toArray(new IDIYComponent<?>[0]);
      for (int i = 0; i < commonPointArr.length - 1; i++) {
        connections.add(new Connection(commonPointArr[i].getControlPoint(0),
            commonPointArr[i + 1].getControlPoint(0), 0));
      }
    }

    return new ArrayList<Connection>(connections);
  }

  private static Point2D snapToEps(Point2D point, double eps) {
    return new Point2D.Double(Math.round(point.getX() / eps) * eps,
        Math.round(point.getY() / eps) * eps);
  }

  static class ProjectGraphNode {
    private Point2D point;
    private Node node;
    private ContinuityArea area;

    public ProjectGraphNode(ContinuityArea area) {
      super();
      this.area = area;
    }

    public ProjectGraphNode(Point2D point, int zIndex) {
      super();
      this.point = point;
      this.node = null;
    }

    public ProjectGraphNode(Point2D point, Node node) {
      super();
      this.point = point;
      this.node = node;
    }

    public Point2D getPoint() {
      return point;
    }

    public Node getNode() {
      return node;
    }

    public ContinuityArea getArea() {
      return area;
    }
  }


  private static class TrivialSwitch implements ISwitch {

    @Override
    public String getPositionName(int position) {
      return "Default";
    }

    @Override
    public int getPositionCount() {
      return 1;
    }

    @Override
    public boolean arePointsConnected(int index1, int index2, int position) {
      return false;
    }

    @Override
    public void setSelectedPosition(Integer selectedPosition) {
      // a placeholder switch has a single position that is always current
    }
  }
}
