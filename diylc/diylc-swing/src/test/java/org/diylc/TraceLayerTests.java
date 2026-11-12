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
package org.diylc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.diylc.common.PCBLayer;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.connectivity.CopperTrace;
import org.diylc.components.connectivity.Jumper;
import org.diylc.components.connectivity.Via;
import org.diylc.core.LayerIds;
import org.diylc.core.Project;
import org.junit.Before;
import org.junit.Test;

public class TraceLayerTests extends TestBase {

  private CopperTrace bottomTrace;
  private CopperTrace topTrace;
  private Via via;
  private Jumper jumper;

  @Before
  public void setUp() {
    bottomTrace = trace(PCBLayer._1);
    topTrace = trace(PCBLayer._2);
    via = new Via();
    jumper = new Jumper();
    presenter.getCurrentProject().getComponents()
        .addAll(Arrays.asList(bottomTrace, topTrace, via, jumper));
  }

  @Test
  public void testSelectAllOfATraceLayer() {
    presenter.selectAll(LayerIds.BOTTOM_TRACES);
    assertEquals(new HashSet<Object>(Arrays.asList(bottomTrace, via)),
        new HashSet<Object>(presenter.getSelectedComponents()));

    presenter.selectAll(LayerIds.TOP_TRACES);
    assertEquals(new HashSet<Object>(Arrays.asList(topTrace, via, jumper)),
        new HashSet<Object>(presenter.getSelectedComponents()));
  }

  @Test
  public void testLockingBottomTracesLocksTheViaButNotTheTopSide() {
    presenter.setLayerLocked(LayerIds.BOTTOM_TRACES, true);

    presenter.selectAll(0);
    assertEquals(new HashSet<Object>(Arrays.asList(topTrace, jumper)),
        new HashSet<Object>(presenter.getSelectedComponents()));
    assertEquals(new HashSet<Integer>(Arrays.asList(LayerIds.BOTTOM_TRACES)),
        presenter.getCurrentProject().getLockedLayers());
  }

  @Test
  public void testBothSidesKeepTheLegacyLayerForOlderVersions() {
    Project project = presenter.getCurrentProject();
    presenter.setLayerVisibility(LayerIds.BOTTOM_TRACES, false);
    presenter.setLayerVisibility(LayerIds.TOP_TRACES, false);
    assertTrue(project.getHiddenLayers().contains(LayerIds.LEGACY_TRACES));

    presenter.setLayerVisibility(LayerIds.TOP_TRACES, true);
    assertEquals(new HashSet<Integer>(Arrays.asList(LayerIds.BOTTOM_TRACES)),
        project.getHiddenLayers());
  }

  @Test
  public void testTraceMovedToALockedSideIsLocked() {
    presenter.setLayerLocked(LayerIds.BOTTOM_TRACES, true);
    presenter.selectAll(0);
    assertTrue(presenter.getSelectedComponents().contains(topTrace));

    List<PropertyWrapper> properties = presenter.getProperties(topTrace);
    for (PropertyWrapper property : properties) {
      if (property.getName().equals("Layer")) {
        property.setValue(PCBLayer._1);
      }
    }
    presenter.applyProperties(topTrace, properties);

    presenter.selectAll(0);
    assertFalse(presenter.getSelectedComponents().contains(topTrace));
  }

  private static CopperTrace trace(PCBLayer layer) {
    CopperTrace trace = new CopperTrace();
    trace.setLayer(layer);
    return trace;
  }
}
