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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;

import org.diylc.common.Display;
import org.diylc.common.Orientation;
import org.diylc.components.semiconductors.AbstractTransistorPackage.TransistorDisplay;
import org.junit.Assert;
import org.junit.Test;

public class TransistorTO3Test {

  @Test
  public void caseIsReachedThroughAMountingHole() {
    for (Orientation orientation : Orientation.values()) {
      TransistorTO3 transistor = new TransistorTO3();
      transistor.setOrientation(orientation);
      Point2D casePoint = transistor.getControlPoint(2);
      Area body = transistor.getBody()[0];
      Assert.assertTrue(orientation.name(), body.getBounds2D().contains(casePoint));
      Assert.assertFalse(orientation.name(), body.contains(casePoint));
    }
  }

  @Test
  public void fileWithoutCasePointGainsIt() throws Exception {
    TransistorTO3 expected = new TransistorTO3();
    expected.setOrientation(Orientation._90);
    TransistorTO3 legacy = new TransistorTO3();
    legacy.setOrientation(Orientation._90);
    setField(legacy, "controlPoints",
        new Point2D[] {legacy.getControlPoint(0), legacy.getControlPoint(1)});

    Assert.assertEquals(3, legacy.getControlPointCount());
    Assert.assertEquals(expected.getControlPoint(2), legacy.getControlPoint(2));
  }

  @Test
  public void nodeNamesFollowThePinout() {
    TransistorTO3 transistor = new TransistorTO3();
    Assert.assertEquals("1", transistor.getControlPointNodeName(0));
    Assert.assertEquals("3", transistor.getControlPointNodeName(2));
    transistor.setPinout(TransistorPinout.BJT_BEC);
    Assert.assertEquals("B", transistor.getControlPointNodeName(0));
    Assert.assertEquals("E", transistor.getControlPointNodeName(1));
    Assert.assertEquals("C", transistor.getControlPointNodeName(2));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void displayIsReadFromTheLegacyField() throws Exception {
    TransistorTO3 transistor = new TransistorTO3();
    transistor.setDisplay(Display.BOTH);
    setField(transistor, "transistorDisplay", null);
    Assert.assertEquals(TransistorDisplay.BOTH, transistor.getTransistorDisplay());
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field field = TransistorTO3.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }
}
