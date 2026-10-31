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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.diylc.components.misc.CommonNode;
import org.diylc.components.misc.GroundSymbol;
import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.schematic.SchematicBuilder.SymbolEntry;
import org.diylc.schematic.SchematicBuilder.Wiring;
import org.junit.Test;

public class SchematicLayoutTest {

  @Test
  public void applyPitchPullsTheLeadSpanInToTheSchematicPitch() {
    ResistorSymbol symbol = new ResistorSymbol();
    double originalSpan = symbol.getControlPoint(0).distance(symbol.getControlPoint(1));
    double cx = (symbol.getControlPoint(0).getX() + symbol.getControlPoint(1).getX()) / 2;
    double cy = (symbol.getControlPoint(0).getY() + symbol.getControlPoint(1).getY()) / 2;

    SchematicBuilder.applyPitch(symbol, false, false);

    assertTrue("the stock one-inch span is what makes the leads overlap the neighbours",
        originalSpan > SchematicBuilder.SYMBOL_PITCH);
    assertEquals(SchematicBuilder.SYMBOL_PITCH,
        symbol.getControlPoint(0).distance(symbol.getControlPoint(1)), 0.001);
    assertTrue(symbol.getControlPoint(0).getX() < symbol.getControlPoint(1).getX());
    // the midpoint has to survive, because the label point is positioned relative to it
    assertEquals(cx, (symbol.getControlPoint(0).getX() + symbol.getControlPoint(1).getX()) / 2,
        0.001);
    assertEquals(cy, symbol.getControlPoint(0).getY(), 0.001);
    assertEquals(cy, symbol.getControlPoint(1).getY(), 0.001);
  }

  @Test
  public void applyPitchStandsShuntPartsUprightAndCanPutPinZeroAtTheBottom() {
    ResistorSymbol upright = new ResistorSymbol();
    SchematicBuilder.applyPitch(upright, true, false);
    assertEquals(upright.getControlPoint(0).getX(), upright.getControlPoint(1).getX(), 0.001);
    assertTrue("pin 0 sits on top when not flipped",
        upright.getControlPoint(0).getY() < upright.getControlPoint(1).getY());

    ResistorSymbol flipped = new ResistorSymbol();
    SchematicBuilder.applyPitch(flipped, true, true);
    assertEquals(flipped.getControlPoint(0).getX(), flipped.getControlPoint(1).getX(), 0.001);
    assertTrue("a grounded pin 0 has to end up at the bottom, where the ground symbol is drawn",
        flipped.getControlPoint(0).getY() > flipped.getControlPoint(1).getY());
  }

  @Test
  public void applyPitchLeavesRigidSymbolsAlone() {
    BJTSymbol bjt = new BJTSymbol();
    int count = bjt.getControlPointCount();
    Point2D[] before = new Point2D[count];
    for (int i = 0; i < count; i++) {
      before[i] = new Point2D.Double(bjt.getControlPoint(i).getX(), bjt.getControlPoint(i).getY());
    }

    SchematicBuilder.applyPitch(bjt, true, false);

    for (int i = 0; i < count; i++) {
      assertEquals("leg " + i + " x", before[i].getX(), bjt.getControlPoint(i).getX(), 0.001);
      assertEquals("leg " + i + " y", before[i].getY(), bjt.getControlPoint(i).getY(), 0.001);
    }
  }

  @Test
  public void railNetsProduceStubsInsteadOfWires() {
    ResistorSymbol physical = named(new ResistorSymbol(), "R1");
    GroundSymbol ground = new GroundSymbol();
    ground.setId(UUID.randomUUID());

    ResistorSymbol symbol = named(new ResistorSymbol(), "R1");
    Map<UUID, List<SymbolEntry>> entries = entriesFor(physical, symbol);

    Netlist netlist = new Netlist(new ArrayList<IDIYComponent<?>>());
    netlist.add(new Group(new Node(ground, 0), new Node(physical, 1)));

    Wiring wiring = new SchematicBuilder().createWires(netlist, entries);

    assertTrue("a rail net must never be routed as a wire", wiring.getWires().isEmpty());
    assertEquals(1, wiring.getStubs().size());
    IDIYComponent<?> stub = wiring.getStubs().get(0);
    assertTrue(stub instanceof GroundSymbol);
    assertEquals(symbol.getControlPoint(1).getX(), stub.getControlPoint(0).getX(), 0.001);
    assertEquals(symbol.getControlPoint(1).getY(), stub.getControlPoint(0).getY(), 0.001);
  }

  @Test
  public void supplyRailsProduceALabelledCommonNodeStub() {
    ResistorSymbol physical = named(new ResistorSymbol(), "R1");
    CommonNode supply = new CommonNode();
    supply.setId(UUID.randomUUID());
    supply.setValue("B+");

    ResistorSymbol symbol = named(new ResistorSymbol(), "R1");
    Map<UUID, List<SymbolEntry>> entries = entriesFor(physical, symbol);

    Netlist netlist = new Netlist(new ArrayList<IDIYComponent<?>>());
    netlist.add(new Group(new Node(supply, 0), new Node(physical, 0)));

    Wiring wiring = new SchematicBuilder().createWires(netlist, entries);

    assertTrue(wiring.getWires().isEmpty());
    assertEquals(1, wiring.getStubs().size());
    IDIYComponent<?> stub = wiring.getStubs().get(0);
    assertTrue(stub instanceof CommonNode);
    assertEquals("B+", ((CommonNode) stub).getValue());
  }

  @Test
  public void ordinarySignalNetsAreStillWired() {
    ResistorSymbol physicalA = named(new ResistorSymbol(), "R1");
    ResistorSymbol physicalB = named(new ResistorSymbol(), "R2");
    ResistorSymbol symbolA = named(new ResistorSymbol(), "R1");
    ResistorSymbol symbolB = named(new ResistorSymbol(), "R2");
    SchematicBuilder.moveAnchorTo(symbolB, 400, 400);

    Map<UUID, List<SymbolEntry>> entries = entriesFor(physicalA, symbolA);
    entries.putAll(entriesFor(physicalB, symbolB));

    Group group = new Group(new Node(physicalA, 1), new Node(physicalB, 0));
    assertEquals("the two pins must stay distinct in the group", 2, group.getNodes().size());

    Netlist netlist = new Netlist(new ArrayList<IDIYComponent<?>>());
    netlist.add(group);

    Wiring wiring = new SchematicBuilder().createWires(netlist, entries);

    assertTrue(wiring.getStubs().isEmpty());
    assertEquals(1, wiring.getWires().size());
  }

  @Test
  public void commonNodesAreNoLongerPlacedAsSymbolsOfTheirOwn() {
    assertFalse(SchematicBuilder.isEligible(new GroundSymbol()));
    assertFalse(SchematicBuilder.isEligible(new CommonNode()));
  }

  private static ResistorSymbol named(ResistorSymbol symbol, String name) {
    symbol.setId(UUID.randomUUID());
    symbol.setName(name);
    return symbol;
  }

  /** Maps one physical component to a single schematic symbol with a straight pin-for-pin mapping. */
  private static Map<UUID, List<SymbolEntry>> entriesFor(IDIYComponent<?> physical,
      IDIYComponent<?> symbol) {
    Map<Integer, Integer> pinMapping = new HashMap<Integer, Integer>();
    pinMapping.put(0, 0);
    pinMapping.put(1, 1);
    List<SymbolEntry> list = new ArrayList<SymbolEntry>();
    list.add(new SymbolEntry(symbol, pinMapping));
    Map<UUID, List<SymbolEntry>> entries = new LinkedHashMap<UUID, List<SymbolEntry>>();
    entries.put(physical.getId(), list);
    return entries;
  }
}
