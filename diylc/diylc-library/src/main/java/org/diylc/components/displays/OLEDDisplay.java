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
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "0.96\" OLED Display (SSD1306)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "0.96\" Monochrome 128x64 OLED Display Module (I2C / SPI)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class OLEDDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum OLEDInterface {
    // Labels stay short because they reach the BOM's value column through getVariantLabel; the pin
    // legend they used to carry is already in the node names.
    I2C_4Pin("I2C"),
    SPI_7Pin("SPI");

    private final String label;
    OLEDInterface(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public static Color OLED_BLUE = Color.decode("#004488");
  public static Color GLASS_COLOR = Color.decode("#0D1B2A");
  public static Color GLASS_BORDER_COLOR = Color.decode("#334E68");
  public static Color ACTIVE_AREA_COLOR = Color.decode("#060D15");
  public static Color PIXEL_BLUE = Color.decode("#00D4FF");

  public static Size BOARD_SIZE = new Size(27.0d, SizeUnit.mm);
  // The panel is nearly as wide as the board but much shorter, which leaves bare PCB above it for
  // the header and below it for the lower mounting holes.
  public static Size GLASS_WIDTH = new Size(26.7d, SizeUnit.mm);
  public static Size GLASS_LENGTH = new Size(19.3d, SizeUnit.mm);
  // 128 x 64 pixels on a 0.17mm pitch. The lit area is much smaller than the panel around it and
  // roughly twice as wide as it is tall, which is what makes the part read as a display.
  public static Size ACTIVE_WIDTH = new Size(21.744d, SizeUnit.mm);
  public static Size ACTIVE_LENGTH = new Size(10.864d, SizeUnit.mm);
  // Centre-to-centre in both directions, which is how the hole pattern is specified and what a
  // builder drills to; the inset from the board edge falls out of it.
  public static Size MOUNTING_HOLE_SPACING = new Size(24.0d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(2.0d, SizeUnit.mm);
  // clearance from the top edge to the pin row
  public static Size HEADER_OFFSET = new Size(1.8d, SizeUnit.mm);

  public static final String[] PIN_NAMES_I2C = new String[] {"GND", "VCC", "SCL", "SDA"};
  public static final String[] PIN_NAMES_SPI = new String[] {"GND", "VCC", "D0 (CLK)", "D1 (MOSI)", "RES", "DC", "CS"};

  private OLEDInterface oledInterface = OLEDInterface.I2C_4Pin;

  public OLEDDisplay() {
    super();
    this.bodyColor = OLED_BLUE;
    updateControlPoints();
  }

  @EditableProperty(name = "Interface")
  public OLEDInterface getOledInterface() {
    return oledInterface;
  }

  public void setOledInterface(OLEDInterface oledInterface) {
    this.oledInterface = oledInterface;
    updateControlPoints();
    invalidateCache();
  }

  @Override
  protected String getVariantLabel() {
    OLEDInterface oledInterface = getOledInterface();
    return oledInterface == null ? null : oledInterface.toString();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (oledInterface == OLEDInterface.I2C_4Pin) {
      if (index >= 0 && index < PIN_NAMES_I2C.length) return PIN_NAMES_I2C[index];
    } else {
      if (index >= 0 && index < PIN_NAMES_SPI.length) return PIN_NAMES_SPI[index];
    }
    return "Pin " + (index + 1);
  }

  private int getPinCount() {
    return oledInterface == OLEDInterface.I2C_4Pin ? PIN_NAMES_I2C.length : PIN_NAMES_SPI.length;
  }

  /** Left edge of the board, derived so that the pin row sits centred on the top edge. */
  private double getBoardX(double x) {
    double spacing = PIN_SPACING.convertToPixels();
    return x - (BOARD_SIZE.convertToPixels() - (getPinCount() - 1) * spacing) / 2.0;
  }

  private double getBoardY(double y) {
    return y - HEADER_OFFSET.convertToPixels();
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[getPinCount()][2];
    for (int i = 0; i < relativeOffsets.length; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double boardSizePx = BOARD_SIZE.convertToPixels();
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()), boardSizePx,
        boardSizePx, 8, 8);
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

    double boardSizePx = BOARD_SIZE.convertToPixels();
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
      double holeInset = (boardSizePx - MOUNTING_HOLE_SPACING.convertToPixels()) / 2.0;
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardSizePx - holeInset, boardY + holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardSizePx - holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardSizePx - holeInset,
          boardY + boardSizePx - holeInset, holeSize);

      // The two rows of holes are inset equally, so centring the panel in the board is what centres
      // it between them and keeps it clear of all four.
      double glassW = GLASS_WIDTH.convertToPixels();
      double glassH = GLASS_LENGTH.convertToPixels();
      double glassX = boardX + (boardSizePx - glassW) / 2.0;
      double glassY = boardY + (boardSizePx - glassH) / 2.0;

      g2d.setColor(GLASS_COLOR);
      g2d.fill(new RoundRectangle2D.Double(glassX, glassY, glassW, glassH, 4, 4));
      g2d.setColor(GLASS_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new RoundRectangle2D.Double(glassX, glassY, glassW, glassH, 4, 4));

      double activeW = ACTIVE_WIDTH.convertToPixels();
      double activeH = ACTIVE_LENGTH.convertToPixels();
      g2d.setColor(ACTIVE_AREA_COLOR);
      g2d.fill(new RoundRectangle2D.Double(glassX + (glassW - activeW) / 2.0,
          glassY + (glassH - activeH) / 2.0, activeW, activeH, 2, 2));
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(OLED_BLUE);
    g2d.fill(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 3, 3));
    g2d.setColor(OLED_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 3, 3));

    // Screen
    g2d.setColor(GLASS_COLOR);
    g2d.fillRect(7, 12, width - 14, height - 18);

    g2d.setColor(PIXEL_BLUE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "OLED", width / 2, height / 2 + 3, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    // Pins
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      g2d.fillRect(10 + i * 3, 5, 2, 2);
    }
  }
}
