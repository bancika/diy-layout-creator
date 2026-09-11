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
package org.diylc.components.micro;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.diylc.common.Orientation;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;

/**
 * Shared checks for the maker board tests.
 *
 * <p>What every board has in common -- naming all of its pins, keeping them apart, drawing in each
 * state, rotating -- is asserted once for all of them in {@code MakerComponentsTest}, but only for
 * the version each board starts with. The tests in this package cover what is specific to a board:
 * its pinout, its geometry and the way those change from one version to the next.
 */
class MakerBoardTestSupport {

  static final double PIN_SPACING = new Size(0.1d, SizeUnit.in).convertToPixels();

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
  static void assertRow(AbstractMakerBoard board, int from, int to) {
    assertRow(board, from, to, PIN_SPACING);
  }

  /**
   * Asserts that pins {@code from} through {@code to} form a straight row on the given pitch. The
   * axis comes from the first pair, so a row that bends part way along fails.
   */
  static void assertRow(AbstractMakerBoard board, int from, int to, double pitch) {
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
  static void assertRowSpacing(AbstractMakerBoard board, int first, int second, Size spacing) {
    Point2D p1 = board.getControlPoint(first);
    Point2D p2 = board.getControlPoint(second);
    Assert.assertEquals("Row spacing", spacing.convertToPixels(), p1.distance(p2), 0.01);
    Assert.assertEquals("Rows should start level", p1.getY(), p2.getY(), 0.01);
  }

  /** Asserts the outline size, which is what a shield, a case or an enclosure has to fit. */
  static void assertBoardSize(AbstractMakerBoard board, Size width, Size length) {
    Rectangle2D bounds = board.getBodyShape().getBounds2D();
    Assert.assertEquals("Board width", width.convertToPixels(), bounds.getWidth(), 0.1);
    Assert.assertEquals("Board length", length.convertToPixels(), bounds.getHeight(), 0.1);
  }

  /**
   * Asserts that two versions of a board share an outline and put their first {@code sharedPins}
   * control points in the same place. A revision that keeps its footprint has to keep its pin
   * positions too, or switching the version moves every connection in an existing file.
   */
  static void assertSharedFootprint(AbstractMakerBoard reference, AbstractMakerBoard variant,
      int sharedPins) {
    Assert.assertEquals("Outline", reference.getBodyShape().getBounds2D(),
        variant.getBodyShape().getBounds2D());
    for (int i = 0; i < sharedPins; i++) {
      Assert.assertEquals("Pin " + i, reference.getControlPoint(i), variant.getControlPoint(i));
    }
  }

  /** Draws the board in every state and orientation; anything that throws fails the test. */
  static void assertDrawsCleanly(AbstractMakerBoard board) {
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
