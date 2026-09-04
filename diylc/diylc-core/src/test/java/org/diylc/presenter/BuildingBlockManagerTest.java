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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.BlockInstantiationMode;
import org.diylc.common.IBlockProcessor;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.components.composite.CompositeComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.IDIYComponent;
import org.diylc.testcomponents.SwitchTestComponent;
import org.diylc.testcomponents.TwoPointTestComponent;
import org.junit.Before;
import org.junit.Test;

/**
 * Exercises {@link BuildingBlockManager#loadBlock} in both {@link BlockInstantiationMode}s (see
 * design decision D10 and section 4.4 of {@code docs/plans/composite-building-blocks.md}).
 */
@SuppressWarnings("unchecked")
public class BuildingBlockManagerTest {

  private static final String BLOCK_NAME = "TestBlock";

  private IConfigurationManager<?> configManager;
  private BuildingBlockManager manager;

  @Before
  public void setUp() {
    List<IDIYComponent<?>> storedComponents = Arrays.asList(
        new TwoPointTestComponent("R1", new Point2D.Double(0, 0), new Point2D.Double(10, 0)),
        new TwoPointTestComponent("R2", new Point2D.Double(20, 0), new Point2D.Double(30, 0)));
    Map<String, List<IDIYComponent<?>>> blocksMap = new HashMap<String, List<IDIYComponent<?>>>();
    blocksMap.put(BLOCK_NAME, storedComponents);

    configManager = mock(IConfigurationManager.class);
    when(configManager.readObject(eq(IPlugInPort.BLOCKS_KEY), org.mockito.ArgumentMatchers.any()))
        .thenReturn(blocksMap);

    manager = new BuildingBlockManager(configManager, null, new InstantiationManager());
  }

  @Test
  public void groupModeClonesEachComponentWithAFreshNameAndId() throws InvalidBlockException {
    List<IDIYComponent<?>> result =
        manager.loadBlock(BLOCK_NAME, new ArrayList<IDIYComponent<?>>(), BlockInstantiationMode.GROUP);

    assertEquals(2, result.size());
    // group mode always assigns fresh, project-unique names (pre-existing behavior, unchanged)
    assertEquals("TP1", result.get(0).getName());
    assertEquals("TP2", result.get(1).getName());
    assertNotEquals(result.get(0).getId(), result.get(1).getId());
  }

  @Test
  public void compositeModeWrapsChildrenInASingleComponent() throws InvalidBlockException {
    List<IDIYComponent<?>> result =
        manager.loadBlock(BLOCK_NAME, new ArrayList<IDIYComponent<?>>(), BlockInstantiationMode.COMPOSITE);

    assertEquals(1, result.size());
    assertTrue(result.get(0) instanceof CompositeComponent);
    CompositeComponent composite = (CompositeComponent) result.get(0);

    assertEquals(BLOCK_NAME, composite.getBlockName());
    assertEquals("BLK1", composite.getName());
    assertEquals(4, composite.getControlPointCount());
  }

  @Test
  public void compositeModePreservesChildNamesUnlikeGroupMode() throws InvalidBlockException {
    List<IDIYComponent<?>> result =
        manager.loadBlock(BLOCK_NAME, new ArrayList<IDIYComponent<?>>(), BlockInstantiationMode.COMPOSITE);
    CompositeComponent composite = (CompositeComponent) result.get(0);

    // children are namespaced under the composite's own name in the netlist, so their saved
    // names must survive untouched
    assertEquals("R1", composite.getChildComponents().get(0).getName());
    assertEquals("R2", composite.getChildComponents().get(1).getName());
  }

  @Test
  public void compositeModeGivesEachInstanceItsOwnNameButChildrenStayIdenticallyNamed()
      throws InvalidBlockException {
    List<IDIYComponent<?>> existing = new ArrayList<IDIYComponent<?>>();
    CompositeComponent first = (CompositeComponent) manager
        .loadBlock(BLOCK_NAME, existing, BlockInstantiationMode.COMPOSITE).get(0);
    existing.add(first);

    CompositeComponent second = (CompositeComponent) manager
        .loadBlock(BLOCK_NAME, existing, BlockInstantiationMode.COMPOSITE).get(0);

    assertNotEquals(first.getName(), second.getName());
    assertEquals(first.getChildComponents().get(0).getName(), second.getChildComponents().get(0).getName());
    // but the underlying child components must be distinct objects with distinct ids
    assertNotEquals(first.getChildComponents().get(0).getId(), second.getChildComponents().get(0).getId());
  }

  @Test(expected = InvalidBlockException.class)
  public void groupModeThrowsOnUnknownBlock() throws InvalidBlockException {
    manager.loadBlock("NoSuchBlock", new ArrayList<IDIYComponent<?>>(), BlockInstantiationMode.GROUP);
  }

  @Test(expected = InvalidBlockException.class)
  public void compositeModeThrowsOnUnknownBlock() throws InvalidBlockException {
    manager.loadBlock("NoSuchBlock", new ArrayList<IDIYComponent<?>>(), BlockInstantiationMode.COMPOSITE);
  }

  @Test
  public void deleteBlockAlsoStripsItFromTheRecentlyUsedList() {
    List<String> recent = new ArrayList<String>(Arrays.asList(
        IBlockProcessor.BLOCK_PREFIX + BLOCK_NAME, "org.diylc.components.SomeComponent"));
    when(configManager.readObject(eq(IPlugInPort.RECENT_COMPONENTS_KEY),
        org.mockito.ArgumentMatchers.any())).thenReturn(recent);

    manager.deleteBlock(BLOCK_NAME);

    assertFalse(recent.contains(IBlockProcessor.BLOCK_PREFIX + BLOCK_NAME));
    assertTrue(recent.contains("org.diylc.components.SomeComponent"));
    verify(configManager).writeValue(eq(IPlugInPort.RECENT_COMPONENTS_KEY), eq(recent));
  }

  @Test
  public void compositeModeToleratesAnInternalSwitchWithoutFailing() throws InvalidBlockException {
    // Design decision D6: the composite is not ISwitch, so a block containing a switch must
    // still place successfully - it just won't switch (see BuildingBlockManager's log warning).
    List<IDIYComponent<?>> storedComponents = Arrays.<IDIYComponent<?>>asList(new SwitchTestComponent("SW1"));
    Map<String, List<IDIYComponent<?>>> blocksMap = new HashMap<String, List<IDIYComponent<?>>>();
    blocksMap.put("SwitchBlock", storedComponents);
    when(configManager.readObject(eq(IPlugInPort.BLOCKS_KEY), org.mockito.ArgumentMatchers.any()))
        .thenReturn(blocksMap);

    List<IDIYComponent<?>> result = manager.loadBlock("SwitchBlock", new ArrayList<IDIYComponent<?>>(),
        BlockInstantiationMode.COMPOSITE);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(((CompositeComponent) result.get(0)).getChildComponents().get(0) instanceof ISwitch);
  }
}
