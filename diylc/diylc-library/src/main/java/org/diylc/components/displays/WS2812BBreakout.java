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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractAddressableLedBoard;
import org.diylc.components.MakerBoardPainter;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "NeoPixel Breakout", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Single-LED Addressable RGB WS2812B NeoPixel Breakout",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BBreakout extends AbstractAddressableLedBoard {

  private static final long serialVersionUID = 1L;

  public static Color NEO_BLACK = Color.decode("#111111");

  // A three-pad column up each short edge, with the two mounting holes on the centre line
  // between them.
  public static Size BOARD_WIDTH = new Size(0.5d, SizeUnit.in);
  public static Size BOARD_HEIGHT = new Size(0.4d, SizeUnit.in);

  // The real board's three-way connectors are drawn as plain pads: at this size a connector body
  // would hide the LED, and what a layout needs is where the three wires land. The pads are flush
  // with their own edge, so how far in their centres sit is half their reach.
  public static Size PAD_WIDTH = new Size(1.5d, SizeUnit.mm);
  public static Size PAD_LENGTH = new Size(1.5d, SizeUnit.mm);

  public static Size MOUNTING_HOLE_SIZE = new Size(2.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SPACING = new Size(0.3d, SizeUnit.in);

  // The two grounds are the same net on the board, as are the two supplies, and are numbered only
  // to keep the node names distinct.
  private static final String[] PIN_NAMES = {
      "GND_1", "VIN_1", "IN",
      "GND_2", "VIN_2", "OUT"
  };

  public WS2812BBreakout() {
    super();
    this.bodyColor = NEO_BLACK;
    updateControlPoints();
  }

  @Override
  protected String getVariantLabel() {
    return getLedType().toString();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    // the columns are flush against their own edges, so what separates them is the board less one
    // whole pad
    double columnSpacing = BOARD_WIDTH.convertToPixels() - PAD_WIDTH.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < 3; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
      relativeOffsets[3 + i][0] = columnSpacing;
      relativeOffsets[3 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  /** The first pad column is flush with this edge, so its centres sit half a pad inboard. */
  private double getBoardX(double x) {
    return x - PAD_WIDTH.convertToPixels() / 2.0;
  }

  /** Derived so that the three-pad column is centred across the height. */
  private double getBoardY(double y) {
    double padSpan = 2 * PIN_SPACING.convertToPixels();
    return y - (BOARD_HEIGHT.convertToPixels() - padSpan) / 2.0;
  }

  Point2D getLedCentre() {
    Point2D p0 = controlPoints[0];
    return new Point2D.Double(getBoardX(p0.getX()) + BOARD_WIDTH.convertToPixels() / 2.0,
        getBoardY(p0.getY()) + BOARD_HEIGHT.convertToPixels() / 2.0);
  }

  /**
   * The holes sit on the centre line astride the LED and are what the board is tightest against:
   * 0.27 mm of board outside each rim and 0.31 mm between the rim and the package.
   */
  Point2D getHoleCentre(boolean bottom) {
    Point2D centre = getLedCentre();
    double offset = MOUNTING_HOLE_SPACING.convertToPixels() / 2.0;
    return new Point2D.Double(centre.getX(), centre.getY() + (bottom ? offset : -offset));
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()),
        BOARD_WIDTH.convertToPixels(), BOARD_HEIGHT.convertToPixels(), 4, 4);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
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

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      for (boolean bottom : new boolean[] {false, true}) {
        Point2D hole = getHoleCentre(bottom);
        MakerBoardPainter.drawMountingHole(g2d, hole.getX(), hole.getY(), holeSize);
      }

      // The 2.35 mm between a pad and the package is not room beside a pad, so there is nowhere a
      // name would go and this face carries no silkscreen. Drawn lit off the family's wheel, so a
      // breakout reads as the same part as one pixel of a stick.
      Point2D led = getLedCentre();
      MakerBoardPainter.drawAddressableLed(g2d, led.getX(), led.getY(),
          RGB_LED_SIZE.convertToPixels(), getLedColors(1)[0], 0, getLedType());
    }

    g2d.setTransform(oldTx);

    drawSurfacePads(g2d, 0, controlPoints.length, PAD_WIDTH, PAD_LENGTH, outlineMode,
        drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // landscape in the board's own 5:4, so the icon does not read as one of the portrait modules
    double rectW = width - 4;
    double rectH = rectW * 0.8;
    double rectX = 2;
    double rectY = (height - rectH) / 2.0;

    g2d.setColor(NEO_BLACK);
    g2d.fill(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 3, 3));
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 3, 3));

    double padW = 3;
    double padH = 2;
    double padStep = 5;
    g2d.setColor(PAD_COLOR);
    for (int i = 0; i < 3; i++) {
      double padY = height / 2.0 - padStep + i * padStep - padH / 2.0;
      g2d.fill(new Rectangle2D.Double(rectX, padY, padW, padH));
      g2d.fill(new Rectangle2D.Double(rectX + rectW - padW, padY, padW, padH));
    }

    double ledSize = Math.min(rectW - 6, 12);
    MakerBoardPainter.drawAddressableLed(g2d, width / 2.0, height / 2.0, ledSize,
        Color.decode("#FF3333"));
  }
}
