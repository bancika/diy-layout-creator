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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.netlist.Node;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

public class TubeSocketDatasheetTest {

  private static final int FIRST_PIN_COLUMN = 4;
  private static final Set<String> UNUSED_PIN_NAMES =
      new HashSet<String>(Arrays.asList("NC", "IC", "SH"));

  @Test
  public void everyRowNamesEveryPinOfItsBase() {
    for (String[] row : datasheet()) {
      TubeSocket socket = new TubeSocket(row);
      String context = String.join(",", row);
      Assert.assertEquals(context, row[1], socket.getValue());
      // control point 0 is the socket centre
      Assert.assertEquals(context, row.length - FIRST_PIN_COLUMN,
          socket.getControlPointCount() - 1);
    }
  }

  @Test
  public void designationsAreUnique() {
    Set<String> tubes = new HashSet<String>();
    for (String[] row : datasheet()) {
      Assert.assertTrue("repeated tube " + row[1], tubes.add(row[1].toUpperCase()));
    }
  }

  /** Two pins share a net only when both say so with a numbered suffix, like K_3 and K_8. */
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

  /** A basing code fixes what each pin is, so every tube on one basing names its pins alike. */
  @Test
  public void tubesOnOneBasingNameTheirPinsAlike() {
    Map<String, String[]> byBasing = new HashMap<String, String[]>();
    for (String[] row : datasheet()) {
      // "-" marks a tube with no RETMA basing, e.g. most Soviet types
      if (row[3].equals("-")) {
        continue;
      }
      String key = row[2] + " " + row[3];
      String[] names = Arrays.copyOfRange(row, FIRST_PIN_COLUMN, row.length);
      // a basing fixes the electrodes; an unused pin is the metal shell, a no-connect or an
      // internal connection depending on the envelope and the maker
      for (int i = 0; i < names.length; i++) {
        if (UNUSED_PIN_NAMES.contains(names[i])) {
          names[i] = "unused";
        }
      }
      String[] first = byBasing.putIfAbsent(key, names);
      if (first != null) {
        Assert.assertArrayEquals(row[1] + " on " + key, first, names);
      }
    }
  }

  @Test
  public void tubeIsFoundByTypeNotByGroup() {
    DatasheetService service = DatasheetService.getInstance();
    String[] first = datasheet().get(0);
    Assert.assertEquals(1, service.findSeries(TubeSocket.class, first[1]).size());
    Assert.assertTrue(service.findSeries(TubeSocket.class, first[0]).isEmpty());
  }

  private static List<String[]> datasheet() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(TubeSocket.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    return datasheet;
  }
}
