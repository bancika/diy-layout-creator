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

import org.diylc.components.micro.RaspberryPiZero.ZeroVersion;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiZeroTest {

  @Test
  public void testPinout() {
    RaspberryPiZero pi = new RaspberryPiZero();
    Assert.assertEquals(ZeroVersion.PI_ZERO, pi.getVersion());

    // the 40-pin header, then the camera port and the two test point pairs
    Assert.assertEquals(45, pi.getControlPointCount());
    Assert.assertEquals("3.3V (Pin 1)", pi.getControlPointNodeName(0));
    Assert.assertEquals("5V (Pin 2)", pi.getControlPointNodeName(1));
    Assert.assertEquals("GND (Pin 39)", pi.getControlPointNodeName(38));
    Assert.assertEquals("GPIO21 (Pin 40)", pi.getControlPointNodeName(39));
    Assert.assertEquals("MIPI (CSI)", pi.getControlPointNodeName(40));
    Assert.assertEquals("RUN 1", pi.getControlPointNodeName(41));
    Assert.assertEquals("TV 2", pi.getControlPointNodeName(44));
  }

  @Test
  public void testGeometryIsVersionIndependent() {
    RaspberryPiZero pi = new RaspberryPiZero();
    Rectangle2D bounds = pi.getBodyShape().getBounds2D();
    double spacing = MakerBoardTestSupport.PIN_SPACING;

    MakerBoardTestSupport.assertBoardSize(pi, new Size(65.0d, SizeUnit.mm),
        new Size(30.0d, SizeUnit.mm));

    // the header is numbered down the columns, odd pins in the inner row and even in the outer,
    // so consecutive pins of a row are two indices apart
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

    // the header straddles the mounting hole line 3.5 mm down and is centred between the holes at
    // 3.5 mm and 61.5 mm from the left edge
    Point2D pin1 = pi.getControlPoint(0);
    Point2D pin2 = pi.getControlPoint(1);
    double millimetre = new Size(1.0d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(pin1.getX(), pin2.getX(), 0.01);
    Assert.assertEquals(3.5d * millimetre, (pin1.getY() + pin2.getY()) / 2.0 - bounds.getY(), 0.1);
    Assert.assertEquals(bounds.getX() + 32.5d * millimetre,
        (pin1.getX() + pi.getControlPoint(38).getX()) / 2.0, 0.1);

    // the Zero W and the Zero 2 W keep the outline, the header position and every connector of the
    // original; only the SoC package and the silkscreen name change
    for (ZeroVersion version : ZeroVersion.values()) {
      RaspberryPiZero variant = new RaspberryPiZero();
      variant.setVersion(version);
      MakerBoardTestSupport.assertSharedFootprint(pi, variant, pi.getControlPointCount());
    }
  }
}
