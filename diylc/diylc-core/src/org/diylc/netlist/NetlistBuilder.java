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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
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
import org.diylc.utils.RTree;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.ImmutableGraph.Builder;
import com.google.common.graph.Traverser;

public class NetlistBuilder {

  public static final float eps = 4; // consider any nodes closer than this as connected

  private static final int MAX_SWITCH_COMBINATIONS = 64;

  private static final String MAX_SWITCH_COMBINATIONS_ERROR = LangUtil
      .translate("Maximum number of switching combinations exceeded. Allowed: %s, actual: %s");

  @SuppressWarnings("unchecked")
  public static List<Netlist> extractNetlists(boolean includeSwitches, Project project, List<Area> continuityAreas)
      throws NetlistException {
    Map<Netlist, Netlist> result = new HashMap<Netlist, Netlist>();
    List<Node> nodes = new ArrayList<Node>();

    List<ISwitch> switches = new ArrayList<ISwitch>();
    
    Set<String> commonNodes = new HashSet<String>();

    for (IDIYComponent<?> c : project.getComponents()) {
      ComponentType type = ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());

      // extract nodes
      if (!(c instanceof IContinuity) && !(c instanceof ICommonNode)) {
        for (int i = 0; i < c.getControlPointCount(); i++) {
          String nodeName = c.getControlPointNodeName(i);
          if (nodeName != null
              && (!includeSwitches || !ISwitch.class.isAssignableFrom(type.getInstanceClass()))) {
            nodes.add(new Node(c, i));
          }
        }
      }
      
      // create only one node per common label to avoid duplications in the netlist, as they will get eventually connected anyways
      if (c instanceof ICommonNode) {
        String label = ((ICommonNode)c).getCommonNodeLabel();
        if (!commonNodes.contains(label)) {
          commonNodes.add(label);
          nodes.add(new Node(c, 0));
        }
      }

      // extract switches
      if (includeSwitches && ISwitch.class.isAssignableFrom(type.getInstanceClass()))
        switches.add((ISwitch) c);
    }

    // save us the trouble
    if (nodes.isEmpty())
      return null;

    // if there are no switches, make one with 1 position so we get 1 result back
    if (switches.isEmpty())
      switches.add(new ISwitch() {

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
      });

    // construct all possible combinations
    int[] positions = new int[switches.size()];
    for (int i = 0; i < switches.size(); i++)
      positions[i] = 0;

    int i = switches.size() - 1;

    int totalSwitchCombinations =
        switches.stream().map(sw -> sw.getPositionCount()).reduce(1, (a, b) -> a * b);

    if (totalSwitchCombinations > MAX_SWITCH_COMBINATIONS) {
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
      List<Connection> connections = getConnections(project, switchPositions);

      Netlist netlist = NetlistBuilder.buildNetlist(project.getComponents(), nodes,
          continuityAreas, connections);

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

    return netlists;
  }

  public static Netlist buildNetlist(List<IDIYComponent<?>> components, Collection<Node> nodes,
      Collection<Area> continuityAreas, Collection<Connection> connections) {
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

  public static ContinuityGraph buildContinuityGraph(Collection<Area> continuityAreas,
      Collection<Connection> connections) {
    ImmutableGraph<ProjectGraphNode> graph =
        buildGraph(new ArrayList<Node>(), continuityAreas, connections);

    Traverser<ProjectGraphNode> traverser = Traverser.forGraph(graph);
    Set<ProjectGraphNode> visited = new HashSet<ProjectGraphNode>();

    ProjectGraphNode[] nodeArr = graph.nodes().toArray(new ProjectGraphNode[0]);

    int groupId = 1;

    RTree<ContinuityNode> groupTree = new RTree<ContinuityGraph.ContinuityNode>();
    Map<Integer, List<Area>> areaGroupMap = new HashMap<Integer, List<Area>>();

    while (true) {

      int i = 0;
      while (i < nodeArr.length && visited.contains(nodeArr[i]))
        i++;

      if (i == graph.nodes().size())
        break;

      final int finalGroupId = groupId;

      List<Area> areaList = new ArrayList<Area>();
      traverser.breadthFirst(nodeArr[i]).forEach(node -> {
        visited.add(node);
        if (node.getArea() != null) {
          areaList.add(node.getArea());
          groupTree.insert(node.getArea(), new ContinuityNode(node.getArea(), finalGroupId));
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
   * each connection and one node per continuity areas. All vertices are added based on object
   * proximity or intersection.
   * 
   * @param nodes
   * @param continuityAreas
   * @param connections
   * @return
   */
  private static ImmutableGraph<ProjectGraphNode> buildGraph(Collection<Node> nodes,
      Collection<Area> continuityAreas, Collection<Connection> connections) {
    Map<Point2D, List<ProjectGraphNode>> nodeMap = new HashMap<Point2D, List<ProjectGraphNode>>();

    RTree<ProjectGraphNode> nodeTree = new RTree<ProjectGraphNode>();
    RTree<ProjectGraphNode> areaTree = new RTree<ProjectGraphNode>();

    Builder<ProjectGraphNode> builder = GraphBuilder.undirected().<ProjectGraphNode>immutable();

    for (Node node : nodes) {
      Point2D p = snapToEps(node.getPoint2D(), eps);
      ProjectGraphNode graphNode = new ProjectGraphNode(p, node);
      builder.addNode(graphNode);
      nodeTree.insert(new float[] {(float) (p.getX() - eps), (float) (p.getY() - eps)},
          new float[] {eps * 2, eps * 2}, graphNode);
      nodeMap.computeIfAbsent(p, (point) -> new ArrayList<ProjectGraphNode>())
          .add(new ProjectGraphNode(p, node));
    }

    for (Connection conn : connections) {
      Point2D p1 = snapToEps(conn.getP1(), eps);
      Point2D p2 = snapToEps(conn.getP2(), eps);

      ProjectGraphNode graphNode1 = new ProjectGraphNode(p1);
      ProjectGraphNode graphNode2 = new ProjectGraphNode(p2);

      nodeTree.insert(new float[] {(float) p1.getX() - eps, (float) p1.getY() - eps},
          new float[] {eps * 2, eps * 2}, graphNode1);
      nodeTree.insert(new float[] {(float) p2.getX() - eps, (float) p2.getY() - eps},
          new float[] {eps * 2, eps * 2}, graphNode2);

      builder.addNode(graphNode1);
      builder.addNode(graphNode2);
      builder.putEdge(graphNode1, graphNode2);

      nodeMap.computeIfAbsent(p1, (point) -> new ArrayList<ProjectGraphNode>()).add(graphNode1);
      nodeMap.computeIfAbsent(p2, (point) -> new ArrayList<ProjectGraphNode>()).add(graphNode2);
    }

    for (Map.Entry<Point2D, List<ProjectGraphNode>> entry : nodeMap.entrySet()) {
      ProjectGraphNode[] nodeArr = entry.getValue().toArray(new ProjectGraphNode[0]);
      for (int i = 0; i < nodeArr.length - 1; i++) {
        for (int j = i + 1; j < nodeArr.length; j++) {
          builder.putEdge(nodeArr[i], nodeArr[j]);
        }
      }
    }

    for (Area area : continuityAreas) {
      ProjectGraphNode areaNode = new ProjectGraphNode(area);
      builder.addNode(areaNode);

      ProjectGraphNode[] nodeArr = nodeTree.search(area).stream()
          .filter(graphNode -> area.contains(graphNode.getPoint())
              || area.intersects(graphNode.getPoint().getX() - eps / 2,
                  graphNode.getPoint().getY() - eps / 2, eps, eps))
          .collect(Collectors.toList()).toArray(new ProjectGraphNode[0]);

      for (ProjectGraphNode node : nodeArr) {
        builder.putEdge(node, areaNode);
      }

      for (int i = 0; i < nodeArr.length - 1; i++) {
        for (int j = i + 1; j < nodeArr.length; j++) {
          builder.putEdge(nodeArr[i], nodeArr[j]);
        }
      }

      areaTree.search(area).stream().filter(graphNode -> {
        if (graphNode.getArea() != null && graphNode.getArea() != area) {
          Area a = new Area(graphNode.getArea());
          a.intersect(area);
          return !a.isEmpty();
        }
        return false;
      }).forEach(graphNode -> {
        builder.putEdge(graphNode, areaNode);
      });

      areaTree.insert(area, areaNode);
    }

    return builder.build();
  }
  
  @SuppressWarnings({"unchecked", "unlikely-arg-type"})
  private static List<Connection> getConnections(Project project, Map<ISwitch, Integer> switchPositions) {
    Set<Connection> connections = new TreeSet<Connection>();
    
    Map<String, List<IDIYComponent<?>>> commonNodeMap = new HashMap<String, List<IDIYComponent<?>>>();
    
    for (IDIYComponent<?> c : project.getComponents()) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      // handle direct connections
      if (c instanceof IContinuity) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (((IContinuity) c).arePointsConnected(i, j))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
      }
      // handle switches
      if (ISwitch.class.isAssignableFrom(type.getInstanceClass()) && switchPositions.containsKey(c)) {
        int position = switchPositions.get(c);
        ISwitch s = (ISwitch) c;
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (s.arePointsConnected(i, j, position))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
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
        connections.add(new Connection(commonPointArr[i].getControlPoint(0), commonPointArr[i + 1].getControlPoint(0)));
      }
    }

    return new ArrayList<Connection>(connections);
  }

  private static Point2D snapToEps(Point2D point, double eps) {
    return new Point2D.Double(Math.round(point.getX() / eps) * eps,
        Math.round(point.getY() / eps) * eps);
  }

  private static class ProjectGraphNode {
    private Point2D point;
    private Node node;
    private Area area;

    public ProjectGraphNode(Area area) {
      super();
      this.area = area;
    }

    public ProjectGraphNode(Point2D point) {
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

    public Area getArea() {
      return area;
    }
  }
}