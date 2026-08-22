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

@ComponentDescriptor(name = "Character LCD (16x2 / 20x4)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "HD44780-Compatible Character LCD Display (Parallel / I2C Backpack)",
    instanceNamePrefix = "LCD", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class CharacterLCD extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public enum LCDSize {
    _16x2("16x2 (80x36mm)", 16, 2, 80.0, 36.0),
    _20x4("20x4 (98x60mm)", 20, 4, 98.0, 60.0);

    private final String label;
    private final int cols;
    private final int rows;
    private final double widthMm;
    private final double heightMm;

    LCDSize(String label, int cols, int rows, double widthMm, double heightMm) {
      this.label = label;
      this.cols = cols;
      this.rows = rows;
      this.widthMm = widthMm;
      this.heightMm = heightMm;
    }

    @Override public String toString() { return label; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public double getWidthMm() { return widthMm; }
    public double getHeightMm() { return heightMm; }
  }

  public enum LCDInterface {
    I2C_Backpack("I2C Backpack (4-Pin)"),
    Parallel_16Pin("Parallel HD44780 (16-Pin)");

    private final String label;
    LCDInterface(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public static Color PCB_GREEN = Color.decode("#1B5E20");
  public static Color SCREEN_BG = Color.decode("#1E88E5");
  public static Color SCREEN_TEXT = Color.decode("#FFFFFF");
  public static Color BEZEL_COLOR = Color.decode("#212121");

  public static final String[] PIN_NAMES_I2C = new String[] {"GND", "VCC", "SDA", "SCL"};
  public static final String[] PIN_NAMES_PARALLEL = new String[] {
      "VSS (GND)", "VDD (+5V)", "V0 (Contrast)", "RS", "RW", "E",
      "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7",
      "A (Backlight +)", "K (Backlight -)"
  };

  private LCDSize lcdSize = LCDSize._16x2;
  private LCDInterface lcdInterface = LCDInterface.I2C_Backpack;
  private Color screenColor = SCREEN_BG;

  public CharacterLCD() {
    super();
    this.bodyColor = PCB_GREEN;
    updateControlPoints();
  }

  @EditableProperty(name = "Size")
  public LCDSize getLcdSize() {
    return lcdSize;
  }

  public void setLcdSize(LCDSize lcdSize) {
    this.lcdSize = lcdSize;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Interface")
  public LCDInterface getLcdInterface() {
    return lcdInterface;
  }

  public void setLcdInterface(LCDInterface lcdInterface) {
    this.lcdInterface = lcdInterface;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Backlight Color")
  public Color getScreenColor() {
    return screenColor;
  }

  public void setScreenColor(Color screenColor) {
    this.screenColor = screenColor;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (lcdInterface == LCDInterface.I2C_Backpack) {
      if (index >= 0 && index < PIN_NAMES_I2C.length) return PIN_NAMES_I2C[index];
    } else {
      if (index >= 0 && index < PIN_NAMES_PARALLEL.length) return PIN_NAMES_PARALLEL[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    int pinCount = (lcdInterface == LCDInterface.I2C_Backpack) ? 4 : 16;
    double[][] relativeOffsets = new double[pinCount][2];

    for (int i = 0; i < pinCount; i++) {
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
    double boardW = new Size(lcdSize.getWidthMm(), SizeUnit.mm).convertToPixels();
    double boardH = new Size(lcdSize.getHeightMm(), SizeUnit.mm).convertToPixels();
    double boardX = x - 60;
    double boardY = y - 20;
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, 10, 10);
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

    double boardW = new Size(lcdSize.getWidthMm(), SizeUnit.mm).convertToPixels();
    double boardH = new Size(lcdSize.getHeightMm(), SizeUnit.mm).convertToPixels();

    double boardX = x - 60;
    double boardY = y - 20;

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
      // 4 Mounting holes in corners
      drawMountingHole(g2d, boardX + 20, boardY + 20, 20);
      drawMountingHole(g2d, boardX + 20, boardY + boardH - 20, 20);
      drawMountingHole(g2d, boardX + boardW - 20, boardY + 20, 20);
      drawMountingHole(g2d, boardX + boardW - 20, boardY + boardH - 20, 20);

      // Metal Bezel around screen
      double bezelMarginX = (lcdSize == LCDSize._16x2) ? 45 : 40;
      double bezelW = boardW - 2 * bezelMarginX;
      double bezelX = boardX + bezelMarginX;
      double bezelY = boardY + 45;
      double bezelH = boardH - 65;

      g2d.setColor(BEZEL_COLOR);
      g2d.fill(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 6, 6));

      // LCD Screen area
      double screenMargin = 12;
      double screenW = bezelW - 2 * screenMargin;
      double screenH = bezelH - 2 * screenMargin;
      double screenX = bezelX + screenMargin;
      double screenY = bezelY + screenMargin;

      g2d.setColor(screenColor);
      g2d.fill(new Rectangle2D.Double(screenX, screenY, screenW, screenH));

      // Render character boxes
      g2d.setColor(SCREEN_TEXT);
      g2d.setFont(new Font("Monospaced", Font.BOLD, (lcdSize == LCDSize._16x2) ? 14 : 11));
      String line1 = (lcdSize == LCDSize._16x2) ? "HELLO WORLD! 16x2" : "DIYLC LCD MODULE";
      String line2 = (lcdSize == LCDSize._16x2) ? "DIY LAYOUT CREATOR" : "20x4 CHARACTER LCD";
      StringUtils.drawCenteredText(g2d, line1, screenX + screenW / 2.0, screenY + screenH / 3.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, line2, screenX + screenW / 2.0, screenY + screenH * 2.0 / 3.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(PCB_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));

    // Screen
    g2d.setColor(SCREEN_BG);
    g2d.fillRect(6, 10, width - 12, height - 20);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 6));
    StringUtils.drawCenteredText(g2d, "LCD", width / 2, height / 2 + 1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
