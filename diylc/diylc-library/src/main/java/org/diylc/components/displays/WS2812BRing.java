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
 * along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.displays;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "NeoPixel Ring (WS2812B)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "Addressable RGB WS2812B NeoPixel Ring (12/16/24 LEDs)",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BRing extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum RingSize {
    _12_LED("12 LEDs (37mm OD)", 12, 37.0, 27.0),
    _16_LED("16 LEDs (44.5mm OD)", 16, 44.5, 32.0),
    _24_LED("24 LEDs (66mm OD)", 24, 66.0, 52.0);

    private final String label;
    private final int ledCount;
    private final double outerDiameterMm;
    private final double innerDiameterMm;

    RingSize(String label, int ledCount, double outerDiameterMm, double innerDiameterMm) {
      this.label = label;
      this.ledCount = ledCount;
      this.outerDiameterMm = outerDiameterMm;
      this.innerDiameterMm = innerDiameterMm;
    }

    @Override public String toString() { return label; }
    public int getLedCount() { return ledCount; }
    public double getOuterDiameterMm() { return outerDiameterMm; }
    public double getInnerDiameterMm() { return innerDiameterMm; }
  }

  // Colors shared with WS2812BStick
  public static Color NEO_BLACK = Color.decode("#111111");
  public static Color LED_PACKAGE = Color.decode("#FDFEFE");
  public static Color LED_BORDER = Color.decode("#D0D3D4");
  public static Color LED_DIFFUSER = Color.decode("#EAECEE");
  public static Color DIFFUSER_BORDER = Color.decode("#BDC3C7");
  public static Color CHIP_DOT_COLOR = Color.decode("#333333");
  public static Color SOLDER_PAD_COLOR = Color.decode("#D4AC0D");
  public static Color SOLDER_PAD_BORDER = Color.decode("#9A7D0A");

  private static final String[] PIN_NAMES = {"DIN", "+5V", "GND", "DOUT"};

  private RingSize ringSize = RingSize._16_LED;

  public WS2812BRing() {
    super();
    this.bodyColor = NEO_BLACK;
    updateControlPoints();
  }

  @EditableProperty(name = "Ring Size")
  public RingSize getRingSize() {
    return ringSize;
  }

  public void setRingSize(RingSize ringSize) {
    this.ringSize = ringSize;
    updateControlPoints();
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  private double getMidRadius() {
    double outerR = new Size(ringSize.getOuterDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    double innerR = new Size(ringSize.getInnerDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    return (outerR + innerR) / 2.0;
  }

  private double getPadAngle(int index) {
    double spacing = PIN_SPACING.convertToPixels();
    double midR = getMidRadius();
    double deltaTheta = spacing / midR;
    // 4 pads placed symmetrically at the bottom of the circular ring (near angle PI / 2)
    return Math.PI / 2.0 + (index - 1.5) * deltaTheta;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double midR = getMidRadius();
    double theta0 = getPadAngle(0);

    double[][] relativeOffsets = new double[4][2];
    for (int i = 0; i < 4; i++) {
      double theta = getPadAngle(i);
      relativeOffsets[i][0] = midR * (Math.cos(theta) - Math.cos(theta0));
      relativeOffsets[i][1] = midR * (Math.sin(theta) - Math.sin(theta0));
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  private Point2D getCenter() {
    Point2D p0 = controlPoints[0];
    double midR = getMidRadius();
    double theta0 = getPadAngle(0);
    // When unrotated, center is at p0 - (midR*cos(theta0), midR*sin(theta0))
    return new Point2D.Double(p0.getX() - midR * Math.cos(theta0), p0.getY() - midR * Math.sin(theta0));
  }

  @Override
  public Shape getBodyShape() {
    Point2D center = getCenter();
    double outerR = new Size(ringSize.getOuterDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    return new Ellipse2D.Double(center.getX() - outerR, center.getY() - outerR, outerR * 2, outerR * 2);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    AffineTransform oldTx = g2d.getTransform();
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.toRadians(), x, y);
    }

    Point2D center = getCenter();
    double cx = center.getX();
    double cy = center.getY();

    double outerR = new Size(ringSize.getOuterDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    double innerR = new Size(ringSize.getInnerDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    double midR = (outerR + innerR) / 2.0;

    // Annular ring shape: outer circle minus inner circle
    Area outerArea = new Area(new Ellipse2D.Double(cx - outerR, cy - outerR, outerR * 2, outerR * 2));
    Area innerArea = new Area(new Ellipse2D.Double(cx - innerR, cy - innerR, innerR * 2, innerR * 2));
    outerArea.subtract(innerArea);

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(outerArea);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(outerArea);

    if (!outlineMode) {
      int ledCount = ringSize.getLedCount();
      double ledSize = Math.max(8.0, Math.min(15.0, (2 * Math.PI * midR / ledCount) * 0.60));
      double halfLed = ledSize / 2.0;

      // Draw all LEDs around the circular ring
      for (int i = 0; i < ledCount; i++) {
        double angle = 2 * Math.PI * i / ledCount - Math.PI / 2.0;
        double lx = cx + midR * Math.cos(angle);
        double ly = cy + midR * Math.sin(angle);

        // 5050 White Package
        g2d.setColor(LED_PACKAGE);
        g2d.fill(new RoundRectangle2D.Double(lx - halfLed, ly - halfLed, ledSize, ledSize, 2, 2));
        g2d.setColor(LED_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(0.5f));
        g2d.draw(new RoundRectangle2D.Double(lx - halfLed, ly - halfLed, ledSize, ledSize, 2, 2));

        // Milky phosphor lens
        double diffR = halfLed * 0.7;
        g2d.setColor(LED_DIFFUSER);
        g2d.fill(new Ellipse2D.Double(lx - diffR, ly - diffR, diffR * 2, diffR * 2));
        g2d.setColor(DIFFUSER_BORDER);
        g2d.draw(new Ellipse2D.Double(lx - diffR, ly - diffR, diffR * 2, diffR * 2));

        // Tiny IC dot
        g2d.setColor(CHIP_DOT_COLOR);
        double dotS = Math.max(2.0, diffR * 0.4);
        g2d.fill(new Rectangle2D.Double(lx - dotS / 2.0, ly - dotS / 2.0, dotS, dotS));
      }

      // Center silkscreen text
      g2d.setColor(Color.decode("#666666"));
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "NeoPixel", cx, cy - 6, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, ringSize.getLedCount() + "x LED", cx, cy + 6, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen labels next to the 4 solder pads on the ring
      g2d.setColor(Color.WHITE);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
      String[] labels = new String[] {"DIN", "5V", "GND", "DOUT"};
      for (int i = 0; i < 4; i++) {
        double theta = getPadAngle(i);
        double labelR = innerR + 5;
        double px = cx + labelR * Math.cos(theta);
        double py = cy + labelR * Math.sin(theta);
        StringUtils.drawCenteredText(g2d, labels[i], px, py, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw the 4 solder pads directly on the circular ring PCB
    drawSolderPads(g2d, 0, 4, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int cx = width / 2;
    int cy = height / 2;
    int outerR = width / 2 - 2;
    int innerR = outerR / 2;

    Area outer = new Area(new Ellipse2D.Double(cx - outerR, cy - outerR, outerR * 2, outerR * 2));
    Area inner = new Area(new Ellipse2D.Double(cx - innerR, cy - innerR, innerR * 2, innerR * 2));
    outer.subtract(inner);
    g2d.setColor(NEO_BLACK);
    g2d.fill(outer);
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(outer);

    Color[] rainbow = new Color[] {
        Color.decode("#FF3333"),
        Color.decode("#FFD700"),
        Color.decode("#00E676"),
        Color.decode("#00E5FF"),
        Color.decode("#E040FB"),
        Color.decode("#FF6B35")
    };

    double midR = (outerR + innerR) / 2.0;
    int dotR = Math.max(2, (int)(midR * 0.35));
    int dotCount = 6;

    for (int i = 0; i < dotCount; i++) {
      double angle = 2 * Math.PI * i / dotCount - Math.PI / 2.0;
      double lx = cx + midR * Math.cos(angle);
      double ly = cy + midR * Math.sin(angle);
      g2d.setColor(rainbow[i % rainbow.length]);
      g2d.fill(new Ellipse2D.Double(lx - dotR, ly - dotR, dotR * 2, dotR * 2));
    }
  }
}
