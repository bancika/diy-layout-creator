/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.plugins.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.utils.BomEntry;
import org.diylc.utils.BomMaker;
import org.diylc.testcomponents.MockDIYComponent;

public class BomMakerTest {

  @Test
  public void testCreateBom() {
    List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
    MockDIYComponent c1 = new MockDIYComponent();
    c1.setValue(new Capacitance(3d, CapacitanceUnit.pF));
    components.add(c1);
    components.add(new MockDIYComponent());
    components.add(new MockDIYComponent());
    List<BomEntry> bom = BomMaker.getInstance().createBom(components);
    assertNotNull(bom);
    assertEquals(2, bom.size());
    Collections.sort(bom, new Comparator<BomEntry>() {

      @Override
      public int compare(BomEntry o1, BomEntry o2) {
        return o1.getQuantity().compareTo(o2.getQuantity());
      }});
    BomEntry entry1 = bom.get(0);
    assertEquals("something", entry1.getName());
    assertEquals(1, (int) entry1.getQuantity());
    BomEntry entry2 = bom.get(1);
    assertEquals("something, something", entry2.getName());
    assertEquals(2, (int) entry2.getQuantity());
  }
}
