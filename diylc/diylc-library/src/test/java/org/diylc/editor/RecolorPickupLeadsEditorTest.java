package org.diylc.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.pickup.CoilDefinition;
import org.diylc.components.guitar.pickup.LeadSpec;
import org.diylc.components.guitar.pickup.LeadType;
import org.diylc.components.guitar.pickup.MagneticPolarity;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionSnapshot;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.components.guitar.pickup.TerminalDefinition;
import org.diylc.components.guitar.pickup.TerminalRole;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Covers the "Recolor Wires to Pickup Colors" action's editor: it is how a user asks for
 * already-attached leads to pick up a newly-selected pickup definition's colours, since selecting
 * a definition deliberately never recolours existing wires by itself (see
 * {@code PickupSelectionModel}/{@code SelectPickupFromLibraryAction} docs).
 */
public class RecolorPickupLeadsEditorTest {

  private static PickupDefinition humbuckerDefinitionWithColors() {
    CoilDefinition coilA =
        new CoilDefinition("coilA", null, null, MagneticPolarity.NORTH, null, null, null);
    CoilDefinition coilB =
        new CoilDefinition("coilB", null, null, MagneticPolarity.SOUTH, null, null, null);
    List<TerminalDefinition> terminals = List.of(
        new TerminalDefinition("coilA.start", "coilA", TerminalRole.START,
            new LeadSpec(LeadType.INSULATED, "Red", "#FF0000", null)),
        new TerminalDefinition("coilA.finish", "coilA", TerminalRole.FINISH,
            new LeadSpec(LeadType.INSULATED, "Green", "#00FF00", null)),
        new TerminalDefinition("coilB.finish", "coilB", TerminalRole.FINISH,
            new LeadSpec(LeadType.INSULATED, "White", "#FFFFFF", null)),
        new TerminalDefinition("coilB.start", "coilB", TerminalRole.START,
            new LeadSpec(LeadType.INSULATED, "Black", "#000000", null)));
    return new PickupDefinition("test:humbucker-colors", 1, "Acme", "Test Humbucker", "Bridge",
        PickupFormat.HUMBUCKER, false, 6, List.of(coilA, coilB), terminals, null, null, null, null);
  }

  @Test
  public void testRecolorsWiresAttachedToEachMatchedTerminal() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(humbuckerDefinitionWithColors()));

    Project project = new Project();
    project.getComponents().add(pickup);

    HookupWire northStartWire = new HookupWire();
    northStartWire.setControlPoint((Point2D) pickup.getControlPoint(0).clone(), 0);
    northStartWire.setLeadColor(Color.BLUE);
    HookupWire southStartWire = new HookupWire();
    southStartWire.setControlPoint((Point2D) pickup.getControlPoint(3).clone(), 0);
    southStartWire.setLeadColor(Color.BLUE);
    project.getComponents().add(northStartWire);
    project.getComponents().add(southStartWire);

    Set<IDIYComponent<?>> result = new RecolorPickupLeadsEditor(pickup).edit(project, Set.of());

    assertEquals(Color.decode("#FF0000"), northStartWire.getLeadColor());
    assertEquals(Color.decode("#000000"), southStartWire.getLeadColor());
    assertTrue(result.contains(pickup));
    assertTrue(result.contains(northStartWire));
    assertTrue(result.contains(southStartWire));
  }

  @Test
  public void testDoesNotTouchWiresNotAttachedToThePickup() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(humbuckerDefinitionWithColors()));

    Project project = new Project();
    project.getComponents().add(pickup);

    HookupWire unrelatedWire = new HookupWire();
    unrelatedWire.setControlPoint(new Point2D.Double(10_000, 10_000), 0);
    unrelatedWire.setLeadColor(Color.BLUE);
    project.getComponents().add(unrelatedWire);

    new RecolorPickupLeadsEditor(pickup).edit(project, Set.of());

    assertEquals(Color.BLUE, unrelatedWire.getLeadColor());
  }

  @Test
  public void testLeavesWireUntouchedWhenPickupHasNoDefinitionApplied() {
    // No definition applied at all - getDefaultLeadColor() returns null for every terminal, so
    // every attached wire must be left exactly as it was (never guess a colour).
    HumbuckerPickup pickup = new HumbuckerPickup();

    Project project = new Project();
    project.getComponents().add(pickup);

    HookupWire wire = new HookupWire();
    wire.setControlPoint((Point2D) pickup.getControlPoint(0).clone(), 0);
    wire.setLeadColor(Color.BLUE);
    project.getComponents().add(wire);

    new RecolorPickupLeadsEditor(pickup).edit(project, Set.of());

    assertEquals(Color.BLUE, wire.getLeadColor());
  }

  @Test
  public void testWorksForTwoTerminalSingleCoilPickupsToo() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    // Single coil format terminals: coil.finish only supplies a colour in this fixture (mirrors
    // the built-in library's own generic single-coil template).
    List<TerminalDefinition> terminals = List.of(new TerminalDefinition("coil.finish", "coil", TerminalRole.FINISH,
        new LeadSpec(LeadType.INSULATED, "White", "#F2F2F2", null)));
    CoilDefinition coil = new CoilDefinition("coil", null, null, MagneticPolarity.UNKNOWN, null, null, null);
    PickupDefinition definition = new PickupDefinition("test:single-colors", 1, "Acme", "Test Single", "Bridge",
        PickupFormat.SINGLE_COIL, false, 6, List.of(coil), terminals, null, null, null, null);
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(definition));

    Project project = new Project();
    project.getComponents().add(pickup);

    HookupWire groundWire = new HookupWire();
    groundWire.setControlPoint((Point2D) pickup.getControlPoint(2).clone(), 0);
    groundWire.setLeadColor(Color.BLUE);
    project.getComponents().add(groundWire);

    new RecolorPickupLeadsEditor(pickup).edit(project, Set.of());

    assertEquals(Color.decode("#F2F2F2"), groundWire.getLeadColor());
  }

  @Test
  public void testNoAttachedWiresYieldsEmptyRecoloredSetBesidesThePickupItself() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(humbuckerDefinitionWithColors()));
    Project project = new Project();
    project.getComponents().add(pickup);

    Set<IDIYComponent<?>> result = new RecolorPickupLeadsEditor(pickup).edit(project, Set.of());

    assertEquals(Set.of(pickup), result);
    assertFalse(result.isEmpty());
  }
}
