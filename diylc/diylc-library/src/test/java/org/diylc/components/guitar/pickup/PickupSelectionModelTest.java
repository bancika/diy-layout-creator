package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;

public class PickupSelectionModelTest {

  private static PickupLibrary sampleLibrary() {
    PickupLibraryLoader loader = new PickupLibraryLoader();
    var parsed = loader.parse("""
        {
          "schemaVersion": 1,
          "pickups": [
            { "id": "bkp:mule", "manufacturer": "Bare Knuckle Pickups", "model": "The Mule", "variant": "Bridge", "format": "HUMBUCKER" },
            { "id": "bkp:mothers-milk", "manufacturer": "Bare Knuckle Pickups", "model": "Mother's Milk", "variant": "Bridge", "format": "SINGLE_COIL" },
            { "id": "acme:single", "manufacturer": "Acme", "model": "Basic Single", "format": "SINGLE_COIL" }
          ]
        }
        """, "test");
    return PickupLibrary.merge(List.of(parsed));
  }

  @Test
  public void testClickedSingleCoilPreFillsSingleCoilFilter() {
    PickupSelectionModel model = PickupSelectionModel.forComponent(sampleLibrary(), new SingleCoilPickup());

    assertEquals(PickupFormat.SINGLE_COIL, model.getInitialFormat());
    assertEquals(PickupFormat.SINGLE_COIL, model.getFormatFilter());
    for (PickupDefinition d : model.getResults()) {
      assertEquals(PickupFormat.SINGLE_COIL, d.format());
    }
  }

  @Test
  public void testClickedHumbuckerPreFillsHumbuckerFilter() {
    PickupSelectionModel model = PickupSelectionModel.forComponent(sampleLibrary(), new HumbuckerPickup());

    assertEquals(PickupFormat.HUMBUCKER, model.getInitialFormat());
    assertEquals(PickupFormat.HUMBUCKER, model.getFormatFilter());
    assertEquals(1, model.getResults().size());
    assertEquals("bkp:mule", model.getResults().get(0).id());
  }

  @Test
  public void testExistingDefinitionIsPreselected() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(sampleLibrary().findById("bkp:mule")));

    PickupSelectionModel model = PickupSelectionModel.forComponent(sampleLibrary(), pickup);

    assertEquals("bkp:mule", model.getInitialDefinitionId());
    assertEquals("bkp:mule", model.getSelectedDefinitionId());
    assertTrue(model.isSelected(model.getLibrary().findById("bkp:mule")));
  }

  @Test
  public void testComponentWithNoDefinitionHasNoPreselection() {
    PickupSelectionModel model = PickupSelectionModel.forComponent(sampleLibrary(), new HumbuckerPickup());

    assertNull(model.getInitialDefinitionId());
    assertNull(model.getSelectedDefinitionId());
    assertNull(model.getSelectedDefinition());
  }

  @Test
  public void testUserCanClearOrChangeTheFormatFilter() {
    PickupSelectionModel model = PickupSelectionModel.forComponent(sampleLibrary(), new HumbuckerPickup());
    assertEquals(1, model.getResults().size());

    model.setFormatFilter(null);
    assertEquals(3, model.getResults().size());

    model.setFormatFilter(PickupFormat.SINGLE_COIL);
    assertEquals(2, model.getResults().size());
  }

  @Test
  public void testSearchTextAndManufacturerFilterCombineWithFormat() {
    PickupSelectionModel model = PickupSelectionModel.forComponent(sampleLibrary(), new HumbuckerPickup());
    model.setFormatFilter(null);

    model.setManufacturerFilter("Bare Knuckle");
    assertEquals(2, model.getResults().size());

    model.setSearchText("mule");
    assertEquals(1, model.getResults().size());
    assertEquals("bkp:mule", model.getResults().get(0).id());
  }
}
