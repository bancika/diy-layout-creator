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

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the Nokia 5110 module. Its pin row is on the bottom edge, so the board grows
 * upwards from control point zero and every feature is measured down from an edge the control
 * points do not touch.
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

  private static Nokia5110LCD of(Color screenColor) {
    Nokia5110LCD lcd = new Nokia5110LCD();
    lcd.setScreenColor(screenColor);
    return lcd;
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

  /** Clear of the bottom edge rather than the top, which is the easy thing to get backwards. */
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
   * The glass is placed from the board's top edge rather than centred in the frame, so nothing
   * keeps the two in step on its own.
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
   * Catches the outline being entered the wrong way round: with the two figures swapped the
   * vertical pair sits 1.4 mm from the edge and a 3 mm hole breaches it.
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
   * The strip between the pin row and the bottom edge is the one place on this board where a label
   * can run out of room, being only as deep as the header's clearance from the edge.
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

  /** No variant property, but a resolution and a controller worth printing. */
  @Test
  public void theGlassPrintsTheResolutionAndController() {
    Nokia5110LCD printed = new Nokia5110LCD();
    Nokia5110LCD blank = new Nokia5110LCD();
    blank.setScreen(Display.NONE);

    Assert.assertEquals("84x48 PCD8544", printed.getValueForDisplay());
    Assert.assertEquals(Display.VALUE, printed.getScreen());
    MakerBoardTestSupport.assertScreenTextIsDrawn(printed, blank);
  }

  /**
   * The dark ink the glass normally carries disappears into the darker backlights, so two at
   * opposite ends of the range must each still show their text.
   */
  @Test
  public void theInkFollowsTheBacklight() {
    Nokia5110LCD dark = of(Color.decode("#1B3C73"));
    Nokia5110LCD pale = of(Color.decode("#6B7668"));

    Nokia5110LCD darkBlank = of(Color.decode("#1B3C73"));
    darkBlank.setScreen(Display.NONE);
    Nokia5110LCD paleBlank = of(Color.decode("#6B7668"));
    paleBlank.setScreen(Display.NONE);

    MakerBoardTestSupport.assertScreenTextIsDrawn(dark, darkBlank);
    MakerBoardTestSupport.assertScreenTextIsDrawn(pale, paleBlank);
  }

  /** A project saved before the backlight colour existed deserialises with none set. */
  @Test
  public void aMissingBacklightColourFallsBackToTheGlass() {
    Assert.assertEquals(Nokia5110LCD.LCD_COLOR, of(null).getScreenColor());
  }

  @Test
  public void drawsCleanlyInEveryState() {
    MakerBoardTestSupport.assertDrawsCleanly(new Nokia5110LCD());
  }
}
