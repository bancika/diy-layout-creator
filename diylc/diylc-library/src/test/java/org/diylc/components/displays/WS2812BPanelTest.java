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

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.displays.WS2812BPanel.Silk;
import org.diylc.components.maker.MakerBoardTestSupport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Geometry tests for the NeoPixel panel. Dimensions can be checked against the board file they
 * came from, so what is worth pinning down here is the arrangement, which cannot: the two ports
 * are each other turned about the centre of the board, and both the ports and the mounting holes
 * sit in the gaps of the pixel grid rather than on bare margin. Every one of those passes whatever
 * the dimensions are, and fails silently in the drawing if it is wrong.
 */
public class WS2812BPanelTest {

  private static final double PX_PER_MM = 200.0d / 25.4d;

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  private static double px(double millimetres) {
    return millimetres * PX_PER_MM;
  }

  private static final int ORDER = WS2812BPanel.MATRIX_ORDER;

  /** The board file's hole pattern, in millimetres from the middle of the board. */
  private static final double[][] HOLES_MM = new double[][] {
      {-33.02d, 17.78d}, {-33.02d, 0d}, {-33.02d, -17.78d},
      {33.02d, 17.78d}, {33.02d, 0d}, {33.02d, -17.78d},
      {-8.89d, 26.67d}, {8.89d, 26.67d},
      {-8.89d, 0d}, {8.89d, 0d},
      {-8.89d, -26.67d}, {8.89d, -26.67d}};

  /** Where a pixel's centre sits, counted along the data chain, in component coordinates. */
  private static Point2D ledCentre(WS2812BPanel panel, int index) {
    Rectangle2D board = panel.getBodyShape().getBounds2D();
    double pitch = WS2812BPanel.PIXEL_PITCH.convertToPixels();
    double margin = (board.getWidth() - (ORDER - 1) * pitch) / 2.0;
    return new Point2D.Double(board.getX() + margin + (index % ORDER) * pitch,
        board.getY() + margin + (index / ORDER) * pitch);
  }

  @Test
  public void theBoardIsTheSquareItWasMeasuredAs() {
    Rectangle2D board = new WS2812BPanel().getBodyShape().getBounds2D();

    Assert.assertEquals(px(71.12d), board.getWidth(), 0.01d);
    Assert.assertEquals("the board is not square", board.getWidth(), board.getHeight(), 0.01d);
  }

  /**
   * On the 8x8 the board is exactly eight pitches across, which puts a half-pitch of margin
   * outside the outer pixels and lands the port row in the first row gap. That is a property of
   * this particular board rather than of panels in general, so it is asserted rather than relied
   * on: a later panel that does not share it should fail here and be given its own figures.
   */
  @Test
  public void theBoardIsAWholeNumberOfPitchesAcross() {
    Assert.assertEquals(ORDER * WS2812BPanel.PIXEL_PITCH.convertToPixels(),
        WS2812BPanel.BOARD_SIZE.convertToPixels(), 0.01d);
  }

  @Test
  public void thePixelGridIsCentredOnTheBoard() {
    WS2812BPanel panel = new WS2812BPanel();
    Rectangle2D board = panel.getBodyShape().getBounds2D();
    Point2D first = ledCentre(panel, 0);
    Point2D last = ledCentre(panel, ORDER * ORDER - 1);

    Assert.assertEquals("left margin", first.getX() - board.getX(),
        board.getMaxX() - last.getX(), 0.01d);
    Assert.assertEquals("top margin", first.getY() - board.getY(),
        board.getMaxY() - last.getY(), 0.01d);
  }

  /**
   * The chain runs left to right along each row and then back to the left of the next, which is
   * how Adafruit wires this panel and not how the serpentine panels are wired. The colours are
   * handed out along the chain, so getting it wrong would show as a gradient that folds back on
   * itself every row.
   */
  @Test
  public void theChainIsAProgressiveRaster() {
    WS2812BPanel panel = new WS2812BPanel();

    Assert.assertEquals("the row does not advance along the chain", ledCentre(panel, 0).getY(),
        ledCentre(panel, ORDER - 1).getY(), 0.01d);
    Assert.assertEquals("the chain does not return to the left edge",
        ledCentre(panel, 0).getX(), ledCentre(panel, ORDER).getX(), 0.01d);
    Assert.assertEquals("the chain does not step down a row",
        WS2812BPanel.PIXEL_PITCH.convertToPixels(),
        ledCentre(panel, ORDER).getY() - ledCentre(panel, 0).getY(), 0.01d);
  }

  @Test
  public void controlPointsAreTheTwoPortsInOrder() {
    WS2812BPanel panel = new WS2812BPanel();
    Assert.assertEquals(6, panel.getControlPointCount());

    Set<String> names = new HashSet<String>();
    for (int i = 0; i < panel.getControlPointCount(); i++) {
      String name = panel.getControlPointNodeName(i);
      Assert.assertNotNull("pin " + i, name);
      Assert.assertTrue("duplicate node name " + name, names.add(name));
    }
    Assert.assertEquals("DIN", panel.getControlPointNodeName(0));
    Assert.assertEquals("DOUT", panel.getControlPointNodeName(3));
  }

  /**
   * The two ports are each other turned half a turn about the middle of the board, which is what
   * lets panels be butted together and chained. Nothing in the drawing shows this is wrong; the
   * output simply ends up somewhere a wire cannot reach.
   */
  @Test
  public void thePortsAreEachOtherTurnedAboutTheCentre() {
    WS2812BPanel panel = new WS2812BPanel();
    Rectangle2D board = panel.getBodyShape().getBounds2D();

    for (int i = 0; i < 3; i++) {
      Point2D in = panel.getControlPoint(i);
      Point2D out = panel.getControlPoint(3 + i);

      Assert.assertEquals("pad " + i + " x", 2 * board.getCenterX() - in.getX(), out.getX(),
          0.01d);
      Assert.assertEquals("pad " + i + " y", 2 * board.getCenterY() - in.getY(), out.getY(),
          0.01d);
    }
  }

  @Test
  public void portPadsSitAtHeaderPitchAndStayOnTheBoard() {
    WS2812BPanel panel = new WS2812BPanel();
    Rectangle2D board = panel.getBodyShape().getBounds2D();
    double padHalf = AbstractMakerBoard.PAD_SIZE.convertToPixels() / 2.0;

    MakerBoardTestSupport.assertRow(panel, 0, 2);
    MakerBoardTestSupport.assertRow(panel, 3, 5);

    for (int i = 0; i < panel.getControlPointCount(); i++) {
      Point2D pad = panel.getControlPoint(i);
      Assert.assertTrue("pad " + i + " runs off the board",
          board.contains(pad.getX() - padHalf, pad.getY() - padHalf, padHalf * 2, padHalf * 2));
    }
  }

  /**
   * Both ports stand in the gap between the first and second rows of pixels rather than on a bare
   * strip, because this board has no bare strip -- the pixel grid reaches within half a pitch of
   * every edge. A port drawn a row out would land under the packages.
   */
  @Test
  public void portsLieInTheGapBetweenTwoPixelRows() {
    WS2812BPanel panel = new WS2812BPanel();
    double firstRow = ledCentre(panel, 0).getY();
    double secondRow = ledCentre(panel, ORDER).getY();

    Assert.assertEquals("input port", (firstRow + secondRow) / 2.0,
        panel.getControlPoint(0).getY(), 0.01d);
  }

  /**
   * The holes are drilled in the gaps of the pixel grid, which on the 8x8 leaves 3.89 mm of bare
   * board for a 2.8 mm hole. There is no room to be wrong by much, and a hole over a package is
   * only visible in a render.
   */
  @Test
  public void mountingHolesClearThePixelsAndStayOnTheBoard() {
    WS2812BPanel panel = new WS2812BPanel();
    Rectangle2D board = panel.getBodyShape().getBounds2D();
    double holeR = WS2812BPanel.MOUNTING_HOLE_SIZE.convertToPixels() / 2.0;
    double packageHalf = AbstractMakerBoard.RGB_LED_SIZE.convertToPixels() / 2.0;

    // the holes are private to the part, so they are reached the way the drawing reaches them
    for (int i = 0; i < ORDER * ORDER; i++) {
      Point2D led = ledCentre(panel, i);
      Rectangle2D pack = new Rectangle2D.Double(led.getX() - packageHalf,
          led.getY() - packageHalf, packageHalf * 2, packageHalf * 2);

      for (double[] hole : HOLES_MM) {
        double hx = board.getCenterX() + px(hole[0]);
        double hy = board.getCenterY() + px(hole[1]);

        Assert.assertTrue("hole breaches the board edge",
            board.contains(hx - holeR, hy - holeR, holeR * 2, holeR * 2));
        Assert.assertFalse("hole sits under a package",
            pack.intersects(hx - holeR, hy - holeR, holeR * 2, holeR * 2));
      }
    }
  }

  @Test
  public void drawsCleanlyInEveryState() {
    MakerBoardTestSupport.assertDrawsCleanly(new WS2812BPanel());
  }

  @Test
  public void bomValueNamesTheMatrix() {
    Assert.assertEquals("8x8, 64 LEDs, RGB", new WS2812BPanel().getValueForDisplay());
  }

  /**
   * The board prints five lines across its face, every one of them in a gap between two rows of
   * pixels -- the only bare board this part has, since the grid reaches within half a pitch of
   * all four edges. The positions come from the board file, but how much room a line takes is the
   * font's business, so a size change could put lettering under a package or off the edge without
   * anything else noticing.
   */
  @Test
  public void silkscreenClearsThePixelsAndTheBoardEdge() {
    WS2812BPanel panel = new WS2812BPanel();
    Rectangle2D board = panel.getBodyShape().getBounds2D();
    double packageHalf = AbstractMakerBoard.RGB_LED_SIZE.convertToPixels() / 2.0;
    Graphics2D g2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
    FontRenderContext context = g2d.getFontRenderContext();

    try {
      for (Silk silk : WS2812BPanel.SILKSCREEN) {
        // the inked rectangle rather than the line box, since leading and descender space the
        // string does not use cannot collide with anything
        Rectangle2D glyphs =
            silk.getFont().createGlyphVector(context, silk.getText()).getVisualBounds();
        Rectangle2D line = new Rectangle2D.Double(board.getCenterX() + px(silk.getXMm()),
            board.getCenterY() + px(silk.getYMm()) - glyphs.getHeight() / 2.0, glyphs.getWidth(),
            glyphs.getHeight());

        Assert.assertTrue(silk.getText() + " runs off the board",
            board.contains(line.getX(), line.getY(), line.getWidth(), line.getHeight()));

        for (int i = 0; i < ORDER * ORDER; i++) {
          Point2D led = ledCentre(panel, i);
          Assert.assertFalse(silk.getText() + " runs under a package",
              line.intersects(led.getX() - packageHalf, led.getY() - packageHalf,
                  packageHalf * 2, packageHalf * 2));
        }
      }
    } finally {
      g2d.dispose();
    }
  }

  @Test
  public void honoursTheLedPackage() {
    MakerBoardTestSupport.assertAddressableLedTypes(WS2812BPanel::new);
  }
}
