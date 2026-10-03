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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

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
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Nokia 5110 LCD", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "84x48 graphic LCD module with a PCD8544 controller and an SPI interface",
    instanceNamePrefix = "LCD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class Nokia5110LCD extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCB_BLUE = Color.decode("#15548C");
  public static Color BEZEL_COLOR = LIGHT_METAL_COLOR;
  public static Color BEZEL_BORDER_COLOR = LIGHT_METAL_COLOR.darker();
  public static Color LCD_COLOR = Color.decode("#6B7668");
  public static Color LCD_BORDER_COLOR = LCD_COLOR.darker();

  public static Size BOARD_WIDTH = new Size(43.8d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(45.8d, SizeUnit.mm);
  // The metal frame holding the glass: centred across the board and measured down from the top
  // edge, which is where the asymmetry of the module comes from -- the pin row takes up the strip
  // left at the bottom.
  public static Size BEZEL_WIDTH = new Size(40.0d, SizeUnit.mm);
  public static Size BEZEL_LENGTH = new Size(35.0d, SizeUnit.mm);
  public static Size BEZEL_TOP_OFFSET = new Size(4.5d, SizeUnit.mm);
  // The glass sits lower in the frame than it is centred, so it is placed from the top edge of the
  // board rather than centred in the bezel.
  public static Size DISPLAY_WIDTH = new Size(37.0d, SizeUnit.mm);
  public static Size DISPLAY_LENGTH = new Size(27.0d, SizeUnit.mm);
  public static Size DISPLAY_TOP_OFFSET = new Size(11.0d, SizeUnit.mm);
  // Centre-to-centre in each direction, which is how the pattern is specified and what a builder
  // drills.
  public static Size MOUNTING_HOLE_SPACING_X = new Size(34.5d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SPACING_Y = new Size(41.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(3.0d, SizeUnit.mm);
  // clearance from the bottom edge to the pin row
  public static Size HEADER_OFFSET = new Size(3.5d, SizeUnit.mm);
  public static Size CORNER_RADIUS = new Size(1.5d, SizeUnit.mm);
  // gap between the top left mounting hole and the part name printed beside it
  public static Size SILK_TEXT_GAP = new Size(1.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES =
      new String[] {"RST", "CE", "DC", "DIN", "CLK", "VCC", "BL", "GND"};

  public Nokia5110LCD() {
    super();
    this.bodyColor = PCB_BLUE;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  /** Left edge of the board, derived so that the pin row sits centred on the bottom edge. */
  private double getBoardX(double x) {
    double span = (PIN_NAMES.length - 1) * PIN_SPACING.convertToPixels();
    return x - (BOARD_WIDTH.convertToPixels() - span) / 2.0;
  }

  /** Top edge of the board, which is a whole board above the pin row rather than below it. */
  private double getBoardY(double y) {
    return y - BOARD_LENGTH.convertToPixels() + HEADER_OFFSET.convertToPixels();
  }

  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels();
    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < relativeOffsets.length; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }
    return relativeOffsets;
  }

  @Override
  protected void updateControlPoints() {
    rotatePoints(controlPoints[0], getRelativeOffsets());
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double radius = CORNER_RADIUS.convertToPixels();
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()),
        BOARD_WIDTH.convertToPixels(), BOARD_LENGTH.convertToPixels(), radius, radius);
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_LENGTH.convertToPixels();
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
      double holeInsetX = (boardW - MOUNTING_HOLE_SPACING_X.convertToPixels()) / 2.0;
      double holeInsetY = (boardH - MOUNTING_HOLE_SPACING_Y.convertToPixels()) / 2.0;
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInsetX, boardY + holeInsetY, holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInsetX, boardY + holeInsetY,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInsetX, boardY + boardH - holeInsetY,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInsetX,
          boardY + boardH - holeInsetY, holeSize);

      // The part name goes in the strip above the frame, starting clear of the top left hole and
      // level with it.
      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_LARGE);
      StringUtils.drawCenteredText(g2d, "Nokia 5110",
          boardX + holeInsetX + holeSize / 2.0 + SILK_TEXT_GAP.convertToPixels(),
          boardY + holeInsetY, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

      double bezelW = BEZEL_WIDTH.convertToPixels();
      double bezelH = BEZEL_LENGTH.convertToPixels();
      double bezelX = boardX + (boardW - bezelW) / 2.0;
      double bezelY = boardY + BEZEL_TOP_OFFSET.convertToPixels();

      g2d.setColor(BEZEL_COLOR);
      g2d.fill(new Rectangle2D.Double(bezelX, bezelY, bezelW, bezelH));
      g2d.setColor(BEZEL_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new Rectangle2D.Double(bezelX, bezelY, bezelW, bezelH));

      double displayW = DISPLAY_WIDTH.convertToPixels();
      double displayH = DISPLAY_LENGTH.convertToPixels();
      double displayX = boardX + (boardW - displayW) / 2.0;
      double displayY = boardY + DISPLAY_TOP_OFFSET.convertToPixels();
      double displayRadius = CORNER_RADIUS.convertToPixels();

      g2d.setColor(LCD_COLOR);
      g2d.fill(new RoundRectangle2D.Double(displayX, displayY, displayW, displayH, displayRadius,
          displayRadius));
      g2d.setColor(LCD_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(displayX, displayY, displayW, displayH, displayRadius,
          displayRadius));

      // The module prints its pin names in the strip below the header. They lie flat along the row
      // rather than standing on end across it the way the Arduino boards print theirs, because the
      // strip is 3.5 mm deep and a label stood on end needs 4.7 mm: one label offset plus the
      // length of the longest name.
      drawFlatRowPinLabels(g2d, x, y, getRelativeOffsets(), 0, PIN_NAMES.length, true,
          SILK_COLOR);
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_BLUE);
    g2d.fill(new RoundRectangle2D.Double(2, 1, width - 4, height - 5, 3, 3));
    g2d.setColor(PCB_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 1, width - 4, height - 5, 3, 3));

    g2d.setColor(BEZEL_COLOR);
    g2d.fillRect(4, 3, width - 8, height - 10);

    g2d.setColor(LCD_COLOR);
    g2d.fill(new RoundRectangle2D.Double(5, 7, width - 10, height - 15, 2, 2));

    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      g2d.fillRect(width / 2 - 5 + i * 3, height - 4, 2, 2);
    }
  }
}
