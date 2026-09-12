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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.schematic.SchematicBox;
import org.diylc.components.schematic.SchematicWire;
import org.diylc.core.IDIYComponent;
import org.junit.Test;

public class SchematicRerouteTest {

  private static ResistorSymbol symbolAt(double x1, double y1, double x2, double y2) {
    ResistorSymbol symbol = new ResistorSymbol();
    symbol.setId(UUID.randomUUID());
    symbol.setControlPoint(new Point2D.Double(x1, y1), 0);
    symbol.setControlPoint(new Point2D.Double(x2, y2), 1);
    return symbol;
  }

  @Test
  public void wireEndpointsFollowMovedSymbol() {
    ResistorSymbol a = symbolAt(100, 100, 140, 100);
    ResistorSymbol b = symbolAt(400, 100, 440, 100);

    SchematicWire wire = new SchematicWire();
    wire.setId(UUID.randomUUID());
    wire.setSourceComponentId(a.getId());
    wire.setSourcePinIndex(1);
    wire.setTargetComponentId(b.getId());
    wire.setTargetPinIndex(0);
    wire.setRoutePoints(new ArrayList<Point2D>(List.of(new Point2D.Double(0, 0), new Point2D.Double(1, 1))));

    List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>(List.of(a, wire, b));

    assertTrue(SchematicBuilder.rerouteWires(components));
    assertEndpoint(wire, 0, a.getControlPoint(1));
    assertEndpoint(wire, last(wire), b.getControlPoint(0));

    // move b down; re-route and check the wire still ends on b's pin
    b.setControlPoint(new Point2D.Double(400, 400), 0);
    b.setControlPoint(new Point2D.Double(440, 400), 1);

    assertTrue(SchematicBuilder.rerouteWires(components));
    assertEndpoint(wire, 0, a.getControlPoint(1));
    assertEndpoint(wire, last(wire), b.getControlPoint(0));

    // a second re-route with nothing moved should be a no-op
    assertTrue(!SchematicBuilder.rerouteWires(components));
  }

  @Test
  public void moveAnchorToTranslatesEverySchematicBoxPinExactlyOnce() {
    SchematicBox box = new SchematicBox();
    box.setLeftNodes("A,B");
    box.setRightNodes("C,D");
    box.setTopNodes("");
    box.setBottomNodes("");

    int n = box.getControlPointCount();
    double ax = box.getControlPoint(0).getX();
    double ay = box.getControlPoint(0).getY();
    double[] relX = new double[n];
    double[] relY = new double[n];
    for (int i = 0; i < n; i++) {
      relX[i] = box.getControlPoint(i).getX() - ax;
      relY[i] = box.getControlPoint(i).getY() - ay;
    }

    SchematicBuilder.moveAnchorTo(box, 500, 300);

    assertEquals(500.0, box.getControlPoint(0).getX(), 0.001);
    assertEquals(300.0, box.getControlPoint(0).getY(), 0.001);
    for (int i = 0; i < n; i++) {
      assertEquals("pin " + i + " x", 500 + relX[i], box.getControlPoint(i).getX(), 0.001);
      assertEquals("pin " + i + " y", 300 + relY[i], box.getControlPoint(i).getY(), 0.001);
    }
  }

  @Test
  public void draggingSchematicBoxTranslatesEveryPinByTheSameDelta() {
    // the presenter drags a box by calling setControlPoint(old + delta, i) for every index, in an
    // arbitrary (HashSet) order; every pin must end up translated by exactly the delta
    int[][] orders = {{0, 1, 2, 3, 4, 5, 6}, {6, 5, 4, 3, 2, 1, 0}, {3, 0, 5, 1, 6, 2, 4}};
    for (int[] order : orders) {
      SchematicBox box = new SchematicBox();
      box.setLeftNodes("A,B,C");
      box.setRightNodes("D,E");
      box.setTopNodes("F");
      box.setBottomNodes("G");
      int n = box.getControlPointCount();
      assertEquals(7, n);

      Point2D[] before = new Point2D[n];
      for (int i = 0; i < n; i++) {
        before[i] = new Point2D.Double(box.getControlPoint(i).getX(), box.getControlPoint(i).getY());
      }

      double dx = 137;
      double dy = -84;
      for (int idx : order) {
        Point2D old = box.getControlPoint(idx);
        box.setControlPoint(new Point2D.Double(old.getX() + dx, old.getY() + dy), idx);
      }

      for (int i = 0; i < n; i++) {
        assertEquals("pin " + i + " x", before[i].getX() + dx, box.getControlPoint(i).getX(), 0.001);
        assertEquals("pin " + i + " y", before[i].getY() + dy, box.getControlPoint(i).getY(), 0.001);
      }
    }
  }

  private static int last(SchematicWire wire) {
    return wire.getRoutePoints().size() - 1;
  }

  private static void assertEndpoint(SchematicWire wire, int index, Point2D expected) {
    Point2D actual = wire.getRoutePoints().get(index);
    assertEquals("x", expected.getX(), actual.getX(), 0.6);
    assertEquals("y", expected.getY(), actual.getY(), 0.6);
  }
}
