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
package org.diylc.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.diylc.serialization.ProjectFileManager;
import org.junit.Test;

public class LayerIdsTest {

  private static final int LEGACY = LayerIds.LEGACY_TRACES;
  private static final int BOTTOM = LayerIds.BOTTOM_TRACES;
  private static final int TOP = LayerIds.TOP_TRACES;

  @Test
  public void testLegacyTraceLayerStandsForBothSides() {
    assertEquals(set(LEGACY, BOTTOM, TOP, IDIYComponent.BOARD),
        migrate(LEGACY, IDIYComponent.BOARD));
  }

  @Test
  public void testBothSidesWithoutLegacyMeansAnOlderVersionClearedTraces() {
    assertEquals(set(IDIYComponent.BOARD), migrate(BOTTOM, TOP, IDIYComponent.BOARD));
  }

  @Test
  public void testOneSideIsKept() {
    assertEquals(set(TOP), migrate(TOP));
  }

  @Test
  public void testLegacyFollowsBothSides() {
    assertEquals(set(LEGACY, BOTTOM, TOP), sync(BOTTOM, TOP));
    assertEquals(set(TOP), sync(LEGACY, TOP));
    assertEquals(set(), sync(LEGACY));
  }

  @Test
  public void testLoadedProjectIsMigrated() {
    Project project = new Project();
    project.getHiddenLayers().add(LEGACY);
    project.getLockedLayers().addAll(set(BOTTOM, TOP));

    XStream xStream = new XStream(new DomDriver("UTF-8"));
    ProjectFileManager.configure(xStream);
    Project loaded = (Project) xStream.fromXML(xStream.toXML(project));

    assertEquals(set(LEGACY, BOTTOM, TOP), loaded.getHiddenLayers());
    assertEquals(set(), loaded.getLockedLayers());
  }

  private static Set<Integer> migrate(Integer... layers) {
    Set<Integer> set = set(layers);
    LayerIds.migrateLoaded(set);
    return set;
  }

  private static Set<Integer> sync(Integer... layers) {
    Set<Integer> set = set(layers);
    LayerIds.syncLegacy(set);
    return set;
  }

  private static Set<Integer> set(Integer... layers) {
    return new HashSet<Integer>(Arrays.asList(layers));
  }
}
