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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.components.semiconductors.TransistorPinout;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.schematic.SchematicBuilder.SymbolEntry;
import org.junit.Test;

public class LayeredPlacementTest {

  /** BJT_EBC puts the emitter at 0, the base at 1 and the collector at 2. */
  private static final int BASE = 1;
  private static final int COLLECTOR = 2;

  @Test
  public void aDrivenPartLandsInAColumnAfterTheOneDrivingIt() {
    Circuit circuit = new Circuit();
    IDIYComponent<?> q1 = circuit.addTransistor("Q1");
    IDIYComponent<?> q2 = circuit.addTransistor("Q2");
    circuit.connect(q1, COLLECTOR, q2, BASE);

    circuit.place();

    assertTrue("the driven transistor must sit to the right of the one driving it",
        circuit.minX(q1) < circuit.minX(q2));
  }

  @Test
  public void aChainOfThreeRunsLeftToRight() {
    Circuit circuit = new Circuit();
    IDIYComponent<?> q1 = circuit.addTransistor("Q1");
    IDIYComponent<?> q2 = circuit.addTransistor("Q2");
    IDIYComponent<?> q3 = circuit.addTransistor("Q3");
    circuit.connect(q1, COLLECTOR, q2, BASE);
    circuit.connect(q2, COLLECTOR, q3, BASE);

    circuit.place();

    assertTrue(circuit.minX(q1) < circuit.minX(q2));
    assertTrue(circuit.minX(q2) < circuit.minX(q3));
  }

  /**
   * Feedback is normal in the circuits this feature exists for, so the layering has to survive a
   * cycle rather than recurse through it forever.
   */
  @Test
  public void aFeedbackLoopIsBrokenInsteadOfHanging() {
    Circuit circuit = new Circuit();
    IDIYComponent<?> q1 = circuit.addTransistor("Q1");
    IDIYComponent<?> q2 = circuit.addTransistor("Q2");
    circuit.connect(q1, COLLECTOR, q2, BASE);
    circuit.connect(q2, COLLECTOR, q1, BASE);

    circuit.place();

    // the forward edge survives, so the two still land in different columns
    assertTrue(circuit.minX(q1) != circuit.minX(q2));
  }

  @Test
  public void aPassivePartIsPulledInBetweenTheComponentsItConnects() {
    Circuit circuit = new Circuit();
    IDIYComponent<?> q1 = circuit.addTransistor("Q1");
    IDIYComponent<?> q2 = circuit.addTransistor("Q2");
    IDIYComponent<?> r1 = circuit.addResistor("R1");
    circuit.connect(q1, COLLECTOR, q2, BASE);
    circuit.connect(q1, COLLECTOR, r1, 0);
    circuit.connect(r1, 1, q2, BASE);

    circuit.place();

    double left = Math.min(circuit.minX(q1), circuit.minX(q2));
    double right = Math.max(circuit.minX(q1), circuit.minX(q2));
    assertTrue("the resistor should not be pushed outside the parts it sits between",
        circuit.minX(r1) >= left && circuit.minX(r1) <= right);
  }

  @Test
  public void symbolsInTheSameColumnDoNotOverlap() {
    Circuit circuit = new Circuit();
    IDIYComponent<?> q1 = circuit.addTransistor("Q1");
    IDIYComponent<?> a = circuit.addResistor("R1");
    IDIYComponent<?> b = circuit.addResistor("R2");
    // both resistors hang off the same pin, so they share a column
    circuit.connect(q1, COLLECTOR, a, 0);
    circuit.connect(q1, COLLECTOR, b, 0);

    circuit.place();

    boolean separated = circuit.maxY(a) <= circuit.minY(b) || circuit.maxY(b) <= circuit.minY(a)
        || circuit.maxX(a) <= circuit.minX(b) || circuit.maxX(b) <= circuit.minX(a);
    assertTrue("two symbols were placed on top of each other", separated);
  }

  /** The regression suite compares rendered output, so the same circuit must place identically. */
  @Test
  public void placementIsRepeatable() {
    List<Point2D> first = placeTwoStageCircuit();
    List<Point2D> second = placeTwoStageCircuit();

    assertEquals(first.size(), second.size());
    for (int i = 0; i < first.size(); i++) {
      assertEquals("point " + i + " x", first.get(i).getX(), second.get(i).getX(), 0.0001);
      assertEquals("point " + i + " y", first.get(i).getY(), second.get(i).getY(), 0.0001);
    }
  }

  private static List<Point2D> placeTwoStageCircuit() {
    Circuit circuit = new Circuit();
    IDIYComponent<?> q1 = circuit.addTransistor("Q1");
    IDIYComponent<?> q2 = circuit.addTransistor("Q2");
    IDIYComponent<?> r1 = circuit.addResistor("R1");
    IDIYComponent<?> r2 = circuit.addResistor("R2");
    circuit.connect(q1, COLLECTOR, r1, 0);
    circuit.connect(r1, 1, q2, BASE);
    circuit.connect(q2, COLLECTOR, r2, 0);
    circuit.place();
    return circuit.allPoints();
  }

  /** Builds a synthetic layout, its symbols and its netlist, and runs the placement over them. */
  private static class Circuit {

    private final List<IDIYComponent<?>> eligible = new ArrayList<IDIYComponent<?>>();
    private final Map<UUID, List<SymbolEntry>> entries = new LinkedHashMap<UUID, List<SymbolEntry>>();
    private final Netlist netlist = new Netlist(new ArrayList<IDIYComponent<?>>());

    IDIYComponent<?> addTransistor(String name) {
      TransistorTO92 transistor = new TransistorTO92();
      transistor.setId(UUID.randomUUID());
      transistor.setName(name);
      transistor.setPinout(TransistorPinout.BJT_EBC);
      BJTSymbol symbol = new BJTSymbol();
      symbol.setId(UUID.randomUUID());
      symbol.setName(name);
      return register(transistor, symbol, 3);
    }

    IDIYComponent<?> addResistor(String name) {
      Resistor resistor = new Resistor();
      resistor.setId(UUID.randomUUID());
      resistor.setName(name);
      ResistorSymbol symbol = new ResistorSymbol();
      symbol.setId(UUID.randomUUID());
      symbol.setName(name);
      return register(resistor, symbol, 2);
    }

    private IDIYComponent<?> register(IDIYComponent<?> physical, IDIYComponent<?> symbol,
        int pinCount) {
      Map<Integer, Integer> pinMapping = new HashMap<Integer, Integer>();
      for (int i = 0; i < pinCount; i++) {
        pinMapping.put(i, i);
      }
      List<SymbolEntry> list = new ArrayList<SymbolEntry>();
      list.add(new SymbolEntry(symbol, pinMapping));
      eligible.add(physical);
      entries.put(physical.getId(), list);
      return physical;
    }

    void connect(IDIYComponent<?> a, int pinA, IDIYComponent<?> b, int pinB) {
      netlist.add(new Group(new Node(a, pinA), new Node(b, pinB)));
    }

    void place() {
      new LayeredPlacement().place(eligible, entries, netlist);
    }

    private IDIYComponent<?> symbolOf(IDIYComponent<?> physical) {
      return entries.get(physical.getId()).get(0).symbol;
    }

    double minX(IDIYComponent<?> physical) {
      return extreme(symbolOf(physical), true, false);
    }

    double maxX(IDIYComponent<?> physical) {
      return extreme(symbolOf(physical), true, true);
    }

    double minY(IDIYComponent<?> physical) {
      return extreme(symbolOf(physical), false, false);
    }

    double maxY(IDIYComponent<?> physical) {
      return extreme(symbolOf(physical), false, true);
    }

    private static double extreme(IDIYComponent<?> symbol, boolean horizontal, boolean max) {
      double result = max ? -Double.MAX_VALUE : Double.MAX_VALUE;
      for (int i = 0; i < symbol.getControlPointCount(); i++) {
        Point2D p = symbol.getControlPoint(i);
        double value = horizontal ? p.getX() : p.getY();
        result = max ? Math.max(result, value) : Math.min(result, value);
      }
      return result;
    }

    List<Point2D> allPoints() {
      List<Point2D> points = new ArrayList<Point2D>();
      for (IDIYComponent<?> physical : eligible) {
        IDIYComponent<?> symbol = symbolOf(physical);
        for (int i = 0; i < symbol.getControlPointCount(); i++) {
          Point2D p = symbol.getControlPoint(i);
          points.add(new Point2D.Double(p.getX(), p.getY()));
        }
      }
      return points;
    }
  }
}
