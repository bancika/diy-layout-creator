package org.diylc.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupFormat;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Covers placing a brand new pickup directly from the library (the "right-click the empty
 * canvas" counterpart to {@link ApplyPickupDefinitionEditorTest}'s replace-in-place scenarios):
 * correct component class, requested location, the required "uncovered" appearance for
 * humbuckers and single coils, definition metadata applied, a unique auto-generated name, and
 * that it is genuinely added to the project rather than replacing anything.
 */
public class PlacePickupFromLibraryEditorTest {

  private static PickupDefinition humbuckerDefinition() {
    return new PickupDefinition("test:humbucker", 1, "Acme", "Test Humbucker", "Bridge", PickupFormat.HUMBUCKER,
        false, 6, null, null, null, null, null, null);
  }

  private static PickupDefinition singleCoilDefinition() {
    return new PickupDefinition("test:single", 1, "Acme", "Test Single", "Bridge", PickupFormat.SINGLE_COIL, false, 6,
        null, null, null, null, null, null);
  }

  @Test
  public void testPlacesHumbuckerAtRequestedLocationAsUncoveredPAF() {
    Project project = new Project();
    Point2D location = new Point2D.Double(123, 456);

    Set<IDIYComponent<?>> result =
        new PlacePickupFromLibraryEditor(humbuckerDefinition(), location).edit(project, new HashSet<>());

    assertEquals(1, project.getComponents().size());
    assertEquals(1, result.size());
    IDIYComponent<?> placed = project.getComponents().get(0);
    assertTrue(placed instanceof HumbuckerPickup);
    HumbuckerPickup humbucker = (HumbuckerPickup) placed;

    assertEquals(location, humbucker.getControlPoint(0));
    assertEquals(HumbuckerPickup.HumbuckerType.PAF, humbucker.getType());
    assertFalse("expected an uncovered humbucker", humbucker.getCover());
    assertEquals("test:humbucker", humbucker.getPickupDefinitionId());
    assertNotNull(humbucker.getName());
  }

  @Test
  public void testPlacesSingleCoilAtRequestedLocationAsRegularUncoveredType() {
    Project project = new Project();
    Point2D location = new Point2D.Double(50, 75);

    new PlacePickupFromLibraryEditor(singleCoilDefinition(), location).edit(project, new HashSet<>());

    assertEquals(1, project.getComponents().size());
    SingleCoilPickup singleCoil = (SingleCoilPickup) project.getComponents().get(0);

    assertEquals(location, singleCoil.getControlPoint(0));
    assertEquals(SingleCoilPickup.SingleCoilType.Stratocaster, singleCoil.getType());
    assertEquals(SingleCoilPickup.PolePieceType.Rods, singleCoil.getPolePieceType());
    assertEquals("test:single", singleCoil.getPickupDefinitionId());
  }

  @Test
  public void testEveryControlPointIsTranslatedNotJustTheFirst() {
    // The whole component must move as one rigid unit - not just its anchor - or its own
    // internal terminal spacing would collapse to a single point.
    Project project = new Project();
    Point2D location = new Point2D.Double(200, 300);

    new PlacePickupFromLibraryEditor(humbuckerDefinition(), location).edit(project, new HashSet<>());

    HumbuckerPickup humbucker = (HumbuckerPickup) project.getComponents().get(0);
    Point2D p0 = humbucker.getControlPoint(0);
    Point2D p1 = humbucker.getControlPoint(1);
    assertFalse("control points must not collapse onto each other", p0.equals(p1));
  }

  @Test
  public void testPlacingTwiceGivesEachPickupAUniqueName() {
    Project project = new Project();

    new PlacePickupFromLibraryEditor(humbuckerDefinition(), new Point2D.Double(0, 0)).edit(project, new HashSet<>());
    new PlacePickupFromLibraryEditor(humbuckerDefinition(), new Point2D.Double(300, 0)).edit(project,
        new HashSet<>());

    assertEquals(2, project.getComponents().size());
    String name1 = project.getComponents().get(0).getName();
    String name2 = project.getComponents().get(1).getName();
    assertNotNull(name1);
    assertNotNull(name2);
    assertFalse("expected unique auto-generated names", name1.equals(name2));
  }
}
