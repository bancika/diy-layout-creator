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
import org.diylc.components.displays.WS2812BStrip.Density;
import org.diylc.components.maker.MakerBoardTestSupport;
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
    return WS2812BStrip.PAD_WIDTH.convertToPixels() / 2.0d;
  }

  /**
   * The copper a cut leaves on the tape: half the pill, reaching inboard from the cut the control
   * point sits on. This is what has to stay on the tape and off the packages; the other half went
   * with the next length.
   */
  private static Rectangle2D padRect(WS2812BStrip strip, int index) {
    Point2D pad = strip.getControlPoint(index);
    double w = WS2812BStrip.PAD_WIDTH.convertToPixels();
    double depth = strip.getPadDepth();
    boolean input = index < 3;
    return new Rectangle2D.Double(input ? pad.getX() : pad.getX() - depth, pad.getY() - w / 2.0,
        depth, w);
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

  /**
   * Tape is a repeating cell one pitch long, so a length of it is exactly as many pitches as it
   * has LEDs -- the two half-cells a pair of cuts leave making one whole one between them. The
   * lead-in used to be a pad-derived constant, which drew the end LEDs 6 mm from the cuts on
   * 30/m tape where every other gap was 33 mm.
   */
  @Test
  public void aLengthOfTapeIsOnePitchPerLed() {
    for (Density density : Density.values()) {
      for (int count : new int[] {1, 8, 40}) {
        WS2812BStrip strip = new WS2812BStrip();
        strip.setDensity(density);
        strip.setLedCount(count);
        double pitch = new Size(density.getPitchMm(), SizeUnit.mm).convertToPixels();

        Assert.assertEquals(density + " x " + count, count * pitch,
            strip.getBodyShape().getBounds2D().getWidth(), 0.01d);
        Assert.assertEquals(density + " lead-in", pitch / 2.0, strip.getLeadIn(), 0.01d);
      }
    }
  }

  /**
   * The pad is a pill as long as the nominal allows and otherwise as long as the density leaves
   * room for, never shorter than its own width. At 144/m that bottoms out: the pill is no longer
   * than it is wide, which makes it a circle, which is what that tape carries.
   */
  @Test
  public void theDensestTapeCarriesCircularPads() {
    WS2812BStrip strip = new WS2812BStrip();
    double width = WS2812BStrip.PAD_WIDTH.convertToPixels();

    strip.setDensity(Density._30);
    Assert.assertEquals("30/m should get the nominal pill",
        WS2812BStrip.PAD_LENGTH.convertToPixels(), strip.getPadLength(), 0.01d);

    strip.setDensity(Density._144);
    Assert.assertTrue("144/m pad should be about as long as it is wide",
        Math.abs(strip.getPadLength() - width) < width * 0.1d);
    Assert.assertTrue("a pad may never be shorter than it is wide",
        strip.getPadLength() >= width - 0.01d);
  }

  /** Six pads whatever the count: the LEDs are drawn, not wired. */
  @Test
  public void controlPointCountIsFixedAtSix() {
    for (Density density : Density.values()) {
      for (int count : new int[] {1, 8, 60, 144}) {
        WS2812BStrip strip = new WS2812BStrip();
        strip.setDensity(density);
        strip.setLedCount(count);
        Assert.assertEquals("control points at " + density + " x " + count, 6,
            strip.getControlPointCount());
      }
    }
  }

  /**
   * Both cuts carry all three lines, which is the whole point of tape: a length of it is chained
   * to the next by soldering its output end to the next one's input. An end short of its supply
   * pads cannot be chained at all, and nothing in the drawing says so.
   */
  @Test
  public void bothCutsCarryAllThreeLines() {
    WS2812BStrip strip = new WS2812BStrip();

    Set<String> names = new HashSet<String>();
    for (int i = 0; i < strip.getControlPointCount(); i++) {
      Assert.assertTrue("duplicate node name " + strip.getControlPointNodeName(i),
          names.add(strip.getControlPointNodeName(i)));
    }
    Assert.assertEquals("DIN", strip.getControlPointNodeName(1));
    Assert.assertEquals("DOUT", strip.getControlPointNodeName(4));

    // the rails are continuous copper down the tape, so each sits in the same row at both ends
    for (int i = 0; i < 3; i++) {
      Assert.assertEquals("row " + i + " is not level across the tape",
          strip.getControlPoint(i).getY(), strip.getControlPoint(3 + i).getY(), 0.01d);
    }
  }

  /**
   * Tape is cut through the middle of a pad pair, so what is left at a cut is half a pad on the
   * very edge rather than a pad set in from it.
   */
  @Test
  public void padsAreFlushWithTheCuts() {
    for (Density density : Density.values()) {
      for (int count : new int[] {1, 8, 40}) {
        WS2812BStrip strip = new WS2812BStrip();
        strip.setDensity(density);
        strip.setLedCount(count);
        Rectangle2D tape = strip.getBodyShape().getBounds2D();

        for (int i = 0; i < 3; i++) {
          Assert.assertEquals(density + " x " + count + " input pad " + i, tape.getX(),
              strip.getControlPoint(i).getX(), 0.01d);
          Assert.assertEquals(density + " x " + count + " output pad " + i, tape.getMaxX(),
              strip.getControlPoint(3 + i).getX(), 0.01d);
        }
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
          Rectangle2D pad = padRect(strip, i);
          String where = density + " x " + count + " pad " + i;
          Assert.assertTrue(where + " runs off the left cut", pad.getX() >= tape.getX() - 0.5d);
          Assert.assertTrue(where + " runs off the right cut",
              pad.getMaxX() <= tape.getMaxX() + 0.5d);
          Assert.assertTrue(where + " runs off the top edge", pad.getY() >= tape.getY() - 0.5d);
          Assert.assertTrue(where + " runs off the bottom edge",
              pad.getMaxY() <= tape.getMaxY() + 0.5d);
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
        double lastLed = tape.getX() + strip.getLeadIn() + (count - 1) * pitch;
        for (int i = 0; i < 3; i++) {
          Assert.assertTrue(density + " x " + count + ": output pad " + i + " overlaps the last LED",
              padRect(strip, 3 + i).getX() >= lastLed + ledHalfWidth() - 0.5d);
        }
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
      double firstLed = tape.getX() + strip.getLeadIn();
      for (int i = 0; i < 3; i++) {
        Assert.assertTrue(density + ": input pad " + i + " overlaps the first LED",
            padRect(strip, i).getMaxX() <= firstLed - ledHalfWidth() + 0.5d);
      }
    }
  }

  private static double mmToPx(double value) {
    return new Size(value, SizeUnit.mm).convertToPixels();
  }

  @Test
  public void honoursTheLedPackage() {
    MakerBoardTestSupport.assertAddressableLedTypes(WS2812BStrip::new);
  }
}
