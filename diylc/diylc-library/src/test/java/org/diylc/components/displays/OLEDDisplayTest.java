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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.displays.OLEDDisplay.Layout;
import org.diylc.components.displays.OLEDDisplay.OLEDInterface;
import org.diylc.components.displays.OLEDDisplay.Version;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the three OLED modules. What is specific to this part is that the interface
 * is not orthogonal to the size: the 0.91" is sold as two different boards, so every check that
 * reads a board figure has to read it for a size and an interface together. The other thing worth
 * pinning down is the panel, which is offset within its board rather than centred on two of the
 * five boards, and wide enough to share the mounting holes' horizontal span on all of them.
 */
public class OLEDDisplayTest {

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

  private static OLEDDisplay of(Version version, OLEDInterface oledInterface) {
    OLEDDisplay display = new OLEDDisplay();
    display.setVersion(version);
    display.setOledInterface(oledInterface);
    return display;
  }

  @Test
  public void boardSizeFollowsTheVersionAndInterface() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        Rectangle2D bounds = of(version, oledInterface).getBodyShape().getBounds2D();
        Assert.assertEquals(version + " " + oledInterface + " width", layout.getBoardWidthMm(),
            mm(bounds.getWidth()), 0.01d);
        Assert.assertEquals(version + " " + oledInterface + " length", layout.getBoardLengthMm(),
            mm(bounds.getHeight()), 0.01d);
      }
    }
  }

  /**
   * The headline fact about this part, and the one the plan had wrong: on the 0.91" the two
   * interfaces are not one board with a longer header but two boards, differing in outline, in
   * which edge the header sits on and in whether there are mounting holes at all. The other two
   * sizes do carry one board across both interfaces, which is what makes the distinction easy to
   * lose.
   */
  @Test
  public void theSmallBoardIsTwoDifferentBoards() {
    Layout i2c = Version.SSD1306_0_91.getLayout(OLEDInterface.I2C_4Pin);
    Layout spi = Version.SSD1306_0_91.getLayout(OLEDInterface.SPI_7Pin);

    Assert.assertEquals("I2C length", 12.0d, i2c.getBoardLengthMm(), 0.01d);
    Assert.assertEquals("SPI length", 20.0d, spi.getBoardLengthMm(), 0.01d);
    Assert.assertEquals("the two share a long edge", i2c.getBoardWidthMm(),
        spi.getBoardWidthMm(), 0.01d);
    Assert.assertTrue("the I2C column should stand on the left edge", i2c.isHeaderOnLeftEdge());
    Assert.assertFalse("the SPI row should lie along the top edge", spi.isHeaderOnLeftEdge());
    Assert.assertFalse("the I2C board has no mounting holes", i2c.hasMountingHoles());
    Assert.assertTrue("the SPI board has four mounting holes", spi.hasMountingHoles());

    for (Version version : new Version[] {Version.SSD1306_0_96, Version.SH1106_1_3}) {
      Assert.assertSame(version + " should carry one board across both interfaces",
          version.getLayout(OLEDInterface.I2C_4Pin), version.getLayout(OLEDInterface.SPI_7Pin));
    }
  }

  @Test
  public void controlPointsAreTheI2CPinsInOrder() {
    OLEDDisplay display = of(Version.SSD1306_0_96, OLEDInterface.I2C_4Pin);
    String[] expected = new String[] {"GND", "VCC", "SCL", "SDA"};
    Assert.assertEquals(expected.length, display.getControlPointCount());
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  @Test
  public void controlPointsAreTheSpiPinsInOrder() {
    OLEDDisplay display = of(Version.SSD1306_0_96, OLEDInterface.SPI_7Pin);
    String[] expected = new String[] {"GND", "VCC", "D0 (CLK)", "D1 (MOSI)", "RES", "DC", "CS"};
    Assert.assertEquals(expected.length, display.getControlPointCount());
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  /** Every size is sold in both interfaces, so no size may carry a shorter pin array. */
  @Test
  public void bothInterfacesExistAtEverySize() {
    for (Version version : Version.values()) {
      Assert.assertEquals(version + " I2C", 4,
          of(version, OLEDInterface.I2C_4Pin).getControlPointCount());
      Assert.assertEquals(version + " SPI", 7,
          of(version, OLEDInterface.SPI_7Pin).getControlPointCount());
    }
  }

  /** Switching either property must resize the array, not leave the longer one's points behind. */
  @Test
  public void switchingInterfaceResizesTheControlPoints() {
    OLEDDisplay display = new OLEDDisplay();
    Assert.assertEquals(4, display.getControlPointCount());
    display.setOledInterface(OLEDInterface.SPI_7Pin);
    Assert.assertEquals(7, display.getControlPointCount());
    display.setVersion(Version.SSD1306_0_91);
    Assert.assertEquals(7, display.getControlPointCount());
    display.setOledInterface(OLEDInterface.I2C_4Pin);
    Assert.assertEquals(4, display.getControlPointCount());
  }

  @Test
  public void pinsSitAtHeaderPitchAlongOneLine() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        OLEDDisplay display = of(version, oledInterface);
        boolean vertical = version.getLayout(oledInterface).isHeaderOnLeftEdge();
        for (int i = 1; i < display.getControlPointCount(); i++) {
          Point2D previous = display.getControlPoint(i - 1);
          Point2D current = display.getControlPoint(i);
          String pin = version + " " + oledInterface + " pin " + i;
          Assert.assertEquals(pin + " pitch", 2.54d,
              mm(vertical ? current.getY() - previous.getY() : current.getX() - previous.getX()),
              0.01d);
          Assert.assertEquals(pin + " left the line", vertical ? previous.getX() : previous.getY(),
              vertical ? current.getX() : current.getY(), 0.01d);
        }
      }
    }
  }

  /**
   * A row on the top edge is centred across the board at whatever pin count it carries. Nothing
   * else in the geometry would notice if the board were placed from the first pin instead: a
   * four-pin row would sit in a corner and a seven-pin one would look very nearly centred anyway.
   */
  @Test
  public void headerRowIsCentredOnTheTopEdge() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        if (layout.isHeaderOnLeftEdge()) {
          continue;
        }
        OLEDDisplay display = of(version, oledInterface);
        Rectangle2D board = display.getBodyShape().getBounds2D();
        Point2D first = display.getControlPoint(0);
        Point2D last = display.getControlPoint(display.getControlPointCount() - 1);

        Assert.assertEquals(version + " " + oledInterface + " margins",
            board.getMaxX() - last.getX(), first.getX() - board.getX(), 0.01d);
        Assert.assertEquals(version + " " + oledInterface + " clearance from the top edge",
            layout.getHeaderOffsetMm(), mm(first.getY() - board.getY()), 0.01d);
      }
    }
  }

  /**
   * The 0.91" I2C column stands its clearance in from the left edge and is centred on the board's
   * height, following the Character LCD's backpack. A board drawn downwards and leftwards from its
   * first pin instead would keep every pitch and size assertion above.
   */
  @Test
  public void headerColumnStandsAgainstTheLeftEdge() {
    OLEDDisplay display = of(Version.SSD1306_0_91, OLEDInterface.I2C_4Pin);
    Rectangle2D board = display.getBodyShape().getBounds2D();
    Point2D first = display.getControlPoint(0);
    Point2D last = display.getControlPoint(display.getControlPointCount() - 1);

    Assert.assertEquals("clearance from the left edge", 1.5d, mm(first.getX() - board.getX()),
        0.01d);
    Assert.assertEquals("the column is centred on the height", board.getMaxY() - last.getY(),
        first.getY() - board.getY(), 0.01d);
  }

  /**
   * Each lit area is its pixel count at the panel's dot pitch, so its aspect is the pixel aspect:
   * a 2:1 letterbox on the two 128 x 64 panels and 4:1 on the 128 x 32. This is the figure the
   * 0.96" was drawn wrong against for as long as its panel was near-square.
   */
  @Test
  public void litAreaMatchesThePixelAspect() {
    Assert.assertEquals(2.0d, aspect(Version.SSD1306_0_96), 0.01d);
    Assert.assertEquals(4.0d, aspect(Version.SSD1306_0_91), 0.01d);
    Assert.assertEquals(2.0d, aspect(Version.SH1106_1_3), 0.01d);
  }

  private static double aspect(Version version) {
    return version.getActiveWidthMm() / version.getActiveLengthMm();
  }

  @Test
  public void litAreaSitsWithinThePanel() {
    for (Version version : Version.values()) {
      Assert.assertTrue(version + " lit area is wider than its panel",
          version.getActiveLeftInPanelMm() + version.getActiveWidthMm() <= version
              .getPanelWidthMm());
      Assert.assertTrue(version + " lit area is longer than its panel",
          version.getActiveLengthMm() <= version.getPanelLengthMm());
      Assert.assertTrue(version + " lit area starts off the panel",
          version.getActiveLeftInPanelMm() >= 0);
    }
  }

  @Test
  public void panelFitsWithinTheBoard() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        String board = version + " " + oledInterface;
        Assert.assertTrue(board + " panel runs off the right edge", layout.getPanelLeftMm()
            + version.getPanelWidthMm() <= layout.getBoardWidthMm());
        Assert.assertTrue(board + " panel runs off the bottom edge", layout.getPanelTopMm()
            + version.getPanelLengthMm() <= layout.getBoardLengthMm());
      }
    }
  }

  /**
   * Where the lit area lands on the board, which is what the maintainer measured: the panel starts
   * 5 mm in on the 0.91" I2C board and 1.25 mm in on its SPI sibling, and the lit area is the
   * panel's own 2.1 mm further in on each. Getting either half wrong moves the lit area while
   * leaving every size and clearance assertion here intact, and swapping the two between the
   * boards leaves both of them plausible -- which is what {@link #pinsStayOffThePanel} is for.
   */
  @Test
  public void litAreaSitsWhereItWasMeasured() {
    Assert.assertEquals("0.91\" I2C", 7.1d,
        litAreaLeftMm(Version.SSD1306_0_91, OLEDInterface.I2C_4Pin), 0.01d);
    Assert.assertEquals("0.91\" SPI", 3.35d,
        litAreaLeftMm(Version.SSD1306_0_91, OLEDInterface.SPI_7Pin), 0.01d);
  }

  /**
   * No pin may land on the glass. Every other check here measures features against each other, so
   * the two 0.91" panel offsets could be swapped between the boards and still pass all of them --
   * and swapped, the I2C board's panel begins 0.25 mm before its own pin column and the four pins
   * are drawn on the display. Only a render showed it, which is what this replaces.
   */
  @Test
  public void pinsStayOffThePanel() {
    double pinRadius = AbstractMakerBoard.PIN_SIZE.convertToPixels() / 2.0;
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        OLEDDisplay display = of(version, oledInterface);
        Rectangle2D board = display.getBodyShape().getBounds2D();
        Rectangle2D panel = new Rectangle2D.Double(
            board.getX() + mmToPx(layout.getPanelLeftMm()),
            board.getY() + mmToPx(layout.getPanelTopMm()),
            mmToPx(version.getPanelWidthMm()), mmToPx(version.getPanelLengthMm()));

        for (int i = 0; i < display.getControlPointCount(); i++) {
          Point2D pin = display.getControlPoint(i);
          Rectangle2D pad = new Rectangle2D.Double(pin.getX() - pinRadius, pin.getY() - pinRadius,
              2 * pinRadius, 2 * pinRadius);
          Assert.assertFalse(version + " " + oledInterface + " pin " + i + " sits on the panel",
              panel.intersects(pad));
        }
      }
    }
  }

  private static double mmToPx(double millimetres) {
    return millimetres * PX_PER_MM;
  }

  private static double litAreaLeftMm(Version version, OLEDInterface oledInterface) {
    return version.getLayout(oledInterface).getPanelLeftMm() + version.getActiveLeftInPanelMm();
  }

  /**
   * The 0.91" SPI board centres its lit area on the board's height, which is how it was measured,
   * rather than hanging the panel below the header the way the taller boards do.
   */
  @Test
  public void theSmallSpiBoardCentresItsLitArea() {
    Version version = Version.SSD1306_0_91;
    Layout layout = version.getLayout(OLEDInterface.SPI_7Pin);
    double litTop = layout.getPanelTopMm()
        + (version.getPanelLengthMm() - version.getActiveLengthMm()) / 2.0;
    Assert.assertEquals(layout.getBoardLengthMm() / 2.0,
        litTop + version.getActiveLengthMm() / 2.0, 0.01d);
  }

  /**
   * The panel is wide enough on every one of these boards to share the holes' horizontal span, so
   * the vertical axis is the only one that can keep them apart -- a symmetric check would fail on
   * a board that is drawn correctly. The 0.91" SPI board is the tight one, clearing each hole row
   * by 0.5 mm.
   */
  @Test
  public void panelClearsTheMountingHolesVertically() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        if (!layout.hasMountingHoles()) {
          continue;
        }
        double holeRadius = layout.getHoleSizeMm() / 2.0;
        double panelTop = layout.getPanelTopMm();
        double panelBottom = panelTop + version.getPanelLengthMm();
        String board = version + " " + oledInterface;

        Assert.assertTrue(board + " panel overlaps the header-side holes",
            panelTop >= layout.getHoleInsetMm() + holeRadius);
        Assert.assertTrue(board + " panel overlaps the far holes", panelBottom <= layout
            .getBoardLengthMm() - layout.getHoleInsetMm() - holeRadius);
      }
    }
  }

  /**
   * The strip between a top-edge row and the panel below it. On the 0.96" it is 2.05 mm, which is
   * the whole reason that board carries no silkscreen: a flat label needs about 2.7 mm and would
   * cross the panel's top edge. The 1.3" has 6.9 mm and could print its names, which is noted in
   * the plan rather than done here.
   */
  @Test
  public void panelClearsThePinRow() {
    Assert.assertEquals("0.96\"", 2.05d, stripDepthMm(Version.SSD1306_0_96), 0.01d);
    Assert.assertEquals("0.91\" SPI", 2.5d, stripDepthMm(Version.SSD1306_0_91), 0.01d);
    Assert.assertEquals("1.3\"", 6.9d, stripDepthMm(Version.SH1106_1_3), 0.01d);
  }

  private static double stripDepthMm(Version version) {
    Layout layout = version.getLayout(OLEDInterface.SPI_7Pin);
    return layout.getPanelTopMm() - layout.getHeaderOffsetMm();
  }

  @Test
  public void mountingHolesSitInsideTheBoard() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        if (!layout.hasMountingHoles()) {
          continue;
        }
        Assert.assertTrue(version + " " + oledInterface + " holes breach an edge",
            layout.getHoleInsetMm() >= layout.getHoleSizeMm() / 2.0);
      }
    }
  }

  /**
   * Two of the three panels happen to sit centred on their board, and their stored offsets are
   * that centred position rather than a measurement of their own. Checking it keeps the two in
   * step: a later correction to a panel or a board would otherwise leave a stale offset behind,
   * which is exactly how a panel comes to be drawn a millimetre off centre.
   */
  @Test
  public void centredPanelsStoreTheCentredOffsets() {
    for (Version version : new Version[] {Version.SSD1306_0_96, Version.SH1106_1_3}) {
      Layout layout = version.getLayout(OLEDInterface.I2C_4Pin);
      Assert.assertEquals(version + " panel is off centre horizontally",
          (layout.getBoardWidthMm() - version.getPanelWidthMm()) / 2.0, layout.getPanelLeftMm(),
          0.01d);
      Assert.assertEquals(version + " panel is off centre vertically",
          (layout.getBoardLengthMm() - version.getPanelLengthMm()) / 2.0, layout.getPanelTopMm(),
          0.01d);
      Assert.assertEquals(version + " lit area is off centre within its panel",
          (version.getPanelWidthMm() - version.getActiveWidthMm()) / 2.0,
          version.getActiveLeftInPanelMm(), 0.01d);
    }
  }

  /**
   * The 0.96"'s hole pattern was specified as a 24 mm centre-to-centre spacing in both directions
   * and is stored as the equivalent inset, following the TFT boards. If its outline is ever
   * corrected the inset has to be recomputed, and this is what says so.
   */
  @Test
  public void holePatternMatchesTheSpecifiedSpacing() {
    Layout layout = Version.SSD1306_0_96.getLayout(OLEDInterface.I2C_4Pin);
    Assert.assertEquals("across", 24.0d,
        layout.getBoardWidthMm() - 2 * layout.getHoleInsetMm(), 0.01d);
    Assert.assertEquals("down", 24.0d,
        layout.getBoardLengthMm() - 2 * layout.getHoleInsetMm(), 0.01d);
  }

  /** Without this the BOM collapses six different modules into one or two rows. */
  @Test
  public void bomValueCarriesSizeAndInterface() {
    Assert.assertEquals("0.96\" SSD1306 128x64, I2C",
        of(Version.SSD1306_0_96, OLEDInterface.I2C_4Pin).getValueForDisplay());
    Assert.assertEquals("1.3\" SH1106 128x64, SPI",
        of(Version.SH1106_1_3, OLEDInterface.SPI_7Pin).getValueForDisplay());

    java.util.Set<String> values = new java.util.HashSet<String>();
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        values.add(of(version, oledInterface).getValueForDisplay());
      }
    }
    Assert.assertEquals("every board should get its own BOM row",
        Version.values().length * OLEDInterface.values().length, values.size());
  }
}
