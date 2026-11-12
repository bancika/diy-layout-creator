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
package org.diylc.filter;

import static org.junit.Assert.assertTrue;

import org.diylc.common.PCBLayer;
import org.diylc.core.gerber.GerberExporterTest.SidedGerberComponent;
import org.diylc.core.gerber.GerberExporterTest.SimpleGerberComponent;
import org.diylc.presenter.DrawingManagerTest.MockBoard;
import org.junit.Test;

public class SilkScreenFilterTest {

  @Test
  public void testPartsGoToTheirOwnSide() {
    SidedGerberComponent top = new SidedGerberComponent(PCBLayer._2);
    SidedGerberComponent bottom = new SidedGerberComponent(PCBLayer._1);

    assertTrue(new SilkScreenFilter(PCBLayer._2).testComponent(top));
    assertTrue(!new SilkScreenFilter(PCBLayer._2).testComponent(bottom));
    assertTrue(new SilkScreenFilter(PCBLayer._1).testComponent(bottom));
    assertTrue(!new SilkScreenFilter(PCBLayer._1).testComponent(top));
  }

  @Test
  public void testPartsWithoutSideAreOnTop() {
    SimpleGerberComponent part = new SimpleGerberComponent(null);

    assertTrue(new SilkScreenFilter(PCBLayer._2).testComponent(part));
    assertTrue(!new SilkScreenFilter(PCBLayer._1).testComponent(part));
  }

  @Test
  public void testBoardOutlineOnBothSides() {
    MockBoard board = new ExportedBoard();

    assertTrue(new SilkScreenFilter(PCBLayer._2).testComponent(board));
    assertTrue(new SilkScreenFilter(PCBLayer._1).testComponent(board));
  }

  @Test
  public void testNoSideKeepsEverything() {
    assertTrue(new SilkScreenFilter().testComponent(new SidedGerberComponent(PCBLayer._1)));
    assertTrue(new SilkScreenFilter().testComponent(new SidedGerberComponent(PCBLayer._2)));
  }

  public static class ExportedBoard extends MockBoard {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean shouldExportToGerber() {
      return true;
    }
  }
}
