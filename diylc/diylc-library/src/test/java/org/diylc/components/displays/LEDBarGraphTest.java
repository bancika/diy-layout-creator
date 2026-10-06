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
import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.displays.LEDBarGraph.BarColor;
import org.diylc.components.displays.LEDBarGraph.Segments;
import org.diylc.components.transform.LEDBarGraphTransformer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry and transform tests for the bar graph. It is deliberately not in
 * {@code MakerComponentsTest}: that suite covers maker boards, and this is a DIP-outline part on a
 * different base class. Its assertions are also weaker than they look -- it positions components
 * with {@code setControlPoint(.., 0)}, which moves one pin and leaves the rest, and then only
 * checks that drawing did not throw. Nothing there would notice a component whose body and pins
 * had come apart.
 *
 * <p>An earlier version of this file asserted that the body sat <em>between</em> the pin rows,
 * which was the DIP IC arrangement the class had been copied from rather than the part's own. The
 * test passed and the component was wrong. {@link #bodyCoversEveryPin()} is the corrected form.
 */
public class LEDBarGraphTest {

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  /** Each segment has its own anode and cathode, so the package grows with the count. */
  @Test
  public void pinCountIsTwicePerSegment() {
    Assert.assertEquals(16, bar(Segments._8).getControlPointCount());
    Assert.assertEquals(20, bar(Segments._10).getControlPointCount());
    Assert.assertEquals(24, bar(Segments._12).getControlPointCount());
  }

  /** One pin pitch of package per segment, so the body lengthens as segments are added. */
  @Test
  public void packageLengthensWithSegmentCount() {
    double pitch = LEDBarGraph.PIN_SPACING.convertToPixels();
    for (Segments segments : Segments.values()) {
      Rectangle2D body = bar(segments).getBody().getBounds2D();
      Assert.assertEquals("length for " + segments, segments.getCount() * pitch, body.getHeight(),
          0.5d);
    }
  }

  /** The width is the package's own and does not vary with the count. */
  @Test
  public void packageWidthIsTheSameForEveryCount() {
    double expected = LEDBarGraph.PACKAGE_WIDTH.convertToPixels();
    for (Segments segments : Segments.values()) {
      Assert.assertEquals("width for " + segments, expected,
          bar(segments).getBody().getBounds2D().getWidth(), 0.5d);
    }
  }

  @Test
  public void pinsFormTwoRowsAtTheRightPitch() {
    LEDBarGraph graph = bar(Segments._10);
    double pitch = LEDBarGraph.PIN_SPACING.convertToPixels();
    double rowSpacing = LEDBarGraph.ROW_SPACING.convertToPixels();
    Point2D first = graph.getControlPoint(0);

    Assert.assertEquals("pitch along the near row", pitch,
        graph.getControlPoint(1).getY() - first.getY(), 0.5d);
    Assert.assertEquals("near row stays on one line", first.getX(),
        graph.getControlPoint(9).getX(), 0.5d);
    Assert.assertEquals("row spacing", rowSpacing,
        graph.getControlPoint(10).getX() - first.getX(), 0.5d);
  }

  /**
   * The defining difference from a DIP IC: its leads leave the sides of the plastic, so its body
   * sits between the rows. A bar graph's pins leave the underside, so the package is wider than
   * the row spacing and every pin falls beneath it.
   */
  @Test
  public void bodyCoversEveryPin() {
    double pinSize = (int) LEDBarGraph.PIN_SIZE.convertToPixels() / 2 * 2;
    for (Orientation orientation : Orientation.values()) {
      for (Segments segments : Segments.values()) {
        LEDBarGraph graph = new LEDBarGraph();
        graph.setOrientation(orientation);
        graph.setSegments(segments);
        Rectangle2D body = graph.getBody().getBounds2D();
        for (int i = 0; i < graph.getControlPointCount(); i++) {
          Point2D pin = graph.getControlPoint(i);
          Rectangle2D pinRect = new Rectangle2D.Double(pin.getX() - pinSize / 2,
              pin.getY() - pinSize / 2, pinSize, pinSize);
          Assert.assertTrue("pin " + i + " is not covered at " + orientation + " " + segments,
              body.contains(pinRect));
        }
      }
    }
  }

  /** DIP numbering: down one side and back up the other, whatever the pin count. */
  @Test
  public void pinsAreNumberedRoundThePackage() {
    for (Segments segments : Segments.values()) {
      LEDBarGraph graph = bar(segments);
      int pins = segments.getPinCount();
      int half = pins / 2;
      Assert.assertEquals("first pin, " + segments, "1", graph.getControlPointNodeName(0));
      Assert.assertEquals("last of near row, " + segments, Integer.toString(half),
          graph.getControlPointNodeName(half - 1));
      Assert.assertEquals("first of far row, " + segments, Integer.toString(pins),
          graph.getControlPointNodeName(half));
      Assert.assertEquals("last pin, " + segments, Integer.toString(half + 1),
          graph.getControlPointNodeName(pins - 1));
    }
  }

  /**
   * Every segment must lie inside the package at every orientation and every count. The field is
   * derived from the body, so an orientation the derivation does not handle shows up here rather
   * than in a drawing nobody checks. {@code getSegmentRects} is private, so this reaches it
   * reflectively rather than widening the class's surface for a test.
   */
  @Test
  public void segmentsStayInsideTheBody() throws Exception {
    java.lang.reflect.Method segmentRects = LEDBarGraph.class.getDeclaredMethod("getSegmentRects");
    segmentRects.setAccessible(true);

    for (Orientation orientation : Orientation.values()) {
      for (Segments segments : Segments.values()) {
        LEDBarGraph graph = new LEDBarGraph();
        graph.setOrientation(orientation);
        graph.setSegments(segments);
        Rectangle2D body = graph.getBody().getBounds2D();
        Rectangle2D[] rects = (Rectangle2D[]) segmentRects.invoke(graph);

        Assert.assertEquals("segment count at " + orientation + " " + segments,
            segments.getCount(), rects.length);
        for (int i = 0; i < rects.length; i++) {
          Assert.assertTrue("segment " + i + " escapes the body at " + orientation + " " + segments,
              body.contains(rects[i]));
          Assert.assertTrue("segment " + i + " has no area at " + orientation + " " + segments,
              rects[i].getWidth() > 0 && rects[i].getHeight() > 0);
        }
      }
    }
  }

  /**
   * Each segment belongs on the pin pair that drives it. This is not implied by
   * {@link #segmentsStayInsideTheBody()}: an earlier version divided the body into equal bars,
   * which left every segment inside the package and still about 5 px off its own pins on a ten
   * segment part. Only a check against the control points catches that.
   */
  @Test
  public void segmentsAreCentredOnTheirPins() throws Exception {
    java.lang.reflect.Method segmentRects = LEDBarGraph.class.getDeclaredMethod("getSegmentRects");
    segmentRects.setAccessible(true);

    for (Orientation orientation : Orientation.values()) {
      for (Segments segments : Segments.values()) {
        LEDBarGraph graph = new LEDBarGraph();
        graph.setOrientation(orientation);
        graph.setSegments(segments);
        Rectangle2D[] rects = (Rectangle2D[]) segmentRects.invoke(graph);

        int count = segments.getCount();
        for (int i = 0; i < count; i++) {
          Point2D near = graph.getControlPoint(i);
          Point2D far = graph.getControlPoint(i + count);
          double pinCentreX = (near.getX() + far.getX()) / 2.0;
          double pinCentreY = (near.getY() + far.getY()) / 2.0;
          String where = "segment " + i + " at " + orientation + " " + segments;
          Assert.assertEquals(where + " is off its pins horizontally", pinCentreX,
              rects[i].getCenterX(), 0.01d);
          Assert.assertEquals(where + " is off its pins vertically", pinCentreY,
              rects[i].getCenterY(), 0.01d);
        }
      }
    }
  }

  /** Neighbouring segments must not touch, or the bar reads as one block rather than a scale. */
  @Test
  public void segmentsDoNotTouchEachOther() throws Exception {
    java.lang.reflect.Method segmentRects = LEDBarGraph.class.getDeclaredMethod("getSegmentRects");
    segmentRects.setAccessible(true);

    for (Segments segments : Segments.values()) {
      LEDBarGraph graph = new LEDBarGraph();
      graph.setSegments(segments);
      Rectangle2D[] rects = (Rectangle2D[]) segmentRects.invoke(graph);
      for (int i = 1; i < rects.length; i++) {
        Assert.assertTrue("segments " + (i - 1) + " and " + i + " overlap at " + segments,
            rects[i].getMinY() >= rects[i - 1].getMaxY() - 0.01d);
      }
    }
  }

  /**
   * The scale is one red at the top, two yellow below it and green for the rest, so the green band
   * grows with the segment count rather than the bands being fixed fractions. Asserted per variant
   * because that proportion is the whole point of the arrangement.
   */
  @Test
  public void segmentColoursRunUpTheScale() throws Exception {
    java.lang.reflect.Method segmentColor =
        LEDBarGraph.class.getDeclaredMethod("getSegmentColor", int.class);
    segmentColor.setAccessible(true);

    for (Segments segments : Segments.values()) {
      LEDBarGraph graph = bar(segments);
      int count = segments.getCount();
      int red = 0;
      int yellow = 0;
      int green = 0;
      for (int i = 0; i < count; i++) {
        Object colour = segmentColor.invoke(graph, i);
        if (LEDBarGraph.SEGMENT_RED.equals(colour)) {
          red++;
        } else if (LEDBarGraph.SEGMENT_YELLOW.equals(colour)) {
          yellow++;
        } else if (LEDBarGraph.SEGMENT_GREEN.equals(colour)) {
          green++;
        } else {
          Assert.fail("segment " + i + " of " + segments + " is none of the three scale colours");
        }
      }
      Assert.assertEquals("red segments on " + segments, 1, red);
      Assert.assertEquals("yellow segments on " + segments, 2, yellow);
      Assert.assertEquals("green segments on " + segments, count - 3, green);
    }
  }

  /** Red belongs at the top of the scale, which is the first segment as the part is drawn. */
  @Test
  public void redIsAtTheTopOfTheScale() throws Exception {
    java.lang.reflect.Method segmentColor =
        LEDBarGraph.class.getDeclaredMethod("getSegmentColor", int.class);
    segmentColor.setAccessible(true);

    LEDBarGraph graph = bar(Segments._10);
    Assert.assertEquals(LEDBarGraph.SEGMENT_RED, segmentColor.invoke(graph, 0));
    Assert.assertEquals(LEDBarGraph.SEGMENT_YELLOW, segmentColor.invoke(graph, 1));
    Assert.assertEquals(LEDBarGraph.SEGMENT_YELLOW, segmentColor.invoke(graph, 2));
    Assert.assertEquals(LEDBarGraph.SEGMENT_GREEN, segmentColor.invoke(graph, 3));
    Assert.assertEquals(LEDBarGraph.SEGMENT_GREEN, segmentColor.invoke(graph, 9));
  }

  /**
   * {@code BomMaker} keys rows on the type name and the value together, so a blank value would
   * merge every variant into one line. The segment count has to reach the value column for an
   * eight, a ten and a twelve segment part to be three rows.
   */
  @Test
  public void everyVariantGetsItsOwnBomRow() {
    java.util.Set<String> values = new java.util.HashSet<String>();
    for (Segments segments : Segments.values()) {
      String value = bar(segments).getValueForDisplay();
      Assert.assertNotNull("value for " + segments, value);
      Assert.assertFalse("value for " + segments + " is blank", value.trim().isEmpty());
      values.add(value);
    }
    Assert.assertEquals("each variant needs a distinct BOM value", Segments.values().length,
        values.size());
  }

  /** A part number the user has typed is the more specific answer, so it is kept and appended. */
  @Test
  public void aTypedPartNumberIsKeptBesideTheVariant() {
    LEDBarGraph graph = bar(Segments._10);
    Assert.assertEquals("10 Segment", graph.getValueForDisplay());

    graph.setValue("LTA-1000G");
    Assert.assertTrue("variant should survive a part number being typed",
        graph.getValueForDisplay().contains("10 Segment"));
    Assert.assertTrue("part number should reach the BOM",
        graph.getValueForDisplay().contains("LTA-1000G"));

    graph.setValue("   ");
    Assert.assertEquals("whitespace is not a part number", "10 Segment",
        graph.getValueForDisplay());
  }

  @Test
  public void transformerAcceptsThisComponent() {
    LEDBarGraphTransformer transformer = new LEDBarGraphTransformer();
    LEDBarGraph graph = new LEDBarGraph();
    Assert.assertTrue("must be rotatable", transformer.canRotate(graph));
    Assert.assertTrue("must be mirrorable", transformer.canMirror(graph));
    Assert.assertFalse("mirroring reorders pins but changes no connection",
        transformer.mirroringChangesCircuit());
  }

  @Test
  public void fourRotationsReturnToStart() {
    LEDBarGraphTransformer transformer = new LEDBarGraphTransformer();
    LEDBarGraph graph = new LEDBarGraph();
    Point2D start =
        new Point2D.Double(graph.getControlPoint(0).getX(), graph.getControlPoint(0).getY());
    Point2D centre = new Point2D.Double(100, 100);
    for (int i = 0; i < 4; i++) {
      transformer.rotate(graph, centre, 1);
    }
    Assert.assertEquals("orientation returns", Orientation.DEFAULT, graph.getOrientation());
    Assert.assertTrue("anchor returns", graph.getControlPoint(0).distance(start) < 0.5d);
  }

  /** Mirroring has to turn the orientation too, or the body faces the way it did before. */
  @Test
  public void mirroringFlipsTheOrientation() {
    LEDBarGraphTransformer transformer = new LEDBarGraphTransformer();
    Point2D centre = new Point2D.Double(100, 100);

    LEDBarGraph horizontal = new LEDBarGraph();
    horizontal.setOrientation(Orientation._90);
    transformer.mirror(horizontal, centre, IComponentTransformer.HORIZONTAL);
    Assert.assertEquals(Orientation._270, horizontal.getOrientation());

    LEDBarGraph vertical = new LEDBarGraph();
    vertical.setOrientation(Orientation.DEFAULT);
    transformer.mirror(vertical, centre, IComponentTransformer.VERTICAL);
    Assert.assertEquals(Orientation._180, vertical.getOrientation());
  }

  @Test
  public void singleColorsRenderUniformSegments() throws Exception {
    java.lang.reflect.Method segmentColor =
        LEDBarGraph.class.getDeclaredMethod("getSegmentColor", int.class);
    segmentColor.setAccessible(true);

    BarColor[] singleColors = new BarColor[] {
        BarColor.RED, BarColor.GREEN, BarColor.YELLOW, BarColor.BLUE, BarColor.ORANGE,
        BarColor.WHITE
    };

    for (BarColor color : singleColors) {
      LEDBarGraph graph = bar(Segments._10);
      graph.setBarColor(color);
      for (int i = 0; i < 10; i++) {
        Assert.assertEquals("segment " + i + " for " + color,
            color.getSegmentColor(i, 10), segmentColor.invoke(graph, i));
      }
    }
  }

  @Test
  public void blueGreenYellowRedDistribution() throws Exception {
    java.lang.reflect.Method segmentColor =
        LEDBarGraph.class.getDeclaredMethod("getSegmentColor", int.class);
    segmentColor.setAccessible(true);

    LEDBarGraph graph10 = bar(Segments._10);
    graph10.setBarColor(BarColor.BLUE_GREEN_YELLOW_RED);
    Assert.assertEquals(LEDBarGraph.SEGMENT_RED, segmentColor.invoke(graph10, 0));
    Assert.assertEquals(LEDBarGraph.SEGMENT_RED, segmentColor.invoke(graph10, 1));
    Assert.assertEquals(LEDBarGraph.SEGMENT_YELLOW, segmentColor.invoke(graph10, 2));
    Assert.assertEquals(LEDBarGraph.SEGMENT_YELLOW, segmentColor.invoke(graph10, 3));
    Assert.assertEquals(LEDBarGraph.SEGMENT_YELLOW, segmentColor.invoke(graph10, 4));
    Assert.assertEquals(LEDBarGraph.SEGMENT_GREEN, segmentColor.invoke(graph10, 5));
    Assert.assertEquals(LEDBarGraph.SEGMENT_GREEN, segmentColor.invoke(graph10, 8));
    Assert.assertEquals(LEDBarGraph.SEGMENT_BLUE, segmentColor.invoke(graph10, 9));

    LEDBarGraph graph4 = bar(Segments._4);
    graph4.setBarColor(BarColor.BLUE_GREEN_YELLOW_RED);
    Assert.assertEquals(LEDBarGraph.SEGMENT_RED, segmentColor.invoke(graph4, 0));
    Assert.assertEquals(LEDBarGraph.SEGMENT_YELLOW, segmentColor.invoke(graph4, 1));
    Assert.assertEquals(LEDBarGraph.SEGMENT_GREEN, segmentColor.invoke(graph4, 2));
    Assert.assertEquals(LEDBarGraph.SEGMENT_BLUE, segmentColor.invoke(graph4, 3));
  }

  @Test
  public void colorVariantDistinguishesBomRow() {
    LEDBarGraph defaultGraph = bar(Segments._10);
    LEDBarGraph redGraph = bar(Segments._10);
    redGraph.setBarColor(BarColor.RED);
    LEDBarGraph bgyrGraph = bar(Segments._10);
    bgyrGraph.setBarColor(BarColor.BLUE_GREEN_YELLOW_RED);

    Assert.assertEquals("10 Segment", defaultGraph.getValueForDisplay());
    Assert.assertEquals("10 Segment, Red", redGraph.getValueForDisplay());
    Assert.assertEquals("10 Segment, Blue-Green-Yellow-Red", bgyrGraph.getValueForDisplay());
  }

  @Test
  public void barColorDefaultsWhenNull() {
    LEDBarGraph graph = new LEDBarGraph();
    graph.setBarColor(null);
    Assert.assertEquals(BarColor.GREEN_YELLOW_RED, graph.getBarColor());
  }

  private static LEDBarGraph bar(Segments segments) {
    LEDBarGraph graph = new LEDBarGraph();
    graph.setSegments(segments);
    return graph;
  }
}
