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
package org.diylc.components.maker;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Supplier;

import org.diylc.common.Orientation;
import org.diylc.components.AbstractAddressableLedBoard;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.AbstractMakerBoard.LedType;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;

/**
 * Shared checks for the maker board tests.
 *
 * <p>What every board has in common -- naming its pins, keeping them apart, drawing, rotating --
 * is asserted for all of them in {@code MakerComponentsTest}, but only for the version each board
 * starts with. The per-board tests cover a board's own pinout and geometry.
 */
public class MakerBoardTestSupport {

  public static final double PIN_SPACING = new Size(0.1d, SizeUnit.in).convertToPixels();

  // large enough for the longest board in the package, with its origin in the middle
  private static final int RENDER_SIZE = 1400;

  private static final Project PROJECT = new Project();

  private static final IDrawingObserver OBSERVER = new IDrawingObserver() {

    @Override
    public void startTracking() {}

    @Override
    public void stopTracking() {}

    @Override
    public void startTrackingContinuityArea(boolean positive) {}

    @Override
    public void stopTrackingContinuityArea() {}

    @Override
    public boolean isTrackingContinuityArea() {
      return false;
    }

    @Override
    public void setContinuityMarker(String marker) {}
  };

  /** Asserts that pins {@code from} through {@code to} form a straight row on 0.1" pitch. */
  public static void assertRow(AbstractMakerBoard board, int from, int to) {
    assertRow(board, from, to, PIN_SPACING);
  }

  /**
   * Asserts that pins {@code from} through {@code to} form a straight row on the given pitch. The
   * axis comes from the first pair, so a row that bends part way along fails.
   */
  public static void assertRow(AbstractMakerBoard board, int from, int to, double pitch) {
    boolean vertical = Math.abs(
        board.getControlPoint(from).getX() - board.getControlPoint(from + 1).getX()) < 0.01;
    for (int i = from; i < to; i++) {
      Point2D p1 = board.getControlPoint(i);
      Point2D p2 = board.getControlPoint(i + 1);
      Assert.assertEquals("Pitch between pins " + i + " and " + (i + 1), pitch, p1.distance(p2),
          0.01);
      Assert.assertEquals("Pins " + i + " and " + (i + 1) + " should line up",
          vertical ? p1.getX() : p1.getY(), vertical ? p2.getX() : p2.getY(), 0.01);
    }
  }

  /** Asserts the distance between the first pins of two parallel rows. */
  public static void assertRowSpacing(AbstractMakerBoard board, int first, int second, Size spacing) {
    Point2D p1 = board.getControlPoint(first);
    Point2D p2 = board.getControlPoint(second);
    Assert.assertEquals("Row spacing", spacing.convertToPixels(), p1.distance(p2), 0.01);
    Assert.assertEquals("Rows should start level", p1.getY(), p2.getY(), 0.01);
  }

  /** Asserts the outline size, which is what a shield, a case or an enclosure has to fit. */
  public static void assertBoardSize(AbstractMakerBoard board, Size width, Size length) {
    Rectangle2D bounds = board.getBodyShape().getBounds2D();
    Assert.assertEquals("Board width", width.convertToPixels(), bounds.getWidth(), 0.1);
    Assert.assertEquals("Board length", length.convertToPixels(), bounds.getHeight(), 0.1);
  }

  /**
   * Asserts that two versions of a board share an outline and put their first {@code sharedPins}
   * control points in the same place. A revision that keeps its footprint has to keep its pin
   * positions too, or switching the version moves every connection in an existing file.
   */
  public static void assertSharedFootprint(AbstractMakerBoard reference, AbstractMakerBoard variant,
      int sharedPins) {
    Assert.assertEquals("Outline", reference.getBodyShape().getBounds2D(),
        variant.getBodyShape().getBounds2D());
    for (int i = 0; i < sharedPins; i++) {
      Assert.assertEquals("Pin " + i, reference.getControlPoint(i), variant.getControlPoint(i));
    }
  }

  /**
   * Renders the board alone at a fixed position. Two renderings differing in one property can then
   * be compared, which is the only way to catch a drawing change that moves no geometry.
   */
  public static int[] renderPixels(AbstractMakerBoard board) {
    BufferedImage image = new BufferedImage(RENDER_SIZE, RENDER_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    board.setControlPoint(new Point2D.Double(RENDER_SIZE / 2.0, RENDER_SIZE / 2.0), 0);
    try {
      board.draw(g2d, ComponentState.NORMAL, false, PROJECT, OBSERVER);
    } finally {
      g2d.dispose();
    }
    return image.getRGB(0, 0, RENDER_SIZE, RENDER_SIZE, null, 0, RENDER_SIZE);
  }

  /**
   * Asserts that a board prints its description on its lit area and that turning the property off
   * takes it away again. A screen that silently refused to print would pass the getter.
   */
  public static void assertScreenTextIsDrawn(AbstractMakerBoard withText,
      AbstractMakerBoard withoutText) {
    Assert.assertFalse("The lit area should carry the description, and does not",
        Arrays.equals(renderPixels(withText), renderPixels(withoutText)));
  }

  /**
   * Asserts that a board built out of addressable LEDs honours the package it is fitted with: a
   * board saved before the property existed reads as RGB, the packages are pin-compatible so
   * fitting one in place of another moves no pad, RGBW is drawn with the white die that is the
   * only thing telling it from RGB on sight, and WWA is drawn off the warm white palette rather
   * than the colour wheel it cannot emit.
   *
   * @param factory Builds a fresh board, since each assertion needs one of its own
   */
  public static void assertAddressableLedTypes(Supplier<AbstractAddressableLedBoard> factory) {
    Assert.assertEquals("A board with no stored package should read as RGB", LedType.RGB,
        factory.get().getLedType());
    AbstractAddressableLedBoard stale = factory.get();
    stale.setLedType(null);
    Assert.assertEquals("A board saved before the property existed should read as RGB",
        LedType.RGB, stale.getLedType());

    AbstractAddressableLedBoard reference = factory.get();
    for (LedType ledType : LedType.values()) {
      AbstractAddressableLedBoard board = factory.get();
      board.setLedType(ledType);
      assertSharedFootprint(reference, board, reference.getControlPointCount());
      Assert.assertTrue(ledType + " should be named in the BOM's value column",
          board.getValueForDisplay().contains(ledType.toString()));
    }

    int[] rgb = renderWithLedType(factory, LedType.RGB);
    Assert.assertFalse("RGBW should show the white die RGB does not carry",
        Arrays.equals(rgb, renderWithLedType(factory, LedType.RGBW_WARM)));
    Assert.assertFalse("WWA should not be drawn lit in colour",
        Arrays.equals(rgb, renderWithLedType(factory, LedType.WWA)));

    // the three RGBW tints name different parts to buy but the same thing to draw
    int[] warm = renderWithLedType(factory, LedType.RGBW_WARM);
    Assert.assertArrayEquals(warm, renderWithLedType(factory, LedType.RGBW_NATURAL));
    Assert.assertArrayEquals(warm, renderWithLedType(factory, LedType.RGBW_COOL));
  }

  private static int[] renderWithLedType(Supplier<AbstractAddressableLedBoard> factory,
      LedType ledType) {
    AbstractAddressableLedBoard board = factory.get();
    board.setLedType(ledType);
    return renderPixels(board);
  }

  /** Draws the board in every state and orientation; anything that throws fails the test. */
  public static void assertDrawsCleanly(AbstractMakerBoard board) {
    BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    Orientation original = board.getOrientation();
    board.setControlPoint(new Point2D.Double(300, 300), 0);
    try {
      for (Orientation orientation : Orientation.values()) {
        board.setOrientation(orientation);
        board.draw(g2d, ComponentState.NORMAL, false, PROJECT, OBSERVER);
        board.draw(g2d, ComponentState.SELECTED, false, PROJECT, OBSERVER);
        board.draw(g2d, ComponentState.NORMAL, true, PROJECT, OBSERVER);
      }
      board.drawIcon(g2d, 32, 32);
    } finally {
      board.setOrientation(original);
      g2d.dispose();
    }
  }
}
