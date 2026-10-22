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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Orientation;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.electromechanical.ClosedJack1_4;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.passive.AxialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Voltage;
import org.diylc.core.measures.VoltageUnit;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;
import org.diylc.plugins.chatbot.model.AiPoint;

/**
 * Covers how a terminal reference such as {@code "R1.1"} is turned into a control point.
 * <p>
 * The index is what the model is handed, both by the project description and by the component
 * catalog, so it has to win over the node name. Node names are frequently the one-based lug
 * numbers, and matching those first used to make the second lead of a two-lead component
 * unreachable: a wire asked for {@code R1.1} landed silently on the first lead.
 *
 * @author Branislav Stojkovic
 */
public class AiEditScriptEditorTest {

  private static final Point2D FIRST_LEAD = new Point2D.Double(100, 100);
  private static final Point2D SECOND_LEAD = new Point2D.Double(200, 100);
  private static final Point2D FAR_END = new Point2D.Double(600, 500);
  private static final Point2D OTHER_FAR_END = new Point2D.Double(700, 450);
  // body color of every Mallory 150 row in AxialFilmCapacitor.datasheet
  private static final Color MALLORY_150_BODY = Color.decode("#FFE303");

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  @Test
  public void numericReferenceIsAControlPointIndex() {
    assertEquals(FIRST_LEAD, wireStartFrom(resistor(), "R1.0", "R1.1"));
    assertEquals(SECOND_LEAD, wireStartFrom(resistor(), "R1.1", "R1.0"));
  }

  @Test
  public void namedTerminalIsStillResolvedByItsName() {
    ClosedJack1_4 jack = new ClosedJack1_4();
    jack.setName("J1");
    jack.setControlPoint(FIRST_LEAD, 0);
    jack.setControlPoint(SECOND_LEAD, 1);

    assertEquals("Tip", jack.getControlPointNodeName(0));
    assertEquals(FIRST_LEAD, wireStartFrom(jack, "J1.Tip", "J1.Sleeve"));
    assertEquals(SECOND_LEAD, wireStartFrom(jack, "J1.Sleeve", "J1.Tip"));
  }

  @Test
  public void indexBeyondTheLastControlPointIsReported() {
    Project project = projectWith(resistor());
    AiEditScriptEditor editor = new AiEditScriptEditor(wireScript("R1.99", "R1.0"));
    editor.edit(project, new HashSet<IDIYComponent<?>>());

    assertNull("no wire should have been added", findWire(project));
    assertFalse("the model should be told, not guessed at", editor.getWarnings().isEmpty());
  }

  @Test
  public void removeOnlyScriptDropsTheRemovedComponentFromTheSelection() {
    Resistor resistor = resistor();
    Project project = projectWith(resistor);
    Set<IDIYComponent<?>> selection = new HashSet<IDIYComponent<?>>();
    selection.add(resistor);

    Set<IDIYComponent<?>> newSelection =
        new AiEditScriptEditor(script(operation("remove", "R1"))).edit(project, selection);

    assertTrue(project.getComponents().isEmpty());
    assertFalse("a removed component must not stay selected", newSelection.contains(resistor));
  }

  @Test
  public void removeDoesNotFallBackToAPrefixMatch() {
    Resistor resistor = resistor();
    resistor.setName("R10");
    Project project = projectWith(resistor);

    AiEditScriptEditor editor = new AiEditScriptEditor(script(operation("remove", "R1")));
    editor.edit(project, new HashSet<IDIYComponent<?>>());

    assertTrue("R10 must survive a request to remove R1",
        project.getComponents().contains(resistor));
    assertFalse(editor.getWarnings().isEmpty());
  }

  @Test
  public void modifyDoesNotFallBackToANormalizedMatch() {
    Resistor resistor = resistor();
    resistor.setName("R_1");
    Project project = projectWith(resistor);
    AiEditOperation operation = operation("modify", "R1");
    operation.setProperties(Map.of("Value", "4.7K"));

    AiEditScriptEditor editor = new AiEditScriptEditor(script(operation));
    editor.edit(project, new HashSet<IDIYComponent<?>>());

    assertNotEquals(Resistance.parseResistance("4.7K"), resistor.getValue());
    assertFalse(editor.getWarnings().isEmpty());
  }

  @Test
  public void removeStillForgivesADifferenceInCase() {
    Project project = projectWith(resistor());

    new AiEditScriptEditor(script(operation("remove", "r1")))
        .edit(project, new HashSet<IDIYComponent<?>>());

    assertTrue(project.getComponents().isEmpty());
  }

  @Test
  public void modifyCanFollowTheAddOfTheSameComponent() {
    Project project = new Project();
    AiEditOperation add = operation("add", "R2");
    add.setComponentType("passive.Resistor");
    add.setFromPos(point(1, 1));
    add.setToPos(point(5, 1));
    AiEditOperation modify = operation("modify", "R2");
    modify.setProperties(Map.of("Value", "4.7K"));

    AiEditScriptEditor editor = new AiEditScriptEditor(script(add, modify));
    editor.edit(project, new HashSet<IDIYComponent<?>>());

    assertEquals(editor.getWarnings().toString(), 1, project.getComponents().size());
    assertEquals(Resistance.parseResistance("4.7K"),
        ((Resistor) project.getComponents().get(0)).getValue());
    assertTrue(editor.getWarnings().isEmpty());
  }

  @Test
  public void removeBeforeAnAddThatReusesTheNameTakesOutTheOldComponent() {
    Resistor old = resistor();
    Project project = projectWith(old);
    AiEditOperation add = operation("add", "R1");
    add.setComponentType("passive.Resistor");
    add.setFromPos(point(1, 3));
    add.setToPos(point(5, 3));

    new AiEditScriptEditor(script(operation("remove", "R1"), add))
        .edit(project, new HashSet<IDIYComponent<?>>());

    assertEquals(1, project.getComponents().size());
    assertNotSame(old, project.getComponents().get(0));
  }

  @Test
  public void wireIsShapedLikeOneDrawnByHand() {
    Project project = projectWith(resistor());
    new AiEditScriptEditor(wireScript("R1.0", "R1.1"))
        .edit(project, new HashSet<IDIYComponent<?>>());

    HookupWire drawn = new HookupWire();
    drawn.stretchBetween(FIRST_LEAD, SECOND_LEAD);
    HookupWire wire = findWire(project);
    for (int i = 0; i < drawn.getControlPointCount(); i++) {
      assertEquals("control point " + i, drawn.getControlPoint(i), wire.getControlPoint(i));
    }
  }

  @Test
  public void datasheetSeriesKeepsTheValueAndTheVoltage() {
    AxialFilmCapacitor capacitor = capacitor(new Capacitance(0.22, CapacitanceUnit.uF));
    capacitor.setVoltageNew(new Voltage(250d, VoltageUnit.V));

    AiEditScriptEditor editor = applySeries(capacitor, "Mallory 150");

    assertTrue(editor.getWarnings().toString(), editor.getWarnings().isEmpty());
    assertEquals(MALLORY_150_BODY, capacitor.getBodyColor());
    assertEquals(0.22, capacitor.getValue().getNormalizedValue(), 1e-9);
    assertEquals(new Voltage(250d, VoltageUnit.V), capacitor.getVoltageNew());
  }

  @Test
  public void datasheetSeriesMatchesAValueWrittenInOtherUnits() {
    AxialFilmCapacitor capacitor = capacitor(new Capacitance(220d, CapacitanceUnit.nF));

    AiEditScriptEditor editor = applySeries(capacitor, "Mallory 150");

    assertTrue(editor.getWarnings().toString(), editor.getWarnings().isEmpty());
    assertEquals(MALLORY_150_BODY, capacitor.getBodyColor());
    assertEquals(0.22, capacitor.getValue().getNormalizedValue(), 1e-9);
  }

  @Test
  public void datasheetSeriesWithoutTheValueLeavesTheComponentAlone() {
    Capacitance value = new Capacitance(1.234, CapacitanceUnit.uF);
    AxialFilmCapacitor capacitor = capacitor(value);
    Color bodyColor = capacitor.getBodyColor();

    AiEditScriptEditor editor = applySeries(capacitor, "Mallory 150");

    assertFalse("the model should be told the series has no such part",
        editor.getWarnings().isEmpty());
    assertEquals(value, capacitor.getValue());
    assertEquals(bodyColor, capacitor.getBodyColor());
  }

  @Test
  public void replaceMovesTheAttachedWireEndsToTheMappedTerminals() {
    SingleCoilPickup pickup = singleCoil();
    Point2D anchor = copy(pickup.getControlPoint(0));
    HookupWire w1 = wire("W1", pickup.getControlPoint(1), FAR_END);
    HookupWire w2 = wire("W2", pickup.getControlPoint(2), OTHER_FAR_END);
    Point2D[] w1Before = points(w1);
    Point2D[] w2Before = points(w2);
    Project project = projectWith(pickup, w1, w2);

    AiEditScriptEditor editor = run(project, replaceWithHumbucker(Map.of("1", "0", "2", "3")));

    assertTrue(editor.getWarnings().toString(), editor.getWarnings().isEmpty());
    assertEquals(3, project.getComponents().size());
    IDIYComponent<?> humbucker = project.getComponents().get(0);
    assertTrue("the new pickup keeps the place of the old one in the z-order",
        humbucker instanceof HumbuckerPickup);
    assertEquals("PU1", humbucker.getName());
    assertEquals(anchor, humbucker.getControlPoint(0));
    assertNotEquals("the test needs a terminal that is not where the old one was",
        w2Before[0], humbucker.getControlPoint(3));
    assertOnlyStartMoved(w1Before, w1, humbucker.getControlPoint(0));
    assertOnlyStartMoved(w2Before, w2, humbucker.getControlPoint(3));
  }

  @Test
  public void replaceRemovesAWireOnAnUnmappedTerminal() {
    SingleCoilPickup pickup = singleCoil();
    HookupWire kept = wire("W1", pickup.getControlPoint(1), FAR_END);
    HookupWire dropped = wire("W2", pickup.getControlPoint(2), OTHER_FAR_END);
    Point2D[] keptBefore = points(kept);
    Project project = projectWith(pickup, kept, dropped);

    AiEditScriptEditor editor = run(project, replaceWithHumbucker(Map.of("1", "0")));

    assertFalse(project.getComponents().contains(dropped));
    assertTrue(project.getComponents().contains(kept));
    assertOnlyStartMoved(keptBefore, kept, project.getComponents().get(0).getControlPoint(0));
    assertFalse("the user should hear that a wire went", editor.getWarnings().isEmpty());
  }

  @Test
  public void replaceMovesBothEndsOfAWireBetweenTwoOfItsTerminals() {
    SingleCoilPickup pickup = singleCoil();
    HookupWire wire = wire("W1", pickup.getControlPoint(1), pickup.getControlPoint(2));
    Point2D[] before = points(wire);
    Project project = projectWith(pickup, wire);

    run(project, replaceWithHumbucker(Map.of("1", "0", "2", "3")));

    IDIYComponent<?> humbucker = project.getComponents().get(0);
    Point2D start = humbucker.getControlPoint(0);
    Point2D end = humbucker.getControlPoint(3);
    assertEquals(start, wire.getControlPoint(0));
    assertEquals(moved(before[1], before[0], start), wire.getControlPoint(1));
    assertEquals(moved(before[2], before[3], end), wire.getControlPoint(2));
    assertEquals(end, wire.getControlPoint(3));
  }

  @Test
  public void replaceLeavesAWireThatOnlyReachesItThroughAnotherWire() {
    SingleCoilPickup pickup = singleCoil();
    HookupWire attached = wire("W1", pickup.getControlPoint(1), FAR_END);
    HookupWire chained = wire("W2", FAR_END, OTHER_FAR_END);
    Point2D[] attachedBefore = points(attached);
    Point2D[] chainedBefore = points(chained);
    Project project = projectWith(pickup, attached, chained);

    run(project, replaceWithHumbucker(Map.of("1", "0", "2", "3")));

    assertOnlyStartMoved(attachedBefore, attached,
        project.getComponents().get(0).getControlPoint(0));
    assertArrayEquals(chainedBefore, points(chained));
  }

  @Test
  public void laterWireReachesTheNewComponentByTheOldName() {
    Project project = projectWith(singleCoil(), resistor());
    AiEditOperation add = operation("add", "W9");
    add.setComponentType("connectivity.HookupWire");
    add.setFromTerminal("PU1.3");
    add.setToTerminal("R1.0");

    AiEditScriptEditor editor =
        run(project, replaceWithHumbucker(Map.of("1", "0", "2", "3")), add);

    assertTrue(editor.getWarnings().toString(), editor.getWarnings().isEmpty());
    IDIYComponent<?> humbucker = project.getComponents().get(0);
    assertEquals(humbucker.getControlPoint(3), findWire(project).getControlPoint(0));
  }

  @Test
  public void undoSnapshotRestoresTheOldComponentAndItsWires() {
    SingleCoilPickup pickup = singleCoil();
    HookupWire w1 = wire("W1", pickup.getControlPoint(1), FAR_END);
    HookupWire w2 = wire("W2", pickup.getControlPoint(2), OTHER_FAR_END);
    Project project = projectWith(pickup, w1, w2);
    Project original = project.clone();
    // Presenter.applyEditor keeps exactly this clone, and undo puts it back
    Project undoSnapshot = project.clone();

    run(project, replaceWithHumbucker(Map.of("1", "0")));

    assertNotEquals("the replace must have changed the project", original, project);
    assertEquals(original, undoSnapshot);
    assertTrue(undoSnapshot.getComponents().get(0) instanceof SingleCoilPickup);
    assertArrayEquals(points(pickup), points(undoSnapshot.getComponents().get(0)));
  }

  @Test
  public void unknownComponentOrTypeIsSkippedAndTheScriptGoesOn() {
    SingleCoilPickup pickup = singleCoil();
    HookupWire wire = wire("W1", pickup.getControlPoint(1), FAR_END);
    Point2D[] wireBefore = points(wire);
    Resistor resistor = resistor();
    Project project = projectWith(pickup, wire, resistor);
    AiEditOperation unknownName = replaceWithHumbucker(Map.of("1", "0"));
    unknownName.setComponentName("PU9");
    AiEditOperation unknownType = operation("replace", "PU1");
    unknownType.setComponentType("guitar.NoSuchPickup");
    AiEditOperation modify = operation("modify", "R1");
    modify.setProperties(Map.of("Value", "4.7K"));

    AiEditScriptEditor editor = run(project, unknownName, unknownType, modify);

    assertEquals(editor.getWarnings().toString(), 2, editor.getWarnings().size());
    assertSame(pickup, project.getComponents().get(0));
    assertArrayEquals(wireBefore, points(wire));
    assertEquals(Resistance.parseResistance("4.7K"), resistor.getValue());
  }

  @Test
  public void replaceWithoutAPinMapPairsTerminalsByName() {
    SingleCoilPickup pickup = singleCoil();
    String name = pickup.getControlPointNodeName(1);
    HookupWire wire = wire("W1", pickup.getControlPoint(1), FAR_END);
    Point2D[] before = points(wire);
    Project project = projectWith(pickup, wire);

    run(project, replaceWithHumbucker(null));

    IDIYComponent<?> humbucker = project.getComponents().get(0);
    int terminal = -1;
    for (int i = 0; i < humbucker.getControlPointCount(); i++) {
      if (name.equalsIgnoreCase(humbucker.getControlPointNodeName(i))) {
        terminal = i;
      }
    }
    assertTrue("the humbucker should have a terminal named " + name, terminal >= 0);
    assertOnlyStartMoved(before, wire, humbucker.getControlPoint(terminal));
  }

  @Test
  public void replaceKeepsTheOrientationUnlessItIsGiven() {
    SingleCoilPickup pickup = singleCoil();
    pickup.setOrientation(Orientation._90);
    Project project = projectWith(pickup);

    run(project, replaceWithHumbucker(Map.of()));

    assertEquals(Orientation._90,
        ((HumbuckerPickup) project.getComponents().get(0)).getOrientation());

    AiEditOperation rotated = operation("replace", "PU1");
    rotated.setComponentType("guitar.SingleCoilPickup");
    rotated.setProperties(Map.of("Orientation", Orientation._180.name()));
    run(project, rotated);

    assertEquals(Orientation._180,
        ((SingleCoilPickup) project.getComponents().get(0)).getOrientation());
  }

  @Test
  public void componentOnATerminalIsReportedAndLeftAlone() {
    SingleCoilPickup pickup = singleCoil();
    Resistor resistor = resistor();
    resistor.setControlPoint(copy(pickup.getControlPoint(1)), 0);
    Point2D[] before = points(resistor);
    Project project = projectWith(pickup, resistor);

    AiEditScriptEditor editor = run(project, replaceWithHumbucker(Map.of("1", "0")));

    assertArrayEquals(before, points(resistor));
    assertEquals(editor.getWarnings().toString(), 1, editor.getWarnings().size());
    assertTrue(editor.getWarnings().get(0).contains("R1"));
  }

  private static SingleCoilPickup singleCoil() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("PU1");
    // away from the origin, where a stray default point could pass for a terminal
    for (int i = 0; i < pickup.getControlPointCount(); i++) {
      Point2D p = pickup.getControlPoint(i);
      pickup.setControlPoint(new Point2D.Double(p.getX() + 300, p.getY() + 200), i);
    }
    return pickup;
  }

  private static HookupWire wire(String name, Point2D from, Point2D to) {
    HookupWire wire = new HookupWire();
    wire.setName(name);
    wire.stretchBetween(from, to);
    return wire;
  }

  private static AiEditOperation replaceWithHumbucker(Map<String, String> pinMap) {
    AiEditOperation operation = operation("replace", "PU1");
    operation.setComponentType("guitar.HumbuckerPickup");
    operation.setPinMap(pinMap);
    return operation;
  }

  private static AiEditScriptEditor run(Project project, AiEditOperation... operations) {
    AiEditScriptEditor editor = new AiEditScriptEditor(script(operations));
    editor.edit(project, new HashSet<IDIYComponent<?>>());
    return editor;
  }

  /**
   * The first end of the wire sits on the terminal and its handle moved along with it, while the
   * rest of the wire stayed where it was.
   */
  private static void assertOnlyStartMoved(Point2D[] before, HookupWire wire, Point2D terminal) {
    assertEquals(terminal, wire.getControlPoint(0));
    assertEquals(moved(before[1], before[0], terminal), wire.getControlPoint(1));
    for (int i = 2; i < before.length; i++) {
      assertEquals("control point " + i, before[i], wire.getControlPoint(i));
    }
  }

  /** Where a handle lands when the end it belongs to moves from {@code end} to {@code target}. */
  private static Point2D moved(Point2D handle, Point2D end, Point2D target) {
    return new Point2D.Double(handle.getX() + (target.getX() - end.getX()),
        handle.getY() + (target.getY() - end.getY()));
  }

  private static Point2D[] points(IDIYComponent<?> component) {
    Point2D[] points = new Point2D[component.getControlPointCount()];
    for (int i = 0; i < points.length; i++) {
      points[i] = copy(component.getControlPoint(i));
    }
    return points;
  }

  private static Point2D copy(Point2D point) {
    return new Point2D.Double(point.getX(), point.getY());
  }

  private static Resistor resistor() {
    Resistor resistor = new Resistor();
    resistor.setName("R1");
    resistor.setControlPoint(FIRST_LEAD, 0);
    resistor.setControlPoint(SECOND_LEAD, 1);
    return resistor;
  }

  /** Runs a script that wires from the given terminal, and reports where the wire starts. */
  private static Point2D wireStartFrom(IDIYComponent<?> component, String from, String to) {
    Project project = projectWith(component);
    new AiEditScriptEditor(wireScript(from, to)).edit(project, new HashSet<IDIYComponent<?>>());
    HookupWire wire = findWire(project);
    return wire == null ? null : wire.getControlPoint(0);
  }

  private static Project projectWith(IDIYComponent<?>... components) {
    Project project = new Project();
    project.getComponents().addAll(List.of(components));
    return project;
  }

  private static AiEditScript wireScript(String fromTerminal, String toTerminal) {
    AiEditOperation operation = new AiEditOperation();
    operation.setAction("add");
    operation.setComponentType("connectivity.HookupWire");
    operation.setComponentName("W1");
    operation.setFromTerminal(fromTerminal);
    operation.setToTerminal(toTerminal);

    AiEditScript script = new AiEditScript();
    script.setOperations(List.of(operation));
    return script;
  }

  private static AiEditOperation operation(String action, String componentName) {
    AiEditOperation operation = new AiEditOperation();
    operation.setAction(action);
    operation.setComponentName(componentName);
    return operation;
  }

  private static AiEditScript script(AiEditOperation... operations) {
    AiEditScript script = new AiEditScript();
    script.setOperations(List.of(operations));
    return script;
  }

  private static AxialFilmCapacitor capacitor(Capacitance value) {
    AxialFilmCapacitor capacitor = new AxialFilmCapacitor();
    capacitor.setName("C1");
    capacitor.setValue(value);
    return capacitor;
  }

  private static AiEditScriptEditor applySeries(IDIYComponent<?> component, String series) {
    AiEditOperation operation = operation("modify", component.getName());
    operation.setDatasheetModel(series);
    AiEditScriptEditor editor = new AiEditScriptEditor(script(operation));
    editor.edit(projectWith(component), new HashSet<IDIYComponent<?>>());
    return editor;
  }

  private static AiPoint point(int gridX, int gridY) {
    return new AiPoint(BigDecimal.valueOf(gridX), BigDecimal.valueOf(gridY));
  }

  private static HookupWire findWire(Project project) {
    for (IDIYComponent<?> component : project.getComponents()) {
      if (component instanceof HookupWire wire) {
        return wire;
      }
    }
    return null;
  }
}
