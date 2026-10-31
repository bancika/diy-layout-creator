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

import java.awt.Color;
import java.util.List;

import org.diylc.common.ResistorColorCode;
import org.diylc.components.passive.Resistor.ResistorShape;
import org.diylc.core.measures.Power;
import org.diylc.core.measures.PowerUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.DatasheetService;
import org.junit.Assert;
import org.junit.Test;

public class ResistorDatasheetTest {

  @Test
  public void everyRowApplies() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(Resistor.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    for (String[] row : datasheet) {
      Resistor resistor = new Resistor(row);
      Assert.assertNotNull(String.join(",", row), resistor.getPowerNew());
      Assert.assertNull("a row must not set the resistance", resistor.getValue());
    }
  }

  @Test
  public void appliesAllColumns() {
    Resistor resistor = new Resistor(DatasheetService.getInstance().findSeries(Resistor.class,
        "Ceramic").stream().filter(row -> row[1].equals("10 W")).findFirst().get());
    Assert.assertEquals(new Power(10d, PowerUnit.W), resistor.getPowerNew());
    Assert.assertEquals(new Size(48d, SizeUnit.mm), resistor.getLength());
    Assert.assertEquals(new Size(9.5d, SizeUnit.mm), resistor.getWidth());
    Assert.assertEquals(ResistorShape.Tubular, resistor.getShape());
    Assert.assertEquals(ResistorColorCode.NONE, resistor.getColorCode());
    Assert.assertEquals(Color.decode("#F6F5F5"), resistor.getBodyColor());
  }

  @Test
  public void sizeKeepsItsUnit() {
    Resistor resistor = new Resistor(new String[] {"Carbon Comp", "0.5 W", "0.41 in", "0.15 in",
        "Tubular", "5 band", "#8A5E20", "#735832"});
    Assert.assertEquals(new Size(0.41d, SizeUnit.in), resistor.getLength());
  }

  @Test
  public void modelFollowsPowerRatingAndKeepsValue() {
    Resistor resistor = new Resistor();
    resistor.setValue(new Resistance(10d, ResistanceUnit.K));
    resistor.setPowerNew(new Power(0.5d, PowerUnit.W));

    DatasheetService service = DatasheetService.getInstance();
    String[] model =
        service.findModel(resistor, service.findSeries(Resistor.class, "carbon comp"));
    Assert.assertNotNull(model);
    Assert.assertEquals("0.5 W", model[1]);

    resistor.applyModel(model);
    Assert.assertEquals(new Resistance(10d, ResistanceUnit.K), resistor.getValue());
    Assert.assertEquals(new Size(0.41d, SizeUnit.in), resistor.getLength());
  }
}
