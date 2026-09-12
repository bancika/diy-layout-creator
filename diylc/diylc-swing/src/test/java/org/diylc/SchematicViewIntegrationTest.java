package org.diylc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.diylc.components.schematic.SchematicWire;
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
        view.getComponents().stream().filter(c -> !(c instanceof SchematicWire)).count();
    long wireCount = view.getComponents().stream().filter(c -> c instanceof SchematicWire).count();
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
