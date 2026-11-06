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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.BuildingBlock;
import org.diylc.common.IBlockProcessor;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.testcomponents.TwoPointTestComponent;
import org.junit.Before;
import org.junit.Test;

/**
 * Covers {@link BuildingBlockManager#importDefaultBlocks(List)}: factory blocks reach existing
 * users by their {@code createdOn}, withdrawn ones leave by their {@code deletedOn}, and the
 * recorded import date comes from the bundled file rather than the clock.
 */
public class DefaultBlockImportTest {

  private static final LocalDateTime OLDER_RELEASE = LocalDateTime.of(2026, 1, 1, 0, 0);
  private static final LocalDateTime NEWER_RELEASE = LocalDateTime.of(2026, 6, 1, 0, 0);

  private MapConfigurationManager configManager;
  private BuildingBlockManager manager;

  @Before
  public void setUp() {
    configManager = new MapConfigurationManager();
    manager = new BuildingBlockManager(configManager, null, null);
  }

  @Test
  public void freshInstallImportsEveryBlock() {
    manager.importDefaultBlocks(
        Arrays.asList(block("Old", OLDER_RELEASE), block("New", NEWER_RELEASE)));

    assertTrue(blocks().containsKey("Old"));
    assertTrue(blocks().containsKey("New"));
    assertEquals(NEWER_RELEASE, importDate());
    assertTrue(configManager.readBoolean(IPlugInPort.DEFAULT_BLOCKS_IMPORTED_KEY, false));
  }

  @Test
  public void configFromTheOneTimeImportGetsTheMissingBlocksWithoutDuplicates() {
    List<IDIYComponent<?>> kept = components();
    Map<String, List<IDIYComponent<?>>> userBlocks = new HashMap<String, List<IDIYComponent<?>>>();
    userBlocks.put("Old", kept);
    configManager.writeValue(IPlugInPort.BLOCKS_KEY, userBlocks);
    configManager.writeValue(IPlugInPort.DEFAULT_BLOCKS_IMPORTED_KEY, true);

    manager.importDefaultBlocks(
        Arrays.asList(block("Old", OLDER_RELEASE), block("New", OLDER_RELEASE)));

    assertEquals(2, blocks().size());
    assertSame(kept, blocks().get("Old"));
    assertTrue(blocks().containsKey("New"));
  }

  @Test
  public void blockOfANewerReleaseArrivesAfterAnOlderReleaseRanFirst() {
    manager.importDefaultBlocks(Arrays.asList(block("Old", OLDER_RELEASE)));
    manager.importDefaultBlocks(
        Arrays.asList(block("Old", OLDER_RELEASE), block("New", NEWER_RELEASE)));

    assertTrue(blocks().containsKey("New"));
    assertEquals(NEWER_RELEASE, importDate());
  }

  @Test
  public void olderReleaseRunAfterANewerOneLeavesTheImportDate() {
    manager.importDefaultBlocks(
        Arrays.asList(block("Old", OLDER_RELEASE), block("New", NEWER_RELEASE)));
    manager.importDefaultBlocks(Arrays.asList(block("Old", OLDER_RELEASE)));

    assertEquals(NEWER_RELEASE, importDate());
  }

  @Test
  public void factoryBlockDeletedByTheUserIsNotImportedAgain() {
    manager.importDefaultBlocks(Arrays.asList(block("Old", OLDER_RELEASE)));
    blocks().remove("Old");

    manager.importDefaultBlocks(Arrays.asList(block("Old", OLDER_RELEASE)));

    assertFalse(blocks().containsKey("Old"));
  }

  @Test
  public void userBlockOfTheSameNameIsKept() {
    List<IDIYComponent<?>> own = components();
    Map<String, List<IDIYComponent<?>>> userBlocks = new HashMap<String, List<IDIYComponent<?>>>();
    userBlocks.put("New", own);
    configManager.writeValue(IPlugInPort.BLOCKS_KEY, userBlocks);

    manager.importDefaultBlocks(Arrays.asList(block("New", OLDER_RELEASE)));

    assertSame(own, blocks().get("New"));
  }

  @Test
  public void withdrawnBlockIsRemovedFromTheRecentlyUsedListToo() {
    manager.importDefaultBlocks(Arrays.asList(block("Old", OLDER_RELEASE)));
    List<String> recent =
        new ArrayList<String>(Arrays.asList(IBlockProcessor.BLOCK_PREFIX + "Old"));
    configManager.writeValue(IPlugInPort.RECENT_COMPONENTS_KEY, recent);

    manager.importDefaultBlocks(Arrays.asList(withdrawn("Old", NEWER_RELEASE)));

    assertFalse(blocks().containsKey("Old"));
    assertTrue(recent.isEmpty());
    assertEquals(NEWER_RELEASE, importDate());
  }

  @Test
  public void blockRecreatedByTheUserAfterTheWithdrawalIsKept() {
    manager.importDefaultBlocks(Arrays.asList(withdrawn("Old", OLDER_RELEASE)));
    blocks().put("Old", components());

    manager.importDefaultBlocks(Arrays.asList(withdrawn("Old", OLDER_RELEASE)));

    assertTrue(blocks().containsKey("Old"));
  }

  @Test
  public void blockCreatedAndWithdrawnSinceTheLastImportNeverArrives() {
    BuildingBlock shortLived = withdrawn("Short-lived", NEWER_RELEASE);
    shortLived.setCreatedOn(OLDER_RELEASE);

    manager.importDefaultBlocks(Arrays.asList(shortLived));

    assertFalse(blocks().containsKey("Short-lived"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<IDIYComponent<?>>> blocks() {
    return (Map<String, List<IDIYComponent<?>>>) configManager.readObject(IPlugInPort.BLOCKS_KEY,
        null);
  }

  private LocalDateTime importDate() {
    return (LocalDateTime) configManager.readObject(IPlugInPort.DEFAULT_BLOCKS_IMPORT_DATE_KEY,
        null);
  }

  private static List<IDIYComponent<?>> components() {
    return new ArrayList<IDIYComponent<?>>(Arrays.asList(
        new TwoPointTestComponent("R1", new Point2D.Double(0, 0), new Point2D.Double(10, 0))));
  }

  private static BuildingBlock block(String name, LocalDateTime createdOn) {
    BuildingBlock block = new BuildingBlock(name, components());
    block.setCreatedOn(createdOn);
    return block;
  }

  private static BuildingBlock withdrawn(String name, LocalDateTime deletedOn) {
    BuildingBlock block = new BuildingBlock(name, null);
    block.setDeletedOn(deletedOn);
    return block;
  }

  private static class MapConfigurationManager implements IConfigurationManager<Object> {

    private final Map<String, Object> values = new HashMap<String, Object>();

    @Override
    public void addConfigListener(String key, IConfigListener listener) {}

    @Override
    public boolean readBoolean(String key, boolean defaultValue) {
      return (Boolean) readObject(key, defaultValue);
    }

    @Override
    public String readString(String key, String defaultValue) {
      return (String) readObject(key, defaultValue);
    }

    @Override
    public int readInt(String key, int defaultValue) {
      return (Integer) readObject(key, defaultValue);
    }

    @Override
    public float readFloat(String key, float defaultValue) {
      return (Float) readObject(key, defaultValue);
    }

    @Override
    public double readDouble(String key, double defaultValue) {
      return (Double) readObject(key, defaultValue);
    }

    @Override
    public Object readObject(String key, Object defaultValue) {
      return values.containsKey(key) ? values.get(key) : defaultValue;
    }

    @Override
    public void writeValue(String key, Object value) {
      values.put(key, value);
    }

    @Override
    public Object getSerializer() {
      return null;
    }

    @Override
    public void initialize(String appName) {}
  }
}
