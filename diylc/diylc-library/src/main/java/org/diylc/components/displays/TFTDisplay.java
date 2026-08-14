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
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "TFT Touch Screen (ILI9341)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "2.8\" 240x320 Color TFT LCD Display with SPI Interface, Touch, and SD Slot",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.BOARD,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class TFTDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color TFT_RED = Color.decode("#C0392B");
  public static Color SCREEN_BG = Color.decode("#111111");

  public static Size BOARD_WIDTH = new Size(86.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(50.0d, SizeUnit.mm);

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

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[14][2];
    for (int i = 0; i < 14; i++) {
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
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 70;
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

    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    double boardX = x - 70;
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

      // Color Screen Area
      double screenMarginX = 35;
      double screenW = boardW - 2 * screenMarginX - 25;
      double screenH = boardH - 65;
      double screenX = boardX + screenMarginX;
      double screenY = boardY + 45;

      g2d.setColor(SCREEN_BG);
      g2d.fill(new Rectangle2D.Double(screenX, screenY, screenW, screenH));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new Rectangle2D.Double(screenX, screenY, screenW, screenH));

      // Display Demo UI
      g2d.setColor(Color.decode("#2980B9"));
      g2d.fillRect((int) screenX, (int) screenY, (int) screenW, 25);
      g2d.setColor(Color.WHITE);
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "TFT 2.8\" 320x240 SPI", screenX + screenW / 2.0, screenY + 12, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // SD Card holder on right edge
      drawMetalConnector(g2d, boardX + boardW - 28, boardY + 100, 36, 120, "SD");
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(TFT_RED);
    g2d.fill(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));
    g2d.setColor(TFT_RED.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 4, width - 4, height - 8, 3, 3));

    // Screen
    g2d.setColor(SCREEN_BG);
    g2d.fillRect(6, 9, width - 12, height - 16);
    g2d.setColor(Color.decode("#2980B9"));
    g2d.fillRect(6, 9, width - 12, 5);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "TFT", width / 2, height / 2 + 3, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
