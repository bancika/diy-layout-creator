package org.diylc.components.micro;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.diylc.common.Orientation;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
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
  public void testDrawingAndOutline() {
    RaspberryPiPico pico = new RaspberryPiPico();
    pico.setControlPoint(new Point2D.Double(200, 200), 0);

    BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 600, 600));

    Project project = new Project();
    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    pico.draw(g2d, ComponentState.NORMAL, false, project, observer);
    pico.draw(g2d, ComponentState.SELECTED, false, project, observer);
    pico.draw(g2d, ComponentState.NORMAL, true, project, observer);
    pico.drawIcon(g2d, 32, 32);

    g2d.dispose();
  }

  @Test
  public void testRotation() {
    RaspberryPiPico pico = new RaspberryPiPico();
    pico.setControlPoint(new Point2D.Double(200, 200), 0);

    Point2D p0Initial = pico.getControlPoint(0);
    Point2D p1Initial = pico.getControlPoint(1);

    pico.setOrientation(Orientation._90);
    Point2D p0Rot = pico.getControlPoint(0);
    Point2D p1Rot = pico.getControlPoint(1);

    Assert.assertEquals(p0Initial.getX(), p0Rot.getX(), 0.01);
    Assert.assertEquals(p0Initial.getY(), p0Rot.getY(), 0.01);
    // After 90 deg clockwise rotation, p1 (which was at (x, y + spacing)) should be at (x - spacing, y)
    double spacing = new Size(0.1d, SizeUnit.in).convertToPixels();
    Assert.assertEquals(p0Rot.getX() - spacing, p1Rot.getX(), 0.01);
    Assert.assertEquals(p0Rot.getY(), p1Rot.getY(), 0.01);
  }

  @Test
  public void testHeadersProperty() {
    RaspberryPiPico pico = new RaspberryPiPico();
    Assert.assertFalse("Headers should be false by default", pico.getHeaders());

    pico.setHeaders(true);
    Assert.assertTrue("Headers should be true after setter", pico.getHeaders());

    pico.setHeaders(false);
    Assert.assertFalse("Headers should be false after setter", pico.getHeaders());

    // Test drawing with headers true and false
    BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 600, 600));
    Project project = new Project();
    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    pico.setHeaders(false);
    pico.draw(g2d, ComponentState.NORMAL, false, project, observer);

    pico.setHeaders(true);
    pico.draw(g2d, ComponentState.NORMAL, false, project, observer);

    g2d.dispose();
  }
}
