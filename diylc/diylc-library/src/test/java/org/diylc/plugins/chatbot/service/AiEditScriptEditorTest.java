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

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.electromechanical.ClosedJack1_4;
import org.diylc.components.passive.Resistor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;

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

  private static Project projectWith(IDIYComponent<?> component) {
    Project project = new Project();
    project.getComponents().add(component);
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

  private static HookupWire findWire(Project project) {
    for (IDIYComponent<?> component : project.getComponents()) {
      if (component instanceof HookupWire wire) {
        return wire;
      }
    }
    return null;
  }
}
