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
import org.diylc.components.maker.MakerBoardTestSupport;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the Jewel. The arrangement is the part worth pinning down rather than the
 * dimensions: five pads share six gaps between the ring LEDs, so one gap is empty, and getting the
 * wrong one empty would still place five pads on the right circle and pass any count or radius
 * check. The pad-against-LED collision tests are here for the same reason they are on the strip --
 * the numbers can all be right while the drawing has a pad under a package.
 */
public class WS2812BJewelTest {

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

  private static double mmToPx(double value) {
    return new Size(value, SizeUnit.mm).convertToPixels();
  }

  private static Point2D centerOf(WS2812BJewel jewel) {
    Rectangle2D bounds = jewel.getBodyShape().getBounds2D();
    return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
  }

  /** The ring LED centres, recomputed from the constants the way {@code draw} places them. */
  private static Point2D[] ringLeds(WS2812BJewel jewel) {
    Point2D center = centerOf(jewel);
    double ledR = WS2812BJewel.LED_CIRCLE_DIAMETER.convertToPixels() / 2.0;
    Point2D[] leds = new Point2D[WS2812BJewel.RING_LED_COUNT];
    for (int i = 0; i < leds.length; i++) {
      double angle = 2 * Math.PI * i / WS2812BJewel.RING_LED_COUNT - Math.PI / 2.0;
      leds[i] = new Point2D.Double(center.getX() + ledR * Math.cos(angle),
          center.getY() + ledR * Math.sin(angle));
    }
    return leds;
  }

  private static Rectangle2D ledRect(Point2D led) {
    double half = WS2812BJewel.RGB_LED_SIZE.convertToPixels() / 2.0;
    double size = half * 2;
    return new Rectangle2D.Double(led.getX() - half, led.getY() - half, size, size);
  }

  /** The shared round pad's bounding box; pads are not turned to face the centre. */
  private static Rectangle2D padRect(Point2D pad) {
    double size = WS2812BJewel.PAD_SIZE.convertToPixels();
    return new Rectangle2D.Double(pad.getX() - size / 2.0, pad.getY() - size / 2.0, size, size);
  }

  @Test
  public void fivePadsAreTheControlPoints() {
    Assert.assertEquals(5, new WS2812BJewel().getControlPointCount());
  }

  @Test
  public void padNamesAreTheClockwiseOrderFromOut() {
    WS2812BJewel jewel = new WS2812BJewel();
    String[] expected = new String[] {"OUT", "G_1", "G_2", "PWR", "IN"};
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("pad " + i, expected[i], jewel.getControlPointNodeName(i));
    }
  }

  /** The two grounds are distinct nodes; the silk would read both as GND. */
  @Test
  public void theTwoGroundsAreSeparateNodes() {
    WS2812BJewel jewel = new WS2812BJewel();
    Assert.assertNotEquals(jewel.getControlPointNodeName(1), jewel.getControlPointNodeName(2));
  }

  @Test
  public void bodyIsATwentyThreeMillimetreDisc() {
    Rectangle2D bounds = new WS2812BJewel().getBodyShape().getBounds2D();
    Assert.assertEquals("diameter across", 23.0d, mm(bounds.getWidth()), 0.01d);
    Assert.assertEquals("a disc, not an ellipse", bounds.getWidth(), bounds.getHeight(), 0.01d);
  }

  /**
   * Every pad the same distance from the centre of the body is what ties {@code getPadRadius} to
   * {@code getCenter}: if those two disagreed the disc would be drawn off its own control points,
   * which is the failure the ring's comment warns about.
   */
  @Test
  public void everyPadSitsOnTheNineMillimetreCircle() {
    WS2812BJewel jewel = new WS2812BJewel();
    Point2D center = centerOf(jewel);
    for (int i = 0; i < jewel.getControlPointCount(); i++) {
      Assert.assertEquals("pad " + i + " off the pad circle", 4.5d,
          mm(center.distance(jewel.getControlPoint(i))), 0.01d);
    }
  }

  /**
   * The arrangement test. Pads belong in the gaps between consecutive ring LEDs, and with five pads
   * in six gaps the one left empty must be the one due left, where the left mounting hole is.
   */
  @Test
  public void padsFillEveryGapButTheOneDueLeft() {
    WS2812BJewel jewel = new WS2812BJewel();
    Point2D center = centerOf(jewel);
    boolean[] occupied = new boolean[WS2812BJewel.RING_LED_COUNT];

    for (int i = 0; i < jewel.getControlPointCount(); i++) {
      Point2D pad = jewel.getControlPoint(i);
      double angle = Math.atan2(pad.getY() - center.getY(), pad.getX() - center.getX());
      // gap g is centred half a step past LED g, LED 0 being at the top
      double steps = (angle + Math.PI / 2.0) / (2 * Math.PI / WS2812BJewel.RING_LED_COUNT) - 0.5;
      long whole = Math.round(steps);
      Assert.assertEquals("pad " + i + " does not sit in the middle of a gap", whole, steps,
          0.001d);
      int gap = (int) (((whole % WS2812BJewel.RING_LED_COUNT) + WS2812BJewel.RING_LED_COUNT)
          % WS2812BJewel.RING_LED_COUNT);
      Assert.assertFalse("two pads in gap " + gap, occupied[gap]);
      occupied[gap] = true;
    }

    // gap 4 spans LED 4 to LED 5, so on a hexagon anchored at the top it is the one pointing left
    for (int gap = 0; gap < occupied.length; gap++) {
      if (gap == 4) {
        Assert.assertFalse("the empty gap should be the one due left", occupied[gap]);
      } else {
        Assert.assertTrue("gap " + gap + " has no pad", occupied[gap]);
      }
    }
  }

  /** Confirms the empty gap really does point at a mounting hole and not somewhere arbitrary. */
  @Test
  public void theEmptyGapPointsAtAMountingHole() {
    WS2812BJewel jewel = new WS2812BJewel();
    Point2D center = centerOf(jewel);

    // the left hole sits at pi from the centre, and no pad may lie along that radius
    for (int i = 0; i < jewel.getControlPointCount(); i++) {
      Point2D pad = jewel.getControlPoint(i);
      double angle = Math.atan2(pad.getY() - center.getY(), pad.getX() - center.getX());
      Assert.assertTrue("pad " + i + " lies along the left mounting hole's radius",
          Math.PI - Math.abs(angle) > 0.1d);
    }
  }

  /** No pad may sit under a package, the centre LED included: it is nearest to the pads. */
  @Test
  public void noPadSitsUnderAnLed() {
    WS2812BJewel jewel = new WS2812BJewel();
    Point2D center = centerOf(jewel);
    Point2D[] leds = ringLeds(jewel);

    for (int i = 0; i < jewel.getControlPointCount(); i++) {
      Rectangle2D pad = padRect(jewel.getControlPoint(i));
      Assert.assertFalse("pad " + i + " sits under the centre LED",
          pad.intersects(ledRect(center)));
      for (int l = 0; l < leds.length; l++) {
        Assert.assertFalse("pad " + i + " sits under ring LED " + l,
            pad.intersects(ledRect(leds[l])));
      }
    }
  }

  @Test
  public void everyPadAndLedSitsOnTheDisc() {
    WS2812BJewel jewel = new WS2812BJewel();
    Shape disc = jewel.getBodyShape();
    Point2D center = centerOf(jewel);
    double outerR = WS2812BJewel.OUTER_DIAMETER.convertToPixels() / 2.0;

    for (int i = 0; i < jewel.getControlPointCount(); i++) {
      Assert.assertTrue("pad " + i + " hangs off the disc",
          disc.contains(padRect(jewel.getControlPoint(i))));
    }
    for (Point2D led : ringLeds(jewel)) {
      Assert.assertTrue("an LED package hangs off the rim",
          center.distance(led) + WS2812BJewel.RGB_LED_SIZE.convertToPixels() / 2.0 <= outerR);
    }
  }

  /**
   * The holes are 19 mm apart on a 23 mm disc, so each one clears the rim by only 0.5 mm. Note that
   * they also come within a fraction of a millimetre of the neighbouring LED packages; that is what
   * the measurements give and it is not an error, but it is why this asserts containment rather
   * than any comfortable clearance.
   */
  @Test
  public void mountingHolesSitInsideTheDisc() {
    WS2812BJewel jewel = new WS2812BJewel();
    Point2D center = centerOf(jewel);
    double holeOffset = WS2812BJewel.MOUNTING_HOLE_SPACING.convertToPixels() / 2.0;
    double holeR = WS2812BJewel.MOUNTING_HOLE_SIZE.convertToPixels() / 2.0;
    double outerR = WS2812BJewel.OUTER_DIAMETER.convertToPixels() / 2.0;

    Assert.assertEquals("holes 19 mm apart", 19.0d, mm(holeOffset * 2), 0.01d);
    Assert.assertTrue("mounting holes breach the rim", holeOffset + holeR <= outerR);
    Assert.assertTrue("a hole is not inside the disc",
        jewel.getBodyShape().contains(center.getX() - holeOffset - holeR, center.getY() - holeR,
            holeR * 2, holeR * 2));
  }

  /** The package it is fitted with is the only thing to tell a buyer about this one. */
  @Test
  public void bomValueNamesThePackage() {
    Assert.assertEquals("RGB", new WS2812BJewel().getValueForDisplay());
  }

  @Test
  public void honoursTheLedPackage() {
    MakerBoardTestSupport.assertAddressableLedTypes(WS2812BJewel::new);
  }

  @Test
  public void padsAreAllDistinctPositions() {
    WS2812BJewel jewel = new WS2812BJewel();
    for (int i = 0; i < jewel.getControlPointCount(); i++) {
      for (int j = i + 1; j < jewel.getControlPointCount(); j++) {
        Assert.assertTrue("pads " + i + " and " + j + " coincide",
            jewel.getControlPoint(i).distance(jewel.getControlPoint(j)) > mmToPx(0.5d));
      }
    }
  }
}
