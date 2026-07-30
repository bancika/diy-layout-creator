package org.diylc.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupLibrary;
import org.diylc.components.guitar.pickup.PickupReplacementService;
import org.diylc.components.guitar.pickup.PickupReplacementService.ReplacementPlan;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Step 8 regression: a single-coil -&gt; humbucker -&gt; single-coil round trip, using the real
 * built-in library seed data (Bare Knuckle "The Mule" / "Mother's Milk") and the exact same
 * {@link ApplyPickupDefinitionEditor} + {@link PickupReplacementService} pipeline the
 * "Select Pickup from Library..." action drives. This is the automated stand-in for the task's
 * "manually verify one single-coil -&gt; humbucker -&gt; single-coil round trip" step; see the
 * completion report for the equivalent manual, in-app steps.
 */
public class PickupRoundTripRegressionTest {

  @Test
  public void testSingleCoilToHumbuckerToSingleCoilRoundTripPreservesConnectionsAndIdentity() {
    PickupLibrary library = PickupLibrary.getInstance();
    PickupDefinition mule = library.findById("bareknuckle:the-mule-bridge");
    PickupDefinition mothersMilk = library.findById("bareknuckle:mothers-milk-bridge");
    assertTrue("built-in seed data must include the Bare Knuckle Mule", mule != null);
    assertTrue("built-in seed data must include Bare Knuckle Mother's Milk", mothersMilk != null);

    // 1. Start with a plain single coil, as if just dragged onto the canvas, with its two leads
    // ("North Start"/"North Finish", the default polarity) already wired to something.
    SingleCoilPickup original = new SingleCoilPickup();
    original.setName("PKP1");
    java.util.UUID originalId = original.getId();

    Project project = new Project();
    project.getComponents().add(original);

    Point2D hotLocation = (Point2D) original.getControlPoint(1).clone();
    Point2D groundLocation = (Point2D) original.getControlPoint(2).clone();
    HookupWire hotWire = new HookupWire();
    hotWire.setControlPoint(hotLocation, 0);
    HookupWire groundWire = new HookupWire();
    groundWire.setControlPoint(groundLocation, 0);
    project.getComponents().add(hotWire);
    project.getComponents().add(groundWire);

    PickupReplacementService replacementService = new PickupReplacementService();

    // 2. Select "The Mule" (humbucker) from the library. Only two leads were ever wired, both on
    // the North side, so this must be seamless: no confirmation required.
    ReplacementPlan toHumbucker = replacementService.plan(project, original, mule);
    assertTrue("expected a seamless replacement to the humbucker: " + toHumbucker.unmatchedConnections(),
        toHumbucker.unmatchedConnections().isEmpty());

    Set<IDIYComponent<?>> selection = new HashSet<>();
    selection.add(original);
    Set<IDIYComponent<?>> afterFirstReplace =
        new ApplyPickupDefinitionEditor(mule).edit(project, selection);

    assertEquals(1, afterFirstReplace.size());
    IDIYComponent<?> asHumbucker = afterFirstReplace.iterator().next();
    assertTrue(asHumbucker instanceof HumbuckerPickup);
    HumbuckerPickup humbucker = (HumbuckerPickup) asHumbucker;
    assertEquals("bareknuckle:the-mule-bridge", humbucker.getPickupDefinitionId());
    assertEquals("PKP1", humbucker.getName());
    assertEquals(originalId, humbucker.getId());
    // both original leads are still exactly where they were - still connected.
    assertEquals(hotLocation, humbucker.getControlPoint(0));
    assertEquals(groundLocation, humbucker.getControlPoint(1));
    assertEquals(hotLocation, hotWire.getControlPoint(0));
    assertEquals(groundLocation, groundWire.getControlPoint(0));

    // 3. Select "Mother's Milk" (single coil) from the library, going back. Again the only wired
    // terminals (North Start/Finish) exist on both sides, so still seamless.
    ReplacementPlan toSingleCoil = replacementService.plan(project, humbucker, mothersMilk);
    assertTrue("expected a seamless replacement back to a single coil: " + toSingleCoil.unmatchedConnections(),
        toSingleCoil.unmatchedConnections().isEmpty());

    Set<IDIYComponent<?>> secondSelection = new HashSet<>();
    secondSelection.add(humbucker);
    Set<IDIYComponent<?>> afterSecondReplace =
        new ApplyPickupDefinitionEditor(mothersMilk).edit(project, secondSelection);

    assertEquals(1, afterSecondReplace.size());
    IDIYComponent<?> finalComponent = afterSecondReplace.iterator().next();
    assertTrue(finalComponent instanceof SingleCoilPickup);
    SingleCoilPickup finalPickup = (SingleCoilPickup) finalComponent;

    // identity, name and both original connections all survived the full round trip.
    assertEquals(originalId, finalPickup.getId());
    assertEquals("PKP1", finalPickup.getName());
    assertEquals("bareknuckle:mothers-milk-bridge", finalPickup.getPickupDefinitionId());
    assertEquals(hotLocation, finalPickup.getControlPoint(1));
    assertEquals(groundLocation, finalPickup.getControlPoint(2));
    assertEquals(hotLocation, hotWire.getControlPoint(0));
    assertEquals(groundLocation, groundWire.getControlPoint(0));

    // exactly one live pickup component in the project throughout - no leftover/duplicate
    // components from either replacement.
    long pickupCount = project.getComponents().stream()
        .filter(c -> c instanceof org.diylc.components.guitar.AbstractGuitarPickup).count();
    assertEquals(1, pickupCount);
  }
}
