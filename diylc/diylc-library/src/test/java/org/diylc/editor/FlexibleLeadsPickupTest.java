package org.diylc.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.guitar.HumbuckerPickup;
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
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Verifies the "Add Flexible Leads" integration added in step 4: leads generated from a pickup's
 * control points should pick up the applied definition's terminal colours, fall back to the
 * existing default when a colour is absent (or no definition is applied at all), and must never
 * touch leads/wires that already exist on the diagram.
 */
public class FlexibleLeadsPickupTest {

  @BeforeClass
  public static void setUpConfigurationManager() {
    // InstantiationManager#fillWithDefaultProperties reads component defaults through
    // ConfigurationManager, which (like in the real application) must be initialized once before
    // use; in a normal run this happens during application start-up. Harmless in tests: if no
    // config file exists yet for the "diylc" app data directory, this simply starts with an
    // empty in-memory configuration map.
    ConfigurationManager.getInstance().initialize("diylc");
  }

  private static PickupDefinition fourConductorHumbucker(boolean includeAllColours) {
    CoilDefinition north = new CoilDefinition("coilA", CoilSide.SIDE_A, null, MagneticPolarity.NORTH, null, null,
        null);
    CoilDefinition south = new CoilDefinition("coilB", CoilSide.SIDE_B, null, MagneticPolarity.SOUTH, null, null,
        null);
    TerminalDefinition northStart = new TerminalDefinition("coilA.start", "coilA", TerminalRole.START,
        new LeadSpec(LeadType.INSULATED, "Red", "#D0202A", null));
    TerminalDefinition northFinish = new TerminalDefinition("coilA.finish", "coilA", TerminalRole.FINISH,
        includeAllColours ? new LeadSpec(LeadType.INSULATED, "Green", "#1E8449", null)
            : new LeadSpec(LeadType.UNKNOWN, null, null, null));
    TerminalDefinition southFinish = new TerminalDefinition("coilB.finish", "coilB", TerminalRole.FINISH,
        new LeadSpec(LeadType.INSULATED, "White", "#F2F2F2", null));
    TerminalDefinition southStart = new TerminalDefinition("coilB.start", "coilB", TerminalRole.START,
        new LeadSpec(LeadType.INSULATED, "Black", "#202020", null));
    return new PickupDefinition("test:humbucker", 1, "Acme", "Test Humbucker", "Bridge", PickupFormat.HUMBUCKER,
        false, 6, List.of(north, south), List.of(northStart, northFinish, southFinish, southStart), null, null,
        null, null);
  }

  /**
   * Runs the editor and returns the generated wires indexed by the pickup control point they
   * originated from (identified by matching each wire's first control point back to the
   * pickup's original control point location, since {@code edit()} returns an unordered
   * {@code Set} and the guitar-pickup path never moves the pickup's own control points).
   */
  private static Map<Integer, HookupWire> runFlexibleLeadsAndGetWiresByControlPoint(HumbuckerPickup pickup,
      Project project) {
    Point2D[] originalPoints = new Point2D[pickup.getControlPointCount()];
    for (int i = 0; i < originalPoints.length; i++) {
      originalPoints[i] = (Point2D) pickup.getControlPoint(i).clone();
    }

    Set<IDIYComponent<?>> selection = new HashSet<>();
    selection.add(pickup);
    FlexibleLeadsEditor editor = new FlexibleLeadsEditor();
    Set<IDIYComponent<?>> result = editor.edit(project, selection);

    Map<Integer, HookupWire> byIndex = new HashMap<>();
    for (IDIYComponent<?> component : result) {
      if (!(component instanceof HookupWire wire)) {
        continue;
      }
      Point2D wireStart = wire.getControlPoint(0);
      for (int i = 0; i < originalPoints.length; i++) {
        if (originalPoints[i].equals(wireStart)) {
          byIndex.put(i, wire);
          break;
        }
      }
    }
    return byIndex;
  }

  @Test
  public void testLeadColoursMatchTerminalMetadata() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(fourConductorHumbucker(true)));
    Project project = new Project();
    project.getComponents().add(pickup);

    Map<Integer, HookupWire> wires = runFlexibleLeadsAndGetWiresByControlPoint(pickup, project);

    assertEquals(4, wires.size());
    assertEquals(Color.decode("#D0202A"), wires.get(0).getLeadColor());
    assertEquals(Color.decode("#1E8449"), wires.get(1).getLeadColor());
    assertEquals(Color.decode("#F2F2F2"), wires.get(2).getLeadColor());
    assertEquals(Color.decode("#202020"), wires.get(3).getLeadColor());
  }

  @Test
  public void testMissingColourFallsBackToCurrentDefault() {
    HumbuckerPickup withDefinitionNoColour = new HumbuckerPickup();
    withDefinitionNoColour.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(fourConductorHumbucker(false)));
    Project projectA = new Project();
    projectA.getComponents().add(withDefinitionNoColour);
    Map<Integer, HookupWire> wiresA = runFlexibleLeadsAndGetWiresByControlPoint(withDefinitionNoColour, projectA);

    HumbuckerPickup noDefinitionAtAll = new HumbuckerPickup();
    Project projectB = new Project();
    projectB.getComponents().add(noDefinitionAtAll);
    Map<Integer, HookupWire> wiresB = runFlexibleLeadsAndGetWiresByControlPoint(noDefinitionAtAll, projectB);

    // control point 1 (North Finish) has no colour supplied by the definition in this fixture -
    // it must fall back to exactly the same default as when there is no definition at all.
    Color defaultColor = wiresB.get(1).getLeadColor();
    assertNotNull(defaultColor);
    assertEquals(defaultColor, wiresA.get(1).getLeadColor());

    // the other three points still get their supplied colours.
    assertEquals(Color.decode("#D0202A"), wiresA.get(0).getLeadColor());
    assertEquals(Color.decode("#F2F2F2"), wiresA.get(2).getLeadColor());
    assertEquals(Color.decode("#202020"), wiresA.get(3).getLeadColor());
  }

  @Test
  public void testNoDefinitionUsesCurrentDefaultBehaviourForAllLeads() {
    HumbuckerPickup pickup = new HumbuckerPickup();
    Project project = new Project();
    project.getComponents().add(pickup);

    Map<Integer, HookupWire> wires = runFlexibleLeadsAndGetWiresByControlPoint(pickup, project);

    assertEquals(4, wires.size());
    Color expected = HookupWire.COLOR;
    for (HookupWire w : wires.values()) {
      assertEquals(expected, w.getLeadColor());
    }
  }

  @Test
  public void testExistingLeadsOnTheDiagramAreNotModified() {
    HookupWire preExisting = new HookupWire();
    preExisting.setLeadColor(Color.MAGENTA);

    HumbuckerPickup pickup = new HumbuckerPickup();
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(fourConductorHumbucker(true)));

    Project project = new Project();
    project.getComponents().add(preExisting);
    project.getComponents().add(pickup);

    runFlexibleLeadsAndGetWiresByControlPoint(pickup, project);

    assertEquals(Color.MAGENTA, preExisting.getLeadColor());
  }
}
