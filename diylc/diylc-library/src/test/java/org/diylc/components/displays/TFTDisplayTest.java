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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.displays.TFTDisplay.Controller;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the controller variants. The pin count differs between them, so the checks
 * that matter are the ones that would catch an array and an outline disagreeing: that the header
 * still fits across the board it belongs to, and that the screen and the mounting holes do not
 * land on top of each other once a different outline is in play.
 */
public class TFTDisplayTest {

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

  private static double px(double millimetres) {
    return new Size(millimetres, SizeUnit.mm).convertToPixels();
  }

  private static TFTDisplay of(Controller controller) {
    TFTDisplay display = new TFTDisplay();
    display.setController(controller);
    return display;
  }

  @Test
  public void boardSizeFollowsTheController() {
    for (Controller controller : Controller.values()) {
      Rectangle2D bounds = of(controller).getBodyShape().getBounds2D();
      Assert.assertEquals(controller + " width", controller.getBoardWidthMm(),
          mm(bounds.getWidth()), 0.01d);
      Assert.assertEquals(controller + " length", controller.getBoardLengthMm(),
          mm(bounds.getHeight()), 0.01d);
    }
  }

  @Test
  public void controlPointCountFollowsThePinArray() {
    for (Controller controller : Controller.values()) {
      Assert.assertEquals(controller.toString(), controller.getPinNames().length,
          of(controller).getControlPointCount());
    }
    Assert.assertEquals(14, Controller.ILI9341_2_8.getPinNames().length);
    Assert.assertEquals(8, Controller.ST7735_1_8.getPinNames().length);
    Assert.assertEquals(8, Controller.ST7789_1_54.getPinNames().length);
    Assert.assertEquals(7, Controller.GC9A01_1_28.getPinNames().length);
  }

  @Test
  public void pinNamesComeFromTheController() {
    TFTDisplay display = of(Controller.ST7735_1_8);
    String[] expected = new String[] {"GND", "VCC", "SCK", "SDA", "RES", "DC", "CS", "BL"};
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  @Test
  public void squareBoardPinNamesComeFromTheController() {
    TFTDisplay display = of(Controller.ST7789_1_54);
    String[] expected = new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC", "CS", "BLK"};
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  /** Switching variants must resize the array, not leave the longer one's points behind. */
  @Test
  public void switchingControllerResizesTheControlPoints() {
    TFTDisplay display = new TFTDisplay();
    Assert.assertEquals(14, display.getControlPointCount());
    display.setController(Controller.ST7735_1_8);
    Assert.assertEquals(8, display.getControlPointCount());
    display.setController(Controller.ILI9341_2_8);
    Assert.assertEquals(14, display.getControlPointCount());
  }

  @Test
  public void pinsSitAtHeaderPitchAlongOneRow() {
    for (Controller controller : Controller.values()) {
      TFTDisplay display = of(controller);
      for (int i = 1; i < display.getControlPointCount(); i++) {
        Point2D previous = display.getControlPoint(i - 1);
        Point2D current = display.getControlPoint(i);
        Assert.assertEquals(controller + " pitch at pin " + i, 2.54d,
            mm(current.getX() - previous.getX()), 0.01d);
        Assert.assertEquals(controller + " pin " + i + " left the row", previous.getY(),
            current.getY(), 0.01d);
      }
    }
  }

  /** A pin array longer than its board would hang the header off both ends. */
  @Test
  public void headerFitsAcrossTheBoard() {
    for (Controller controller : Controller.values()) {
      TFTDisplay display = of(controller);
      Rectangle2D board = display.getBodyShape().getBounds2D();
      for (int i = 0; i < display.getControlPointCount(); i++) {
        Point2D pin = display.getControlPoint(i);
        Assert.assertTrue(controller + " pin " + i + " hangs off the board",
            pin.getX() >= board.getX() && pin.getX() <= board.getMaxX());
      }
    }
  }

  @Test
  public void screenFitsWithinTheBoard() {
    for (Controller controller : Controller.values()) {
      Assert.assertTrue(controller + " screen is wider than its board",
          controller.getScreenWidthMm() < controller.getBoardWidthMm());
      Assert.assertTrue(controller + " screen is longer than its board",
          controller.getScreenLengthMm() < controller.getBoardLengthMm());
    }
  }

  /**
   * The screen is centred between the hole rows, so a board whose holes move has to be rechecked:
   * on the 1.8" the two pairs are only 3 mm in and the panel very nearly reaches them.
   */
  @Test
  public void screenClearsTheMountingHoles() {
    for (Controller controller : Controller.values()) {
      if (!controller.hasMountingHoles()) {
        continue;
      }
      double holeInset = px(controller.getHoleInsetMm());
      double holeRadius = px(controller.getHoleSizeMm()) / 2.0;
      double boardH = px(controller.getBoardLengthMm());

      // every hole shares one inset, so the midpoint of the two rows is the middle of the board
      double bandCentre = boardH / 2.0;
      double panelH = controller.hasGlass() ? px(controller.getGlassLengthMm())
          : px(controller.getScreenLengthMm());
      double panelTop = bandCentre - panelH / 2.0;
      double panelBottom = bandCentre + panelH / 2.0;

      Assert.assertTrue(controller + " panel overlaps the header-side holes",
          panelTop >= holeInset + holeRadius);
      Assert.assertTrue(controller + " panel overlaps the far holes",
          panelBottom <= boardH - holeInset - holeRadius);
    }
  }

  /**
   * The panel has to start below the pin row. It is placed between the hole rows rather than from
   * an edge, so a long panel on a board whose holes sit close to the edges can reach back over its
   * own header: the 1.54" clears by 3.51 mm, with its row only 1.5 mm in from the edge.
   */
  @Test
  public void panelStartsBelowThePinRow() {
    for (Controller controller : Controller.values()) {
      if (controller.isRound()) {
        continue;
      }
      double boardH = controller.getBoardLengthMm();
      double bandTop = controller.hasMountingHoles() ? controller.getHoleInsetMm()
          : controller.getHeaderOffsetMm();
      double bandBottom =
          controller.hasMountingHoles() ? boardH - controller.getHoleInsetMm() : boardH;
      double panelH = controller.hasGlass() ? controller.getGlassLengthMm()
          : controller.getScreenLengthMm();
      double panelTop = (bandTop + bandBottom) / 2.0 - panelH / 2.0;

      Assert.assertTrue(controller + " panel covers the pin row",
          panelTop > controller.getHeaderOffsetMm());
    }
  }

  @Test
  public void mountingHolesSitInsideTheBoard() {
    for (Controller controller : Controller.values()) {
      if (!controller.hasMountingHoles()) {
        continue;
      }
      double holeRadius = controller.getHoleSizeMm() / 2.0;
      Assert.assertTrue(controller + " holes breach an edge",
          controller.getHoleInsetMm() >= holeRadius);
      Assert.assertTrue(controller + " far holes breach the bottom edge",
          controller.getHoleInsetMm() + holeRadius <= controller.getBoardLengthMm());
    }
  }

  /**
   * Each board's holes are one drill at one inset from each of the two edges nearest them, but the
   * figures are the board's own rather than the package's: the two rectangular SPI boards are 3 mm
   * at 3 mm and the 1.54" is 2 mm at 2.5 mm. The 2.8" board had a deeper inset on its header-side
   * pair and a narrower 2.5 mm drill for a while, and this guards against either coming back.
   */
  @Test
  public void mountingHoleFiguresAreEachBoardsOwn() {
    double[][] expected = {{3.0d, 3.0d}, {3.0d, 3.0d}, {2.5d, 2.0d}};
    Controller[] withHoles =
        {Controller.ILI9341_2_8, Controller.ST7735_1_8, Controller.ST7789_1_54};
    for (int i = 0; i < withHoles.length; i++) {
      Assert.assertEquals(withHoles[i] + " inset", expected[i][0], withHoles[i].getHoleInsetMm(),
          0.01d);
      Assert.assertEquals(withHoles[i] + " diameter", expected[i][1], withHoles[i].getHoleSizeMm(),
          0.01d);
    }
  }

  /** Without this the BOM collapses every variant of the part into a single row. */
  @Test
  public void bomValueCarriesTheController() {
    for (Controller controller : Controller.values()) {
      Assert.assertEquals(controller.toString(), of(controller).getValueForDisplay());
    }
    Assert.assertNotEquals(of(Controller.ILI9341_2_8).getValueForDisplay(),
        of(Controller.ST7735_1_8).getValueForDisplay());
  }

  /** The 2.8" and 1.54" modules carry a dark panel wider than their lit area; the others do not. */
  @Test
  public void glassPanelFollowsTheModule() {
    Assert.assertTrue(Controller.ILI9341_2_8.hasGlass());
    Assert.assertTrue(Controller.ST7789_1_54.hasGlass());
    Assert.assertFalse(Controller.ST7735_1_8.hasGlass());
    Assert.assertFalse(Controller.GC9A01_1_28.hasGlass());
  }

  @Test
  public void onlyTheRoundBoardHasATab() {
    Assert.assertFalse(Controller.ILI9341_2_8.isRound());
    Assert.assertFalse(Controller.ST7735_1_8.isRound());
    Assert.assertFalse(Controller.ST7789_1_54.isRound());
    Assert.assertTrue(Controller.GC9A01_1_28.isRound());
    Assert.assertEquals(22.9d, Controller.GC9A01_1_28.getTabWidthMm(), 0.01d);
  }

  /** The projection is the outline's length beyond its width, not a figure of its own. */
  @Test
  public void tabProjectionFollowsFromTheOutline() {
    Assert.assertEquals(7.5d, Controller.GC9A01_1_28.getTabProjectionMm(), 0.01d);
  }

  @Test
  public void roundBoardHasNoMountingHoles() {
    Assert.assertFalse(Controller.GC9A01_1_28.hasMountingHoles());
  }

  @Test
  public void roundPinNamesComeFromTheController() {
    TFTDisplay display = of(Controller.GC9A01_1_28);
    String[] expected = new String[] {"VCC", "GND", "SCL", "SDA", "DC", "CS", "RST"};
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  /**
   * The outline is a disc with a narrower tab on it, so the corners of its bounding box are off the
   * board while the middle of the top edge is on it. Taking the tab only as far as the top of the
   * disc would have left a notch either side of the tangent point, which these corners would not
   * catch but the lower check would: low down, the disc is wider than the tab.
   */
  @Test
  public void roundBodyIsADiscWithATab() {
    TFTDisplay display = of(Controller.GC9A01_1_28);
    Shape body = display.getBodyShape();
    Rectangle2D bounds = body.getBounds2D();

    Assert.assertEquals("disc diameter across", 38.0d, mm(bounds.getWidth()), 0.01d);
    Assert.assertEquals("tab tip to disc bottom", 45.5d, mm(bounds.getHeight()), 0.01d);

    double x = bounds.getX();
    double y = bounds.getY();

    Assert.assertTrue("the middle of the tab is off the board",
        body.contains(x + px(19.0d), y + px(1.0d)));
    Assert.assertFalse("the top left corner is on the board",
        body.contains(x + px(1.0d), y + px(1.0d)));
    Assert.assertFalse("the top right corner is on the board",
        body.contains(x + px(37.0d), y + px(1.0d)));

    // at the disc's centre line the board is wider than the tab, which is what proves the union
    // took the disc's outline and not the rectangle's
    Assert.assertTrue("the disc is no wider than its tab",
        body.contains(x + px(2.0d), y + px(26.5d)));
    Assert.assertTrue("the centre of the disc is off the board",
        body.contains(x + px(19.0d), y + px(26.5d)));
  }
}
