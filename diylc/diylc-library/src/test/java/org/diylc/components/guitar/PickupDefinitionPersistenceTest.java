package org.diylc.components.guitar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.diylc.components.guitar.pickup.CoilDefinition;
import org.diylc.components.guitar.pickup.CoilSide;
import org.diylc.components.guitar.pickup.LeadSpec;
import org.diylc.components.guitar.pickup.LeadType;
import org.diylc.components.guitar.pickup.MagneticPolarity;
import org.diylc.components.guitar.pickup.Measurement;
import org.diylc.components.guitar.pickup.MeasurementUnit;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionSnapshot;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.components.guitar.pickup.TerminalDefinition;
import org.diylc.components.guitar.pickup.TerminalRole;
import org.diylc.serialization.ProjectFileManager;

/**
 * Confirms the pickup definition id/snapshot survive the exact XStream serializer used to save
 * and load {@code .diy} project files, and that pickups placed before this feature existed (i.e.
 * with no snapshot at all) continue to load unchanged.
 */
public class PickupDefinitionPersistenceTest {

  private static PickupDefinition sampleDefinition() {
    CoilDefinition north = new CoilDefinition("coilA", CoilSide.SIDE_A, null, MagneticPolarity.NORTH, null,
        new Measurement(7.1, MeasurementUnit.KILOOHM), null);
    CoilDefinition south = new CoilDefinition("coilB", CoilSide.SIDE_B, null, MagneticPolarity.SOUTH, null, null,
        null);
    TerminalDefinition northStart = new TerminalDefinition("coilA.start", "coilA", TerminalRole.START,
        new LeadSpec(LeadType.INSULATED, "Red", "#D0202A", null));
    TerminalDefinition shield = new TerminalDefinition("shield", null, TerminalRole.SHIELD,
        new LeadSpec(LeadType.BARE, "Bare", "#B87333", null));
    return new PickupDefinition("bareknuckle:the-mule-bridge", 1, "Bare Knuckle Pickups", "The Mule", "Bridge",
        PickupFormat.HUMBUCKER, false, 6, List.of(north, south), List.of(northStart, shield), null, null, null,
        null);
  }

  @Test
  public void testDefinitionIdAndSnapshotSurviveProjectRoundTrip() {
    HumbuckerPickup original = new HumbuckerPickup();
    original.setValue("The Mule (Bridge)");
    PickupDefinition definition = sampleDefinition();
    original.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(definition));

    String xml = ProjectFileManager.xStreamSerializer.toXML(original);
    HumbuckerPickup restored = (HumbuckerPickup) ProjectFileManager.xStreamSerializer.fromXML(xml);

    assertEquals("bareknuckle:the-mule-bridge", restored.getPickupDefinitionId());
    assertEquals(definition, restored.getAppliedDefinition());
    assertEquals("coilA.start", restored.getTerminalForControlPoint(0).id());
    assertEquals("#D0202A", restored.getLeadSpecForControlPoint(0).baseColour());
  }

  @Test
  public void testGenericPickupWithNoDefinitionRoundTripsAndOmitsNewElements() {
    HumbuckerPickup original = new HumbuckerPickup();
    original.setValue("Generic Humbucker");

    String xml = ProjectFileManager.xStreamSerializer.toXML(original);
    // a pickup with no definition applied should serialize just like one from before this
    // feature existed - i.e. it must not mention the new fields at all.
    assertFalse(xml.contains("pickupDefinitionId"));
    assertFalse(xml.contains("pickupDefinitionSnapshot"));

    HumbuckerPickup restored = (HumbuckerPickup) ProjectFileManager.xStreamSerializer.fromXML(xml);
    assertNull(restored.getPickupDefinitionId());
    assertNull(restored.getAppliedDefinition());
    assertEquals("Generic Humbucker", restored.getValue());
    // analysis-facing behaviour is completely unaffected.
    assertEquals("North Start", restored.getControlPointNodeName(0));
    assertEquals("South Start", restored.getControlPointNodeName(3));
    assertTrue(restored.isHumbucker());
  }

  @Test
  public void testOldStyleXmlWithoutNewElementsStillLoads() {
    // Simulates a .diy file saved before this feature existed by taking a real serialization and
    // stripping out the (already-absent, since they were null) new elements is not meaningful -
    // instead we build the "old" XML from scratch using only fields that predate this feature,
    // mirroring exactly what a pre-existing project file looks like: no pickupDefinitionId/
    // pickupDefinitionSnapshot elements, only legacy fields.
    String legacyXml = "<diylc.guitar.HumbuckerPickup>\n"
        + "  <value>Legacy Humbucker</value>\n"
        + "  <orientation>DEFAULT</orientation>\n"
        + "  <controlPoints>\n"
        + "    <point x=\"0.0\" y=\"0.0\"/>\n"
        + "    <point x=\"0.0\" y=\"0.1\"/>\n"
        + "    <point x=\"0.0\" y=\"0.2\"/>\n"
        + "    <point x=\"0.0\" y=\"0.3\"/>\n"
        + "  </controlPoints>\n"
        + "  <polarity>Humbucking</polarity>\n"
        + "</diylc.guitar.HumbuckerPickup>";

    HumbuckerPickup restored = (HumbuckerPickup) ProjectFileManager.xStreamSerializer.fromXML(legacyXml);

    assertEquals("Legacy Humbucker", restored.getValue());
    assertNull(restored.getPickupDefinitionId());
    assertNull(restored.getAppliedDefinition());
    assertEquals("North Start", restored.getControlPointNodeName(0));
    assertTrue(restored.isHumbucker());
  }
}
