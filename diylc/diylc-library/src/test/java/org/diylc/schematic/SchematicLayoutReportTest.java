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
package org.diylc.schematic;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.DrawOption;
import org.diylc.components.connectivity.OrthogonalLine;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.presenter.ContinuityArea;
import org.diylc.presenter.DrawingManager;
import org.diylc.schematic.flow.SignalFlowAnalyzer;
import org.diylc.serialization.ProjectFileManager;
import org.junit.Test;

/**
 * Measures what the schematic generator actually produces for real projects, rather than for the
 * synthetic graphs the other tests build.
 *
 * <p>
 * This is a report, not an assertion. For each sample project it renders the layout offscreen to
 * collect continuity areas - without them most boards have no netlist at all, because pads joined by
 * copper are only connected through those areas - then generates a schematic and prints how many
 * columns came out, how large the busiest net is, how many components any flow rule recognised, and
 * how many wire segments run across a symbol.
 * </p>
 *
 * <p>
 * A layout collapsed into a single column shows up here immediately, which is the point: unit tests
 * over hand built graphs cannot see it. It skips silently when the regression data is absent, and it
 * never fails the build.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class SchematicLayoutReportTest {

  /** How many projects to report on; the corpus is large and this is a diagnostic. */
  private static final int SAMPLE_SIZE = 40;
  /** Nothing decorative is wanted, only enough of a render to populate the component areas. */
  private static final Set<DrawOption> DRAW_OPTIONS = EnumSet.noneOf(DrawOption.class);
  private static final int MAX_CANVAS = 4000;

  @Test
  public void reportLayoutQualityOverRealProjects() {
    File directory = findSampleDirectory();
    if (directory == null) {
      System.out.println("[layout-report] regression data not present, skipping");
      return;
    }
    File[] files = directory.listFiles((dir, name) -> name.endsWith(".diy"));
    if (files == null || files.length == 0) {
      System.out.println("[layout-report] no sample projects found, skipping");
      return;
    }
    Arrays.sort(files);
    ConfigurationManager.getInstance().initialize("diylc-test");

    System.out.println();
    System.out.printf("%-24s %6s %8s %7s %8s %9s %9s%n", "project", "symb", "columns", "nets",
        "maxNet", "flowKnown", "crossings");
    System.out.println("--------------------------------------------------------------------------------");

    int reported = 0;
    int singleColumn = 0;
    int totalCrossings = 0;
    int loadFailures = 0;
    int generateFailures = 0;
    for (File file : files) {
      if (reported >= SAMPLE_SIZE) {
        break;
      }
      Project project = load(file);
      if (project == null) {
        loadFailures++;
        continue;
      }
      Report report = analyze(project);
      if (report == null) {
        generateFailures++;
        continue;
      }
      reported++;
      if (report.columns <= 1) {
        singleColumn++;
      }
      totalCrossings += report.crossings;
      System.out.printf("%-24s %6d %8d %7d %8d %9d %9d%n", trim(file.getName()), report.symbols,
          report.columns, report.nets, report.largestNet, report.flowKnown, report.crossings);
    }

    System.out.println("--------------------------------------------------------------------------------");
    System.out.printf("reported %d   single-column %d   crossings %d   loadFailed %d   genFailed %d%n",
        reported, singleColumn, totalCrossings, loadFailures, generateFailures);
    System.out.println();
  }

  private static Project load(File file) {
    try {
      Project project = new ProjectFileManager(null)
          .deserializeProjectFromFile(file.getAbsolutePath(), new ArrayList<String>());
      return project == null || project.getComponents().isEmpty() ? null : project;
    } catch (Exception e) {
      return null;
    }
  }

  private static Report analyze(Project project) {
    List<ContinuityArea> areas;
    try {
      areas = continuityAreasOf(project);
    } catch (Exception e) {
      System.out.println("[layout-report] render failed: " + e);
      areas = new ArrayList<ContinuityArea>();
    }

    SchematicBuilder builder = new SchematicBuilder();
    try {
      builder.build(project, areas);
    } catch (Exception e) {
      System.out.println("[layout-report] generation failed: " + e);
      return null;
    }

    SchematicView view = project.getSchematicView();
    if (view == null) {
      return null;
    }

    Report report = new Report();
    List<Rectangle2D> bodies = new ArrayList<Rectangle2D>();
    Set<Long> columns = new LinkedHashSet<Long>();
    for (IDIYComponent<?> component : view.getComponents()) {
      if (component instanceof OrthogonalLine) {
        continue;
      }
      report.symbols++;
      Rectangle2D bounds = boundsOf(component);
      if (bounds != null) {
        bodies.add(bounds);
        // symbols are centred within their column, so the centre - not the left edge - is what is
        // shared by everything in the same column
        columns.add(Math.round(bounds.getCenterX() / 5));
      }
    }
    report.columns = columns.size();

    for (IDIYComponent<?> component : view.getComponents()) {
      if (!(component instanceof OrthogonalLine) || component.getControlPointCount() < 3) {
        continue;
      }
      OrthogonalLine wire = (OrthogonalLine) component;
      List<Point2D> vertices = WireRouter.vertices(wire.getControlPoint(0), wire.getControlPoint(1),
          wire.getControlPoint(2), wire.getStartDirection());
      for (int i = 1; i < vertices.size(); i++) {
        Line2D segment = new Line2D.Double(vertices.get(i - 1), vertices.get(i));
        for (Rectangle2D body : bodies) {
          // a wire ending on a pin touches that symbol's box legitimately, so only count a segment
          // that actually passes through
          if (body.getWidth() > 1 && body.getHeight() > 1 && body.intersectsLine(segment)
              && !body.contains(segment.getP1()) && !body.contains(segment.getP2())) {
            report.crossings++;
          }
        }
      }
    }

    Netlist netlist = builder.extractNetlist(project, areas);
    if (netlist != null) {
      report.nets = netlist.getGroups().size();
      for (Group group : netlist.getGroups()) {
        report.largestNet = Math.max(report.largestNet, group.getNodes().size());
      }
    }

    SignalFlowAnalyzer analyzer = new SignalFlowAnalyzer();
    for (IDIYComponent<?> component : project.getComponents()) {
      if (analyzer.analyze(component) != null) {
        report.flowKnown++;
      }
    }
    return report;
  }

  /**
   * Renders the layout offscreen purely so that the drawing manager records each component's area,
   * which is where continuity areas come from. Nothing looks at the image.
   */
  private static List<ContinuityArea> continuityAreasOf(Project project) {
    int width = clampCanvas(project.getWidth().convertToPixels());
    int height = clampCanvas(project.getHeight().convertToPixels());
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    try {
      DrawingManager manager =
          new DrawingManager(null, InMemoryConfigurationManager.getInstance());
      manager.drawProject(g2d, project, DRAW_OPTIONS, null, null,
          new HashSet<IDIYComponent<?>>(), new HashSet<IDIYComponent<?>>(),
          new HashSet<IDIYComponent<?>>(), null, null, false, null, null, null);
      return manager.getContinuityAreas();
    } finally {
      g2d.dispose();
    }
  }

  private static int clampCanvas(double pixels) {
    return (int) Math.min(MAX_CANVAS, Math.max(100, pixels));
  }

  private static Rectangle2D boundsOf(IDIYComponent<?> symbol) {
    int count = symbol.getControlPointCount();
    if (count == 0) {
      return null;
    }
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (int i = 0; i < count; i++) {
      Point2D p = symbol.getControlPoint(i);
      minX = Math.min(minX, p.getX());
      minY = Math.min(minY, p.getY());
      maxX = Math.max(maxX, p.getX());
      maxY = Math.max(maxY, p.getY());
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  private static File findSampleDirectory() {
    String[] candidates = {"../../diylc-regression-data/input/cloud/diy",
        "../diylc-regression-data/input/cloud/diy", "diylc-regression-data/input/cloud/diy"};
    for (String candidate : candidates) {
      File directory = new File(candidate);
      if (directory.isDirectory()) {
        return directory;
      }
    }
    return null;
  }

  private static String trim(String name) {
    return name.length() <= 24 ? name : name.substring(0, 21) + "...";
  }

  private static class Report {
    int symbols;
    int columns;
    int nets;
    int largestNet;
    int flowKnown;
    int crossings;
  }
}
