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
package org.diylc.presenter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.diylc.common.PCBLayer;
import org.diylc.components.composite.CompositeComponent;
import org.diylc.components.connectivity.CopperTrace;
import org.diylc.components.connectivity.Eyelet;
import org.diylc.components.connectivity.Jumper;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.components.connectivity.Turret;
import org.diylc.components.connectivity.Via;
import org.diylc.components.passive.Resistor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.LayerIds;
import org.junit.Test;

public class ComponentLayersTest {

  private static final int BOTTOM = LayerIds.BOTTOM_TRACES;
  private static final int TOP = LayerIds.TOP_TRACES;

  @Test
  public void testCopperIsOnTheLayerOfItsSide() {
    assertOnlyOn(trace(PCBLayer._1), BOTTOM);
    assertOnlyOn(trace(PCBLayer._2), TOP);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testInnerLayerCopperIsOnTheBottom() {
    assertOnlyOn(trace(PCBLayer._5), BOTTOM);
  }

  @Test
  public void testComponentsThroughTheBoardAreOnBothTraceLayers() {
    assertOnBoth(pad(SolderPad.HoleType.PTH));
    assertOnBoth(new Via());
    assertOnBoth(new Eyelet());
    assertOnBoth(new Turret());
  }

  @Test
  public void testNonPlatedPadIsOnItsSide() {
    assertOnlyOn(pad(SolderPad.HoleType.NTPH), BOTTOM);
  }

  @Test
  public void testJumperIsOnTopTraces() {
    assertOnlyOn(new Jumper(), TOP);
  }

  @Test
  public void testTraceBlockIsOnBothTraceLayers() {
    CompositeComponent block = new CompositeComponent();
    block.getChildComponents().addAll(Arrays.asList(trace(PCBLayer._1), trace(PCBLayer._2)));
    assertOnBoth(block);
  }

  @Test
  public void testOtherComponentsKeepTheirZOrderLayer() {
    Resistor resistor = new Resistor();
    assertTrue(ComponentLayers.isOnLayer(resistor, IDIYComponent.COMPONENT));
    assertFalse(ComponentLayers.isOnLayer(resistor, TOP));
    assertTrue(ComponentLayers.isHidden(set(IDIYComponent.COMPONENT), resistor));
    assertTrue(ComponentLayers.isLocked(set(IDIYComponent.COMPONENT), resistor));
  }

  @Test
  public void testComponentOnBothIsHiddenWithBothAndLockedWithEither() {
    Via via = new Via();
    assertFalse(ComponentLayers.isHidden(set(BOTTOM), via));
    assertTrue(ComponentLayers.isHidden(set(BOTTOM, TOP), via));
    assertTrue(ComponentLayers.isLocked(set(BOTTOM), via));
    assertTrue(ComponentLayers.isLocked(set(TOP), via));
    assertFalse(ComponentLayers.isLocked(set(IDIYComponent.COMPONENT), via));
  }

  @Test
  public void testJumperStaysUnlockedWithOnlyBottomTracesLocked() {
    assertFalse(ComponentLayers.isLocked(set(BOTTOM), new Jumper()));
  }

  private static void assertOnlyOn(IDIYComponent<?> component, int layer) {
    int other = layer == BOTTOM ? TOP : BOTTOM;
    assertTrue(ComponentLayers.isOnLayer(component, layer));
    assertFalse(ComponentLayers.isOnLayer(component, other));
    assertFalse(ComponentLayers.isOnLayer(component, IDIYComponent.TRACE));
    assertTrue(ComponentLayers.isHidden(set(layer), component));
    assertFalse(ComponentLayers.isHidden(set(other), component));
    assertTrue(ComponentLayers.isLocked(set(layer), component));
    assertFalse(ComponentLayers.isLocked(set(other), component));
  }

  private static void assertOnBoth(IDIYComponent<?> component) {
    assertTrue(ComponentLayers.isOnLayer(component, BOTTOM));
    assertTrue(ComponentLayers.isOnLayer(component, TOP));
  }

  private static CopperTrace trace(PCBLayer layer) {
    CopperTrace trace = new CopperTrace();
    trace.setLayer(layer);
    return trace;
  }

  private static SolderPad pad(SolderPad.HoleType holeType) {
    SolderPad pad = new SolderPad();
    pad.setLayer(PCBLayer._1);
    pad.setHoleType(holeType);
    return pad;
  }

  private static Set<Integer> set(Integer... layers) {
    return new HashSet<Integer>(Arrays.asList(layers));
  }
}
