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
import java.util.HashSet;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the single-LED breakout. Three things compete for a board half an inch by four
 * tenths: a three-pad column up each short edge, the two mounting holes on the centre line between
 * them, and the package in the middle. The holes are what everything else is tight against -- 0.3
 * inch apart across a 0.4 inch height leaves 0.27 mm of board outside each rim and 0.31 mm between
 * a rim and the package -- so what is asserted here is that none of the three has taken another's
 * room.
 */
public class WS2812BBreakoutTest {

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

  private static Rectangle2D padRect(WS2812BBreakout breakout, int index) {
    Point2D pad = breakout.getControlPoint(index);
    double w = WS2812BBreakout.PAD_WIDTH.convertToPixels();
    double h = WS2812BBreakout.PAD_LENGTH.convertToPixels();
    return new Rectangle2D.Double(pad.getX() - w / 2.0, pad.getY() - h / 2.0, w, h);
  }

  private static Rectangle2D holeRect(WS2812BBreakout breakout, boolean bottom) {
    Point2D hole = breakout.getHoleCentre(bottom);
    double r = WS2812BBreakout.MOUNTING_HOLE_SIZE.convertToPixels() / 2.0;
    return new Rectangle2D.Double(hole.getX() - r, hole.getY() - r, r * 2, r * 2);
  }

  private static Rectangle2D ledPackage(WS2812BBreakout breakout) {
    Point2D led = breakout.getLedCentre();
    double half = AbstractMakerBoard.RGB_LED_SIZE.convertToPixels() / 2.0;
    return new Rectangle2D.Double(led.getX() - half, led.getY() - half, half * 2, half * 2);
  }

  @Test
  public void boardIsHalfAnInchByFourTenths() {
    MakerBoardTestSupport.assertBoardSize(new WS2812BBreakout(), WS2812BBreakout.BOARD_WIDTH,
        WS2812BBreakout.BOARD_HEIGHT);
  }

  /** Ground, power and a data line up each long edge, and no two pads sharing a name. */
  @Test
  public void eachEdgeCarriesThreeDistinctlyNamedPads() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    Assert.assertEquals(6, breakout.getControlPointCount());

    Set<String> names = new HashSet<String>();
    for (int i = 0; i < breakout.getControlPointCount(); i++) {
      Assert.assertTrue("duplicate node name " + breakout.getControlPointNodeName(i),
          names.add(breakout.getControlPointNodeName(i)));
    }
    Assert.assertEquals("IN", breakout.getControlPointNodeName(2));
    Assert.assertEquals("OUT", breakout.getControlPointNodeName(5));
  }

  /**
   * The two grounds are one net on the real board, as are the two supplies, so their names differ
   * only by a disambiguator that comes off again wherever a pad is named to the user.
   */
  @Test
  public void theNumberedPadsReadAsOneName() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    for (int i : new int[] {0, 3}) {
      Assert.assertEquals("GND",
          AbstractMakerBoard.getDisplayPinLabel(breakout.getControlPointNodeName(i)));
    }
    for (int i : new int[] {1, 4}) {
      Assert.assertEquals("VIN",
          AbstractMakerBoard.getDisplayPinLabel(breakout.getControlPointNodeName(i)));
    }
  }

  @Test
  public void padColumnsRunUpTheShortEdgesAtHeaderPitch() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    MakerBoardTestSupport.assertRow(breakout, 0, 2);
    MakerBoardTestSupport.assertRow(breakout, 3, 5);

    Rectangle2D board = breakout.getBodyShape().getBounds2D();
    for (int i = 0; i < 3; i++) {
      Assert.assertEquals("left pad " + i, board.getX(), padRect(breakout, i).getX(), 0.01d);
      Assert.assertEquals("right pad " + i, board.getMaxX(), padRect(breakout, 3 + i).getMaxX(),
          0.01d);
    }
  }

  @Test
  public void holesSitOnTheCentreLineASpanApartAstrideTheLed() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    Rectangle2D board = breakout.getBodyShape().getBounds2D();
    Point2D top = breakout.getHoleCentre(false);
    Point2D bottom = breakout.getHoleCentre(true);

    Assert.assertEquals("span", WS2812BBreakout.MOUNTING_HOLE_SPACING.convertToPixels(),
        bottom.getY() - top.getY(), 0.01d);
    Assert.assertEquals("not centred across the board", board.getCenterX(), top.getX(), 0.01d);
    Assert.assertEquals("not centred along the board", board.getCenterY(),
        (top.getY() + bottom.getY()) / 2.0, 0.01d);
  }

  /**
   * The margin the 0.3 inch span leaves outside each rim. Asserted as the figure it works out to
   * rather than as mere containment, because a quarter of a millimetre is the whole of it: any
   * revision to the board height or the hole spacing eats it entirely.
   */
  @Test
  public void aQuarterMillimetreOfBoardSurvivesOutsideEachHole() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    Rectangle2D board = breakout.getBodyShape().getBounds2D();

    Assert.assertEquals("above the top hole", 0.27d,
        mm(holeRect(breakout, false).getY() - board.getY()), 0.01d);
    Assert.assertEquals("below the bottom hole", 0.27d,
        mm(board.getMaxY() - holeRect(breakout, true).getMaxY()), 0.01d);
  }

  /**
   * The package has the middle, and the holes and pads are placed to leave it there. The holes are
   * the pair worth watching: 0.31 mm of board separates a rim from the package, so this fails on
   * any revision that brings the two together rather than letting them overlap unnoticed.
   */
  @Test
  public void thePackageClearsTheHolesAndThePads() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    Rectangle2D pack = ledPackage(breakout);

    for (boolean bottom : new boolean[] {false, true}) {
      Assert.assertFalse("the package sits over a mounting hole",
          pack.intersects(holeRect(breakout, bottom)));
    }
    for (int i = 0; i < breakout.getControlPointCount(); i++) {
      Assert.assertFalse("the package runs under pad " + i,
          pack.intersects(padRect(breakout, i)));
    }
  }

  /** The pads keep well clear of the holes at these figures; this is what says they still do. */
  @Test
  public void theHolesClearThePads() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    for (boolean bottom : new boolean[] {false, true}) {
      Rectangle2D hole = holeRect(breakout, bottom);
      for (int i = 0; i < breakout.getControlPointCount(); i++) {
        Assert.assertFalse("a mounting hole runs under pad " + i,
            hole.intersects(padRect(breakout, i)));
      }
    }
  }

  @Test
  public void everythingStaysOnTheBoard() {
    WS2812BBreakout breakout = new WS2812BBreakout();
    Rectangle2D board = breakout.getBodyShape().getBounds2D();

    for (boolean bottom : new boolean[] {false, true}) {
      Rectangle2D hole = holeRect(breakout, bottom);
      Assert.assertTrue("a mounting hole breaches the board edge",
          board.contains(hole.getX(), hole.getY(), hole.getWidth(), hole.getHeight()));
    }
    Rectangle2D pack = ledPackage(breakout);
    Assert.assertTrue("the package hangs off the board",
        board.contains(pack.getX(), pack.getY(), pack.getWidth(), pack.getHeight()));
    for (int i = 0; i < breakout.getControlPointCount(); i++) {
      Rectangle2D pad = padRect(breakout, i);
      Assert.assertTrue("pad " + i + " runs off the board",
          board.contains(pad.getX(), pad.getY(), pad.getWidth(), pad.getHeight()));
    }
  }

  @Test
  public void drawsCleanlyInEveryState() {
    MakerBoardTestSupport.assertDrawsCleanly(new WS2812BBreakout());
  }

  @Test
  public void honoursTheLedPackage() {
    MakerBoardTestSupport.assertAddressableLedTypes(WS2812BBreakout::new);
  }
}
