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
package org.diylc.components.micro;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.components.micro.RaspberryPi.RaspberryPiVersion;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiTest {

  @Test
  public void testPinoutPerVersion() {
    RaspberryPi pi = new RaspberryPi();
    Assert.assertEquals(RaspberryPiVersion.PI_5, pi.getVersion());

    // the 40-pin header, then the PoE header and one point per flat-flex connector
    Assert.assertEquals(48, pi.getControlPointCount());
    Assert.assertEquals("3.3V (Pin 1)", pi.getControlPointNodeName(0));
    Assert.assertEquals("5V (Pin 2)", pi.getControlPointNodeName(1));
    Assert.assertEquals("GPIO2/SDA (Pin 3)", pi.getControlPointNodeName(2));
    Assert.assertEquals("GND (Pin 39)", pi.getControlPointNodeName(38));
    Assert.assertEquals("GPIO21 (Pin 40)", pi.getControlPointNodeName(39));
    Assert.assertEquals("PoE TR0 (Pin 1)", pi.getControlPointNodeName(40));
    Assert.assertEquals("PoE TR3 (Pin 4)", pi.getControlPointNodeName(43));
    Assert.assertEquals("PCIe", pi.getControlPointNodeName(44));
    Assert.assertEquals("MIPI 1", pi.getControlPointNodeName(45));
    Assert.assertEquals("UART", pi.getControlPointNodeName(47));

    // the PCIe socket and the second camera port arrived with the Pi 5, so the earlier boards are
    // two points short
    for (RaspberryPiVersion version : new RaspberryPiVersion[] {RaspberryPiVersion.PI_3_B,
        RaspberryPiVersion.PI_4_B}) {
      RaspberryPi earlier = version(version);
      Assert.assertEquals(version + " pin count", 46, earlier.getControlPointCount());
      Assert.assertEquals("GPIO21 (Pin 40)", earlier.getControlPointNodeName(39));
      Assert.assertEquals("PoE TR3 (Pin 4)", earlier.getControlPointNodeName(43));
    }
  }

  @Test
  public void testHeaderGeometry() {
    RaspberryPi pi = new RaspberryPi();
    Rectangle2D bounds = pi.getBodyShape().getBounds2D();
    double spacing = MakerBoardTestSupport.PIN_SPACING;

    MakerBoardTestSupport.assertBoardSize(pi, new Size(85.0d, SizeUnit.mm),
        new Size(56.0d, SizeUnit.mm));

    // the header is numbered down the columns, odd pins in the inner row and even in the outer,
    // so consecutive pins of a row are two indices apart
    Point2D pin1 = pi.getControlPoint(0);
    Point2D pin2 = pi.getControlPoint(1);
    for (int column = 0; column < 19; column++) {
      Point2D odd = pi.getControlPoint(column * 2);
      Point2D nextOdd = pi.getControlPoint((column + 1) * 2);
      Point2D even = pi.getControlPoint(column * 2 + 1);
      Point2D nextEven = pi.getControlPoint((column + 1) * 2 + 1);
      Assert.assertEquals(spacing, nextOdd.getX() - odd.getX(), 0.01);
      Assert.assertEquals(odd.getY(), nextOdd.getY(), 0.01);
      Assert.assertEquals(spacing, nextEven.getX() - even.getX(), 0.01);
      Assert.assertEquals(even.getY(), nextEven.getY(), 0.01);
    }

    // the header straddles the mounting hole line 3.5 mm down, and is centred between the holes at
    // 3.5 mm and 61.5 mm from the left edge
    Assert.assertEquals(pin1.getX(), pin2.getX(), 0.01);
    Assert.assertEquals(mm(3.5d), (pin1.getY() + pin2.getY()) / 2.0 - bounds.getY(), 0.1);
    Assert.assertEquals(bounds.getX() + mm(32.5d),
        (pin1.getX() + pi.getControlPoint(38).getX()) / 2.0, 0.1);

    // the PoE header is a 2x2 block centred on the bottom right mounting hole
    double poeCenterX = bounds.getX() + mm(61.5d);
    double poeCenterY = bounds.getY() + mm(46.5d);
    Assert.assertEquals(poeCenterX - spacing / 2.0, pi.getControlPoint(40).getX(), 0.1);
    Assert.assertEquals(poeCenterY - spacing / 2.0, pi.getControlPoint(40).getY(), 0.1);
    Assert.assertEquals(poeCenterX + spacing / 2.0, pi.getControlPoint(43).getX(), 0.1);
    Assert.assertEquals(poeCenterY + spacing / 2.0, pi.getControlPoint(43).getY(), 0.1);

    // the flat-flex connectors are single points at the centre of the socket they stand for
    for (int i = 44; i < pi.getControlPointCount(); i++) {
      Assert.assertTrue("Connector " + pi.getControlPointNodeName(i) + " should be on the board",
          bounds.contains(pi.getControlPoint(i)));
    }
  }

  @Test
  public void testEveryVersionDraws() {
    for (RaspberryPiVersion version : RaspberryPiVersion.values()) {
      MakerBoardTestSupport.assertDrawsCleanly(version(version));
    }
  }

  private static double mm(double value) {
    return new Size(value, SizeUnit.mm).convertToPixels();
  }

  private static RaspberryPi version(RaspberryPiVersion version) {
    RaspberryPi pi = new RaspberryPi();
    pi.setVersion(version);
    return pi;
  }
}
