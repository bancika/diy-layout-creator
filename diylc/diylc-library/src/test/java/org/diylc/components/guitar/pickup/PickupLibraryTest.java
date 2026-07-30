package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class PickupLibraryTest {

  private final PickupLibraryLoader loader = new PickupLibraryLoader();

  private static String pickupJson(String id, String manufacturer, String model, String format) {
    return """
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "%s", "manufacturer": "%s", "model": "%s", "format": "%s" }
          ]
        }
        """.formatted(id, manufacturer, model, format);
  }

  @Test
  public void testDuplicateIdsAreRejectedKeepingFirst() {
    var first = loader.parse(pickupJson("dup:1", "First Co", "Model A", "SINGLE_COIL"), "file-1.json");
    var second = loader.parse(pickupJson("dup:1", "Second Co", "Model B", "HUMBUCKER"), "file-2.json");

    PickupLibrary library = PickupLibrary.merge(List.of(first, second));

    assertEquals(1, library.getAll().size());
    assertEquals("First Co", library.findById("dup:1").manufacturer());
    assertTrue(library.getIssues().stream().anyMatch(i -> i.message().contains("duplicate pickup id")));
  }

  @Test
  public void testMalformedFileDoesNotBlockOtherValidFiles() {
    var malformed = loader.parse("{ not json at all", "bad-file.json");
    var valid = loader.parse(pickupJson("good:1", "Acme", "Model", "P90"), "good-file.json");

    PickupLibrary library = PickupLibrary.merge(List.of(malformed, valid));

    assertEquals(1, library.getAll().size());
    assertEquals("good:1", library.getAll().get(0).id());
    assertTrue(library.getIssues().stream().anyMatch(i -> "bad-file.json".equals(i.source())));
  }

  @Test
  public void testFindByIdReturnsNullWhenMissing() {
    PickupLibrary library = PickupLibrary.merge(List.of());
    assertNull(library.findById("does-not-exist"));
    assertTrue(library.getAll().isEmpty());
  }

  @Test
  public void testSearchByFormatManufacturerAndText() {
    var parsed = loader.parse("""
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "a", "manufacturer": "Bare Knuckle Pickups", "model": "The Mule", "variant": "Bridge", "format": "HUMBUCKER" },
            { "id": "b", "manufacturer": "Bare Knuckle Pickups", "model": "Mother's Milk", "variant": "Bridge", "format": "SINGLE_COIL" },
            { "id": "c", "manufacturer": "Acme", "model": "Basic Single", "format": "SINGLE_COIL" }
          ]
        }
        """, "test");
    PickupLibrary library = PickupLibrary.merge(List.of(parsed));

    assertEquals(2, library.search(null, PickupFormat.SINGLE_COIL, null).size());
    assertEquals(1, library.search(null, PickupFormat.HUMBUCKER, null).size());
    assertEquals(2, library.search(null, null, "Bare Knuckle").size());
    assertEquals(1, library.search("mule", null, null).size());
    assertEquals("a", library.search("mule", null, null).get(0).id());
    assertEquals(1, library.search(null, PickupFormat.SINGLE_COIL, "Bare Knuckle").size());
  }

  @Test
  public void testGetManufacturersIsDistinctAndSorted() {
    var parsed = loader.parse("""
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "a", "manufacturer": "Zeta", "model": "M1", "format": "SINGLE_COIL" },
            { "id": "b", "manufacturer": "Acme", "model": "M2", "format": "SINGLE_COIL" },
            { "id": "c", "manufacturer": "Acme", "model": "M3", "format": "P90" }
          ]
        }
        """, "test");
    PickupLibrary library = PickupLibrary.merge(List.of(parsed));

    assertEquals(List.of("Acme", "Zeta"), library.getManufacturers());
  }

  @Test
  public void testLoadDefaultNeverThrowsAndIncludesBuiltInSeedData() {
    PickupLibrary library = PickupLibrary.loadDefault();

    assertTrue(library.getAll().size() >= 3);
    assertTrue(library.getAll().stream().anyMatch(d -> d.id().equals("bareknuckle:the-mule-bridge")));
  }

  @Test
  public void testGetInstanceIsCachedSingleton() {
    PickupLibrary a = PickupLibrary.getInstance();
    PickupLibrary b = PickupLibrary.getInstance();
    assertTrue(a == b);
  }
}
