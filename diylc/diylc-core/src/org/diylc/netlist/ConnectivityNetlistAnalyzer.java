package org.diylc.netlist;

import java.awt.geom.Point2D;
import java.util.Optional;
import org.diylc.presenter.DrawingManager;

public class ConnectivityNetlistAnalyzer extends NetlistAnalyzer {

  private Point2D point1;
  private Point2D point2;

  public ConnectivityNetlistAnalyzer(Point2D point1, Point2D point2) {
    this.point1 = point1;
    this.point2 = point2;
  }

  @Override
  protected Summary summarize(Netlist netlist, Node preferredOutput) throws TreeException {
    Node node1 = netlist.getGroups().stream()
        .flatMap(x -> x.getNodes().stream())
        .filter(n -> n.getPoint2D().distance(point1) < DrawingManager.CONTROL_POINT_SIZE)
        .findFirst().orElse(null);
    
    Node node2 = netlist.getGroups().stream()
        .flatMap(x -> x.getNodes().stream())
        .filter(n -> n.getPoint2D().distance(point2) < DrawingManager.CONTROL_POINT_SIZE)
        .findFirst().orElse(null);
    
    if (node1 == null || node2 == null) {
      return new Summary(netlist, "At least one of the selected points does not represent a node.");
    }

    Tree treeBetween = constructTreeBetween(netlist, node1, node2);

    return new Summary(netlist, Optional.ofNullable(treeBetween)
        .map(x -> x.toAsciiString())
        .orElse("No conection detected."));
  }
}
