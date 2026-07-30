package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.awt.Color;
import java.util.List;

import org.junit.Test;

public class PickupDefinitionTest {

  @Test
  public void testRequiredFieldsAreEnforced() {
    assertThrows(NullPointerException.class,
        () -> new PickupDefinition(null, null, "Acme", "Model", null, PickupFormat.P90, null, null, null, null,
            null, null, null, null));
    assertThrows(NullPointerException.class,
        () -> new PickupDefinition("id", null, null, "Model", null, PickupFormat.P90, null, null, null, null, null,
            null, null, null));
    assertThrows(NullPointerException.class,
        () -> new PickupDefinition("id", null, "Acme", null, null, PickupFormat.P90, null, null, null, null, null,
            null, null, null));
    assertThrows(NullPointerException.class,
        () -> new PickupDefinition("id", null, "Acme", "Model", null, null, null, null, null, null, null, null,
            null, null));
  }

  @Test
  public void testMinimalDefinitionDefaultsListsToEmpty() {
    PickupDefinition d = new PickupDefinition("id", null, "Acme", "Model", null, PickupFormat.SINGLE_COIL, null,
        null, null, null, null, null, null, null);

    assertEquals(List.of(), d.coils());
    assertEquals(List.of(), d.terminals());
    assertEquals(List.of(), d.sources());
  }

  @Test
  public void testGetDisplayNameIncludesVariantWhenPresent() {
    PickupDefinition withVariant = new PickupDefinition("id", null, "Bare Knuckle Pickups", "The Mule", "Bridge",
        PickupFormat.HUMBUCKER, null, null, null, null, null, null, null, null);
    assertEquals("Bare Knuckle Pickups The Mule (Bridge)", withVariant.getDisplayName());

    PickupDefinition withoutVariant = new PickupDefinition("id", null, "Acme", "Basic", null,
        PickupFormat.SINGLE_COIL, null, null, null, null, null, null, null, null);
    assertEquals("Acme Basic", withoutVariant.getDisplayName());
  }

  @Test
  public void testFindCoilAndFindTerminal() {
    CoilDefinition coilA = new CoilDefinition("coilA", CoilSide.SIDE_A, null, MagneticPolarity.NORTH, null, null,
        null);
    TerminalDefinition start = new TerminalDefinition("coilA.start", "coilA", TerminalRole.START, null);

    PickupDefinition d = new PickupDefinition("id", null, "Acme", "Model", null, PickupFormat.HUMBUCKER, null, null,
        List.of(coilA), List.of(start), null, null, null, null);

    assertEquals(coilA, d.findCoil("coilA"));
    assertNull(d.findCoil("does-not-exist"));
    assertEquals(start, d.findTerminal("coilA.start"));
    assertNull(d.findTerminal("does-not-exist"));
  }

  @Test
  public void testLeadSpecParsesHexColour() {
    LeadSpec lead = new LeadSpec(LeadType.INSULATED, "Red", "#D0202A", null);
    assertEquals(Color.decode("#D0202A"), lead.toAwtColor());
  }

  @Test
  public void testLeadSpecWithNoColourReturnsNull() {
    LeadSpec lead = new LeadSpec(LeadType.UNKNOWN, null, null, null);
    assertNull(lead.toAwtColor());
  }
}
