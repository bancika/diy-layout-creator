package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.diylc.components.guitar.pickup.PickupLibraryLoader.ParsedFile;

public class PickupLibraryLoaderTest {

  private final PickupLibraryLoader loader = new PickupLibraryLoader();

  @Test
  public void testMinimalDefinitionLoads() {
    String json = """
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "test:minimal", "manufacturer": "Acme", "model": "Basic", "format": "SINGLE_COIL" }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    assertTrue("minimal definition should not produce issues: " + parsed.issues(), parsed.issues().isEmpty());
    assertEquals(1, parsed.pickups().size());
    PickupDefinition d = parsed.pickups().get(0);
    assertEquals("test:minimal", d.id());
    assertEquals("Acme", d.manufacturer());
    assertEquals("Basic", d.model());
    assertEquals(PickupFormat.SINGLE_COIL, d.format());
    assertNull(d.variant());
    assertTrue(d.coils().isEmpty());
    assertTrue(d.terminals().isEmpty());
  }

  @Test
  public void testFullOptionalDefinitionLoads() {
    String json = """
        {
          "schemaVersion": 1,
          "libraryId": "test-lib",
          "pickups": [
            {
              "id": "example:four-conductor-humbucker",
              "definitionVersion": 1,
              "manufacturer": "Example",
              "model": "Four Conductor Humbucker",
              "variant": "Bridge",
              "format": "HUMBUCKER",
              "active": false,
              "stringCount": 6,
              "coils": [
                { "id": "slug", "localSide": "SIDE_A", "polePieceType": "SLUG", "magneticPolarity": "NORTH",
                  "windingDirection": "UNKNOWN",
                  "dcResistance": { "value": 7.1, "unit": "KILOOHM" },
                  "inductance": { "value": 3.2, "unit": "HENRY" } },
                { "id": "screw", "localSide": "SIDE_B", "polePieceType": "SCREW", "magneticPolarity": "SOUTH",
                  "windingDirection": "UNKNOWN" }
              ],
              "terminals": [
                { "id": "slug.start", "coilId": "slug", "role": "START",
                  "lead": { "type": "INSULATED", "displayName": "Red", "baseColour": "#D02020" } },
                { "id": "slug.finish", "coilId": "slug", "role": "FINISH",
                  "lead": { "type": "INSULATED", "displayName": "Green", "baseColour": "#17853A" } },
                { "id": "screw.finish", "coilId": "screw", "role": "FINISH",
                  "lead": { "type": "INSULATED", "displayName": "White", "baseColour": "#F2F2F2" } },
                { "id": "screw.start", "coilId": "screw", "role": "START",
                  "lead": { "type": "INSULATED", "displayName": "Black", "baseColour": "#202020" } },
                { "id": "shield", "role": "SHIELD", "lead": { "type": "BARE", "displayName": "Bare", "baseColour": "#B87333" } }
              ],
              "electrical": {
                "seriesDcResistance": { "value": 14.2, "unit": "KILOOHM" },
                "seriesInductance": { "value": 6.4, "unit": "HENRY" },
                "parasiticCapacitance": { "value": 110, "unit": "PICOFARAD" }
              },
              "magnet": { "material": "ALNICO", "grade": "V" },
              "physical": {
                "width": { "value": 38.0, "unit": "MILLIMETRE" },
                "length": { "value": 69.0, "unit": "MILLIMETRE" },
                "depth": { "value": 22.0, "unit": "MILLIMETRE" },
                "poleSpacing": { "value": 50.0, "unit": "MILLIMETRE" },
                "mountingLeg": "SHORT"
              },
              "sources": [ { "type": "MANUFACTURER", "reference": "Example specification" } ]
            }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    assertTrue("full definition should not produce issues: " + parsed.issues(), parsed.issues().isEmpty());
    assertEquals(1, parsed.pickups().size());
    PickupDefinition d = parsed.pickups().get(0);
    assertEquals(2, d.coils().size());
    assertEquals(5, d.terminals().size());
    assertEquals(MagneticPolarity.NORTH, d.findCoil("slug").magneticPolarity());
    assertEquals(MagneticPolarity.SOUTH, d.findCoil("screw").magneticPolarity());
    assertEquals("#D02020", d.findTerminal("slug.start").lead().baseColour());
    assertEquals(14.2, d.electrical().seriesDcResistance().value(), 0.0001);
    assertEquals(MeasurementUnit.KILOOHM, d.electrical().seriesDcResistance().unit());
    assertEquals("ALNICO", d.magnet().material());
    assertEquals(MountingLeg.SHORT, d.physical().mountingLeg());
  }

  @Test
  public void testMissingOptionalFieldsAreAccepted() {
    String json = """
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "test:no-electrical", "manufacturer": "Acme", "model": "Basic", "format": "P90",
              "coils": [ { "id": "coil" } ],
              "terminals": [ { "id": "coil.finish", "coilId": "coil" } ] }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    assertTrue(parsed.issues().isEmpty());
    PickupDefinition d = parsed.pickups().get(0);
    assertNull(d.electrical());
    assertNull(d.magnet());
    assertNull(d.physical());
    assertNull(d.findCoil("coil").magneticPolarity());
    assertNull(d.findTerminal("coil.finish").role());
  }

  @Test
  public void testUnknownFieldsAreIgnored() {
    String json = """
        {
          "schemaVersion": 1,
          "somethingFromTheFuture": { "nested": true },
          "pickups": [
            { "id": "test:unknown-fields", "manufacturer": "Acme", "model": "Basic", "format": "SINGLE_COIL",
              "futureField": "should be ignored",
              "coils": [ { "id": "coil", "futureCoilField": 42 } ] }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    assertTrue("unknown fields must not cause issues: " + parsed.issues(), parsed.issues().isEmpty());
    assertEquals(1, parsed.pickups().size());
    assertEquals("test:unknown-fields", parsed.pickups().get(0).id());
  }

  @Test
  public void testMalformedColourProducesValidationError() {
    String json = """
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "test:bad-colour", "manufacturer": "Acme", "model": "Basic", "format": "SINGLE_COIL",
              "terminals": [
                { "id": "coil.finish", "lead": { "type": "INSULATED", "baseColour": "not-a-colour" } }
              ] }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    // the entry itself still loads (identity fields are fine) - only the bad colour is dropped.
    assertEquals(1, parsed.pickups().size());
    assertNull(parsed.pickups().get(0).findTerminal("coil.finish").lead().baseColour());
    assertFalse(parsed.issues().isEmpty());
    assertTrue(parsed.issues().stream()
        .anyMatch(i -> i.fieldPath() != null && i.fieldPath().contains("baseColour")));
  }

  @Test
  public void testMalformedUnitProducesValidationError() {
    String json = """
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "test:bad-unit", "manufacturer": "Acme", "model": "Basic", "format": "HUMBUCKER",
              "electrical": { "seriesDcResistance": { "value": 8.4, "unit": "NOT_A_UNIT" } } }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    assertEquals(1, parsed.pickups().size());
    assertNull(parsed.pickups().get(0).electrical().seriesDcResistance());
    assertTrue(parsed.issues().stream().anyMatch(i -> i.message().contains("unknown unit")));
  }

  @Test
  public void testMalformedEnumProducesValidationError() {
    String json = """
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "test:bad-format", "manufacturer": "Acme", "model": "Basic", "format": "NOT_A_FORMAT" }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    // format is a required, identity-level field - an invalid value rejects the whole entry.
    assertTrue(parsed.pickups().isEmpty());
    assertTrue(parsed.issues().stream()
        .anyMatch(i -> "test:bad-format".equals(i.pickupId()) && i.message().contains("PickupFormat")));
  }

  @Test
  public void testMissingIdentityFieldsRejectEntryButKeepOthers() {
    String json = """
        {
          "schemaVersion": 1,
          "pickups": [
            { "manufacturer": "Acme", "model": "No Id", "format": "SINGLE_COIL" },
            { "id": "test:valid", "manufacturer": "Acme", "model": "Valid", "format": "SINGLE_COIL" }
          ]
        }
        """;

    ParsedFile parsed = loader.parse(json, "test");

    assertEquals(1, parsed.pickups().size());
    assertEquals("test:valid", parsed.pickups().get(0).id());
    assertTrue(parsed.issues().stream().anyMatch(i -> i.fieldPath() != null && i.fieldPath().endsWith(".id")));
  }

  @Test
  public void testMalformedJsonDoesNotThrow() {
    String json = "{ this is not valid json ";

    ParsedFile parsed = loader.parse(json, "test-malformed-file");

    assertNotNull(parsed);
    assertTrue(parsed.pickups().isEmpty());
    assertFalse(parsed.issues().isEmpty());
    assertEquals("test-malformed-file", parsed.issues().get(0).source());
  }

  @Test
  public void testBuiltInLibraryResourceParsesCleanly() throws Exception {
    String json;
    try (var in = getClass().getResourceAsStream("/pickups/pickups.json")) {
      assertNotNull("built-in pickups.json resource must exist", in);
      json = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    ParsedFile parsed = loader.parse(json, PickupLibrary.BUILTIN_RESOURCE);

    assertTrue("built-in library should parse without issues: " + parsed.issues(), parsed.issues().isEmpty());
    assertTrue(parsed.pickups().size() >= 3);
    List<PickupFormat> formats = parsed.pickups().stream().map(PickupDefinition::format).toList();
    assertTrue(formats.contains(PickupFormat.HUMBUCKER));
    assertTrue(formats.contains(PickupFormat.SINGLE_COIL));
    assertTrue(formats.contains(PickupFormat.P90));
  }
}
