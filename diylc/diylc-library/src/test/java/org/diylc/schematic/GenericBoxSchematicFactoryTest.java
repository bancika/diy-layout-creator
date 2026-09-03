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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.diylc.components.semiconductors.ICSymbol;
import org.diylc.components.schematic.SchematicBox;
import org.junit.Test;

public class GenericBoxSchematicFactoryTest {

  @Test
  public void producesOneBoxWithOnePinPerStickyNode() {
    ICSymbol physical = new ICSymbol();
    physical.setName("IC1");
    physical.setValue("TL072");

    List<SchematicSymbolMapping> mappings =
        new GenericBoxSchematicFactory().createSchematicSymbols(physical);

    assertEquals(1, mappings.size());
    SchematicSymbolMapping mapping = mappings.get(0);
    assertTrue(mapping.getSchematicSymbol() instanceof SchematicBox);
    assertNotNull(mapping.getSchematicSymbol().getId());

    int stickyNodes = 0;
    for (int i = 0; i < physical.getControlPointCount(); i++) {
      if (physical.isControlPointSticky(i) && physical.getControlPointNodeName(i) != null) {
        stickyNodes++;
      }
    }
    assertEquals(stickyNodes, mapping.getPinMapping().size());
    assertEquals(stickyNodes, mapping.getSchematicSymbol().getControlPointCount());
    assertEquals("IC1", mapping.getSchematicSymbol().getName());
  }

  @Test
  public void schematicBoxExposesConfiguredNodeNames() {
    SchematicBox box = new SchematicBox();
    box.setLeftNodes("IN+,IN-");
    box.setRightNodes("OUT");
    box.setTopNodes("");
    box.setBottomNodes("");
    assertEquals(3, box.getControlPointCount());
    assertEquals("IN+", box.getControlPointNodeName(0));
    assertEquals("IN-", box.getControlPointNodeName(1));
    assertEquals("OUT", box.getControlPointNodeName(2));
  }
}
