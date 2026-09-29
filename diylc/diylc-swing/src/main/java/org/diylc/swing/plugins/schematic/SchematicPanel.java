/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.plugins.schematic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.presenter.ContinuityArea;
import org.diylc.presenter.DrawingManager;
import org.diylc.schematic.SchematicSynchronizer;

/**
 * Read-only rendering surface for the generated {@link SchematicView}. It reuses the regular
 * {@link DrawingManager} pipeline so schematic symbols are drawn by exactly the same code that draws
 * layout components.
 *
 * <p>
 * This is the minimal viewer shipped with the first iteration of the feature: it can regenerate the
 * schematic, pan (via the enclosing scroll pane), zoom and export. Interactive editing (moving,
 * rotating and mirroring symbols) with a restricted {@code Presenter} is a planned follow-up.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class SchematicPanel extends JComponent {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(SchematicPanel.class);

  private final DrawingManager drawingManager;
  private Project schematicProject;
  private double zoom = 1d;

  public SchematicPanel() {
    this.drawingManager =
        new DrawingManager(new MessageDispatcher<EventType>(false), ConfigurationManager.getInstance());
    setDoubleBuffered(true);
  }

  /**
   * Regenerates (or incrementally syncs) the schematic for the given layout project and shows it.
   */
  public void refresh(Project layoutProject, List<ContinuityArea> continuityAreas) {
    try {
      new SchematicSynchronizer().synchronize(layoutProject,
          continuityAreas == null ? Collections.<ContinuityArea>emptyList() : continuityAreas);
    } catch (Exception e) {
      LOG.error("Schematic synchronization failed", e);
    }
    this.schematicProject = wrap(layoutProject.getOrCreateSchematicView(), layoutProject);
    revalidate();
    repaint();
  }

  public boolean hasContent() {
    return schematicProject != null && !schematicProject.getComponents().isEmpty();
  }

  public double getZoom() {
    return zoom;
  }

  public void setZoom(double zoom) {
    this.zoom = Math.max(0.1d, Math.min(5d, zoom));
    revalidate();
    repaint();
  }

  private static Project wrap(SchematicView view, Project layoutProject) {
    Project p = new Project();
    p.setTitle(layoutProject.getTitle() + " — Schematic");
    p.setAuthor(layoutProject.getAuthor());
    p.setWidth(view.getWidth());
    p.setHeight(view.getHeight());
    p.setGridSpacing(view.getGridSpacing());
    p.setFont(layoutProject.getFont());
    p.getComponents().addAll(view.getComponents());
    return p;
  }

  @Override
  public Dimension getPreferredSize() {
    if (schematicProject == null) {
      return new Dimension(800, 600);
    }
    return drawingManager.getCanvasDimensions(schematicProject, zoom, true);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (schematicProject == null) {
      return;
    }
    drawingManager.setZoomLevel(zoom);
    drawSchematic((Graphics2D) g);
  }

  private void drawSchematic(Graphics2D g2d) {
    Set<DrawOption> options = EnumSet.of(DrawOption.ANTIALIASING, DrawOption.ZOOM, DrawOption.GRID,
        DrawOption.EXTRA_SPACE);
    Set<IDIYComponent<?>> empty = Collections.emptySet();
    drawingManager.drawProject(g2d, schematicProject, options, null, null, empty, empty, empty,
        Arrays.<Point2D>asList(null, null), null, false, 1d, null, null);
  }

  /**
   * @return an image of the whole schematic at the current zoom, for export.
   */
  public BufferedImage renderToImage() {
    Dimension size = getPreferredSize();
    BufferedImage image =
        new BufferedImage(Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    g2d.setColor(java.awt.Color.white);
    g2d.fillRect(0, 0, size.width, size.height);
    drawingManager.setZoomLevel(zoom);
    drawSchematic(g2d);
    g2d.dispose();
    return image;
  }
}
