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
package org.diylc.components.micro;

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

import java.awt.*;
import java.awt.geom.*;

@ComponentDescriptor(name = "ESP32 DevKitC", category = "Controllers",
    author = "Branislav Stojkovic", description = "ESP32 DevKitC V4 Wi-Fi and Bluetooth Development Board",
    instanceNamePrefix = "MCU", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class ESP32DevKitC extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color ESP_BLACK = Color.decode("#1E1E1E");
  public static Color ANTENNA_COLOR = Color.decode("#DAA520");
  public static Color ANTENNA_TAB_COLOR = Color.decode("#383838");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");
  public static Color SILK_COLOR = Color.WHITE;
  public static Size BOARD_WIDTH = new Size(27.9d, SizeUnit.mm);
  public static Size BOARD_LENGTH = new Size(54.4d, SizeUnit.mm);
  public static Size MAIN_BODY_LENGTH = new Size(48.2d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH = new Size(6.2d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH = new Size(20.0d, SizeUnit.mm);
  public static Size ROW_SPACING = new Size(1.0d, SizeUnit.in);

  public static final String[] PIN_NAMES = new String[] {
      // Left row (pins 0..18, top to bottom)
      "3V3", "EN", "VP (GPIO36)", "VN (GPIO39)", "GPIO34", "GPIO35", "GPIO32", "GPIO33",
      "GPIO25", "GPIO26", "GPIO27", "GPIO14", "GPIO12", "GND1", "GPIO13",
      "D2 (GPIO9)", "D3 (GPIO10)", "CMD (GPIO11)", "5V",
      // Right row (pins 19..37, top to bottom)
      "GND2", "GPIO23", "GPIO22", "TX0 (GPIO1)", "RX0 (GPIO3)", "GPIO21", "GND3",
      "GPIO19", "GPIO18", "GPIO5", "GPIO17", "GPIO16", "GPIO4", "GPIO0", "GPIO2",
      "GPIO15", "D1 (GPIO8)", "D0 (GPIO7)", "CLK (GPIO6)"
  };

  public ESP32DevKitC() {
    super();
    this.bodyColor = ESP_BLACK;
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
    double rowSpacing = ROW_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];
    for (int i = 0; i < 19; i++) {
      relativeOffsets[i][0] = 0;
      relativeOffsets[i][1] = i * spacing;
    }
    for (int i = 0; i < 19; i++) {
      relativeOffsets[19 + i][0] = rowSpacing;
      relativeOffsets[19 + i][1] = i * spacing;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    double rowSpacing = ROW_SPACING.convertToPixels();
    double mainW = BOARD_WIDTH.convertToPixels();
    double mainH = MAIN_BODY_LENGTH.convertToPixels();
    double antennaW = ANTENNA_WIDTH.convertToPixels();
    double antennaH = ANTENNA_LENGTH.convertToPixels();

    double mainX = (x + rowSpacing / 2.0) - mainW / 2.0;
    double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;
    double antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
    double antennaY = mainY - antennaH;

    Area bodyArea = new Area(new RoundRectangle2D.Double(mainX, mainY, mainW, mainH, 8, 8));
    bodyArea.add(new Area(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH + 1)));
    return bodyArea;
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

    double rowSpacing = ROW_SPACING.convertToPixels();
    double mainW = BOARD_WIDTH.convertToPixels();
    double mainH = MAIN_BODY_LENGTH.convertToPixels();
    double antennaW = ANTENNA_WIDTH.convertToPixels();
    double antennaH = ANTENNA_LENGTH.convertToPixels();

    double mainX = (x + rowSpacing / 2.0) - mainW / 2.0;
    double mainY = (y + 18 * PIN_SPACING.convertToPixels() / 2.0) - mainH / 2.0;
    double antennaX = (x + rowSpacing / 2.0) - antennaW / 2.0;
    double antennaY = mainY - antennaH;

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    if (outlineMode) {
      g2d.setColor(Constants.TRANSPARENT_COLOR);
      g2d.fill(boardShape);
    } else {
      // Main board body
      g2d.setColor(bodyColor);
      g2d.fill(new RoundRectangle2D.Double(mainX, mainY, mainW, mainH, 8, 8));

      // Antenna tab in slightly lighter color
      Color antennaColor = new Color(
          Math.min(255, bodyColor.getRed() + 32),
          Math.min(255, bodyColor.getGreen() + 32),
          Math.min(255, bodyColor.getBlue() + 32)
      );
      g2d.setColor(antennaColor);
      g2d.fill(new Rectangle2D.Double(antennaX, antennaY, antennaW, antennaH));
    }
    drawingObserver.stopTracking();

    // Outline border around the full board
    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    if (!outlineMode) {
      // Antenna trace (gold/copper serpentine PCB trace)
      g2d.setColor(ANTENNA_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
      Path2D.Double antPath = new Path2D.Double();
      double antPadX = antennaX + 16;
      double antPadW = antennaW - 32;
      double antTopY = antennaY + 10;
      double antMidY = antennaY + antennaH - 12;
      antPath.moveTo(antPadX, antMidY);
      antPath.lineTo(antPadX, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.25, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.25, antMidY);
      antPath.lineTo(antPadX + antPadW * 0.50, antMidY);
      antPath.lineTo(antPadX + antPadW * 0.50, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.75, antTopY);
      antPath.lineTo(antPadX + antPadW * 0.75, antMidY);
      antPath.lineTo(antPadX + antPadW, antMidY);
      antPath.lineTo(antPadX + antPadW, antTopY);
      g2d.draw(antPath);

      // Micro-USB Jack at bottom
      double usbW = 58;
      double usbH = 34;
      double usbX = (x + rowSpacing / 2.0) - usbW / 2.0;
      double usbY = mainY + mainH - 22;
      drawMetalConnector(g2d, usbX, usbY, usbW, usbH, "USB");

      // ESP32-WROOM-32 metal shield module below antenna
      double shieldW = new Size(18.0d, SizeUnit.mm).convertToPixels();
      double shieldH = new Size(18.0d, SizeUnit.mm).convertToPixels();
      double shieldX = (x + rowSpacing / 2.0) - shieldW / 2.0;
      double shieldY = mainY + 10;
      drawMetalConnector(g2d, shieldX, shieldY, shieldW, shieldH, "ESP32-WROOM-32");

      // EN & BOOT tactile buttons at bottom (flanking the Micro-USB port)
      double btnW = 20;
      double btnH = 20;
      double btnY = mainY + mainH - 25;
      double btnLeftX = mainX + 38;
      double btnRightX = mainX + mainW - 38 - btnW;

      g2d.setColor(BUTTON_BODY_COLOR);
      g2d.fill(new RoundRectangle2D.Double(btnLeftX, btnY, btnW, btnH, 3, 3));
      g2d.fill(new RoundRectangle2D.Double(btnRightX, btnY, btnW, btnH, 3, 3));
      g2d.setColor(BUTTON_BORDER_COLOR);
      g2d.draw(new RoundRectangle2D.Double(btnLeftX, btnY, btnW, btnH, 3, 3));
      g2d.draw(new RoundRectangle2D.Double(btnRightX, btnY, btnW, btnH, 3, 3));

      // Button actuators
      g2d.setColor(BUTTON_ACTUATOR_COLOR);
      g2d.fillOval((int) (btnLeftX + btnW / 2.0 - 4), (int) (btnY + btnH / 2.0 - 4), 8, 8);
      g2d.fillOval((int) (btnRightX + btnW / 2.0 - 4), (int) (btnY + btnH / 2.0 - 4), 8, 8);

      g2d.setColor(SILK_COLOR);
      g2d.setFont(SILK_FONT_SMALL);
      StringUtils.drawCenteredText(g2d, "EN", btnLeftX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, "BOOT", btnRightX + btnW / 2.0, btnY - 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

      // Silkscreen
      g2d.setFont(SILK_FONT);
      StringUtils.drawCenteredText(g2d, "ESP32", x + rowSpacing / 2.0, mainY + shieldH + 45, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    g2d.setTransform(oldTx);

    drawPins(g2d, 0, controlPoints.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(ESP_BLACK);
    g2d.fill(new RoundRectangle2D.Double(5, 6, width - 10, height - 8, 3, 3));

    // Antenna tab
    g2d.setColor(ANTENNA_TAB_COLOR);
    g2d.fillRect(8, 2, width - 16, 5);
    g2d.setColor(Color.GRAY);
    g2d.draw(new RoundRectangle2D.Double(5, 6, width - 10, height - 8, 3, 3));

    // Metal shield
    g2d.setColor(METAL_SHIELD_COLOR);
    g2d.fillRect(8, 9, width - 16, 10);

    g2d.setColor(SILK_COLOR);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "ESP32", width / 2, height / 2 + 8, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }
}
