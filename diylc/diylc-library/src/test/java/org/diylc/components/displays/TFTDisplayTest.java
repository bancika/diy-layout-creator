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
import java.util.HashSet;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.displays.TFTDisplay.Controller;
import org.diylc.components.maker.MakerBoardTestSupport;
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
      int secondary =
          controller.hasSecondaryHeader() ? controller.getSecondaryPinNames().length : 0;
      Assert.assertEquals(controller.toString(), controller.getPinNames().length + secondary,
          of(controller).getControlPointCount());
    }
    Assert.assertEquals(14, Controller.ILI9341_2_8.getPinNames().length);
    Assert.assertEquals(14, Controller.ILI9341_2_4.getPinNames().length);
    Assert.assertEquals(8, Controller.ST7735_0_96.getPinNames().length);
    Assert.assertEquals(8, Controller.ST7735_1_8.getPinNames().length);
    Assert.assertEquals(8, Controller.ST7789_1_54.getPinNames().length);
    Assert.assertEquals(7, Controller.GC9A01_1_28.getPinNames().length);

    Assert.assertEquals("the 2.4\" carries a second row", 18,
        of(Controller.ILI9341_2_4).getControlPointCount());
    Assert.assertEquals("the 2.8\" carries the same second row", 18,
        of(Controller.ILI9341_2_8).getControlPointCount());
  }

  /**
   * The 2.4" is the same board as the 2.8" with a smaller panel in it, so it shares the pin array
   * rather than repeating it. Asserted by identity: a copy would pass every name check and still
   * drift the day one of the two is corrected.
   */
  @Test
  public void bothILI9341BoardsShareOnePinArray() {
    Assert.assertSame(Controller.ILI9341_2_8.getPinNames(), Controller.ILI9341_2_4.getPinNames());
    Assert.assertEquals(TFTDisplay.SILK_NAMES_ILI9341.length,
        Controller.ILI9341_2_4.getPinNames().length);
  }

  /** Eighteen pins on one part, so a repeated name would reach the netlist as one node. */
  @Test
  public void everyNodeNameIsUnique() {
    for (Controller controller : Controller.values()) {
      TFTDisplay display = of(controller);
      Set<String> names = new HashSet<String>();
      for (int i = 0; i < display.getControlPointCount(); i++) {
        String name = display.getControlPointNodeName(i);
        Assert.assertNotNull(controller + " pin " + i + " has no name", name);
        Assert.assertTrue(controller + " duplicate node name " + name, names.add(name));
      }
    }
  }

  /**
   * Three boards, two controllers, one header: what a module of this size brings out is the
   * four-wire SPI interface plus reset, data/command and the backlight, and that does not vary with
   * the driver behind it. Shared by identity rather than by three copies, so that a correction to
   * one reaches all of them.
   */
  @Test
  public void theSmallBoardsShareOnePinArray() {
    Controller[] small =
        {Controller.ST7735_0_96, Controller.ST7735_1_8, Controller.ST7789_1_54};
    String[] expected = new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC", "CS", "BLK"};

    for (Controller controller : small) {
      Assert.assertSame(controller.toString(), TFTDisplay.PIN_NAMES_SPI_8PIN,
          controller.getPinNames());
      TFTDisplay display = of(controller);
      for (int i = 0; i < expected.length; i++) {
        Assert.assertEquals(controller + " pin " + i, expected[i],
            display.getControlPointNodeName(i));
      }
    }
  }

  /** Switching variants must resize the array, not leave the longer one's points behind. */
  @Test
  public void switchingControllerResizesTheControlPoints() {
    TFTDisplay display = new TFTDisplay();
    Assert.assertEquals(18, display.getControlPointCount());
    display.setController(Controller.ST7735_1_8);
    Assert.assertEquals(8, display.getControlPointCount());
    display.setController(Controller.ILI9341_2_8);
    Assert.assertEquals(18, display.getControlPointCount());
  }

  @Test
  public void pinsSitAtHeaderPitchAlongEachRow() {
    for (Controller controller : Controller.values()) {
      TFTDisplay display = of(controller);
      int primaryCount = controller.getPinNames().length;
      MakerBoardTestSupport.assertRow(display, 0, primaryCount - 1);
      if (controller.hasSecondaryHeader()) {
        MakerBoardTestSupport.assertRow(display, primaryCount,
            primaryCount + controller.getSecondaryPinNames().length - 1);
      }
    }
  }

  /**
   * The SD row faces the 14-pin row across the board and is centred on the width the way the longer
   * row is. Both are worth pinning: the across-the-board figure is what the two offsets leave of
   * the length rather than a measurement, and the horizontal placement is the one figure of this
   * header that was not supplied at all. Both boards that carry one are headed at the bottom, so
   * their second row is on the top edge.
   */
  @Test
  public void theSdRowStandsOffTheOppositeEdge() {
    for (Controller controller : Controller.values()) {
      if (!controller.hasSecondaryHeader()) {
        continue;
      }
      TFTDisplay display = of(controller);
      Rectangle2D board = display.getBodyShape().getBounds2D();
      int primaryCount = controller.getPinNames().length;

      Point2D firstSd = display.getControlPoint(primaryCount);
      double fromItsEdge = controller.isHeaderAtTop() ? board.getMaxY() - firstSd.getY()
          : firstSd.getY() - board.getY();
      Assert.assertEquals(controller + " SD row offset from its own edge",
          controller.getSecondaryHeaderOffsetMm(), mm(fromItsEdge), 0.01d);

      double between = controller.isHeaderAtTop()
          ? firstSd.getY() - display.getControlPoint(0).getY()
          : display.getControlPoint(0).getY() - firstSd.getY();
      Assert.assertEquals(controller + " rows are not on opposite edges",
          controller.getBoardLengthMm() - controller.getHeaderOffsetMm()
              - controller.getSecondaryHeaderOffsetMm(),
          mm(between), 0.01d);

      Point2D lastSd = display.getControlPoint(display.getControlPointCount() - 1);
      Assert.assertEquals(controller + " SD row is not centred across the board",
          firstSd.getX() - board.getX(), board.getMaxX() - lastSd.getX(), 0.01d);

      // It shares its edge with a pair of mounting holes, and unlike the long row it is short
      // enough to pass between them. That is why the top pair keeps the shared inset where the
      // 2.4"'s header-side pair could not.
      double holeRim = controller.getHoleInsetMm() + controller.getHoleSizeMm() / 2.0;
      Assert.assertTrue(controller + " SD row runs into the holes on its edge",
          mm(firstSd.getX() - board.getX()) > holeRim);
      Assert.assertTrue(controller + " SD row runs into the holes on its edge",
          mm(board.getMaxX() - lastSd.getX()) > holeRim);
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
   * on the 1.8" the two pairs are only 3 mm in and the panel very nearly reaches them. The two
   * rows no longer necessarily share an inset, so the band is taken from both figures rather than
   * from the board's middle -- which is also what makes this the assertion that rejects centring
   * the 2.4"'s glass on its lit area.
   */
  @Test
  public void screenClearsTheMountingHoles() {
    for (Controller controller : Controller.values()) {
      if (!controller.hasMountingHoles()) {
        continue;
      }
      double holeRadius = px(controller.getHoleSizeMm()) / 2.0;
      double boardH = px(controller.getBoardLengthMm());
      double bandTop = px(topInsetOf(controller));
      double bandBottom = boardH - px(bottomInsetOf(controller));

      double panelH = panelLengthOf(controller);
      double panelTop = panelTopOf(controller);
      double panelBottom = panelTop + panelH;

      Assert.assertTrue(controller + " panel overlaps the holes on its top edge",
          panelTop >= bandTop + holeRadius);
      Assert.assertTrue(controller + " panel overlaps the holes on its bottom edge",
          panelBottom <= bandBottom - holeRadius);
    }
  }

  /**
   * No mounting hole may be drilled where a pin is. This is the assertion the 2.4"'s deeper
   * header-side pair exists for, and it has to be two-dimensional to say anything useful: that
   * board's 14-pin row spans 33 mm of a 42.72 mm board, so its end pads reach the hole columns and
   * the pair had to move; the 2.8" is wider and its row passes inside them, as a four-pin SD row
   * does on both boards. A one-axis clearance would have forced a deeper inset on boards that do
   * not need one.
   */
  @Test
  public void mountingHolesDoNotOverlapAnyPin() {
    double pin = AbstractMakerBoard.PIN_SIZE.convertToPixels();
    for (Controller controller : Controller.values()) {
      if (!controller.hasMountingHoles()) {
        continue;
      }
      TFTDisplay display = of(controller);
      Rectangle2D board = display.getBodyShape().getBounds2D();
      double inset = px(controller.getHoleInsetMm());
      double headerSide = px(controller.getHeaderSideHoleInsetMm());
      double size = px(controller.getHoleSizeMm());
      double topInset = controller.isHeaderAtTop() ? headerSide : inset;
      double bottomInset = controller.isHeaderAtTop() ? inset : headerSide;

      Rectangle2D[] holes = new Rectangle2D[] {
          hole(board.getX() + inset, board.getY() + topInset, size),
          hole(board.getMaxX() - inset, board.getY() + topInset, size),
          hole(board.getX() + inset, board.getMaxY() - bottomInset, size),
          hole(board.getMaxX() - inset, board.getMaxY() - bottomInset, size)};

      for (int i = 0; i < display.getControlPointCount(); i++) {
        Point2D p = display.getControlPoint(i);
        Rectangle2D pad =
            new Rectangle2D.Double(p.getX() - pin / 2.0, p.getY() - pin / 2.0, pin, pin);
        for (Rectangle2D h : holes) {
          Assert.assertFalse(controller + " a mounting hole is drilled through pin " + i,
              h.intersects(pad));
        }
      }
    }
  }

  private static Rectangle2D hole(double cx, double cy, double size) {
    return new Rectangle2D.Double(cx - size / 2.0, cy - size / 2.0, size, size);
  }

  private static double topInsetOf(Controller controller) {
    return controller.isHeaderAtTop() ? controller.getHeaderSideHoleInsetMm()
        : controller.getHoleInsetMm();
  }

  private static double bottomInsetOf(Controller controller) {
    return controller.isHeaderAtTop() ? controller.getHoleInsetMm()
        : controller.getHeaderSideHoleInsetMm();
  }

  /** The dark panel where there is one, and the lit area alone where there is not. */
  private static double panelLengthOf(Controller controller) {
    return px(controller.hasGlass() ? controller.getGlass().getLengthMm()
        : controller.getScreenLengthMm());
  }

  /** Mirrors the drawing rule: every panel is centred between the hole rows. */
  private static double panelTopOf(Controller controller) {
    double bandTop = controller.hasMountingHoles() ? px(topInsetOf(controller))
        : px(controller.getHeaderOffsetMm());
    double bandBottom = controller.hasMountingHoles()
        ? px(controller.getBoardLengthMm() - bottomInsetOf(controller))
        : px(controller.getBoardLengthMm());
    return (bandTop + bandBottom) / 2.0 - panelLengthOf(controller) / 2.0;
  }

  /**
   * The panel has to leave the pin row uncovered. It is placed between the hole rows rather than
   * from an edge, so a long panel on a board whose holes sit close to the edges can reach back over
   * its own header: the 1.54" clears by 3.51 mm, with its row only 1.5 mm in from the edge. The
   * 2.4" has to be measured from the other end, since its row is on the bottom edge, and it is the
   * only board that must clear a row at each end.
   */
  @Test
  public void panelLeavesThePinRowUncovered() {
    for (Controller controller : Controller.values()) {
      if (controller.isRound()) {
        continue;
      }
      double boardH = controller.getBoardLengthMm();
      double panelTop = mm(panelTopOf(controller));
      double panelBottom = panelTop + mm(panelLengthOf(controller));

      double mainRow = controller.isHeaderAtTop() ? controller.getHeaderOffsetMm()
          : boardH - controller.getHeaderOffsetMm();
      if (controller.isHeaderAtTop()) {
        Assert.assertTrue(controller + " panel covers the main row", panelTop > mainRow);
      } else {
        Assert.assertTrue(controller + " panel covers the main row", panelBottom < mainRow);
      }
      if (controller.hasSecondaryHeader()) {
        Assert.assertTrue(controller + " panel covers the SD row",
            panelTop > controller.getSecondaryHeaderOffsetMm());
      }
    }
  }

  /**
   * The 2.4" is the only board whose lit area is a measurement rather than a derivation, and the
   * reason is worth asserting rather than only commenting: its panel carries the driver's bonding
   * region along the bottom, so the lit area sits high in the glass and the family's centring rule
   * would drop it 4.85 mm. The assertion is that the two disagree -- if a later correction ever
   * makes them agree, the field has stopped earning its place.
   */
  @Test
  public void theLitAreaIsMeasuredOnlyWhereCentringWouldBeWrong() {
    for (Controller controller : Controller.values()) {
      Assert.assertEquals(controller + " measured lit area",
          controller == Controller.ILI9341_2_4, controller.hasMeasuredScreenTop());
      if (!controller.hasMeasuredScreenTop()) {
        continue;
      }

      double glassH = controller.getGlass().getLengthMm();
      double glassTop = mm(panelTopOf(controller));
      double screenTop = controller.getScreenTopMm();

      Assert.assertTrue(controller + " lit area starts above its glass", screenTop >= glassTop);
      Assert.assertTrue(controller + " lit area runs past its glass",
          screenTop + controller.getScreenLengthMm() <= glassTop + glassH);
      Assert.assertTrue(controller + " is measured where centring would have done",
          Math.abs(screenTop - (glassTop + (glassH - controller.getScreenLengthMm()) / 2.0)) > 1d);
    }
  }

  /**
   * The 0.96" is the only board whose panel does not span its width: a 23.7 mm bezel on a 30 mm
   * board, pushed to the right so that the driver's circuitry has the bare L it leaves. That offset
   * is across the board only -- down it the bezel is centred like every other variant's -- and it
   * is a measurement rather than a derivation, since centring it across the board too would move it
   * 1.15 mm. The lit area is centred inside it with a 1 mm frame on all four sides, which the
   * drawing gets for free once the glass is placed, and which would silently break if the lit area
   * went back to being centred on the board.
   */
  @Test
  public void theOffsetPanelIsPlacedFromTheRightEdge() {
    for (Controller controller : Controller.values()) {
      Assert.assertEquals(controller + " panel spans the board",
          controller != Controller.ST7735_0_96,
          !controller.hasGlass() || controller.getGlass().spansTheBoard());
    }

    Controller controller = Controller.ST7735_0_96;
    TFTDisplay.Glass glass = controller.getGlass();

    double leftMargin = controller.getBoardWidthMm() - glass.getRightInsetMm() - glass.getWidthMm();
    Assert.assertEquals("left margin", 4.3d, leftMargin, 0.01d);
    Assert.assertTrue("the bezel is where centring would have put it",
        Math.abs(leftMargin - (controller.getBoardWidthMm() - glass.getWidthMm()) / 2.0) > 0.1d);

    Assert.assertEquals("frame left and right", 1.0d,
        (glass.getWidthMm() - controller.getScreenWidthMm()) / 2.0, 0.01d);
    Assert.assertEquals("frame top and bottom", 1.0d,
        (glass.getLengthMm() - controller.getScreenLengthMm()) / 2.0, 0.01d);
    Assert.assertFalse("its lit area is measured, not derived from a centred glass",
        controller.hasMeasuredScreenTop());
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
   * pair and a narrower 2.5 mm drill for a while, and this guards against either coming back --
   * the 2.4" really does have a deeper pair, which is asserted beside this rather than within it
   * so that the two cases cannot be confused for each other again.
   */
  @Test
  public void mountingHoleFiguresAreEachBoardsOwn() {
    double[][] expected =
        {{3.0d, 3.0d}, {3.0d, 3.0d}, {2.0d, 2.0d}, {3.0d, 3.0d}, {2.5d, 2.0d}};
    Controller[] withHoles = {Controller.ILI9341_2_8, Controller.ILI9341_2_4,
        Controller.ST7735_0_96, Controller.ST7735_1_8, Controller.ST7789_1_54};
    for (int i = 0; i < withHoles.length; i++) {
      Assert.assertEquals(withHoles[i] + " inset", expected[i][0], withHoles[i].getHoleInsetMm(),
          0.01d);
      Assert.assertEquals(withHoles[i] + " diameter", expected[i][1], withHoles[i].getHoleSizeMm(),
          0.01d);
    }

    for (Controller controller : Controller.values()) {
      double expectedHeaderSide =
          controller == Controller.ILI9341_2_4 ? 6.92d : controller.getHoleInsetMm();
      Assert.assertEquals(controller + " header-side inset", expectedHeaderSide,
          controller.getHeaderSideHoleInsetMm(), 0.01d);
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

  /**
   * The round board reads RST first, which is to say leftmost. The array had been in the opposite
   * order, which looked plausible while the tab was drawn at the top and was wrong against the
   * part either way; turning the board over is what made it visible.
   */
  @Test
  public void roundPinNamesComeFromTheController() {
    TFTDisplay display = of(Controller.GC9A01_1_28);
    String[] expected = new String[] {"RST", "CS", "DC", "SDA", "SCL", "GND", "VCC"};
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  /**
   * The outline is a disc with a narrower tab hanging below it, so the corners of its bounding box
   * are off the board while the middles of the top and bottom edges are on it. Taking the tab only
   * as far as the bottom of the disc would have left a notch either side of the tangent point,
   * which these corners would not catch but the centre-line check would: there the disc is wider
   * than the tab.
   */
  @Test
  public void roundBodyIsADiscWithATab() {
    TFTDisplay display = of(Controller.GC9A01_1_28);
    Shape body = display.getBodyShape();
    Rectangle2D bounds = body.getBounds2D();

    Assert.assertEquals("disc diameter across", 38.0d, mm(bounds.getWidth()), 0.01d);
    Assert.assertEquals("disc top to tab tip", 45.5d, mm(bounds.getHeight()), 0.01d);

    double x = bounds.getX();
    double y = bounds.getY();

    Assert.assertTrue("the top of the disc is off the board",
        body.contains(x + px(19.0d), y + px(1.0d)));
    Assert.assertTrue("the middle of the tab is off the board",
        body.contains(x + px(19.0d), y + px(44.5d)));
    Assert.assertFalse("the top left corner is on the board",
        body.contains(x + px(1.0d), y + px(1.0d)));
    Assert.assertFalse("the bottom left corner is on the board",
        body.contains(x + px(1.0d), y + px(44.5d)));

    // at the disc's centre line the board is wider than the tab, which is what proves the union
    // took the disc's outline and not the rectangle's; a third of the way further down only the
    // tab is left, and the same point is off the board
    Assert.assertTrue("the disc is no wider than its tab",
        body.contains(x + px(2.0d), y + px(19.0d)));
    Assert.assertFalse("the board is as wide as its disc below the centre line",
        body.contains(x + px(2.0d), y + px(30.0d)));
  }

  /**
   * Which edge the main row leaves by, which is a fact about each board rather than a drawing
   * convention: with the pin order fixed left to right, a header at the top and a header at the
   * bottom are mirror images and not the same part. The two ILI9341s and the round board stand
   * above their row; the
   * 0.96", the 1.8" and the 1.54" hang below theirs. Asserted for every variant because nothing
   * else in the geometry would notice one being flipped.
   */
  @Test
  public void headerSitsOnItsOwnEdge() {
    for (Controller controller : Controller.values()) {
      TFTDisplay display = of(controller);
      Rectangle2D bounds = display.getBodyShape().getBounds2D();
      Point2D firstPin = display.getControlPoint(0);
      double fromEdge = controller.isHeaderAtTop() ? firstPin.getY() - bounds.getY()
          : bounds.getMaxY() - firstPin.getY();

      Assert.assertEquals(controller + " header offset", controller.getHeaderOffsetMm(),
          mm(fromEdge), 0.01d);
    }
    // A roll call rather than a rule: every one of these was sourced from the part, and a future
    // variant must be too rather than inheriting whichever way the majority happens to go.
    Assert.assertTrue("0.96\"", Controller.ST7735_0_96.isHeaderAtTop());
    Assert.assertTrue("1.8\"", Controller.ST7735_1_8.isHeaderAtTop());
    Assert.assertTrue("1.54\"", Controller.ST7789_1_54.isHeaderAtTop());
    Assert.assertFalse("2.8\"", Controller.ILI9341_2_8.isHeaderAtTop());
    Assert.assertFalse("2.4\"", Controller.ILI9341_2_4.isHeaderAtTop());
    Assert.assertFalse("1.28\" round", Controller.GC9A01_1_28.isHeaderAtTop());
  }

  /**
   * Every panel prints its description, the round one included -- it is the only variant whose
   * text has to fit a circle, and it does so by being given the square inscribed in the glass.
   */
  @Test
  public void everyPanelPrintsItsDescription() {
    for (Controller controller : Controller.values()) {
      TFTDisplay printed = of(controller);
      TFTDisplay blank = of(controller);
      blank.setScreen(Display.NONE);

      Assert.assertEquals(Display.VALUE, printed.getScreen());
      MakerBoardTestSupport.assertScreenTextIsDrawn(printed, blank);
    }
  }
}
