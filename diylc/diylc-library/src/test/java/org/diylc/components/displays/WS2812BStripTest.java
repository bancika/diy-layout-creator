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
import org.diylc.components.displays.WS2812BStrip.Density;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the strip, written after three separate attempts at the output pad's position
 * each passed every numeric check that existed while the drawing was still wrong. The assertions
 * here are the ones that would have failed: that no pad hangs off the tape, and that no pad sits
 * under an LED package.
 */
public class WS2812BStripTest {

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

  private static double padHalfWidth() {
    return new Size(0.11d, SizeUnit.in).convertToPixels() / 2.0d;
  }

  private static double ledHalfWidth() {
    return new Size(5.0d, SizeUnit.mm).convertToPixels() / 2.0d;
  }

  @Test
  public void pitchFollowsFromDensity() {
    Assert.assertEquals(1000.0d / 30.0d, Density._30.getPitchMm(), 0.01d);
    Assert.assertEquals(1000.0d / 60.0d, Density._60.getPitchMm(), 0.01d);
    Assert.assertEquals(1000.0d / 144.0d, Density._144.getPitchMm(), 0.01d);
  }

  @Test
  public void tapeWidthFollowsFromDensity() {
    for (Density density : Density.values()) {
      WS2812BStrip strip = new WS2812BStrip();
      strip.setDensity(density);
      Assert.assertEquals("tape width for " + density, density.getWidthMm(),
          mm(strip.getBodyShape().getBounds2D().getHeight()), 0.01d);
    }
  }

  /** The count drives the length, and the two lead-ins do not scale, so it is linear not doubled. */
  @Test
  public void lengthIsLinearInLedCount() {
    WS2812BStrip strip = new WS2812BStrip();
    strip.setDensity(Density._60);
    strip.setLedCount(8);
    double eight = strip.getBodyShape().getBounds2D().getWidth();
    strip.setLedCount(16);
    double sixteen = strip.getBodyShape().getBounds2D().getWidth();
    double pitch = new Size(Density._60.getPitchMm(), SizeUnit.mm).convertToPixels();
    Assert.assertEquals("eight more LEDs adds eight pitches", 8 * pitch, sixteen - eight, 0.5d);
  }

  /** Four pads whatever the count: the LEDs are drawn, not wired. */
  @Test
  public void controlPointCountIsFixedAtFour() {
    for (Density density : Density.values()) {
      for (int count : new int[] {1, 8, 60, 144}) {
        WS2812BStrip strip = new WS2812BStrip();
        strip.setDensity(density);
        strip.setLedCount(count);
        Assert.assertEquals("control points at " + density + " x " + count, 4,
            strip.getControlPointCount());
      }
    }
  }

  @Test
  public void ledCountIsClamped() {
    WS2812BStrip strip = new WS2812BStrip();
    strip.setLedCount(100000);
    Assert.assertEquals(WS2812BStrip.MAX_LED_COUNT, strip.getLedCount());
    strip.setLedCount(0);
    Assert.assertEquals(WS2812BStrip.MIN_LED_COUNT, strip.getLedCount());
    strip.setLedCount(-5);
    Assert.assertEquals(WS2812BStrip.MIN_LED_COUNT, strip.getLedCount());
  }

  /** Every pad must lie within the tape. The output pad sat exactly on the far edge once. */
  @Test
  public void everyPadSitsOnTheTape() {
    for (Density density : Density.values()) {
      for (int count : new int[] {1, 8, 40}) {
        WS2812BStrip strip = new WS2812BStrip();
        strip.setDensity(density);
        strip.setLedCount(count);
        Rectangle2D tape = strip.getBodyShape().getBounds2D();
        for (int i = 0; i < strip.getControlPointCount(); i++) {
          Point2D pad = strip.getControlPoint(i);
          String where = density + " x " + count + " pad " + i;
          Assert.assertTrue(where + " runs off the left cut",
              pad.getX() - padHalfWidth() >= tape.getX() - 0.5d);
          Assert.assertTrue(where + " runs off the right cut",
              pad.getX() + padHalfWidth() <= tape.getMaxX() + 0.5d);
          Assert.assertTrue(where + " runs off the top edge",
              pad.getY() >= tape.getY() - 0.5d);
          Assert.assertTrue(where + " runs off the bottom edge",
              pad.getY() <= tape.getMaxY() + 0.5d);
        }
      }
    }
  }

  /**
   * The output pad must clear the last LED package. This is the one that kept regressing: at 144/m
   * the pitch is 6.94 mm against a 5 mm package, so there is very little room to be wrong in.
   */
  @Test
  public void outputPadClearsTheLastLed() {
    for (Density density : Density.values()) {
      for (int count : new int[] {1, 8, 40}) {
        WS2812BStrip strip = new WS2812BStrip();
        strip.setDensity(density);
        strip.setLedCount(count);
        Rectangle2D tape = strip.getBodyShape().getBounds2D();
        double pitch = new Size(density.getPitchMm(), SizeUnit.mm).convertToPixels();
        double leadIn = mmToPx(2.5d) + padHalfWidth() + ledHalfWidth() + mmToPx(0.5d);
        double lastLed = tape.getX() + leadIn + (count - 1) * pitch;
        double padLeadingEdge = strip.getControlPoint(3).getX() - padHalfWidth();
        Assert.assertTrue(density + " x " + count + ": output pad overlaps the last LED",
            padLeadingEdge >= lastLed + ledHalfWidth() - 0.5d);
      }
    }
  }

  /** And the input pads must clear the first one, which is the same failure mirrored. */
  @Test
  public void inputPadsClearTheFirstLed() {
    for (Density density : Density.values()) {
      WS2812BStrip strip = new WS2812BStrip();
      strip.setDensity(density);
      strip.setLedCount(8);
      Rectangle2D tape = strip.getBodyShape().getBounds2D();
      double leadIn = mmToPx(2.5d) + padHalfWidth() + ledHalfWidth() + mmToPx(0.5d);
      double firstLed = tape.getX() + leadIn;
      for (int i = 0; i < 3; i++) {
        double padTrailingEdge = strip.getControlPoint(i).getX() + padHalfWidth();
        Assert.assertTrue(density + ": input pad " + i + " overlaps the first LED",
            padTrailingEdge <= firstLed - ledHalfWidth() + 0.5d);
      }
    }
  }

  private static double mmToPx(double value) {
    return new Size(value, SizeUnit.mm).convertToPixels();
  }
}
