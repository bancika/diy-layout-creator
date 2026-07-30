package org.diylc.components.guitar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import org.diylc.components.guitar.AbstractGuitarPickup.Polarity;
import org.diylc.components.guitar.pickup.CoilDefinition;
import org.diylc.components.guitar.pickup.CoilSide;
import org.diylc.components.guitar.pickup.LeadSpec;
import org.diylc.components.guitar.pickup.LeadType;
import org.diylc.components.guitar.pickup.MagneticPolarity;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionSnapshot;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.components.guitar.pickup.TerminalDefinition;
import org.diylc.components.guitar.pickup.TerminalRole;

/**
 * Verifies control-point -&gt; terminal resolution added in
 * {@link AbstractGuitarPickup#getTerminalForControlPoint(int)}. Crucially, this must bind purely
 * on magnetic polarity and start/finish role - never on pole-piece construction (screw/slug) - and
 * must never change existing analyser-facing behaviour ({@code getControlPointNodeName}).
 */
public class PickupTerminalResolutionTest {

  private static PickupDefinition fourConductorHumbucker() {
    // Deliberately uses SCREW on the SOUTH coil and SLUG on the NORTH coil - the opposite of the
    // common assumption - to prove the resolver does not infer polarity from pole-piece type.
    CoilDefinition north = new CoilDefinition("coilA", CoilSide.SIDE_A, null, MagneticPolarity.NORTH, null, null, null);
    CoilDefinition south = new CoilDefinition("coilB", CoilSide.SIDE_B, null, MagneticPolarity.SOUTH, null, null, null);
    TerminalDefinition northStart = new TerminalDefinition("coilA.start", "coilA", TerminalRole.START,
        new LeadSpec(LeadType.INSULATED, "Red", "#D0202A", null));
    TerminalDefinition northFinish = new TerminalDefinition("coilA.finish", "coilA", TerminalRole.FINISH,
        new LeadSpec(LeadType.INSULATED, "Green", "#1E8449", null));
    TerminalDefinition southFinish = new TerminalDefinition("coilB.finish", "coilB", TerminalRole.FINISH,
        new LeadSpec(LeadType.INSULATED, "White", "#F2F2F2", null));
    TerminalDefinition southStart = new TerminalDefinition("coilB.start", "coilB", TerminalRole.START,
        new LeadSpec(LeadType.INSULATED, "Black", "#202020", null));
    TerminalDefinition shield = new TerminalDefinition("shield", null, TerminalRole.SHIELD,
        new LeadSpec(LeadType.BARE, "Bare", "#B87333", null));

    return new PickupDefinition("test:humbucker", 1, "Acme", "Test Humbucker", "Bridge", PickupFormat.HUMBUCKER,
        false, 6, List.of(north, south), List.of(northStart, northFinish, southFinish, southStart, shield), null,
        null, null, null);
  }

  private static PickupDefinition singleCoil() {
    CoilDefinition coil = new CoilDefinition("coil", null, null, MagneticPolarity.UNKNOWN, null, null, null);
    TerminalDefinition start = new TerminalDefinition("coil.start", "coil", TerminalRole.START,
        new LeadSpec(LeadType.INSULATED, "Black", "#202020", null));
    TerminalDefinition finish = new TerminalDefinition("coil.finish", "coil", TerminalRole.FINISH,
        new LeadSpec(LeadType.INSULATED, "White", "#F2F2F2", null));
    return new PickupDefinition("test:single-coil", 1, "Acme", "Test Single Coil", "Bridge", PickupFormat.SINGLE_COIL,
        false, 6, List.of(coil), List.of(start, finish), null, null, null, null);
  }

  @Test
  public void testHumbuckerBindsByPolarityNotPolePieceType() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(fourConductorHumbucker()));

    assertEquals("coilA.start", pickup.getTerminalForControlPoint(0).id());
    assertEquals("coilA.finish", pickup.getTerminalForControlPoint(1).id());
    assertEquals("coilB.finish", pickup.getTerminalForControlPoint(2).id());
    assertEquals("coilB.start", pickup.getTerminalForControlPoint(3).id());

    // existing analyser semantics must be completely untouched by applying a definition.
    assertEquals("North Start", pickup.getControlPointNodeName(0));
    assertEquals("North Finish", pickup.getControlPointNodeName(1));
    assertEquals("South Finish", pickup.getControlPointNodeName(2));
    assertEquals("South Start", pickup.getControlPointNodeName(3));
  }

  @Test
  public void testLeadSpecResolutionMatchesTerminal() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(fourConductorHumbucker()));

    assertEquals("#D0202A", pickup.getLeadSpecForControlPoint(0).baseColour());
    assertEquals("#1E8449", pickup.getLeadSpecForControlPoint(1).baseColour());
    assertEquals("#F2F2F2", pickup.getLeadSpecForControlPoint(2).baseColour());
    assertEquals("#202020", pickup.getLeadSpecForControlPoint(3).baseColour());
  }

  @Test
  public void testSingleCoilHumbuckingModeUsesFourPointConvention() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setPolarity(Polarity.Humbucking);
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(fourConductorHumbucker()));

    assertEquals("coilA.start", pickup.getTerminalForControlPoint(0).id());
    assertEquals("coilB.start", pickup.getTerminalForControlPoint(3).id());
  }

  @Test
  public void testSingleCoilTwoTerminalModeBindsToTheSingleCoil() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setPolarity(Polarity.North);
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(singleCoil()));

    assertEquals("coil.start", pickup.getTerminalForControlPoint(1).id());
    assertEquals("coil.finish", pickup.getTerminalForControlPoint(2).id());
    // points 0 and 3 are not active/sticky terminals in this mode.
    assertNull(pickup.getTerminalForControlPoint(0));
    assertNull(pickup.getTerminalForControlPoint(3));

    // applying this generic-polarity definition must not silently change the pickup's own
    // polarity property or its existing analyser naming.
    assertEquals(Polarity.North, pickup.getPolarity());
    assertEquals("North Start", pickup.getControlPointNodeName(1));
    assertEquals("North Finish", pickup.getControlPointNodeName(2));
  }

  @Test
  public void testAmbiguousHumbuckerCoilsFallBackToGeneric() {
    // two coils, both claiming NORTH - cannot be disambiguated, so resolution must back off.
    CoilDefinition a = new CoilDefinition("a", null, null, MagneticPolarity.NORTH, null, null, null);
    CoilDefinition b = new CoilDefinition("b", null, null, MagneticPolarity.NORTH, null, null, null);
    PickupDefinition ambiguous = new PickupDefinition("test:ambiguous", null, "Acme", "Ambiguous", null,
        PickupFormat.HUMBUCKER, null, null, List.of(a, b), List.of(), null, null, null, null);

    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(ambiguous));

    for (int i = 0; i < 4; i++) {
      assertNull("index " + i + " should not resolve when coil polarity is ambiguous",
          pickup.getTerminalForControlPoint(i));
    }
  }

  @Test
  public void testNoDefinitionAppliedReturnsNullEverywhere() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    for (int i = 0; i < pickup.getControlPointCount(); i++) {
      assertNull(pickup.getTerminalForControlPoint(i));
      assertNull(pickup.getLeadSpecForControlPoint(i));
    }
    assertNull(pickup.getAppliedDefinition());
    assertNull(pickup.getPickupDefinitionId());
  }
}
