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
package org.diylc.netlist;

import java.util.Arrays;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.diylc.components.passive.Resistor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;

public class SpiceSumarizerTests {

  private Locale defaultLocale;

  @Before
  public void setUp() {
    defaultLocale = Locale.getDefault();
  }

  @After
  public void tearDown() {
    Locale.setDefault(defaultLocale);
  }

  @Test
  public void testDecimalValueIgnoresLocale() throws TreeException {
    Locale.setDefault(Locale.GERMANY);

    Resistor resistor = new Resistor();
    resistor.setName("R1");
    resistor.setValue(new Resistance(4.7d, ResistanceUnit.K));

    Netlist netlist = new Netlist(Arrays.<IDIYComponent<?>>asList(resistor));
    netlist.add(new Group(new Node(resistor, 0), new Node(resistor, 1)));

    String spice = SpiceSumarizer.summarize(netlist, false);
    Assert.assertTrue(spice, spice.contains(" 4.7K "));
  }
}
