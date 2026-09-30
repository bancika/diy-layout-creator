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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "8x8 LED Matrix (MAX7219)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "MAX7219 Dot LED Matrix Display Module (Cascadable SPI)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class LEDMatrix8x8 extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCB_BLUE = Color.decode("#0055A5");
  public static Color MATRIX_BODY = Color.decode("#1A1A1A");
  public static Color LED_RED = Color.decode("#E53935");
  public static Color LED_OFF = Color.decode("#333333");

  public static Size BOARD_WIDTH = new Size(32.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(50.0d, SizeUnit.mm);
  // the matrix block is very nearly as wide as the board it sits on
  public static Size MATRIX_SIZE = new Size(30.0d, SizeUnit.mm);
  public static Size MATRIX_TOP_MARGIN = new Size(3.8d, SizeUnit.mm);
  // clearance from the bottom edge to the input pin row; the output row sits a board apart from it
  public static Size HEADER_OFFSET = new Size(2.54d, SizeUnit.mm);
  public static Size HEADER_SPACING = new Size(44.0d, SizeUnit.mm);
  public static Size CHIP_MARGIN_X = new Size(2.54d, SizeUnit.mm);
  public static Size CHIP_LENGTH = new Size(5.7d, SizeUnit.mm);
  public static Size CHIP_GAP = new Size(1.9d, SizeUnit.mm);
  public static Size SILK_OFFSET = new Size(2.0d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Input Header (0..4)
      "VCC_IN", "GND_IN", "DIN", "CS_IN", "CLK_IN",
      // Output Header (5..9)
      "VCC_OUT", "GND_OUT", "DOUT", "CS_OUT", "CLK_OUT"
  };

  private Color dotColor = LED_RED;

  public LEDMatrix8x8() {
    super();
    this.bodyColor = PCB_BLUE;
    updateControlPoints();
  }

  @EditableProperty(name = "Dot Color")
  public Color getDotColor() {
    return dotColor;
  }

  public void setDotColor(Color dotColor) {
    this.dotColor = dotColor;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // the input row sits near the bottom edge and the output row the same distance from the top
    double topY = -HEADER_SPACING.convertToPixels();
    for (int i = 0; i < 5; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
      relativeOffsets[5 + i][0] = i * spacing;
      relativeOffsets[5 + i][1] = topY;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  /** Left edge of the board, derived so that the five-pin rows sit centred across it. */
  private double getBoardX(double x) {
    double spacing = PIN_SPACING.convertToPixels();
    return x - (BOARD_WIDTH.convertToPixels() - 4 * spacing) / 2.0;
  }

  private double getBoardY(double y) {
    return y - BOARD_HEIGHT.convertToPixels() + HEADER_OFFSET.convertToPixels();
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()),
        BOARD_WIDTH.convertToPixels(), BOARD_HEIGHT.convertToPixels(), 8, 8);
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
      double matrixSize = MATRIX_SIZE.convertToPixels();
      double matrixX = boardX + (boardW - matrixSize) / 2.0;
      double matrixY = boardY + MATRIX_TOP_MARGIN.convertToPixels();

      g2d.setColor(MATRIX_BODY);
      g2d.fill(new RoundRectangle2D.Double(matrixX, matrixY, matrixSize, matrixSize, 6, 6));

      // Draw 64 circular LED dots
      double dotPitch = matrixSize / 8.0;
      double dotR = dotPitch * 0.7;
      for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {
          double dx = matrixX + col * dotPitch + (dotPitch - dotR) / 2.0;
          double dy = matrixY + row * dotPitch + (dotPitch - dotR) / 2.0;
          // create a demo heart or smile pattern
          boolean on = (row == 0 && (col == 2 || col == 5)) ||
                       (row == 1 && (col == 1 || col == 3 || col == 4 || col == 6)) ||
                       (row == 2 && col >= 1 && col <= 6) ||
                       (row == 3 && col >= 1 && col <= 6) ||
                       (row == 4 && col >= 2 && col <= 5) ||
                       (row == 5 && col >= 3 && col <= 4);
          g2d.setColor(on ? dotColor : LED_OFF);
          g2d.fill(new Ellipse2D.Double(dx, dy, dotR, dotR));
        }
      }

      double chipMarginX = CHIP_MARGIN_X.convertToPixels();
      MakerBoardPainter.drawChip(g2d, boardX + chipMarginX,
          matrixY + matrixSize + CHIP_GAP.convertToPixels(), boardW - 2 * chipMarginX,
          CHIP_LENGTH.convertToPixels(), "MAX7219");

      // each label sits just inside the board from the row it names
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT_SMALL);
      double silkOffset = SILK_OFFSET.convertToPixels();
      double silkX = x + 2 * PIN_SPACING.convertToPixels();
      StringUtils.drawCenteredText(g2d, "IN", silkX, y - silkOffset, HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "OUT", silkX,
          y - HEADER_SPACING.convertToPixels() + silkOffset, HorizontalAlignment.CENTER,
          VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_BLUE);
    g2d.fill(new RoundRectangle2D.Double(4, 2, width - 8, height - 4, 3, 3));
    g2d.setColor(PCB_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(4, 2, width - 8, height - 4, 3, 3));

    // Matrix block
    g2d.setColor(MATRIX_BODY);
    g2d.fillRect(6, 4, width - 12, height - 12);

    // 4x4 sample dots
    g2d.setColor(LED_RED);
    for (int r = 0; r < 4; r++) {
      for (int c = 0; c < 4; c++) {
        g2d.fillOval(9 + c * 4, 7 + r * 4, 2, 2);
      }
    }
  }
}
