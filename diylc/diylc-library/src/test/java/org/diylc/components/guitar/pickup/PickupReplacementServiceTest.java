package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

import org.junit.Test;

import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.guitar.AbstractGuitarPickup.Polarity;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.P90Pickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.pickup.PickupReplacementService.ReplacementPlan;
import org.diylc.common.Percentage;
import org.diylc.core.Project;

/**
 * Covers the cross-type ("different component type") replacement rules: correct target
 * component class, connection preservation by semantic terminal name (never raw index),
 * required confirmation for unmatched connected terminals, seamless replacement when
 * unconnected, and preservation of name/anchor/orientation/label styling/alpha/group membership.
 */
public class PickupReplacementServiceTest {

  private final PickupReplacementService service = new PickupReplacementService();

  private static PickupDefinition humbuckerDefinition() {
    return new PickupDefinition("test:humbucker", 1, "Acme", "Test Humbucker", "Bridge", PickupFormat.HUMBUCKER,
        false, 6, null, null, null, null, null, null);
  }

  private static PickupDefinition p90Definition() {
    return new PickupDefinition("test:p90", 1, "Acme", "Test P90", "Bridge", PickupFormat.P90, false, 6, null, null,
        null, null, null, null);
  }

  @Test
  public void testCrossTypeReplacementCreatesCorrectComponentClass() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    HumbuckerPickup replacement = (HumbuckerPickup) service.replace(project, pickup, humbuckerDefinition());

    assertEquals(1, project.getComponents().size());
    assertEquals(replacement, project.getComponents().get(0));
    assertEquals("test:humbucker", replacement.getPickupDefinitionId());
  }

  @Test
  public void testUnconnectedPickupReplacementIsSeamless() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    ReplacementPlan plan = service.plan(project, pickup, humbuckerDefinition());

    assertFalse(plan.sameType());
    assertTrue(plan.unmatchedConnections().isEmpty());
    assertFalse(plan.requiresConfirmation());
  }

  @Test
  public void testCompatibleConnectionsArePreservedAcrossSingleCoilFamily() {
    // Single coil (default North polarity) has its two leads at control points 1 and 2.
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    Point2D hotLocation = (Point2D) pickup.getControlPoint(1).clone();
    Point2D groundLocation = (Point2D) pickup.getControlPoint(2).clone();

    HookupWire hotWire = new HookupWire();
    hotWire.setControlPoint(hotLocation, 0);
    HookupWire groundWire = new HookupWire();
    groundWire.setControlPoint(groundLocation, 0);
    project.getComponents().add(hotWire);
    project.getComponents().add(groundWire);

    // P90 is also a 2-terminal, non-humbucking pickup - seeding it with the old pickup's (North)
    // polarity means its own "North Start"/"North Finish" terminals match the old ones exactly,
    // so replacement should be seamless (no confirmation) and both wires should stay connected.
    ReplacementPlan plan = service.plan(project, pickup, p90Definition());
    assertTrue("expected no unmatched connections for a same-polarity single-coil-family swap: "
        + plan.unmatchedConnections(), plan.unmatchedConnections().isEmpty());

    P90Pickup replacement = (P90Pickup) service.replace(project, pickup, p90Definition());

    assertEquals(hotLocation, replacement.getControlPoint(1));
    assertEquals(groundLocation, replacement.getControlPoint(2));
    // the wires themselves were never touched.
    assertEquals(hotLocation, hotWire.getControlPoint(0));
    assertEquals(groundLocation, groundWire.getControlPoint(0));
  }

  @Test
  public void testIncompatibleConnectedReplacementRequiresConfirmation() {
    // A humbucker has 4 terminals (North/South Start/Finish); replacing it with a P90 (2
    // terminals) can only ever preserve the North pair - anything attached to the South pair
    // must be flagged as unmatched-and-connected.
    HumbuckerPickup pickup = new HumbuckerPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    Point2D southFinishLocation = (Point2D) pickup.getControlPoint(2).clone();
    HookupWire southWire = new HookupWire();
    southWire.setControlPoint(southFinishLocation, 0);
    project.getComponents().add(southWire);

    ReplacementPlan plan = service.plan(project, pickup, p90Definition());

    assertTrue(plan.requiresConfirmation());
    assertEquals(1, plan.unmatchedConnections().size());
    assertEquals("South Finish", plan.unmatchedConnections().get(0).terminalName());
  }

  @Test
  public void testSameTypePlanNeverRequiresConfirmation() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    Project project = new Project();
    project.getComponents().add(pickup);
    // connect every terminal, so if same-type detection were broken this would surely show up as
    // "unmatched".
    for (int i = 0; i < pickup.getControlPointCount(); i++) {
      HookupWire wire = new HookupWire();
      wire.setControlPoint((Point2D) pickup.getControlPoint(i).clone(), 0);
      project.getComponents().add(wire);
    }

    ReplacementPlan plan = service.plan(project, pickup, humbuckerDefinition());

    assertTrue(plan.sameType());
    assertTrue(plan.unmatchedConnections().isEmpty());
    assertFalse(plan.requiresConfirmation());
  }

  @Test
  public void testPreservesNameOrientationLabelStylingAlphaAndListPosition() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setName("PKP7");
    pickup.setOrientation(org.diylc.common.Orientation._90);
    pickup.setLabelColor(Color.decode("#ABCDEF"));
    pickup.setFontSizeOverride(18);
    pickup.setAlpha(new Percentage(42));
    // SingleCoilPickup's control point 0 is not itself a real/sticky terminal (only 1 and 2 are,
    // for its default North polarity); its "North Start" - the terminal a same-family P90
    // replacement actually matches by name - is control point 1.
    Point2D oldNorthStart = (Point2D) pickup.getControlPoint(1).clone();

    Project project = new Project();
    HookupWire before = new HookupWire();
    HookupWire after = new HookupWire();
    project.getComponents().add(before);
    project.getComponents().add(pickup);
    project.getComponents().add(after);

    P90Pickup replacement = (P90Pickup) service.replace(project, pickup, p90Definition());

    assertEquals("PKP7", replacement.getName());
    // The plain, unadjusted Orientation value - the same "rotate" concept a user sets on any
    // DIYLC component, and the same value that governs the new component's own body rendering
    // (see PickupReplacementService.replace()'s comment on why this is a raw copy, not derived).
    assertEquals(org.diylc.common.Orientation._90, replacement.getOrientation());
    assertEquals(Color.decode("#ABCDEF"), replacement.getLabelColor());
    assertEquals(Integer.valueOf(18), replacement.getFontSizeOverride());
    assertEquals(new Percentage(42), replacement.getAlpha());
    assertEquals(oldNorthStart, replacement.getControlPoint(1));
    // list position (i.e. layer/z-order) preserved exactly: still the middle element.
    assertEquals(List.of(before, replacement, after), project.getComponents());
  }

  @Test
  public void testMatchedTerminalCoordinateTakesPriorityOverRawAnchorWhenTheyDiverge() {
    // Edge case: HumbuckerPickup's control point 0 is itself the "North Start" terminal, so when
    // replacing a single coil (whose own "North Start" lives at point 1, not point 0) the task's
    // priority - preserve connections via matching semantic terminal identity - wins over simply
    // copying the old raw control point 0 coordinate. The resulting shift in the pickup's overall
    // reference point is small (one terminal spacing) and is the correct trade-off: it is what
    // keeps any wire attached to the old "North Start" lead genuinely still touching the new
    // pickup's "North Start" lead.
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);
    Point2D oldNorthStart = (Point2D) pickup.getControlPoint(1).clone();

    HumbuckerPickup replacement = (HumbuckerPickup) service.replace(project, pickup, humbuckerDefinition());

    assertEquals(oldNorthStart, replacement.getControlPoint(0));
    assertEquals("North Start", replacement.getControlPointNodeName(0));
  }

  @Test
  public void testPreservesGroupMembershipViaComponentId() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);
    project.getGroupsEx().add(new org.diylc.core.ComponentGroup(java.util.Set.of(pickup.getId()), null));

    HumbuckerPickup replacement = (HumbuckerPickup) service.replace(project, pickup, humbuckerDefinition());

    assertEquals(pickup.getId(), replacement.getId());
    assertTrue(project.getGroupsEx().stream().anyMatch(g -> g.getComponentIds().contains(replacement.getId())));
  }

  @Test
  public void testPreservesLockedState() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);
    project.getLockedComponents().add(pickup);

    HumbuckerPickup replacement = (HumbuckerPickup) service.replace(project, pickup, humbuckerDefinition());

    assertTrue(project.getLockedComponents().contains(replacement));
    assertFalse(project.getLockedComponents().contains(pickup));
  }

  @Test
  public void testCrossTypeReplacementOrientationMatchesFreshPlacementNotRawAxisSwap() {
    // Regression test for two related reports: (1) replacing a humbucker with a strat-style
    // single coil via the library initially left the single coil's control points vertical
    // instead of horizontal like a freshly-placed single coil - caused by the (now fixed)
    // anchoring logic remapping each matched terminal independently, which could distort the new
    // component's own internal layout; and (2) an interim fix that adjusted the Orientation value
    // itself (to compensate for HumbuckerPickup and SingleCoilPickup laying out their control
    // points on different native axes) made the replacement's Orientation *property* diverge from
    // what dragging a fresh single coil from the sidebar shows, which is itself a regression.
    //
    // The correct fix touches only the anchoring (see the "rigid transform" comment in replace()):
    // Orientation is a plain, unadjusted copy - matching a fresh sidebar placement whenever the
    // old component was itself unrotated - and the new component's own terminals naturally end up
    // laid out along *its own* class's native axis (horizontal for a single coil), because they
    // are computed from the new component's own default layout and only translated into place, not
    // copied point-by-point from the old component's (differently laid out) coordinates.
    HumbuckerPickup pickup = new HumbuckerPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    SingleCoilPickup replacement = (SingleCoilPickup) service.replace(project, pickup,
        new PickupDefinition("test:single", 1, "Acme", "Test Single", null, PickupFormat.SINGLE_COIL, false, 6, null,
            null, null, null, null, null));

    // Same Orientation value as a freshly-placed (default, unrotated) single coil from the
    // sidebar would have.
    assertEquals(org.diylc.common.Orientation.DEFAULT, replacement.getOrientation());
    // Its two real leads ("North Start"/"North Finish", control points 1 and 2 for a single
    // coil's default North polarity) lie on a horizontal line - the same axis a freshly-placed
    // single coil uses - not a vertical one inherited from the humbucker's own native axis.
    assertEquals(replacement.getControlPoint(1).getY(), replacement.getControlPoint(2).getY(), 0.0001);
  }

  @Test
  public void testReplacementNeverAutoAssumesHumbuckingPolarityContinuity() {
    // Going from a 4-terminal humbucker to a 2-terminal single coil: there is no "old polarity"
    // to seed (humbuckers have none), so the new pickup keeps its own class default.
    HumbuckerPickup pickup = new HumbuckerPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    SingleCoilPickup replacement = (SingleCoilPickup) service.replace(project, pickup,
        new PickupDefinition("test:single", 1, "Acme", "Test Single", null, PickupFormat.SINGLE_COIL, false, 6, null,
            null, null, null, null, null));

    assertEquals(Polarity.North, replacement.getPolarity());
  }
}
