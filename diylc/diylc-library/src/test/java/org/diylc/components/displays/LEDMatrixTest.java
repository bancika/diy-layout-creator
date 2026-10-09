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
package org.diylc.components.displays;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.displays.LEDMatrix.Modules;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Basic checks for the MAX7219 matrix. The three variants differ in outline and in which edge
 * their headers leave by, so each one places the same ten pins differently.
 */
public class LEDMatrixTest {

  private static final double PX_PER_MM = 200.0d / 25.4d;

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  private static double mm(double px) {
    return px / PX_PER_MM;
  }

  private static LEDMatrix of(Modules modules) {
    LEDMatrix matrix = new LEDMatrix();
    matrix.setModules(modules);
    return matrix;
  }

  @Test
  public void boardSizeFollowsTheModules() {
    for (Modules modules : Modules.values()) {
      Rectangle2D bounds = of(modules).getBodyShape().getBounds2D();
      Assert.assertEquals(modules + " width", modules.getBoardWidthMm(), mm(bounds.getWidth()),
          0.01d);
      Assert.assertEquals(modules + " length", modules.getBoardHeightMm(), mm(bounds.getHeight()),
          0.01d);
    }
  }

  /**
   * Every board is exactly as wide as its modules. Asserted as an equality rather than a fit,
   * because that is where the cascaded boards' widths come from: a transcribed 128 or 256 that did
   * not match the module count would otherwise pass.
   */
  @Test
  public void theBoardIsExactlyAsWideAsItsModules() {
    for (Modules modules : Modules.values()) {
      double matrixSize = mm(LEDMatrix.MATRIX_SIZE.convertToPixels());
      Assert.assertEquals(modules + " width", modules.getCount() * matrixSize,
          modules.getBoardWidthMm(), 0.01d);
    }
  }

  @Test
  public void controlPointsAreTheTwoHeadersInOrder() {
    String[] expected = new String[] {"VCC_IN", "GND_IN", "DIN", "CS_IN", "CLK_IN", "VCC_OUT",
        "GND_OUT", "DOUT", "CS_OUT", "CLK_OUT"};

    for (Modules modules : Modules.values()) {
      LEDMatrix matrix = of(modules);
      Assert.assertEquals(modules.toString(), expected.length, matrix.getControlPointCount());

      Set<String> names = new HashSet<String>();
      for (int i = 0; i < expected.length; i++) {
        Assert.assertEquals(modules + " pin " + i, expected[i], matrix.getControlPointNodeName(i));
        Assert.assertTrue(modules + " duplicate node name " + expected[i], names.add(expected[i]));
      }
    }
  }

  /**
   * The tall single runs its rows across the width; the tileable variants stand them on end at the
   * short edges.
   */
  @Test
  public void eachHeaderIsAFivePinRow() {
    for (Modules modules : Modules.values()) {
      LEDMatrix matrix = of(modules);
      MakerBoardTestSupport.assertRow(matrix, 0, 4);
      MakerBoardTestSupport.assertRow(matrix, 5, 9);
    }
  }

  @Test
  public void everyPinSitsOnTheBoard() {
    for (Modules modules : Modules.values()) {
      LEDMatrix matrix = of(modules);
      Rectangle2D board = matrix.getBodyShape().getBounds2D();
      for (int i = 0; i < matrix.getControlPointCount(); i++) {
        Assert.assertTrue(modules + " pin " + i + " is off the board",
            board.contains(matrix.getControlPoint(i)));
      }
    }
  }

  @Test
  public void modulesRoundTripThroughTheSetter() {
    Assert.assertEquals(Modules.Single_8x8, new LEDMatrix().getModules());
    for (Modules modules : Modules.values()) {
      LEDMatrix matrix = of(modules);
      Assert.assertEquals(modules, matrix.getModules());
      Assert.assertEquals(modules.toString(), matrix.getValueForDisplay());
    }
  }

  /** A project saved before the variant property existed deserialises with no modules set. */
  @Test
  public void aMissingModulesValueFallsBackToTheSingle() {
    LEDMatrix matrix = of(null);
    Assert.assertEquals(Modules.Single_8x8, matrix.getModules());
  }

  @Test
  public void drawsCleanlyInEveryState() {
    for (Modules modules : Modules.values()) {
      MakerBoardTestSupport.assertDrawsCleanly(of(modules));
    }
  }
}
