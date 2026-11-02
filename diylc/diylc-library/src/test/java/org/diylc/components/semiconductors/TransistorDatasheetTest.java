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
package org.diylc.components.semiconductors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.core.IDIYComponent;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

/** The datasheets of all transistor packages, which share one row format. */
public class TransistorDatasheetTest {

  private static final List<Class<? extends IDIYComponent<?>>> PACKAGES =
      Arrays.asList(TransistorTO1.class, TransistorTO3.class, TransistorTO92.class,
          TransistorTO126.class, TransistorTO220.class);

  @Test
  public void everyRowApplies() throws Exception {
    for (Class<? extends IDIYComponent<?>> type : PACKAGES) {
      for (String[] row : datasheet(type)) {
        String context = type.getSimpleName() + ": " + String.join(",", row);
        Assert.assertEquals(context, 3, row.length);
        IDIYComponent<?> transistor =
            type.getConstructor(String[].class).newInstance((Object) row);
        Assert.assertEquals(context, row[1], transistor.getValue());
        String pinout = TransistorPinout.valueOf(row[2]).toPinout();
        for (int i = 0; i < pinout.length(); i++) {
          Assert.assertEquals(context, pinout.substring(i, i + 1),
              transistor.getControlPointNodeName(i));
        }
      }
    }
  }

  @Test
  public void partNumbersAreUnique() {
    for (Class<? extends IDIYComponent<?>> type : PACKAGES) {
      Set<String> parts = new HashSet<String>();
      for (String[] row : datasheet(type)) {
        Assert.assertTrue(type.getSimpleName() + " repeats " + row[1],
            parts.add(row[1].toUpperCase()));
      }
    }
  }

  @Test
  public void partIsFoundByNumberNotByGroup() {
    DatasheetService service = DatasheetService.getInstance();
    for (Class<? extends IDIYComponent<?>> type : PACKAGES) {
      String[] first = datasheet(type).get(0);
      Assert.assertEquals(1, service.findSeries(type, first[1]).size());
      Assert.assertTrue(service.findSeries(type, first[0]).isEmpty());
    }
  }

  private static List<String[]> datasheet(Class<? extends IDIYComponent<?>> type) {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(type);
    Assert.assertNotNull(type.getSimpleName(), datasheet);
    Assert.assertFalse(type.getSimpleName(), datasheet.isEmpty());
    return datasheet;
  }
}
