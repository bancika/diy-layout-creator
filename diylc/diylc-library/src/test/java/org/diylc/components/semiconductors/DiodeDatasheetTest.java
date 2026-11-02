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

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.core.measures.Size;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

/** The datasheets of the plastic and glass diodes, which share their first seven columns. */
public class DiodeDatasheetTest {

  @Test
  public void everyPlasticRowApplies() {
    for (String[] row : datasheet(DiodePlastic.class)) {
      DiodePlastic diode = new DiodePlastic(row);
      String context = String.join(",", row);
      Assert.assertEquals(context, 7, row.length);
      Assert.assertEquals(context, row[1], diode.getValue());
      Assert.assertEquals(context, Size.parseSize(row[3]), diode.getLength());
      Assert.assertEquals(context, Color.decode(row[6]), diode.getMarkerColor());
    }
  }

  @Test
  public void everyGlassRowApplies() {
    for (String[] row : datasheet(DiodeGlass.class)) {
      DiodeGlass diode = new DiodeGlass(row);
      String context = String.join(",", row);
      Assert.assertEquals(context, 8, row.length);
      Assert.assertEquals(context, row[1], diode.getValue());
      Assert.assertEquals(context, Size.parseSize(row[4]), diode.getWidth());
      Assert.assertEquals(context, Color.decode(row[7]), diode.getInsideColor());
    }
  }

  /** A part ships in one envelope, so it is listed under one component only. */
  @Test
  public void partNumbersAreUniqueAcrossBothFiles() {
    Set<String> parts = new HashSet<String>();
    for (Class<?> type : new Class<?>[] {DiodePlastic.class, DiodeGlass.class}) {
      for (String[] row : datasheet(type)) {
        Assert.assertTrue("repeated part " + row[1], parts.add(row[1].toUpperCase()));
      }
    }
  }

  /** Packages are standard outlines, so every part in one package is drawn the same size. */
  @Test
  public void onePackageHasOneSize() {
    for (Class<?> type : new Class<?>[] {DiodePlastic.class, DiodeGlass.class}) {
      Map<String, String> sizes = new HashMap<String, String>();
      for (String[] row : datasheet(type)) {
        String size = row[3] + " x " + row[4];
        String first = sizes.putIfAbsent(row[2], size);
        if (first != null) {
          Assert.assertEquals(row[1] + " in " + row[2], first, size);
        }
      }
    }
  }

  @Test
  public void partIsFoundByNumberNotByGroup() {
    DatasheetService service = DatasheetService.getInstance();
    for (Class<?> type : new Class<?>[] {DiodePlastic.class, DiodeGlass.class}) {
      String[] first = datasheet(type).get(0);
      Assert.assertEquals(1, service.findSeries(type, first[1]).size());
      Assert.assertTrue(service.findSeries(type, first[0]).isEmpty());
    }
  }

  private static List<String[]> datasheet(Class<?> type) {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(type);
    Assert.assertNotNull(type.getSimpleName(), datasheet);
    Assert.assertFalse(type.getSimpleName(), datasheet.isEmpty());
    return datasheet;
  }
}
