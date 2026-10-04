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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMakerBoard;
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

@ComponentDescriptor(name = "NeoPixel Stick", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "8-LED Addressable RGB WS2812B NeoPixel Stick",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BStick extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NEO_BLACK = Color.decode("#111111");

  public static Size BOARD_WIDTH = new Size(51.1d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(10.22d, SizeUnit.mm);

  // Surface pads rather than plated holes, and flush with the cut end rather than inset from it,
  // so how far in their centres sit is half their width and not a figure of its own. The width is
  // the reach inward from the edge; the length is what has to fit the 0.1 inch pad pitch.
  public static Size PAD_WIDTH = new Size(3.0d, SizeUnit.mm);
  public static Size PAD_LENGTH = new Size(1.5d, SizeUnit.mm);

  // Air between a pad and the nearest package, as the strip keeps at its cut ends. Spreading the
  // row to the pads' inner edge instead leaves the two exactly touching, which draws as copper
  // against plastic with no board between them and puts one over the other on any rounding.
  public static Size PAD_CLEARANCE = new Size(0.5d, SizeUnit.mm);

  public static Size MOUNTING_HOLE_SIZE = new Size(2.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SPACING = new Size(1.0d, SizeUnit.in);
  public static Size MOUNTING_HOLE_TOP_OFFSET = new Size(2.0d, SizeUnit.mm);

  // Drawn lit, off the same colour wheel as the ring, so the two read as the same family of part.
  public static Color[] LED_COLORS_8 = buildLedGradient(8);

  // Each end carries four pads in the order GND, data, power, GND. Both grounds on an end are the
  // same net on the board, so they are numbered only to keep the node names distinct; the
  // silkscreen prints "GND" for all four.
  private static final String[] PIN_NAMES = {
      "GND_1", "DIN", "+5V_1", "GND_2",
      "GND_3", "DOUT", "+5V_2", "GND_4"
  };

  public WS2812BStick() {
    super();
    this.bodyColor = NEO_BLACK;
    updateControlPoints();
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
    // the two pad columns stand flush against their own ends, so what separates them is the
    // board less one whole pad
    double columnSpacing = BOARD_WIDTH.convertToPixels() - PAD_WIDTH.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < 4; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
      relativeOffsets[4 + i][0] = columnSpacing;
      relativeOffsets[4 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  /** Left edge of the board; the first pad column is flush with it, so its centres sit half a
   * pad inboard. */
  private double getBoardX(double x) {
    return x - PAD_WIDTH.convertToPixels() / 2.0;
  }

  /** Top edge of the board, derived so that the four-pad column is centred across its width. */
  private double getBoardY(double y) {
    double padSpan = 3 * PIN_SPACING.convertToPixels();
    return y - (BOARD_HEIGHT.convertToPixels() - padSpan) / 2.0;
  }

  /**
   * Where the package of the given LED sits. The row is spread evenly between the inner edges of
   * the two pad columns rather than packed edge to edge, and it sits low on the board: the
   * mounting holes take the top of it, so the packages are centred in what is left below them
   * rather than on the board's own middle.
   */
  Point2D getLedCentre(int index) {
    Point2D p0 = controlPoints[0];
    double boardX = getBoardX(p0.getX());
    double boardY = getBoardY(p0.getY());
    double ledSize = RGB_LED_SIZE.convertToPixels();
    double inset = PAD_WIDTH.convertToPixels() + PAD_CLEARANCE.convertToPixels();
    double fieldStart = boardX + inset;
    double fieldEnd = boardX + BOARD_WIDTH.convertToPixels() - inset;
    double pitch = (fieldEnd - fieldStart - ledSize) / (LED_COLORS_8.length - 1);

    return new Point2D.Double(fieldStart + ledSize / 2.0 + index * pitch,
        (getHoleCentre(false).getY() + MOUNTING_HOLE_SIZE.convertToPixels() / 2.0 + boardY
            + BOARD_HEIGHT.convertToPixels()) / 2.0);
  }

  /** One of the two mounting holes, which sit a fixed span apart astride the board's centre. */
  Point2D getHoleCentre(boolean right) {
    Point2D p0 = controlPoints[0];
    double offset = MOUNTING_HOLE_SPACING.convertToPixels() / 2.0;
    return new Point2D.Double(
        getBoardX(p0.getX()) + BOARD_WIDTH.convertToPixels() / 2.0 + (right ? offset : -offset),
        getBoardY(p0.getY()) + MOUNTING_HOLE_TOP_OFFSET.convertToPixels());
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()),
        BOARD_WIDTH.convertToPixels(), BOARD_HEIGHT.convertToPixels(), 4, 4);
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
      for (boolean right : new boolean[] {false, true}) {
        Point2D hole = getHoleCentre(right);
        MakerBoardPainter.drawMountingHole(g2d, hole.getX(), hole.getY(), holeSize);
      }

      // The packages go down before the pads do, so the row starts at the pads' inner edge rather
      // than under them. The pad names are printed on the back of the real board, so this face
      // carries no silkscreen.
      double ledSize = RGB_LED_SIZE.convertToPixels();
      for (int i = 0; i < LED_COLORS_8.length; i++) {
        Point2D led = getLedCentre(i);
        MakerBoardPainter.drawAddressableLed(g2d, led.getX(), led.getY(), ledSize, LED_COLORS_8[i]);
      }
    }

    g2d.setTransform(oldTx);

    // The real stick ships bare and is wired by soldering to its pads, so it carries pads rather
    // than a fitted header. They are surface copper at the cut ends rather than the plated holes
    // the ring and the jewel carry.
    drawSurfacePads(g2d, 0, controlPoints.length, PAD_WIDTH, PAD_LENGTH, outlineMode,
        drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double rectX = 2;
    double rectY = height / 2.0 - 5;
    double rectW = width - 4;
    double rectH = 10;

    g2d.setColor(NEO_BLACK);
    g2d.fill(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 2, 2));
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(new RoundRectangle2D.Double(rectX, rectY, rectW, rectH, 2, 2));

    Color[] rainbow = new Color[] {
        Color.decode("#FF3333"), // Red
        Color.decode("#FFD700"), // Yellow
        Color.decode("#00E676"), // Green
        Color.decode("#00E5FF"), // Cyan
        Color.decode("#E040FB")  // Magenta
    };

    double dotSize = Math.max(3.0, Math.min(4.0, rectH - 4.0));
    double margin = 3.0;
    double step = (rectW - 2 * margin - dotSize) / (rainbow.length - 1);

    for (int i = 0; i < rainbow.length; i++) {
      double dotX = rectX + margin + i * step;
      double dotY = height / 2.0 - dotSize / 2.0;

      // 5050 package mini white backing
      g2d.setColor(Color.WHITE);
      g2d.fill(new Rectangle2D.Double(dotX - 0.5, dotY - 0.5, dotSize + 1, dotSize + 1));

      // RGB LED dot
      g2d.setColor(rainbow[i]);
      g2d.fill(new Ellipse2D.Double(dotX, dotY, dotSize, dotSize));
    }
  }
}
