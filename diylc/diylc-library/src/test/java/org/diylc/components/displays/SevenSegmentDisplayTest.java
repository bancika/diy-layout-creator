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
import org.diylc.components.displays.SevenSegmentDisplay.Common;
import org.diylc.components.displays.SevenSegmentDisplay.DisplayType;
import org.diylc.components.displays.SevenSegmentDisplay.Punctuation;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Basic checks for the seven-segment displays. All but one are DIP packages straddling their pins
 * with a row either side; the last is a module carrying a single header down one edge.
 */
public class SevenSegmentDisplayTest {

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

  private static SevenSegmentDisplay of(DisplayType displayType) {
    SevenSegmentDisplay display = new SevenSegmentDisplay();
    display.setDisplayType(displayType);
    return display;
  }

  @Test
  public void bodySizeFollowsThePackage() {
    for (DisplayType displayType : DisplayType.values()) {
      Rectangle2D bounds = of(displayType).getBodyShape().getBounds2D();
      Assert.assertEquals(displayType + " width", displayType.getBodyWidthMm(),
          mm(bounds.getWidth()), 0.01d);
      Assert.assertEquals(displayType + " length", displayType.getBodyLengthMm(),
          mm(bounds.getHeight()), 0.01d);
    }
  }

  @Test
  public void pinCountFollowsThePackage() {
    Assert.assertEquals(10, of(DisplayType.SingleDigit_10Pin).getControlPointCount());
    Assert.assertEquals(10, of(DisplayType.TwoDigit_0_56_10Pin).getControlPointCount());
    Assert.assertEquals(12, of(DisplayType.FourDigit_0_36_12Pin).getControlPointCount());
    Assert.assertEquals(12, of(DisplayType.FourDigit_0_56_12Pin).getControlPointCount());
    Assert.assertEquals(4, of(DisplayType.TM1637_Module_4Pin).getControlPointCount());
  }

  /**
   * Pin arrays are in DIP order: pin 1 bottom left, along the bottom row, then back along the top.
   */
  @Test
  public void pinNamesAreInDipOrder() {
    Assert.assertArrayEquals(new String[] {"E", "D", "COM1", "C", "DP", "B", "A", "COM2", "F", "G"},
        SevenSegmentDisplay.PIN_NAMES_1DIGIT);
    Assert.assertArrayEquals(new String[] {"E", "D", "DP", "C", "G", "B", "D2", "D1", "F", "A"},
        SevenSegmentDisplay.PIN_NAMES_2DIGIT);
    Assert.assertArrayEquals(
        new String[] {"E", "D", "DP", "C", "G", "D4", "B", "D3", "D2", "F", "A", "D1"},
        SevenSegmentDisplay.PIN_NAMES_4DIGIT);

    for (DisplayType displayType : DisplayType.values()) {
      SevenSegmentDisplay display = of(displayType);
      Set<String> names = new HashSet<String>();
      for (int i = 0; i < display.getControlPointCount(); i++) {
        String name = display.getControlPointNodeName(i);
        Assert.assertFalse(displayType + " pin " + i + " is unnamed", name.isEmpty());
        Assert.assertTrue(displayType + " duplicate node name " + name, names.add(name));
      }
    }
  }

  /** The 3641AS and the 5641AS differ in size but not in pin function, so one array serves both. */
  @Test
  public void bothFourDigitPackagesSharePinNames() {
    SevenSegmentDisplay small = of(DisplayType.FourDigit_0_36_12Pin);
    SevenSegmentDisplay large = of(DisplayType.FourDigit_0_56_12Pin);

    for (int i = 0; i < small.getControlPointCount(); i++) {
      Assert.assertEquals("pin " + i, small.getControlPointNodeName(i),
          large.getControlPointNodeName(i));
    }
    Assert.assertNotEquals(small.getBodyShape().getBounds2D(), large.getBodyShape().getBounds2D());
    Assert.assertSame("shared by identity, not by two matching copies",
        DisplayType.FourDigit_0_36_12Pin.getPinNames(),
        DisplayType.FourDigit_0_56_12Pin.getPinNames());
  }

  /**
   * The digit count is the variant's own figure rather than something the drawing infers from
   * which constant it is looking at, which is what lets a two-, six- or eight-digit part be a
   * constant beside these.
   */
  @Test
  public void digitCountComesFromThePackage() {
    Assert.assertEquals(1, DisplayType.SingleDigit_10Pin.getDigitCount());
    Assert.assertEquals(2, DisplayType.TwoDigit_0_56_10Pin.getDigitCount());
    Assert.assertEquals(4, DisplayType.FourDigit_0_36_12Pin.getDigitCount());
    Assert.assertEquals(4, DisplayType.FourDigit_0_56_12Pin.getDigitCount());
    Assert.assertEquals(4, DisplayType.TM1637_Module_4Pin.getDigitCount());

    for (DisplayType displayType : DisplayType.values()) {
      Assert.assertTrue(displayType + " shows no digits", displayType.getDigitCount() >= 1);
      Assert.assertEquals(displayType + " pin count", displayType.getPinNames().length,
          of(displayType).getControlPointCount());
    }
  }

  /**
   * A driven module brings out no common pins, so its polarity is not something you order and the
   * BOM line omits it. Paired with the pin arrangement so the two cannot become one question: they
   * agree on every variant today, but a single-in-line bare package would separate them.
   */
  @Test
  public void onlyModulesCarryABoard() {
    for (DisplayType displayType : DisplayType.values()) {
      Assert.assertEquals(displayType + " module", displayType == DisplayType.TM1637_Module_4Pin,
          displayType.isModule());
      Assert.assertEquals(displayType + " board figures", displayType.isModule(),
          displayType.getModule() != null);
      Assert.assertNotEquals(displayType + " row arrangement", displayType.isModule(),
          displayType.isDualRow());
    }
  }

  @Test
  public void dipPackagesCarryTwoRowsAndTheModuleOne() {
    for (DisplayType displayType : DisplayType.values()) {
      SevenSegmentDisplay display = of(displayType);
      int pinCount = display.getControlPointCount();

      if (displayType.isDualRow()) {
        int perRow = pinCount / 2;
        MakerBoardTestSupport.assertRow(display, 0, perRow - 1);
        MakerBoardTestSupport.assertRow(display, perRow, pinCount - 1);
        Assert.assertEquals(displayType + " row spacing", displayType.getRowSpacingMm(),
            mm(Math.abs(display.getControlPoint(0).getY()
                - display.getControlPoint(pinCount - 1).getY())),
            0.01d);
      } else {
        MakerBoardTestSupport.assertRow(display, 0, pinCount - 1);
      }
    }
  }

  /** A package only works if its pin rows sit within its body. */
  @Test
  public void everyPinSitsOnTheBody() {
    for (DisplayType displayType : DisplayType.values()) {
      SevenSegmentDisplay display = of(displayType);
      Rectangle2D body = display.getBodyShape().getBounds2D();
      for (int i = 0; i < display.getControlPointCount(); i++) {
        Assert.assertTrue(displayType + " pin " + i + " is off the body",
            body.contains(display.getControlPoint(i)));
      }
    }
  }

  @Test
  public void propertiesRoundTripThroughTheirSetters() {
    SevenSegmentDisplay display = new SevenSegmentDisplay();
    Assert.assertEquals(DisplayType.SingleDigit_10Pin, display.getDisplayType());
    Assert.assertEquals(Common.Cathode, display.getCommon());
    Assert.assertEquals(Punctuation.DecimalPoints, display.getPunctuation());

    for (DisplayType displayType : DisplayType.values()) {
      Assert.assertEquals(displayType, of(displayType).getDisplayType());
    }
    for (Common common : Common.values()) {
      display.setCommon(common);
      Assert.assertEquals(common, display.getCommon());
    }
    for (Punctuation punctuation : Punctuation.values()) {
      display.setPunctuation(punctuation);
      Assert.assertEquals(punctuation, display.getPunctuation());
    }
  }

  /** The module's polarity is not part of what you order, so its BOM value leaves it out. */
  @Test
  public void bomValueNamesThePackageAndWhatVariesWithIt() {
    SevenSegmentDisplay module = of(DisplayType.TM1637_Module_4Pin);
    module.setCommon(Common.Anode);
    module.setPunctuation(Punctuation.Colon);
    Assert.assertEquals(DisplayType.TM1637_Module_4Pin + ", " + Punctuation.Colon,
        module.getValueForDisplay());

    SevenSegmentDisplay bare = of(DisplayType.SingleDigit_10Pin);
    bare.setCommon(Common.Anode);
    bare.setPunctuation(Punctuation.None);
    Assert.assertEquals(
        DisplayType.SingleDigit_10Pin + ", " + Common.Anode + ", " + Punctuation.None,
        bare.getValueForDisplay());
  }

  @Test
  public void drawsCleanlyInEveryState() {
    for (DisplayType displayType : DisplayType.values()) {
      for (Punctuation punctuation : Punctuation.values()) {
        SevenSegmentDisplay display = of(displayType);
        display.setPunctuation(punctuation);
        MakerBoardTestSupport.assertDrawsCleanly(display);
      }
    }
  }
}
