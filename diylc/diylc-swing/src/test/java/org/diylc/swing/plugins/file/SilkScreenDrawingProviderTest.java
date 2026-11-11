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
package org.diylc.swing.plugins.file;

import static org.junit.Assert.assertEquals;

import java.awt.Point;

import org.diylc.TestBase;
import org.diylc.common.PCBLayer;
import org.diylc.components.passive.Resistor;
import org.junit.Test;

public class SilkScreenDrawingProviderTest extends TestBase {

  @Test
  public void testOnePageWithoutBottomParts() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));

    assertEquals(1, new SilkScreenDrawingProvider(presenter).getPageCount());
  }

  @Test
  public void testBottomPageForBottomParts() {
    instantiateTwoClick("Passive", "Resistor", new Point(1000, 1000), new Point(1200, 1000));
    Resistor resistor = (Resistor) presenter.getCurrentProject().getComponents().get(0);
    resistor.setBoardSide(PCBLayer._1);

    assertEquals(2, new SilkScreenDrawingProvider(presenter).getPageCount());
  }
}
