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
 * Geometry tests for the NeoPixel stick. Everything on this board competes for 10.22 mm of height:
 * the mounting holes take the top of it, the packages take what is left, and the pads run the
 * whole of it at each end. None of the three can be moved without crowding another, and nothing
 * but a render shows it, so the clearances are asserted here.
 */
public class WS2812BStickTest {

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  private static Rectangle2D ledPackage(WS2812BStick stick, int index) {
    Point2D led = stick.getLedCentre(index);
    double half = AbstractMakerBoard.RGB_LED_SIZE.convertToPixels() / 2.0;
    return new Rectangle2D.Double(led.getX() - half, led.getY() - half, half * 2, half * 2);
  }

  private static Rectangle2D padRect(WS2812BStick stick, int index) {
    Point2D pad = stick.getControlPoint(index);
    double w = WS2812BStick.PAD_WIDTH.convertToPixels();
    double h = WS2812BStick.PAD_LENGTH.convertToPixels();
    return new Rectangle2D.Double(pad.getX() - w / 2.0, pad.getY() - h / 2.0, w, h);
  }

  private static Rectangle2D holeRect(WS2812BStick stick, boolean right) {
    Point2D hole = stick.getHoleCentre(right);
    double r = WS2812BStick.MOUNTING_HOLE_SIZE.convertToPixels() / 2.0;
    return new Rectangle2D.Double(hole.getX() - r, hole.getY() - r, r * 2, r * 2);
  }

  @Test
  public void boardIsTheMeasuredSize() {
    MakerBoardTestSupport.assertBoardSize(new WS2812BStick(), WS2812BStick.BOARD_WIDTH,
        WS2812BStick.BOARD_HEIGHT);
  }

  /**
   * Four pads at each end, and no two sharing a name: both grounds on an end are the same net on
   * the board, which is what made this easy to get wrong.
   */
  @Test
  public void eachEndCarriesFourDistinctlyNamedPads() {
    WS2812BStick stick = new WS2812BStick();
    Assert.assertEquals(8, stick.getControlPointCount());

    Set<String> names = new HashSet<String>();
    for (int i = 0; i < stick.getControlPointCount(); i++) {
      Assert.assertTrue("duplicate node name " + stick.getControlPointNodeName(i),
          names.add(stick.getControlPointNodeName(i)));
    }
    Assert.assertEquals("DIN", stick.getControlPointNodeName(1));
    Assert.assertEquals("DOUT", stick.getControlPointNodeName(5));
  }

  /**
   * The pads are surface copper flush with the cut ends rather than plated holes set in from
   * them, so each column's outer edge lands exactly on the board edge: half a pad out and it
   * overhangs, half a pad in and there is a bare strip the real board does not have.
   */
  @Test
  public void padsAreFlushWithTheCutEnds() {
    WS2812BStick stick = new WS2812BStick();
    Rectangle2D board = stick.getBodyShape().getBounds2D();

    for (int i = 0; i < 4; i++) {
      Assert.assertEquals("input pad " + i, board.getX(), padRect(stick, i).getX(), 0.01d);
      Assert.assertEquals("output pad " + i, board.getMaxX(), padRect(stick, 4 + i).getMaxX(),
          0.01d);
    }
    for (int i = 0; i < stick.getControlPointCount(); i++) {
      Rectangle2D pad = padRect(stick, i);
      Assert.assertTrue("pad " + i + " runs off the board",
          board.contains(pad.getX(), pad.getY(), pad.getWidth(), pad.getHeight()));
    }
  }

  /** A 1.5 mm pad on a 0.1 inch pitch leaves about a millimetre; any taller and they merge. */
  @Test
  public void padsInAColumnDoNotTouch() {
    WS2812BStick stick = new WS2812BStick();
    for (int i = 0; i < 3; i++) {
      Assert.assertFalse("input pads " + i + " and " + (i + 1) + " overlap",
          padRect(stick, i).intersects(padRect(stick, i + 1)));
      Assert.assertFalse("output pads " + i + " and " + (i + 1) + " overlap",
          padRect(stick, 4 + i).intersects(padRect(stick, 5 + i)));
    }
  }

  @Test
  public void mountingHolesAreASpanApartAstrideTheCentre() {
    WS2812BStick stick = new WS2812BStick();
    Rectangle2D board = stick.getBodyShape().getBounds2D();
    Point2D left = stick.getHoleCentre(false);
    Point2D right = stick.getHoleCentre(true);

    Assert.assertEquals("span", WS2812BStick.MOUNTING_HOLE_SPACING.convertToPixels(),
        right.getX() - left.getX(), 0.01d);
    Assert.assertEquals("not centred", board.getCenterX(), (left.getX() + right.getX()) / 2.0,
        0.01d);
    Assert.assertEquals("depth from the top edge",
        WS2812BStick.MOUNTING_HOLE_TOP_OFFSET.convertToPixels(), left.getY() - board.getY(),
        0.01d);
  }

  /**
   * The holes and the packages are the pair that cannot both have the middle of the board. The
   * packages were moved down to make room, and this is what says they went far enough.
   */
  @Test
  public void packagesClearTheMountingHoles() {
    WS2812BStick stick = new WS2812BStick();

    for (int i = 0; i < 8; i++) {
      Rectangle2D pack = ledPackage(stick, i);
      Assert.assertFalse("package " + i + " sits over the left hole",
          pack.intersects(holeRect(stick, false)));
      Assert.assertFalse("package " + i + " sits over the right hole",
          pack.intersects(holeRect(stick, true)));
    }
  }

  @Test
  public void packagesAndHolesStayOnTheBoard() {
    WS2812BStick stick = new WS2812BStick();
    Rectangle2D board = stick.getBodyShape().getBounds2D();

    for (boolean right : new boolean[] {false, true}) {
      Rectangle2D hole = holeRect(stick, right);
      Assert.assertTrue("a mounting hole breaches the board edge",
          board.contains(hole.getX(), hole.getY(), hole.getWidth(), hole.getHeight()));
    }
    for (int i = 0; i < 8; i++) {
      Rectangle2D pack = ledPackage(stick, i);
      Assert.assertTrue("package " + i + " hangs off the board",
          board.contains(pack.getX(), pack.getY(), pack.getWidth(), pack.getHeight()));
    }
  }

  /** The pads are drawn after the packages, so an overlap would be hidden under copper. */
  @Test
  public void packagesClearThePads() {
    WS2812BStick stick = new WS2812BStick();

    for (int i = 0; i < 8; i++) {
      Rectangle2D pack = ledPackage(stick, i);
      for (int pad = 0; pad < stick.getControlPointCount(); pad++) {
        Assert.assertFalse("package " + i + " runs under pad " + pad,
            pack.intersects(padRect(stick, pad)));
      }
    }
  }

  @Test
  public void drawsCleanlyInEveryState() {
    MakerBoardTestSupport.assertDrawsCleanly(new WS2812BStick());
  }
}
