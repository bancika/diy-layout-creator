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
package org.diylc.components;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.TestConfiguration;
import org.diylc.common.BuildingBlock;
import org.diylc.serialization.ProjectFileManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Checks that {@code import-defaults/blocks.xml} loads as factory blocks, each either a full block
 * with its {@code createdOn} or a withdrawal stub carrying only its name and {@code deletedOn}.
 */
public class DefaultBlocksTest {

  @BeforeClass
  public static void setUp() {
    TestConfiguration.initialize();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void everyFactoryBlockIsCompleteOrAStub() {
    InputStream in = DefaultBlocksTest.class.getClassLoader()
        .getResourceAsStream("import-defaults/blocks.xml");
    Assert.assertNotNull("blocks.xml is not on the classpath", in);
    List<BuildingBlock> blocks =
        (List<BuildingBlock>) ProjectFileManager.xStreamSerializer.fromXML(in);

    Assert.assertFalse(blocks.isEmpty());
    Set<String> names = new HashSet<String>();
    for (BuildingBlock block : blocks) {
      Assert.assertNotNull(block.getName());
      Assert.assertTrue("duplicate " + block.getName(), names.add(block.getName()));
      if (block.getDeletedOn() == null) {
        Assert.assertNotNull(block.getName(), block.getCreatedOn());
        Assert.assertNotNull(block.getName(), block.getComponents());
        Assert.assertFalse(block.getName(), block.getComponents().isEmpty());
      } else {
        Assert.assertNull(block.getName(), block.getComponents());
      }
    }
  }
}
