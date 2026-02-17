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
package org.diylc.core.measures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SizeTest {

  private static final double DELTA = 1e-9;

  // --- convertToUnits: same unit ---

  @Test
  public void convertToUnits_sameUnit_returnsNewSizeWithValueRoundedTo4Decimals() {
    Size size = new Size(1.23456789, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.mm);
    assertNotSame(size, result);
    assertEquals(SizeUnit.mm, result.getUnit());
    assertEquals(1.2346, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_sameUnit_roundsTo4Decimals() {
    assertEquals(2.3457, new Size(2.3456789, SizeUnit.cm).convertToUnits(SizeUnit.cm).getValue(), DELTA);
    assertEquals(0.5, new Size(0.5, SizeUnit.in).convertToUnits(SizeUnit.in).getValue(), DELTA);
  }

  // --- convertToUnits: metric ↔ metric ---

  @Test
  public void convertToUnits_mmToCm() {
    Size size = new Size(10.0, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.cm);
    assertEquals(SizeUnit.cm, result.getUnit());
    assertEquals(1.0, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_cmToMm() {
    Size size = new Size(2.5, SizeUnit.cm);
    Size result = size.convertToUnits(SizeUnit.mm);
    assertEquals(SizeUnit.mm, result.getUnit());
    assertEquals(25.0, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_mmToM() {
    Size size = new Size(1500.0, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.m);
    assertEquals(SizeUnit.m, result.getUnit());
    assertEquals(1.5, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_mToMm() {
    Size size = new Size(0.5, SizeUnit.m);
    Size result = size.convertToUnits(SizeUnit.mm);
    assertEquals(SizeUnit.mm, result.getUnit());
    assertEquals(500.0, result.getValue(), DELTA);
  }

  // --- convertToUnits: imperial ↔ imperial ---

  @Test
  public void convertToUnits_inToFt() {
    Size size = new Size(24.0, SizeUnit.in);
    Size result = size.convertToUnits(SizeUnit.ft);
    assertEquals(SizeUnit.ft, result.getUnit());
    assertEquals(2.0, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_ftToIn() {
    Size size = new Size(1.0, SizeUnit.ft);
    Size result = size.convertToUnits(SizeUnit.in);
    assertEquals(SizeUnit.in, result.getUnit());
    assertEquals(12.0, result.getValue(), DELTA);
  }

  // --- convertToUnits: metric ↔ imperial ---

  @Test
  public void convertToUnits_mmToIn() {
    Size size = new Size(25.4, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.in);
    assertEquals(SizeUnit.in, result.getUnit());
    assertEquals(1.0, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_inToMm() {
    Size size = new Size(1.0, SizeUnit.in);
    Size result = size.convertToUnits(SizeUnit.mm);
    assertEquals(SizeUnit.mm, result.getUnit());
    assertEquals(25.4, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_cmToIn() {
    Size size = new Size(2.54, SizeUnit.cm);
    Size result = size.convertToUnits(SizeUnit.in);
    assertEquals(SizeUnit.in, result.getUnit());
    assertEquals(1.0, result.getValue(), DELTA);
  }

  // --- convertToUnits: rounding to 4 decimal places ---

  @Test
  public void convertToUnits_resultIsRoundedTo4Decimals() {
    Size size = new Size(1.0, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.in);
    // 1 mm = 1/25.4 in ≈ 0.03937007874015748...
    assertEquals(0.0394, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_roundingDown() {
    Size size = new Size(1.23441, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.mm);
    assertEquals(1.2344, result.getValue(), DELTA);
  }

  @Test
  public void convertToUnits_roundingUp() {
    Size size = new Size(1.23446, SizeUnit.mm);
    Size result = size.convertToUnits(SizeUnit.mm);
    assertEquals(1.2345, result.getValue(), DELTA);
  }

  // --- parseSize ---

  @Test
  public void parseSize_mm() {
    Size size = Size.parseSize("10.5mm");
    assertEquals(10.5, size.getValue(), DELTA);
    assertEquals(SizeUnit.mm, size.getUnit());
  }

  @Test
  public void parseSize_cm() {
    Size size = Size.parseSize("2.54cm");
    assertEquals(2.54, size.getValue(), DELTA);
    assertEquals(SizeUnit.cm, size.getUnit());
  }

  @Test
  public void parseSize_in() {
    Size size = Size.parseSize("1in");
    assertEquals(1.0, size.getValue(), DELTA);
    assertEquals(SizeUnit.in, size.getUnit());
  }

  @Test
  public void parseSize_caseInsensitive() {
    Size size = Size.parseSize("5 MM");
    assertEquals(5.0, size.getValue(), DELTA);
    assertEquals(SizeUnit.mm, size.getUnit());
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseSize_unknownUnit_throws() {
    Size.parseSize("10 xyz");
  }

  // --- fromPixels ---

  @Test
  public void fromPixels_roundTripWithConvertToPixels() {
    Size original = new Size(1.0, SizeUnit.in);
    double pixels = original.convertToPixels();
    Size fromPixels = Size.fromPixels(pixels, SizeUnit.in);
    assertEquals(original.getValue(), fromPixels.getValue(), 0.01);
    assertEquals(original.getUnit(), fromPixels.getUnit());
  }

  // --- scale ---

  @Test
  public void scale() {
    Size size = new Size(10.0, SizeUnit.mm);
    Size scaled = size.scale(2.0);
    assertEquals(20.0, scaled.getValue(), DELTA);
    assertEquals(SizeUnit.mm, scaled.getUnit());
  }

  // --- compareTo ---

  @Test
  public void compareTo_samePhysicalSize_returnsZero() {
    Size a = new Size(25.4, SizeUnit.mm);
    Size b = new Size(1.0, SizeUnit.in);
    assertEquals(0, a.compareTo(b));
  }

  @Test
  public void compareTo_smaller_returnsNegative() {
    Size a = new Size(1.0, SizeUnit.mm);
    Size b = new Size(1.0, SizeUnit.cm);
    assertTrue(a.compareTo(b) < 0);
  }

  @Test
  public void compareTo_larger_returnsPositive() {
    Size a = new Size(10.0, SizeUnit.mm);
    Size b = new Size(1.0, SizeUnit.mm);
    assertTrue(a.compareTo(b) > 0);
  }

  // --- clone ---

  @Test
  public void clone_returnsEqualSize() throws CloneNotSupportedException {
    Size size = new Size(3.14, SizeUnit.cm);
    Size cloned = size.clone();
    assertNotSame(size, cloned);
    assertEquals(size.getValue(), cloned.getValue(), DELTA);
    assertEquals(size.getUnit(), cloned.getUnit());
  }
}
