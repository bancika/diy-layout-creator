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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the Nokia 5110 module. The part has no variants, so what is worth pinning
 * down is the one thing its layout does differently from the other display boards: the pin row is
 * on the bottom edge, which means the board grows upwards from control point zero and every
 * feature is measured down from an edge the control points do not touch.
 */
public class Nokia5110LCDTest {

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

  @Test
  public void boardIsTheModuleOutline() {
    Rectangle2D bounds = new Nokia5110LCD().getBodyShape().getBounds2D();
    Assert.assertEquals("width", 43.8d, mm(bounds.getWidth()), 0.01d);
    Assert.assertEquals("length", 45.8d, mm(bounds.getHeight()), 0.01d);
  }

  @Test
  public void controlPointsAreTheEightPinsInOrder() {
    Nokia5110LCD display = new Nokia5110LCD();
    String[] expected = new String[] {"RST", "CE", "DC", "DIN", "CLK", "VCC", "BL", "GND"};
    Assert.assertEquals(expected.length, display.getControlPointCount());
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pin " + i, expected[i], display.getControlPointNodeName(i));
    }
  }

  @Test
  public void pinsSitAtHeaderPitchAlongOneRow() {
    Nokia5110LCD display = new Nokia5110LCD();
    for (int i = 1; i < display.getControlPointCount(); i++) {
      Point2D previous = display.getControlPoint(i - 1);
      Point2D current = display.getControlPoint(i);
      Assert.assertEquals("pitch at pin " + i, 2.54d, mm(current.getX() - previous.getX()), 0.01d);
      Assert.assertEquals("pin " + i + " left the row", previous.getY(), current.getY(), 0.01d);
    }
  }

  /**
   * The row is centred across the board and stands clear of the bottom edge rather than of the top
   * one, which is the easiest thing to get backwards when the board is placed from its header.
   */
  @Test
  public void headerIsCentredOnTheBottomEdge() {
    Nokia5110LCD display = new Nokia5110LCD();
    Rectangle2D board = display.getBodyShape().getBounds2D();
    Point2D first = display.getControlPoint(0);
    Point2D last = display.getControlPoint(display.getControlPointCount() - 1);

    Assert.assertEquals("left margin", board.getMaxX() - last.getX(), first.getX() - board.getX(),
        0.01d);
    Assert.assertEquals("clearance from the bottom edge", 3.5d, mm(board.getMaxY() - first.getY()),
        0.01d);
    Assert.assertTrue("the row sits below the glass", first.getY() > board.getCenterY());
  }

  /**
   * The glass is placed from the top edge of the board rather than centred in the frame, so
   * nothing keeps the two in step on its own: both offsets have to be checked against each other.
   */
  @Test
  public void glassSitsInsideTheBezel() {
    double bezelTop = Nokia5110LCD.BEZEL_TOP_OFFSET.convertToPixels();
    double bezelBottom = bezelTop + Nokia5110LCD.BEZEL_LENGTH.convertToPixels();
    double glassTop = Nokia5110LCD.DISPLAY_TOP_OFFSET.convertToPixels();
    double glassBottom = glassTop + Nokia5110LCD.DISPLAY_LENGTH.convertToPixels();

    Assert.assertTrue("glass reaches above the bezel", glassTop >= bezelTop);
    Assert.assertTrue("glass reaches below the bezel", glassBottom <= bezelBottom);
    Assert.assertTrue("glass is wider than the bezel", Nokia5110LCD.DISPLAY_WIDTH
        .convertToPixels() <= Nokia5110LCD.BEZEL_WIDTH.convertToPixels());
    Assert.assertTrue("bezel runs off the bottom of the board",
        bezelBottom <= Nokia5110LCD.BOARD_LENGTH.convertToPixels());
  }

  /**
   * The hole pattern is 34.5 x 41 mm on a 43.8 x 45.8 mm board, so every drill sits 2.4 mm or more
   * from the two edges nearest it and a 3 mm hole clears both. This is what catches the outline
   * being entered the wrong way round: with the two figures swapped the vertical pair is 1.4 mm
   * from the edge and the hole breaches it.
   */
  @Test
  public void mountingHolesClearTheBoardEdges() {
    double insetX = (Nokia5110LCD.BOARD_WIDTH.convertToPixels()
        - Nokia5110LCD.MOUNTING_HOLE_SPACING_X.convertToPixels()) / 2.0;
    double insetY = (Nokia5110LCD.BOARD_LENGTH.convertToPixels()
        - Nokia5110LCD.MOUNTING_HOLE_SPACING_Y.convertToPixels()) / 2.0;
    double radius = Nokia5110LCD.MOUNTING_HOLE_SIZE.convertToPixels() / 2.0;

    Assert.assertTrue("holes breach the side edges", insetX >= radius);
    Assert.assertTrue("holes breach the top and bottom edges", insetY >= radius);
  }

  /**
   * The silkscreen stands in the strip between the pin row and the bottom edge, which is the one
   * place on this board where a label can run out of room: the strip is only as deep as the
   * header's clearance from the edge.
   */
  @Test
  public void silkLabelsStayInTheStripBelowThePins() {
    Assert.assertTrue("the labels sit past the bottom edge",
        AbstractMakerBoard.PIN_ROW_FLAT_LABEL_OFFSET.convertToPixels() < Nokia5110LCD.HEADER_OFFSET
            .convertToPixels());
    Assert.assertTrue("the labels sit on top of the pins",
        AbstractMakerBoard.PIN_ROW_FLAT_LABEL_OFFSET.convertToPixels() > AbstractMakerBoard.PIN_SIZE
            .convertToPixels());
  }
}
