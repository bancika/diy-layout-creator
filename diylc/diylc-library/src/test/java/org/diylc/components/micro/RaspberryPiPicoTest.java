package org.diylc.components.micro;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class RaspberryPiPicoTest {

  @Test
  public void testControlPointCountAndNames() {
    RaspberryPiPico pico = new RaspberryPiPico();
    Assert.assertEquals(43, pico.getControlPointCount());

    for (int i = 0; i < pico.getControlPointCount(); i++) {
      String name = pico.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    Assert.assertEquals("GP0", pico.getControlPointNodeName(0));
    Assert.assertEquals("GP1", pico.getControlPointNodeName(1));
    Assert.assertEquals("GP15", pico.getControlPointNodeName(19));
    Assert.assertEquals("VBUS", pico.getControlPointNodeName(20));
    Assert.assertEquals("VSYS", pico.getControlPointNodeName(21));
    Assert.assertEquals("GP16", pico.getControlPointNodeName(39));
    Assert.assertEquals("SWCLK", pico.getControlPointNodeName(40));
    Assert.assertEquals("GND_SWD", pico.getControlPointNodeName(41));
    Assert.assertEquals("SWDIO", pico.getControlPointNodeName(42));
  }

  @Test
  public void testDimensionsAndPinGeometry() {
    RaspberryPiPico pico = new RaspberryPiPico();

    // Board dimensions: 21 mm x 51 mm
    double expectedWidth = new Size(21.0d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(51.0d, SizeUnit.mm).convertToPixels();

    Shape body = pico.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    // GP0 (Pin 0) offset relative to board top-left:
    // X offset: 1.61 mm (symmetrical with right row: (21 - 17.78) / 2 = 1.61 mm)
    // Y offset: 1.37 mm (symmetrical with bottom row: (51 - 48.26) / 2 = 1.37 mm)
    double expectedPin0OffsetX = new Size(1.61d, SizeUnit.mm).convertToPixels();
    double expectedPin0OffsetY = new Size(1.37d, SizeUnit.mm).convertToPixels();

    Point2D p0 = pico.getControlPoint(0); // GP0
    Assert.assertEquals(expectedPin0OffsetX, p0.getX() - bounds.getX(), 0.1);
    Assert.assertEquals(expectedPin0OffsetY, p0.getY() - bounds.getY(), 0.1);

    // GP15 (Pin 19, bottom-most left pin)
    Point2D p19 = pico.getControlPoint(19);
    double expectedPin19OffsetY = expectedPin0OffsetY + 19 * new Size(0.1d, SizeUnit.in).convertToPixels();
    Assert.assertEquals(p0.getX(), p19.getX(), 0.01);
    Assert.assertEquals(expectedPin19OffsetY, p19.getY() - bounds.getY(), 0.1);
    // Symmetrical distance to bottom edge (1.37 mm)
    Assert.assertEquals(expectedPin0OffsetY, bounds.getY() + bounds.getHeight() - p19.getY(), 0.1);

    // VBUS (Pin 20, top-right pin)
    Point2D p20 = pico.getControlPoint(20);
    double rowSpacing = new Size(0.7d, SizeUnit.in).convertToPixels();
    Assert.assertEquals(rowSpacing, p20.getX() - p0.getX(), 0.01);
    Assert.assertEquals(p0.getY(), p20.getY(), 0.01);
    // Symmetrical distance to right edge (1.61 mm)
    Assert.assertEquals(expectedPin0OffsetX, bounds.getX() + bounds.getWidth() - p20.getX(), 0.1);

    // GP16 (Pin 39, bottom-right pin)
    Point2D p39 = pico.getControlPoint(39);
    Assert.assertEquals(p20.getX(), p39.getX(), 0.01);
    Assert.assertEquals(p19.getY(), p39.getY(), 0.01);

    // SWD debug pins (40..42) should be vertically in line with bottom pins (p19 & p39)
    Point2D p40 = pico.getControlPoint(40); // SWCLK
    Point2D p41 = pico.getControlPoint(41); // GND_SWD
    Point2D p42 = pico.getControlPoint(42); // SWDIO

    Assert.assertEquals(p19.getY(), p40.getY(), 0.01);
    Assert.assertEquals(p19.getY(), p41.getY(), 0.01);
    Assert.assertEquals(p19.getY(), p42.getY(), 0.01);

    // GND_SWD (middle pin) should be dead center of the board
    double boardCenterX = bounds.getX() + bounds.getWidth() / 2.0;
    Assert.assertEquals(boardCenterX, p41.getX(), 0.1);

    // SWD pins should have 0.1" spacing
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();
    Assert.assertEquals(spacing, p41.getX() - p40.getX(), 0.01);
    Assert.assertEquals(spacing, p42.getX() - p41.getX(), 0.01);
  }

  @Test
  public void testHeadersProperty() {
    RaspberryPiPico pico = new RaspberryPiPico();
    Assert.assertFalse("Headers should be false by default", pico.getHeaders());

    pico.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", pico.getHeaders());

    pico.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", pico.getHeaders());
  }

  @Test
  public void testVersionProperty() {
    RaspberryPiPico pico = new RaspberryPiPico();
    Assert.assertEquals("Default version should be Pi Pico", RaspberryPiPico.PicoVersion.PICO, pico.getVersion());
    Assert.assertEquals("Pi Pico", RaspberryPiPico.PicoVersion.PICO.toString());
    Assert.assertEquals("Pi Pico W", RaspberryPiPico.PicoVersion.PICO_W.toString());

    pico.setVersion(RaspberryPiPico.PicoVersion.PICO_W);
    Assert.assertEquals("Version should be Pi Pico W after setter", RaspberryPiPico.PicoVersion.PICO_W, pico.getVersion());

    pico.setVersion(RaspberryPiPico.PicoVersion.PICO);
    Assert.assertEquals("Version should be Pi Pico after setter", RaspberryPiPico.PicoVersion.PICO, pico.getVersion());
  }

  @Test
  public void testPicoWDimensionsAndPinGeometry() {
    RaspberryPiPico pico = new RaspberryPiPico();
    pico.setVersion(RaspberryPiPico.PicoVersion.PICO_W);

    Assert.assertEquals(43, pico.getControlPointCount());

    Shape body = pico.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();

    // Board dimensions: 21 mm x 51 mm
    double expectedWidth = new Size(21.0d, SizeUnit.mm).convertToPixels();
    double expectedHeight = new Size(51.0d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedWidth, bounds.getWidth(), 0.1);
    Assert.assertEquals(expectedHeight, bounds.getHeight(), 0.1);

    // GP0 (Pin 0) and GP15 (Pin 19)
    Point2D p0 = pico.getControlPoint(0);
    Point2D p19 = pico.getControlPoint(19);
    Point2D p20 = pico.getControlPoint(20);
    Point2D p39 = pico.getControlPoint(39);

    double expectedPin0OffsetX = new Size(1.61d, SizeUnit.mm).convertToPixels();
    double expectedPin0OffsetY = new Size(1.37d, SizeUnit.mm).convertToPixels();
    Assert.assertEquals(expectedPin0OffsetX, p0.getX() - bounds.getX(), 0.1);
    Assert.assertEquals(expectedPin0OffsetY, p0.getY() - bounds.getY(), 0.1);

    // SWD debug pins (40..42) on Pico W
    Point2D p40 = pico.getControlPoint(40); // SWCLK
    Point2D p41 = pico.getControlPoint(41); // GND_SWD (middle pin)
    Point2D p42 = pico.getControlPoint(42); // SWDIO

    // Center of middle debug pad (p41) lies 19.8mm from bottom edge and 7.38mm from right edge
    double expectedDebugBottomDist = new Size(19.8d, SizeUnit.mm).convertToPixels();
    double expectedDebugRightDist = new Size(7.38d, SizeUnit.mm).convertToPixels();

    double actualDebugBottomDist = bounds.getY() + bounds.getHeight() - p41.getY();
    double actualDebugRightDist = bounds.getX() + bounds.getWidth() - p41.getX();

    Assert.assertEquals("Middle debug pad distance from bottom edge must be 19.8mm",
        expectedDebugBottomDist, actualDebugBottomDist, 0.1);
    Assert.assertEquals("Middle debug pad distance from right edge must be 7.38mm",
        expectedDebugRightDist, actualDebugRightDist, 0.1);

    // SWD pins should be horizontally aligned
    Assert.assertEquals(p41.getY(), p40.getY(), 0.01);
    Assert.assertEquals(p41.getY(), p42.getY(), 0.01);

    // SWD pins should have 0.1" (2.54mm) spacing: p40 (left), p41 (middle), p42 (right)
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();
    Assert.assertEquals("SWCLK (p40) to GND_SWD (p41) spacing must be 0.1 in", spacing, p41.getX() - p40.getX(), 0.01);
    Assert.assertEquals("GND_SWD (p41) to SWDIO (p42) spacing must be 0.1 in", spacing, p42.getX() - p41.getX(), 0.01);
  }

  @Test
  public void testPicoWDrawing() {
    RaspberryPiPico pico = new RaspberryPiPico();
    pico.setVersion(RaspberryPiPico.PicoVersion.PICO_W);

    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(400, 400, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g2d = img.createGraphics();
    g2d.setClip(new java.awt.Rectangle(0, 0, 400, 400));

    org.diylc.core.Project project = new org.diylc.core.Project();
    org.diylc.core.IDrawingObserver observer = new org.diylc.core.IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    // Draw with castellated pads
    pico.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);
    pico.draw(g2d, org.diylc.core.ComponentState.SELECTED, false, project, observer);
    pico.draw(g2d, org.diylc.core.ComponentState.NORMAL, true, project, observer);

    // Draw with pin headers
    pico.setHeaders(true);
    pico.draw(g2d, org.diylc.core.ComponentState.NORMAL, false, project, observer);
    pico.draw(g2d, org.diylc.core.ComponentState.NORMAL, true, project, observer);

    g2d.dispose();
  }
}
