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

import org.diylc.components.semiconductors.DIL_IC.PinCount;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.Node;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

public class DIL_ICDatasheetTest {

  private static final int FIRST_PIN_COLUMN = 3;

  @Test
  public void everyRowApplies() {
    for (String[] row : datasheet()) {
      DIL_IC ic = new DIL_IC(row);
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
    Assert.assertEquals(1, service.findSeries(DIL_IC.class, "tl072").size());
    Assert.assertTrue(service.findSeries(DIL_IC.class, "Op-Amp (Dual)").isEmpty());
  }

  @Test
  public void appliesAllColumns() {
    DIL_IC ic = new DIL_IC(DatasheetService.getInstance().findSeries(DIL_IC.class, "TL074").get(0));
    Assert.assertEquals("TL074", ic.getValue());
    Assert.assertEquals(PinCount._14, ic.getPinCount());
    Assert.assertEquals(new Size(0.3d, SizeUnit.in), ic.getRowSpacing());
    Assert.assertEquals("1OUT", ic.getControlPointNodeName(0));
    Assert.assertEquals("4OUT", ic.getControlPointNodeName(7));
  }

  /** A new part has a blank value, which must not count as a value the model would change. */
  @Test
  public void modelAppliesToNewPart() {
    DIL_IC ic = new DIL_IC();
    DatasheetService service = DatasheetService.getInstance();
    String[] model = service.findModel(ic, service.findSeries(DIL_IC.class, "NE555"));
    Assert.assertNotNull(model);
    ic.applyModel(model);
    Assert.assertEquals("GND", ic.getControlPointNodeName(0));
    Assert.assertEquals("VCC", ic.getControlPointNodeName(4));
  }

  @Test
  public void modelMustMatchTheValue() {
    DatasheetService service = DatasheetService.getInstance();
    List<String[]> rows = service.findSeries(DIL_IC.class, "TL072");
    DIL_IC ic = new DIL_IC();
    ic.setValue("TL072");
    Assert.assertNotNull(service.findModel(ic, rows));
    ic.setValue("NE5532");
    Assert.assertNull(service.findModel(ic, rows));
  }

  private static List<String[]> datasheet() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(DIL_IC.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    return datasheet;
  }
}
