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
 * 
 */
package org.diylc.netlist;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.diylc.presenter.ContinuityArea;
import org.diylc.utils.RTree;

/***
 * A graph containing continuity areas sorted into groups that are connected together. 
 * 
 * @author bancika
 *
 */
public class ContinuityGraph {

  private RTree<ContinuityNode> groupTree;
  private Map<Integer, List<ContinuityArea>> areaGroupMap;

  public ContinuityGraph(RTree<ContinuityNode> groupTree, Map<Integer, List<ContinuityArea>> areaGroupMap) {
    super();
    this.groupTree = groupTree;
    this.areaGroupMap = areaGroupMap;
  }

  public List<Area> findAreasFor(Point2D point) {
    List<ContinuityNode> matchingNodes = groupTree.search(point, NetlistBuilder.eps)
        .stream()
        .filter(node -> node.getArea().getArea().contains(point))
        .sorted(Comparator.comparing(x -> x.getArea().getZIndex(), Comparator.reverseOrder()))
        .collect(Collectors.toList());
    
    if (matchingNodes.isEmpty())
      return null;
    
    return areaGroupMap.get(matchingNodes.get(0).getGroupId())
        .stream()
        .map(x -> x.getArea())
        .collect(Collectors.toList());
  }
  
  public static class ContinuityNode {
    private ContinuityArea area;
    private Integer groupId;

    public ContinuityNode(ContinuityArea area, Integer groupId) {
      super();
      this.area = area;
      this.groupId = groupId;
    }

    public ContinuityArea getArea() {
      return area;
    }

    public Integer getGroupId() {
      return groupId;
    }
  }
}
