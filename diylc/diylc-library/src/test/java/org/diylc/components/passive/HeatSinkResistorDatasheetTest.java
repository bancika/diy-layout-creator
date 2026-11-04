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
package org.diylc.components.passive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.diylc.components.passive.HeatSinkResistor.MountingHoleCount;
import org.diylc.core.measures.Power;
import org.diylc.core.measures.PowerUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

public class HeatSinkResistorDatasheetTest {

  @Test
  public void everyRowApplies() {
    List<String[]> datasheet =
        DatasheetService.getInstance().loadDatasheet(HeatSinkResistor.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    Set<String> parts = new HashSet<String>();
    for (String[] row : datasheet) {
      Assert.assertTrue("duplicate part " + row[1], parts.add(row[1]));
      HeatSinkResistor resistor = new HeatSinkResistor(row);
      Assert.assertNotNull(String.join(",", row), resistor.getPower());
      Assert.assertNull("a row must not set the resistance", resistor.getValue());
    }
  }

  @Test
  public void appliesAllColumns() {
    List<String[]> rows =
        DatasheetService.getInstance().findSeries(HeatSinkResistor.class, "HS100");
    Assert.assertEquals(1, rows.size());
    HeatSinkResistor resistor = new HeatSinkResistor(rows.get(0));
    Assert.assertEquals(new Power(100d, PowerUnit.W), resistor.getPower());
    Assert.assertEquals(new Size(65.2d, SizeUnit.mm), resistor.getLength());
    Assert.assertEquals(new Size(47.5d, SizeUnit.mm), resistor.getWidth());
    Assert.assertEquals(new Size(11.4d, SizeUnit.mm), resistor.getLeadLength());
    Assert.assertEquals(new Size(10.5d, SizeUnit.mm), resistor.getLipSize());
    Assert.assertEquals(MountingHoleCount.FOUR, resistor.getMountingHoles());
    Assert.assertEquals(new Size(35d, SizeUnit.mm), resistor.getHorizontalHoleSpacing());
    Assert.assertEquals(new Size(4.4d, SizeUnit.mm), resistor.getMountingHoleDiameter());
    Assert.assertEquals(new Size(3.2d, SizeUnit.mm), resistor.getTerminalLeadDiameter());
  }

  @Test
  public void modelKeepsValue() {
    HeatSinkResistor resistor = new HeatSinkResistor();
    resistor.setValue(new Resistance(10d, ResistanceUnit.R));

    DatasheetService service = DatasheetService.getInstance();
    String[] model =
        service.findModel(resistor, service.findSeries(HeatSinkResistor.class, "HS50"));
    Assert.assertNotNull(model);

    resistor.applyModel(model);
    Assert.assertEquals(new Resistance(10d, ResistanceUnit.R), resistor.getValue());
    Assert.assertEquals(new Power(50d, PowerUnit.W), resistor.getPower());
  }
}
