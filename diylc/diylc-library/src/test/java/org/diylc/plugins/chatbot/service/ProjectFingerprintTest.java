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
import static org.junit.Assert.assertNotEquals;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.passive.Resistor;
import org.diylc.core.Project;
import org.diylc.plugins.chatbot.model.AiProject;

/**
 * An edit script is only applied while the project fingerprint still matches the one taken when the
 * prompt was sent, so the fingerprint has to change with anything the script depends on and with
 * nothing else. A fingerprint that drifted on its own would make every proposal look stale.
 *
 * @author Branislav Stojkovic
 */
public class ProjectFingerprintTest {

  @Test
  public void unchangedProjectKeepsItsFingerprint() {
    Project project = project();

    assertEquals(fingerprint(project), fingerprint(project));
  }

  @Test
  public void movingAComponentChangesTheFingerprint() {
    Project project = project();
    String before = fingerprint(project);

    project.getComponents().get(0).setControlPoint(new Point2D.Double(300, 100), 1);

    assertNotEquals(before, fingerprint(project));
  }

  @Test
  public void renamingAComponentChangesTheFingerprint() {
    Project project = project();
    String before = fingerprint(project);

    project.getComponents().get(0).setName("R2");

    assertNotEquals(before, fingerprint(project));
  }

  @Test
  public void deletingAComponentChangesTheFingerprint() {
    Project project = project();
    String before = fingerprint(project);

    project.getComponents().remove(1);

    assertNotEquals(before, fingerprint(project));
  }

  @Test
  public void cosmeticChangeKeepsTheFingerprint() {
    Project project = project();
    String before = fingerprint(project);

    ((Resistor) project.getComponents().get(0)).setBodyColor(Color.red);

    assertEquals(before, fingerprint(project));
  }

  @Test
  public void orderOfNetsAndTheirMembersDoesNotMatter() {
    Map<String, Set<String>> nets = new LinkedHashMap<>();
    nets.put("N001", new LinkedHashSet<>(List.of("R1.0", "W1.0")));
    nets.put("N002", new LinkedHashSet<>(List.of("R1.1", "C1.0")));
    Map<String, Set<String>> reordered = new LinkedHashMap<>();
    reordered.put("N001", new LinkedHashSet<>(List.of("C1.0", "R1.1")));
    reordered.put("N002", new LinkedHashSet<>(List.of("W1.0", "R1.0")));

    assertEquals(ChatbotService.fingerprint(aiProject(nets)),
        ChatbotService.fingerprint(aiProject(reordered)));
  }

  private static Project project() {
    Resistor resistor = new Resistor();
    resistor.setName("R1");
    resistor.setControlPoint(new Point2D.Double(100, 100), 0);
    resistor.setControlPoint(new Point2D.Double(200, 100), 1);

    HookupWire wire = new HookupWire();
    wire.setName("W1");
    wire.stretchBetween(new Point2D.Double(200, 100), new Point2D.Double(200, 200));

    Project project = new Project();
    project.getComponents().add(resistor);
    project.getComponents().add(wire);
    return project;
  }

  private static String fingerprint(Project project) {
    return ChatbotService.fingerprint(
        AiProjectBuilder.build(project, new ArrayList<>(), c -> null, List.of()));
  }

  private static AiProject aiProject(Map<String, Set<String>> nets) {
    return new AiProject(Map.of(), List.of(), null, List.of(), nets, List.of());
  }
}
