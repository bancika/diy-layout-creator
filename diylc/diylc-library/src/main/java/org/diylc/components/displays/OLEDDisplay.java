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
    I2C_4Pin("I2C (4-Pin: GND, VCC, SCL, SDA)"),
    SPI_7Pin("SPI (7-Pin: GND, VCC, D0, D1, RES, DC, CS)");

    private final String label;
    OLEDInterface(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public static Color OLED_BLUE = Color.decode("#004488");
  public static Color GLASS_COLOR = Color.decode("#0D1B2A");
  public static Color PIXEL_BLUE = Color.decode("#00D4FF");
  public static Color PIXEL_YELLOW = Color.decode("#FFD700");

  public static Size BOARD_SIZE = new Size(27.0d, SizeUnit.mm);

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
  public String getControlPointNodeName(int index) {
    if (oledInterface == OLEDInterface.I2C_4Pin) {
      if (index >= 0 && index < PIN_NAMES_I2C.length) return PIN_NAMES_I2C[index];
    } else {
      if (index >= 0 && index < PIN_NAMES_SPI.length) return PIN_NAMES_SPI[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    int count = (oledInterface == OLEDInterface.I2C_4Pin) ? 4 : 7;
    double[][] relativeOffsets = new double[count][2];

    for (int i = 0; i < count; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardSizePx = BOARD_SIZE.convertToPixels();
    int count = (oledInterface == OLEDInterface.I2C_4Pin) ? 4 : 7;
    double boardX = x - (boardSizePx - (count - 1) * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - 14;
    return new RoundRectangle2D.Double(boardX, boardY, boardSizePx, boardSizePx, 8, 8);
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
    int count = (oledInterface == OLEDInterface.I2C_4Pin) ? 4 : 7;
    double boardX = x - (boardSizePx - (count - 1) * PIN_SPACING.convertToPixels()) / 2.0;
    double boardY = y - 14;

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
      // 4 Corner mounting holes
      drawMountingHole(g2d, boardX + 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + 16, boardY + boardSizePx - 16, 16);
      drawMountingHole(g2d, boardX + boardSizePx - 16, boardY + 16, 16);
      drawMountingHole(g2d, boardX + boardSizePx - 16, boardY + boardSizePx - 16, 16);

      // Glass OLED Panel
      double glassMarginX = 18;
      double glassW = boardSizePx - 2 * glassMarginX;
      double glassH = boardSizePx - 65;
      double glassX = boardX + glassMarginX;
      double glassY = boardY + 45;

      g2d.setColor(GLASS_COLOR);
      g2d.fill(new RoundRectangle2D.Double(glassX, glassY, glassW, glassH, 4, 4));
      g2d.setColor(Color.decode("#334E68"));
      g2d.draw(new RoundRectangle2D.Double(glassX, glassY, glassW, glassH, 4, 4));

      // Display demo graphics (yellow top banner + blue body)
      g2d.setColor(PIXEL_YELLOW);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "SSD1306 128x64", glassX + glassW / 2.0, glassY + 15, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      g2d.setColor(PIXEL_BLUE);
      StringUtils.drawCenteredText(g2d, "OLED DISPLAY", glassX + glassW / 2.0, glassY + glassH / 2.0 + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

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
