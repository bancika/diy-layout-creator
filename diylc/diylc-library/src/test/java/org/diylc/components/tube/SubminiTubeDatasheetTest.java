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
package org.diylc.components.tube;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.netlist.Node;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

public class SubminiTubeDatasheetTest {

  private static final int FIRST_LEAD_COLUMN = 7;

  @Test
  public void everyRowApplies() {
    for (String[] row : datasheet()) {
      SubminiTube tube = new SubminiTube(row);
      String context = String.join(",", row);
      Assert.assertEquals(context, row[1], tube.getValue());
      Assert.assertTrue(context, row[3].equals("yes") || row[3].equals("no"));
      int leads = row.length - FIRST_LEAD_COLUMN - (tube.getTopLead() ? 1 : 0);
      Assert.assertEquals(context, leads, tube.getPinCount().getValue());
    }
  }

  @Test
  public void tubesAreUnique() {
    Set<String> tubes = new HashSet<String>();
    for (String[] row : datasheet()) {
      Assert.assertTrue("repeated tube " + row[1], tubes.add(row[1].toUpperCase()));
    }
  }

  /** Two leads share a net only when both say so with a numbered suffix, like K_1 and K_2. */
  @Test
  public void leadsShareANetOnlyThroughTheSuffix() {
    for (String[] row : datasheet()) {
      for (int i = FIRST_LEAD_COLUMN; i < row.length; i++) {
        for (int j = i + 1; j < row.length; j++) {
          String rail = Node.railName(row[i]);
          if (rail != null && rail.equals(Node.railName(row[j]))) {
            Assert.assertTrue(row[1] + " leads " + row[i] + " and " + row[j],
                row[i].matches(".*_\\d+") && row[j].matches(".*_\\d+"));
          }
        }
      }
    }
  }

  @Test
  public void tubeIsFoundByTypeNotByGroup() {
    DatasheetService service = DatasheetService.getInstance();
    String[] first = datasheet().get(0);
    Assert.assertEquals(1, service.findSeries(SubminiTube.class, first[1]).size());
    Assert.assertTrue(service.findSeries(SubminiTube.class, first[0]).isEmpty());
  }

  private static List<String[]> datasheet() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(SubminiTube.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    return datasheet;
  }
}
