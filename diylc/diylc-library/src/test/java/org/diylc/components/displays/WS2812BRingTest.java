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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.AbstractMakerBoard.LedType;
import org.diylc.components.displays.WS2812BRing.RingSize;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Basic checks for the NeoPixel ring. The pad arrangement itself is guarded by {@link RingSize}'s
 * constructor, which throws unless each ring's LED gaps sum to its LED count, so what is left to
 * assert here is that the three sizes carry the pads and diameters they were measured with and
 * that the body is drawn concentric with them.
 */
public class WS2812BRingTest {

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

  private static WS2812BRing of(RingSize ringSize) {
    WS2812BRing ring = new WS2812BRing();
    ring.setRingSize(ringSize);
    return ring;
  }

  @Test
  public void bodyIsTheOuterDiameterOfEachRing() {
    for (RingSize ringSize : RingSize.values()) {
      Rectangle2D bounds = of(ringSize).getBodyShape().getBounds2D();
      Assert.assertEquals(ringSize + " width", ringSize.getOuterDiameterMm(), mm(bounds.getWidth()),
          0.01d);
      Assert.assertEquals(ringSize + " height", ringSize.getOuterDiameterMm(),
          mm(bounds.getHeight()), 0.01d);
    }
  }

  /** Four pads on the 12-LED ring, six on the two larger ones, and no two sharing a name. */
  @Test
  public void padCountFollowsTheRingSize() {
    Assert.assertEquals(4, of(RingSize._12_LED).getControlPointCount());
    Assert.assertEquals(6, of(RingSize._16_LED).getControlPointCount());
    Assert.assertEquals(6, of(RingSize._24_LED).getControlPointCount());

    for (RingSize ringSize : RingSize.values()) {
      WS2812BRing ring = of(ringSize);
      Set<String> names = new HashSet<String>();
      for (int i = 0; i < ring.getControlPointCount(); i++) {
        String name = ring.getControlPointNodeName(i);
        Assert.assertNotNull(ringSize + " pad " + i, name);
        Assert.assertFalse(ringSize + " pad " + i + " is unnamed", name.isEmpty());
        Assert.assertTrue(ringSize + " duplicate node name " + name, names.add(name));
      }
    }
  }

  /**
   * Every pad sits one inset in from the rim, on a circle of its own rather than on the one the
   * LEDs occupy. This also says the body is drawn concentric with the pads: the outline is placed
   * by working backwards from the first pad, so a disagreement there would leave the ring drawn
   * away from its own control points and show up as an off-centre radius here.
   */
  @Test
  public void everyPadSitsOnItsOwnCircleInsideTheRim() {
    for (RingSize ringSize : RingSize.values()) {
      WS2812BRing ring = of(ringSize);
      Rectangle2D bounds = ring.getBodyShape().getBounds2D();
      Point2D centre = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
      double expected = ringSize.getOuterDiameterMm() / 2.0
          - mm(WS2812BRing.PAD_EDGE_INSET.convertToPixels());

      for (int i = 0; i < ring.getControlPointCount(); i++) {
        Assert.assertEquals(ringSize + " pad " + i + " radius", expected,
            mm(centre.distance(ring.getControlPoint(i))), 0.01d);
      }
    }
  }

  @Test
  public void ringSizeRoundTripsThroughTheSetter() {
    Assert.assertEquals(RingSize._16_LED, new WS2812BRing().getRingSize());
    for (RingSize ringSize : RingSize.values()) {
      WS2812BRing ring = of(ringSize);
      Assert.assertEquals(ringSize, ring.getRingSize());
      Assert.assertTrue(ring.getValueForDisplay().startsWith(ringSize.toString()));
    }
  }

  /** A ring saved before the property existed deserializes without one and has to read as RGB. */
  @Test
  public void ledTypeDefaultsToRgb() {
    Assert.assertEquals(LedType.RGB, new WS2812BRing().getLedType());
    WS2812BRing ring = new WS2812BRing();
    ring.setLedType(null);
    Assert.assertEquals(LedType.RGB, ring.getLedType());
  }

  /** Both halves name a different thing to buy, so the BOM's value column has to carry both. */
  @Test
  public void variantLabelCarriesSizeAndLedType() {
    for (RingSize ringSize : RingSize.values()) {
      for (LedType ledType : LedType.values()) {
        WS2812BRing ring = of(ringSize);
        ring.setLedType(ledType);
        Assert.assertEquals(ringSize + ", " + ledType, ring.getValueForDisplay());
      }
    }
  }

  /** The packages are pin-compatible, so fitting a different one moves no pad. */
  @Test
  public void ledTypeLeavesTheFootprintAlone() {
    for (RingSize ringSize : RingSize.values()) {
      WS2812BRing reference = of(ringSize);
      for (LedType ledType : LedType.values()) {
        WS2812BRing ring = of(ringSize);
        ring.setLedType(ledType);
        MakerBoardTestSupport.assertSharedFootprint(reference, ring, ring.getControlPointCount());
      }
    }
  }

  /**
   * RGBW shows the white die the RGB package does not carry, and WWA cannot emit the colour wheel
   * the other two are drawn lit with. Comparing renderings is what makes this a check on the
   * drawing: a draw() that ignored the property would pass the getter.
   */
  @Test
  public void eachPackageIsDrawnDifferently() {
    int[] rgb = renderWith(LedType.RGB);
    Assert.assertFalse("RGBW should show its white die, and does not",
        Arrays.equals(rgb, renderWith(LedType.RGBW_WARM)));
    Assert.assertFalse("A WWA ring should not be drawn lit in colour",
        Arrays.equals(rgb, renderWith(LedType.WWA)));
  }

  /** The three RGBW tints differ only in what you buy, so they are drawn the same. */
  @Test
  public void theRgbwTintsShareOneDrawing() {
    int[] warm = renderWith(LedType.RGBW_WARM);
    Assert.assertArrayEquals(warm, renderWith(LedType.RGBW_NATURAL));
    Assert.assertArrayEquals(warm, renderWith(LedType.RGBW_COOL));
  }

  private static int[] renderWith(LedType ledType) {
    WS2812BRing ring = of(RingSize._16_LED);
    ring.setLedType(ledType);
    return MakerBoardTestSupport.renderPixels(ring);
  }

  @Test
  public void drawsCleanlyInEveryState() {
    for (RingSize ringSize : RingSize.values()) {
      for (LedType ledType : LedType.values()) {
        WS2812BRing ring = of(ringSize);
        ring.setLedType(ledType);
        MakerBoardTestSupport.assertDrawsCleanly(ring);
      }
    }
  }
}
