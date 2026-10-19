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
package org.diylc.plugins.chatbot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import org.diylc.common.Orientation;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.passive.Resistor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.plugins.chatbot.model.AiComponent;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;
import org.diylc.plugins.chatbot.model.AiProject;
import org.diylc.plugins.chatbot.model.AiTerminal;

/**
 * The AI edits a project through what this export tells it, so the export has to let it name every
 * component, see which wires are attached where, and notice properties such as a rotated pickup.
 * Several components can share a name, which used to leave all but one of them out of reach of an
 * edit.
 *
 * @author Branislav Stojkovic
 */
public class AiProjectBuilderTest {

  private static final Point2D FIRST_LEAD = new Point2D.Double(100, 100);
  private static final Point2D SECOND_LEAD = new Point2D.Double(200, 100);
  private static final Point2D LOOSE_END = new Point2D.Double(200, 300);
  private static final UUID FIRST = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000000");
  private static final UUID SECOND = UUID.fromString("c3d4a1b2-0000-0000-0000-000000000000");

  @Test
  public void uniqueNameIsTheId() {
    AiProject export = export(resistor(), wire("W1", SECOND_LEAD, LOOSE_END));

    assertEquals(List.of("R1", "W1"), export.components().stream().map(AiComponent::id).toList());
  }

  @Test
  public void repeatedNameGetsTheStartOfTheUuid() {
    AiProject export = export(wire("W1", FIRST, FIRST_LEAD, LOOSE_END),
        wire("W1", SECOND, SECOND_LEAD, LOOSE_END));

    assertEquals(List.of("W1#a1b2", "W1#c3d4"),
        export.components().stream().map(AiComponent::id).toList());
  }

  @Test
  public void suffixGrowsUntilUuidsDiffer() {
    AiProject export = export(
        wire("W1", UUID.fromString("a1b2e5f6-0000-0000-0000-000000000000"), FIRST_LEAD, LOOSE_END),
        wire("W1", UUID.fromString("a1b2e5f7-0000-0000-0000-000000000000"), SECOND_LEAD,
            LOOSE_END));

    assertEquals(List.of("W1#a1b2e5f6", "W1#a1b2e5f7"),
        export.components().stream().map(AiComponent::id).toList());
  }

  @Test
  public void wireEndListsTheTerminalItLandsOn() {
    AiProject export = export(resistor(), wire("W1", SECOND_LEAD, LOOSE_END));

    List<AiTerminal> ends = component(export, "W1").terminals();
    assertEquals(List.of("R1.1"), ends.get(0).connectedTo());
    assertEquals("a loose end is reported as attached to nothing", List.of(),
        ends.get(ends.size() - 1).connectedTo());
  }

  @Test
  public void copiedWiresCanBeToldApart() {
    AiProject export = export(resistor(), wire("W1", FIRST, FIRST_LEAD, LOOSE_END),
        wire("W1", SECOND, SECOND_LEAD, LOOSE_END));

    assertEquals(List.of("R1.0"), component(export, "W1#a1b2").terminals().get(0).connectedTo());
    assertEquals(List.of("R1.1"), component(export, "W1#c3d4").terminals().get(0).connectedTo());
  }

  @Test
  public void wiresMeetingAtOnePointSeeEachOther() {
    AiProject export =
        export(wire("W1", FIRST_LEAD, LOOSE_END), wire("W2", SECOND_LEAD, LOOSE_END));

    List<AiTerminal> ends = component(export, "W1").terminals();
    List<String> attached = ends.get(ends.size() - 1).connectedTo();
    assertEquals(1, attached.size());
    assertTrue(attached.get(0).startsWith("W2."));
  }

  @Test
  public void regularComponentsCarryNoContacts() {
    AiProject export = export(resistor(), wire("W1", SECOND_LEAD, LOOSE_END));

    for (AiTerminal terminal : component(export, "R1").terminals()) {
      assertNull(terminal.connectedTo());
    }
  }

  @Test
  public void propertiesCarryCurrentValues() {
    SingleCoilPickup straight = new SingleCoilPickup();
    straight.setName("Bridge");
    SingleCoilPickup rotated = new SingleCoilPickup();
    rotated.setName("Neck");
    rotated.setOrientation(Orientation._180);

    AiProject export = export(straight, rotated);

    assertEquals("DEFAULT", component(export, "Bridge").properties().get("Orientation"));
    assertEquals("_180", component(export, "Neck").properties().get("Orientation"));
  }

  @Test
  public void identityAndCosmeticPropertiesAreLeftOut() {
    Map<String, String> properties = component(export(resistor()), "R1").properties();

    assertFalse(properties.containsKey("Name"));
    assertFalse(properties.containsKey("Value"));
    assertFalse(properties.containsKey("Alpha"));
    assertFalse("colors are cosmetic", properties.containsKey("Color"));
  }

  @Test
  public void editScriptReachesACopiedWireByItsId() {
    HookupWire first = wire("W1", FIRST, FIRST_LEAD, LOOSE_END);
    HookupWire copy = wire("W1", SECOND, SECOND_LEAD, LOOSE_END);
    Project project = project(first, copy);

    AiEditOperation remove = new AiEditOperation();
    remove.setAction("remove");
    remove.setComponentName("W1#c3d4");
    AiEditScript script = new AiEditScript();
    script.setOperations(List.of(remove));
    new AiEditScriptEditor(script).edit(project, new HashSet<IDIYComponent<?>>());

    assertEquals(List.of(first), project.getComponents());
  }

  private static AiProject export(IDIYComponent<?>... components) {
    return AiProjectBuilder.build(project(components), new ArrayList<>());
  }

  private static Project project(IDIYComponent<?>... components) {
    Project project = new Project();
    project.getComponents().addAll(List.of(components));
    return project;
  }

  private static AiComponent component(AiProject export, String id) {
    return export.components().stream().filter(c -> c.id().equals(id)).findFirst().orElseThrow();
  }

  private static Resistor resistor() {
    Resistor resistor = new Resistor();
    resistor.setName("R1");
    resistor.setControlPoint(FIRST_LEAD, 0);
    resistor.setControlPoint(SECOND_LEAD, 1);
    return resistor;
  }

  private static HookupWire wire(String name, Point2D from, Point2D to) {
    HookupWire wire = new HookupWire();
    wire.setName(name);
    wire.stretchBetween(from, to);
    return wire;
  }

  private static HookupWire wire(String name, UUID id, Point2D from, Point2D to) {
    HookupWire wire = wire(name, from, to);
    wire.setId(id);
    return wire;
  }
}
