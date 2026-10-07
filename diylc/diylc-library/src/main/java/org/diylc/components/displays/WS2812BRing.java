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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.MakerBoardPainter;
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

@ComponentDescriptor(name = "NeoPixel Ring", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "Addressable RGB WS2812B NeoPixel Ring (12/16/24 LEDs)",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BRing extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NEO_BLACK = Color.decode("#111111");

  // Pads are small and sit near the rim; these are what the ring overrides the inherited solder
  // pad footprint with, the default being sized for a breakout board rather than a ring.
  public static Size PAD_EDGE_INSET = new Size(1.4d, SizeUnit.mm);

  // Clearance between a pad name and the pad itself, measured from the pad's edge so that the gap
  // stays the same if the pad footprint changes.
  public static Size PAD_LABEL_GAP = new Size(0.4d, SizeUnit.mm);

  // The ring is drawn lit: unlit it is a black disc with white specks on it, which reads as no
  // particular part. One palette per variant, off the shared colour wheel.
  public static Color[] LED_COLORS_12 = buildLedGradient(12);
  public static Color[] LED_COLORS_16 = buildLedGradient(16);
  public static Color[] LED_COLORS_24 = buildLedGradient(24);

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
  protected String getVariantLabel() {
    RingSize ringSize = getRingSize();
    return ringSize == null ? null : ringSize.toString();
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] padNames = ringSize.getPadNames();
    if (index >= 0 && index < padNames.length) {
      return padNames[index];
    }
    return Integer.toString(index + 1);
  }

  /** The circle the LED centres sit on: the midpoint between the inner and outer rims. */
  private double getMidRadius() {
    double outerR = new Size(ringSize.getOuterDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    double innerR = new Size(ringSize.getInnerDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    return (outerR + innerR) / 2.0;
  }

  private Color[] getLedColors() {
    switch (ringSize) {
      case _12_LED:
        return LED_COLORS_12;
      case _16_LED:
        return LED_COLORS_16;
      case _24_LED:
        return LED_COLORS_24;
      default:
        // a ring size added later still renders before it is given a palette of its own
        return buildLedGradient(ringSize.getLedCount());
    }
  }

  /**
   * Pads sit out towards the rim rather than on the circle the LEDs occupy. This has to be the one
   * place the radius is decided: {@link #updateControlPoints()} offsets every pad from the first
   * one by it, and {@link #getCenter()} works backwards from the first pad to find the middle of
   * the ring, so the two disagreeing would leave the body drawn away from its own control points.
   */
  private double getPadRadius() {
    double outerR = new Size(ringSize.getOuterDiameterMm() / 2.0, SizeUnit.mm).convertToPixels();
    return outerR - PAD_EDGE_INSET.convertToPixels();
  }

  /**
   * Pads are not grouped: each one sits in a gap between two consecutive LEDs, the gaps being the
   * ones {@link RingSize} records. The sequence is anchored at the gap just past the bottom of the
   * ring and runs clockwise from there; where it starts is arbitrary, since the component rotates.
   */
  private double getPadAngle(int index) {
    int ledCount = ringSize.getLedCount();
    double gap = ringSize.getPadGapIndex(index) + ledCount / 2.0 + 0.5;
    return 2 * Math.PI * gap / ledCount - Math.PI / 2.0;
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double padR = getPadRadius();
    double theta0 = getPadAngle(0);

    int padCount = ringSize.getPadNames().length;
    double[][] relativeOffsets = new double[padCount][2];
    for (int i = 0; i < padCount; i++) {
      double theta = getPadAngle(i);
      relativeOffsets[i][0] = padR * (Math.cos(theta) - Math.cos(theta0));
      relativeOffsets[i][1] = padR * (Math.sin(theta) - Math.sin(theta0));
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  private Point2D getCenter() {
    Point2D p0 = controlPoints[0];
    double padR = getPadRadius();
    double theta0 = getPadAngle(0);
    // unrotated, the centre sits one pad radius back along the first pad's angle
    return new Point2D.Double(p0.getX() - padR * Math.cos(theta0), p0.getY() - padR * Math.sin(theta0));
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
      double ledR = getMidRadius();
      double ledSize = RGB_LED_SIZE.convertToPixels();
      Color[] ledColors = getLedColors();

      // Draw all LEDs around the circular ring, each package turned to face out along its own
      // radius the way it is mounted rather than left square to the board
      for (int i = 0; i < ledCount; i++) {
        double angle = 2 * Math.PI * i / ledCount - Math.PI / 2.0;
        MakerBoardPainter.drawAddressableLed(g2d, cx + ledR * Math.cos(angle),
            cy + ledR * Math.sin(angle), ledSize, ledColors[i % ledColors.length],
            angle + Math.PI / 2.0);
      }

      // Pad names run along their own radius, reading out from the middle of the ring towards the
      // pad they belong to, turned with it the same way the LEDs are. A pad sits in the gap
      // between two consecutive LEDs, and that gap is the only room a name has: standing it square
      // to the board would put it across the LEDs on either side.
      g2d.setColor(SILK_COLOR);
      g2d.setFont(PIN_FONT);
      double labelR =
          getPadRadius() - PAD_SIZE.convertToPixels() / 2.0 - PAD_LABEL_GAP.convertToPixels();
      String[] padNames = ringSize.getPadNames();
      for (int i = 0; i < padNames.length; i++) {
        String label = getSilkPinLabel(i);
        if (label == null || label.isEmpty()) {
          continue;
        }
        double angle = getPadAngle(i);
        double textX = cx + labelR * Math.cos(angle);
        double textY = cy + labelR * Math.sin(angle);

        // the turn maps the text advance direction onto the outward radius, so RIGHT alignment
        // ends the name at the pad and grows it back towards the centre
        AffineTransform oldLabelTx = g2d.getTransform();
        g2d.rotate(angle, textX, textY);
        StringUtils.drawCenteredText(g2d, label, textX, textY, HorizontalAlignment.RIGHT,
            VerticalAlignment.CENTER);
        g2d.setTransform(oldLabelTx);
      }
    }

    g2d.setTransform(oldTx);

    drawPcbSolderPads(g2d, 0, ringSize.getPadNames().length, false, outlineMode, drawingObserver);

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

  public enum RingSize {
    // The rings are dimensioned in inches, so both diameters are exact conversions of the measured
    // figures (1.45 / 0.92, 1.75 / 1.25, 2.58 / 2.06) rather than roundings. LEDs are placed on the
    // midpoint between the two rims. The measured LED circles are 1.16, 1.5 and 2.3 inches, which
    // the midpoint reproduces exactly on the 16-LED ring and misses by 0.64 mm on the 12 and
    // 0.51 mm on the 24 -- accepted deliberately rather than carrying a third diameter per ring.
    // Pads sit in the gaps between consecutive LEDs and are not grouped together, so each ring
    // lists its pads in order around the circle with the number of LEDs that follow each one
    // before the next. The final figure wraps back round to the first pad, which means the gaps
    // must sum to the LED count; the constructor checks that rather than trusting a transcription.
    _12_LED("12 LEDs", 12, 36.83, 23.368,
        new String[] {"OUT", "IN", "GND", "PWR"},
        new int[] {2, 4, 2, 4}),
    _16_LED("16 LEDs", 16, 44.45, 31.75,
        new String[] {"IN", "OUT", "G_1", "G_2", "V+_1", "V+_2"},
        new int[] {2, 5, 1, 4, 1, 3}),
    _24_LED("24 LEDs", 24, 65.532, 52.324,
        new String[] {"OUT", "IN", "G_1", "G_2", "PWR_1", "PWR_2"},
        new int[] {2, 8, 2, 2, 2, 8});

    private final String label;
    private final int ledCount;
    private final double outerDiameterMm;
    private final double innerDiameterMm;
    private final String[] padNames;
    private final int[] padGapIndex;

    RingSize(String label, int ledCount, double outerDiameterMm, double innerDiameterMm,
        String[] padNames, int[] ledsAfterPad) {
      this.label = label;
      this.ledCount = ledCount;
      this.outerDiameterMm = outerDiameterMm;
      this.innerDiameterMm = innerDiameterMm;
      this.padNames = padNames;
      this.padGapIndex = new int[padNames.length];
      int gap = 0;
      for (int i = 0; i < padNames.length; i++) {
        padGapIndex[i] = gap;
        gap += ledsAfterPad[i];
      }
      if (gap != ledCount) {
        throw new IllegalArgumentException(
            label + ": pad gaps sum to " + gap + " but the ring carries " + ledCount + " LEDs");
      }
    }

    @Override public String toString() { return label; }
    public int getLedCount() { return ledCount; }
    public double getOuterDiameterMm() { return outerDiameterMm; }
    public double getInnerDiameterMm() { return innerDiameterMm; }
    public String[] getPadNames() { return padNames; }

    /** Which gap between LEDs the pad at {@code index} sits in, counted from the first pad. */
    public int getPadGapIndex(int index) { return padGapIndex[index]; }
  }
}
