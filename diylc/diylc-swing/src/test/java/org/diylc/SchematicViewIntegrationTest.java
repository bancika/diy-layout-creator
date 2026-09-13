package org.diylc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.diylc.components.connectivity.OrthogonalLine;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.schematic.SchematicBuilder;
import org.diylc.schematic.SchematicSynchronizer;
import org.diylc.swing.plugins.schematic.SchematicPanel;
import org.junit.Test;

/**
 * End-to-end check of the schematic pipeline: instantiate real components through the presenter,
 * build/sync the schematic and render it through the viewer panel.
 */
public class SchematicViewIntegrationTest extends TestBase {

  @Test
  public void buildsSymbolsAndWiresFromLayout() {
    // two resistors sharing a node at (1200, 1000)
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    instantiateTwoClick("Passive", "Resistor", new Point(1200, 1000), new Point(1400, 1000));

    Project project = presenter.getCurrentProject();
    new SchematicBuilder().build(project, presenter.getContinuityAreas());

    SchematicView view = project.getSchematicView();
    assertNotNull(view);
    assertTrue(view.isGenerated());
    assertEquals(2, view.getPhysicalToSchematicMap().size());

    long symbolCount =
        view.getComponents().stream().filter(c -> !(c instanceof OrthogonalLine)).count();
    long wireCount = view.getComponents().stream().filter(c -> c instanceof OrthogonalLine).count();
    assertEquals(2, symbolCount);
    assertTrue("expected the shared node to produce a wire", wireCount >= 1);
  }

  @Test
  public void synchronizerAddsAndRemovesSymbols() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    Project project = presenter.getCurrentProject();

    new SchematicSynchronizer().synchronize(project, presenter.getContinuityAreas());
    assertEquals(1, project.getSchematicView().getPhysicalToSchematicMap().size());

    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1400), new Point(1200, 1400));
    new SchematicSynchronizer().synchronize(project, presenter.getContinuityAreas());
    assertEquals(2, project.getSchematicView().getPhysicalToSchematicMap().size());

    project.getComponents().remove(project.getComponents().size() - 1);
    new SchematicSynchronizer().synchronize(project, presenter.getContinuityAreas());
    assertEquals(1, project.getSchematicView().getPhysicalToSchematicMap().size());
  }

  @Test
  public void wiresAreOrderedUnderneathTheSymbols() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    instantiateTwoClick("Passive", "Resistor", new Point(1200, 1000), new Point(1400, 1000));

    Project project = presenter.getCurrentProject();
    new SchematicBuilder().build(project, presenter.getContinuityAreas());

    // the canvas paints in list order and hands a click to the last match, so wires belong first
    boolean seenSymbol = false;
    for (IDIYComponent<?> component : project.getSchematicView().getComponents()) {
      if (component instanceof OrthogonalLine) {
        assertTrue("wire found after a symbol", !seenSymbol);
      } else {
        seenSymbol = true;
      }
    }
    assertTrue(seenSymbol);
  }

  @Test
  public void stretchingASymbolReconnectsTheWiresOnSync() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    instantiateTwoClick("Passive", "Resistor", new Point(1200, 1000), new Point(1400, 1000));

    Project project = presenter.getCurrentProject();
    new SchematicBuilder().build(project, presenter.getContinuityAreas());
    SchematicView view = project.getSchematicView();
    assertTrue(countWires(view) > 0);
    assertEndpointsSitOnPins(view);

    // stretch a symbol by dragging a single pin, which is the case that used to leave the wire
    // behind: the pin the wire was drawn to is now somewhere else
    IDIYComponent<?> moved = null;
    for (IDIYComponent<?> component : view.getComponents()) {
      if (!(component instanceof OrthogonalLine)) {
        moved = component;
        break;
      }
    }
    assertNotNull(moved);
    Point2D pin = moved.getControlPoint(1);
    moved.setControlPoint(new Point2D.Double(pin.getX() + 300, pin.getY() + 400), 1);

    new SchematicSynchronizer().synchronize(project, presenter.getContinuityAreas());

    view = project.getSchematicView();
    assertTrue(countWires(view) > 0);
    assertEndpointsSitOnPins(view);
  }

  private static long countWires(SchematicView view) {
    return view.getComponents().stream().filter(c -> c instanceof OrthogonalLine).count();
  }

  private static void assertEndpointsSitOnPins(SchematicView view) {
    for (IDIYComponent<?> wire : view.getComponents()) {
      if (!(wire instanceof OrthogonalLine)) {
        continue;
      }
      for (int end : new int[] {0, wire.getControlPointCount() - 1}) {
        Point2D endpoint = wire.getControlPoint(end);
        boolean onPin = false;
        for (IDIYComponent<?> symbol : view.getComponents()) {
          if (symbol instanceof OrthogonalLine) {
            continue;
          }
          for (int i = 0; i < symbol.getControlPointCount(); i++) {
            if (symbol.getControlPoint(i).distance(endpoint) < 0.6) {
              onPin = true;
            }
          }
        }
        assertTrue("wire endpoint " + endpoint + " is not on any symbol pin", onPin);
      }
    }
  }

  @Test
  public void viewerPanelRendersWithoutError() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    Project project = presenter.getCurrentProject();

    SchematicPanel panel = new SchematicPanel();
    panel.refresh(project, presenter.getContinuityAreas());

    assertTrue(panel.hasContent());
    BufferedImage image = panel.renderToImage();
    assertNotNull(image);
    assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
  }
}
