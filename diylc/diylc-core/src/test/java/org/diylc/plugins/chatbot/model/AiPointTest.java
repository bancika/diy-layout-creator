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
package org.diylc.plugins.chatbot.model;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;
import java.math.BigDecimal;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Test;

public class AiPointTest {

  @Test
  public void toPixels_validCoordinates_returnsCorrectScaledPixels() {
    AiPoint pos = new AiPoint(new BigDecimal("5.0"), new BigDecimal("10.0"));

    // 0.1 inch grid spacing
    Size gridSpacing = new Size(0.1, SizeUnit.in);

    Point2D pixels = pos.toPixels(gridSpacing);

    // 1 inch = 200 pixels (Constants.PIXELS_PER_INCH)
    // 0.1 inch = 20 pixels
    // X = Math.round(5.0 * 20) = 100
    // Y = Math.round(10.0 * 20) = 200
    assertEquals(100.0, pixels.getX(), 0.001);
    assertEquals(200.0, pixels.getY(), 0.001);
  }

  @Test
  public void toPixels_nullCoordinates_returnsOrigin() {
    AiPoint pos = new AiPoint(null, null);
    
    Size gridSpacing = new Size(0.1, SizeUnit.in);
    Point2D pixels = pos.toPixels(gridSpacing);

    assertEquals(0.0, pixels.getX(), 0.001);
    assertEquals(0.0, pixels.getY(), 0.001);
  }
}
