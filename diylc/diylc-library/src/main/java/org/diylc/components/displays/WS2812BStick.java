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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
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

@ComponentDescriptor(name = "NeoPixel Stick (8x WS2812B)", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "8-LED Addressable RGB WS2812B NeoPixel Stick",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BStick extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color NEO_BLACK = Color.decode("#111111");
  public static Color LED_PACKAGE = Color.decode("#FDFEFE");
  public static Color LED_BORDER = Color.decode("#D0D3D4");
  public static Color LED_DIFFUSER = Color.decode("#EAECEE");
  public static Color DIFFUSER_BORDER = Color.decode("#BDC3C7");
  public static Color CHIP_DOT_COLOR = Color.decode("#333333");

  public static Font PIN_LABEL_FONT = new Font("SansSerif", Font.BOLD, 9);

  public static Size BOARD_WIDTH = new Size(53.0d, SizeUnit.mm);
  public static Size BOARD_HEIGHT = new Size(10.2d, SizeUnit.mm);

  private static final String[] PIN_NAMES = {
      "DIN", "+5V (In)", "GND (In)", "GND (In)",
      "DOUT", "+5V (Out)", "GND (Out)", "GND (Out)"
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
    double w = BOARD_WIDTH.convertToPixels();

    // 8 pins total:
    // Left input header (4 pins): 0 = DIN, 1 = +5V, 2 = GND, 3 = GND
    // Right output header (4 pins): 4 = DOUT, 5 = +5V, 6 = GND, 7 = GND
    double[][] relativeOffsets = new double[][] {
      { 0, 0 },
      { 0, spacing },
      { 0, spacing * 2 },
      { 0, spacing * 3 },
      { w - 20, 0 },
      { w - 20, spacing },
      { w - 20, spacing * 2 },
      { w - 20, spacing * 3 }
    };

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double boardW = BOARD_WIDTH.convertToPixels();
    double boardH = BOARD_HEIGHT.convertToPixels();
    return new RoundRectangle2D.Double(x - 10, y - 10, boardW, boardH, 4, 4);
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
    double boardX = x - 10;
    double boardY = y - 10;

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
      // 8x 5050 SMD WS2812B RGB LEDs (centered between left and right pin areas)
      double spacing = PIN_SPACING.convertToPixels();
      double ledAreaStartX = boardX + 68;
      double ledAreaEndX = boardX + boardW - 68;
      double ledPitch = (ledAreaEndX - ledAreaStartX) / 7.0;

      for (int i = 0; i < 8; i++) {
        double lx = ledAreaStartX + i * ledPitch;
        double ly = boardY + boardH / 2.0;

        // 5050 White Package (28x28)
        g2d.setColor(LED_PACKAGE);
        g2d.fill(new RoundRectangle2D.Double(lx - 14, ly - 14, 28, 28, 3, 3));
        g2d.setColor(LED_BORDER);
        g2d.draw(new RoundRectangle2D.Double(lx - 14, ly - 14, 28, 28, 3, 3));

        // Circular milky phosphor/lens
        g2d.setColor(LED_DIFFUSER);
        g2d.fill(new Ellipse2D.Double(lx - 10, ly - 10, 20, 20));
        g2d.setColor(DIFFUSER_BORDER);
        g2d.draw(new Ellipse2D.Double(lx - 10, ly - 10, 20, 20));

        // Internal tiny IC dot
        g2d.setColor(CHIP_DOT_COLOR);
        g2d.fill(new Rectangle2D.Double(lx - 2.5, ly - 2.5, 5, 5));
      }

      // Silk Screen Pin Labels — all 4 pins labeled on both sides with clean spacing
      g2d.setColor(Color.WHITE);
      g2d.setFont(PIN_LABEL_FONT);

      // Left Input Header Labels (DIN, 5V, GND, GND)
      String[] inLabels = {"DIN", "5V", "GND", "GND"};
      for (int i = 0; i < 4; i++) {
        StringUtils.drawCenteredText(g2d, inLabels[i], boardX + 23, y + i * spacing,
            HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      }

      // Right Output Header Labels (DOUT, 5V, GND, GND)
      String[] outLabels = {"DOUT", "5V", "GND", "GND"};
      for (int i = 0; i < 4; i++) {
        StringUtils.drawCenteredText(g2d, outLabels[i], boardX + boardW - 23, y + i * spacing,
            HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      }
    }

    g2d.setTransform(oldTx);

    // Draw input and output header pins
    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(NEO_BLACK);
    g2d.fill(new RoundRectangle2D.Double(2, 10, width - 4, height - 20, 2, 2));
    Color[] rainbow = new Color[] { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA };
    for (int i = 0; i < 5; i++) {
      g2d.setColor(rainbow[i]);
      g2d.fill(new Ellipse2D.Double(4 + i * 6, height / 2 - 2, 4, 4));
    }
  }
}
