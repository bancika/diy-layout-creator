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
package org.diylc.components.connectivity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.diylc.TestConfiguration;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.PCBLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.presenter.DrawingManager;
import org.junit.Before;
import org.junit.Test;

public class CopperLayerContinuityTest {

  private static final Point2D JOINT = new Point2D.Double(300, 100);
  private static final Point2D ON_TOP_TRACE = new Point2D.Double(150, 100);
  private static final Point2D ON_BOTTOM_TRACE = new Point2D.Double(450, 100);

  @Before
  public void setUp() {
    TestConfiguration.initialize();
  }

  @Test
  public void testTracesOnDifferentLayersDoNotConnect() {
    assertFalse(areConnected(null));
  }

  @Test
  public void testPlatedPadJoinsBothLayers() {
    assertTrue(areConnected(createPad(SolderPad.HoleType.PTH)));
  }

  @Test
  public void testNonPlatedPadStaysOnItsLayer() {
    assertFalse(areConnected(createPad(SolderPad.HoleType.NTPH)));
  }

  @Test
  public void testViaJoinsBothLayers() {
    Via via = new Via();
    via.setControlPoint(JOINT, 0);
    assertTrue(areConnected(via));
  }

  private static SolderPad createPad(SolderPad.HoleType holeType) {
    SolderPad pad = new SolderPad();
    pad.setLayer(PCBLayer._1);
    pad.setHoleType(holeType);
    pad.setControlPoint(JOINT, 0);
    return pad;
  }

  // a top trace and a bottom trace that meet at the joint
  private static boolean areConnected(IDIYComponent<?> joint) {
    Project project = new Project();
    project.getComponents().add(createTrace(PCBLayer._2, new Point2D.Double(100, 100), JOINT));
    project.getComponents().add(createTrace(PCBLayer._1, JOINT, new Point2D.Double(500, 100)));
    if (joint != null) {
      project.getComponents().add(joint);
    }
    DrawingManager drawingManager = new DrawingManager(new MessageDispatcher<EventType>(false),
        InMemoryConfigurationManager.getInstance());
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, new Rectangle2D.Double(0, 0, 1000, 1000));
    g2d.dispose();
    List<Area> areas = drawingManager.getContinuityGraph(project).findAreasFor(ON_TOP_TRACE);
    return areas.stream().anyMatch(area -> area.contains(ON_BOTTOM_TRACE));
  }

  private static CopperTrace createTrace(PCBLayer layer, Point2D start, Point2D end) {
    CopperTrace trace = new CopperTrace();
    trace.setLayer(layer);
    trace.setControlPoint(start, 0);
    trace.setControlPoint(end, 1);
    return trace;
  }
}
