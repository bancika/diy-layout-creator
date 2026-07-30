package org.diylc.swing.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

import java.awt.GraphicsEnvironment;

import org.junit.Test;

import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionSnapshot;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.components.guitar.pickup.PickupLibrary;
import org.diylc.components.guitar.pickup.PickupSelectionModel;

/**
 * A construction/wiring smoke test for {@link PickupLibraryDialog}: builds the dialog (without
 * showing it) against the real, lazily-loaded {@link PickupLibrary} to catch obvious NPEs/wiring
 * mistakes in the table model, filters and details panel that unit tests of the Swing-free
 * {@link PickupSelectionModel} can't reach. Skips itself on a truly headless CI box where even
 * off-screen component construction isn't supported.
 */
public class PickupLibraryDialogTest {

  @Test
  public void testDialogCanBeBuiltAndPreselectsExistingDefinition() {
    assumeFalse("Skipping Swing construction test in a headless environment",
        GraphicsEnvironment.isHeadless());

    HumbuckerPickup pickup = new HumbuckerPickup();
    PickupLibrary library = PickupLibrary.getInstance();
    PickupDefinition mule = library.findById("bareknuckle:the-mule-bridge");
    assertNotNull("expected the built-in seed data to include the Bare Knuckle Mule", mule);
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(mule));

    PickupSelectionModel model = PickupSelectionModel.forComponent(library, pickup);
    PickupLibraryDialog dialog = new PickupLibraryDialog(null, model);

    assertFalse("dialog must start unapplied", dialog.isApplied());
    // pre-fill/preselection behaviour itself is covered exhaustively by PickupSelectionModelTest;
    // this just confirms the format/id the model was built with matches the clicked component.
    assertNotNull(model.getInitialFormat());
    org.junit.Assert.assertEquals(PickupFormat.HUMBUCKER, model.getInitialFormat());
    org.junit.Assert.assertEquals("bareknuckle:the-mule-bridge", model.getInitialDefinitionId());

    dialog.dispose();
  }

  @Test
  public void testDialogCanBeBuiltWithNoClickedComponentForThePlaceFromBaseLayerCase() {
    assumeFalse("Skipping Swing construction test in a headless environment",
        GraphicsEnvironment.isHeadless());

    // SelectPickupFromLibraryAction's "place a new pickup" mode (right-clicking the empty
    // canvas/base layer, per CLAUDE.md) has no clicked component to pre-fill from at all.
    PickupSelectionModel model = PickupSelectionModel.forComponent(PickupLibrary.getInstance(), null);
    PickupLibraryDialog dialog = new PickupLibraryDialog(null, model);

    assertFalse("dialog must start unapplied", dialog.isApplied());
    org.junit.Assert.assertNull("no clicked component means no format pre-fill", model.getInitialFormat());
    org.junit.Assert.assertNull("no clicked component means nothing preselected", model.getInitialDefinitionId());

    dialog.dispose();
  }
}
