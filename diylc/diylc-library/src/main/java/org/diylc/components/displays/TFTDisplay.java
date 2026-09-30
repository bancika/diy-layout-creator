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
import java.awt.Font;
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

@ComponentDescriptor(name = "TFT Touch Screen (ILI9341)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "2.8\" 240x320 Color TFT LCD Display with SPI Interface, Touch, and SD Slot",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class TFTDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color TFT_RED = Color.decode("#C0392B");
  public static Color SCREEN_BG = Color.decode("#111111");
  public static Color GLASS_COLOR = Color.decode("#1A1A1A");

  // The board is taller than it is wide, with the header along the short top edge. Rotation is the
  // Orientation property's job, so the drawing keeps the orientation the module ships in.
  public static Size BOARD_WIDTH = new Size(50.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(86.0d, SizeUnit.mm);
  // the glass runs the full width of the board and sits centred between the two rows of mounting
  // holes, leaving bare PCB for the header above it and a narrow lip below
  public static Size GLASS_LENGTH = new Size(69.1d, SizeUnit.mm);
  // 2.8" diagonal at 4:3, centred within the glass
  public static Size SCREEN_WIDTH = new Size(43.2d, SizeUnit.mm);
  public static Size SCREEN_LENGTH = new Size(57.6d, SizeUnit.mm);
  // clearance from the top edge to the pin row; the outline dimensions are published but this one
  // is not, so it is taken from the pitch of the 2.54mm header
  public static Size HEADER_OFFSET = new Size(3.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_INSET = new Size(3.0d, SizeUnit.mm);
  // the pair nearest the header sits further in to clear the pin row; the other two keep the 3mm
  // inset on all sides
  public static Size MOUNTING_HOLE_HEADER_INSET = new Size(6.92d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(2.5d, SizeUnit.mm);
  public static Size CORNER_RADIUS = new Size(1.5d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      "VCC", "GND", "CS", "RESET", "DC", "MOSI (SDI)", "SCK", "LED", "MISO (SDO)",
      "T_CLK", "T_CS", "T_DIN", "T_DO", "T_IRQ"
  };

  public TFTDisplay() {
    super();
    this.bodyColor = TFT_RED;
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  /** Left edge of the board, derived so that the pin row sits centred on the top edge. */
  private double getBoardX(double x) {
    double spacing = PIN_SPACING.convertToPixels();
    return x - (BOARD_WIDTH.convertToPixels() - (PIN_NAMES.length - 1) * spacing) / 2.0;
  }

  private double getBoardY(double y) {
    return y - HEADER_OFFSET.convertToPixels();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < PIN_NAMES.length; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double radius = CORNER_RADIUS.convertToPixels();
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()),
        BOARD_WIDTH.convertToPixels(), BOARD_HEIGHT.convertToPixels(), radius, radius);
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
      double holeInset = MOUNTING_HOLE_INSET.convertToPixels();
      double headerInset = MOUNTING_HOLE_HEADER_INSET.convertToPixels();
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + headerInset, holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + headerInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardH - holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
          boardY + boardH - holeInset, holeSize);

      // centred between the two rows of mounting holes rather than measured from an edge, which is
      // what keeps it clear of both pairs
      double glassH = GLASS_LENGTH.convertToPixels();
      double glassY = boardY + (headerInset + boardH - holeInset) / 2.0 - glassH / 2.0;
      g2d.setColor(GLASS_COLOR);
      g2d.fill(new Rectangle2D.Double(boardX, glassY, boardW, glassH));

      double screenW = SCREEN_WIDTH.convertToPixels();
      double screenH = SCREEN_LENGTH.convertToPixels();
      double screenX = boardX + (boardW - screenW) / 2.0;
      double screenY = glassY + (glassH - screenH) / 2.0;

      g2d.setColor(SCREEN_BG);
      g2d.fill(new Rectangle2D.Double(screenX, screenY, screenW, screenH));
      g2d.setColor(Color.DARK_GRAY);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new Rectangle2D.Double(screenX, screenY, screenW, screenH));
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(TFT_RED);
    g2d.fill(new RoundRectangle2D.Double(8, 1, width - 16, height - 2, 3, 3));
    g2d.setColor(TFT_RED.darker());
    g2d.draw(new RoundRectangle2D.Double(8, 1, width - 16, height - 2, 3, 3));

    g2d.setColor(SCREEN_BG);
    g2d.fillRect(10, 8, width - 20, height - 12);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "TFT", width / 2, height / 2 + 3, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }
}
