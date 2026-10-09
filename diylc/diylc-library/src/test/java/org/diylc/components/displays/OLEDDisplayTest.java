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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.displays.OLEDDisplay.Layout;
import org.diylc.components.displays.OLEDDisplay.OLEDInterface;
import org.diylc.components.displays.OLEDDisplay.Version;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the three OLED modules. The interface is not orthogonal to the size: the
 * 0.91" is sold as two different boards, so every check that reads a board figure reads it for a
 * size and an interface together.
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
   * On the 0.91" the two interfaces are not one board with a longer header but two boards,
   * differing in outline, in which edge the header sits on and in whether there are mounting holes
   * at all. The other two sizes do carry one board across both interfaces.
   */
  @Test
  public void theSmallBoardIsTwoDifferentBoards() {
    Layout i2c = Version.SSD1306_0_91.getLayout(OLEDInterface.I2C_4Pin);
    Layout spi = Version.SSD1306_0_91.getLayout(OLEDInterface.SPI_7Pin);

    Assert.assertEquals("I2C width", 35.8d, i2c.getBoardWidthMm(), 0.01d);
    Assert.assertEquals("I2C length", 12.0d, i2c.getBoardLengthMm(), 0.01d);
    Assert.assertEquals("SPI width", 32.5d, spi.getBoardWidthMm(), 0.01d);
    Assert.assertEquals("SPI length", 20.5d, spi.getBoardLengthMm(), 0.01d);
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
    String[] expected = new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC", "CS"};
    Assert.assertEquals(expected.length, display.getControlPointCount());
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  @Test
  public void controlPointsAreTheEightPinRowInOrder() {
    OLEDDisplay display = of(Version.SSD1306_0_96, OLEDInterface.SPI_8Pin);
    String[] expected = new String[] {"SDA", "SCL", "DC", "RES", "CS", "VDD", "VIN", "GND"};
    Assert.assertEquals(expected.length, display.getControlPointCount());
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  /** Every size is sold in every interface, so no size may carry a shorter pin array. */
  @Test
  public void everyInterfaceExistsAtEverySize() {
    for (Version version : Version.values()) {
      Assert.assertEquals(version + " I2C", 4,
          of(version, OLEDInterface.I2C_4Pin).getControlPointCount());
      Assert.assertEquals(version + " SPI", 7,
          of(version, OLEDInterface.SPI_7Pin).getControlPointCount());
      Assert.assertEquals(version + " SPI-6", 6,
          of(version, OLEDInterface.SPI_6Pin).getControlPointCount());
      Assert.assertEquals(version + " SPI-8", 8,
          of(version, OLEDInterface.SPI_8Pin).getControlPointCount());
    }
  }

  /**
   * The 6-pin board is the 7-pin one with the chip select tied low, so its row is the other's
   * without that pin and in the same order. It borrows the 7-pin board's outline, which is a
   * derivation rather than a measurement.
   */
  @Test
  public void theSixPinBoardDropsOnlyTheChipSelect() {
    String[] seven = OLEDInterface.SPI_7Pin.getPinNames();
    String[] six = OLEDInterface.SPI_6Pin.getPinNames();

    Assert.assertEquals("one pin fewer", seven.length - 1, six.length);
    Assert.assertEquals("CS is the pin that goes", "CS", seven[seven.length - 1]);
    for (int i = 0; i < six.length; i++) {
      Assert.assertEquals("pin " + i, seven[i], six[i]);
    }

    for (Version version : Version.values()) {
      Assert.assertSame(version + " shares the SPI board",
          version.getLayout(OLEDInterface.SPI_7Pin), version.getLayout(OLEDInterface.SPI_6Pin));
    }
  }

  /**
   * The eight-pin row cannot be compared in order, following the controller's pin numbering rather
   * than the silk the others print, so what has to hold is that the set has grown by exactly VDD.
   * VDD and VIN are separate rails, so neither may be renamed to the other or to VCC, which a set
   * comparison alone would let through.
   */
  @Test
  public void theEightPinBoardAddsOnlyTheLogicSupply() {
    Set<String> seven = new HashSet<String>(Arrays.asList(OLEDInterface.SPI_7Pin.getPinNames()));
    Set<String> eight = new HashSet<String>(Arrays.asList(OLEDInterface.SPI_8Pin.getPinNames()));

    Assert.assertEquals("no pin names repeat in the row", 8,
        OLEDInterface.SPI_8Pin.getPinNames().length);
    Assert.assertEquals("no pin names repeat in the row", 8, eight.size());
    Assert.assertTrue("VIN replaces the shorter rows' VCC", eight.contains("VIN"));
    Assert.assertTrue("the logic rail is the pin that is added", eight.contains("VDD"));
    Assert.assertFalse("VCC belongs to the rows with one supply", eight.contains("VCC"));

    seven.remove("VCC");
    eight.remove("VIN");
    eight.remove("VDD");
    Assert.assertEquals("the signal lines are the seven-pin row's", seven, eight);
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
    display.setOledInterface(OLEDInterface.SPI_6Pin);
    Assert.assertEquals(6, display.getControlPointCount());
    display.setOledInterface(OLEDInterface.SPI_8Pin);
    Assert.assertEquals(8, display.getControlPointCount());
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
   * Nothing else in the geometry would notice if a board were placed from its first pin instead of
   * being centred: a four-pin row would sit in a corner and a seven-pin one would look very nearly
   * centred anyway.
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
   * The column stands its clearance in from the left edge and is centred on the height. A board
   * drawn down and left from its first pin instead would keep every pitch and size assertion above.
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
   * A lit area's aspect is very nearly the pixel aspect: 2:1 on the two 128 x 64 panels and 4:1 on
   * the 128 x 32. The two derived panels sit exactly on it; the measured 0.96" comes out a little
   * under, which the tolerance allows while still catching a transposition.
   */
  @Test
  public void litAreaMatchesThePixelAspect() {
    Assert.assertEquals(2.0d, aspect(Version.SSD1306_0_96), 0.05d);
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

      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        Layout layout = version.getLayout(oledInterface);
        String board = version + " " + oledInterface;
        Assert.assertTrue(board + " lit area starts above its panel",
            layout.getActiveTopMm() >= layout.getPanelTopMm());
        Assert.assertTrue(board + " lit area runs off the bottom of its panel",
            layout.getActiveTopMm() + version.getActiveLengthMm() <= layout.getPanelTopMm()
                + version.getPanelLengthMm());
      }
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
   * Where the lit area lands on the board. Getting either half wrong moves it while leaving every
   * size and clearance assertion intact, and swapping the two between the boards leaves both
   * plausible -- which is what {@link #pinsStayOffThePanel} is for.
   */
  @Test
  public void litAreaSitsWhereItWasMeasured() {
    Assert.assertEquals("0.91\" I2C", 7.1d,
        litAreaLeftMm(Version.SSD1306_0_91, OLEDInterface.I2C_4Pin), 0.01d);
    Assert.assertEquals("0.91\" SPI", 3.35d,
        litAreaLeftMm(Version.SSD1306_0_91, OLEDInterface.SPI_7Pin), 0.01d);
  }

  /**
   * No pin may land on the glass. Every other check measures features against each other, so the
   * two 0.91" panel offsets could be swapped between the boards and pass all of them -- and
   * swapped, the I2C board's four pins are drawn on the display.
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
   * The 0.91" SPI board's measured 1.25 mm panel offset is exactly centred on its measured 32.5 mm
   * width, two independent readings agreeing. The vertical pair do not: the panel hangs below the
   * header rather than sitting centred, so there is nothing to cross-check down the board.
   */
  @Test
  public void theSmallSpiBoardCentresItsPanelAcrossTheBoard() {
    Version version = Version.SSD1306_0_91;
    Layout layout = version.getLayout(OLEDInterface.SPI_7Pin);
    Assert.assertEquals("the panel is off centre across the board",
        (layout.getBoardWidthMm() - version.getPanelWidthMm()) / 2.0, layout.getPanelLeftMm(),
        0.01d);
  }

  /**
   * Every lit area is measured down from its board's top edge rather than centred on the panel,
   * the driver's bonding region taking the bottom of the glass. The centred position is asserted
   * beside each measurement because a stale one would still land inside the panel and clear every
   * pin and hole.
   */
  @Test
  public void measuredLitAreasSitHighInTheirPanels() {
    assertLitAreaTop(Version.SSD1306_0_96, OLEDInterface.I2C_4Pin, 6.14d, 8.0d);
    assertLitAreaTop(Version.SSD1306_0_91, OLEDInterface.I2C_4Pin, 2.35d, 3.208d);
    assertLitAreaTop(Version.SSD1306_0_91, OLEDInterface.SPI_7Pin, 6.6d, 7.208d);
    assertLitAreaTop(Version.SH1106_1_3, OLEDInterface.I2C_4Pin, 7.35d, 9.4d);
  }

  private static void assertLitAreaTop(Version version, OLEDInterface oledInterface,
      double measuredMm, double centredMm) {
    Layout layout = version.getLayout(oledInterface);
    Assert.assertEquals(version + " measured from the top edge", measuredMm,
        layout.getActiveTopMm(), 0.01d);
    Assert.assertEquals(version + " centring would drop it", centredMm, layout.getPanelTopMm()
        + (version.getPanelLengthMm() - version.getActiveLengthMm()) / 2.0, 0.01d);
  }

  /**
   * The panel shares the holes' horizontal span on every board, so only the vertical axis can keep
   * them apart and a symmetric check would fail on a board that is drawn correctly. The 0.91" SPI
   * board is the tight one, clearing its header-side holes by 0.5 mm.
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
   * The strip the pin names have to fit into. The 0.96"'s 2.27 mm is the shallowest, and the
   * reason these boards place their own labels rather than using the shared helper.
   */
  @Test
  public void panelClearsThePinRow() {
    Assert.assertEquals("0.96\"", 2.27d, stripDepthMm(Version.SSD1306_0_96), 0.01d);
    Assert.assertEquals("0.91\" SPI", 2.5d, stripDepthMm(Version.SSD1306_0_91), 0.01d);
    Assert.assertEquals("1.3\"", 3.75d, stripDepthMm(Version.SH1106_1_3), 0.01d);
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
   * Two panels sit centred on their board, and their stored offsets are that centred position
   * rather than a measurement. Checking it keeps the two in step, so that a later correction to a
   * panel or a board cannot leave a stale offset behind.
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
   * Every board prints its pin names and none prints one on the glass. No geometry assertion here
   * would notice a label drawn over the display, and the shared helper's offset would put one
   * there on the 0.96".
   *
   * <p>The silkscreen colour is repointed at a colour nothing else on these boards draws in, since
   * mounting holes are filled in the canvas's white and would otherwise count as lettering.
   */
  @Test
  public void pinNamesAreDrawnAndStayOffThePanel() {
    Color silk = AbstractMakerBoard.SILK_COLOR;
    AbstractMakerBoard.SILK_COLOR = Color.MAGENTA;
    try {
      for (Version version : Version.values()) {
        for (OLEDInterface oledInterface : OLEDInterface.values()) {
          Layout layout = version.getLayout(oledInterface);
          OLEDDisplay display = of(version, oledInterface);
          int[] pixels = MakerBoardTestSupport.renderPixels(display);
          int side = (int) Math.round(Math.sqrt(pixels.length));

          Rectangle2D outline = display.getBodyShape().getBounds2D();
          Rectangle2D glass = new Rectangle2D.Double(
              outline.getX() + mmToPx(layout.getPanelLeftMm()),
              outline.getY() + mmToPx(layout.getPanelTopMm()),
              mmToPx(version.getPanelWidthMm()), mmToPx(version.getPanelLengthMm()));

          int printed = 0;
          int onGlass = 0;
          for (int i = 0; i < pixels.length; i++) {
            if (!isSilk(pixels[i])) {
              continue;
            }
            printed++;
            if (glass.contains(i % side, i / side)) {
              onGlass++;
            }
          }

          String board = version + " " + oledInterface;
          Assert.assertTrue(board + " prints no pin names at all", printed > 0);
          Assert.assertEquals(board + " prints a pin name on the panel", 0, onGlass);
        }
      }
    } finally {
      AbstractMakerBoard.SILK_COLOR = silk;
    }
  }

  /**
   * Matching hue rather than value catches lettering that antialiasing has dimmed where it clips
   * the glass. Nothing else drawn on these boards leaves red and blue both well ahead of green.
   */
  private static boolean isSilk(int argb) {
    int red = (argb >> 16) & 0xFF;
    int green = (argb >> 8) & 0xFF;
    int blue = argb & 0xFF;
    return red > 60 && blue > 60 && red > green * 1.5 && blue > green * 1.5;
  }

  /** Without this the BOM collapses six different modules into one or two rows. */
  @Test
  public void bomValueCarriesSizeAndInterface() {
    Assert.assertEquals("0.96\" SSD1306 128x64, I2C",
        of(Version.SSD1306_0_96, OLEDInterface.I2C_4Pin).getValueForDisplay());
    Assert.assertEquals("1.3\" SH1106 128x64, SPI-7",
        of(Version.SH1106_1_3, OLEDInterface.SPI_7Pin).getValueForDisplay());

    Set<String> values = new HashSet<String>();
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        values.add(of(version, oledInterface).getValueForDisplay());
      }
    }
    Assert.assertEquals("every board should get its own BOM row",
        Version.values().length * OLEDInterface.values().length, values.size());
  }

  /**
   * Rendered rather than asked for: a screen that found no room for the text would still answer
   * the getter.
   */
  @Test
  public void everyPanelPrintsItsDescription() {
    for (Version version : Version.values()) {
      for (OLEDInterface oledInterface : OLEDInterface.values()) {
        OLEDDisplay printed = of(version, oledInterface);
        OLEDDisplay blank = of(version, oledInterface);
        blank.setScreen(Display.NONE);

        Assert.assertEquals(Display.VALUE, printed.getScreen());
        MakerBoardTestSupport.assertScreenTextIsDrawn(printed, blank);
      }
    }
  }

  /**
   * The pixels are the only thing the colour touches -- there is no backlight behind them and no
   * ink in front -- and the text has to come through whichever one is picked.
   */
  @Test
  public void theLitAreaFollowsThePixelColour() {
    OLEDDisplay white = new OLEDDisplay();
    white.setPixelColor(Color.decode("#F5F5F5"));
    OLEDDisplay whiteBlank = new OLEDDisplay();
    whiteBlank.setPixelColor(Color.decode("#F5F5F5"));
    whiteBlank.setScreen(Display.NONE);

    OLEDDisplay yellow = new OLEDDisplay();
    yellow.setPixelColor(Color.decode("#FFD54F"));
    OLEDDisplay yellowBlank = new OLEDDisplay();
    yellowBlank.setPixelColor(Color.decode("#FFD54F"));
    yellowBlank.setScreen(Display.NONE);

    MakerBoardTestSupport.assertScreenTextIsDrawn(white, whiteBlank);
    MakerBoardTestSupport.assertScreenTextIsDrawn(yellow, yellowBlank);
  }

  /** A project saved before the pixel colour existed deserialises with none set. */
  @Test
  public void aMissingPixelColourFallsBackToBlue() {
    OLEDDisplay display = new OLEDDisplay();
    display.setPixelColor(null);
    Assert.assertEquals(OLEDDisplay.PIXEL_BLUE, display.getPixelColor());
  }
}
