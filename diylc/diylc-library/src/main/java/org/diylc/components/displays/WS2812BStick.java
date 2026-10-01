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
  public static Size PAD_INSET = new Size(2.0d, SizeUnit.mm);

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
    // the two pad columns sit the same distance in from each end of the board
    double columnSpacing = BOARD_WIDTH.convertToPixels() - 2 * PAD_INSET.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < 4; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
      relativeOffsets[4 + i][0] = columnSpacing;
      relativeOffsets[4 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  /** Left edge of the board; the first pad column sits {@code PAD_INSET} in from it. */
  private double getBoardX(double x) {
    return x - PAD_INSET.convertToPixels();
  }

  /** Top edge of the board, derived so that the four-pad column is centred across its width. */
  private double getBoardY(double y) {
    double padSpan = 3 * PIN_SPACING.convertToPixels();
    return y - (BOARD_HEIGHT.convertToPixels() - padSpan) / 2.0;
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = getBoardX(x);
    double boardY = getBoardY(y);

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
      // Eight 5mm packages spread evenly between the two pad columns rather than packed edge to
      // edge. The field has to clear the header block drawPinHeader paints around each pin, which
      // reaches one pin width plus a pixel either side of the control point and is drawn after the
      // LEDs -- starting the field at the pad inset alone put that block over the first and last
      // package. The pad names are printed on the back of the real board, so this face carries no
      // silkscreen.
      double ledSize = RGB_LED_SIZE.convertToPixels();
      double padInset = PAD_INSET.convertToPixels();
      double headerHalf = PIN_SIZE.convertToPixels() + 1;
      double fieldStart = boardX + padInset + headerHalf;
      double fieldEnd = boardX + boardW - padInset - headerHalf;
      double pitch = (fieldEnd - fieldStart - ledSize) / 7.0;

      for (int i = 0; i < 8; i++) {
        MakerBoardPainter.drawAddressableLed(g2d, fieldStart + ledSize / 2.0 + i * pitch,
            boardY + boardH / 2.0, ledSize, LED_COLORS_8[i]);
      }
    }

    g2d.setTransform(oldTx);

    // Draw input and output header pins
    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

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
