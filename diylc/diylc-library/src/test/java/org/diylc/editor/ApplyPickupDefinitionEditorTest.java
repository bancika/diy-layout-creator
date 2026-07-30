package org.diylc.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.diylc.components.guitar.AbstractGuitarPickup.Polarity;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.pickup.CoilDefinition;
import org.diylc.components.guitar.pickup.CoilPolePieceType;
import org.diylc.components.guitar.pickup.MagneticPolarity;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

public class ApplyPickupDefinitionEditorTest {

  private static PickupDefinition humbuckerDefinition() {
    return new PickupDefinition("test:humbucker", 1, "Acme", "Test Humbucker", "Bridge", PickupFormat.HUMBUCKER,
        false, 6, null, null, null, null, null, null);
  }

  private static PickupDefinition humbuckerDefinitionWithPolePieces(CoilPolePieceType northType,
      CoilPolePieceType southType) {
    CoilDefinition north = new CoilDefinition("coilA", null, northType, MagneticPolarity.NORTH, null, null, null);
    CoilDefinition south = new CoilDefinition("coilB", null, southType, MagneticPolarity.SOUTH, null, null, null);
    return new PickupDefinition("test:humbucker-poles", 1, "Acme", "Test Humbucker", "Bridge", PickupFormat.HUMBUCKER,
        false, 6, List.of(north, south), null, null, null, null, null);
  }

  private static PickupDefinition ambiguousHumbuckerDefinition() {
    // both coils claim NORTH - which one is "coil 1" vs "coil 2" is genuinely ambiguous.
    CoilDefinition a = new CoilDefinition("a", null, CoilPolePieceType.SLUG, MagneticPolarity.NORTH, null, null,
        null);
    CoilDefinition b = new CoilDefinition("b", null, CoilPolePieceType.SLUG, MagneticPolarity.NORTH, null, null,
        null);
    return new PickupDefinition("test:ambiguous", 1, "Acme", "Ambiguous", null, PickupFormat.HUMBUCKER, false, 6,
        List.of(a, b), null, null, null, null, null);
  }

  private static PickupDefinition singleCoilDefinition(CoilPolePieceType poleType, MagneticPolarity polarity) {
    CoilDefinition coil = new CoilDefinition("coil", null, poleType, polarity, null, null, null);
    return new PickupDefinition("test:single-coil", 1, "Acme", "Test Single Coil", "Bridge", PickupFormat.SINGLE_COIL,
        false, 6, List.of(coil), null, null, null, null, null);
  }

  private static void applyTo(PickupDefinition definition, org.diylc.components.guitar.AbstractGuitarPickup pickup) {
    Project project = new Project();
    project.getComponents().add(pickup);
    Set<IDIYComponent<?>> selection = new HashSet<>();
    selection.add(pickup);
    new ApplyPickupDefinitionEditor(definition).edit(project, selection);
  }

  @Test
  public void testSameTypeApplyUpdatesInPlace() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    Project project = new Project();
    project.getComponents().add(pickup);
    Set<IDIYComponent<?>> selection = new HashSet<>();
    selection.add(pickup);

    PickupDefinition definition = humbuckerDefinition();
    new ApplyPickupDefinitionEditor(definition).edit(project, selection);

    assertEquals("test:humbucker", pickup.getPickupDefinitionId());
    assertEquals(definition, pickup.getAppliedDefinition());
    assertEquals("Acme Test Humbucker (Bridge)", pickup.getValue());
    // the component instance itself is unchanged/untouched (no replacement).
    assertEquals(1, project.getComponents().size());
    assertEquals(pickup, project.getComponents().get(0));
  }

  @Test
  public void testCrossTypeSelectionReplacesTheComponentAndUpdatesSelection() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);
    Set<IDIYComponent<?>> selection = new HashSet<>();
    selection.add(pickup);

    // a HUMBUCKER definition applied to a SingleCoilPickup is a cross-type case - the actual
    // replacement mechanics are covered in detail by PickupReplacementServiceTest; here we just
    // confirm the editor routes to it and returns an updated selection pointing at the new
    // component (never the stale old one).
    Set<IDIYComponent<?>> newSelection =
        new ApplyPickupDefinitionEditor(humbuckerDefinition()).edit(project, selection);

    assertEquals(1, project.getComponents().size());
    IDIYComponent<?> replacement = project.getComponents().get(0);
    org.junit.Assert.assertTrue(replacement instanceof HumbuckerPickup);
    assertEquals("test:humbucker", ((HumbuckerPickup) replacement).getPickupDefinitionId());
    assertEquals(1, newSelection.size());
    assertEquals(replacement, newSelection.iterator().next());
  }

  @Test
  public void testHumbuckerPolePieceMappingAppliedWhenUnambiguous() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    applyTo(humbuckerDefinitionWithPolePieces(CoilPolePieceType.SLUG, CoilPolePieceType.SCREW),
        pickup);

    assertEquals(HumbuckerPickup.PolePieceType.Rods, pickup.getCoilType1());
    assertEquals(HumbuckerPickup.PolePieceType.Screws, pickup.getCoilType2());
  }

  @Test
  public void testHumbuckerPolePieceMappingSkippedWhenPolarityIsAmbiguous() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    HumbuckerPickup.PolePieceType before1 = pickup.getCoilType1();
    HumbuckerPickup.PolePieceType before2 = pickup.getCoilType2();

    applyTo(ambiguousHumbuckerDefinition(), pickup);

    assertEquals(before1, pickup.getCoilType1());
    assertEquals(before2, pickup.getCoilType2());
  }

  @Test
  public void testSingleCoilPolePieceMappingApplied() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    applyTo(singleCoilDefinition(CoilPolePieceType.RAIL, MagneticPolarity.UNKNOWN),
        pickup);

    assertEquals(SingleCoilPickup.PolePieceType.Rail, pickup.getPolePieceType());
  }

  @Test
  public void testSingleCoilPolePieceMappingSkippedForUnsupportedType() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    SingleCoilPickup.PolePieceType before = pickup.getPolePieceType();

    // SCREW has no equivalent on SingleCoilPickup's own PolePieceType enum.
    applyTo(singleCoilDefinition(CoilPolePieceType.SCREW, MagneticPolarity.UNKNOWN),
        pickup);

    assertEquals(before, pickup.getPolePieceType());
  }

  @Test
  public void testPolarityAppliedWhenPickupNotAlreadyHumbucking() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setPolarity(Polarity.North);

    applyTo(singleCoilDefinition(CoilPolePieceType.ROD, MagneticPolarity.SOUTH),
        pickup);

    assertEquals(Polarity.South, pickup.getPolarity());
  }

  @Test
  public void testPolarityNeverAutoSwitchesHumbuckingMode() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setPolarity(Polarity.Humbucking);

    // even though this SINGLE_COIL definition has a definite NORTH polarity, applying it must
    // not silently collapse a 4-lead humbucking-mode pickup down to a 2-lead one - that would
    // change the diagram's circuit topology.
    applyTo(singleCoilDefinition(CoilPolePieceType.ROD, MagneticPolarity.NORTH),
        pickup);

    assertEquals(Polarity.Humbucking, pickup.getPolarity());
  }

  @Test
  public void testPolarityUnchangedWhenDefinitionPolarityIsUnknown() {
    SingleCoilPickup pickup = new SingleCoilPickup();
    pickup.setPolarity(Polarity.North);

    applyTo(singleCoilDefinition(CoilPolePieceType.ROD, MagneticPolarity.UNKNOWN),
        pickup);

    assertEquals(Polarity.North, pickup.getPolarity());
  }

  @Test
  public void testDoesNotOverwriteUnrelatedUserStyling() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    Color customColor = Color.decode("#123456");
    pickup.setColor(customColor);
    pickup.setBobinColor1(Color.PINK);

    applyTo(humbuckerDefinitionWithPolePieces(CoilPolePieceType.SLUG, CoilPolePieceType.SCREW),
        pickup);

    assertEquals(customColor, pickup.getColor());
    assertEquals(Color.PINK, pickup.getBobinColor1());
  }

  @Test
  public void testCrossTypeReplacementParticipatesInUndo() {
    // Mirrors exactly what Presenter#applyEditor does before invoking any IProjectEditor: clone
    // the project first. The application's undo/redo stack (EditMenuPlugin's UndoHandler) relies
    // entirely on this "before" clone being unaffected by whatever the editor then does to the
    // live project - which is true for every editor already, and this confirms it holds for a
    // cross-type pickup replacement too.
    SingleCoilPickup pickup = new SingleCoilPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    Project beforeEdit = project.clone();

    Set<IDIYComponent<?>> selection = new HashSet<>();
    selection.add(pickup);
    new ApplyPickupDefinitionEditor(humbuckerDefinition()).edit(project, selection);

    // the live project reflects the replacement...
    assertEquals(1, project.getComponents().size());
    assertTrue(project.getComponents().get(0) instanceof HumbuckerPickup);

    // ...while the untouched "before" clone still has the original component, exactly what lets
    // the undo stack restore it.
    assertEquals(1, beforeEdit.getComponents().size());
    assertTrue(beforeEdit.getComponents().get(0) instanceof SingleCoilPickup);
    assertFalse(project.equals(beforeEdit));
  }
}
