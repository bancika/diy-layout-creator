/*
 *
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.components.semiconductors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.netlist.Node;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

public class MultiwattICDatasheetTest {

  private static final int FIRST_PIN_COLUMN = 2;

  @Test
  public void everyRowApplies() {
    for (String[] row : datasheet()) {
      MultiwattIC ic = new MultiwattIC(row);
      String context = String.join(",", row);
      Assert.assertEquals(context, row[1], ic.getValue());
      Assert.assertEquals(context, row.length - FIRST_PIN_COLUMN, ic.getControlPointCount());
      for (int i = 0; i < ic.getControlPointCount(); i++) {
        Assert.assertFalse(context, ic.getControlPointNodeName(i).matches("\\d+"));
      }
    }
  }

  @Test
  public void partNumbersAreUnique() {
    Set<String> parts = new HashSet<String>();
    for (String[] row : datasheet()) {
      Assert.assertTrue("repeated part " + row[1], parts.add(row[1].toUpperCase()));
    }
  }

  /** Two pins share a net only when both say so with a numbered suffix, like GND_1 and GND_2. */
  @Test
  public void pinsShareANetOnlyThroughTheSuffix() {
    for (String[] row : datasheet()) {
      for (int i = FIRST_PIN_COLUMN; i < row.length; i++) {
        for (int j = i + 1; j < row.length; j++) {
          String rail = Node.railName(row[i]);
          if (rail != null && rail.equals(Node.railName(row[j]))) {
            Assert.assertTrue(row[1] + " pins " + row[i] + " and " + row[j],
                row[i].matches(".*_\\d+") && row[j].matches(".*_\\d+"));
          }
        }
      }
    }
  }

  @Test
  public void partIsFoundByNumberNotByGroup() {
    DatasheetService service = DatasheetService.getInstance();
    Assert.assertEquals(1, service.findSeries(MultiwattIC.class, "LM3886").size());
    Assert.assertTrue(service.findSeries(MultiwattIC.class, "Power Amp").isEmpty());
  }

  private static List<String[]> datasheet() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(MultiwattIC.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    return datasheet;
  }
}
