package org.diylc.components.micro;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import org.diylc.common.Orientation;
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.junit.Assert;
import org.junit.Test;

public class ArduinoUnoTest {

  @Test
  public void testControlPointCountAndNames() {
    ArduinoUno uno = new ArduinoUno();
    Assert.assertEquals(44, uno.getControlPointCount());

    for (int i = 0; i < uno.getControlPointCount(); i++) {
      String name = uno.getControlPointNodeName(i);
      Assert.assertNotNull("Pin " + i + " name should not be null", name);
      Assert.assertFalse("Pin " + i + " name should not be empty", name.trim().isEmpty());
    }

    // Power header
    Assert.assertEquals("NC", uno.getControlPointNodeName(0));
    Assert.assertEquals("IOREF", uno.getControlPointNodeName(1));
    Assert.assertEquals("RESET", uno.getControlPointNodeName(2));
    Assert.assertEquals("3.3V", uno.getControlPointNodeName(3));
    Assert.assertEquals("5V", uno.getControlPointNodeName(4));
    Assert.assertEquals("GND1", uno.getControlPointNodeName(5));
    Assert.assertEquals("GND2", uno.getControlPointNodeName(6));
    Assert.assertEquals("VIN", uno.getControlPointNodeName(7));

    // Analog header
    Assert.assertEquals("A0", uno.getControlPointNodeName(8));
    Assert.assertEquals("A5", uno.getControlPointNodeName(13));

    // Digital low header
    Assert.assertEquals("D0 (RX)", uno.getControlPointNodeName(14));
    Assert.assertEquals("D7", uno.getControlPointNodeName(21));

    // Digital high header
    Assert.assertEquals("D8", uno.getControlPointNodeName(22));
    Assert.assertEquals("SCL", uno.getControlPointNodeName(31));

    // ICSP header
    Assert.assertEquals("MISO", uno.getControlPointNodeName(32));
    Assert.assertEquals("GND_ICSP", uno.getControlPointNodeName(37));

    // Top-left ICSP header (16U2)
    Assert.assertEquals("MISO_16U2", uno.getControlPointNodeName(38));
    Assert.assertEquals("GND_16U2", uno.getControlPointNodeName(43));
  }

  @Test
  public void testPinGeometryAndGaps() {
    ArduinoUno uno = new ArduinoUno();

    // Power header spacing (20px per pin)
    for (int i = 0; i < 7; i++) {
      Point2D p1 = uno.getControlPoint(i);
      Point2D p2 = uno.getControlPoint(i + 1);
      Assert.assertEquals(20.0, p1.distance(p2), 0.01);
      Assert.assertEquals(p1.getY(), p2.getY(), 0.01);
    }

    // Gap between VIN (Pin 7) and A0 (Pin 8) is 0.2" = 40px
    Point2D pVin = uno.getControlPoint(7);
    Point2D pA0 = uno.getControlPoint(8);
    Assert.assertEquals(40.0, pVin.distance(pA0), 0.01);

    // D0 aligns with A5 (dx = 280 from NC)
    Point2D pD0 = uno.getControlPoint(14);
    Point2D pA5 = uno.getControlPoint(13);
    Assert.assertEquals(pA5.getX(), pD0.getX(), 0.01);

    // D7 aligns with VIN (dx = 140 from NC)
    Point2D pD7 = uno.getControlPoint(21);
    Assert.assertEquals(pVin.getX(), pD7.getX(), 0.01);

    // Gap between D7 (Pin 21) and D8 (Pin 22) is 0.16" = 32px
    Point2D pD8 = uno.getControlPoint(22);
    Assert.assertEquals(32.0, pD7.distance(pD8), 0.01);

    // Top-left ICSP header: 2 rows of 3 pins
    Point2D pIcsp2_0 = uno.getControlPoint(38);
    Point2D pIcsp2_1 = uno.getControlPoint(39);
    Assert.assertEquals(20.0, pIcsp2_0.distance(pIcsp2_1), 0.01);
  }

  @Test
  public void testBodyShapeAndRendering() throws Exception {
    ArduinoUno uno = new ArduinoUno();

    Shape body = uno.getBodyShape();
    Assert.assertNotNull(body);
    Rectangle2D bounds = body.getBounds2D();
    Assert.assertEquals(539.78, bounds.getWidth(), 1.0);
    Assert.assertEquals(419.78, bounds.getHeight(), 1.0);

    BufferedImage img = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 800, 800));

    Project project = new Project();
    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    g2d.translate(250, 450);
    uno.draw(g2d, ComponentState.NORMAL, false, project, observer);

    File outDir = new File("/Users/branislavstojkovic/.gemini/antigravity-cli/brain/26ea8af1-303a-4fb2-ba45-9eb1a8e27129/scratch");
    outDir.mkdirs();
    File outFile = new File(outDir, "arduino_uno_rendered.png");
    ImageIO.write(img, "PNG", outFile);
    g2d.dispose();

    // Render Icon (32x32)
    BufferedImage iconImg = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D iconG2d = iconImg.createGraphics();
    iconG2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
    uno.drawIcon(iconG2d, 32, 32);
    iconG2d.dispose();
    ImageIO.write(iconImg, "PNG", new File(outDir, "arduino_uno_icon.png"));
  }
}
